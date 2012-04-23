package edgruberman.bukkit.livemarkers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONValue;

/**
 * Marker file JSON output based on a collection of caches.
 */
public class MarkerWriter implements Runnable {

    public static final String internalCaches = MarkerWriter.class.getPackage().getName() + ".caches";

    /**
     * Owning plugin to use for logging, event registration, etc.
     * (Not available until load() method is called.)
     */
    public final Plugin plugin;

    /**
     * How often (in MilliSeconds) to check if the marker output file should
     * be updated.
     */
    public final long period;

    public final File output;
    public final List<MarkerCache> caches = new ArrayList<MarkerCache>();

    /**
     * Formatting to use for marker timestamp which indicates when marker was
     * last refreshed.
     */
    public final SimpleDateFormat timestamp;

    private int taskId = -1;

    public MarkerWriter(final Plugin plugin, final long period, final String output, final SimpleDateFormat timestamp) {
        this.plugin = plugin;
        this.period = period;

        if (new File(output).isAbsolute()) {
            this.output = new File(output);
        } else {
            this.output = new File(plugin.getDataFolder(), output);
        }

        this.timestamp = timestamp;
    }

    /**
     * Initializes a new MarkerCache configured and prompts the subclass
     * loading process if defined.
     */
    public void addCache(String name, final ConfigurationSection config) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (!name.contains(".")) name = MarkerWriter.internalCaches + '.' + name;
        final MarkerCache cache = (MarkerCache) Class.forName(name).newInstance();
        cache.writer = this;
        cache.load(config);
        this.caches.add(cache);
        return;
    }

    public void start() {
        if (!this.output.exists() && this.output.getParentFile() != null) this.output.getParentFile().mkdirs();

        final int TICKS_PER_SECOND = 20;
        this.taskId = this.plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(this.plugin, this
                , TimeUnit.MILLISECONDS.toSeconds(this.period) * TICKS_PER_SECOND
                , TimeUnit.MILLISECONDS.toSeconds(this.period) * TICKS_PER_SECOND
        );
    }

    /**
     * Update marker file if any cache is stale.
     */
    @Override
    public void run() {
        if (!this.isStale()) return;

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(this.output);
        } catch (final IOException e) {
            this.plugin.getLogger().severe("Error opening file: " + this.output + "; " + e.getClass().getName() + ": " + e.getMessage());
            return;
        }

        final BufferedWriter writer = new BufferedWriter(fileWriter);
        try {
            JSONValue.writeJSONString(this.getMarkers(), writer);

        } catch (final IOException e) {
            this.plugin.getLogger().severe("Error writing to file: " + this.output + "; " + e.getClass().getName() + ": " + e.getMessage());

        } finally {
            try {
                writer.close();
            } catch (final IOException e) {
                this.plugin.getLogger().severe("Error closing BufferedWriter for : " + this.output + "; " + e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }

    public List<Object> getMarkers() {
        final List<Object> markers = new ArrayList<Object>();
        for (final MarkerCache cache : this.caches) markers.addAll(cache.getMarkers());
        return markers;
    }

    public boolean isStale() {
        for (final MarkerCache cache : this.caches)
            if (cache.isStale()) return true;

        return false;
    }

    public void clear() {
        if (this.taskId != -1) this.plugin.getServer().getScheduler().cancelTask(this.taskId);
        for (final MarkerCache cache : this.caches) cache.clear();
        this.caches.clear();
    }

}
