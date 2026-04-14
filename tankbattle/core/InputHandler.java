package main.java.com.tankbattle.core;

import main.java.com.tankbattle.game.ui.GamePanel;
import main.java.com.tankbattle.utils.LogUtil;

import java.awt.*;
import java.awt.event.*;

public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {
    public boolean upPressed = false, downPressed = false, leftPressed = false, rightPressed = false;
    public boolean shiftPressed = false;
    public boolean spacePressed = false;
    public boolean qPressed = false, ePressed = false, rPressed = false, fPressed = false;
    public boolean mouseLeftPressed = false;
    public boolean mouseRightPressed = false;
    public int mouseX = 0, mouseY = 0;
    public boolean pauseTyped = false;
    public boolean escapeTyped = false;

    private GamePanel gamePanel;
    private int baseWidth, baseHeight;

    public InputHandler() {
        ConfigManager config = ConfigManager.getInstance();
        baseWidth = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        baseHeight = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);
    }

    public void setGamePanel(GamePanel panel) {
        this.gamePanel = panel;
    }

    private Point convertMousePoint(int x, int y) {
        if (gamePanel == null) {
            return new Point(x, y);
        }
        float scale = gamePanel.getCurrentScale();
        int offsetX = gamePanel.getOffsetX();
        int offsetY = gamePanel.getOffsetY();

        if (scale <= 0) return new Point(x, y);

        int gameX = (int) ((x - offsetX) / scale);
        int gameY = (int) ((y - offsetY) / scale);
        gameX = Math.max(0, Math.min(baseWidth - 1, gameX));
        gameY = Math.max(0, Math.min(baseHeight - 1, gameY));
        return new Point(gameX, gameY);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_W:     upPressed = true; break;
            case KeyEvent.VK_S:     downPressed = true; break;
            case KeyEvent.VK_A:     leftPressed = true; break;
            case KeyEvent.VK_D:     rightPressed = true; break;
            case KeyEvent.VK_SHIFT: shiftPressed = true; break;
            case KeyEvent.VK_SPACE: spacePressed = true; break;
            case KeyEvent.VK_Q:     qPressed = true; break;
            case KeyEvent.VK_E:     ePressed = true; break;
            case KeyEvent.VK_R:     rPressed = true; break;
            case KeyEvent.VK_F:     fPressed = true; break;
            case KeyEvent.VK_P:     pauseTyped = true; break;
            case KeyEvent.VK_ESCAPE: escapeTyped = true; break;
        }
        GameManager.getInstance().handleKeyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_W:     upPressed = false; break;
            case KeyEvent.VK_S:     downPressed = false; break;
            case KeyEvent.VK_A:     leftPressed = false; break;
            case KeyEvent.VK_D:     rightPressed = false; break;
            case KeyEvent.VK_SHIFT: shiftPressed = false; break;
            case KeyEvent.VK_SPACE: spacePressed = false; break;
            case KeyEvent.VK_Q:     qPressed = false; break;
            case KeyEvent.VK_E:     ePressed = false; break;
            case KeyEvent.VK_R:     rPressed = false; break;
            case KeyEvent.VK_F:     fPressed = false; break;
            case KeyEvent.VK_P:     pauseTyped = false; break;
            case KeyEvent.VK_ESCAPE: escapeTyped = false; break;
        }
        GameManager.getInstance().handleKeyReleased(e);
    }

    @Override public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        Point p = convertMousePoint(e.getX(), e.getY());
        mouseX = p.x;
        mouseY = p.y;
        GameManager.getInstance().handleMouseMoved(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point p = convertMousePoint(e.getX(), e.getY());
        mouseX = p.x;
        mouseY = p.y;
        GameManager.getInstance().handleMouseMoved(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = convertMousePoint(e.getX(), e.getY());
        mouseX = p.x;
        mouseY = p.y;
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseLeftPressed = true;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            mouseRightPressed = true;
        }
        LogUtil.debug("鼠标按下: 屏幕(" + e.getX() + "," + e.getY() + ") -> 游戏(" + mouseX + "," + mouseY + ")");
        GameManager.getInstance().handleMousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Point p = convertMousePoint(e.getX(), e.getY());
        mouseX = p.x;
        mouseY = p.y;
        if (e.getButton() == MouseEvent.BUTTON1) {
            mouseLeftPressed = false;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            mouseRightPressed = false;
        }
        GameManager.getInstance().handleMouseReleased(e);
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public boolean isMoving() {
        return upPressed || downPressed || leftPressed || rightPressed;
    }
}