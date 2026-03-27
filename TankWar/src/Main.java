import main.java.com.tankbattle.core.GameWindow;
import main.java.com.tankbattle.core.SoundGenerator;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("      精致坦克大战 - 启动中...");
        System.out.println("=========================================");

        // 检查是否需要生成音效文件
        if (args.length > 0 && args[0].equals("generate-sounds")) {
            SoundGenerator.generateAllSounds();
            return;
        }

        System.out.println("游戏功能说明:");
        System.out.println("1. 开始界面: 按空格键或R键开始游戏");
        System.out.println("2. 移动控制: W(上) A(左) S(下) D(右)");
        System.out.println("3. 瞄准射击: 鼠标移动瞄准 + 左键射击");
        System.out.println("4. 游戏目标: 消灭10个红色敌人坦克");
        System.out.println("5. 特殊机制: 进入绿色草丛区域可隐身");
        System.out.println("6. 重新开始: 游戏结束/胜利时按R键");
        System.out.println("7. 音效系统: 支持背景音乐和游戏音效");
        System.out.println("=========================================");

        SwingUtilities.invokeLater(() -> {
            new GameWindow();
        });
    }
}