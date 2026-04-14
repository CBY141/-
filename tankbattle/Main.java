package main.java.com.tankbattle;

import main.java.com.tankbattle.game.ui.GameWindow;
import main.java.com.tankbattle.utils.LogUtil;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        LogUtil.info("=========================================");
        LogUtil.info("      精致坦克大战 - 启动中...");
        LogUtil.info("=========================================");

        LogUtil.info("操作系统: " + System.getProperty("os.name"));
        LogUtil.info("Java版本: " + System.getProperty("java.version"));
        LogUtil.info("当前目录: " + System.getProperty("user.dir"));

        LogUtil.info("初始化配置管理器...");
        main.java.com.tankbattle.core.ConfigManager config =
                main.java.com.tankbattle.core.ConfigManager.getInstance();
        config.printCurrentConfig();

        LogUtil.info("=========================================");
        LogUtil.info("游戏功能说明:");
        LogUtil.info("1. 开始界面: 按空格键或R键开始游戏");
        LogUtil.info("2. 移动控制: W(上) A(左) S(下) D(右)");
        LogUtil.info("3. 瞄准射击: 鼠标左键 或 空格键");
        LogUtil.info("4. 瞄准: 鼠标移动");
        LogUtil.info("5. 游戏目标: 消灭所有敌人坦克");
        LogUtil.info("6. 特殊机制: 进入绿色草丛区域可隐身");
        LogUtil.info("7. 重新开始: 游戏结束/胜利时按R键");
        LogUtil.info("8. 音效系统: 支持背景音乐和游戏音效");
        LogUtil.info("=========================================");
        LogUtil.info("提示: 如果无法移动/射击，请点击游戏画面获取焦点");
        LogUtil.info("=========================================");

        SwingUtilities.invokeLater(() -> {
            new GameWindow();
        });
    }
}