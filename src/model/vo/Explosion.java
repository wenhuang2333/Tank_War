package model.vo;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;
import model.load.ElementLoad;
import util.ResourceManager;

public class Explosion extends SuperElement {
    private int frame = 0;
    private static final int MAX_FRAMES = 16;
    private int centerX, centerY;
    private BufferedImage[] frames;

    public Explosion(int centerX, int centerY) {
        super(centerX - 28, centerY - 28, 56, 56);
        this.centerX = centerX;
        this.centerY = centerY;
        ElementLoad el = ElementLoad.getInstance();
        String[] paths = ResourceManager.explosionFrames();
        frames = new BufferedImage[paths.length];
        for (int i = 0; i < paths.length; i++) {
            frames[i] = el.getImage(paths[i]);
        }
    }

    @Override
    public void show(Graphics2D g) {
        if (!visible) return;
        int idx = frame / 2;
        if (idx >= 0 && idx < frames.length && frames[idx] != null
            && frames[idx].getWidth() > 40) {
            int size = 40 + frame * 3;
            g.drawImage(frames[idx], centerX - size / 2, centerY - size / 2, size, size, null);
        } else {
            float progress = (float) frame / MAX_FRAMES;
            int r = (int)(30 * progress);
            int alpha = (int)(255 * (1 - progress));
            if (alpha < 0) alpha = 0;
            g.setColor(new Color(255, 200, 50, alpha));
            g.fillOval(centerX - r, centerY - r, r * 2, r * 2);
            g.setColor(new Color(255, 100, 20, alpha / 2));
            g.fillOval(centerX - r / 2, centerY - r / 2, r, r);
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
