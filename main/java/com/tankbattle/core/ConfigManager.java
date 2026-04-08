package main.java.com.tankbattle.core;

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
    private static ConfigManager instance;
    private Properties configProps = new Properties();
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

    // 配置文件名
    private static final String CONFIG_FILE_NAME = "game.properties";

    // 私有构造器
    private ConfigManager() {
        loadDefaultConfig();
        boolean loaded = loadConfigFile();

        // 如果配置文件不存在或加载失败，创建默认配置文件
        if (!loaded) {
            createDefaultConfigFile();
        }
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
    private boolean loadConfigFile() {
        try {
            // 尝试从多个位置加载配置文件
            String[] configPaths = {
                    "config/" + CONFIG_FILE_NAME,       // 项目根目录的config文件夹
                    "./" + CONFIG_FILE_NAME,            // 当前目录
                    "src/config/" + CONFIG_FILE_NAME    // src目录下的config文件夹
            };

            for (String path : configPaths) {
                File configFile = new File(path);
                if (configFile.exists() && configFile.isFile()) {
                    try (InputStream input = new FileInputStream(configFile)) {
                        configProps.load(input);
                        applyConfigFromFile();
                        System.out.println("✓ 已加载配置文件: " + configFile.getAbsolutePath());
                        return true;
                    } catch (IOException e) {
                        System.out.println("✗ 读取配置文件失败 (" + path + "): " + e.getMessage());
                    }
                }
            }

            System.out.println("ℹ 未找到配置文件，使用默认配置");
            return false;

        } catch (Exception e) {
            System.out.println("✗ 加载配置文件异常: " + e.getMessage());
            return false;
        }
    }

    // 应用配置文件中的配置
    private void applyConfigFromFile() {
        int loadedCount = 0;
        int errorCount = 0;

        // 窗口配置
        if (applyIntConfig(KEY_WINDOW_WIDTH, "window.width")) loadedCount++;
        else if (configProps.containsKey("window.width")) errorCount++;

        if (applyIntConfig(KEY_WINDOW_HEIGHT, "window.height")) loadedCount++;
        else if (configProps.containsKey("window.height")) errorCount++;

        if (applyStringConfig(KEY_WINDOW_TITLE, "window.title")) loadedCount++;
        else if (configProps.containsKey("window.title")) errorCount++;

        // 地图配置
        if (applyIntConfig(KEY_TILE_SIZE, "tile.size")) loadedCount++;
        else if (configProps.containsKey("tile.size")) errorCount++;

        if (applyIntConfig(KEY_MAP_WIDTH, "map.width")) loadedCount++;
        else if (configProps.containsKey("map.width")) errorCount++;

        if (applyIntConfig(KEY_MAP_HEIGHT, "map.height")) loadedCount++;
        else if (configProps.containsKey("map.height")) errorCount++;

        // 坦克配置
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

        // 子弹配置
        if (applyIntConfig(KEY_BULLET_SPEED, "bullet.speed")) loadedCount++;
        else if (configProps.containsKey("bullet.speed")) errorCount++;

        if (applyIntConfig(KEY_BULLET_SIZE, "bullet.size")) loadedCount++;
        else if (configProps.containsKey("bullet.size")) errorCount++;

        System.out.println("ℹ 配置加载结果: " + loadedCount + " 项成功, " + errorCount + " 项格式错误");
    }

    private boolean applyIntConfig(String key, String propKey) {
        String value = configProps.getProperty(propKey);
        if (value != null) {
            try {
                int intValue = Integer.parseInt(value.trim());
                runtimeConfig.put(key, intValue);
                return true;
            } catch (NumberFormatException e) {
                System.out.println("✗ 配置项 " + propKey + " 格式错误: '" + value + "'，使用默认值: " + runtimeConfig.get(key));
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

    // 创建默认配置文件
    private void createDefaultConfigFile() {
        try {
            File configDir = new File("config");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            File configFile = new File("config/" + CONFIG_FILE_NAME);

            // 设置配置属性
            configProps.setProperty("# 坦克大战游戏配置文件", "");
            configProps.setProperty("# 窗口配置", "");
            configProps.setProperty("window.width", "800");
            configProps.setProperty("window.height", "600");
            configProps.setProperty("window.title", "像素坦克大战 - 精致版");

            configProps.setProperty("# 地图配置", "");
            configProps.setProperty("tile.size", "16");
            configProps.setProperty("map.width", "50");
            configProps.setProperty("map.height", "37");

            configProps.setProperty("# 坦克配置", "");
            configProps.setProperty("tank.width", "20");
            configProps.setProperty("tank.height", "20");
            configProps.setProperty("tank.turret.length", "12");
            configProps.setProperty("player.lives", "3");
            configProps.setProperty("enemy.count", "10");
            configProps.setProperty("player.speed", "2");
            configProps.setProperty("enemy.speed", "1");

            configProps.setProperty("# 子弹配置", "");
            configProps.setProperty("bullet.speed", "6");
            configProps.setProperty("bullet.size", "3");

            // 保存到文件
            try (FileOutputStream output = new FileOutputStream(configFile)) {
                configProps.store(output, "坦克大战游戏配置");
                System.out.println("✓ 已创建默认配置文件: " + configFile.getAbsolutePath());
            }

        } catch (IOException e) {
            System.out.println("✗ 创建默认配置文件失败: " + e.getMessage());
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
                System.out.println("✗ 配置项 " + key + " 转换失败，返回默认值0");
                return 0;
            }
        } else if (value == null) {
            System.out.println("⚠ 配置项 " + key + " 不存在，返回默认值0");
            return 0;
        }
        return 0;
    }

    public String getString(String key) {
        Object value = runtimeConfig.get(key);
        if (value != null) {
            return value.toString();
        } else {
            System.out.println("⚠ 配置项 " + key + " 不存在，返回空字符串");
            return "";
        }
    }

    // 运行时修改配置
    public void setConfig(String key, Object value) {
        runtimeConfig.put(key, value);
    }

    // 保存配置到文件
    public boolean saveConfig() {
        try {
            File configDir = new File("config");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            File configFile = new File("config/" + CONFIG_FILE_NAME);

            // 更新Properties对象
            configProps.clear();
            configProps.setProperty("# 坦克大战游戏配置文件", "");
            configProps.setProperty("# 窗口配置", "");
            configProps.setProperty("window.width", String.valueOf(getInt(KEY_WINDOW_WIDTH)));
            configProps.setProperty("window.height", String.valueOf(getInt(KEY_WINDOW_HEIGHT)));
            configProps.setProperty("window.title", getString(KEY_WINDOW_TITLE));

            configProps.setProperty("# 地图配置", "");
            configProps.setProperty("tile.size", String.valueOf(getInt(KEY_TILE_SIZE)));
            configProps.setProperty("map.width", String.valueOf(getInt(KEY_MAP_WIDTH)));
            configProps.setProperty("map.height", String.valueOf(getInt(KEY_MAP_HEIGHT)));

            configProps.setProperty("# 坦克配置", "");
            configProps.setProperty("tank.width", String.valueOf(getInt(KEY_TANK_WIDTH)));
            configProps.setProperty("tank.height", String.valueOf(getInt(KEY_TANK_HEIGHT)));
            configProps.setProperty("tank.turret.length", String.valueOf(getInt(KEY_TANK_TURRET_LENGTH)));
            configProps.setProperty("player.lives", String.valueOf(getInt(KEY_PLAYER_LIVES)));
            configProps.setProperty("enemy.count", String.valueOf(getInt(KEY_ENEMY_COUNT)));
            configProps.setProperty("player.speed", String.valueOf(getInt(KEY_PLAYER_SPEED)));
            configProps.setProperty("enemy.speed", String.valueOf(getInt(KEY_ENEMY_SPEED)));

            configProps.setProperty("# 子弹配置", "");
            configProps.setProperty("bullet.speed", String.valueOf(getInt(KEY_BULLET_SPEED)));
            configProps.setProperty("bullet.size", String.valueOf(getInt(KEY_BULLET_SIZE)));

            // 保存到文件
            try (FileOutputStream output = new FileOutputStream(configFile)) {
                configProps.store(output, "坦克大战游戏配置");
                System.out.println("✓ 配置已保存到文件: " + configFile.getAbsolutePath());
                return true;
            }

        } catch (IOException e) {
            System.out.println("✗ 保存配置文件失败: " + e.getMessage());
            return false;
        }
    }

    // 打印当前配置（调试用）
    public void printCurrentConfig() {
        System.out.println("========== 当前游戏配置 ==========");
        System.out.println("窗口: " + getInt(KEY_WINDOW_WIDTH) + "x" + getInt(KEY_WINDOW_HEIGHT));
        System.out.println("地图: " + getInt(KEY_MAP_WIDTH) + "x" + getInt(KEY_MAP_HEIGHT) +
                " (瓦片大小: " + getInt(KEY_TILE_SIZE) + ")");
        System.out.println("坦克: " + getInt(KEY_TANK_WIDTH) + "x" + getInt(KEY_TANK_HEIGHT));
        System.out.println("玩家: 生命" + getInt(KEY_PLAYER_LIVES) +
                " 速度" + getInt(KEY_PLAYER_SPEED));
        System.out.println("敌人: 数量" + getInt(KEY_ENEMY_COUNT) +
                " 速度" + getInt(KEY_ENEMY_SPEED));
        System.out.println("子弹: 速度" + getInt(KEY_BULLET_SPEED) +
                " 大小" + getInt(KEY_BULLET_SIZE));
        System.out.println("=================================");
    }
}