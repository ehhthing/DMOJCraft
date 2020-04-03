package science.larry.dmojcraft.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import science.larry.dmojcraft.util.RateLimiter;

public class BookCommand implements CommandExecutor {
    RateLimiter limiter = new RateLimiter(2, 1);

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        if (limiter.invoke(player)) {
            commandSender.sendMessage(ChatColor.RED + "Slow down! (2 books per second)");
            return true;
        }
        player.getInventory().addItem(new ItemStack(Material.WRITABLE_BOOK));
        commandSender.sendMessage(ChatColor.GREEN + "You can use /submit [PROBLEM ID] [LANGUAGE] to submit your code.");
        return true;
    }
}
