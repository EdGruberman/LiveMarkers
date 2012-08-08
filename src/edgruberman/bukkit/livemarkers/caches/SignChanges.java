package edgruberman.bukkit.livemarkers.caches;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import edgruberman.bukkit.livemarkers.KnownMarkers;

/** newly created, modified, and removed signs */
public class SignChanges extends MarkerCache implements Listener {

    private long clean;
    private File storage;
    private final Map<String, SignFilter> filters = new HashMap<String, SignFilter>();
    private final Map<String, List<String>> worlds = new HashMap<String, List<String>>();

    private long lastCleaning = -1;
    private Set<SignChange> signChanges = new HashSet<SignChange>();

    @Override
    public String getId() {
        return KnownMarkers.SIGN_CHANGE.id;
    }

    @Override
    public void load(final ConfigurationSection config) {
        if (config == null) throw new RuntimeException("Configuration section missing");

        this.clean = config.getLong("clean", Long.MAX_VALUE);
        if (this.clean <= 0) this.clean = Long.MAX_VALUE;

        this.storage = new File(config.getString("storage"));
        if (!this.storage.isAbsolute()) this.storage = new File(this.writer.plugin.getDataFolder(), this.storage.getPath()); // Relative paths should be from the plugin data folder
        this.writer.plugin.getLogger().config(this.getClass().getName() + " Storage: " + this.storage.getPath());
        if (this.storage.isFile()) this.loadStorage(); else this.saveStorage();

        final ConfigurationSection configFilters = config.getConfigurationSection("filters");
        if (configFilters != null) {
            for (final String group : configFilters.getKeys(false)) {
                final ConfigurationSection configGroup = configFilters.getConfigurationSection(group);
                try {
                    this.filters.put(group, new SignFilter(configGroup.getString("text1"), configGroup.getString("text2"), configGroup.getString("text3"), configGroup.getString("text4")));
                } catch (final PatternSyntaxException e) {
                    this.writer.plugin.getLogger().warning("Pattern syntax exception: " + e.getPattern() + "; " + configGroup.getCurrentPath() + "; " + e.getDescription());
                }
            }
        }

        final ConfigurationSection configWorlds = config.getConfigurationSection("worlds");
        if (configWorlds != null) {
            for (final String world : configWorlds.getKeys(false)) {
                if (!this.worlds.containsKey(world)) this.worlds.put(world, new ArrayList<String>());
                final List<String> worldFilters = configWorlds.getStringList(world);
                if (worldFilters == null) continue;

                for (final String worldFilter : worldFilters) {
                    if (!this.filters.containsKey(worldFilter)) {
                        this.writer.plugin.getLogger().warning("Filter not defined: " + worldFilter + "; Path: " + configWorlds.getCurrentPath() + "." + world);
                        continue;
                    }

                    this.worlds.get(world).add(worldFilter);
                }
            }
        }

        this.writer.plugin.getServer().getPluginManager().registerEvents(this, this.writer.plugin);
    }

    @Override
    public boolean isStale() {
        if (System.currentTimeMillis() - this.lastCleaning > this.clean) this.clean();
        return this.stale;
    }

