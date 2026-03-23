import java.awt.*;
import java.util.List;

public class Player {
    public int x, y;
    public int lives = GameConfig.PLAYER_LIVES;
    public boolean inGrass = false;
    private int shootCooldown = 0;
    private static final int SHOOT_COOLDOWN_TIME = 20;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update(boolean up, boolean down, boolean left, boolean right, boolean shoot,
                       int mouseX, int mouseY, GameWorld world, List<Bullet> bullets) {
        // 1. 计算期望的新位置（简化版，先不考虑复杂碰撞）
        int newX = x;
        int newY = y;

        if (up) newY -= GameConfig.PLAYER_SPEED;
        if (down) newY += GameConfig.PLAYER_SPEED;
        if (left) newX -= GameConfig.PLAYER_SPEED;
        if (right) newX += GameConfig.PLAYER_SPEED;

        // 2. 保存原始位置用于回退
        int originalX = x;
        int originalY = y;

        // 3. 先尝试移动到新位置
        x = newX;
        y = newY;

        // 4. 检查新位置是否有效（地图通行性和窗口边界）
        boolean positionValid = world.isPositionPassable(x, y, GameConfig.TANK_WIDTH, GameConfig.TANK_HEIGHT);
        boolean inBounds = (x >= 0 && x <= GameConfig.WINDOW_WIDTH - GameConfig.TANK_WIDTH &&
                y >= 0 && y <= GameConfig.WINDOW_HEIGHT - GameConfig.TANK_HEIGHT);

        // 5. 如果位置无效，则回退到原始位置
        if (!positionValid || !inBounds) {
            x = originalX;
            y = originalY;

            // 6. 尝试分别移动X或Y方向（沿墙滑动）
            if (world.isPositionPassable(newX, originalY, GameConfig.TANK_WIDTH, GameConfig.TANK_HEIGHT) &&
                    newX >= 0 && newX <= GameConfig.WINDOW_WIDTH - GameConfig.TANK_WIDTH) {
                x = newX; // 只移动X方向
            }
            if (world.isPositionPassable(originalX, newY, GameConfig.TANK_WIDTH, GameConfig.TANK_HEIGHT) &&
                    newY >= 0 && newY <= GameConfig.WINDOW_HEIGHT - GameConfig.TANK_HEIGHT) {
                y = newY; // 只移动Y方向
            }
        }

        // 7. 更新草丛状态
        inGrass = world.isInGrass(x, y);

        // 8. 处理射击
        if (shoot && shootCooldown == 0) {
            int bulletX = x + GameConfig.TANK_WIDTH / 2;
            int bulletY = y + GameConfig.TANK_HEIGHT / 2;

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

        if (shootCooldown > 0) shootCooldown--;
    }

    public void draw(Graphics g, int mouseX, int mouseY) {
        int centerX = x + GameConfig.TANK_WIDTH / 2;
        int centerY = y + GameConfig.TANK_HEIGHT / 2;

        Color tankColor = GameConfig.PLAYER_TANK_COLOR;
        Color detailColor = GameConfig.TANK_DETAIL_COLOR;
        Color turretColor = GameConfig.TANK_TURRET_COLOR;

        if (inGrass) {
            tankColor = new Color(tankColor.getRed(), tankColor.getGreen(), tankColor.getBlue(), 100);
            detailColor = new Color(detailColor.getRed(), detailColor.getGreen(), detailColor.getBlue(), 100);
            turretColor = new Color(turretColor.getRed(), turretColor.getGreen(), turretColor.getBlue(), 100);
        }

        // 绘制坦克主体
        drawTankBody(g, x, y, tankColor, detailColor);

        // 绘制炮管（指向鼠标）
        drawTankTurret(g, centerX, centerY, mouseX, mouseY, turretColor);

        // 绘制玩家标识
        g.setColor(Color.WHITE);
        g.fillPolygon(
                new int[]{centerX, centerX - 3, centerX + 3},
                new int[]{y + 2, y + 8, y + 8},
                3
        );
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

    private void drawTankTurret(Graphics g, int centerX, int centerY, int targetX, int targetY, Color turretColor) {
        double angle = Math.atan2(targetY - centerY, targetX - centerX);
        int turretEndX = centerX + (int)(GameConfig.TANK_TURRET_LENGTH * Math.cos(angle));
        int turretEndY = centerY + (int)(GameConfig.TANK_TURRET_LENGTH * Math.sin(angle));

        g.setColor(turretColor);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(centerX, centerY, turretEndX, turretEndY);
        g2d.setStroke(new BasicStroke(1));

        g.setColor(turretColor.darker());
        g.fillOval(centerX - 4, centerY - 4, 8, 8);
    }
}