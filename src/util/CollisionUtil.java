package util;

import java.util.List;
import model.vo.SuperElement;
import model.vo.Players;
import model.vo.Bullet;
import model.vo.Brick;
import model.vo.Iron;
import model.vo.Explosion;
import model.vo.Modification;
import model.manager.ElementManager;
import java.util.ArrayList;

public class CollisionUtil {

    public static boolean wouldCollideWithWall(int newX, int newY, int width, int height, SuperElement self) {
        ElementManager em = ElementManager.getInstance();
        java.awt.Rectangle newRect = new java.awt.Rectangle(newX, newY, width, height);

        for (SuperElement e : em.getElements("brick")) {
            if (e != self && e.isVisible() && newRect.intersects(e.getRect())) return true;
        }
        for (SuperElement e : em.getElements("iron")) {
            if (e != self && e.isVisible() && newRect.intersects(e.getRect())) return true;
        }
        if (newX < GameConfig.MAP_OFFSET_X || newY < GameConfig.MAP_OFFSET_Y
            || newX + width > GameConfig.MAP_OFFSET_X + GameConfig.MAP_WIDTH
            || newY + height > GameConfig.MAP_OFFSET_Y + GameConfig.MAP_HEIGHT) {
            return true;
        }
        return false;
    }

    public static void checkBulletWallCollisions() {
        ElementManager em = ElementManager.getInstance();
        List<SuperElement> bullets = new ArrayList<>(em.getElements("bullet"));
        List<SuperElement> bricks = em.getElements("brick");
        List<SuperElement> irons = em.getElements("iron");

        for (SuperElement be : bullets) {
            Bullet b = (Bullet) be;
            if (!b.isVisible()) continue;

            java.awt.Rectangle bulletRect = b.getRect();
            boolean hitBrick = false;

            for (SuperElement br : bricks) {
                if (br.isVisible() && bulletRect.intersects(br.getRect())) {
                    if (b.isLaser()) {
                        br.destroy();
                    } else {
                        b.destroy();
                        br.destroy();
                        em.addElement("explosion", new Explosion(br.getCenterX(), br.getCenterY()));
                        hitBrick = true;
                        break;
                    }
                }
            }
            if (hitBrick) continue;

            for (SuperElement ir : irons) {
                if (ir.isVisible() && bulletRect.intersects(ir.getRect())) {
                    if (b.isLaser()) {
                        if (b.canRebound() && b.getReboundCount() < b.getMaxRebounds()) {
                            b.rebound();
                        } else {
                            b.destroy();
                        }
                    } else if (b.isRebound()) {
                        b.rebound();
                    } else {
                        b.destroy();
                    }
                    break;
                }
            }
        }
    }

    public static void checkBulletTankCollisions() {
        ElementManager em = ElementManager.getInstance();
        List<SuperElement> bullets = new ArrayList<>(em.getElements("bullet"));
        List<SuperElement> players = em.getElements("players");
        List<SuperElement> bosses = em.getElements("boss");

        for (SuperElement be : bullets) {
            Bullet b = (Bullet) be;
            if (!b.isVisible()) continue;

            for (SuperElement pe : players) {
                Players p = (Players) pe;
                if (!p.isVisible() || p == b.getOwner()) continue;
                if (b.isLaser()) {
                    if (laserIntersectsTank(b, p)) {
                        handleBulletHit(b, p);
                        break;
                    }
                } else if (b.isStrike(p)) {
                    handleBulletHit(b, p);
                    break;
                }
            }
            if (!b.isVisible()) continue;

            for (SuperElement be2 : bosses) {
                Players boss = (Players) be2;
                if (!boss.isVisible() || boss == b.getOwner()) continue;
                if (b.isLaser()) {
                    if (laserIntersectsTank(b, boss)) {
                        handleBulletHit(b, boss);
                        break;
                    }
                } else if (b.isStrike(boss)) {
                    handleBulletHit(b, boss);
                    break;
                }
            }
        }
    }

    private static void handleBulletHit(Bullet b, Players tank) {
        boolean friendlyFire = (b.getOwner() instanceof model.vo.Boss) == (tank instanceof model.vo.Boss)
            || (!(b.getOwner() instanceof model.vo.Boss) && !(tank instanceof model.vo.Boss));
        if (friendlyFire && (b.getOwner() == null || b.getOwner().hasMod(Modification.Type.ANTI_FRIENDLY_FIRE))) {
            return;
        }
        tank.takeDamage(b.getDamage());
        if (b.isLaser()) {
            b.setLaserHitCount(b.getLaserHitCount() + 1);
        } else {
            b.destroy();
        }
    }

    private static boolean laserIntersectsTank(Bullet laser, Players tank) {
        if (laser.getLaserHitCount() > 0) return false;
        java.awt.Rectangle tankRect = tank.getRect();
        int lx = laser.getX(), ly = laser.getY();
        int lex = laser.getLaserEndX(), ley = laser.getLaserEndY();
        return tankRect.intersects(
            Math.min(lx, lex), Math.min(ly, ley),
            Math.abs(lex - lx) + 10, Math.abs(ley - ly) + 10);
    }

    public static void checkTankWallCollisions() {
    }
}
