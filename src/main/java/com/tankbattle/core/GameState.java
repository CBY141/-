package main.java.com.tankbattle.core;

import java.awt.Graphics;

/**
 * 游戏状态接口 - 状态模式的核心
 */
public interface GameState {
    void enter();
    void exit();
    void update(float deltaTime);
    void render(Graphics g);
    void keyPressed(int keyCode);
    void keyReleased(int keyCode);
    void mousePressed(int x, int y, int button);
    void mouseReleased(int x, int y, int button);
    void mouseMoved(int x, int y);
}