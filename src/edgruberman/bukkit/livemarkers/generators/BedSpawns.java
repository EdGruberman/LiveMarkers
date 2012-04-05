package edgruberman.bukkit.livemarkers.generators;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.plugin.Plugin;

/**
 * Markers for player bed spawns
 */
public class BedSpawns extends MarkerGenerator implements Listener {

    private static final int ID = 8;

    private final SimpleDateFormat timestamp;

    public BedSpawns(final Plugin plugin, final SimpleDateFormat timestamp) {
        super(plugin, BedSpawns.ID);
        this.timestamp = timestamp;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public List<Object> call() {
        final String timestamp = this.timestamp.format(new Date());

        this.markers.clear();
        for (final OfflinePlayer player : this.plugin.getServer().getOfflinePlayers()) {

            final Map<String, Object> marker = new HashMap<String, Object>();
            marker.put("id", this.id);
            marker.put("msg", player.getName());
            marker.put("world", player.getBedSpawnLocation().getWorld().getName());
            marker.put("x", player.getBedSpawnLocation().getBlockX());
            marker.put("y", player.getBedSpawnLocation().getBlockY());
            marker.put("z", player.getBedSpawnLocation().getBlockZ());
            marker.put("timestamp", timestamp);

            this.markers.add(marker);
        }

        this.isStale = false;

        return this.markers;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBedEnter(final PlayerBedEnterEvent event) {
        // Without a bed spawn change event, assume the worst and check for updates after
        this.isStale = true;
    }

}
