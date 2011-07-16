package edgruberman.bukkit.simplemarkers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

//TODO New Signs, PID
public class Main extends org.bukkit.plugin.java.JavaPlugin {
    
    private static final int TICKS_PER_SECOND = 20;
    
    static ConfigurationFile configurationFile;
    static MessageManager messageManager;
    
    private static HashMap<Player, String> lastSeen = new HashMap<Player, String>();
    private static boolean isUpdated = true;
    private static SimpleDateFormat timestamp;
    
    public void onLoad() {
        Main.configurationFile = new ConfigurationFile(this);
        Main.configurationFile.load();
        
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
    }
	
    public void onEnable() {
        int period = this.getConfiguration().getInt("period", 15);
        Main.timestamp = new SimpleDateFormat(this.getConfiguration().getString("timestamp"));
        Main.messageManager.log("period: " + period + "s" + "; output: " + this.getConfiguration().getString("output"), MessageLevel.CONFIG);
        
        getServer().getScheduler().scheduleSyncRepeatingTask(
              this
            , new WriteFileTimerTask(this.getConfiguration().getString("output"))
            , period * TICKS_PER_SECOND
            , period * TICKS_PER_SECOND
        );
        
        new PlayerListener(this);
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
    }
    
    static void updatePlayer(Player player) {
        Main.isUpdated = true;
        synchronized(Main.lastSeen) {
            Main.lastSeen.put(player, Main.timestamp.format(new Date()));
        }
    }
    
    static void removePlayer(Player player) {
        Main.isUpdated = true;
        synchronized(Main.lastSeen) {
            Main.lastSeen.remove(player);
        }
    }
    
    @SuppressWarnings("unchecked")
    static JSONArray getJson() {
        if (Main.isUpdated == false) { return null; }
        Main.isUpdated = false;
        
        JSONArray jsonList = new JSONArray();
        JSONObject out;
        
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            out = new JSONObject();
            out.put("msg", p.getName());
            out.put("id", 4);
            out.put("world", p.getLocation().getWorld().getName());
            out.put("x", p.getLocation().getX());
            out.put("y", p.getLocation().getY());
            out.put("z", p.getLocation().getZ());
            synchronized(Main.lastSeen) {
                String s = Main.lastSeen.get(p);
                if(s != null) out.put("timestamp", s);
            }
            jsonList.add(out);
        }
        return jsonList;
    }
}