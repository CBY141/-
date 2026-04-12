package main.java.com.tankbattle.managers;

import main.java.com.tankbattle.system.Event;
import main.java.com.tankbattle.system.EventListener;
import main.java.com.tankbattle.system.GameEvent;

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

    // ==================== 新增：获取统计信息的方法 ====================
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 监听器数量统计
        int totalListeners = 0;
        Map<String, Integer> listenerCounts = new HashMap<>();

        for (Map.Entry<Event.EventType, List<EventListener>> entry : listeners.entrySet()) {
            int count = entry.getValue().size();
            if (count > 0) {
                listenerCounts.put(entry.getKey().name(), count);
                totalListeners += count;
            }
        }

        stats.put("totalListeners", totalListeners);
        stats.put("listenerCounts", listenerCounts);
        stats.put("eventTypes", listeners.size());

        return stats;
    }

    // 打印统计信息
    public void printStats() {
        Map<String, Object> stats = getStats();
        System.out.println("=== 事件管理器统计 ===");
        System.out.println("事件类型数量: " + stats.get("eventTypes"));
        System.out.println("监听器总数: " + stats.get("totalListeners"));

        @SuppressWarnings("unchecked")
        Map<String, Integer> listenerCounts = (Map<String, Integer>) stats.get("listenerCounts");
        if (!listenerCounts.isEmpty()) {
            System.out.println("各类型监听器数量:");
            for (Map.Entry<String, Integer> entry : listenerCounts.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }
        }
    }
}