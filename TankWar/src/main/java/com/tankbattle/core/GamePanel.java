package main.java.com.tankbattle.core;

import main.java.com.tankbattle.entity.*;
import main.java.com.tankbattle.world.GameWorld;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private Player player;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();
    private GameWorld gameWorld;
    private InputHandler inputHandler = new InputHandler();
    private GameLogic gameLogic;

    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        gameWorld = new GameWorld();

        int startX = GameConfig.MAP_WIDTH / 2 * GameConfig.TILE_SIZE;
        int startY = GameConfig.MAP_HEIGHT / 2 * GameConfig.TILE_SIZE;
        player = new Player(startX, startY);

        if (!gameWorld.isPositionPassable(player.x, player.y, GameConfig.TANK_WIDTH, GameConfig.TANK_HEIGHT)) {
            boolean found = false;
            for (int offset = 1; offset < 10 && !found; offset++) {
                for (int dx = -offset; dx <= offset && !found; dx++) {
                    for (int dy = -offset; dy <= offset && !found; dy++) {
                        int testX = startX + dx * GameConfig.TILE_SIZE;
                        int testY = startY + dy * GameConfig.TILE_SIZE;
                        if (gameWorld.isPositionPassable(testX, testY, GameConfig.TANK_WIDTH, GameConfig.TANK_HEIGHT)) {
                            player.x = testX;
                            player.y = testY;
                            found = true;
                        }
                    }
                }
            }
        }

        gameLogic = new GameLogic(player, enemies, bullets, explosions, gameWorld, inputHandler);
        gameLogic.initializeEnemies(GameConfig.ENEMY_COUNT);

        this.addKeyListener(inputHandler);
        this.addMouseListener(inputHandler);
        this.addMouseMotionListener(inputHandler);
    }

    public void updateGame() {
        gameLogic.update();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        gameWorld.draw(g);
        player.draw(g, inputHandler.mouseX, inputHandler.mouseY);
        for (Enemy e : enemies) e.draw(g);
        for (Bullet b : bullets) b.draw(g);
        for (Explosion exp : explosions) exp.draw(g);
        drawUI(g);
    }

    private void drawUI(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("生命值: " + player.lives, 10, 20);
        g.drawString("敌人剩余: " + enemies.size(), 10, 40);

        if (this.isFocusOwner()) {
            g.setColor(Color.GREEN);
            g.drawString("焦点状态: 已获得键盘焦点", 10, 60);
        } else {
            g.setColor(Color.RED);
            g.drawString("焦点状态: 未获得焦点 (请点击游戏画面)", 10, 60);
        }

        if (player.dead) {
            g.setColor(new Color(255, 50, 50, 200));
            g.fillRect(GameConfig.WINDOW_WIDTH/2 - 150, GameConfig.WINDOW_HEIGHT/2 - 60, 300, 120);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            String gameOverText = "游戏结束!";
            int textWidth = g.getFontMetrics().stringWidth(gameOverText);
            g.drawString(gameOverText, GameConfig.WINDOW_WIDTH/2 - textWidth/2, GameConfig.WINDOW_HEIGHT/2 - 10);

            g.setFont(new Font("Arial", Font.PLAIN, 24));
            String restartHint = "按 R 键重新开始";
            int hintWidth = g.getFontMetrics().stringWidth(restartHint);
            g.drawString(restartHint, GameConfig.WINDOW_WIDTH/2 - hintWidth/2, GameConfig.WINDOW_HEIGHT/2 + 40);
        }

        g.setColor(Color.RED);
        g.drawLine(inputHandler.mouseX - 10, inputHandler.mouseY, inputHandler.mouseX + 10, inputHandler.mouseY);
        g.drawLine(inputHandler.mouseX, inputHandler.mouseY - 10, inputHandler.mouseX, inputHandler.mouseY + 10);
        g.drawOval(inputHandler.mouseX - 5, inputHandler.mouseY - 5, 10, 10);
    }
}