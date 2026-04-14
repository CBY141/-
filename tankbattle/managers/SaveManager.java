package main.java.com.tankbattle.managers;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.system.*;
import main.java.com.tankbattle.utils.LogUtil;

import java.io.*;
import java.util.*;

public class SaveManager {
    private static volatile SaveManager instance;
    private SaveSystem saveSystem;

    private boolean autoSaveEnabled = true;
    private int autoSaveInterval = 300;
    private float autoSaveTimer = 0;

    private SaveManager() {
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

    // ========== 槽位管理 ==========
    public boolean saveToSlot(int slot, SaveData data) {
        if (slot < 1 || slot > 5) return false;
        String saveName = "slot" + slot;
        data.setSaveName(saveName);
        boolean result = saveSystem.saveGame(data);
        if (result) {
            LogUtil.info("存档已保存到槽位 " + slot);
            EventManager.getInstance().triggerEvent(
                    new GameEvent(Event.EventType.GAME_SAVED, null, saveName)
            );
        }
        return result;
    }

    public SaveData loadFromSlot(int slot) {
        if (slot < 1 || slot > 5) return null;
        return loadGame("slot" + slot);
    }

    public boolean isSlotExist(int slot) {
        return saveExists("slot" + slot);
    }

    public String getSlotInfo(int slot) {
        SaveData data = loadFromSlot(slot);
        if (data == null) return "空";
        long time = data.getTimestamp();
        Date date = new Date(time);
        return String.format("%tF %tR", date, date);
    }

    public void deleteSlot(int slot) {
        deleteGame("slot" + slot);
    }

    // ========== 原有方法 ==========
    public boolean quickSave() {
        String saveName = "quicksave_" + System.currentTimeMillis();
        return saveGame(saveName);
    }

    public SaveData quickLoad() {
        String[] saves = listSaves();
        if (saves.length == 0) return null;
        String latestSave = null;
        for (String save : saves) {
            if (save.startsWith("quicksave_")) {
                if (latestSave == null || save.compareTo(latestSave) > 0) {
                    latestSave = save;
                }
            }
        }
        if (latestSave != null) return loadGame(latestSave);
        Arrays.sort(saves);
        return loadGame(saves[saves.length - 1]);
    }

    public boolean saveGame(String saveName) {
        if (saveName == null || saveName.trim().isEmpty()) {
            saveName = "save_" + System.currentTimeMillis();
        }
        SaveData saveData = new SaveData(saveName);
        saveConfigData(saveData);
        saveGameState(saveData);
        savePlayerData(saveData);
        saveStatsData(saveData);

        boolean result = saveSystem.saveGame(saveData);
        if (result) {
            LogUtil.info("游戏已保存: " + saveName);
            EventManager.getInstance().triggerEvent(
                    new GameEvent(Event.EventType.GAME_SAVED, null, saveName)
            );
        }
        return result;
    }

    public boolean saveData(SaveData saveData) {
        if (saveData == null) return false;
        boolean result = saveSystem.saveGame(saveData);
        if (result) {
            LogUtil.info("游戏已保存: " + saveData.getSaveName());
            EventManager.getInstance().triggerEvent(
                    new GameEvent(Event.EventType.GAME_SAVED, null, saveData.getSaveName())
            );
        }
        return result;
    }

    public SaveData loadGame(String saveName) {
        SaveData saveData = saveSystem.loadGame(saveName);
        if (saveData != null) {
            LogUtil.info("游戏已加载: " + saveName);
            EventManager.getInstance().triggerEvent(
                    new GameEvent(Event.EventType.GAME_LOADED, null, saveName)
            );
        }
        return saveData;
    }

    public boolean deleteGame(String saveName) {
        boolean result = saveSystem.deleteGame(saveName);
        if (result) LogUtil.info("存档已删除: " + saveName);
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
        LogUtil.info("自动保存完成: " + saveName);
    }

    private void saveConfigData(SaveData saveData) {
        ConfigManager config = ConfigManager.getInstance();
        saveData.put("windowWidth", config.getInt(ConfigManager.KEY_WINDOW_WIDTH));
        saveData.put("windowHeight", config.getInt(ConfigManager.KEY_WINDOW_HEIGHT));
        saveData.put("playerLives", config.getInt(ConfigManager.KEY_PLAYER_LIVES));
        saveData.put("enemyCount", config.getInt(ConfigManager.KEY_ENEMY_COUNT));
        saveData.put("saveTime", System.currentTimeMillis());
        saveData.put("saveDate", new Date().toString());
    }

    private void saveGameState(SaveData saveData) {
        GameManager gameManager = GameManager.getInstance();
        saveData.put("gameState", gameManager.getCurrentStateType().name());
        saveData.put("gameVersion", "1.0.0");
    }

    private void savePlayerData(SaveData saveData) {
        saveData.put("playerScore", 0);
        saveData.put("playerLevel", 1);
    }

    private void saveStatsData(SaveData saveData) {
        saveData.put("playTime", 0);
        saveData.put("enemiesKilled", 0);
        saveData.put("shotsFired", 0);

        AchievementManager am = AchievementManager.getInstance();
        Achievement[] achievements = am.getAllAchievements();
        Map<String, Boolean> achievementStatus = new HashMap<>();
        for (Achievement achievement : achievements) {
            achievementStatus.put(achievement.getId(), achievement.isUnlocked());
        }
        saveData.put("achievements", achievementStatus);
    }

    private static class RealFileSaveSystem implements SaveSystem {
        private static final String SAVE_DIR = "saves/";

        public RealFileSaveSystem() {
            File saveDir = new File(SAVE_DIR);
            if (!saveDir.exists()) saveDir.mkdirs();
        }

        @Override
        public boolean saveGame(SaveData saveData) {
            try {
                String saveName = saveData.getSaveName();
                File saveFile = new File(SAVE_DIR + saveName + ".sav");
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
                    oos.writeObject(saveData);
                }
                return true;
            } catch (IOException e) {
                LogUtil.severe("保存游戏失败: " + e.getMessage());
                return false;
            }
        }

        @Override
        public SaveData loadGame(String saveName) {
            try {
                File saveFile = new File(SAVE_DIR + saveName + ".sav");
                if (!saveFile.exists()) return null;
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
                    return (SaveData) ois.readObject();
                }
            } catch (IOException | ClassNotFoundException e) {
                LogUtil.severe("加载游戏失败: " + e.getMessage());
                return null;
            }
        }

        @Override
        public boolean deleteGame(String saveName) {
            File saveFile = new File(SAVE_DIR + saveName + ".sav");
            return saveFile.delete();
        }

        @Override
        public String[] listSaves() {
            File saveDir = new File(SAVE_DIR);
            if (!saveDir.exists()) return new String[0];
            File[] saveFiles = saveDir.listFiles((dir, name) -> name.endsWith(".sav"));
            if (saveFiles == null) return new String[0];
            String[] saves = new String[saveFiles.length];
            for (int i = 0; i < saveFiles.length; i++) {
                String name = saveFiles[i].getName();
                saves[i] = name.substring(0, name.length() - 4);
            }
            return saves;
        }

        @Override
        public boolean saveExists(String saveName) {
            return new File(SAVE_DIR + saveName + ".sav").exists();
        }
    }
}