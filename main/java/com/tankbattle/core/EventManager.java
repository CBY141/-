package main.java.com.tankbattle.core;

import java.util.*;
import java.util.concurrent.*;

/**
 * 事件管理器 - 单例模式
 * 负责事件的分发和管理
 */
public class EventManager {
    private static EventManager instance;

    // 事件监听器映射
    private Map<Event.EventType, List<EventListener>> listeners = new ConcurrentHashMap<>();

    private EventManager() {
        // 初始化所有事件类型的监听器列表
        for (Event.EventType type : Event.EventType.values()) {
            listeners.put(type, new CopyOnWriteArrayList<>());
        }

        System.out.println("事件管理器初始化完成");
    }

    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    // 注册事件监听器
    public void registerListener(Event.EventType type, EventListener listener) {
        List<EventListener> list = listeners.get(type);
        if (list != null && !list.contains(listener)) {
            list.add(listener);
        }
    }

    // 注册多个事件类型的监听器
    public void registerListener(EventListener listener, Event.EventType... types) {
        for (Event.EventType type : types) {
            registerListener(type, listener);
        }
    }

    // 注销事件监听器
    public void unregisterListener(Event.EventType type, EventListener listener) {
        List<EventListener> list = listeners.get(type);
        if (list != null) {
            list.remove(listener);
        }
    }

    // 注销所有事件类型的监听器
    public void unregisterListener(EventListener listener) {
        for (List<EventListener> list : listeners.values()) {
            list.remove(listener);
        }
    }

    // 触发事件（立即处理）
    public void triggerEvent(GameEvent event) {
        if (event == null) return;

        List<EventListener> list = listeners.get(event.getType());
        if (list != null) {
            for (EventListener listener : list) {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    System.err.println("事件处理异常: " + e.getMessage());
                }
            }
        }
    }

    // 清理资源
    public void dispose() {
        listeners.clear();
    }
}