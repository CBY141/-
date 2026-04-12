package main.java.com.tankbattle.core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * 渲染组件 - 管理实体的渲染
 */
public class RenderComponent implements Component {
    private Entity entity;
    private Color color = Color.WHITE;
    private boolean visible = true;
    private int renderOrder = 0;

    // 透明度
    private float alpha = 1.0f;

    // 闪烁效果
    private boolean flashing = false;
    private int flashTimer = 0;
    private int flashDuration = 0;
    private Color flashColor = Color.WHITE;

    // 缩放
    private float renderScaleX = 1.0f;
    private float renderScaleY = 1.0f;

    // 旋转
    private boolean useRotation = false;
    private float renderRotation = 0.0f;

    // ========== 必须实现的 Component 接口方法 ==========

    @Override
    public void initialize(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void update(float deltaTime) {
        // 更新闪烁效果
        if (flashing) {
            flashTimer++;
            if (flashTimer >= flashDuration) {
                flashing = false;
                flashTimer = 0;
            }
        }
    }

    @Override
    public void dispose() {
        // 清理资源
        this.entity = null;
        this.color = null;
        this.flashColor = null;
    }

    // ========== 公共方法 ==========

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getRenderOrder() {
        return renderOrder;
    }

    public void setRenderOrder(int renderOrder) {
        this.renderOrder = renderOrder;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = Math.max(0.0f, Math.min(1.0f, alpha));
    }

    public float getRenderScaleX() {
        return renderScaleX;
    }

    public void setRenderScaleX(float renderScaleX) {
        this.renderScaleX = renderScaleX;
    }

    public float getRenderScaleY() {
        return renderScaleY;
    }

    public void setRenderScaleY(float renderScaleY) {
        this.renderScaleY = renderScaleY;
    }

    public void setRenderScale(float scaleX, float scaleY) {
        this.renderScaleX = scaleX;
        this.renderScaleY = scaleY;
    }

    public float getRenderRotation() {
        return renderRotation;
    }

    public void setRenderRotation(float renderRotation) {
        this.renderRotation = renderRotation;
        this.useRotation = (renderRotation != 0.0f);
    }

    public void flash(Color color, int duration) {
        this.flashing = true;
        this.flashTimer = 0;
        this.flashDuration = duration;
        this.flashColor = color;
    }

    public void stopFlash() {
        this.flashing = false;
        this.flashTimer = 0;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    /**
     * 渲染实体
     */
    public void render(Graphics g) {
        if (!visible || entity == null) return;

        TransformComponent transform = entity.getComponent(TransformComponent.class);
        if (transform == null) return;

        int x = transform.getX();
        int y = transform.getY();
        int width = transform.getWidth();
        int height = transform.getHeight();

        // 将Graphics转换为Graphics2D以便进行更高级的渲染
        Graphics2D g2d = (Graphics2D) g;

        // 保存原始变换状态
        AffineTransform originalTransform = g2d.getTransform();

        // 应用变换：旋转和缩放
        if (useRotation || renderScaleX != 1.0f || renderScaleY != 1.0f) {
            // 计算中心点
            int centerX = x + width / 2;
            int centerY = y + height / 2;

            // 创建新的变换
            AffineTransform transformMatrix = new AffineTransform();

            // 平移至中心点
            transformMatrix.translate(centerX, centerY);

            // 应用旋转
            if (useRotation) {
                transformMatrix.rotate(renderRotation);
            }

            // 应用缩放
            transformMatrix.scale(renderScaleX, renderScaleY);

            // 平移回原点
            transformMatrix.translate(-centerX, -centerY);

            // 应用变换
            g2d.setTransform(transformMatrix);
        }

        // 计算渲染颜色
        Color renderColor = calculateRenderColor();

        // 设置颜色
        g.setColor(renderColor);

        // 绘制实体
        g.fillRect(x, y, width, height);

        // 绘制边框
        g.setColor(renderColor.darker());
        g.drawRect(x, y, width, height);

        // 恢复原始变换状态
        g2d.setTransform(originalTransform);
    }

    /**
     * 计算渲染颜色（考虑闪烁和透明度）
     */
    private Color calculateRenderColor() {
        Color resultColor = color;

        if (flashing) {
            // 计算闪烁进度
            float flashProgress = (float) flashTimer / flashDuration;
            float flashValue = (float) (0.5 + 0.5 * Math.sin(flashProgress * Math.PI * 4));

            // 混合原始颜色和闪烁颜色
            int r = (int)(color.getRed() + (flashColor.getRed() - color.getRed()) * flashValue);
            int g = (int)(color.getGreen() + (flashColor.getGreen() - color.getGreen()) * flashValue);
            int b = (int)(color.getBlue() + (flashColor.getBlue() - color.getBlue()) * flashValue);

            // 确保颜色值在有效范围内
            r = Math.max(0, Math.min(255, r));
            g = Math.max(0, Math.min(255, g));
            b = Math.max(0, Math.min(255, b));

            resultColor = new Color(r, g, b, (int)(alpha * 255));
        } else if (alpha < 1.0f) {
            // 应用透明度
            int alphaValue = (int)(alpha * 255);
            resultColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alphaValue);
        }

        return resultColor;
    }

    /**
     * 简单渲染（无变换）
     */
    public void renderSimple(Graphics g) {
        if (!visible || entity == null) return;

        TransformComponent transform = entity.getComponent(TransformComponent.class);
        if (transform == null) return;

        int x = transform.getX();
        int y = transform.getY();
        int width = transform.getWidth();
        int height = transform.getHeight();

        // 计算颜色
        Color renderColor = color;
        if (alpha < 1.0f) {
            int alphaValue = (int)(alpha * 255);
            renderColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alphaValue);
        }

        g.setColor(renderColor);
        g.fillRect(x, y, width, height);

        g.setColor(renderColor.darker());
        g.drawRect(x, y, width, height);
    }

