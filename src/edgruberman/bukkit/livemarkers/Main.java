package edgruberman.bukkit.livemarkers;

import java.text.SimpleDateFormat;

import edgruberman.bukkit.livemarkers.caches.MarkerCache;
import edgruberman.bukkit.livemarkers.commands.Clean;
import edgruberman.bukkit.livemarkers.util.CustomPlugin;

public class Main extends CustomPlugin {

    public MarkerWriter writer = null;

    @Override
    public void onLoad() { this.putConfigMinimum(CustomPlugin.CONFIGURATION_FILE, "2.1.0"); }

    @Override
    public void onEnable() {
        this.reloadConfig();

        // load marker writer configuration
        final long period = this.getConfig().getLong("period");
        final String output = this.getConfig().getString("output");
        final SimpleDateFormat timestamp = new SimpleDateFormat(this.getConfig().getString("timestamp"));
        final MarkerWriter writer = new MarkerWriter(this, output, timestamp);
        this.getLogger().config("period: " + period + "ms" + "; timestamp: " + writer.timestamp.toPattern() + "; output: " + writer.output.getPath());

        // load marker cache managers
        for (final String name : this.getConfig().getStringList("markers")) {
            try {
                writer.caches.add(MarkerCache.create(name, writer, this.getConfig().getConfigurationSection(name)));
            } catch (final Exception e) {
                this.getLogger().warning("Unable to add marker cache: " + name + "; " + e.getClass().getName() + ": " + e.getMessage());
                continue;
            }
        }
        this.getLogger().config("Marker caches loaded (" + writer.caches.size() + "): " + writer.caches.toString());

        // enable marker writer
        this.writer = writer;
        this.writer.schedule(period);

        this.getCommand("livemarkers:clean").setExecutor(new Clean(this.writer));
    }

    @Override
    public void onDisable() {
        this.writer.clear();
        this.writer = null;
    }

}
