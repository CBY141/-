import java.awt.*;
import java.util.List;

public class Player {
    public int x, y;
    public int lives = GameConfig.PLAYER_LIVES;
    public boolean inGrass = false;
    public boolean dead = false; // 新增：死亡状态标志
    private int shootCooldown = 0;
    private static final int SHOOT_COOLDOWN_TIME = 20;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update(boolean up, boolean down, boolean left, boolean right, boolean shoot,
                       int mouseX, int mouseY, GameWorld world, List<Bullet> bullets) {
        // 新增：如果玩家已死亡，不执行任何更新
        if (dead) {
            return;
        }

        int newX = x;
        int newY = y;
        if (up) newY -= GameConfig.PLAYER_SPEED;
        if (down) newY += GameConfig.PLAYER_SPEED;
        if (left) newX -= GameConfig.PLAYER_SPEED;
        if (right) newX += GameConfig.PLAYER_SPEED;

        int originalX = x;
        int originalY = y;

        x = newX;
        y = newY;

        boolean positionValid = world.isPositionPassable(x, y, GameConfig.TANK_WIDTH, GameConfig.TANK_HEIGHT);
        boolean inBounds = (x >= 0 && x <= GameConfig.WINDOW_WIDTH - GameConfig.TANK_WIDTH &&
                y >= 0 && y <= GameConfig.WINDOW_HEIGHT - GameConfig.TANK_HEIGHT);

        if (!positionValid || !inBounds) {
            x = originalX;
            y = originalY;

            if (world.isPositionPassable(newX, originalY, GameConfig.TANK_WIDTH, GameConfig.TANK_HEIGHT) &&
                    newX >= 0 && newX <= GameConfig.WINDOW_WIDTH - GameConfig.TANK_WIDTH) {
                x = newX;
            }
            if (world.isPositionPassable(originalX, newY, GameConfig.TANK_WIDTH, GameConfig.TANK_HEIGHT) &&
                    newY >= 0 && newY <= GameConfig.WINDOW_HEIGHT - GameConfig.TANK_HEIGHT) {
                y = newY;
            }
        }

        inGrass = world.isInGrass(x, y);

        if (shoot && shootCooldown == 0) {
            int bulletX = x + GameConfig.TANK_WIDTH / 2;
            int bulletY = y + GameConfig.TANK_HEIGHT / 2;
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
        drawTankBody(g, x, y, tankColor, detailColor);
        drawTankTurret(g, centerX, centerY, mouseX, mouseY, turretColor);
        g.setColor(Color.WHITE);
        g.fillPolygon(new int[]{centerX, centerX - 3, centerX + 3},
                new int[]{y + 2, y + 8, y + 8}, 3);
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