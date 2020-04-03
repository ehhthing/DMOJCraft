package science.larry.dmojcraft.util;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class Status {
    int invocations;
    long startTime;
}

public class RateLimiter {
    Map<UUID, Status> statuses = new HashMap<>();
    int invocations, time;

    public RateLimiter(int invocations, int time) {
        this.invocations = invocations;
        this.time = time;
    }

    public boolean invoke(Player user) {
        UUID uuid = user.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Status userStatus = statuses.getOrDefault(uuid, new Status());
        if (userStatus.startTime == 0 || currentTime - userStatus.startTime > time) {
            userStatus.startTime = currentTime;
            userStatus.invocations = 1;
            statuses.put(uuid, userStatus);
            return false;
        } else if (userStatus.invocations < invocations) {
            userStatus.invocations++;
            return false;
        }
        return true;
    }
}
