package main.java.com.tankbattle.game.ui;

import main.java.com.tankbattle.core.ConfigManager;
import main.java.com.tankbattle.core.GameManager;
import main.java.com.tankbattle.utils.FrameRateManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GameWindow extends JFrame {
    private GamePanel gamePanel;
    private boolean isFullscreen = false;
    private Rectangle windowedBounds;

    public GameWindow() {
        ConfigManager config = ConfigManager.getInstance();
        setTitle(config.getString(ConfigManager.KEY_WINDOW_TITLE));
        setSize(config.getInt(ConfigManager.KEY_WINDOW_WIDTH),
                config.getInt(ConfigManager.KEY_WINDOW_HEIGHT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
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
                GameManager.getInstance().stop();
            }
        });

        setVisible(true);
        windowedBounds = getBounds();

        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());

        startGameLoop();
    }

    public void toggleFullscreen() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (isFullscreen) {
            device.setFullScreenWindow(null);
            dispose();
            setUndecorated(false);
            setBounds(windowedBounds);
            setVisible(true);
            isFullscreen = false;
        } else {
            windowedBounds = getBounds();
            dispose();
            setUndecorated(true);
            setVisible(true);
            device.setFullScreenWindow(this);
            isFullscreen = true;
        }
        gamePanel.requestFocusInWindow();
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    private void startGameLoop() {
        new Thread(() -> {
            FrameRateManager fpsManager = FrameRateManager.getInstance();
            GameManager gameManager = GameManager.getInstance();
            while (gameManager.isRunning()) {
                fpsManager.startFrame();
                gamePanel.updateGame();
                gamePanel.repaint();
                fpsManager.endFrame();
            }
            System.out.println("游戏循环已退出");
        }, "GameLoop").start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameWindow::new);
    }
}