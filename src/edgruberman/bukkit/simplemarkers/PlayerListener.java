package edgruberman.bukkit.simplemarkers;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {
    
    public PlayerListener(Plugin plugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvent(Event.Type.PLAYER_MOVE , this, Event.Priority.Monitor, plugin);
        pluginManager.registerEvent(Event.Type.PLAYER_LOGIN, this, Event.Priority.Monitor, plugin);
        pluginManager.registerEvent(Event.Type.PLAYER_QUIT , this, Event.Priority.Monitor, plugin);
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        Main.updatePlayer(event.getPlayer());
    }
    
    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        Main.updatePlayer(event.getPlayer());
    }
    
    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Main.updatePlayer(event.getPlayer());
    }
    
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Main.removePlayer(event.getPlayer());
    }
}