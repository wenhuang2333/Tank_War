package model.vo;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import model.load.ElementLoad;
import util.GameConfig;
import util.ResourceManager;

public class Bullet extends SuperElement {
    private int speed;
    private int damage;
    private Players owner;
    private long spawnTime;
    private int lifeTime;
    private boolean isLaser;
    private boolean rebound;
    private int maxRebounds;
    private int reboundCount;
    private int laserEndX, laserEndY;
    private int laserHitCount;

    public Bullet(int x, int y, int direction, Players owner) {
        super(x, y, GameConfig.BULLET_SIZE, GameConfig.BULLET_SIZE);
        this.direction = direction;
        this.owner = owner;
        this.spawnTime = System.currentTimeMillis();
        this.reboundCount = 0;
        this.laserHitCount = 0;
    }

    @Override
    public void show(Graphics2D g) {
        if (!visible) return;
        if (isLaser) {
            BufferedImage beamImg = ElementLoad.getInstance().getImage(ResourceManager.LASER_BEAM);
            if (beamImg != null && beamImg.getWidth() > 6) {
                double rad = Math.toRadians(direction);
                int dx = laserEndX - x;
                int dy = laserEndY - y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                java.awt.geom.AffineTransform old = g.getTransform();
                g.translate(x, y);
                g.rotate(rad);
                int segW = 6;
                for (int i = 0; i < (int)dist; i += segW) {
                    int segH = Math.min(segW, (int)dist - i);
                    g.drawImage(beamImg, i, -3, segH, 6, null);
                }
                g.setTransform(old);
            } else {
                g.setColor(new Color(255, 50, 50, 200));
                g.setStroke(new BasicStroke(3));
                g.drawLine(x, y, laserEndX, laserEndY);
                g.setColor(new Color(255, 200, 100, 100));
                g.setStroke(new BasicStroke(7));
                g.drawLine(x, y, laserEndX, laserEndY);
            }
        } else {
            BufferedImage bulletImg = ElementLoad.getInstance().getImage(ResourceManager.BULLET_ALL);
            if (bulletImg != null && bulletImg.getWidth() > 8) {
                g.drawImage(bulletImg, x - width / 2, y - height / 2, width, height, null);
            } else {
                g.setColor(new Color(255, 255, 100));
                g.fillOval(x - width / 2, y - height / 2, width, height);
                g.setColor(new Color(255, 200, 50));
                g.fillOval(x - width / 4, y - height / 4, width / 2, height / 2);
            }
        }
    }

    @Override
    public void update() {
        if (isLaser) {
            destroy();
            return;
        }

        long now = System.currentTimeMillis();
        if (now - spawnTime > lifeTime) {
            destroy();
            return;
        }

        double rad = Math.toRadians(direction);
        int dx = (int)(speed * Math.sin(rad));
        int dy = -(int)(speed * Math.cos(rad));
        x += dx;
        y += dy;

        if (x < GameConfig.MAP_OFFSET_X || x > GameConfig.MAP_OFFSET_X + GameConfig.MAP_WIDTH
            || y < GameConfig.MAP_OFFSET_Y || y > GameConfig.MAP_OFFSET_Y + GameConfig.MAP_HEIGHT) {
            if (rebound) {
                rebound();
            } else {
                destroy();
            }
        }
    }

    public void rebound() {
        if (maxRebounds > 0 && reboundCount >= maxRebounds) {
            destroy();
            return;
        }
        reboundCount++;
        direction = (direction + 180) % 360;
    }

    // Getters and setters
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
    public Players getOwner() { return owner; }
    public void setOwner(Players owner) { this.owner = owner; }
    public long getSpawnTime() { return spawnTime; }
    public int getLifeTime() { return lifeTime; }
    public void setLifeTime(int lifeTime) { this.lifeTime = lifeTime; }
    public boolean isLaser() { return isLaser; }
    public void setLaser(boolean laser) { isLaser = laser; }
    public boolean isRebound() { return rebound; }
    public void setRebound(boolean rebound) { this.rebound = rebound; }
    public int getMaxRebounds() { return maxRebounds; }
    public void setMaxRebounds(int max) { this.maxRebounds = max; }
    public int getReboundCount() { return reboundCount; }
    public void setReboundCount(int count) { this.reboundCount = count; }
    public boolean canRebound() { return rebound; }
    public int getLaserEndX() { return laserEndX; }
    public void setLaserEndX(int x) { this.laserEndX = x; }
    public int getLaserEndY() { return laserEndY; }
    public void setLaserEndY(int y) { this.laserEndY = y; }
    public int getLaserHitCount() { return laserHitCount; }
    public void setLaserHitCount(int c) { this.laserHitCount = c; }
}
