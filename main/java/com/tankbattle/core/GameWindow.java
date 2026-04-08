package main.java.com.tankbattle.core;

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
            while (true) {
                gamePanel.updateGame();
                gamePanel.repaint();
                try {
                    Thread.sleep(16); // 约60FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameWindow();
        });
    }
}