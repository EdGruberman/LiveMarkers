package edgruberman.bukkit.simplemarkers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

//TODO New Signs, PID
public class Main extends org.bukkit.plugin.java.JavaPlugin {

    private static final int TICKS_PER_SECOND = 20;

    private ConfigurationFile configurationFile;
    static MessageManager messageManager;

    private static HashMap<String, String> lastSeen = new HashMap<String, String>();
    private static boolean isUpdated = true;
    private static SimpleDateFormat timestamp;

    @Override
    public void onLoad() {
        Main.messageManager = new MessageManager(this);
        this.configurationFile = new ConfigurationFile(this);
    }

    @Override
    public void onEnable() {
        this.loadConfiguration();

        new PlayerListener(this);

    }

    public void loadConfiguration() {
        final FileConfiguration config = this.configurationFile.load();

        final int period = config.getInt("period", 15);
        Main.timestamp = new SimpleDateFormat(config.getString("timestamp"));
        Main.messageManager.log("period: " + period + "s" + "; output: " + config.getString("output"), MessageLevel.CONFIG);

        this.getServer().getScheduler().scheduleSyncRepeatingTask(
              this
            , new WriteFileTimerTask(config.getString("output"))
            , period * Main.TICKS_PER_SECOND
            , period * Main.TICKS_PER_SECOND
        );
    }

    static void updatePlayer(final Player player) {
        Main.isUpdated = true;
        synchronized(Main.lastSeen) {
            Main.lastSeen.put(player.getName(), Main.timestamp.format(new Date()));
        }
    }

    static void removePlayer(final Player player) {
        Main.isUpdated = true;
        synchronized(Main.lastSeen) {
            Main.lastSeen.remove(player);
        }
    }

    @SuppressWarnings("unchecked")
    static JSONArray getJson() {
        if (Main.isUpdated == false) { return null; }
        Main.isUpdated = false;

        final JSONArray jsonList = new JSONArray();
        JSONObject out;

        for (final Player p : Bukkit.getServer().getOnlinePlayers()) {
            out = new JSONObject();
            out.put("msg", p.getName());
            out.put("id", 4);
            out.put("world", p.getLocation().getWorld().getName());
            out.put("x", p.getLocation().getX());
            out.put("y", p.getLocation().getY());
            out.put("z", p.getLocation().getZ());
            synchronized(Main.lastSeen) {
                final String s = Main.lastSeen.get(p.getName());
                if(s != null) out.put("timestamp", s);
            }
            jsonList.add(out);
        }
        return jsonList;
    }

}
