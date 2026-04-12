package main.java.com.tankbattle.core;

import main.java.com.tankbattle.game.states.GameOverState;
import main.java.com.tankbattle.game.states.PlayingState;
import main.java.com.tankbattle.game.states.StartMenuState;
import main.java.com.tankbattle.game.states.VictoryState;
import main.java.com.tankbattle.managers.EventManager;
import main.java.com.tankbattle.system.Event;
import main.java.com.tankbattle.system.GameEvent;
import main.java.com.tankbattle.utils.SoundEffectListener;

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

    // 游戏状态类型枚举
    public enum GameStateType {
        MENU,        // 主菜单
        PLAYING,     // 游戏中
        VICTORY,     // 胜利
        GAME_OVER,   // 游戏结束
        PAUSED       // 暂停
    }

    // 当前状态相关
    private GameStateType currentStateType = GameStateType.MENU;
    private GameState currentState = null;
    private Map<GameStateType, GameState> states = new EnumMap<>(GameStateType.class);

    // 输入处理器
    private InputHandler inputHandler = null;

    // 游戏循环控制
    private boolean running = true;
    private boolean paused = false;
    private long lastUpdateTime = System.nanoTime();

    // 性能统计
    private int totalFrames = 0;
    private long totalUpdateTime = 0;
    private long gameStartTime = System.currentTimeMillis();

    // 私有构造器
    private GameManager() {
        initializeStates();
        changeState(GameStateType.MENU);

        // 初始化音效监听器
        new SoundEffectListener();

        System.out.println("游戏管理器初始化完成");
    }

    // 获取单例实例
    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    // 初始化所有游戏状态
    private void initializeStates() {
        states.put(GameStateType.MENU, new StartMenuState(this));
        states.put(GameStateType.PLAYING, new PlayingState(this));
        states.put(GameStateType.VICTORY, new VictoryState(this));
        states.put(GameStateType.GAME_OVER, new GameOverState(this));

        // 注意：暂停状态通常不需要独立的类，可以通过PlayingState控制
    }

    /**
     * 切换到指定状态
     */
    public void changeState(GameStateType newStateType) {
        if (currentState != null) {
            currentState.exit();
        }

        currentStateType = newStateType;
        currentState = states.get(newStateType);

        if (currentState != null) {
            currentState.enter();

            // 触发状态变化事件
            EventManager.getInstance().triggerEvent(
                    new GameEvent(Event.EventType.GAME_STARTED, this, newStateType.name())
            );
        }
    }

    /**
     * 获取指定状态实例
     */
    public GameState getState(GameStateType stateType) {
        return states.get(stateType);
    }

    /**
     * 获取当前状态类型
     */
    public GameStateType getCurrentStateType() {
        return currentStateType;
    }

    /**
     * 获取输入处理器
     */
    public InputHandler getInputHandler() {
        if (inputHandler == null) {
            inputHandler = new InputHandler();
        }
        return inputHandler;
    }

    /**
     * 游戏主更新逻辑
     */
    public void update() {
        if (!running || paused) return;

        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0f;
        lastUpdateTime = currentTime;

        // 防止deltaTime过大（如从暂停恢复时）
        if (deltaTime > 0.1f) deltaTime = 0.1f;

        if (currentState != null) {
            currentState.update(deltaTime);
        }

        // 更新统计
        totalFrames++;
        totalUpdateTime += (System.nanoTime() - currentTime);
    }

    /**
     * 游戏主渲染逻辑
     */
    public void render(Graphics g) {
        if (currentState != null) {
            currentState.render(g);
        }
    }

    // ========== 输入事件转发 ==========

    public void handleKeyPressed(KeyEvent e) {
        if (currentState != null) {
            currentState.keyPressed(e.getKeyCode());
        }
    }

    public void handleKeyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // 全局快捷键处理
        if (keyCode == KeyEvent.VK_ESCAPE) {
            togglePause();
        }

        if (currentState != null) {
            currentState.keyReleased(keyCode);
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
        // 鼠标拖动通常转发为鼠标移动
        handleMouseMoved(e);
    }

    // ========== 游戏控制方法 ==========

    /**
     * 开始新游戏
     */
    public void startNewGame() {
        changeState(GameStateType.PLAYING);
    }

    /**
     * 返回主菜单
     */
    public void returnToMenu() {
        changeState(GameStateType.MENU);
    }

    /**
     * 暂停/恢复游戏
     */
    public void togglePause() {
        if (currentStateType == GameStateType.PLAYING) {
            paused = !paused;

            if (paused) {
                EventManager.getInstance().triggerEvent(
                        GameEvent.gamePaused()
                );
                System.out.println("游戏已暂停");
            } else {
                EventManager.getInstance().triggerEvent(
                        GameEvent.gameResumed()
                );
                lastUpdateTime = System.nanoTime(); // 重置更新时间
                System.out.println("游戏已恢复");
            }
        }
    }

    /**
     * 重新启动游戏
     */
    public void restart() {
        paused = false;
        changeState(GameStateType.MENU);
    }

    /**
     * 停止游戏
     */
    public void stop() {
        running = false;
        ResourceManager.getInstance().dispose();
        EventManager.getInstance().dispose();

        System.out.println("游戏已停止");
    }

    // ========== 获取游戏信息 ==========

    /**
     * 是否在游戏中
     */
    public boolean isInGame() {
        return currentStateType == GameStateType.PLAYING;
    }

    /**
     * 游戏是否暂停
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * 获取游戏运行时间（毫秒）
     */
    public long getGameTime() {
        return System.currentTimeMillis() - gameStartTime;
    }

    /**
     * 获取帧率统计
     */
    public float getAverageFPS() {
        if (totalFrames == 0) return 0;
        return 1000.0f / ((float)totalUpdateTime / totalFrames / 1_000_000.0f);
    }

    /**
     * 获取平均帧时间（毫秒）
     */
    public float getAverageFrameTime() {
        if (totalFrames == 0) return 0;
        return (float)totalUpdateTime / totalFrames / 1_000_000.0f;
    }

    /**
     * 打印性能统计
     */
    public void printPerformanceStats() {
        System.out.println("========== 性能统计 ==========");
        System.out.printf("平均帧率: %.2f FPS%n", getAverageFPS());
        System.out.printf("平均帧时间: %.3f ms%n", getAverageFrameTime());
        System.out.println("总帧数: " + totalFrames);
        System.out.println("游戏时间: " + (getGameTime() / 1000) + " 秒");
        System.out.println("当前状态: " + currentStateType);
        System.out.println("============================");
    }

    /**
     * 获取游戏管理器状态摘要
     */
    public String getStatusSummary() {
        return String.format(
                "GameManager{状态=%s, 运行中=%s, 暂停=%s, 总帧数=%d, 游戏时间=%.1fs}",
                currentStateType, running, paused, totalFrames, getGameTime() / 1000.0
        );
    }
}