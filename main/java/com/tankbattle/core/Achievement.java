package main.java.com.tankbattle.core;

/**
 * 成就类
 */
public class Achievement {
    private String id;
    private String name;
    private String description;
    private boolean unlocked;
    private long unlockTime;

    public Achievement(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.unlocked = false;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isUnlocked() { return unlocked; }
    public long getUnlockTime() { return unlockTime; }

    public void unlock() {
        if (!unlocked) {
            unlocked = true;
            unlockTime = System.currentTimeMillis();
            System.out.println("成就解锁: " + name);
        }
    }

    public void reset() {
        unlocked = false;
        unlockTime = 0;
    }
}