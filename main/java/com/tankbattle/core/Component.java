package main.java.com.tankbattle.core;

/**
 * 组件接口（简化版）
 */
public interface Component {
    /**
     * 组件更新
     */
    default void update(float deltaTime) {}

    /**
     * 组件销毁
     */
    default void dispose() {}
}