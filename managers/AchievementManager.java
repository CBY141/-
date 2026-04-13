package main.java.com.tankbattle.managers;

import main.java.com.tankbattle.system.Achievement;
import main.java.com.tankbattle.system.Event;
import main.java.com.tankbattle.system.EventListener;
import main.java.com.tankbattle.system.GameEvent;

import java.util.*;

/**
 * 成就管理器
 */
public class AchievementManager implements EventListener {
    private static AchievementManager instance;
    private Map<String, Achievement> achievements = new HashMap<>();

    // 成就进度追踪
    private int enemiesKilled = 0;
    private int shotsFired = 0;
    private int damageTaken = 0;
    private int playerDeaths = 0;
    private int gamesPlayed = 0;
    private int gamesWon = 0;

    private AchievementManager() {
        initializeAchievements();
        EventManager.getInstance().registerListener(this,
                Event.EventType.ENEMY_DIED,
                Event.EventType.PLAYER_DIED,
                Event.EventType.PLAYER_HIT,
                Event.EventType.BULLET_FIRED,
                Event.EventType.GAME_STARTED,
                Event.EventType.GAME_VICTORY,
                Event.EventType.GAME_OVER
        );
    }

    public static synchronized AchievementManager getInstance() {
        if (instance == null) {
            instance = new AchievementManager();
        }
        return instance;
    }

    private void initializeAchievements() {
        // 战斗成就
        addAchievement(new Achievement("first_blood", "第一滴血", "消灭第一个敌人"));
        addAchievement(new Achievement("tank_destroyer", "坦克杀手", "消灭10个敌人"));
        addAchievement(new Achievement("tank_exterminator", "坦克灭绝者", "消灭50个敌人"));
        addAchievement(new Achievement("sharpshooter", "神射手", "连续命中5个敌人"));
        addAchievement(new Achievement("explosive_expert", "爆炸专家", "一次爆炸消灭3个敌人"));

        // 生存成就
        addAchievement(new Achievement("survivor", "幸存者", "一场游戏中没有死亡"));
        addAchievement(new Achievement("iron_will", "钢铁意志", "承受1000点伤害"));
        addAchievement(new Achievement("ghost", "幽灵", "一场游戏中没有被敌人击中"));
        addAchievement(new Achievement("pacifist", "和平主义者", "一场游戏中没有消灭敌人"));

        // 技能成就
        addAchievement(new Achievement("quick_draw", "快枪手", "一秒内发射5发子弹"));
        addAchievement(new Achievement("trick_shot", "技巧射击", "在最大距离消灭敌人"));
        addAchievement(new Achievement("ninja", "忍者", "消灭敌人而不被发现"));

        // 游戏进度成就
        addAchievement(new Achievement("first_win", "首胜", "赢得第一场游戏"));
        addAchievement(new Achievement("veteran", "老兵", "完成10场游戏"));
        addAchievement(new Achievement("master", "大师", "赢得10场游戏"));
        addAchievement(new Achievement("perfectionist", "完美主义者", "以满生命值赢得游戏"));

        // 特殊成就
        addAchievement(new Achievement("speedrunner", "速通玩家", "在60秒内赢得游戏"));
        addAchievement(new Achievement("collector", "收藏家", "解锁所有成就"));
    }

    private void addAchievement(Achievement achievement) {
        achievements.put(achievement.getId(), achievement);
    }

    public Achievement getAchievement(String id) {
        return achievements.get(id);
    }

    public Achievement[] getAllAchievements() {
        return achievements.values().toArray(new Achievement[0]);
    }

    public Achievement[] getUnlockedAchievements() {
        List<Achievement> unlocked = new ArrayList<>();
        for (Achievement achievement : achievements.values()) {
            if (achievement.isUnlocked()) {
                unlocked.add(achievement);
            }
        }
        return unlocked.toArray(new Achievement[0]);
    }