    @Override
    public Void call() {
        final String timestamp = this.writer.timestamp.format(new Date());

        this.markers.clear();
        for (final SignChange change: this.signChanges) {
            final Map<String, Object> marker = new HashMap<String, Object>();
            marker.put("id", this.getId());
            marker.put("group", change.filterGroup);
            if (change instanceof SignChange) {
                marker.put("text1", change.lines[0]);
                marker.put("text2", change.lines[1]);
                marker.put("text3", change.lines[2]);
                marker.put("text4", change.lines[3]);
            } else {
                marker.put("removal", true);
            }
            marker.put("changed", this.writer.timestamp.format(change.changed));
            marker.put("world", change.location.world);
            marker.put("x", change.location.x);
            marker.put("y", change.location.y);
            marker.put("z", change.location.z);
            marker.put("timestamp", timestamp);
            this.markers.add(marker);
        }

        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(final SignChangeEvent changing) {
        final List<String> worldFilters = this.worlds.get(changing.getBlock().getWorld().getName());
        if (worldFilters == null) return;

        final String[] lines = changing.getLines();
        for (final String filterName : worldFilters) {
            if (this.filters.get(filterName).accepts(lines)) {
                this.signChanges.add(new SignChange(filterName, changing.getBlock().getLocation(), System.currentTimeMillis(), lines));
                this.stale = true;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent breaking) {
        if (breaking.getBlock().getTypeId() != Material.WALL_SIGN.getId() && breaking.getBlock().getTypeId() != Material.SIGN_POST.getId()) return;

        final List<String> worldFilters = this.worlds.get(breaking.getBlock().getWorld().getName());
        if (worldFilters == null) return;

        final String[] lines = ((org.bukkit.block.Sign) breaking.getBlock().getState()).getLines();
        for (final String filterName : worldFilters) {
            if (this.filters.get(filterName).accepts(lines)) {
                this.signChanges.add(new SignRemoval(filterName, breaking.getBlock().getLocation(), System.currentTimeMillis()));
                this.stale = true;
            }
        }
    }


    @Override
    public void clear() {
        this.saveStorage();
        this.filters.clear();
        this.worlds.clear();
        this.signChanges.clear();
        super.clear();
    }

    @Override
    public void clean() {
        this.lastCleaning = System.currentTimeMillis();
        if (this.signChanges.size() > 0) {
            this.writer.plugin.getLogger().fine("Flushing " + this.signChanges.size() + " markers from cache: " + this.getClass().getName());
            this.signChanges.clear();
        }
        super.clean();
    }

    @SuppressWarnings("unchecked")
    private void loadStorage() {
        try {
            final ObjectInputStream in = new ObjectInputStream(new FileInputStream(this.storage));
            this.signChanges = (Set<SignChange>) in.readObject();
            this.lastCleaning = in.readLong();
            in.close();
        } catch (final Exception e) {
            throw new RuntimeException("Unable to load storage file: " + this.storage.getPath() + "; " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private void saveStorage() {
        if (!this.storage.getParentFile().exists()) this.storage.getParentFile().mkdirs();

        try {
            final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(this.storage));
            out.writeObject(this.signChanges);
            out.writeLong(this.lastCleaning);
            out.close();
        } catch (final Exception e) {
            this.writer.plugin.getLogger().severe("Unable to save storage file: " + this.storage.getPath() + "; " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private static class SignChange implements Serializable {
        private static final long serialVersionUID = 1;

        public final String filterGroup;
        public final SimpleLocation location;
        public final long changed;
        public final String[] lines;

        private SignChange(final String filterGroup, final Location location, final long changed, final String[] lines) {
            this.filterGroup = filterGroup;
            this.location = new SimpleLocation(location);
            this.changed = changed;
            this.lines = lines;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.filterGroup == null) ? 0 : this.filterGroup.hashCode());
            result = prime * result + ((this.location == null) ? 0 : this.location.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (this.getClass() != obj.getClass()) return false;
            final SignChange other = (SignChange) obj;
            if (this.filterGroup == null) {
                if (other.filterGroup != null) return false;
            } else if (!this.filterGroup.equals(other.filterGroup)) return false;
            if (this.location == null) {
                if (other.location != null) return false;
            } else if (!this.location.equals(other.location)) return false;
            return true;
        }

    }

    private static class SignRemoval extends SignChange implements Serializable {
        private static final long serialVersionUID = 1;

        private SignRemoval(final String filterGroup, final Location location, final long changed) {
            super(filterGroup, location, changed, null);
        }

    }

    private static class SimpleLocation implements Serializable {
        private static final long serialVersionUID = 1;

        public final String world;
        public final double x, y, z;

        private SimpleLocation(final String world, final double x, final double y, final double z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private SimpleLocation(final Location location) {
            this(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.world == null) ? 0 : this.world.hashCode());
            long temp;
            temp = Double.doubleToLongBits(this.x);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(this.y);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(this.z);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (this.getClass() != obj.getClass()) return false;
            final SimpleLocation other = (SimpleLocation) obj;
            if (this.world == null) {
                if (other.world != null) return false;
            } else if (!this.world.equals(other.world)) return false;
            if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) return false;
            if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) return false;
            if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z)) return false;
            return true;
        }

    }

    private static class SignFilter {

        public final Pattern[] text = new Pattern[4];

        private SignFilter(final String text1, final String text2, final String text3, final String text4) throws PatternSyntaxException {
            this.text[0] = (text1 != null ? Pattern.compile(text1) : null);
            this.text[1] = (text2 != null ? Pattern.compile(text2) : null);
            this.text[2] = (text3 != null ? Pattern.compile(text3) : null);
            this.text[3] = (text4 != null ? Pattern.compile(text4) : null);
        }

        private boolean accepts(final String[] lines) {
            for (int i = 0; i <= 3; i++)
                if (this.text[i] != null && !this.text[i].matcher(lines[i]).find())
                    return false;

            return true;
        }

    }

}
