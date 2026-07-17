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

    public void pauseGame() { paused = true; }
    public void resumeGame() { paused = false; startTime = System.currentTimeMillis() - (matchDuration * 1000L - remainingTime); }
    public void stopGame() { running = false; }
    public boolean isRunning() { return running; }
    public boolean isPaused() { return paused; }
    public long getRemainingTime() { return remainingTime; }
}
