package main.java.com.tankbattle.utils;

import main.java.com.tankbattle.core.ConfigManager;

import java.awt.*;

/**
 * 坦克渲染工具类 - 提供统一的坦克绘制方法
 */
public class TankRenderer {

    /**
     * 绘制坦克主体
     */
    public static void drawTankBody(Graphics g, int x, int y, Color mainColor, Color detailColor) {
        ConfigManager config = ConfigManager.getInstance();
        int tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        int tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);

        g.setColor(mainColor);
        g.fillRect(x + 2, y + 2, tankWidth - 4, tankHeight - 4);

        g.setColor(detailColor);
        g.fillRect(x, y, tankWidth, 3);
        g.fillRect(x, y + tankHeight - 3, tankWidth, 3);
        g.fillRect(x, y, 3, tankHeight);
        g.fillRect(x + tankWidth - 3, y, 3, tankHeight);

        g.setColor(mainColor.darker());
        g.fillRect(x + 6, y + 6, tankWidth - 12, tankHeight - 12);

        g.setColor(Color.BLACK);
        g.drawRect(x + 6, y + 6, tankWidth - 12, tankHeight - 12);
    }

    /**
     * 绘制坦克炮塔（方向固定四个方向）
     */
    public static void drawTankTurret(Graphics g, int centerX, int centerY, int direction,
                                      Color turretColor, int turretLength) {
        int turretEndX = centerX;
        int turretEndY = centerY;
        switch (direction) {
            case ConfigManager.DIR_UP:
                turretEndY = centerY - turretLength;
                break;
            case ConfigManager.DIR_DOWN:
                turretEndY = centerY + turretLength;
                break;
            case ConfigManager.DIR_LEFT:
                turretEndX = centerX - turretLength;
                break;
            case ConfigManager.DIR_RIGHT:
                turretEndX = centerX + turretLength;
                break;
        }

        g.setColor(turretColor);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(centerX, centerY, turretEndX, turretEndY);
        g2d.setStroke(new BasicStroke(1));

        g.setColor(turretColor.darker());
        g.fillOval(centerX - 4, centerY - 4, 8, 8);
    }

    /**
     * 绘制坦克炮塔（鼠标方向）
     */
    public static void drawTankTurret(Graphics g, int centerX, int centerY,
                                      int targetX, int targetY, Color turretColor, int turretLength) {
        double angle = Math.atan2(targetY - centerY, targetX - centerX);
        int turretEndX = centerX + (int) (turretLength * Math.cos(angle));
        int turretEndY = centerY + (int) (turretLength * Math.sin(angle));

        g.setColor(turretColor);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(centerX, centerY, turretEndX, turretEndY);
        g2d.setStroke(new BasicStroke(1));

        g.setColor(turretColor.darker());
        g.fillOval(centerX - 4, centerY - 4, 8, 8);
    }

    /**
     * 绘制血条
     */
    public static void drawHealthBar(Graphics g, int x, int y, int tankWidth,
                                     float healthPercent, boolean isPlayer) {
        int barY = y - 8;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, barY, tankWidth, 4);

        int fillWidth = (int) (tankWidth * healthPercent);
        if (isPlayer) {
            if (healthPercent > 0.6) g.setColor(Color.GREEN);
            else if (healthPercent > 0.3) g.setColor(Color.YELLOW);
            else g.setColor(Color.RED);
        } else {
            if (healthPercent > 0.6) g.setColor(Color.RED.brighter());
            else if (healthPercent > 0.3) g.setColor(Color.ORANGE);
            else g.setColor(Color.RED.darker());
        }
        g.fillRect(x, barY, fillWidth, 4);
        g.setColor(Color.BLACK);
        g.drawRect(x, barY, tankWidth, 4);
    }
}