package edgruberman.bukkit.livemarkers.caches;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import edgruberman.bukkit.livemarkers.KnownMarkers;
import edgruberman.bukkit.livemarkers.MarkerCache;

/**
 * Players that previously connected while plugin was running.
 */
public class OfflinePlayers extends MarkerCache implements Listener {

    private File storage;
    private Map<String, LocationCapture> last = new HashMap<String, LocationCapture>();

    @Override
    public String getId() {
        return KnownMarkers.OFFLINE_PLAYER.id;
    }

    @Override
    public void load(final ConfigurationSection config) {
        if (config == null) throw new RuntimeException("Configuration section missing for OfflinePlayers");

        this.storage = new File(config.getString("storage"));
        if (!this.storage.isAbsolute()) this.storage = new File(this.writer.plugin.getDataFolder(), this.storage.getPath()); // Relative paths should be from the plugin data folder
        this.writer.plugin.getLogger().config("OfflinePlayers Storage: " + this.storage.getPath());
        if (this.storage.isFile()) this.loadLast(); else this.saveLast();

        this.writer.plugin.getServer().getPluginManager().registerEvents(this, this.writer.plugin);
    }

    @Override
    public Void call() {
        this.markers.clear();
        for (final Map.Entry<String, LocationCapture> entry : this.last.entrySet()) {
            final String timestamp = this.writer.timestamp.format(new Date(entry.getValue().captured));

            final Map<String, Object> marker = new HashMap<String, Object>();
            marker.put("id", this.getId());
            marker.put("msg", entry.getKey());
            marker.put("world", entry.getValue().world);
            marker.put("x", entry.getValue().x);
            marker.put("y", entry.getValue().y);
            marker.put("z", entry.getValue().z);
            marker.put("timestamp", timestamp);

            this.markers.add(marker);
        }

        this.stale = false;

        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.last.remove(event.getPlayer().getName());
        this.stale = true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        this.last.put(event.getPlayer().getName(), new LocationCapture(event.getPlayer().getLocation()));
        this.stale = true;
    }

    @Override
    public void clear() {
        this.saveLast();
        super.clear();
    }

    @SuppressWarnings("unchecked")
    private void loadLast() {
        try {
            final ObjectInputStream in = new ObjectInputStream(new FileInputStream(this.storage));
            this.last = (HashMap<String, LocationCapture>) in.readObject();
            in.close();
        } catch (final Exception e) {
            this.writer.plugin.getLogger().severe("Unable to load OfflinePlayer storage file: " + this.storage.getPath() + "; " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private void saveLast() {
        if (!this.storage.getParentFile().exists()) this.storage.getParentFile().mkdirs();

        try {
            final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(this.storage));
            out.writeObject(this.last);
            out.close();
        } catch (final Exception e) {
            this.writer.plugin.getLogger().severe("Unable to save OfflinePlayer storage file: " + this.storage.getPath() + "; " + e.getClass().getName() + ": " + e.getMessage());
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
