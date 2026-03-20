import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
    // 玩家相关
    private int playerX = 100;
    private int playerY = 100;
    private int playerLives = GameConfig.PLAYER_LIVES;
    private boolean playerInGrass = false;

    // 鼠标位置
    private int mouseX = 0;
    private int mouseY = 0;

    // 按键状态
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean spacePressed = false;

    // 游戏对象
    private GameWorld gameWorld;
    private List<Bullet> bullets = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();
    private Random random = new Random();

    // 射击冷却
    private int shootCooldown = 0;
    private static final int SHOOT_COOLDOWN_TIME = 20;

    public GamePanel() {
        setBackground(Color.BLACK);
        gameWorld = new GameWorld();
        initializeEnemies();
    }

    private void initializeEnemies() {
        enemies.clear();
        for (int i = 0; i < GameConfig.ENEMY_COUNT; i++) {
            int x, y;
            do {
                x = random.nextInt(GameConfig.WINDOW_WIDTH - GameConfig.TANK_WIDTH);
                y = random.nextInt(GameConfig.WINDOW_HEIGHT - GameConfig.TANK_HEIGHT);
            } while (!gameWorld.isPositionPassable(x, y, GameConfig.TANK_WIDTH, GameConfig.TANK_HEIGHT));

            enemies.add(new Enemy(x, y));
        }
    }

    // 爆炸效果类
    class Explosion {
        int x, y;
        int size = 5;
        int maxSize = 25;
        boolean alive = true;

        Explosion(int x, int y) {
            this.x = x;
            this.y = y;
        }

        void update() {
            size += 2;
            if (size > maxSize) {
                alive = false;
            }
        }

        void draw(Graphics g) {
            if (!alive) return;

            // 爆炸中心
            g.setColor(new Color(255, 200, 0, 200));
            g.fillOval(x - size/2, y - size/2, size, size);

            // 爆炸外圈
            g.setColor(new Color(255, 100, 0, 100));
            g.fillOval(x - size, y - size, size*2, size*2);

            // 爆炸粒子
            g.setColor(Color.YELLOW);
            for (int i = 0; i < 8; i++) {
                double angle = i * Math.PI / 4;
                int px = x + (int)(size * Math.cos(angle));
                int py = y + (int)(size * Math.sin(angle));
                g.fillOval(px - 2, py - 2, 4, 4);
            }
        }
    }

    // 敌人类
    class Enemy {
        int x, y;
        int direction;
        boolean alive = true;
        int moveTimer = 0;
        int shootTimer = 0;

        Enemy(int x, int y) {
            this.x = x;
            this.y = y;
            this.direction = random.nextInt(4);
        }

        void update() {
            if (!alive) return;

            moveTimer++;
            shootTimer++;

            // 随机移动
            if (moveTimer > 60) {
                direction = random.nextInt(4);
                moveTimer = 0;
            }

            // 移动
            int newX = x;
            int newY = y;
            switch (direction) {
                case GameConfig.DIR_UP: newY -= GameConfig.ENEMY_SPEED; break;
                case GameConfig.DIR_DOWN: newY += GameConfig.ENEMY_SPEED; break;
                case GameConfig.DIR_LEFT: newX -= GameConfig.ENEMY_SPEED; break;
                case GameConfig.DIR_RIGHT: newX += GameConfig.ENEMY_SPEED; break;
            }

            if (gameWorld.isPositionPassable(newX, newY, GameConfig.TANK_WIDTH, GameConfig.TANK_HEIGHT)) {
                x = newX;
                y = newY;
            } else {
                direction = random.nextInt(4);
            }

            // 随机射击
            if (shootTimer > 120 && random.nextInt(100) < 5) {
                int bulletX = x + GameConfig.TANK_WIDTH / 2;
                int bulletY = y + GameConfig.TANK_HEIGHT / 2;
                bullets.add(new Bullet(bulletX, bulletY, direction, false));
                shootTimer = 0;
            }
        }

        void draw(Graphics g) {
            if (!alive) return;

            int centerX = x + GameConfig.TANK_WIDTH / 2;
            int centerY = y + GameConfig.TANK_HEIGHT / 2;

            // 绘制敌人坦克主体
            drawTankBody(g, x, y, GameConfig.ENEMY_TANK_COLOR, GameConfig.TANK_DETAIL_COLOR);

            // 绘制敌人炮管（根据移动方向）
            Color turretColor = GameConfig.TANK_TURRET_COLOR;
            int turretEndX = centerX, turretEndY = centerY;

            switch (direction) {
                case GameConfig.DIR_UP:
                    turretEndY = centerY - GameConfig.TANK_TURRET_LENGTH;
                    break;
                case GameConfig.DIR_DOWN:
                    turretEndY = centerY + GameConfig.TANK_TURRET_LENGTH;
                    break;
                case GameConfig.DIR_LEFT:
                    turretEndX = centerX - GameConfig.TANK_TURRET_LENGTH;
                    break;
                case GameConfig.DIR_RIGHT:
                    turretEndX = centerX + GameConfig.TANK_TURRET_LENGTH;
                    break;
            }

            // 绘制炮管
            g.setColor(turretColor);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(centerX, centerY, turretEndX, turretEndY);
            g2d.setStroke(new BasicStroke(1));

            // 炮塔
            g.setColor(turretColor.darker());
            g.fillOval(centerX - 4, centerY - 4, 8, 8);

            // 敌人标识（红色方块）
            g.setColor(Color.RED);
            g.fillRect(centerX - 2, y + 2, 4, 4);
        }

        boolean collidesWith(int targetX, int targetY) {
            // 使用坦克的实际中心点
            int thisCenterX = x + GameConfig.TANK_WIDTH / 2;
            int thisCenterY = y + GameConfig.TANK_HEIGHT / 2;
            int targetCenterX = targetX + GameConfig.TANK_WIDTH / 2;
            int targetCenterY = targetY + GameConfig.TANK_HEIGHT / 2;

            // 计算距离
            int dx = thisCenterX - targetCenterX;
            int dy = thisCenterY - targetCenterY;
            int distance = (int)Math.sqrt(dx*dx + dy*dy);

            // 碰撞距离阈值（略小于两个坦克的半径和）
            int collisionDistance = GameConfig.TANK_WIDTH - 2;

            return distance < collisionDistance;
        }
    }

    // 绘制坦克主体
    private void drawTankBody(Graphics g, int x, int y, Color mainColor, Color detailColor) {
        // 坦克主体（圆角矩形效果）
        g.setColor(mainColor);
        g.fillRect(x + 2, y + 2, GameConfig.TANK_WIDTH - 4, GameConfig.TANK_HEIGHT - 4);

        // 坦克履带
        g.setColor(detailColor);
        g.fillRect(x, y, GameConfig.TANK_WIDTH, 3);  // 上履带
        g.fillRect(x, y + GameConfig.TANK_HEIGHT - 3, GameConfig.TANK_WIDTH, 3);  // 下履带
        g.fillRect(x, y, 3, GameConfig.TANK_HEIGHT);  // 左履带
        g.fillRect(x + GameConfig.TANK_WIDTH - 3, y, 3, GameConfig.TANK_HEIGHT);  // 右履带

        // 坦克舱盖
        g.setColor(mainColor.darker());
        g.fillRect(x + 6, y + 6, GameConfig.TANK_WIDTH - 12, GameConfig.TANK_HEIGHT - 12);

        // 舱盖细节
        g.setColor(Color.BLACK);
        g.drawRect(x + 6, y + 6, GameConfig.TANK_WIDTH - 12, GameConfig.TANK_HEIGHT - 12);
    }

    // 绘制炮管
    private void drawTankTurret(Graphics g, int centerX, int centerY, int targetX, int targetY, Color turretColor) {
        // 计算炮管角度
        double angle = Math.atan2(targetY - centerY, targetX - centerX);

        // 炮管末端坐标
        int turretEndX = centerX + (int)(GameConfig.TANK_TURRET_LENGTH * Math.cos(angle));
        int turretEndY = centerY + (int)(GameConfig.TANK_TURRET_LENGTH * Math.sin(angle));

        // 绘制炮管（有厚度）
        g.setColor(turretColor);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));  // 炮管厚度
        g2d.drawLine(centerX, centerY, turretEndX, turretEndY);
        g2d.setStroke(new BasicStroke(1));  // 恢复默认

        // 炮管根部（炮塔）
        g.setColor(turretColor.darker());
        g.fillOval(centerX - 4, centerY - 4, 8, 8);
    }

    // 绘制玩家坦克
    private void drawPlayer(Graphics g) {
        int centerX = playerX + GameConfig.TANK_WIDTH / 2;
        int centerY = playerY + GameConfig.TANK_HEIGHT / 2;

        // 根据是否在草丛设置透明度
        Color tankColor = GameConfig.PLAYER_TANK_COLOR;
        Color detailColor = GameConfig.TANK_DETAIL_COLOR;
        Color turretColor = GameConfig.TANK_TURRET_COLOR;

        if (playerInGrass) {
            tankColor = new Color(tankColor.getRed(), tankColor.getGreen(), tankColor.getBlue(), 100);
            detailColor = new Color(detailColor.getRed(), detailColor.getGreen(), detailColor.getBlue(), 100);
            turretColor = new Color(turretColor.getRed(), turretColor.getGreen(), turretColor.getBlue(), 100);
        }

        // 绘制坦克主体（更精细的形状）
        drawTankBody(g, playerX, playerY, tankColor, detailColor);

        // 绘制炮管（指向鼠标方向）
        drawTankTurret(g, centerX, centerY, mouseX, mouseY, turretColor);

        // 绘制坦克标识（玩家的小三角标记）
        g.setColor(Color.WHITE);
        g.fillPolygon(
                new int[]{centerX, centerX - 3, centerX + 3},
                new int[]{playerY + 2, playerY + 8, playerY + 8},
                3
        );
    }

    public void updateGame() {
        // 更新玩家位置
        updatePlayer();

        // 更新子弹
        bullets.forEach(Bullet::update);
        bullets.removeIf(bullet -> !bullet.isAlive());

        // 更新敌人
        enemies.forEach(Enemy::update);

        // 更新爆炸效果
        updateExplosions();

        // 检查碰撞
        checkCollisions();

        // 射击冷却
        if (shootCooldown > 0) {
            shootCooldown--;
        }
    }

    private void updatePlayer() {
        int newX = playerX;
        int newY = playerY;

        if (upPressed) newY -= GameConfig.PLAYER_SPEED;
        if (downPressed) newY += GameConfig.PLAYER_SPEED;
        if (leftPressed) newX -= GameConfig.PLAYER_SPEED;
        if (rightPressed) newX += GameConfig.PLAYER_SPEED;

        // 边界检查
        newX = Math.max(0, Math.min(newX, GameConfig.WINDOW_WIDTH - GameConfig.TANK_WIDTH));
        newY = Math.max(0, Math.min(newY, GameConfig.WINDOW_HEIGHT - GameConfig.TANK_HEIGHT));

        // 碰撞检查
        if (gameWorld.isPositionPassable(newX, newY, GameConfig.TANK_WIDTH, GameConfig.TANK_HEIGHT)) {
            playerX = newX;
            playerY = newY;
        }

        // 检查是否在草丛中
        playerInGrass = gameWorld.isInGrass(playerX, playerY);

        // 射击
        if (spacePressed && shootCooldown == 0) {
            int bulletX = playerX + GameConfig.TANK_WIDTH / 2;
            int bulletY = playerY + GameConfig.TANK_HEIGHT / 2;

            // 计算射击方向（指向鼠标）
            double angle = Math.atan2(mouseY - bulletY, mouseX - bulletX);
            int direction;
            if (Math.abs(angle) < Math.PI/4) direction = GameConfig.DIR_RIGHT;
            else if (Math.abs(angle) > 3*Math.PI/4) direction = GameConfig.DIR_LEFT;
            else if (angle > 0) direction = GameConfig.DIR_DOWN;
            else direction = GameConfig.DIR_UP;

            bullets.add(new Bullet(bulletX, bulletY, direction, true));
            shootCooldown = SHOOT_COOLDOWN_TIME;
        }
    }

    private void updateExplosions() {
        explosions.forEach(Explosion::update);
        explosions.removeIf(exp -> !exp.alive);
    }

    private void checkCollisions() {
        // 检查子弹与敌人碰撞
        for (Bullet bullet : bullets) {
            if (!bullet.isAlive()) continue;

            for (Enemy enemy : enemies) {
                if (enemy.alive && bullet.collidesWith(enemy.x, enemy.y, GameConfig.TANK_WIDTH)) {
                    bullet.setAlive(false);
                    enemy.alive = false;
                    explosions.add(new Explosion(enemy.x + GameConfig.TANK_WIDTH/2,
                            enemy.y + GameConfig.TANK_HEIGHT/2));
                    break;
                }
            }

            // 检查子弹与玩家碰撞
            if (bullet.collidesWith(playerX, playerY, GameConfig.TANK_WIDTH)) {
                bullet.setAlive(false);
                playerLives--;
                if (playerLives <= 0) {
                    // 游戏结束逻辑
                    playerLives = 0;
                }
            }
        }

        // 检查敌人与玩家碰撞
        for (Enemy enemy : enemies) {
            if (enemy.alive && enemy.collidesWith(playerX, playerY)) {
                playerLives--;
                enemy.alive = false;
                explosions.add(new Explosion(enemy.x + GameConfig.TANK_WIDTH/2,
                        enemy.y + GameConfig.TANK_HEIGHT/2));
                if (playerLives <= 0) {
                    playerLives = 0;
                }
            }
        }

        // 移除死亡的敌人
        enemies.removeIf(enemy -> !enemy.alive);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 绘制地图
        gameWorld.draw(g);

        // 绘制玩家
        drawPlayer(g);

        // 绘制敌人
        enemies.forEach(enemy -> enemy.draw(g));

        // 绘制子弹
        bullets.forEach(bullet -> bullet.draw(g));

        // 绘制爆炸效果
        explosions.forEach(exp -> exp.draw(g));

        // 绘制UI
        drawUI(g);
    }

    private void drawUI(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("生命值: " + playerLives, 10, 20);
        g.drawString("敌人剩余: " + enemies.size(), 10, 40);

        // 绘制准星
        g.setColor(Color.RED);
        g.drawLine(mouseX - 10, mouseY, mouseX + 10, mouseY);
        g.drawLine(mouseX, mouseY - 10, mouseX, mouseY + 10);
        g.drawOval(mouseX - 5, mouseY - 5, 10, 10);
    }

    // 键盘事件
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: upPressed = true; break;
            case KeyEvent.VK_S: downPressed = true; break;
            case KeyEvent.VK_A: leftPressed = true; break;
            case KeyEvent.VK_D: rightPressed = true; break;
            case KeyEvent.VK_SPACE: spacePressed = true; break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: upPressed = false; break;
            case KeyEvent.VK_S: downPressed = false; break;
            case KeyEvent.VK_A: leftPressed = false; break;
            case KeyEvent.VK_D: rightPressed = false; break;
            case KeyEvent.VK_SPACE: spacePressed = false; break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // 鼠标事件
    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            spacePressed = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            spacePressed = false;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}