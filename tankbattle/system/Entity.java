package main.java.com.tankbattle.system;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 游戏实体基类（简化版）
 */
public class Entity {
    private static int nextId = 1;

    protected int id;
    protected String name;
    protected boolean active = true;

    // 组件映射
    private Map<Class<?>, Object> components = new HashMap<>();

    public Entity(String name) {
        this.id = nextId++;
        this.name = name != null ? name : "Entity_" + id;
    }

    public Entity() {
        this(null);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // 添加组件
    public <T> void addComponent(T component) {
        components.put(component.getClass(), component);
    }

    // 获取组件
    public <T> T getComponent(Class<T> componentClass) {
        return componentClass.cast(components.get(componentClass));
    }

    // 检查是否有组件
    public <T> boolean hasComponent(Class<T> componentClass) {
        return components.containsKey(componentClass);
    }

    // 移除组件
    public <T> void removeComponent(Class<T> componentClass) {
        components.remove(componentClass);
    }

    // 获取所有组件类型
    public Set<Class<?>> getComponentTypes() {
        return components.keySet();
    }

    // 实体更新
    public void update(float deltaTime) {
        // 默认实现为空，由组件处理具体逻辑
    }


    @Override
    public String toString() {
        return String.format("%s[id=%d, active=%s, components=%d]",
                name, id, active, components.size());
    }
}