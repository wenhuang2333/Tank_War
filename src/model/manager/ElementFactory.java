package model.manager;

import model.vo.*;
import thread.ai.AIController;
import util.GameConfig;
import java.util.List;

public class ElementFactory {
    private static ElementFactory instance;

    private ElementFactory() {}

    public static ElementFactory getInstance() {
        if (instance == null) {
            synchronized (ElementFactory.class) {
                if (instance == null) instance = new ElementFactory();
            }
        }
        return instance;
    }

    public Players createPlayer(int tankId, int x, int y) {
        TankData data = TankDataManager.getInstance().getTankData(tankId);
        if (data == null) return null;
        Players p = new Players(data);
        p.setX(x);
        p.setY(y);
        return p;
    }

    public Boss createBoss(int tankId, int x, int y, AIController ai) {
        TankData data = TankDataManager.getInstance().getTankData(tankId);
        if (data == null) return null;
        Boss b = new Boss(data, ai);
        b.setX(x);
        b.setY(y);
        return b;
    }

    public Brick createBrick(int x, int y) {
        return new Brick(x, y);
    }

    public Iron createIron(int x, int y) {
        return new Iron(x, y);
    }

    public Explosion createExplosion(int x, int y) {
        return new Explosion(x, y);
    }

    public Bullet createNormalBullet(int x, int y, double direction, Players owner) {
        Bullet b = new Bullet(x, y, (int)direction, owner);
        b.setDamage(owner.getAttack());
        b.setSpeed((int)(owner.getBulletSpeed() * TankData.BULLET_SPEED_FACTOR));
        b.setLifeTime((int)(owner.getBulletDuration() * 1000));
        b.setRebound(true);
        b.setMaxRebounds(0);
        b.setLaser(false);
        ElementManager.getInstance().addElement("bullet", b);
        return b;
    }

    public void createSpreadBullets(int x, int y, double direction, Players owner,
                                     int count, float damageMult, int spreadAngle) {
        int totalSpread = (count - 1) * spreadAngle;
        double startAngle = direction - totalSpread / 2.0;
        for (int i = 0; i < count; i++) {
            double bulletDir = startAngle + i * spreadAngle;
            Bullet b = new Bullet(x, y, (int)((bulletDir % 360 + 360) % 360), owner);
            b.setDamage((int)(owner.getAttack() * damageMult));
            b.setSpeed((int)(owner.getBulletSpeed() * TankData.BULLET_SPEED_FACTOR));
            b.setLifeTime((int)(owner.getBulletDuration() * 1000));
            b.setRebound(false);
            b.setMaxRebounds(0);
            b.setLaser(false);
            ElementManager.getInstance().addElement("bullet", b);
        }
    }

    public void createLaser(int x, int y, int direction, Players owner, boolean rebound, int maxRebounds) {
        Bullet laser = new Bullet(x, y, direction, owner);
        laser.setLaser(true);
        laser.setRebound(rebound);
        laser.setMaxRebounds(maxRebounds);
        laser.setDamage((int)(owner.getAttack() * 0.1));
        double rad = Math.toRadians(direction);
        int endX = x + (int)(GameConfig.MAP_WIDTH * Math.sin(rad));
        int endY = y - (int)(GameConfig.MAP_WIDTH * Math.cos(rad));
        laser.setLaserEndX(endX);
        laser.setLaserEndY(endY);
        laser.setLifeTime(1);
        ElementManager.getInstance().addElement("bullet", laser);
    }
}
