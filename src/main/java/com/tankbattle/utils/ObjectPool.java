package main.java.com.tankbattle.utils;

import java.util.*;
import java.util.function.Supplier;

/**
 * 对象池 - 通用对象池实现
 * 解决频繁创建销毁对象的性能问题
 */
public class ObjectPool<T> {
    private final Queue<T> availableObjects = new LinkedList<>();
    private final Set<T> inUseObjects = new HashSet<>();
    private final Supplier<T> objectFactory;
    private final int maxSize;

    public ObjectPool(Supplier<T> objectFactory, int initialSize, int maxSize) {
        this.objectFactory = objectFactory;
        this.maxSize = maxSize;

        // 预创建对象
        for (int i = 0; i < initialSize && i < maxSize; i++) {
            availableObjects.offer(objectFactory.get());
        }
    }

    public ObjectPool(Supplier<T> objectFactory, int initialSize) {
        this(objectFactory, initialSize, 100);
    }

    public synchronized T borrow() {
        T obj = availableObjects.poll();
        if (obj == null) {
            if (size() < maxSize) {
                obj = objectFactory.get();
            } else {
                // 池已满，等待或返回null
                return null;
            }
        }
        inUseObjects.add(obj);
        return obj;
    }

    public synchronized void returnObject(T obj) {
        if (obj == null) return;

        if (inUseObjects.remove(obj)) {
            availableObjects.offer(obj);
        }
    }

    public synchronized int size() {
        return availableObjects.size() + inUseObjects.size();
    }

    public synchronized int availableCount() {
        return availableObjects.size();
    }

    public synchronized int inUseCount() {
        return inUseObjects.size();
    }

    public synchronized void clear() {
        availableObjects.clear();
        inUseObjects.clear();
    }
}