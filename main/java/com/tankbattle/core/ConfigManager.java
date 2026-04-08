package main.java.com.tankbattle.core;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;

/**
 * 配置管理器 - 单例模式
 * 负责管理所有游戏配置，支持从配置文件加载
 */
public class ConfigManager {
    private static ConfigManager instance;
    private Properties configProps;
    private Map<String, Object> runtimeConfig = new HashMap<>();

    // 游戏配置常量
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

    // 瓦片类型常量
    public static final int TILE_EMPTY = 0;
    public static final int TILE_BRICK = 1;
    public static final int TILE_STEEL = 2;
    public static final int TILE_GRASS = 3;
    public static final int TILE_WATER = 4;

    // 方向常量
    public static final int DIR_UP = 0;
    public static final int DIR_DOWN = 1;
    public static final int DIR_LEFT = 2;
    public static final int DIR_RIGHT = 3;

    // 颜色配置
    public static final Color PLAYER_TANK_COLOR = new Color(0, 180, 0);
    public static final Color ENEMY_TANK_COLOR = new Color(220, 0, 0);
    public static final Color TANK_TURRET_COLOR = new Color(100, 100, 100);
    public static final Color TANK_DETAIL_COLOR = new Color(60, 60, 60);

    // 私有构造器
    private ConfigManager() {
        configProps = new Properties();
        loadDefaultConfig();
        loadConfigFile();
    }

    // 获取单例实例
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

    // 加载默认配置
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

    // 从配置文件加载配置
    private void loadConfigFile() {
        try {
            // 尝试从多个位置加载配置文件
            String[] configPaths = {
                    "config/game.properties",       // 项目根目录
                    "./config/game.properties",     // 当前目录
                    "src/config/game.properties"    // src目录
            };

            for (String path : configPaths) {
                File configFile = new File(path);
                if (configFile.exists() && configFile.isFile()) {
                    try (InputStream input = new FileInputStream(configFile)) {
                        configProps.load(input);
                        applyConfigFromFile();
                        System.out.println("已加载配置文件: " + path);
                        return;
                    }
                }
            }

            System.out.println("未找到配置文件，使用默认配置");

        } catch (Exception e) {
            System.out.println("加载配置文件失败: " + e.getMessage());
        }
    }

    // 应用配置文件中的配置
    private void applyConfigFromFile() {
        // 窗口配置
        applyIntConfig(KEY_WINDOW_WIDTH, "window.width");
        applyIntConfig(KEY_WINDOW_HEIGHT, "window.height");
        applyStringConfig(KEY_WINDOW_TITLE, "window.title");

        // 地图配置
        applyIntConfig(KEY_TILE_SIZE, "tile.size");
        applyIntConfig(KEY_MAP_WIDTH, "map.width");
        applyIntConfig(KEY_MAP_HEIGHT, "map.height");

        // 坦克配置
        applyIntConfig(KEY_TANK_WIDTH, "tank.width");
        applyIntConfig(KEY_TANK_HEIGHT, "tank.height");
        applyIntConfig(KEY_TANK_TURRET_LENGTH, "tank.turret.length");
        applyIntConfig(KEY_PLAYER_LIVES, "player.lives");
        applyIntConfig(KEY_ENEMY_COUNT, "enemy.count");
        applyIntConfig(KEY_PLAYER_SPEED, "player.speed");
        applyIntConfig(KEY_ENEMY_SPEED, "enemy.speed");
        applyIntConfig(KEY_BULLET_SPEED, "bullet.speed");
        applyIntConfig(KEY_BULLET_SIZE, "bullet.size");
    }

    private void applyIntConfig(String key, String propKey) {
        String value = configProps.getProperty(propKey);
        if (value != null) {
            try {
                runtimeConfig.put(key, Integer.parseInt(value.trim()));
            } catch (NumberFormatException e) {
                System.out.println("配置项 " + propKey + " 格式错误: " + value);
            }
        }
    }

    private void applyStringConfig(String key, String propKey) {
        String value = configProps.getProperty(propKey);
        if (value != null) {
            runtimeConfig.put(key, value.trim());
        }
    }

    // 获取配置值的方法
    public int getInt(String key) {
        Object value = runtimeConfig.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public String getString(String key) {
        Object value = runtimeConfig.get(key);
        return value != null ? value.toString() : "";
    }

    // 运行时修改配置
    public void setConfig(String key, Object value) {
        runtimeConfig.put(key, value);
    }

    // 保存配置到文件（预留功能）
    public void saveConfig() {
        // TODO: 实现配置保存
        System.out.println("配置保存功能（预留）");
    }
}