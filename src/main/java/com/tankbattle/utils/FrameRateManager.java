package main.java.com.tankbattle.utils;

/**
 * 帧率管理器 - 控制游戏帧率
 */
public class FrameRateManager {
    private static FrameRateManager instance;

    // 目标帧率
    private int targetFPS = 60;
    private long targetFrameTime = 1000 / targetFPS;

    // 帧率统计
    private long lastFrameTime = 0;
    private long frameCount = 0;
    private long lastStatsTime = 0;
    private int currentFPS = 0;

    // 时间追踪
    private long deltaTime = 0;

    private FrameRateManager() {
        lastFrameTime = System.currentTimeMillis();
        lastStatsTime = lastFrameTime;
    }

    public static synchronized FrameRateManager getInstance() {
        if (instance == null) {
            instance = new FrameRateManager();
        }
        return instance;
    }

    /**
     * 开始一帧
     */
    public void startFrame() {
        long currentTime = System.currentTimeMillis();

        if (lastFrameTime > 0) {
            deltaTime = currentTime - lastFrameTime;
        }

        lastFrameTime = currentTime;
        frameCount++;

        if (currentTime - lastStatsTime >= 1000) {
            currentFPS = (int) (frameCount * 1000 / (currentTime - lastStatsTime));
            frameCount = 0;
            lastStatsTime = currentTime;
        }
    }

    /**
     * 结束一帧，如果需要等待以达到目标帧率
     */
    public void endFrame() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastFrameTime;

        if (elapsed < targetFrameTime) {
            try {
                Thread.sleep(targetFrameTime - elapsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public int getTargetFPS() {
        return targetFPS;
    }

    public void setTargetFPS(int fps) {
        this.targetFPS = Math.max(1, Math.min(fps, 120));
        this.targetFrameTime = 1000 / this.targetFPS;
    }

    public int getCurrentFPS() {
        return currentFPS;
    }

    public float getDeltaTime() {
        return deltaTime / 1000.0f;
    }

    public long getDeltaTimeMillis() {
        return deltaTime;
    }

    public float getTimeScale() {
        return getDeltaTime() * targetFPS;
    }

    public void printStats() {
        System.out.printf("帧率: %d FPS, DeltaTime: %.3fs, TimeScale: %.2f%n",
                currentFPS, getDeltaTime(), getTimeScale());
    }
}