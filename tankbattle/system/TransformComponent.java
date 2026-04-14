package main.java.com.tankbattle.system;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * 变换组件 - 管理实体的位置、旋转、缩放
 */
public class TransformComponent implements Component {
    private Entity entity;

    // 位置
    private int x = 0;
    private int y = 0;

    // 旋转角度（弧度）
    private float rotation = 0;

    // 缩放
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;

    // 大小
    private int width = 0;
    private int height = 0;

    // ========== 必须实现的 Component 接口方法 ==========

    @Override
    public void initialize(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void update(float deltaTime) {
        // 可以在这里添加每帧更新的变换逻辑
    }

    @Override
    public void dispose() {
        // 清理资源
        this.entity = null;
    }

    // ========== 公共方法 ==========

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point getPosition() {
        return new Point(x, y);
    }

    public void translate(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public void rotate(float angle) {
        this.rotation += angle;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Point getCenter() {
        return new Point(x + width / 2, y + height / 2);
    }

    public boolean contains(Point point) {
        return point.x >= x && point.x <= x + width &&
                point.y >= y && point.y <= y + height;
    }

    public boolean intersects(TransformComponent other) {
        if (other == null) return false;
        return x < other.x + other.width &&
                x + width > other.x &&
                y < other.y + other.height &&
                y + height > other.y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        return String.format("TransformComponent{x=%d, y=%d, w=%d, h=%d}", x, y, width, height);
    }
}