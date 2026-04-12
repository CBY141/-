package main.java.com.tankbattle.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel {
    private InputHandler inputHandler;

    public GamePanel() {
        ConfigManager config = ConfigManager.getInstance();
        setBackground(Color.BLACK);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setPreferredSize(new Dimension(
                config.getInt(ConfigManager.KEY_WINDOW_WIDTH),
                config.getInt(ConfigManager.KEY_WINDOW_HEIGHT)
        ));

        // 强制获取焦点
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
                System.out.println("游戏面板获得焦点");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        // 添加焦点变化监听
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                System.out.println("游戏面板获得键盘焦点");
            }

            @Override
            public void focusLost(FocusEvent e) {
                System.out.println("游戏面板失去键盘焦点");
            }
        });

        // 使用GameManager的输入处理器
        inputHandler = GameManager.getInstance().getInputHandler();

        // 添加键盘和鼠标监听器
        this.addKeyListener(inputHandler);
        this.addMouseListener(inputHandler);
        this.addMouseMotionListener(inputHandler);

        // 预加载音效
        ResourceManager.getInstance().preloadSounds();

        // 确保面板获得焦点
        requestFocusInWindow();
    }

    public void updateGame() {
        GameManager.getInstance().update();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        GameManager.getInstance().render(g);
    }
}