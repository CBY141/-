package main.java.com.tankbattle.core;

import java.awt.Graphics;

/**
 * 游戏状态基类 - 提供默认空实现
 */
public abstract class BaseGameState implements GameState {
    protected GameManager gameManager;

    public BaseGameState(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override public void enter() {}
    @Override public void exit() {}
    @Override public void update(float deltaTime) {}
    @Override public void render(Graphics g) {}
    @Override public void keyPressed(int keyCode) {}
    @Override public void keyReleased(int keyCode) {}
    @Override public void mousePressed(int x, int y, int button) {}
    @Override public void mouseReleased(int x, int y, int button) {}
    @Override public void mouseMoved(int x, int y) {}
}