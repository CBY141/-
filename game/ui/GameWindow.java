package main.java.com.tankbattle.game.ui;

import main.java.com.tankbattle.core.ConfigManager;
import main.java.com.tankbattle.core.GameManager;
import main.java.com.tankbattle.utils.FrameRateManager;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameWindow extends JFrame {
    private GamePanel gamePanel;

    public GameWindow() {
        ConfigManager config = ConfigManager.getInstance();
        setTitle(config.getString(ConfigManager.KEY_WINDOW_TITLE));
        setSize(config.getInt(ConfigManager.KEY_WINDOW_WIDTH),
                config.getInt(ConfigManager.KEY_WINDOW_HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        gamePanel = new GamePanel();
        add(gamePanel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                gamePanel.requestFocusInWindow();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                // 窗口关闭时停止游戏
                GameManager.getInstance().stop();
            }
        });

        setVisible(true);

        SwingUtilities.invokeLater(() -> {
            gamePanel.requestFocusInWindow();
        });

        startGameLoop();
    }

    private void startGameLoop() {
        new Thread(() -> {
            // 获取全局帧率管理器
            FrameRateManager fpsManager = FrameRateManager.getInstance();

            while (true) {
                // 记录这一帧开始的时间
                fpsManager.startFrame();

                // 更新游戏逻辑与重绘
                gamePanel.updateGame();
                gamePanel.repaint();

                // 管理器会自动计算并 sleep 足够的时间，确保稳定在目标帧率(60FPS)
                fpsManager.endFrame();
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameWindow();
        });
    }
}