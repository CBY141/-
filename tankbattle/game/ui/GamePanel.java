package main.java.com.tankbattle.game.ui;

import main.java.com.tankbattle.core.ConfigManager;
import main.java.com.tankbattle.core.GameManager;
import main.java.com.tankbattle.core.InputHandler;
import main.java.com.tankbattle.core.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel {
    private InputHandler inputHandler;
    private int baseWidth;
    private int baseHeight;

    public GamePanel() {
        ConfigManager config = ConfigManager.getInstance();
        baseWidth = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        baseHeight = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);

        setBackground(Color.BLACK);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setPreferredSize(new Dimension(baseWidth, baseHeight));

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        inputHandler = GameManager.getInstance().getInputHandler();
        // 将 GamePanel 引用传递给 InputHandler
        inputHandler.setGamePanel(this);

        this.addKeyListener(inputHandler);
        this.addMouseListener(inputHandler);
        this.addMouseMotionListener(inputHandler);

        ResourceManager.getInstance().preloadSounds();
        requestFocusInWindow();
    }

    public void updateGame() {
        GameManager.getInstance().update();
    }

    // 获取当前缩放信息
    public float getCurrentScale() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        if (panelWidth == 0 || panelHeight == 0) return 1.0f;
        float scaleX = (float) panelWidth / baseWidth;
        float scaleY = (float) panelHeight / baseHeight;
        return Math.min(scaleX, scaleY);
    }

    public int getOffsetX() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        float scale = getCurrentScale();
        int drawWidth = (int) (baseWidth * scale);
        return (panelWidth - drawWidth) / 2;
    }

    public int getOffsetY() {
        int panelHeight = getHeight();
        float scale = getCurrentScale();
        int drawHeight = (int) (baseHeight * scale);
        return (panelHeight - drawHeight) / 2;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        float scale = getCurrentScale();
        int offsetX = getOffsetX();
        int offsetY = getOffsetY();

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);

        GameManager.getInstance().render(g);
    }
}