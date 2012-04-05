package edgruberman.bukkit.livemarkers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.livemarkers.generators.BedSpawns;
import edgruberman.bukkit.livemarkers.generators.MarkerGenerator;
import edgruberman.bukkit.livemarkers.generators.OfflinePlayers;
import edgruberman.bukkit.livemarkers.generators.OnlinePlayers;
import edgruberman.bukkit.livemarkers.generators.TamedOcelots;
import edgruberman.bukkit.livemarkers.generators.TamedWolves;

public class Main extends JavaPlugin {

    private UpdateMarkers updater = null;

    @Override
    public void onEnable() {
        if (!(new File(this.getDataFolder(), "config.yml")).exists()) this.saveDefaultConfig();
        this.reloadConfig();
        this.setLoggingLevel();
        this.start(this, this.getConfig());
    }

    @Override
    public void onDisable() {
        this.updater.run();
        this.updater.clear();
    }

    public void start(final Plugin plugin, final ConfigurationSection config) {
        final long period = config.getLong("period");
        final String output = config.getString("output");
        final SimpleDateFormat timestamp = new SimpleDateFormat(config.getString("timestamp"));
        plugin.getLogger().log(Level.CONFIG, "period: " + period + "s" + "; output: " + output + "; timestamp: " + timestamp.toPattern());

        final List<MarkerGenerator> generators = new ArrayList<MarkerGenerator>();
        for (final String generator : config.getStringList("generators")) {
            if (generator.equals("OnlinePlayers")) {
                generators.add(new OnlinePlayers(plugin, timestamp));

            } else if (generator.equals("OfflinePlayers")) {
                generators.add(new OfflinePlayers(plugin, timestamp, config.getString("OfflinePlayers.storage")));

            } else if (generator.equals("TamedWolves")) {
                generators.add(new TamedWolves(plugin, timestamp));

            } else if (generator.equals("TamedOcelots")) {
                generators.add(new TamedOcelots(plugin, timestamp));

            } else if (generator.equals("BedSpawns")) {
                generators.add(new BedSpawns(plugin, timestamp));

            } else {
                plugin.getLogger().log(Level.WARNING, "Unsupported marker generator: " + generator);
            }
        }

        this.updater = new UpdateMarkers(plugin, period, output, generators);
        this.updater.start();
    }

    private void setLoggingLevel() {
        final String name = this.getConfig().getString("logLevel", "INFO");
        Level level;
        try { level = Level.parse(name); } catch (final Exception e) {
            level = Level.INFO;
            this.getLogger().warning("Unrecognized java.util.logging.Level in \"" + this.getDataFolder().getPath() + "\\config.yml\"; logLevel: " + name);
        }

        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it.
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().log(Level.CONFIG, "Logging level set to: " + this.getLogger().getLevel());
    }

}
