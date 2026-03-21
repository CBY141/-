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
        player = new Player(100, 100);
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