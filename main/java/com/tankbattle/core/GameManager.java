package main.java.com.tankbattle.core;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;

/**
 * 游戏管理器 - 单例模式
 * 负责管理游戏状态、输入分发和游戏循环
 */
public class GameManager {
    private static GameManager instance;

    public enum GameStateType {
        MENU, PLAYING, VICTORY, GAME_OVER
    }

    private GameStateType currentStateType = GameStateType.MENU;
    private GameState currentState = null;
    private Map<GameStateType, GameState> states = new EnumMap<>(GameStateType.class);
    private InputHandler inputHandler = null;
    private boolean running = true;
    private long lastUpdateTime = System.nanoTime();

    private GameManager() {
        initStates();
        changeState(GameStateType.MENU);
    }

    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    private void initStates() {
        states.put(GameStateType.MENU, new StartMenuState(this));
        states.put(GameStateType.PLAYING, new PlayingState(this));
        states.put(GameStateType.VICTORY, new VictoryState(this));
        states.put(GameStateType.GAME_OVER, new GameOverState(this));
    }

    public void changeState(GameStateType newStateType) {
        if (currentState != null) {
            currentState.exit();
        }
        currentStateType = newStateType;
        currentState = states.get(newStateType);
        if (currentState != null) {
            currentState.enter();
        }
    }

    public GameState getState(GameStateType stateType) {
        return states.get(stateType);
    }

    public GameStateType getCurrentStateType() {
        return currentStateType;
    }

    public InputHandler getInputHandler() {
        if (inputHandler == null) {
            inputHandler = new InputHandler();
        }
        return inputHandler;
    }

    public void update() {
        if (!running) return;
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0f;
        lastUpdateTime = currentTime;
        if (deltaTime > 0.1f) deltaTime = 0.1f;
        if (currentState != null) {
            currentState.update(deltaTime);
        }
    }

    public void render(Graphics g) {
        if (currentState != null) {
            currentState.render(g);
        }
    }

    public void handleKeyPressed(KeyEvent e) {
        if (currentState != null) {
            currentState.keyPressed(e.getKeyCode());
        }
    }

    public void handleKeyReleased(KeyEvent e) {
        if (currentState != null) {
            currentState.keyReleased(e.getKeyCode());
        }
    }

    public void handleMousePressed(MouseEvent e) {
        if (currentState != null) {
            currentState.mousePressed(e.getX(), e.getY(), e.getButton());
        }
    }

    public void handleMouseReleased(MouseEvent e) {
        if (currentState != null) {
            currentState.mouseReleased(e.getX(), e.getY(), e.getButton());
        }
    }

    public void handleMouseMoved(MouseEvent e) {
        if (currentState != null) {
            currentState.mouseMoved(e.getX(), e.getY());
        }
    }

    public void stop() {
        running = false;
        ResourceManager.getInstance().dispose();
    }

    public void restart() {
        changeState(GameStateType.MENU);
    }
}