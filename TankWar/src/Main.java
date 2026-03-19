import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("坦克大战启动...");
        System.out.println("控制: WASD移动, 鼠标瞄准+左键射击");
        System.out.println("目标: 消灭10个红色敌人");
        System.out.println("草丛可以隐身!");

        SwingUtilities.invokeLater(() -> {
            new GameWindow();
        });
    }
}