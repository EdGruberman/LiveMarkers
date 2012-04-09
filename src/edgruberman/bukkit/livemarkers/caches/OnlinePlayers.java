package edgruberman.bukkit.livemarkers.caches;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import edgruberman.bukkit.livemarkers.KnownMarkers;
import edgruberman.bukkit.livemarkers.MarkerCache;

/**
 * Players currently connected.
 */
public class OnlinePlayers extends MarkerCache implements Listener {

    @Override
    public String getId() {
        return KnownMarkers.ONLINE_PLAYER.id;
    }

    @Override
    public void load(final ConfigurationSection config) {
        this.writer.plugin.getServer().getPluginManager().registerEvents(this, this.writer.plugin);
    }

    @Override
    public boolean isStale() {
        // Avoid expensive PlayerMoveEvent tracking and always refresh online players
        return (this.stale || (this.markers.size() != 0));
    }

    @Override
    public Void call() {
        final String timestamp = this.writer.timestamp.format(new Date());

        this.markers.clear();
        for (final Player player : this.writer.plugin.getServer().getOnlinePlayers()) {

            final Map<String, Object> marker = new HashMap<String, Object>();
            marker.put("id", this.getId());
            marker.put("msg", player.getName());
            marker.put("world", player.getLocation().getWorld().getName());
            marker.put("x", player.getLocation().getX());
            marker.put("y", player.getLocation().getY());
            marker.put("z", player.getLocation().getZ());
            marker.put("timestamp", timestamp);

            this.markers.add(marker);
        }

        this.stale = false;

        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        // Ensure a player joining when no online players previously existed indicates markers are ready to be refreshed
        this.stale = true;
    }

}