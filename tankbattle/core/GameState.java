package main.java.com.tankbattle.core;

import java.awt.Graphics;

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
    void mouseWheelMoved(int rotation); // 新增
}