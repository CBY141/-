import java.awt.*;

public class Explosion {
    public int x, y;
    public int size = 5;
    private int maxSize = 25;
    public boolean alive = true;

    public Explosion(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        size += 2;
        if (size > maxSize) {
            alive = false;
        }
    }

    public void draw(Graphics g) {
        if (!alive) return;
        g.setColor(new Color(255, 200, 0, 200));
        g.fillOval(x - size/2, y - size/2, size, size);
        g.setColor(new Color(255, 100, 0, 100));
        g.fillOval(x - size, y - size, size*2, size*2);
        g.setColor(Color.YELLOW);
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            int px = x + (int)(size * Math.cos(angle));
            int py = y + (int)(size * Math.sin(angle));
            g.fillOval(px - 2, py - 2, 4, 4);
        }
    }
}