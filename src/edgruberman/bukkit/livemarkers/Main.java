package edgruberman.bukkit.livemarkers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.livemarkers.caches.MarkerCache;
import edgruberman.bukkit.livemarkers.commands.Clean;

public class Main extends JavaPlugin {

    private static final Version MINIMUM_CONFIGURATION = new Version("2.1.0");

    public MarkerWriter writer = null;

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

    @Override
    public void reloadConfig() {
        this.saveDefaultConfig();
        super.reloadConfig();
        this.setLogLevel(this.getConfig().getString("logLevel"));

        final Version version = new Version(this.getConfig().isSet("version") ? this.getConfig().getString("version") : null);
        if (version.compareTo(Main.MINIMUM_CONFIGURATION) >= 0) return;

        this.archiveConfig("config.yml", version);
        this.saveDefaultConfig();
        this.reloadConfig();
    }

    @Override
    public void saveDefaultConfig() {
        this.extractConfig("config.yml", false);
    }

    private void archiveConfig(final String resource, final Version version) {
        final String backupName = "%1$s - Archive version %2$s - %3$tY%3$tm%3$tdT%3$tH%3$tM%3$tS.yml";
        final File backup = new File(this.getDataFolder(), String.format(backupName, resource.replaceAll("(?i)\\.yml$", ""), version, new Date()));
        final File existing = new File(this.getDataFolder(), resource);

        if (!existing.renameTo(backup))
            throw new IllegalStateException("Unable to archive configuration file \"" + existing.getPath() + "\" with version \"" + version + "\" to \"" + backup.getPath() + "\"");

        this.getLogger().warning("Archived configuration file \"" + existing.getPath() + "\" with version \"" + version + "\" to \"" + backup.getPath() + "\"");
    }

    private void extractConfig(final String resource, final boolean replace) {
        final Charset source = Charset.forName("UTF-8");
        final Charset target = Charset.defaultCharset();
        if (target.equals(source)) {
            super.saveResource(resource, replace);
            return;
        }

        final File config = new File(this.getDataFolder(), resource);
        if (config.exists()) return;

        final char[] cbuf = new char[1024]; int read;
        try {
            final Reader in = new BufferedReader(new InputStreamReader(this.getResource(resource), source));
            final Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(config), target));
            while((read = in.read(cbuf)) > 0) out.write(cbuf, 0, read);
            out.close(); in.close();

        } catch (final Exception e) {
            throw new IllegalArgumentException("Could not extract configuration file \"" + resource + "\" to " + config.getPath() + "\";" + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private void setLogLevel(final String name) {
        Level level;
        try { level = Level.parse(name); } catch (final Exception e) {
            level = Level.INFO;
            this.getLogger().warning("Log level defaulted to " + level.getName() + "; Unrecognized java.util.logging.Level: " + name);
        }

        // only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it
        for (final Handler h : this.getLogger().getParent().getHandlers())
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);

        this.getLogger().setLevel(level);
        this.getLogger().config("Log level set to: " + this.getLogger().getLevel());
    }

}
