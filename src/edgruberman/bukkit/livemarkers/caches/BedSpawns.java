package edgruberman.bukkit.livemarkers.caches;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

import edgruberman.bukkit.livemarkers.KnownMarkers;
import edgruberman.bukkit.livemarkers.MarkerCache;

/**
 * Player bed spawns.
 */
public class BedSpawns extends MarkerCache implements Listener {

    @Override
    public String getId() {
        return KnownMarkers.BED_SPAWN.id;
    }

    @Override
    public void load(final ConfigurationSection config) {
        this.writer.plugin.getServer().getPluginManager().registerEvents(this, this.writer.plugin);
    }

    @Override
    public Void call() {
        final String timestamp = this.writer.timestamp.format(new Date());

        this.markers.clear();
        for (final OfflinePlayer player : this.writer.plugin.getServer().getOfflinePlayers()) {

            final Map<String, Object> marker = new HashMap<String, Object>();
            marker.put("id", this.getId());
            marker.put("msg", player.getName());
            marker.put("world", player.getBedSpawnLocation().getWorld().getName());
            marker.put("x", player.getBedSpawnLocation().getBlockX());
            marker.put("y", player.getBedSpawnLocation().getBlockY());
            marker.put("z", player.getBedSpawnLocation().getBlockZ());
            marker.put("timestamp", timestamp);

            this.markers.add(marker);
        }

        this.stale = false;

        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBedEnter(final PlayerBedEnterEvent event) {
        // Without a bed spawn change event, assume the worst and check for updates after
        this.stale = true;
    }

}
