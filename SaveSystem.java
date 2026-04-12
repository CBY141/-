package main.java.com.tankbattle.core;

/**
 * 存档系统接口
 */
public interface SaveSystem {
    /**
     * 保存游戏
     */
    boolean saveGame(SaveData saveData);

    /**
     * 加载游戏
     */
    SaveData loadGame(String saveName);

    /**
     * 删除存档
     */
    boolean deleteGame(String saveName);

    /**
     * 获取所有存档列表
     */
    String[] listSaves();

    /**
     * 检查存档是否存在
     */
    boolean saveExists(String saveName);
}