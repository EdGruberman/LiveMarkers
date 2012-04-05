package edgruberman.bukkit.livemarkers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;
import org.json.simple.JSONValue;

import edgruberman.bukkit.livemarkers.generators.MarkerGenerator;

/**
 * File output based on a collection of marker generators.
 */
public class UpdateMarkers implements Runnable {

    private static final int TICKS_PER_SECOND = 20;

    public Plugin plugin;
    public long period;
    public File output;
    public final List<MarkerGenerator> generators = new ArrayList<MarkerGenerator>();

    public UpdateMarkers(final Plugin plugin, final long period, final String output, final List<MarkerGenerator> generators) {
        this.plugin = plugin;
        this.period = period;
        this.output = new File(output);
        if (generators != null) this.generators.addAll(generators);

        if (!this.output.isAbsolute()) this.output = new File(plugin.getDataFolder(), this.output.getPath());
        if (!this.output.exists() && this.output.getParentFile() != null) this.output.getParentFile().mkdirs();
    }

    public void start() {
        this.plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(this.plugin, this
                , this.period / 1000 * UpdateMarkers.TICKS_PER_SECOND
                , this.period / 1000 * UpdateMarkers.TICKS_PER_SECOND
        );
    }

    @Override
    public void run() {
        if (!this.isStale()) return;

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(this.output);
        } catch (final IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Error opening file: " + this.output, e);
        }

        final BufferedWriter writer = new BufferedWriter(fileWriter);
        try {
            JSONValue.writeJSONString(this.getMarkers(), writer);
        } catch (final IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Error writing to file: " + this.output, e);
        } finally {
            try {
                writer.close();
            } catch (final IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Error closing BufferedWriter for : " + this.output, e);
            }
        }
    }

    public List<Object> getMarkers() {
        final List<Object> markers = new ArrayList<Object>();
        for (final MarkerGenerator generator : this.generators) markers.addAll(generator.getMarkers());
        return markers;
    }

    public boolean isStale() {
        for (final MarkerGenerator generator : this.generators)
            if (generator.isStale()) return true;

        return false;
    }

    public void clear() {
        for (final MarkerGenerator generator : this.generators) generator.clear();
        this.generators.clear();
    }

}
