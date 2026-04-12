package main.java.com.tankbattle.system;

/**
 * 事件监听器接口
 */
public interface EventListener {
    /**
     * 处理事件
     */
    void onEvent(GameEvent event);
}