import main.java.com.tankbattle.core.GameWindow;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("      精致坦克大战 - 启动中...");
        System.out.println("=========================================");

        // 打印调试信息
        System.out.println("操作系统: " + System.getProperty("os.name"));
        System.out.println("Java版本: " + System.getProperty("java.version"));
        System.out.println("当前目录: " + System.getProperty("user.dir"));

        // 检查配置管理器
        System.out.println("初始化配置管理器...");
        main.java.com.tankbattle.core.ConfigManager config =
                main.java.com.tankbattle.core.ConfigManager.getInstance();
        config.printCurrentConfig();

        System.out.println("=========================================");
        System.out.println("游戏功能说明:");
        System.out.println("1. 开始界面: 按空格键或R键开始游戏");
        System.out.println("2. 移动控制: W(上) A(左) S(下) D(右)");
        System.out.println("3. 瞄准射击: 鼠标左键 或 空格键");
        System.out.println("4. 瞄准: 鼠标移动");
        System.out.println("5. 游戏目标: 消灭所有敌人坦克");
        System.out.println("6. 特殊机制: 进入绿色草丛区域可隐身");
        System.out.println("7. 重新开始: 游戏结束/胜利时按R键");
        System.out.println("8. 音效系统: 支持背景音乐和游戏音效");
        System.out.println("=========================================");
        System.out.println("提示: 如果无法移动/射击，请点击游戏画面获取焦点");
        System.out.println("=========================================");

        SwingUtilities.invokeLater(() -> {
            new GameWindow();
        });
    }
}