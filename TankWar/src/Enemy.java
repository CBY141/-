import java.awt.*;
import java.util.List;
import java.util.Random;

public class Enemy {
    public int x, y;
    public int direction;
    public boolean alive = true;
    private int moveTimer = 0;
    private int shootTimer = 0;
    private Random random = new Random();

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
        this.direction = random.nextInt(4);
    }

    public void update(GameWorld gameWorld, List<Bullet> bullets) {
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

    public void draw(Graphics g) {
        if (!alive) return;

        int centerX = x + GameConfig.TANK_WIDTH / 2;
        int centerY = y + GameConfig.TANK_HEIGHT / 2;

        // 绘制坦克主体
        drawTankBody(g, x, y, GameConfig.ENEMY_TANK_COLOR, GameConfig.TANK_DETAIL_COLOR);

        // 绘制炮管
        Color turretColor = GameConfig.TANK_TURRET_COLOR;
        int turretEndX = centerX, turretEndY = centerY;
        switch (direction) {
            case GameConfig.DIR_UP: turretEndY = centerY - GameConfig.TANK_TURRET_LENGTH; break;
            case GameConfig.DIR_DOWN: turretEndY = centerY + GameConfig.TANK_TURRET_LENGTH; break;
            case GameConfig.DIR_LEFT: turretEndX = centerX - GameConfig.TANK_TURRET_LENGTH; break;
            case GameConfig.DIR_RIGHT: turretEndX = centerX + GameConfig.TANK_TURRET_LENGTH; break;
        }
        g.setColor(turretColor);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(centerX, centerY, turretEndX, turretEndY);
        g2d.setStroke(new BasicStroke(1));

        // 炮塔
        g.setColor(turretColor.darker());
        g.fillOval(centerX - 4, centerY - 4, 8, 8);

        // 敌人标识
        g.setColor(Color.RED);
        g.fillRect(centerX - 2, y + 2, 4, 4);
    }

    private void drawTankBody(Graphics g, int x, int y, Color mainColor, Color detailColor) {
        g.setColor(mainColor);
        g.fillRect(x + 2, y + 2, GameConfig.TANK_WIDTH - 4, GameConfig.TANK_HEIGHT - 4);
        g.setColor(detailColor);
        g.fillRect(x, y, GameConfig.TANK_WIDTH, 3);
        g.fillRect(x, y + GameConfig.TANK_HEIGHT - 3, GameConfig.TANK_WIDTH, 3);
        g.fillRect(x, y, 3, GameConfig.TANK_HEIGHT);
        g.fillRect(x + GameConfig.TANK_WIDTH - 3, y, 3, GameConfig.TANK_HEIGHT);
        g.setColor(mainColor.darker());
        g.fillRect(x + 6, y + 6, GameConfig.TANK_WIDTH - 12, GameConfig.TANK_HEIGHT - 12);
        g.setColor(Color.BLACK);
        g.drawRect(x + 6, y + 6, GameConfig.TANK_WIDTH - 12, GameConfig.TANK_HEIGHT - 12);
    }

    public boolean collidesWith(int targetX, int targetY) {
        int thisCenterX = x + GameConfig.TANK_WIDTH / 2;
        int thisCenterY = y + GameConfig.TANK_HEIGHT / 2;
        int targetCenterX = targetX + GameConfig.TANK_WIDTH / 2;
        int targetCenterY = targetY + GameConfig.TANK_HEIGHT / 2;
        int dx = thisCenterX - targetCenterX;
        int dy = thisCenterY - targetCenterY;
        int distance = (int)Math.sqrt(dx*dx + dy*dy);
        int collisionDistance = GameConfig.TANK_WIDTH - 2;
        return distance < collisionDistance;
    }
}