package main.java.com.tankbattle.managers;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.system.*;

import java.io.*;
import java.util.*;

/**
 * 存档管理器 - 单例模式
 */
public class SaveManager {
    private static SaveManager instance;
    private SaveSystem saveSystem;

    // 自动保存配置
    private boolean autoSaveEnabled = true;
    private int autoSaveInterval = 300; // 每5分钟（300秒）
    private float autoSaveTimer = 0;

    private SaveManager() {
        // 使用真实的文件系统存档
        saveSystem = new RealFileSaveSystem();
    }

    public static synchronized SaveManager getInstance() {
        if (instance == null) {
            instance = new SaveManager();
        }
        return instance;
    }

    public void setSaveSystem(SaveSystem saveSystem) {
        this.saveSystem = saveSystem;
    }

    public SaveSystem getSaveSystem() {
        return saveSystem;
    }

    // 快速保存
    public boolean quickSave() {
        String saveName = "quicksave_" + System.currentTimeMillis();
        return saveGame(saveName);
    }

    // 快速加载
    public SaveData quickLoad() {
        String[] saves = listSaves();
        if (saves.length == 0) {
            System.out.println("没有找到存档文件");
            return null;
        }

        // 获取最新的快速存档
        String latestSave = null;
        for (String save : saves) {
            if (save.startsWith("quicksave_")) {
                if (latestSave == null || save.compareTo(latestSave) > 0) {
                    latestSave = save;
                }
            }
        }

        if (latestSave != null) {
            return loadGame(latestSave);
        } else if (saves.length > 0) {
            // 没有快速存档，加载最新的存档
            Arrays.sort(saves);
            return loadGame(saves[saves.length - 1]);
        }

        return null;
    }

    // 手动保存
    public boolean saveGame(String saveName) {
        if (saveName == null || saveName.trim().isEmpty()) {
            saveName = "save_" + System.currentTimeMillis();
        }

        // 创建存档数据
        SaveData saveData = new SaveData(saveName);

        // 保存游戏配置
        saveConfigData(saveData);

        // 保存游戏状态
        saveGameState(saveData);

        // 保存玩家数据
        savePlayerData(saveData);

        // 保存统计信息
        saveStatsData(saveData);

        // 调用存档系统保存
        boolean result = saveSystem.saveGame(saveData);
        if (result) {
            System.out.println("游戏已保存: " + saveName);
            EventManager.getInstance().triggerEvent(
                    new GameEvent(Event.EventType.GAME_SAVED, null, saveName)
            );
        } else {
            System.out.println("保存游戏失败: " + saveName);
        }

        return result;
    }

    public SaveData loadGame(String saveName) {
        SaveData saveData = saveSystem.loadGame(saveName);
        if (saveData != null) {
            System.out.println("游戏已加载: " + saveName);
            EventManager.getInstance().triggerEvent(
                    new GameEvent(Event.EventType.GAME_LOADED, null, saveName)
            );
        } else {
            System.out.println("加载游戏失败: " + saveName);
        }
        return saveData;
    }

    public boolean deleteGame(String saveName) {
        boolean result = saveSystem.deleteGame(saveName);
        if (result) {
            System.out.println("存档已删除: " + saveName);
        }
        return result;
    }

    public String[] listSaves() {
        return saveSystem.listSaves();
    }

    public boolean saveExists(String saveName) {
        return saveSystem.saveExists(saveName);
    }

    public void update(float deltaTime) {
        if (!autoSaveEnabled) return;

        autoSaveTimer += deltaTime;
        if (autoSaveTimer >= autoSaveInterval) {
            autoSave();
            autoSaveTimer = 0;
        }
    }

    private void autoSave() {
        String saveName = "autosave_" + System.currentTimeMillis();
        saveGame(saveName);
        System.out.println("自动保存完成: " + saveName);
    }

