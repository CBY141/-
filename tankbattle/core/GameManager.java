package main.java.com.tankbattle.core;

import main.java.com.tankbattle.game.states.*;
import main.java.com.tankbattle.managers.EventManager;
import main.java.com.tankbattle.system.Event;
import main.java.com.tankbattle.system.GameEvent;
import main.java.com.tankbattle.utils.FrameRateManager;
import main.java.com.tankbattle.utils.LogUtil;
import main.java.com.tankbattle.utils.SoundEffectListener;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;

public class GameManager {
    private static volatile GameManager instance;

    public enum GameStateType {
        MENU, TANK_SELECT, PLAYING, VICTORY, GAME_OVER, ACHIEVEMENT, SAVE_LOAD, PAUSED
    }

    private GameStateType currentStateType = GameStateType.MENU;
    private GameState currentState = null;
    private Map<GameStateType, GameState> states = new EnumMap<>(GameStateType.class);

    private InputHandler inputHandler = null;

    private boolean running = true;
    private boolean paused = false;

    private int totalFrames = 0;
    private long totalUpdateTime = 0;
    private long gameStartTime = System.currentTimeMillis();

    private FrameRateManager frameRateManager;

    private GameManager() {
        frameRateManager = FrameRateManager.getInstance();
        initializeStates();
        changeState(GameStateType.MENU);
        new SoundEffectListener();
        LogUtil.info("游戏管理器初始化完成");
    }

    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    private void initializeStates() {
        states.put(GameStateType.MENU, new StartMenuState(this));
        states.put(GameStateType.TANK_SELECT, new TankSelectState(this));
        states.put(GameStateType.PLAYING, new PlayingState(this));
        states.put(GameStateType.VICTORY, new VictoryState(this));
        states.put(GameStateType.GAME_OVER, new GameOverState(this));
        states.put(GameStateType.ACHIEVEMENT, new AchievementState(this));
        states.put(GameStateType.SAVE_LOAD, new SaveLoadState(this));
    }

    public void changeState(GameStateType newStateType) {
        if (currentState != null) {
            currentState.exit();
        }

        currentStateType = newStateType;
        currentState = states.get(newStateType);

        if (currentState != null) {
            currentState.enter();
            EventManager.getInstance().triggerEvent(
                    new GameEvent(Event.EventType.GAME_STARTED, this, newStateType.name())
            );
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

    // ========== 关键修改：即使暂停也调用状态的update ==========
    public void update() {
        if (!running) return;

        float deltaTime = frameRateManager.getDeltaTime();
        if (deltaTime > 0.1f) deltaTime = 0.1f;

        long startNano = System.nanoTime();

        // 始终调用当前状态的update，让状态内部处理暂停逻辑
        if (currentState != null) {
            currentState.update(deltaTime);
        }

        totalFrames++;
        totalUpdateTime += (System.nanoTime() - startNano);
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

    public void handleMouseDragged(MouseEvent e) {
        handleMouseMoved(e);
    }

    public void startNewGame() {
        paused = false;
        changeState(GameStateType.PLAYING);
    }

    public void returnToMenu() {
        paused = false;
        changeState(GameStateType.MENU);
    }

    public void togglePause() {
        if (currentStateType == GameStateType.PLAYING) {
            paused = !paused;
            if (paused) {
                EventManager.getInstance().triggerEvent(GameEvent.gamePaused());
                LogUtil.info("游戏已暂停");
            } else {
                EventManager.getInstance().triggerEvent(GameEvent.gameResumed());
                LogUtil.info("游戏已恢复");
            }
        }
    }

    public void restart() {
        paused = false;
        changeState(GameStateType.MENU);
    }

    public void stop() {
        running = false;
        ResourceManager.getInstance().dispose();
        EventManager.getInstance().dispose();
        LogUtil.info("游戏已停止");
    }

    public boolean isInGame() {
        return currentStateType == GameStateType.PLAYING;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isRunning() {
        return running;
    }

    public long getGameTime() {
        return System.currentTimeMillis() - gameStartTime;
    }

    public float getAverageFPS() {
        if (totalFrames == 0) return 0;
        return 1000.0f / ((float) totalUpdateTime / totalFrames / 1_000_000.0f);
    }

    public float getAverageFrameTime() {
        if (totalFrames == 0) return 0;
        return (float) totalUpdateTime / totalFrames / 1_000_000.0f;
    }

    public void printPerformanceStats() {
        LogUtil.info("========== 性能统计 ==========");
        LogUtil.info(String.format("平均帧率: %.2f FPS", getAverageFPS()));
        LogUtil.info(String.format("平均帧时间: %.3f ms", getAverageFrameTime()));
        LogUtil.info("总帧数: " + totalFrames);
        LogUtil.info("游戏时间: " + (getGameTime() / 1000) + " 秒");
        LogUtil.info("当前状态: " + currentStateType);
        LogUtil.info("============================");
    }

    public String getStatusSummary() {
        return String.format(
                "GameManager{状态=%s, 运行中=%s, 暂停=%s, 总帧数=%d, 游戏时间=%.1fs}",
                currentStateType, running, paused, totalFrames, getGameTime() / 1000.0
        );
    }
}