package main.java.com.tankbattle.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 日志工具类 - 统一日志输出
 */
public class LogUtil {
    private static final Logger LOGGER = Logger.getLogger("TankWar");

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new java.util.logging.SimpleFormatter() {
            @Override
            public synchronized String format(java.util.logging.LogRecord record) {
                return String.format("[%s] %s%n", record.getLevel().getName(), record.getMessage());
            }
        });
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.INFO);
    }

    public static void info(String msg) {
        LOGGER.info(msg);
    }

    public static void warning(String msg) {
        LOGGER.warning(msg);
    }

    public static void severe(String msg) {
        LOGGER.severe(msg);
    }

    public static void debug(String msg) {
        LOGGER.fine(msg);
    }

    public static void setLevel(Level level) {
        LOGGER.setLevel(level);
    }
}