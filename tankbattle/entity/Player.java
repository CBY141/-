package main.java.com.tankbattle.entity;

import main.java.com.tankbattle.core.*;
import main.java.com.tankbattle.game.skill.*;
import main.java.com.tankbattle.managers.EventManager;
import main.java.com.tankbattle.system.*;
import main.java.com.tankbattle.world.GameWorld;
import main.java.com.tankbattle.game.factory.GameObjectFactory;
import main.java.com.tankbattle.utils.TankRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Player {
    public int x, y;
    public int lives;
    public boolean inGrass = false;
    public boolean dead = false;

    private static TankType selectedTankType = TankType.BALANCED;

    public static void setSelectedTankType(TankType type) {
        selectedTankType = type;
    }

    public static TankType getSelectedTankType() {
        return selectedTankType;
    }

    private List<Skill> skills = new ArrayList<>();
    private Skill skillQ;
    private Skill skillE;
    private Skill skillR;
    private Skill skillF;

    private int specialBulletCount;
    private static final int MAX_SPECIAL_BULLETS = 10;

    private boolean invulnerable = false;
    private boolean piercingBullet = false;
    private int currentDirection = ConfigManager.DIR_UP;

    private float shootCooldown = 0f;
    private static final float SHOOT_COOLDOWN_TIME = 0.25f;

    private int startX, startY;
    private int score = 0;

    private int playerSpeed;
    private int tankWidth;
    private int tankHeight;
    private int turretLength;
    private int windowWidth;
    private int windowHeight;
    private int initialLives;

    private GameWorld currentWorld;

    private TransformComponent transform = null;
    private HealthComponent health = null;
    private RenderComponent render = null;
    private boolean useComponents = false;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.startX = startX;
        this.startY = startY;
        cacheConfig();
        initSkills();
        EventManager.getInstance().triggerEvent(GameEvent.playerSpawned(this));
    }

    private void cacheConfig() {
        ConfigManager config = ConfigManager.getInstance();
        TankType type = getSelectedTankType();
        this.playerSpeed = type.getBaseSpeed();
        this.tankWidth = config.getInt(ConfigManager.KEY_TANK_WIDTH);
        this.tankHeight = config.getInt(ConfigManager.KEY_TANK_HEIGHT);
        this.turretLength = config.getInt(ConfigManager.KEY_TANK_TURRET_LENGTH);
        this.windowWidth = config.getInt(ConfigManager.KEY_WINDOW_WIDTH);
        this.windowHeight = config.getInt(ConfigManager.KEY_WINDOW_HEIGHT);
        this.initialLives = type.getBaseLives();
        this.lives = initialLives;
        this.specialBulletCount = type.getInitialSpecialBullets();
    }

    private void initSkills() {
        TankType type = getSelectedTankType();
        TankType.SkillInfo q = type.getSkillQ();
        TankType.SkillInfo e = type.getSkillE();
        TankType.SkillInfo r = type.getSkillR();
        TankType.SkillInfo f = type.getSkillF();

        skillQ = new ShieldSkill(q.displayName, q.iconText, q.cooldown, q.duration);
        skillE = new PierceSkill(e.displayName, e.iconText, e.cooldown, e.duration);
        skillR = new BlinkSkill(r.displayName, r.iconText, r.cooldown);
        skillF = new TimeSlowSkill(f.displayName, f.iconText, f.cooldown, f.duration, f.extraParam);

        skills.clear();
        skills.add(skillQ);
        skills.add(skillE);
        skills.add(skillR);
        skills.add(skillF);
    }

    public void enableComponents() {
        useComponents = true;
        transform = new TransformComponent();
        health = new HealthComponent();
        render = new RenderComponent();
        transform.setPosition(x, y);
        transform.setSize(tankWidth, tankHeight);
        health.setEntity(new Entity("Player_Entity"));
        health.setMaxHealth(lives * 100);
        health.setCurrentHealth(lives * 100);
        render.setColor(ConfigManager.PLAYER_TANK_COLOR);
        render.setVisible(true);
    }

    public void reset() {
        this.x = startX;
        this.y = startY;
        cacheConfig();
        this.inGrass = false;
        this.dead = false;
        this.shootCooldown = 0f;
        this.score = 0;
        this.invulnerable = false;
        this.piercingBullet = false;
        initSkills();
        if (useComponents && transform != null && health != null) {
            transform.setPosition(startX, startY);
            health.setCurrentHealth(lives * 100);
        }
    }

    public void update(float deltaTime, InputHandler input, GameWorld world, List<Bullet> bullets) {
        if (dead) return;
        this.currentWorld = world;

        for (Skill s : skills) {
            s.update(deltaTime);
        }
        invulnerable = skillQ.isActive();

        if (input.qPressed) tryActivateSkill(skillQ);
        if (input.ePressed) tryActivateSkill(skillE);
        if (input.rPressed) tryActivateSkill(skillR);
        if (input.fPressed) tryActivateSkill(skillF);

        float speedMultiplier = 1.0f;
        if (input.shiftPressed) speedMultiplier = 2.0f;
        if (input.spacePressed) speedMultiplier = 0.3f;
        int currentSpeed = Math.round(playerSpeed * speedMultiplier);

        int newX = x;
        int newY = y;
        if (input.upPressed) newY -= currentSpeed;
        if (input.downPressed) newY += currentSpeed;
        if (input.leftPressed) newX -= currentSpeed;
        if (input.rightPressed) newX += currentSpeed;

        if (input.upPressed) currentDirection = ConfigManager.DIR_UP;
        else if (input.downPressed) currentDirection = ConfigManager.DIR_DOWN;
        else if (input.leftPressed) currentDirection = ConfigManager.DIR_LEFT;
        else if (input.rightPressed) currentDirection = ConfigManager.DIR_RIGHT;

        int originalX = x;
        int originalY = y;
        x = newX;
        y = newY;

        boolean positionValid = world.isPositionPassable(x, y, tankWidth, tankHeight);
        boolean inBounds = (x >= 0 && x <= windowWidth - tankWidth && y >= 0 && y <= windowHeight - tankHeight);

        if (!positionValid || !inBounds) {
            x = originalX;
            y = originalY;
            if (world.isPositionPassable(newX, originalY, tankWidth, tankHeight) && newX >= 0 && newX <= windowWidth - tankWidth) {
                x = newX;
            }
            if (world.isPositionPassable(originalX, newY, tankWidth, tankHeight) && newY >= 0 && newY <= windowHeight - tankHeight) {
                y = newY;
            }
        }

        inGrass = world.isInGrass(x, y);

        if (shootCooldown > 0) {
            shootCooldown -= deltaTime;
        }

        if (input.mouseLeftPressed && shootCooldown <= 0) {
            shoot(bullets, input.mouseX, input.mouseY, false);
            shootCooldown = SHOOT_COOLDOWN_TIME;
        }
        if (input.mouseRightPressed && shootCooldown <= 0 && specialBulletCount > 0) {
            shoot(bullets, input.mouseX, input.mouseY, true);
            specialBulletCount--;
            shootCooldown = SHOOT_COOLDOWN_TIME;
        }

        if (useComponents && health != null) {
            health.update(deltaTime);
            this.lives = (health.getCurrentHealth() + 99) / 100;
            if (!health.isAlive() && !dead) {
                dead = true;
                EventManager.getInstance().triggerEvent(GameEvent.playerDied(this));
            }
        }
    }

    private void tryActivateSkill(Skill skill) {
        if (skill.tryActivate(this)) {
        }
    }

    private void shoot(List<Bullet> bullets, int targetX, int targetY, boolean special) {
        int bulletX = x + tankWidth / 2;
        int bulletY = y + tankHeight / 2;
        double angle = Math.atan2(targetY - bulletY, targetX - bulletX);
        int direction;
        if (Math.abs(angle) < Math.PI / 4) direction = ConfigManager.DIR_RIGHT;
        else if (Math.abs(angle) > 3 * Math.PI / 4) direction = ConfigManager.DIR_LEFT;
        else if (angle > 0) direction = ConfigManager.DIR_DOWN;
        else direction = ConfigManager.DIR_UP;

        Bullet bullet = GameObjectFactory.createBullet(bulletX, bulletY, direction, true);
        if (bullet != null) {
            if (special) {
                bullet.setSpecial(true);
                bullet.setDamage(2);
                bullet.setPenetrating(true);
            }
            if (piercingBullet && !special) {
                bullet.setPenetrating(true);
                piercingBullet = false;
                skillE.forceDeactivate();
            }
            bullets.add(bullet);
        }
        EventManager.getInstance().triggerEvent(GameEvent.bulletFired(new Point(bulletX, bulletY), true));
    }

    public void takeDamage(int damage) {
        if (dead || damage <= 0 || invulnerable) return;
        if (useComponents && health != null) {
            health.damage(damage);
            lives = (health.getCurrentHealth() + 99) / 100;
        } else {
            lives -= damage;
        }
        EventManager.getInstance().triggerEvent(GameEvent.playerHit(this, damage));
        if (lives <= 0) {
            lives = 0;
            dead = true;
            EventManager.getInstance().triggerEvent(GameEvent.playerDied(this));
        }
    }

    public void addScore(int points) {
        score += points;
        EventManager.getInstance().triggerEvent(GameEvent.playerScored(this, points));
    }

    public int getScore() { return score; }

    public void draw(Graphics g, int mouseX, int mouseY) {
        if (dead) return;

        int centerX = x + tankWidth / 2;
        int centerY = y + tankHeight / 2;

        Color tankColor = selectedTankType.getColor();
        Color detailColor = ConfigManager.TANK_DETAIL_COLOR;
        Color turretColor = ConfigManager.TANK_TURRET_COLOR;

        if (invulnerable) {
            tankColor = new Color(100, 200, 255, 180);
            detailColor = new Color(70, 150, 200, 180);
            turretColor = new Color(150, 220, 255, 180);
        }
        if (inGrass) {
            tankColor = new Color(tankColor.getRed(), tankColor.getGreen(), tankColor.getBlue(), 100);
            detailColor = new Color(detailColor.getRed(), detailColor.getGreen(), detailColor.getBlue(), 100);
            turretColor = new Color(turretColor.getRed(), turretColor.getGreen(), turretColor.getBlue(), 100);
        }

        TankRenderer.drawTankBody(g, x, y, tankColor, detailColor);
        TankRenderer.drawTankTurret(g, centerX, centerY, mouseX, mouseY, turretColor, turretLength);

        g.setColor(Color.WHITE);
        g.fillPolygon(new int[]{centerX, centerX - 3, centerX + 3}, new int[]{y + 2, y + 8, y + 8}, 3);

        if (useComponents && health != null) {
            TankRenderer.drawHealthBar(g, x, y, tankWidth, health.getHealthPercentage(), true);
        }
    }

    // 兼容旧版调用
    public void update(boolean up, boolean down, boolean left, boolean right, boolean shoot,
                       int mouseX, int mouseY, GameWorld world, List<Bullet> bullets) {
    }

    public boolean isInvulnerable() { return invulnerable; }
    public void setInvulnerable(boolean inv) { this.invulnerable = inv; }
    public void setPiercingBullet(boolean pierce) { this.piercingBullet = pierce; }
    public int getCurrentDirection() { return currentDirection; }
    public GameWorld getCurrentWorld() { return currentWorld; }
    public int getSpecialBulletCount() { return specialBulletCount; }
    public void addSpecialBullet(int amount) {
        specialBulletCount = Math.min(MAX_SPECIAL_BULLETS, specialBulletCount + amount);
    }
    public Skill getSkillQ() { return skillQ; }
    public Skill getSkillE() { return skillE; }
    public Skill getSkillR() { return skillR; }
    public Skill getSkillF() { return skillF; }
    // 兼容旧方法名
    public Skill getShieldSkill() { return skillQ; }
    public Skill getPierceSkill() { return skillE; }
    public Skill getBlinkSkill() { return skillR; }
    public Skill getTimeSlowSkill() { return skillF; }
    public int getCurrentHealth() { return useComponents && health != null ? health.getCurrentHealth() : lives * 100; }
    public int getMaxHealth() { return useComponents && health != null ? health.getMaxHealth() : initialLives * 100; }
    public boolean isUsingComponents() { return useComponents; }
    public TransformComponent getTransformComponent() { return transform; }
    public HealthComponent getHealthComponent() { return health; }
    public RenderComponent getRenderComponent() { return render; }
}