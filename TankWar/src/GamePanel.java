import javax.swing.*;
import java.awt.*;
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
        gameWorld = new GameWorld();
        // 设置玩家初始位置为地图中心，并确保可通行
        int startX = GameConfig.MAP_WIDTH / 2 * GameConfig.TILE_SIZE;
        int startY = GameConfig.MAP_HEIGHT / 2 * GameConfig.TILE_SIZE;
        player = new Player(startX, startY);
        if (!gameWorld.isPositionPassable(player.x, player.y, GameConfig.TANK_WIDTH, GameConfig.TANK_HEIGHT)) {
            boolean found = false;
            for (int offset = 1; offset < 10 && !found; offset++) {
                for (int dx = -offset; dx <= offset && !found; dx++) {
                    for (int dy = -offset; dy <= offset && !found; dy++) {
                        int testX = player.x + dx * GameConfig.TILE_SIZE;
                        int testY = player.y + dy * GameConfig.TILE_SIZE;
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
        this.setFocusable(true);
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
        g.setColor(Color.RED);
        g.drawLine(inputHandler.mouseX - 10, inputHandler.mouseY, inputHandler.mouseX + 10, inputHandler.mouseY);
        g.drawLine(inputHandler.mouseX, inputHandler.mouseY - 10, inputHandler.mouseX, inputHandler.mouseY + 10);
        g.drawOval(inputHandler.mouseX - 5, inputHandler.mouseY - 5, 10, 10);
    }
}