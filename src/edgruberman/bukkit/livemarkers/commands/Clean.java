package edgruberman.bukkit.livemarkers.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import edgruberman.bukkit.livemarkers.MarkerCache;
import edgruberman.bukkit.livemarkers.MarkerWriter;

public class Clean implements CommandExecutor {

    private final MarkerWriter writer;

    public Clean(final MarkerWriter writer) {
        this.writer = writer;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§7-> Required argument §cmissing§7: §f§o<Cache>");
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
            sender.sendMessage("§7-> Unable to clean marker cache; Cache §enot found§7: §f" + search);
            return true;
        }

        target.clean();
        sender.sendMessage("§7-> Marker cache §acleaned§7: §f" + target.getClass().getSimpleName());
        return true;
    }

}
