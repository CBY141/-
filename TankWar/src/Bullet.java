import java.awt.*;

public class Bullet {
    private int x, y;
    private int direction;
    private boolean alive = true;
    private Color color;

    public Bullet(int x, int y, int direction, boolean fromPlayer) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.color = fromPlayer ? new Color(255, 255, 0) : new Color(255, 100, 0); // 玩家黄色，敌人橙色
    }

    public void update() {
        switch (direction) {
            case GameConfig.DIR_UP: y -= GameConfig.BULLET_SPEED; break;
            case GameConfig.DIR_DOWN: y += GameConfig.BULLET_SPEED; break;
            case GameConfig.DIR_LEFT: x -= GameConfig.BULLET_SPEED; break;
            case GameConfig.DIR_RIGHT: x += GameConfig.BULLET_SPEED; break;
        }

        if (x < 0 || x > GameConfig.WINDOW_WIDTH || y < 0 || y > GameConfig.WINDOW_HEIGHT) {
            alive = false;
        }
    }

    public void draw(Graphics g) {
        if (!alive) return;

        // 子弹主体
        g.setColor(color);
        g.fillOval(x - GameConfig.BULLET_SIZE/2, y - GameConfig.BULLET_SIZE/2,
                GameConfig.BULLET_SIZE, GameConfig.BULLET_SIZE);

        // 子弹光晕效果
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
        g.fillOval(x - GameConfig.BULLET_SIZE, y - GameConfig.BULLET_SIZE,
                GameConfig.BULLET_SIZE * 2, GameConfig.BULLET_SIZE * 2);

        // 子弹尾迹（根据方向）
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
        switch (direction) {
            case GameConfig.DIR_UP:
                g.fillOval(x - 1, y + 3, 2, 4);
                break;
            case GameConfig.DIR_DOWN:
                g.fillOval(x - 1, y - 7, 2, 4);
                break;
            case GameConfig.DIR_LEFT:
                g.fillOval(x + 3, y - 1, 4, 2);
                break;
            case GameConfig.DIR_RIGHT:
                g.fillOval(x - 7, y - 1, 4, 2);
                break;
        }
    }

    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }

    public boolean collidesWith(int targetX, int targetY, int targetSize) {
        // 更精确的圆形碰撞检测
        int bulletCenterX = x;
        int bulletCenterY = y;
        int targetCenterX = targetX + targetSize / 2;
        int targetCenterY = targetY + targetSize / 2;

        int dx = bulletCenterX - targetCenterX;
        int dy = bulletCenterY - targetCenterY;
        int distance = (int)Math.sqrt(dx*dx + dy*dy);

        return distance < (GameConfig.BULLET_SIZE/2 + targetSize/2);
    }
}