    /**
     * 渲染为圆形
     */
    public void renderAsCircle(Graphics g) {
        if (!visible || entity == null) return;

        TransformComponent transform = entity.getComponent(TransformComponent.class);
        if (transform == null) return;

        int x = transform.getX();
        int y = transform.getY();
        int width = transform.getWidth();
        int height = transform.getHeight();

        // 使用最小边长作为直径
        int diameter = Math.min(width, height);
        int circleX = x + (width - diameter) / 2;
        int circleY = y + (height - diameter) / 2;

        // 计算颜色
        Color renderColor = color;
        if (alpha < 1.0f) {
            int alphaValue = (int)(alpha * 255);
            renderColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alphaValue);
        }

        g.setColor(renderColor);
        g.fillOval(circleX, circleY, diameter, diameter);

        g.setColor(renderColor.darker());
        g.drawOval(circleX, circleY, diameter, diameter);
    }

    /**
     * 渲染为带边框的矩形
     */
    public void renderWithBorder(Graphics g, Color borderColor, int borderThickness) {
        if (!visible || entity == null) return;

        TransformComponent transform = entity.getComponent(TransformComponent.class);
        if (transform == null) return;

        int x = transform.getX();
        int y = transform.getY();
        int width = transform.getWidth();
        int height = transform.getHeight();

        // 计算颜色
        Color renderColor = color;
        if (alpha < 1.0f) {
            int alphaValue = (int)(alpha * 255);
            renderColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alphaValue);
        }

        // 绘制边框
        g.setColor(borderColor);
        for (int i = 0; i < borderThickness; i++) {
            g.drawRect(x - i, y - i, width + 2 * i, height + 2 * i);
        }

        // 绘制内部
        g.setColor(renderColor);
        g.fillRect(x, y, width, height);
    }

    /**
     * 渲染为带圆角的矩形
     */
    public void renderWithRoundedCorners(Graphics g, int arcWidth, int arcHeight) {
        if (!visible || entity == null) return;

        TransformComponent transform = entity.getComponent(TransformComponent.class);
        if (transform == null) return;

        int x = transform.getX();
        int y = transform.getY();
        int width = transform.getWidth();
        int height = transform.getHeight();

        // 计算颜色
        Color renderColor = color;
        if (alpha < 1.0f) {
            int alphaValue = (int)(alpha * 255);
            renderColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alphaValue);
        }

        g.setColor(renderColor);
        g.fillRoundRect(x, y, width, height, arcWidth, arcHeight);

        g.setColor(renderColor.darker());
        g.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    /**
     * 获取渲染组件的状态摘要
     */
    public String getStatusSummary() {
        return String.format("RenderComponent[visible=%s, color=%s, alpha=%.2f, order=%d, scale=(%.2f,%.2f), rotation=%.2f]",
                visible,
                color.toString(),
                alpha,
                renderOrder,
                renderScaleX,
                renderScaleY,
                renderRotation
        );
    }

    @Override
    public String toString() {
        return getStatusSummary();
    }
}