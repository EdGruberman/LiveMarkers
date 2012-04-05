package edgruberman.bukkit.livemarkers.generators;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/**
 * Markers for players that previously connected while plugin was running.
 */
public class OfflinePlayers extends MarkerGenerator implements Listener {

    private static final int ID = 5;

    private final SimpleDateFormat timestamp;
    private File storage;
    private Map<String, LocationCapture> last = new HashMap<String, LocationCapture>();

    public OfflinePlayers(final Plugin plugin, final SimpleDateFormat timestamp, final String storage) {
        super(plugin, OfflinePlayers.ID);
        this.timestamp = timestamp;

        this.storage = new File(storage);
        if (!this.storage.isAbsolute()) this.storage = new File(plugin.getDataFolder(), this.storage.getPath());
        if (!this.storage.exists() && this.storage.getParentFile() != null) this.storage.getParentFile().mkdirs();

        this.load();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public List<Object> call() {
        this.markers.clear();
        for (final Map.Entry<String, LocationCapture> entry : this.last.entrySet()) {
            final String timestamp = this.timestamp.format(new Date(entry.getValue().captured));

            final Map<String, Object> marker = new HashMap<String, Object>();
            marker.put("id", this.id);
            marker.put("msg", entry.getKey());
            marker.put("world", entry.getValue().world);
            marker.put("x", entry.getValue().x);
            marker.put("y", entry.getValue().y);
            marker.put("z", entry.getValue().z);
            marker.put("timestamp", timestamp);

            this.markers.add(marker);
        }

        this.isStale = false;

        return this.markers;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.last.remove(event.getPlayer().getName());
        this.isStale = true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        this.last.put(event.getPlayer().getName(), new LocationCapture(event.getPlayer().getLocation()));
        this.isStale = true;
    }

    @Override
    public void clear() {
        this.save();
        super.clear();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (!this.storage.exists() || !this.storage.isFile()) return;

        try {
            final ObjectInputStream in = new ObjectInputStream(new FileInputStream(this.storage));
            this.last = (HashMap<String, LocationCapture>) in.readObject();
            in.close();
        } catch (final Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to load OfflinePlayer storage file: " + this.storage.getPath(), e);
        }
    }

    private void save() {
        try {
            final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(this.storage));
            out.writeObject(this.last);
            out.close();
        } catch (final Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to save OfflinePlayer storage file: " + this.storage.getPath(), e);
        }
    }

    @SuppressWarnings("serial")
    private static class LocationCapture implements Serializable {

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
