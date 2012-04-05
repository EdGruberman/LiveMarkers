package edgruberman.bukkit.markers.generators;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

/**
 * Markers for players currently connected
 */
public class OnlinePlayers extends MarkerGenerator implements Listener {

    private static final int ID = 4;

    private final SimpleDateFormat timestamp;

    public OnlinePlayers(final Plugin plugin, final SimpleDateFormat timestamp) {
        super(plugin, OnlinePlayers.ID);
        this.timestamp = timestamp;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean isStale() {
        // If markers exist, players existed on last refresh, online player location is always considered stale
        return (this.isStale || (this.markers.size() != 0));
    }

    @Override
    public List<Object> call() {
        final String timestamp = this.timestamp.format(new Date());

        this.markers.clear();
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {

            final Map<String, Object> marker = new HashMap<String, Object>();
            marker.put("id", this.id);
            marker.put("msg", player.getName());
            marker.put("world", player.getLocation().getWorld().getName());
            marker.put("x", player.getLocation().getX());
            marker.put("y", player.getLocation().getY());
            marker.put("z", player.getLocation().getZ());
            marker.put("timestamp", timestamp);

            this.markers.add(marker);
        }

        this.isStale = false;

        return this.markers;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.isStale = true;
    }

}
