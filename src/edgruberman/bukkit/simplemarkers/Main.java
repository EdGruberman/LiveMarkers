package edgruberman.bukkit.simplemarkers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import edgruberman.bukkit.messagemanager.MessageLevel;
import edgruberman.bukkit.messagemanager.MessageManager;

//TODO New Signs, PID
public class Main extends org.bukkit.plugin.java.JavaPlugin {
    
    public static MessageManager messageManager;
    
    public final HashMap<Player, String> lastSeen = new HashMap<Player, String>();
    
    private boolean isUpdated = true;
    private SimpleDateFormat timestamp;
    
    public void onLoad() {
        Configuration.load(this);
    }
	
    public void onEnable() {
        Main.messageManager = new MessageManager(this);
        Main.messageManager.log("Version " + this.getDescription().getVersion());
        
        int period = this.getConfiguration().getInt("period", 15);
        this.timestamp = new SimpleDateFormat(this.getConfiguration().getString("timestamp"));
        Main.messageManager.log(MessageLevel.CONFIG,
            "period: " + period + "s"
            + "; output: " + this.getConfiguration().getString("output")
        );
        
        getServer().getScheduler().scheduleSyncRepeatingTask(
              this
            , new WriteFileTimerTask(this, this.getConfiguration().getString("output"))
            , period * 20 // 1s = 20 ticks
            , period * 20 // 1s = 20 ticks
        );
        
        this.registerEvents();
        
        Main.messageManager.log("Plugin Enabled");
    }
    
    public void onDisable() {
        Main.messageManager.log("Plugin Disabled");
        Main.messageManager = null;
    }
    
    private void registerEvents() {
        PlayerListener playerListener = new PlayerListener(this);
        
        org.bukkit.plugin.PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.PLAYER_MOVE , playerListener, Event.Priority.Monitor, this);
        pluginManager.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Event.Priority.Monitor, this);
        pluginManager.registerEvent(Event.Type.PLAYER_QUIT , playerListener, Event.Priority.Monitor, this);
    }
    
    public void updatePlayer(Player player) {
        this.isUpdated = true;
        synchronized(lastSeen) {
            lastSeen.put(player, this.timestamp.format(new Date()));
        }
    }
    public void removePlayer(Player player) {
        this.isUpdated = true;
        synchronized(lastSeen) {
            lastSeen.remove(player);
        }
    }
    
    public JSONArray getJson() {
        if (this.isUpdated == false) { return null; }
        this.isUpdated = false;
        
        JSONArray jsonList = new JSONArray();
        JSONObject out;
        
        for (Player p : getServer().getOnlinePlayers()) {
            out = new JSONObject();
            out.put("msg", p.getName());
            out.put("id", 4);
            out.put("world", p.getLocation().getWorld().getName());
            out.put("x", p.getLocation().getX());
            out.put("y", p.getLocation().getY());
            out.put("z", p.getLocation().getZ());
            synchronized(lastSeen) {
                String s = lastSeen.get(p);
                if(s != null) out.put("timestamp", s);
            }
            jsonList.add(out);
        }
        return jsonList;
    }
    
}
