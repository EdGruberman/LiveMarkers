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

import edgruberman.bukkit.livemarkers.MarkerCache;
import edgruberman.bukkit.livemarkers.MarkerType;

/** players currently connected */
public class OnlinePlayers extends MarkerCache implements Listener {

    private boolean hideSneaking = false;

    @Override
    public MarkerType getType() {
        return MarkerType.ONLINE_PLAYER;
    }

    @Override
    public void load(final ConfigurationSection config) {
        this.hideSneaking = config.getBoolean("hideSneaking");
        this.writer.plugin.getLogger().config("OnlinePlayers Hide Sneaking Players: " + this.hideSneaking);

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

        boolean sneakers = false;
        this.markers.clear();
        for (final Player player : this.writer.plugin.getServer().getOnlinePlayers()) {
            if (!player.hasPermission("livemarkers.onlineplayers")) continue;
            if (this.hideSneaking && player.isSneaking()) {
                sneakers = true;
                continue;
            }

            final Map<String, Object> marker = new HashMap<String, Object>();
            marker.put("id", this.getType().id);
            marker.put("msg", player.getName());
            marker.put("world", player.getLocation().getWorld().getName());
            marker.put("x", player.getLocation().getX());
            marker.put("y", player.getLocation().getY());
            marker.put("z", player.getLocation().getZ());
            marker.put("timestamp", timestamp);

            this.markers.add(marker);
        }

        this.stale = ( sneakers ? true : false );

        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent join) {
        if (!join.getPlayer().hasPermission("livemarkers.onlineplayers")) return;

        // Ensure a player joining when no online players previously existed indicates markers are ready to be refreshed
        this.stale = true;
    }

}
