package thread.ai;

import model.vo.Boss;
import model.vo.Players;
import model.vo.SuperElement;
import model.manager.ElementManager;
import java.util.List;
import java.util.Random;

public class HardAI implements AIController {
    private long lastDecisionTime;
    private long reactionDelay = 200;
    private long dodgeUntil;
    private Random rand = new Random();

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
        double angleToEnemy = Math.toDegrees(Math.atan2(dx, -dy));
        if (angleToEnemy < 0) angleToEnemy += 360;

        int enemyPrevAmmo = enemy.getAmmo();
        if (ammoDecreased && now < dodgeUntil) {
            ammoDecreased = false;
        }

        if (now < dodgeUntil) {
            boss.setMovingForward(true);
            boss.fire();
            return;
        }

        boss.setDirection(angleToEnemy + (rand.nextDouble() - 0.5) * 6);

        double optimalDist = 200;
        if (dist > optimalDist * 1.3) {
            boss.setMovingForward(true);
        } else if (dist < optimalDist * 0.7) {
            boss.setMovingBackward(true);
        } else {
            if (rand.nextBoolean()) {
                boss.setRotatingCW(true);
            } else {
                boss.setRotatingCCW(true);
            }
            boss.setMovingForward(true);
        }

        if (boss.getAmmo() <= 0) {
            boss.reload();
        } else {
            double angleDiff = Math.abs(((angleToEnemy - boss.getDirection()) % 360 + 540) % 360 - 180);
            if (angleDiff < 20) {
                boss.fire();
            }
        }
    }

    private boolean ammoDecreased;

    public void onEnemyFired() {
        ammoDecreased = true;
        dodgeUntil = System.currentTimeMillis() + 300;
    }
}
