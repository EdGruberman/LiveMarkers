package edgruberman.bukkit.livemarkers.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Marker cache and creation
 */
public abstract class MarkerGenerator implements Callable<List<Object>> {

    protected long timeout = 5;
    protected TimeUnit timeoutUnit = TimeUnit.SECONDS;

    protected final Plugin plugin;
    protected final int id;
    protected boolean isStale = true;
    protected final List<Object> markers = new ArrayList<Object>();

    protected MarkerGenerator(final Plugin plugin, final int id) {
        this.plugin = plugin;
        this.id = id;
    }

    /**
     * Refresh markers.
     */
    @Override
    public abstract List<Object> call();

    /**
     * Refreshes markers if stale.
     */
    public List<Object> getMarkers() {
        if (this.isStale()) {
            // Refresh markers in the main thread synchronously
            final Future<List<Object>> refresh = Bukkit.getServer().getScheduler().callSyncMethod(this.plugin, this);
            try {
                refresh.get(this.timeout, this.timeoutUnit);
            } catch (final Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, "Unable to refresh markers for " + this.getClass().getName(), e);
                return null;
            }
        }

        return this.markers;
    }

    /**
     * Indicates if markers are out of date.
     *
     * @return true if markers are out of date; false otherwise
     */
    public boolean isStale() {
        return this.isStale;
    }

    /**
     * Prepare object for garbage collection.
     * If SubClasses override this they should call this method directly also.
     */
    public void clear() {
        this.markers.clear();
    }

}
