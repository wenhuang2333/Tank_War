package thread.ai;

import model.vo.Boss;
import model.vo.Players;
import model.vo.SuperElement;
import model.manager.ElementManager;
import java.util.List;

public class SuperAI implements AIController {
    private long lastDecisionTime;
    private long reactionDelay = 50;
    private long dodgeUntil;

    @Override
    public void decide(Boss boss) {
        long now = System.currentTimeMillis();
        if (now - lastDecisionTime < reactionDelay) return;
        lastDecisionTime = now;

        List<SuperElement> players = ElementManager.getInstance().getElements("players");
        if (players.isEmpty()) return;
        Players enemy = (Players) players.get(0);
        if (!enemy.isVisible()) return;

        double dx = enemy.getX() - boss.getX();
        double dy = enemy.getY() - boss.getY();
        double dist = Math.hypot(dx, dy);

        double predictedAngle = predictAim(boss, enemy, dist);
        boss.setDirection(predictedAngle);

        double optimalDist = 180;
        if (dist > optimalDist * 1.2) {
            boss.setMovingForward(true);
        } else if (dist < optimalDist * 0.6) {
            boss.setMovingBackward(true);
        } else {
            boss.setRotatingCW(true);
            boss.setMovingForward(true);
        }

        if (boss.getAmmo() <= 4 && boss.getAmmo() > 0 && dist > 150) {
            boss.reload();
        } else if (boss.getAmmo() <= 0) {
            boss.reload();
        } else if (dist < 600) {
            boss.fire();
        }
    }

    private double predictAim(Boss boss, Players enemy, double dist) {
        double bulletSpeed = boss.getBulletSpeed() * model.vo.TankData.BULLET_SPEED_FACTOR;
        double enemySpeed = enemy.getSpeed() * model.vo.TankData.TANK_SPEED_FACTOR;

        double ex = enemy.getX();
        double ey = enemy.getY();
        double eRad = Math.toRadians(enemy.getDirection());
        double evx = enemySpeed * Math.sin(eRad);
        double evy = -enemySpeed * Math.cos(eRad);

        double bx = boss.getCenterX();
        double by = boss.getCenterY();

        double predictedX = ex;
        double predictedY = ey;
        for (int i = 0; i < 3; i++) {
            double pdx = predictedX - bx;
            double pdy = predictedY - by;
            double pdist = Math.hypot(pdx, pdy);
            double flightTime = pdist / bulletSpeed;
            predictedX = ex + evx * flightTime;
            predictedY = ey + evy * flightTime;
        }

        double angle = Math.toDegrees(Math.atan2(predictedX - bx, -(predictedY - by)));
        if (angle < 0) angle += 360;
        return angle;
    }
}
