import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
    private GameWorld gameWorld = new GameWorld();
    private int playerX = 400, playerY = 500;
    private int playerDirection = GameConfig.DIR_UP;
    private int playerLives = GameConfig.PLAYER_LIVES;
    private boolean playerInGrass = false;

    private List<Enemy> enemies = new ArrayList<>();
    private List<Bullet> playerBullets = new ArrayList<>();
    private List<Bullet> enemyBullets = new ArrayList<>();

    private boolean up, down, left, right;
    private int mouseX, mouseY;
    private boolean mousePressed = false;
    private Random random = new Random();

    public GamePanel() {
        setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        initEnemies();
    }

    private void initEnemies() {
        enemies.clear();
        for (int i = 0; i < GameConfig.ENEMY_COUNT; i++) {
            int x = 50 + (i % 5) * 150;
            int y = 50 + (i / 5) * 100;
            enemies.add(new Enemy(x, y));
        }
    }

    public void updateGame() {
        updatePlayer();
        updateEnemies();
        updateBullets();
        checkCollisions();
    }

    private void updatePlayer() {
        int newX = playerX;
        int newY = playerY;

        if (up) { newY -= GameConfig.PLAYER_SPEED; playerDirection = GameConfig.DIR_UP; }
        if (down) { newY += GameConfig.PLAYER_SPEED; playerDirection = GameConfig.DIR_DOWN; }
        if (left) { newX -= GameConfig.PLAYER_SPEED; playerDirection = GameConfig.DIR_LEFT; }
        if (right) { newX += GameConfig.PLAYER_SPEED; playerDirection = GameConfig.DIR_RIGHT; }

        if (gameWorld.isPositionPassable(newX, newY, 30, 30)) {
            playerX = newX;
            playerY = newY;
        }

        playerInGrass = gameWorld.isInGrass(playerX, playerY);

        if (mousePressed) {
            int centerX = playerX + 15;
            int centerY = playerY + 15;
            double angle = Math.atan2(mouseY - centerY, mouseX - centerX);
            int dir = getDirectionFromAngle(angle);
            playerBullets.add(new Bullet(centerX, centerY, dir));
            mousePressed = false;
        }
    }

    private int getDirectionFromAngle(double angle) {
        if (angle < 0) angle += 2 * Math.PI;
        if (angle < Math.PI/4 || angle >= 7*Math.PI/4) return GameConfig.DIR_RIGHT;
        if (angle < 3*Math.PI/4) return GameConfig.DIR_DOWN;
        if (angle < 5*Math.PI/4) return GameConfig.DIR_LEFT;
        return GameConfig.DIR_UP;
    }

    private void updateEnemies() {
        for (Enemy enemy : enemies) {
            if (!enemy.alive) continue;

            enemy.update();

            if (random.nextInt(100) < 1) {
                enemyBullets.add(new Bullet(enemy.x + 15, enemy.y + 15, enemy.direction));
            }
        }
        enemies.removeIf(enemy -> !enemy.alive);
    }

    private void updateBullets() {
        playerBullets.forEach(Bullet::update);
        enemyBullets.forEach(Bullet::update);
        playerBullets.removeIf(bullet -> !bullet.isAlive());
        enemyBullets.removeIf(bullet -> !bullet.isAlive());
    }

    private void checkCollisions() {
        for (Bullet bullet : playerBullets) {
            for (Enemy enemy : enemies) {
                if (enemy.alive && bullet.collidesWith(enemy.x, enemy.y, 30)) {
                    bullet.setAlive(false);
                    enemy.alive = false;
                    break;
                }
            }
        }

        for (Bullet bullet : enemyBullets) {
            if (bullet.collidesWith(playerX, playerY, 30)) {
                bullet.setAlive(false);
                playerLives--;
                if (playerLives <= 0) {
                    playerLives = GameConfig.PLAYER_LIVES;
                    playerX = 400;
                    playerY = 500;
                    initEnemies();
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        gameWorld.draw(g);

        enemyBullets.forEach(bullet -> bullet.draw(g));
        playerBullets.forEach(bullet -> bullet.draw(g));

        for (Enemy enemy : enemies) {
            enemy.draw(g);
        }

        drawPlayer(g);
        drawUI(g);

        g.setColor(new Color(255, 255, 0, 100));
        g.drawLine(playerX + 15, playerY + 15, mouseX, mouseY);
    }

    private void drawPlayer(Graphics g) {
        if (playerInGrass) {
            g.setColor(new Color(0, 255, 0, 100));
        } else {
            g.setColor(Color.GREEN);
        }
        g.fillRect(playerX, playerY, 30, 30);

        g.setColor(Color.RED);
        int centerX = playerX + 15;
        int centerY = playerY + 15;
        double angle = Math.atan2(mouseY - centerY, mouseX - centerX);
        int endX = centerX + (int)(20 * Math.cos(angle));
        int endY = centerY + (int)(20 * Math.sin(angle));
        g.drawLine(centerX, centerY, endX, endY);
    }

    private void drawUI(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("生命: " + playerLives, 20, 30);
        g.drawString("敌人: " + enemies.size(), 20, 60);

        if (playerInGrass) {
            g.setColor(new Color(0, 255, 0, 150));
            g.drawString("隐身中", 20, 90);
        }
    }

    @Override public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: up = true; break;
            case KeyEvent.VK_S: down = true; break;
            case KeyEvent.VK_A: left = true; break;
            case KeyEvent.VK_D: right = true; break;
        }
    }

    @Override public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: up = false; break;
            case KeyEvent.VK_S: down = false; break;
            case KeyEvent.VK_A: left = false; break;
            case KeyEvent.VK_D: right = false; break;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mousePressed(MouseEvent e) { mousePressed = true; }
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); }
    @Override public void mouseDragged(MouseEvent e) { mouseMoved(e); }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    class Enemy {
        int x, y, direction;
        boolean alive = true;
        int moveCounter = 0;

        Enemy(int x, int y) {
            this.x = x;
            this.y = y;
            this.direction = random.nextInt(4);
        }

        void update() {
            moveCounter++;
            if (moveCounter > 60) {
                direction = random.nextInt(4);
                moveCounter = 0;
            }

            int newX = x, newY = y;
            switch (direction) {
                case GameConfig.DIR_UP: newY -= GameConfig.ENEMY_SPEED; break;
                case GameConfig.DIR_DOWN: newY += GameConfig.ENEMY_SPEED; break;
                case GameConfig.DIR_LEFT: newX -= GameConfig.ENEMY_SPEED; break;
                case GameConfig.DIR_RIGHT: newX += GameConfig.ENEMY_SPEED; break;
            }

            if (gameWorld.isPositionPassable(newX, newY, 30, 30)) {
                x = newX;
                y = newY;
            } else {
                direction = random.nextInt(4);
            }
        }

        void draw(Graphics g) {
            if (!alive) return;
            g.setColor(Color.RED);
            g.fillRect(x, y, 30, 30);
        }
    }
}