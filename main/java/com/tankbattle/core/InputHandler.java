package main.java.com.tankbattle.core;

import java.awt.event.*;

public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {
    public boolean upPressed = false, downPressed = false, leftPressed = false, rightPressed = false;
    public boolean spacePressed = false, rPressed = false;
    public int mouseX = 0, mouseY = 0;

    // 调试计数器
    private int keyPressCount = 0;
    private int mousePressCount = 0;

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        System.out.println("按键按下: " + keyCode + " (" + KeyEvent.getKeyText(keyCode) + ")");
        keyPressCount++;

        switch (keyCode) {
            case KeyEvent.VK_W:
                upPressed = true;
                System.out.println("上键按下 - upPressed=true");
                break;
            case KeyEvent.VK_S:
                downPressed = true;
                System.out.println("下键按下 - downPressed=true");
                break;
            case KeyEvent.VK_A:
                leftPressed = true;
                System.out.println("左键按下 - leftPressed=true");
                break;
            case KeyEvent.VK_D:
                rightPressed = true;
                System.out.println("右键按下 - rightPressed=true");
                break;
            case KeyEvent.VK_SPACE:
                spacePressed = true;
                System.out.println("空格键按下 - spacePressed=true");
                break;
            case KeyEvent.VK_R:
                rPressed = true;
                System.out.println("R键按下 - rPressed=true");
                break;
        }
        GameManager.getInstance().handleKeyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        System.out.println("按键释放: " + keyCode + " (" + KeyEvent.getKeyText(keyCode) + ")");

        switch (keyCode) {
            case KeyEvent.VK_W:
                upPressed = false;
                System.out.println("上键释放 - upPressed=false");
                break;
            case KeyEvent.VK_S:
                downPressed = false;
                System.out.println("下键释放 - downPressed=false");
                break;
            case KeyEvent.VK_A:
                leftPressed = false;
                System.out.println("左键释放 - leftPressed=false");
                break;
            case KeyEvent.VK_D:
                rightPressed = false;
                System.out.println("右键释放 - rightPressed=false");
                break;
            case KeyEvent.VK_SPACE:
                spacePressed = false;
                System.out.println("空格键释放 - spacePressed=false");
                break;
            case KeyEvent.VK_R:
                rPressed = false;
                System.out.println("R键释放 - rPressed=false");
                break;
        }
        GameManager.getInstance().handleKeyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        // 减少调试输出频率
        if (e.getX() % 50 == 0) {
            System.out.println("鼠标移动: (" + mouseX + ", " + mouseY + ")");
        }
        GameManager.getInstance().handleMouseMoved(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        GameManager.getInstance().handleMouseMoved(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePressCount++;
        System.out.println("鼠标按下: 按钮" + e.getButton() + ", 点击次数: " + mousePressCount);

        if (e.getButton() == MouseEvent.BUTTON1) {
            spacePressed = true;
            System.out.println("鼠标左键按下 - spacePressed=true");
        }
        GameManager.getInstance().handleMousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        System.out.println("鼠标释放: 按钮" + e.getButton());

        if (e.getButton() == MouseEvent.BUTTON1) {
            spacePressed = false;
            System.out.println("鼠标左键释放 - spacePressed=false");
        }
        GameManager.getInstance().handleMouseReleased(e);
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {
        System.out.println("鼠标进入游戏区域");
    }
    @Override public void mouseExited(MouseEvent e) {
        System.out.println("鼠标离开游戏区域");
    }

    // 获取输入状态统计
    public String getInputStats() {
        return String.format("按键: %d次, 鼠标: %d次, 方向: [W:%s A:%s S:%s D:%s] 射击: %s",
                keyPressCount, mousePressCount,
                upPressed, leftPressed, downPressed, rightPressed, spacePressed);
    }
}