package thread;

import model.vo.Players;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;
import java.util.HashSet;

public class GameKeyListener implements KeyListener {
    private Set<Integer> pressedKeys = new HashSet<>();
    private Players player1;
    private Players player2;
    private GameThread gameThread;
    private Runnable onPause;

    public GameKeyListener(Players p1, Players p2, GameThread gameThread, Runnable onPause) {
        this.player1 = p1;
        this.player2 = p2;
        this.gameThread = gameThread;
        this.onPause = onPause;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());
        updatePlayerState();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
        updatePlayerState();
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && onPause != null) {
            onPause.run();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private void updatePlayerState() {
        if (player1 == null) return;

        player1.setMovingForward(pressedKeys.contains(KeyEvent.VK_W));
        player1.setMovingBackward(pressedKeys.contains(KeyEvent.VK_S));
        player1.setRotatingCCW(pressedKeys.contains(KeyEvent.VK_A));
        player1.setRotatingCW(pressedKeys.contains(KeyEvent.VK_D));

        if (pressedKeys.contains(KeyEvent.VK_SPACE)) {
            player1.fire();
        }
        if (pressedKeys.contains(KeyEvent.VK_R)) {
            player1.reload();
        }

        if (player2 != null) {
            player2.setMovingForward(pressedKeys.contains(KeyEvent.VK_UP));
            player2.setMovingBackward(pressedKeys.contains(KeyEvent.VK_DOWN));
            player2.setRotatingCCW(pressedKeys.contains(KeyEvent.VK_LEFT));
            player2.setRotatingCW(pressedKeys.contains(KeyEvent.VK_RIGHT));

            if (pressedKeys.contains(KeyEvent.VK_ENTER)) {
                player2.fire();
            }
            if (pressedKeys.contains(KeyEvent.VK_BACK_SPACE)) {
                player2.reload();
            }
        }
    }

    public void clearKeys() {
        pressedKeys.clear();
        if (player1 != null) {
            player1.setMovingForward(false);
            player1.setMovingBackward(false);
            player1.setRotatingCW(false);
            player1.setRotatingCCW(false);
        }
        if (player2 != null) {
            player2.setMovingForward(false);
            player2.setMovingBackward(false);
            player2.setRotatingCW(false);
            player2.setRotatingCCW(false);
        }
    }
}
