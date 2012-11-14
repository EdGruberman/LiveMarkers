package edgruberman.bukkit.livemarkers.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public final class Reload implements CommandExecutor {

    private final Plugin plugin;

    public Reload(final Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        this.plugin.onDisable();
        this.plugin.onEnable();
        sender.sendMessage("§7-> " + this.plugin.getName() + " plugin §2reloaded");
        return true;
    }

}
