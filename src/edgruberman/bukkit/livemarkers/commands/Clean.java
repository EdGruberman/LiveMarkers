package edgruberman.bukkit.livemarkers.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.livemarkers.MarkerWriter;
import edgruberman.bukkit.livemarkers.caches.MarkerCache;

public class Clean implements CommandExecutor {

    private final MarkerWriter writer;

    public Clean(final MarkerWriter writer) {
        this.writer = writer;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Syntax error; Missing <Cache> argument");
            return false;
        }

        String search = args[0].toLowerCase();
        if (!search.contains(".")) search = MarkerCache.class.getPackage().getName() + "." + search;
        MarkerCache target = null;
        for (final MarkerCache cache : this.writer.caches) {
            if (cache.getClass().getPackage().getName().equalsIgnoreCase(search)) {
                target = cache;
                break;
            }
        }

        if (target == null) {
            sender.sendMessage(ChatColor.YELLOW + "Unable to clean marker cache; Cache not found: " + ChatColor.WHITE + search);
            return true;
        }

        target.clean();
        sender.sendMessage(ChatColor.GREEN + "Marker cache cleaned: " + ChatColor.WHITE + target.getClass().getSimpleName());
        return true;
    }

}
