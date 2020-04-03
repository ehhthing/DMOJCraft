package science.larry.dmojcraft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import science.larry.dmojcraft.commands.AuthCommand;
import science.larry.dmojcraft.commands.BookCommand;
import science.larry.dmojcraft.commands.SubmitCommand;
import science.larry.dmojcraft.util.LogSuppressor;
import science.larry.dmojcraft.util.SessionCache;

import java.util.Iterator;
import java.util.Objects;

public final class DMOJCraft extends JavaPlugin {
    public static SessionCache authCache;
    public static java.util.logging.Logger logger;

    private void loadLogSuppressor() {
        boolean alreadyLoaded = false;
        org.apache.logging.log4j.core.Logger rootLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();

        for (Iterator<Filter> it = ((Logger) LogManager.getRootLogger()).getFilters(); it.hasNext(); ) {
            Filter filter = it.next();
            if (filter.getClass().getName().equals(LogSuppressor.class.getName())) {
                logger.info("Skipping loading log filter since it is already loaded.");
                alreadyLoaded = true;
                break;
            }
        }

        if (!alreadyLoaded) {
            rootLogger.addFilter(new LogSuppressor());
            logger.info("Loaded log filter.");
        }
    }

    @Override
    public void onEnable() {
        authCache = new SessionCache();
        logger = getLogger();
        loadLogSuppressor();

        Objects.requireNonNull(this.getCommand("auth")).setExecutor(new AuthCommand());
        Objects.requireNonNull(this.getCommand("submit")).setExecutor(new SubmitCommand());
        Objects.requireNonNull(this.getCommand("book")).setExecutor(new BookCommand());
    }

    @Override
    public void onDisable() {
    }
}
