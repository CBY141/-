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
        ConfigManager config = ConfigManager.getInstance();
        ResourceManager resourceManager = ResourceManager.getInstance();

        setBackground(Color.BLACK);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // 设置窗口大小
        setPreferredSize(new Dimension(
                config.getInt(ConfigManager.KEY_WINDOW_WIDTH),
                config.getInt(ConfigManager.KEY_WINDOW_HEIGHT)
        ));

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        // 使用GameObjectFactory创建游戏世界
        gameWorld = GameObjectFactory.createGameWorld();

        // 使用GameObjectFactory创建玩家
        player = GameObjectFactory.createPlayerAtSafePosition(gameWorld);

        // 初始化资源管理器
        resourceManager.preloadSounds();

        gameLogic = new GameLogic(player, enemies, bullets, explosions, gameWorld, inputHandler);
        gameLogic.initializeEnemies(config.getInt(ConfigManager.KEY_ENEMY_COUNT));

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

        GameLogic.GameState state = gameLogic.getGameState();

        switch (state) {
            case START:
                drawStartScreen(g);
                break;
            case PLAYING:
                drawPlayingScreen(g);
                break;
            case VICTORY:
                drawVictoryScreen(g);
                break;
            case GAME_OVER:
                drawGameOverScreen(g);
                break;
        }
    }

    private void drawStartScreen(Graphics g) {
        gameWorld.draw(g);

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());

        ResourceManager resourceManager = ResourceManager.getInstance();

        g.setColor(Color.YELLOW);
        g.setFont(resourceManager.getSafeFont(Font.BOLD, 64));
        String title = "坦克大战";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, getWidth()/2 - titleWidth/2, getHeight()/2 - 100);

        g.setColor(Color.GREEN);
        g.setFont(resourceManager.getSafeFont(Font.BOLD, 36));
        String startText = "按 空格键 或 R键 开始游戏";
        int startWidth = g.getFontMetrics().stringWidth(startText);
        g.drawString(startText, getWidth()/2 - startWidth/2, getHeight()/2);

        g.setColor(Color.WHITE);
        g.setFont(resourceManager.getSafeFont(Font.PLAIN, 20));
        String[] controls = {
                "控制说明:",
                "移动: W A S D",
                "射击: 鼠标左键",
                "瞄准: 鼠标移动",
                "重新开始: R键"
        };

        int y = getHeight()/2 + 80;
        for (String line : controls) {
            int lineWidth = g.getFontMetrics().stringWidth(line);
            g.drawString(line, getWidth()/2 - lineWidth/2, y);
            y += 30;
        }
    }

    private void drawPlayingScreen(Graphics g) {
        gameWorld.draw(g);

        player.draw(g, inputHandler.mouseX, inputHandler.mouseY);
        for (Enemy e : enemies) e.draw(g);
        for (Bullet b : bullets) b.draw(g);
        for (Explosion exp : explosions) exp.draw(g);

        drawGameUI(g);
        drawCrosshair(g);
    }

    private void drawVictoryScreen(Graphics g) {
        gameWorld.draw(g);

        g.setColor(new Color(0, 200, 0, 120));
        g.fillRect(0, 0, getWidth(), getHeight());

        ResourceManager resourceManager = ResourceManager.getInstance();

        g.setColor(new Color(255, 215, 0));
        g.setFont(resourceManager.getSafeFont(Font.BOLD, 72));
        String victoryText = "胜利!";
        int victoryWidth = g.getFontMetrics().stringWidth(victoryText);
        g.drawString(victoryText, getWidth()/2 - victoryWidth/2, getHeight()/2 - 50);

        g.setColor(Color.WHITE);
        g.setFont(resourceManager.getSafeFont(Font.BOLD, 36));
        String congratsText = "恭喜你消灭了所有敌人!";
        int congratsWidth = g.getFontMetrics().stringWidth(congratsText);
        g.drawString(congratsText, getWidth()/2 - congratsWidth/2, getHeight()/2 + 30);

        g.setColor(Color.CYAN);
        g.setFont(resourceManager.getSafeFont(Font.PLAIN, 28));
        String restartText = "按 R 键重新开始游戏";
        int restartWidth = g.getFontMetrics().stringWidth(restartText);
        g.drawString(restartText, getWidth()/2 - restartWidth/2, getHeight()/2 + 100);

        g.setColor(Color.WHITE);
        g.setFont(resourceManager.getSafeFont(Font.PLAIN, 24));
        String infoText = "剩余生命: " + player.lives;
        int infoWidth = g.getFontMetrics().stringWidth(infoText);
        g.drawString(infoText, getWidth()/2 - infoWidth/2, getHeight()/2 + 150);
    }

    private void drawGameOverScreen(Graphics g) {
        gameWorld.draw(g);

        ConfigManager config = ConfigManager.getInstance();
        int windowWidth = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        int windowHeight = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);
        ResourceManager resourceManager = ResourceManager.getInstance();

        g.setColor(new Color(255, 50, 50, 150));
        g.fillRect(windowWidth/2 - 180, windowHeight/2 - 100, 360, 200);

        g.setColor(Color.WHITE);
        g.setFont(resourceManager.getSafeFont(Font.BOLD, 48));
        String gameOverText = "游戏结束!";
        int textWidth = g.getFontMetrics().stringWidth(gameOverText);
        g.drawString(gameOverText, windowWidth/2 - textWidth/2, windowHeight/2 - 30);

        g.setFont(resourceManager.getSafeFont(Font.PLAIN, 24));
        String restartHint = "按 R 键重新开始";
        int hintWidth = g.getFontMetrics().stringWidth(restartHint);
        g.drawString(restartHint, windowWidth/2 - hintWidth/2, windowHeight/2 + 40);

        g.setFont(resourceManager.getSafeFont(Font.PLAIN, 20));
        int enemiesKilled = config.getInt(ConfigManager.KEY_ENEMY_COUNT) - gameLogic.getEnemyCount();
        String scoreText = "消灭敌人: " + enemiesKilled + "/" + config.getInt(ConfigManager.KEY_ENEMY_COUNT);
        int scoreWidth = g.getFontMetrics().stringWidth(scoreText);
        g.drawString(scoreText, windowWidth/2 - scoreWidth/2, windowHeight/2 + 80);
    }

    private void drawGameUI(Graphics g) {
        ResourceManager resourceManager = ResourceManager.getInstance();

        g.setColor(Color.WHITE);
        g.setFont(resourceManager.getSafeFont(Font.BOLD, 16));
        g.drawString("生命值: " + player.lives, 10, 20);
        g.drawString("敌人剩余: " + enemies.size(), 10, 40);

        if (this.isFocusOwner()) {
            g.setColor(Color.GREEN);
            g.drawString("焦点状态: 已获得键盘焦点", 10, 60);
        } else {
            g.setColor(Color.RED);
            g.drawString("焦点状态: 未获得焦点 (请点击游戏画面)", 10, 60);
        }

        g.setColor(Color.CYAN);
        g.drawString("按 R 键重新开始游戏", 10, 80);
    }

    private void drawCrosshair(Graphics g) {
        g.setColor(Color.RED);
        g.drawLine(inputHandler.mouseX - 10, inputHandler.mouseY, inputHandler.mouseX + 10, inputHandler.mouseY);
        g.drawLine(inputHandler.mouseX, inputHandler.mouseY - 10, inputHandler.mouseX, inputHandler.mouseY + 10);
        g.drawOval(inputHandler.mouseX - 5, inputHandler.mouseY - 5, 10, 10);
    }

    // 获取GameLogic（用于GameWindow关闭时释放资源）
    public GameLogic getGameLogic() {
        return gameLogic;
    }
}