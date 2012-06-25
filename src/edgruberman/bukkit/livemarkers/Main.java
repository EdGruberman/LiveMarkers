package edgruberman.bukkit.livemarkers;

import java.text.SimpleDateFormat;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.livemarkers.commands.Clean;

public class Main extends JavaPlugin {

    public MarkerWriter writer = null;

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.setLoggingLevel(this.getConfig().getString("logLevel", "INFO"));
        this.start(this, this.getConfig());
    }

    @Override
    public void onDisable() {
        if (this.writer != null) {
            this.writer.clear();
            this.writer = null;
        }
    }

    public void start(final JavaPlugin context, final ConfigurationSection config) {
        // Load marker writer configuration
        final long period = config.getLong("period");
        final String output = config.getString("output");
        final SimpleDateFormat timestamp = new SimpleDateFormat(config.getString("timestamp"));
        final MarkerWriter writer = new MarkerWriter(context, period, output, timestamp);
        context.getLogger().config("period: " + writer.period + "ms" + "; timestamp: " + writer.timestamp.toPattern() + "; output: " + writer.output.getPath());

        // Load marker cache managers
        for (final String name : config.getStringList("markers")) {
            try {
                writer.addCache(name, config.getConfigurationSection(name));
            } catch (final Exception e) {
                context.getLogger().warning("Unable to add marker cache: " + name + "; " + e.getClass().getName() + ": " + e.getMessage());
                continue;
            }
        }
        context.getLogger().config("Marker caches loaded (" + writer.caches.size() + "): " + writer.caches.toString());

        // Enable marker writer
        this.writer = writer;
        this.writer.start();

        new Clean(context, context.getName().toLowerCase() + ":clean", this.writer);
    }

    private void setLoggingLevel(final String name) {
        Level level;
        try { level = Level.parse(name); } catch (final Exception e) {
            level = Level.INFO;
            this.getLogger().warning("Defaulting to " + level.getName() + "; Unrecognized java.util.logging.Level: " + name);
        }

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it.
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().config("Logging level set to: " + this.getLogger().getLevel());
    }

}
