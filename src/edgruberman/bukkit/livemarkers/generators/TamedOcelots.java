package edgruberman.bukkit.livemarkers.generators;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Ocelot;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * Markers for pet cats
 */
public class TamedOcelots extends MarkerGenerator implements Listener {

    private static final int ID = 7;

    private final SimpleDateFormat timestamp;

    public TamedOcelots(final Plugin plugin, final SimpleDateFormat timestamp) {
        super(plugin, TamedOcelots.ID);
        this.timestamp = timestamp;
    }

    @Override
    public List<Object> call() {
        final String timestamp = this.timestamp.format(new Date());

        this.markers.clear();
        for (final World world : this.plugin.getServer().getWorlds()) {
            for (final Ocelot ocelot : world.getEntitiesByClass(Ocelot.class)) {
                if (!ocelot.isTamed() || !(ocelot.getOwner() instanceof OfflinePlayer)) continue;

                final OfflinePlayer owner = (OfflinePlayer) ocelot.getOwner();
                final Map<String, Object> marker = new HashMap<String, Object>();
                marker.put("id", this.id);
                marker.put("msg", owner.getName());
                marker.put("world", ocelot.getLocation().getWorld().getName());
                marker.put("x", ocelot.getLocation().getX());
                marker.put("y", ocelot.getLocation().getY());
                marker.put("z", ocelot.getLocation().getZ());
                marker.put("timestamp", timestamp);

                this.markers.add(marker);
            }
        }

        return this.markers;
    }

}
