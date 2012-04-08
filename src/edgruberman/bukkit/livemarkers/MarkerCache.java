package edgruberman.bukkit.livemarkers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Base class for cache management.
 */
public abstract class MarkerCache implements MarkerIdentifier, Callable<Void> {

    /**
     * Instance that manages this cache.
     */
    protected MarkerWriter writer;

    /**
     * The marker cache. An array of markers.
     * Where a marker is a grouping of key/value pairs.
     */
    protected final List<Map<String, Object>> markers = new ArrayList<Map<String, Object>>();

    /**
     * false if the marker cache is fresh and can be used without an
     * expensive refresh operation; true it is stale and needs to be refreshed
     * (Initial assumption is that the marker cache is stale.)
     */
    protected boolean stale = true;

    /**
     * Post shared field (plugin, timestamp) initialization. SubClasses should
     * override this method if they need to load any custom settings when
     * initialized, or if they need to do any custom initialization after
     * the shared fields have been applied.
     *
     * @param config configuration settings
     */
    public void load(final ConfigurationSection config) {
        return;
    }

    /**
     * Refresh marker cache.
     *
     * @return null; use {@link #getMarkers()} to retrieve the updated cache
     */
    @Override
    public abstract Void call();

    private static final long TIMEOUT = 5;
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    /**
     * Current cache if fresh, otherwise automatically refreshes markers if stale.
     *
     * @return {@link #markers}
     */
    public List<Map<String, Object>> getMarkers() {
        if (this.isStale()) {
            // Refresh markers in the main thread synchronously
            final Future<Void> refresh = Bukkit.getServer().getScheduler().callSyncMethod(this.writer.plugin, this);
            try {
                refresh.get(MarkerCache.TIMEOUT, MarkerCache.TIMEOUT_UNIT);
            } catch (final Exception e) {
                this.writer.plugin.getLogger().warning("Unable to refresh markers for: " + this.getClass().getName() + "; " + e.getClass().getName() + ": " + e.getMessage());
                return null;
            }
        }

        return this.markers;
    }

    /**
     * Indicates if markers are out of date.
     *
     * @return {@link #stale}
     */
    public boolean isStale() {
        return this.stale;
    }

    /**
     * Prepare object for garbage collection.
     * If SubClasses override this they should call this method directly also.
     */
    public void clear() {
        this.markers.clear();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
