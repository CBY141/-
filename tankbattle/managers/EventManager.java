package main.java.com.tankbattle.managers;

import main.java.com.tankbattle.system.Event;
import main.java.com.tankbattle.system.EventListener;
import main.java.com.tankbattle.system.GameEvent;
import main.java.com.tankbattle.utils.LogUtil;

import java.util.*;
import java.util.concurrent.*;

/**
 * 事件管理器 - 单例模式
 * 负责事件的分发和管理
 */
public class EventManager {
    private static volatile EventManager instance;

    private Map<Event.EventType, List<EventListener>> listeners = new ConcurrentHashMap<>();

    private EventManager() {
        for (Event.EventType type : Event.EventType.values()) {
            listeners.put(type, new CopyOnWriteArrayList<>());
        }
        LogUtil.info("事件管理器初始化完成");
    }

    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public void registerListener(Event.EventType type, EventListener listener) {
        List<EventListener> list = listeners.get(type);
        if (list != null && !list.contains(listener)) {
            list.add(listener);
        }
    }

    public void registerListener(EventListener listener, Event.EventType... types) {
        for (Event.EventType type : types) {
            registerListener(type, listener);
        }
    }

    public void unregisterListener(Event.EventType type, EventListener listener) {
        List<EventListener> list = listeners.get(type);
        if (list != null) {
            list.remove(listener);
        }
    }

    public void unregisterListener(EventListener listener) {
        for (List<EventListener> list : listeners.values()) {
            list.remove(listener);
        }
    }

    public void triggerEvent(GameEvent event) {
        if (event == null) return;

        List<EventListener> list = listeners.get(event.getType());
        if (list != null) {
            for (EventListener listener : list) {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    LogUtil.warning("事件处理异常: " + e.getMessage());
                }
            }
        }
    }

    public void dispose() {
        listeners.clear();
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
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

    public void printStats() {
        Map<String, Object> stats = getStats();
        LogUtil.info("=== 事件管理器统计 ===");
        LogUtil.info("事件类型数量: " + stats.get("eventTypes"));
        LogUtil.info("监听器总数: " + stats.get("totalListeners"));

        @SuppressWarnings("unchecked")
        Map<String, Integer> listenerCounts = (Map<String, Integer>) stats.get("listenerCounts");
        if (!listenerCounts.isEmpty()) {
            LogUtil.info("各类型监听器数量:");
            for (Map.Entry<String, Integer> entry : listenerCounts.entrySet()) {
                LogUtil.info("  " + entry.getKey() + ": " + entry.getValue());
            }
        }
    }
}