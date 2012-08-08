package edgruberman.bukkit.livemarkers.caches;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import edgruberman.bukkit.livemarkers.MarkerIdentifier;
import edgruberman.bukkit.livemarkers.MarkerWriter;

/** collection of cached markers */
public abstract class MarkerCache implements MarkerIdentifier, Callable<Void> {

    // ---- Static Factory ----

    public static MarkerCache create(final String className, final MarkerWriter writer, final ConfigurationSection definition) throws ClassNotFoundException, ClassCastException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        final MarkerCache cache = MarkerCache.find(className).newInstance();
        cache.writer = writer;
        cache.load(definition);
        return cache;
    }

    public static Class<? extends MarkerCache> find(final String className) throws ClassNotFoundException, ClassCastException {
        // Look in local package first
        try {
            return Class.forName(MarkerCache.class.getPackage().getName() + "." + className).asSubclass(MarkerCache.class);
        } catch (final Exception e) {
            // Ignore to try searching for custom class next
        }

        // Look for a custom class
        return Class.forName(className).asSubclass(MarkerCache.class);
    }



    // ---- Instance ----

    /** cache manager */
    protected MarkerWriter writer;

    /** the cache of markers, a grouping of key/value pairs */
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
     * refresh marker cache
     *
     * @return Void; use {@link #getMarkers()} to retrieve the updated cache
     */
    @Override
    public abstract Void call();

    private static final long TIMEOUT = 5;
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    /**
     * current cache if fresh, otherwise automatically refreshes markers if stale
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
     * indicates if markers are out of date
     *
     * @return {@link #stale}
     */
    public boolean isStale() {
        return this.stale;
    }

    /** prepare object for garbage collection */
    public void clear() {
        this.markers.clear();
    }

    /** force cache to be refreshed next run */
    public void clean() {
        this.stale = true;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
