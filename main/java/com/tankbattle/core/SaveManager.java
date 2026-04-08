package main.java.com.tankbattle.core;

/**
 * 存档管理器 - 单例模式
 */
public class SaveManager {
    private static SaveManager instance;
    private SaveSystem saveSystem;

    private SaveManager() {
        // 初始化时创建简单的文件系统存档
        saveSystem = new SimpleFileSaveSystem();
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

        // 保存基本配置
        ConfigManager config = ConfigManager.getInstance();
        saveData.put("windowWidth", config.getInt(ConfigManager.KEY_WINDOW_WIDTH));
        saveData.put("windowHeight", config.getInt(ConfigManager.KEY_WINDOW_HEIGHT));
        saveData.put("playerLives", config.getInt(ConfigManager.KEY_PLAYER_LIVES));

        // 保存当前游戏状态
        GameManager gameManager = GameManager.getInstance();
        saveData.put("gameState", gameManager.getCurrentStateType().name());

        // 调用存档系统保存
        boolean result = saveSystem.saveGame(saveData);
        if (result) {
            System.out.println("游戏已保存: " + saveName);
        }
        return result;
    }

    public SaveData loadGame(String saveName) {
        SaveData saveData = saveSystem.loadGame(saveName);
        if (saveData != null) {
            System.out.println("游戏已加载: " + saveName);
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
        // 简化版，暂时不实现自动保存
    }

    // 简单的文件系统存档实现
    private static class SimpleFileSaveSystem implements SaveSystem {
        @Override
        public boolean saveGame(SaveData saveData) {
            // 简化实现，只打印日志
            System.out.println("简化存档系统: 保存游戏 " + saveData.getSaveName());
            return true;
        }

        @Override
        public SaveData loadGame(String saveName) {
            // 简化实现，只打印日志
            System.out.println("简化存档系统: 加载游戏 " + saveName);
            return new SaveData(saveName);
        }

        @Override
        public boolean deleteGame(String saveName) {
            // 简化实现，只打印日志
            System.out.println("简化存档系统: 删除游戏 " + saveName);
            return true;
        }

        @Override
        public String[] listSaves() {
            // 简化实现，返回空数组
            return new String[0];
        }

        @Override
        public boolean saveExists(String saveName) {
            return false;
        }
    }
}