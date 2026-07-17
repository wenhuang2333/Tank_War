package model.vo;

import java.awt.Graphics2D;
import java.awt.Color;

public class Explosion extends SuperElement {
    private int frame = 0;
    private static final int MAX_FRAMES = 15;
    private int centerX, centerY;

    public Explosion(int centerX, int centerY) {
        super(centerX - 20, centerY - 20, 40, 40);
        this.centerX = centerX;
        this.centerY = centerY;
    }

    @Override
    public void show(Graphics2D g) {
        if (!visible) return;
        float progress = (float) frame / MAX_FRAMES;
        int r = (int)(30 * progress);
        int alpha = (int)(255 * (1 - progress));
        if (alpha < 0) alpha = 0;

        g.setColor(new Color(255, 200, 50, alpha));
        g.fillOval(centerX - r, centerY - r, r * 2, r * 2);
        g.setColor(new Color(255, 100, 20, alpha / 2));
        g.fillOval(centerX - r / 2, centerY - r / 2, r, r);

        if (frame < MAX_FRAMES / 2) {
            g.setColor(new Color(255, 255, 200, alpha));
            int innerR = r / 3;
            g.fillOval(centerX - innerR, centerY - innerR, innerR * 2, innerR * 2);
        }
    }

    @Override
    public void update() {
        frame++;
        if (frame >= MAX_FRAMES) {
            destroy();
        }
    }

    public boolean isFinished() { return !visible; }
}
