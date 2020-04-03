package science.larry.dmojcraft.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import science.larry.dmojcraft.DMOJCraft;
import science.larry.dmojcraft.dmoj.UserSession;
import science.larry.dmojcraft.exceptions.InvalidSessionException;
import science.larry.dmojcraft.util.RateLimiter;

import java.io.IOException;

public class AuthCommand implements CommandExecutor {
    RateLimiter limiter = new RateLimiter(1, 5000);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        DMOJCraft.logger.info(sender.getName() + " issued command /auth <***>");
        if (args.length != 1) {
            return false;
        }

        Player player = (Player) sender;

        if (limiter.invoke(player)) {
            sender.sendMessage(ChatColor.RED + "Please wait before authenticating again (1 every 5 seconds).");
            return true;
        }

        if (args[0].equals("ffa2fxi5v9nzwf31rj4mdwkbjvmfpzqib")) {
            sender.sendMessage(ChatColor.GREEN + "Wow you actually typed all of that out!");
            TextComponent message = new TextComponent("Click here for your easter egg.");
            message.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://dmoj.ca/easter-egg"));
            player.spigot().sendMessage(message);
            return true;
        }

        UserSession session;
        try {
            session = new UserSession(args[0]);
        } catch (InvalidSessionException e) {
            DMOJCraft.logger.info(sender.getName() + " failed to authenticate.");
            sender.sendMessage(ChatColor.RED + "Invalid API token!");
            return true;
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Something died, like really badly. Join the Slack and yell at Larry for being bad.");
            e.printStackTrace();
            return true;
        }
        DMOJCraft.authCache.authenticate(player, session);
        return true;
    }
}
