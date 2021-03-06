package edgruberman.bukkit.livemarkers.caches;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Ocelot;
import org.bukkit.event.Listener;

import edgruberman.bukkit.livemarkers.MarkerCache;
import edgruberman.bukkit.livemarkers.MarkerType;

/** pet cats */
public class TamedOcelots extends MarkerCache implements Listener {

    @Override
    public MarkerType getType() {
        return MarkerType.TAMED_OCELOT;
    }

    @Override
    public Void call() {
        final String timestamp = this.writer.timestamp.format(new Date());

        this.markers.clear();
        for (final World world : this.writer.plugin.getServer().getWorlds()) {
            for (final Ocelot ocelot : world.getEntitiesByClass(Ocelot.class)) {
                if (!ocelot.isTamed() || !(ocelot.getOwner() instanceof OfflinePlayer)) continue;

                final OfflinePlayer owner = (OfflinePlayer) ocelot.getOwner();
                final Map<String, Object> marker = new HashMap<String, Object>();
                marker.put("id", this.getType().id);
                marker.put("msg", owner.getName());
                marker.put("world", ocelot.getLocation().getWorld().getName());
                marker.put("x", ocelot.getLocation().getX());
                marker.put("y", ocelot.getLocation().getY());
                marker.put("z", ocelot.getLocation().getZ());
                marker.put("timestamp", timestamp);

                this.markers.add(marker);
            }
        }

        return null;
    }

}
