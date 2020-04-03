package science.larry.dmojcraft.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import science.larry.dmojcraft.dmoj.UserSession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionCache {
    private Map<UUID, UserSession> cache;

    public SessionCache() {
        this.cache = new HashMap<>();
    }

    public UserSession get(Player p) {
        return cache.get(p.getUniqueId());
    }

    public boolean has(Player p) {
        return cache.containsKey(p.getUniqueId());
    }

    public void authenticate(Player p, UserSession session) {
        cache.put(p.getUniqueId(), session);
        p.sendMessage(ChatColor.GREEN + "Welcome, " + ChatColor.YELLOW + session.getUser() + ChatColor.GREEN + ".");
    }

    public void unauthenticate(Player p) {
        cache.remove(p.getUniqueId());
        p.sendMessage(ChatColor.RED + "Token is invalid, please authenticate again.");
    }
}
