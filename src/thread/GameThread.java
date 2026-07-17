package thread;

import model.vo.SuperElement;
import model.vo.Players;
import model.manager.ElementManager;
import util.CollisionUtil;
import util.GameConfig;
import util.GameContext;
import java.util.List;
import java.util.Map;

public class GameThread extends Thread {
    private volatile boolean running;
    private volatile boolean paused;
    private long startTime;
    private int matchDuration;
    private long remainingTime;
    private long lastPenaltyTime;
    private boolean overtimeTriggered;

    public GameThread(int matchDuration) {
        this.running = true;
        this.paused = false;
        this.matchDuration = matchDuration;
        this.remainingTime = matchDuration * 1000L;
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        while (running) {
            if (!paused) {
                long frameStart = System.currentTimeMillis();

                updateAllElements();
                checkCollisions();
                cleanupDestroyed();
                checkWinCondition();

                long elapsed = System.currentTimeMillis() - startTime;
                remainingTime = matchDuration * 1000L - elapsed;

                long frameElapsed = System.currentTimeMillis() - frameStart;
                if (frameElapsed < GameConfig.FRAME_DURATION) {
                    try {
                        Thread.sleep(GameConfig.FRAME_DURATION - frameElapsed);
                    } catch (InterruptedException ignored) {}
                }
            } else {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private void updateAllElements() {
        ElementManager em = ElementManager.getInstance();
        Map<String, List<SuperElement>> all = em.getAllElements();
        for (List<SuperElement> list : all.values()) {
            for (SuperElement e : list) {
                if (e.isVisible()) e.update();
            }
        }
    }

    private void checkCollisions() {
        CollisionUtil.checkTankWallCollisions();
        CollisionUtil.checkBulletWallCollisions();
        CollisionUtil.checkBulletTankCollisions();
    }

    private void cleanupDestroyed() {
        ElementManager em = ElementManager.getInstance();
        for (List<SuperElement> list : em.getAllElements().values()) {
            list.removeIf(e -> !e.isVisible());
        }
    }

    private void checkWinCondition() {
        ElementManager em = ElementManager.getInstance();
        List<SuperElement> players = em.getElements("players");
        List<SuperElement> bosses = em.getElements("boss");

        if ("pvp".equals(GameContext.battleMode)) {
            checkPvpWinCondition(players, em);
            return;
        }

        boolean playerAlive = players.stream().anyMatch(SuperElement::isVisible);
        boolean bossAlive = bosses.stream().anyMatch(SuperElement::isVisible);

        if (remainingTime <= 0) {
            running = false;
            GameContext.battleEnded = true;
            Players p = playerAlive && !players.isEmpty() ? (Players) players.get(0) : null;
            Players b = bossAlive && !bosses.isEmpty() ? (Players) bosses.get(0) : null;
            if (p != null && b != null) {
                GameContext.playerWin = p.getHp() > b.getHp();
            } else {
                GameContext.playerWin = playerAlive;
            }
            return;
        }

        if (!playerAlive) {
            running = false;
            GameContext.battleEnded = true;
            GameContext.playerWin = false;
        }
        if (!bossAlive) {
            running = false;
            GameContext.battleEnded = true;
            GameContext.playerWin = true;
        }
    }

    private void checkPvpWinCondition(List<SuperElement> playerList, ElementManager em) {
        Players p1 = null, p2 = null;
        for (SuperElement e : playerList) {
            if (e instanceof Players) {
                if (p1 == null) p1 = (Players) e;
                else { p2 = (Players) e; break; }
            }
        }
        if (p1 == null || p2 == null) return;

        boolean p1Alive = p1.isVisible();
        boolean p2Alive = p2.isVisible();

        if (remainingTime <= 0) {
            // Overtime mode: clear obstacles, reset 5-min timer once
            if (GameContext.overtimeMode && !overtimeTriggered && p1Alive && p2Alive) {
                clearObstaclesForOvertime(em);
                matchDuration = 300;
                startTime = System.currentTimeMillis();
                remainingTime = 300 * 1000L;
                overtimeTriggered = true;
                return;
            }

            // Timeout penalty: periodic HP deduction
            if (GameContext.overtimePenalty && p1Alive && p2Alive) {
                long now = System.currentTimeMillis();
                if (lastPenaltyTime == 0) lastPenaltyTime = now;
                if (now - lastPenaltyTime >= 1000) {
                    lastPenaltyTime = now;
                    int dmg = 20;
                    p1.setHp(Math.max(0, p1.getHp() - dmg));
                    p2.setHp(Math.max(0, p2.getHp() - dmg));
                    p1.setDamageReceived(p1.getDamageReceived() + dmg);
                    p2.setDamageReceived(p2.getDamageReceived() + dmg);
                    if (p1.getHp() <= 0) { p1.setVisible(false); p1Alive = false; }
                    if (p2.getHp() <= 0) { p2.setVisible(false); p2Alive = false; }
                } else {
                    return;
                }
            }
            if (GameContext.overtimePenalty && p1Alive && p2Alive) return;

            running = false;
            GameContext.battleEnded = true;
            if (p1Alive && p2Alive) {
                GameContext.playerWin = p1.getHp() > p2.getHp();
            } else {
                GameContext.playerWin = p1Alive;
            }
            return;
        }

        if (!p1Alive) {
            running = false;
            GameContext.battleEnded = true;
            GameContext.playerWin = false;
        }
        if (!p2Alive) {
            running = false;
            GameContext.battleEnded = true;
            GameContext.playerWin = true;
        }
    }

    private void clearObstaclesForOvertime(ElementManager em) {
        List<SuperElement> bricks = em.getElements("brick");
        if (bricks != null) bricks.clear();
        List<SuperElement> iron = em.getElements("iron");
        if (iron != null) iron.clear();
    }

    public void pauseGame() { paused = true; }
    public void resumeGame() { paused = false; startTime = System.currentTimeMillis() - (matchDuration * 1000L - remainingTime); }
    public void stopGame() { running = false; }
    public boolean isRunning() { return running; }
    public boolean isPaused() { return paused; }
    public long getRemainingTime() { return remainingTime; }
}