    private void saveConfigData(SaveData saveData) {
        ConfigManager config = ConfigManager.getInstance();
        saveData.put("windowWidth", config.getInt(ConfigManager.KEY_WINDOW_WIDTH));
        saveData.put("windowHeight", config.getInt(ConfigManager.KEY_WINDOW_HEIGHT));
        saveData.put("playerLives", config.getInt(ConfigManager.KEY_PLAYER_LIVES));
        saveData.put("enemyCount", config.getInt(ConfigManager.KEY_ENEMY_COUNT));

        // 保存当前时间
        saveData.put("saveTime", System.currentTimeMillis());
        saveData.put("saveDate", new Date().toString());
    }

    private void saveGameState(SaveData saveData) {
        GameManager gameManager = GameManager.getInstance();
        saveData.put("gameState", gameManager.getCurrentStateType().name());

        // 这里可以扩展保存更多游戏状态
        // 例如：敌人位置、子弹位置、地图状态等

        saveData.put("gameVersion", "1.0.0");
    }

    private void savePlayerData(SaveData saveData) {
        // 在实际游戏中，这里会保存玩家的具体数据
        // 例如：位置、生命值、分数、装备等
        saveData.put("playerScore", 0);
        saveData.put("playerLevel", 1);
    }

    private void saveStatsData(SaveData saveData) {
        // 保存游戏统计
        saveData.put("playTime", 0);
        saveData.put("enemiesKilled", 0);
        saveData.put("shotsFired", 0);

        // 保存成就进度
        AchievementManager am = AchievementManager.getInstance();
        Achievement[] achievements = am.getAllAchievements();
        Map<String, Boolean> achievementStatus = new HashMap<>();
        for (Achievement achievement : achievements) {
            achievementStatus.put(achievement.getId(), achievement.isUnlocked());
        }
        saveData.put("achievements", achievementStatus);
    }

    // 真实的文件系统存档实现
    private static class RealFileSaveSystem implements SaveSystem {
        private static final String SAVE_DIR = "saves/";

        public RealFileSaveSystem() {
            // 确保保存目录存在
            File saveDir = new File(SAVE_DIR);
            if (!saveDir.exists()) {
                if (saveDir.mkdirs()) {
                    System.out.println("创建存档目录: " + saveDir.getAbsolutePath());
                }
            }
        }

        @Override
        public boolean saveGame(SaveData saveData) {
            try {
                String saveName = saveData.getSaveName();
                File saveFile = new File(SAVE_DIR + saveName + ".sav");

                try (ObjectOutputStream oos = new ObjectOutputStream(
                        new FileOutputStream(saveFile))) {
                    oos.writeObject(saveData);
                }

                return true;
            } catch (IOException e) {
                System.err.println("保存游戏失败: " + e.getMessage());
                return false;
            }
        }

        @Override
        public SaveData loadGame(String saveName) {
            try {
                File saveFile = new File(SAVE_DIR + saveName + ".sav");
                if (!saveFile.exists()) {
                    return null;
                }

                try (ObjectInputStream ois = new ObjectInputStream(
                        new FileInputStream(saveFile))) {
                    return (SaveData) ois.readObject();
                }
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("加载游戏失败: " + e.getMessage());
                return null;
            }
        }

        @Override
        public boolean deleteGame(String saveName) {
            try {
                File saveFile = new File(SAVE_DIR + saveName + ".sav");
                return saveFile.delete();
            } catch (Exception e) {
                System.err.println("删除存档失败: " + e.getMessage());
                return false;
            }
        }

        @Override
        public String[] listSaves() {
            File saveDir = new File(SAVE_DIR);
            if (!saveDir.exists() || !saveDir.isDirectory()) {
                return new String[0];
            }

            File[] saveFiles = saveDir.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".sav"));

            if (saveFiles == null) {
                return new String[0];
            }

            String[] saves = new String[saveFiles.length];
            for (int i = 0; i < saveFiles.length; i++) {
                String name = saveFiles[i].getName();
                saves[i] = name.substring(0, name.length() - 4); // 移除.sav扩展名
            }

            return saves;
        }

        @Override
        public boolean saveExists(String saveName) {
            File saveFile = new File(SAVE_DIR + saveName + ".sav");
            return saveFile.exists();
        }
    }
}