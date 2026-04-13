package main.java.com.tankbattle.system;

/**
 * 组件接口 - 所有游戏组件的基础
 */
public interface Component {
    /**
     * 初始化组件
     * @param entity 所属的实体
     */
    void initialize(Entity entity);

    /**
     * 更新组件状态
     * @param deltaTime 自上一帧以来的时间（秒）
     */
    void update(float deltaTime);

    /**
     * 清理组件资源
     */
    void dispose();
}