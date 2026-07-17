package thread.ai;

import model.vo.Boss;
import model.vo.Players;
import model.vo.SuperElement;
import model.manager.ElementManager;
import java.util.List;

public class EasyAI implements AIController {
    private long lastDecisionTime;
    private long reactionDelay = 500;
    private double lastDirection = 0;

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
        double angleToEnemy = Math.toDegrees(Math.atan2(dx, -dy));
        if (angleToEnemy < 0) angleToEnemy += 360;

        if (Math.random() < 0.03) {
            lastDirection = Math.random() * 360;
        }

        double dist = Math.hypot(dx, dy);
        if (dist > 200) {
            boss.setMovingForward(true);
        }
        boss.setDirection(lastDirection);

        if (boss.getAmmo() <= 0) {
            boss.reload();
        } else {
            boss.setDirection(angleToEnemy + (Math.random() - 0.5) * 20);
            boss.fire();
        }
    }
}