    public int getUnlockedCount() {
        int count = 0;
        for (Achievement achievement : achievements.values()) {
            if (achievement.isUnlocked()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalCount() {
        return achievements.size();
    }

    public void resetAll() {
        for (Achievement achievement : achievements.values()) {
            achievement.reset();
        }
        resetStats();
    }

    public void resetStats() {
        enemiesKilled = 0;
        shotsFired = 0;
        damageTaken = 0;
        playerDeaths = 0;
        gamesPlayed = 0;
        gamesWon = 0;
    }

    @Override
    public void onEvent(GameEvent event) {
        switch (event.getType()) {
            case ENEMY_DIED:
                handleEnemyDied();
                break;

            case PLAYER_DIED:
                handlePlayerDied();
                break;

            case PLAYER_HIT:
                handlePlayerHit(event);
                break;

            case BULLET_FIRED:
                handleBulletFired();
                break;

            case GAME_STARTED:
                handleGameStarted();
                break;

            case GAME_VICTORY:
                handleGameVictory();
                break;

            case GAME_OVER:
                handleGameOver(event);
                break;
        }

        // 检查收集者成就
        if (getUnlockedCount() == getTotalCount() - 1) { // 还剩收集者成就未解锁
            Achievement collector = achievements.get("collector");
            if (collector != null && !collector.isUnlocked()) {
                collector.unlock();
            }
        }
    }

    private void handleEnemyDied() {
        enemiesKilled++;

        // 第一滴血成就
        if (enemiesKilled == 1) {
            unlockAchievement("first_blood");
        }

        // 坦克杀手成就
        if (enemiesKilled == 10) {
            unlockAchievement("tank_destroyer");
        }

        // 坦克灭绝者成就
        if (enemiesKilled == 50) {
            unlockAchievement("tank_exterminator");
        }
    }

    private void handlePlayerDied() {
        playerDeaths++;
    }

    private void handlePlayerHit(GameEvent event) {
        Object data = event.getData();
        if (data instanceof Integer) {
            damageTaken += (Integer) data;

            // 钢铁意志成就
            if (damageTaken >= 1000) {
                unlockAchievement("iron_will");
            }
        }
    }

    private void handleBulletFired() {
        shotsFired++;

        // 快枪手成就（需要在游戏中实时检测，这里简化处理）
        if (shotsFired >= 100) {
            unlockAchievement("quick_draw");
        }
    }

    private void handleGameStarted() {
        gamesPlayed++;

        // 老兵成就
        if (gamesPlayed == 10) {
            unlockAchievement("veteran");
        }
    }

    private void handleGameVictory() {
        gamesWon++;

        // 首胜成就
        if (gamesWon == 1) {
            unlockAchievement("first_win");
        }

        // 大师成就
        if (gamesWon == 10) {
            unlockAchievement("master");
        }

        // 幸存者成就（如果本场游戏没有死亡）
        if (playerDeaths == 0) {
            unlockAchievement("survivor");
        }

        // 完美主义者成就（需要记录本场游戏是否满血）
        // 这里简化处理，假设在VictoryState中会检查
    }

    private void handleGameOver(GameEvent event) {
        Object data = event.getData();
        if (data instanceof Boolean && !(Boolean)data) {
            // 游戏失败
            // 可以添加失败相关的成就
        }
    }

    private void unlockAchievement(String achievementId) {
        Achievement achievement = achievements.get(achievementId);
        if (achievement != null && !achievement.isUnlocked()) {
            achievement.unlock();

            // 触发成就解锁事件
            EventManager.getInstance().triggerEvent(
                    new GameEvent(Event.EventType.ACHIEVEMENT_UNLOCKED, achievement, null)
            );
        }
    }

    public void printStats() {
        System.out.println("========== 成就统计 ==========");
        System.out.println("总成就数: " + getTotalCount());
        System.out.println("已解锁: " + getUnlockedCount());
        System.out.println("进度: " + getUnlockedCount() + "/" + getTotalCount());

        System.out.println("\n游戏统计:");
        System.out.println("游戏场次: " + gamesPlayed);
        System.out.println("获胜场次: " + gamesWon);
        System.out.println("消灭敌人: " + enemiesKilled);
        System.out.println("发射子弹: " + shotsFired);
        System.out.println("承受伤害: " + damageTaken);
        System.out.println("玩家死亡: " + playerDeaths);

        System.out.println("\n已解锁成就:");
        for (Achievement achievement : getUnlockedAchievements()) {
            System.out.println("  ✓ " + achievement.getName() + " - " + achievement.getDescription());
        }

        System.out.println("\n未解锁成就:");
        for (Achievement achievement : achievements.values()) {
            if (!achievement.isUnlocked()) {
                System.out.println("  ○ " + achievement.getName() + " - " + achievement.getDescription());
            }
        }
        System.out.println("============================");
    }
}