package edgruberman.bukkit.livemarkers.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import edgruberman.bukkit.livemarkers.MarkerCache;
import edgruberman.bukkit.livemarkers.MarkerWriter;

public class Clean implements CommandExecutor {

    private MarkerWriter writer;

    public Clean(final JavaPlugin plugin, final String label, final MarkerWriter writer) {
        this.writer = writer;

        final PluginCommand command = plugin.getCommand(label);
        if (command == null) {
            plugin.getLogger().warning("Unable to get plugin command: " + label);
            return;
        }

        command.setExecutor(this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Syntax error; Missing <Cache> argument");
            return false;
        }

        String search = args[0].toLowerCase();
        if (!search.contains(".")) search = MarkerWriter.internalCaches + "." + search;
        MarkerCache target = null;
        for (final MarkerCache cache : this.writer.caches) {
            if (cache.getClass().getPackage().getName().equalsIgnoreCase(search)) {
                target = cache;
                break;
            }
        }

        if (target == null) {
            sender.sendMessage(ChatColor.YELLOW + "Marker cache not found: " + ChatColor.WHITE + search);
            return true;
        }

        target.clean();
        sender.sendMessage(ChatColor.GREEN + "Cache cleaned: " + ChatColor.WHITE + target.getClass().getSimpleName());
        return true;
    }

}
