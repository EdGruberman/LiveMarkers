package edgruberman.bukkit.livemarkers.caches;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Listener;

import edgruberman.bukkit.livemarkers.KnownMarkers;
import edgruberman.bukkit.livemarkers.MarkerCache;

/**
 * Pet wolves.
 */
public class TamedWolves extends MarkerCache implements Listener {

    @Override
    public String getId() {
        return KnownMarkers.TAMED_WOLF.id;
    }

    @Override
    public Void call() {
        final String timestamp = this.writer.timestamp.format(new Date());

        this.markers.clear();
        for (final World world : this.writer.plugin.getServer().getWorlds()) {
            for (final Wolf wolf : world.getEntitiesByClass(Wolf.class)) {
                if (!wolf.isTamed() || !(wolf.getOwner() instanceof OfflinePlayer)) continue;

                final OfflinePlayer owner = (OfflinePlayer) wolf.getOwner();
                final Map<String, Object> marker = new HashMap<String, Object>();
                marker.put("id", this.getId());
                marker.put("msg", owner.getName());
                marker.put("world", wolf.getLocation().getWorld().getName());
                marker.put("x", wolf.getLocation().getX());
                marker.put("y", wolf.getLocation().getY());
                marker.put("z", wolf.getLocation().getZ());
                marker.put("timestamp", timestamp);

                this.markers.add(marker);
            }
        }

        return null;
    }

}
