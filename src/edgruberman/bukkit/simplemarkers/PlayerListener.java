package edgruberman.bukkit.simplemarkers;

import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {
    
    private Main main;
    
    public PlayerListener(Main main) {
        this.main = main;
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        main.updatePlayer(event.getPlayer());
    }
    
    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        main.updatePlayer(event.getPlayer());
    }
    
    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        main.updatePlayer(event.getPlayer());
    }
    
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        main.removePlayer(event.getPlayer());
    }
}