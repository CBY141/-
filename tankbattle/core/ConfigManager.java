package main.java.com.tankbattle.core;

import main.java.com.tankbattle.utils.LogUtil;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;

/**
 * 配置管理器 - 单例模式
 * 负责管理所有游戏配置，支持从配置文件加载
 */
public class ConfigManager {
    private static volatile ConfigManager instance;
    private Properties configProps = new Properties();
    private Map<String, Object> runtimeConfig = new HashMap<>();

    public static final String KEY_WINDOW_WIDTH = "window.width";
    public static final String KEY_WINDOW_HEIGHT = "window.height";
    public static final String KEY_WINDOW_TITLE = "window.title";
    public static final String KEY_TILE_SIZE = "tile.size";
    public static final String KEY_MAP_WIDTH = "map.width";
    public static final String KEY_MAP_HEIGHT = "map.height";
    public static final String KEY_TANK_WIDTH = "tank.width";
    public static final String KEY_TANK_HEIGHT = "tank.height";
    public static final String KEY_TANK_TURRET_LENGTH = "tank.turret.length";
    public static final String KEY_PLAYER_LIVES = "player.lives";
    public static final String KEY_ENEMY_COUNT = "enemy.count";
    public static final String KEY_PLAYER_SPEED = "player.speed";
    public static final String KEY_ENEMY_SPEED = "enemy.speed";
    public static final String KEY_BULLET_SPEED = "bullet.speed";
    public static final String KEY_BULLET_SIZE = "bullet.size";

    public static final int TILE_EMPTY = 0;
    public static final int TILE_BRICK = 1;
    public static final int TILE_STEEL = 2;
    public static final int TILE_GRASS = 3;
    public static final int TILE_WATER = 4;

    public static final int DIR_UP = 0;
    public static final int DIR_DOWN = 1;
    public static final int DIR_LEFT = 2;
    public static final int DIR_RIGHT = 3;

    public static final Color PLAYER_TANK_COLOR = new Color(0, 180, 0);
    public static final Color ENEMY_TANK_COLOR = new Color(220, 0, 0);
    public static final Color TANK_TURRET_COLOR = new Color(100, 100, 100);
    public static final Color TANK_DETAIL_COLOR = new Color(60, 60, 60);

    private static final String CONFIG_FILE_NAME = "game.properties";

