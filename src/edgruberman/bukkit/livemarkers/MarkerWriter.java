package edgruberman.bukkit.livemarkers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONValue;


/** marker file JSON output based on a collection of caches */
public class MarkerWriter implements Runnable {

    /** owning plugin to use for logging, event registration, etc. */
    public final Plugin plugin;

    public final File output;
    public final List<MarkerCache> caches = new ArrayList<MarkerCache>();

    /** formatting to use for marker timestamp which indicates when marker was last refreshed */
    public final SimpleDateFormat timestamp;

    private int taskId = -1;

    public MarkerWriter(final Plugin plugin, final String output, final SimpleDateFormat timestamp) {
        this.plugin = plugin;

        if (new File(output).isAbsolute()) {
            this.output = new File(output);
        } else {
            this.output = new File(plugin.getDataFolder(), output);
        }

        this.timestamp = timestamp;
    }

    /** @param period milliseconds */
    public void schedule(final long period) {
        if (!this.output.exists() && this.output.getParentFile() != null) this.output.getParentFile().mkdirs();

        final int TICKS_PER_SECOND = 20;
        this.taskId = Bukkit.getScheduler().scheduleAsyncRepeatingTask(this.plugin, this
                , TimeUnit.MILLISECONDS.toSeconds(period) * TICKS_PER_SECOND
                , TimeUnit.MILLISECONDS.toSeconds(period) * TICKS_PER_SECOND
        );
    }

    /** update marker file if any cache is stale */
    @Override
    public void run() {
        if (!this.isStale()) return;

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(this.output);
        } catch (final IOException e) {
            this.plugin.getLogger().severe("Error opening file: " + this.output + "; " + e);
            return;
        }

        final BufferedWriter writer = new BufferedWriter(fileWriter);
        try {
            JSONValue.writeJSONString(this.getMarkers(), writer);

        } catch (final IOException e) {
            this.plugin.getLogger().severe("Error writing to file: " + this.output + "; " + e);

        } finally {
            try {
                writer.close();
            } catch (final IOException e) {
                this.plugin.getLogger().severe("Error closing BufferedWriter for : " + this.output + "; " + e);
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
        if (this.taskId != -1) Bukkit.getScheduler().cancelTask(this.taskId);
        for (final MarkerCache cache : this.caches) cache.clear();
        this.caches.clear();
    }

}
