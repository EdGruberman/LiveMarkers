package edgruberman.bukkit.livemarkers.caches;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import edgruberman.bukkit.livemarkers.KnownMarkers;
import edgruberman.bukkit.livemarkers.MarkerCache;

/**
 * Newly created signs since last static generation.
 */
public class SignChanges extends MarkerCache implements Listener {

    private File status;
    private File storage;
    private final Map<String, Long> worldStatus = new HashMap<String, Long>(); // World name, last marker generation
    private Map<LocationCapture, String[]> signChanges = new HashMap<LocationCapture, String[]>();

    @Override
    public String getId() {
        return KnownMarkers.SIGN_CHANGE.id;
    }

    @Override
    public void load(final ConfigurationSection config) {
        if (config == null) throw new RuntimeException("Configuration section missing for NewSigns");

        this.status = new File(config.getString("status"));
        if (!this.status.isAbsolute()) this.status = new File(this.writer.plugin.getDataFolder(), this.status.getPath()); // Relative paths should be from the plugin data folder
        this.writer.plugin.getLogger().config("NewSigns Storage: " + this.status.getPath());
        this.clean();

        this.storage = new File(config.getString("storage"));
        if (!this.storage.isAbsolute()) this.storage = new File(this.writer.plugin.getDataFolder(), this.storage.getPath()); // Relative paths should be from the plugin data folder
        this.writer.plugin.getLogger().config("OfflinePlayers Storage: " + this.storage.getPath());
        if (this.storage.isFile()) this.loadStorage(); else this.saveStorage();

        this.writer.plugin.getServer().getPluginManager().registerEvents(this, this.writer.plugin);
    }

    @Override
    public Void call() {
        final String timestamp = this.writer.timestamp.format(new Date());

        this.markers.clear();
        final Iterator<Map.Entry<LocationCapture, String[]>> it =  this.signChanges.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<LocationCapture, String[]> sign = it.next();

            final Long status = this.worldStatus.get(sign.getKey().world);
            if (status != null && sign.getKey().captured >= status) {
                // Clean out signs created before last marker generation
                it.remove();
                continue;
            }

            final Map<String, Object> marker = new HashMap<String, Object>();
            marker.put("id", this.getId());
            marker.put("Text1", sign.getValue()[0]);
            marker.put("Text2", sign.getValue()[1]);
            marker.put("Text3", sign.getValue()[2]);
            marker.put("Text4", sign.getValue()[3]);
            marker.put("world", sign.getKey().world);
            marker.put("x", sign.getKey().x);
            marker.put("y", sign.getKey().y);
            marker.put("z", sign.getKey().z);
            marker.put("timestamp", timestamp);
            this.markers.add(marker);
        }

        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(final SignChangeEvent change) {
        this.signChanges.put(new LocationCapture(change.getBlock().getLocation()), change.getLines());
        this.stale = true;
    }

    @Override
    public void clear() {
        this.saveStorage();
        this.worldStatus.clear();
        this.signChanges.clear();
        super.clear();
    }

    @Override
    public void clean() {
        if (!this.status.isFile()) {
            this.writer.plugin.getLogger().fine("Marker generation status file does not exist: " + this.status.getPath());
            return;
        }

        //TODO - load status file

        // Worlds/World1:
        //   Billboards: 123456789
        //   All Signs: 123456789

        // set stale if changed
    }

    @SuppressWarnings("unchecked")
    private void loadStorage() {
        try {
            final ObjectInputStream in = new ObjectInputStream(new FileInputStream(this.storage));
            this.signChanges = (HashMap<LocationCapture, String[]>) in.readObject();
            in.close();
        } catch (final Exception e) {
            this.writer.plugin.getLogger().severe("Unable to load NewSigns storage file: " + this.storage.getPath() + "; " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private void saveStorage() {
        if (!this.storage.getParentFile().exists()) this.storage.getParentFile().mkdirs();

        try {
            final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(this.storage));
            out.writeObject(this.signChanges);
            out.close();
        } catch (final Exception e) {
            this.writer.plugin.getLogger().severe("Unable to save NewSigns storage file: " + this.storage.getPath() + "; " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private static class LocationCapture implements Serializable {
        private static final long serialVersionUID = 1;

        public String world;
        public double x, y, z;
        public long captured;

        private LocationCapture(final String world, final double x, final double y, final double z, final long captured) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.captured = captured;
        }

        private LocationCapture(final Location location) {
            this(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), System.currentTimeMillis());
        }

    }

}