    private ConfigManager() {
        loadDefaultConfig();
        boolean loaded = loadConfigFile();

        if (!loaded) {
            createDefaultConfigFile();
        }
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    private void loadDefaultConfig() {
        runtimeConfig.put(KEY_WINDOW_WIDTH, 800);
        runtimeConfig.put(KEY_WINDOW_HEIGHT, 600);
        runtimeConfig.put(KEY_WINDOW_TITLE, "像素坦克大战 - 精致版");
        runtimeConfig.put(KEY_TILE_SIZE, 16);
        runtimeConfig.put(KEY_MAP_WIDTH, 50);
        runtimeConfig.put(KEY_MAP_HEIGHT, 37);
        runtimeConfig.put(KEY_TANK_WIDTH, 20);
        runtimeConfig.put(KEY_TANK_HEIGHT, 20);
        runtimeConfig.put(KEY_TANK_TURRET_LENGTH, 12);
        runtimeConfig.put(KEY_PLAYER_LIVES, 3);
        runtimeConfig.put(KEY_ENEMY_COUNT, 10);
        runtimeConfig.put(KEY_PLAYER_SPEED, 2);
        runtimeConfig.put(KEY_ENEMY_SPEED, 1);
        runtimeConfig.put(KEY_BULLET_SPEED, 6);
        runtimeConfig.put(KEY_BULLET_SIZE, 3);
    }

    private boolean loadConfigFile() {
        try {
            String[] configPaths = {
                    "config/" + CONFIG_FILE_NAME,
                    "./" + CONFIG_FILE_NAME,
                    "src/config/" + CONFIG_FILE_NAME
            };

            for (String path : configPaths) {
                File configFile = new File(path);
                if (configFile.exists() && configFile.isFile()) {
                    try (InputStream input = new FileInputStream(configFile)) {
                        configProps.load(input);
                        applyConfigFromFile();
                        LogUtil.info("✓ 已加载配置文件: " + configFile.getAbsolutePath());
                        return true;
                    } catch (IOException e) {
                        LogUtil.warning("✗ 读取配置文件失败 (" + path + "): " + e.getMessage());
                    }
                }
            }

            LogUtil.info("ℹ 未找到配置文件，使用默认配置");
            return false;

        } catch (Exception e) {
            LogUtil.warning("✗ 加载配置文件异常: " + e.getMessage());
            return false;
        }
    }

    private void applyConfigFromFile() {
        int loadedCount = 0;
        int errorCount = 0;

        if (applyIntConfig(KEY_WINDOW_WIDTH, "window.width")) loadedCount++;
        else if (configProps.containsKey("window.width")) errorCount++;

        if (applyIntConfig(KEY_WINDOW_HEIGHT, "window.height")) loadedCount++;
        else if (configProps.containsKey("window.height")) errorCount++;

        if (applyStringConfig(KEY_WINDOW_TITLE, "window.title")) loadedCount++;
        else if (configProps.containsKey("window.title")) errorCount++;

        if (applyIntConfig(KEY_TILE_SIZE, "tile.size")) loadedCount++;
        else if (configProps.containsKey("tile.size")) errorCount++;

        if (applyIntConfig(KEY_MAP_WIDTH, "map.width")) loadedCount++;
        else if (configProps.containsKey("map.width")) errorCount++;

        if (applyIntConfig(KEY_MAP_HEIGHT, "map.height")) loadedCount++;
        else if (configProps.containsKey("map.height")) errorCount++;

        if (applyIntConfig(KEY_TANK_WIDTH, "tank.width")) loadedCount++;
        else if (configProps.containsKey("tank.width")) errorCount++;

        if (applyIntConfig(KEY_TANK_HEIGHT, "tank.height")) loadedCount++;
        else if (configProps.containsKey("tank.height")) errorCount++;

        if (applyIntConfig(KEY_TANK_TURRET_LENGTH, "tank.turret.length")) loadedCount++;
        else if (configProps.containsKey("tank.turret.length")) errorCount++;

        if (applyIntConfig(KEY_PLAYER_LIVES, "player.lives")) loadedCount++;
        else if (configProps.containsKey("player.lives")) errorCount++;

        if (applyIntConfig(KEY_ENEMY_COUNT, "enemy.count")) loadedCount++;
        else if (configProps.containsKey("enemy.count")) errorCount++;

        if (applyIntConfig(KEY_PLAYER_SPEED, "player.speed")) loadedCount++;
        else if (configProps.containsKey("player.speed")) errorCount++;

        if (applyIntConfig(KEY_ENEMY_SPEED, "enemy.speed")) loadedCount++;
        else if (configProps.containsKey("enemy.speed")) errorCount++;

        if (applyIntConfig(KEY_BULLET_SPEED, "bullet.speed")) loadedCount++;
        else if (configProps.containsKey("bullet.speed")) errorCount++;

        if (applyIntConfig(KEY_BULLET_SIZE, "bullet.size")) loadedCount++;
        else if (configProps.containsKey("bullet.size")) errorCount++;

        LogUtil.info("ℹ 配置加载结果: " + loadedCount + " 项成功, " + errorCount + " 项格式错误");
    }

    private boolean applyIntConfig(String key, String propKey) {
        String value = configProps.getProperty(propKey);
        if (value != null) {
            try {
                int intValue = Integer.parseInt(value.trim());
                runtimeConfig.put(key, intValue);
                return true;
            } catch (NumberFormatException e) {
                LogUtil.warning("✗ 配置项 " + propKey + " 格式错误: '" + value + "'，使用默认值: " + runtimeConfig.get(key));
                return false;
            }
        }
        return false;
    }

    private boolean applyStringConfig(String key, String propKey) {
        String value = configProps.getProperty(propKey);
        if (value != null) {
            runtimeConfig.put(key, value.trim());
            return true;
        }
        return false;
    }

    private void createDefaultConfigFile() {
        try {
            File configDir = new File("config");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            File configFile = new File("config/" + CONFIG_FILE_NAME);

            configProps.setProperty("window.width", "800");
            configProps.setProperty("window.height", "600");
            configProps.setProperty("window.title", "像素坦克大战 - 精致版");
            configProps.setProperty("tile.size", "16");
            configProps.setProperty("map.width", "50");
            configProps.setProperty("map.height", "37");
            configProps.setProperty("tank.width", "20");
            configProps.setProperty("tank.height", "20");
            configProps.setProperty("tank.turret.length", "12");
            configProps.setProperty("player.lives", "3");
            configProps.setProperty("enemy.count", "10");
            configProps.setProperty("player.speed", "2");
            configProps.setProperty("enemy.speed", "1");
            configProps.setProperty("bullet.speed", "6");
            configProps.setProperty("bullet.size", "3");

            try (FileOutputStream output = new FileOutputStream(configFile)) {
                configProps.store(output, "坦克大战游戏配置");
                LogUtil.info("✓ 已创建默认配置文件: " + configFile.getAbsolutePath());
            }

        } catch (IOException e) {
            LogUtil.severe("✗ 创建默认配置文件失败: " + e.getMessage());
        }
    }

    public int getInt(String key) {
        Object value = runtimeConfig.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                LogUtil.warning("✗ 配置项 " + key + " 转换失败，返回默认值0");
                return 0;
            }
        } else if (value == null) {
            LogUtil.warning("⚠ 配置项 " + key + " 不存在，返回默认值0");
            return 0;
        }
        return 0;
    }

    public String getString(String key) {
        Object value = runtimeConfig.get(key);
        if (value != null) {
            return value.toString();
        } else {
            LogUtil.warning("⚠ 配置项 " + key + " 不存在，返回空字符串");
            return "";
        }
    }

    public void setConfig(String key, Object value) {
        runtimeConfig.put(key, value);
    }

    public boolean saveConfig() {
        try {
            File configDir = new File("config");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            File configFile = new File("config/" + CONFIG_FILE_NAME);

            configProps.clear();
            configProps.setProperty("window.width", String.valueOf(getInt(KEY_WINDOW_WIDTH)));
            configProps.setProperty("window.height", String.valueOf(getInt(KEY_WINDOW_HEIGHT)));
            configProps.setProperty("window.title", getString(KEY_WINDOW_TITLE));
            configProps.setProperty("tile.size", String.valueOf(getInt(KEY_TILE_SIZE)));
            configProps.setProperty("map.width", String.valueOf(getInt(KEY_MAP_WIDTH)));
            configProps.setProperty("map.height", String.valueOf(getInt(KEY_MAP_HEIGHT)));
            configProps.setProperty("tank.width", String.valueOf(getInt(KEY_TANK_WIDTH)));
            configProps.setProperty("tank.height", String.valueOf(getInt(KEY_TANK_HEIGHT)));
            configProps.setProperty("tank.turret.length", String.valueOf(getInt(KEY_TANK_TURRET_LENGTH)));
            configProps.setProperty("player.lives", String.valueOf(getInt(KEY_PLAYER_LIVES)));
            configProps.setProperty("enemy.count", String.valueOf(getInt(KEY_ENEMY_COUNT)));
            configProps.setProperty("player.speed", String.valueOf(getInt(KEY_PLAYER_SPEED)));
            configProps.setProperty("enemy.speed", String.valueOf(getInt(KEY_ENEMY_SPEED)));
            configProps.setProperty("bullet.speed", String.valueOf(getInt(KEY_BULLET_SPEED)));
            configProps.setProperty("bullet.size", String.valueOf(getInt(KEY_BULLET_SIZE)));

            try (FileOutputStream output = new FileOutputStream(configFile)) {
                configProps.store(output, "坦克大战游戏配置");
                LogUtil.info("✓ 配置已保存到文件: " + configFile.getAbsolutePath());
                return true;
            }

        } catch (IOException e) {
            LogUtil.severe("✗ 保存配置文件失败: " + e.getMessage());
            return false;
        }
    }

    public void printCurrentConfig() {
        LogUtil.info("========== 当前游戏配置 ==========");
        LogUtil.info("窗口: " + getInt(KEY_WINDOW_WIDTH) + "x" + getInt(KEY_WINDOW_HEIGHT));
        LogUtil.info("地图: " + getInt(KEY_MAP_WIDTH) + "x" + getInt(KEY_MAP_HEIGHT) +
                " (瓦片大小: " + getInt(KEY_TILE_SIZE) + ")");
        LogUtil.info("坦克: " + getInt(KEY_TANK_WIDTH) + "x" + getInt(KEY_TANK_HEIGHT));
        LogUtil.info("玩家: 生命" + getInt(KEY_PLAYER_LIVES) +
                " 速度" + getInt(KEY_PLAYER_SPEED));
        LogUtil.info("敌人: 数量" + getInt(KEY_ENEMY_COUNT) +
                " 速度" + getInt(KEY_ENEMY_SPEED));
        LogUtil.info("子弹: 速度" + getInt(KEY_BULLET_SPEED) +
                " 大小" + getInt(KEY_BULLET_SIZE));
        LogUtil.info("=================================");
    }
}