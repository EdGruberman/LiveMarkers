package edgruberman.bukkit.livemarkers.generators;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * Markers for pet wolves
 */
public class TamedWolves extends MarkerGenerator implements Listener {

    private static final int ID = 6;

    private final SimpleDateFormat timestamp;

    public TamedWolves(final Plugin plugin, final SimpleDateFormat timestamp) {
        super(plugin, TamedWolves.ID);
        this.timestamp = timestamp;
    }

    @Override
    public List<Object> call() {
        final String timestamp = this.timestamp.format(new Date());

        this.markers.clear();
        for (final World world : this.plugin.getServer().getWorlds()) {
            for (final Wolf wolf : world.getEntitiesByClass(Wolf.class)) {
                if (!wolf.isTamed() || !(wolf.getOwner() instanceof OfflinePlayer)) continue;

                final OfflinePlayer owner = (OfflinePlayer) wolf.getOwner();
                final Map<String, Object> marker = new HashMap<String, Object>();
                marker.put("id", this.id);
                marker.put("msg", owner.getName());
                marker.put("world", wolf.getLocation().getWorld().getName());
                marker.put("x", wolf.getLocation().getX());
                marker.put("y", wolf.getLocation().getY());
                marker.put("z", wolf.getLocation().getZ());
                marker.put("timestamp", timestamp);

                this.markers.add(marker);
            }
        }

        return this.markers;
    }

}
