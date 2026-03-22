import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("精致坦克大战启动...");
        System.out.println("控制: WASD移动, 鼠标瞄准+左键射击");
        System.out.println("目标: 消灭10个红色敌人坦克");
        System.out.println("草丛隐身: 进入绿色区域时敌人不会攻击你");
        SwingUtilities.invokeLater(() -> {
            new GameWindow();
        });
    }
}