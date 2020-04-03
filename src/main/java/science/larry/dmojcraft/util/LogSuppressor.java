package science.larry.dmojcraft.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import java.util.logging.Logger;
import java.util.regex.Pattern;

public class LogSuppressor extends AbstractFilter {
    private static final Pattern regex = Pattern.compile("^[a-zA-Z0-9_]{1,16} issued server command: /auth .*$");

    private static Result check(Message message) {
        if (message == null) {
            return Result.NEUTRAL;
        }
        return check(message.getFormattedMessage());
    }


    private static Result check(String message) {
        return regex.matcher(message).matches() ? Result.DENY : Result.NEUTRAL;
    }

    @Override
    public Result filter(LogEvent event) {
        Message candidate = null;
        if (event != null) {
            candidate = event.getMessage();
        }
        return check(candidate);
    }

    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return check(msg);
    }

    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return check(msg);
    }

    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        String candidate = null;
        if (msg != null) {
            candidate = msg.toString();
        }
        return check(candidate);
    }
}
