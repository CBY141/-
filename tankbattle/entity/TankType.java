package main.java.com.tankbattle.entity;

import java.awt.Color;

public enum TankType {
    BALANCED("均衡型",
            new Color(0, 180, 0),
            2, 3, 5,
            new SkillInfo("护盾", "盾", 10f, 3f),
            new SkillInfo("穿透弹", "穿", 8f, 5f),
            new SkillInfo("闪现", "闪", 5f, 0f),
            new SkillInfo("时间减速", "缓", 12f, 5f, 0.3f)),

    SPEEDSTER("速度型",
            new Color(0, 200, 255),
            3, 2, 3,
            new SkillInfo("冲刺", "冲", 6f, 2f),
            new SkillInfo("双倍射速", "速", 6f, 4f),
            new SkillInfo("幻影", "影", 4f, 0f),
            new SkillInfo("时间加速", "疾", 10f, 4f, 0.5f)),

    HEAVY("重装型",
            new Color(180, 100, 50),
            1, 5, 8,
            new SkillInfo("钢铁护盾", "钢", 15f, 5f),
            new SkillInfo("高爆弹", "爆", 12f, 3f),
            new SkillInfo("震荡波", "震", 8f, 0f),
            new SkillInfo("装甲强化", "甲", 0f, 0f, 1.0f)),

    SNIPER("狙击型",
            new Color(150, 150, 150),
            2, 3, 6,
            new SkillInfo("隐身", "隐", 10f, 4f),
            new SkillInfo("穿甲弹", "穿", 8f, 6f),
            new SkillInfo("狙击镜", "镜", 6f, 0f),
            new SkillInfo("标记", "标", 12f, 8f, 0.8f)),

    TRICKSTER("诡术型",
            new Color(200, 0, 200),
            2, 3, 4,
            new SkillInfo("分身", "分", 12f, 3f),
            new SkillInfo("弹射弹", "弹", 10f, 4f),
            new SkillInfo("瞬移", "瞬", 3f, 0f),
            new SkillInfo("混乱", "乱", 15f, 5f, 0.4f));

    private final String name;
    private final Color color;
    private final int baseSpeed;
    private final int baseLives;
    private final int initialSpecialBullets;
    private final SkillInfo skillQ, skillE, skillR, skillF;

    TankType(String name, Color color, int baseSpeed, int baseLives, int initialSpecialBullets,
             SkillInfo q, SkillInfo e, SkillInfo r, SkillInfo f) {
        this.name = name;
        this.color = color;
        this.baseSpeed = baseSpeed;
        this.baseLives = baseLives;
        this.initialSpecialBullets = initialSpecialBullets;
        this.skillQ = q;
        this.skillE = e;
        this.skillR = r;
        this.skillF = f;
    }

    public String getName() { return name; }
    public Color getColor() { return color; }
    public int getBaseSpeed() { return baseSpeed; }
    public int getBaseLives() { return baseLives; }
    public int getInitialSpecialBullets() { return initialSpecialBullets; }

    public SkillInfo getSkillQ() { return skillQ; }
    public SkillInfo getSkillE() { return skillE; }
    public SkillInfo getSkillR() { return skillR; }
    public SkillInfo getSkillF() { return skillF; }

    public String getSkillDesc() {
        return skillQ.displayName + "/" + skillE.displayName + "/" + skillR.displayName + "/" + skillF.displayName;
    }

    // 内部类存储技能信息
    public static class SkillInfo {
        public final String displayName;
        public final String iconText;
        public final float cooldown;
        public final float duration;
        public final float extraParam; // 用于减速比例等

        public SkillInfo(String displayName, String iconText, float cooldown, float duration) {
            this(displayName, iconText, cooldown, duration, 0f);
        }

        public SkillInfo(String displayName, String iconText, float cooldown, float duration, float extraParam) {
            this.displayName = displayName;
            this.iconText = iconText;
            this.cooldown = cooldown;
            this.duration = duration;
            this.extraParam = extraParam;
        }
    }
}