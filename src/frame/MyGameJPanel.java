package frame;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import model.vo.*;
import model.manager.ElementManager;
import model.manager.ElementFactory;
import model.manager.SaveManager;
import model.load.ElementLoad;
import model.load.MapData;
import thread.GameThread;
import thread.GameKeyListener;
import thread.ai.EasyAI;
import thread.ai.HardAI;
import thread.ai.SuperAI;
import util.GameConfig;
import util.GameContext;
import java.awt.Point;

public class MyGameJPanel extends JPanel {
    private Timer renderTimer;
    private Players player1, player2;
    private int mapId;
    private String difficulty;
    private GameThread gameThread;
    private GameKeyListener keyListener;
    private PauseOverlay pauseOverlay;
    private boolean paused = false;
    private long remainingTime;
    private boolean battleFinished = false;
    private MyJFrame frame;

    public MyGameJPanel(MyJFrame frame, Players p1, Players p2, int mapId, String difficulty) {
        this.frame = frame;
        this.player1 = p1;
        this.player2 = p2;
        this.mapId = mapId;
        this.difficulty = difficulty;
        setPreferredSize(new java.awt.Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (paused && pauseOverlay != null) {
                    pauseOverlay.handleClick(e.getX(), e.getY());
                }
                if (battleFinished) {
                    returnToMenu();
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (battleFinished) {
                    returnToMenu();
                }
            }
        });
    }

    public void startGame(int duration) {
        initBattle();
        gameThread = new GameThread(duration);
        keyListener = new GameKeyListener(player1, player2 instanceof Boss ? null : player2,
            gameThread, this::togglePause);
        addKeyListener(keyListener);
        gameThread.start();
        startRenderThread();
    }

    private void initBattle() {
        ElementManager em = ElementManager.getInstance();
        em.clearAll();

        em.addElement("background", new Background(
            GameConfig.MAP_OFFSET_X, GameConfig.MAP_OFFSET_Y,
            GameConfig.MAP_WIDTH, GameConfig.MAP_HEIGHT));

        MapData mapData = ElementLoad.getInstance().loadMap(mapId);
        ElementFactory factory = ElementFactory.getInstance();
        if (mapData != null) {
            for (Point p : mapData.getBrickPositions()) {
                em.addElement("brick", factory.createBrick(p.x, p.y));
            }
            for (Point p : mapData.getIronPositions()) {
                em.addElement("iron", factory.createIron(p.x, p.y));
            }
        }

        for (int x = GameConfig.MAP_OFFSET_X; x < GameConfig.MAP_OFFSET_X + GameConfig.MAP_WIDTH; x += GameConfig.TILE_SIZE) {
            em.addElement("iron", factory.createIron(x, GameConfig.MAP_OFFSET_Y));
            em.addElement("iron", factory.createIron(x, GameConfig.MAP_OFFSET_Y + GameConfig.MAP_HEIGHT - GameConfig.TILE_SIZE));
        }
        for (int y = GameConfig.MAP_OFFSET_Y; y < GameConfig.MAP_OFFSET_Y + GameConfig.MAP_HEIGHT; y += GameConfig.TILE_SIZE) {
            em.addElement("iron", factory.createIron(GameConfig.MAP_OFFSET_X, y));
            em.addElement("iron", factory.createIron(GameConfig.MAP_OFFSET_X + GameConfig.MAP_WIDTH - GameConfig.TILE_SIZE, y));
        }

        player1.setX(GameConfig.P1_SPAWN_X);
        player1.setY(GameConfig.P1_SPAWN_Y);
        player1.setDirection(90);
        player1.resetBattleState();
        player1.setVisible(true);
        em.addElement("players", player1);

        if (player2 instanceof Boss) {
            Boss boss = (Boss) player2;
            boss.setX(GameConfig.P2_SPAWN_X);
            boss.setY(GameConfig.P2_SPAWN_Y);
            boss.setDirection(270);
            boss.resetBattleState();
            boss.setVisible(true);
            switch (difficulty) {
                case "hard": boss.setAi(new HardAI()); break;
                case "super": boss.setAi(new SuperAI()); break;
                default: boss.setAi(new EasyAI()); break;
            }
            em.addElement("boss", boss);
        } else if (player2 != null) {
            player2.setX(GameConfig.P2_SPAWN_X);
            player2.setY(GameConfig.P2_SPAWN_Y);
            player2.setDirection(270);
            player2.resetBattleState();
            player2.setVisible(true);
            em.addElement("players", player2);
        }
    }

    private void startRenderThread() {
        renderTimer = new Timer(GameConfig.FRAME_DURATION, e -> {
            if (gameThread != null && !gameThread.isRunning() && !battleFinished) {
                battleFinished = true;
                onBattleEnd();
            }
            repaint();
        });
        renderTimer.start();
    }

    private void onBattleEnd() {
        renderTimer.stop();
        giveRewards();
        SaveManager.getInstance().save(GameContext.currentSave);
    }

    private void giveRewards() {
        if (GameContext.currentSave == null) return;
        PlayerSaveData.Resources r = GameContext.currentSave.getResources();
        if (GameContext.playerWin) {
            switch (difficulty) {
                case "easy": r.setIron(r.getIron() + 200); r.setSteel(r.getSteel() + 30); break;
                case "hard": r.setIron(r.getIron() + 400); r.setSteel(r.getSteel() + 60); break;
                case "super": r.setIron(r.getIron() + 500); r.setSteel(r.getSteel() + 100); break;
            }
        }
        int blueprintReward = difficulty.equals("hard") ? 2 : difficulty.equals("super") ? 3 : 1;
        r.setBlueprints(r.getBlueprints() + blueprintReward);

        if (GameContext.currentSave.getBattleHistory() != null) {
            PlayerSaveData.BattleHistory bh = GameContext.currentSave.getBattleHistory();
            bh.setTotalPlayed(bh.getTotalPlayed() + 1);
            if (GameContext.playerWin) bh.setTotalWins(bh.getTotalWins() + 1);
            else bh.setTotalLosses(bh.getTotalLosses() + 1);
        }
    }

    private void returnToMenu() {
        cleanup();
        GameContext.isInBattle = false;
        frame.showBattleResult();
    }

    private void cleanup() {
        if (renderTimer != null) renderTimer.stop();
        if (gameThread != null) gameThread.stopGame();
        ElementManager.getInstance().clearAll();
    }

    private void togglePause() {
        if (gameThread == null || battleFinished) return;
        paused = !paused;
        if (paused) {
            gameThread.pauseGame();
            pauseOverlay = new PauseOverlay(this, gameThread);
        } else {
            gameThread.resumeGame();
            pauseOverlay = null;
            requestFocusInWindow();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawElements(g2d, "background");
        drawElements(g2d, "iron");
        drawElements(g2d, "brick");
        drawElements(g2d, "players");
        drawElements(g2d, "boss");
        drawElements(g2d, "bullet");
        drawElements(g2d, "explosion");

        drawHUD(g2d);

        if (paused && pauseOverlay != null) {
            pauseOverlay.paint(g2d);
        }

        if (battleFinished) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 48));
            String result = GameContext.playerWin ? "胜利！" : "失败！";
            g2d.drawString(result, GameConfig.WINDOW_WIDTH / 2 - 60, GameConfig.WINDOW_HEIGHT / 2);
            g2d.setFont(new Font("微软雅黑", Font.PLAIN, 20));
            g2d.drawString("点击或按任意键返回主菜单...", GameConfig.WINDOW_WIDTH / 2 - 120, GameConfig.WINDOW_HEIGHT / 2 + 50);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawElements(Graphics2D g2d, String type) {
        List<SuperElement> elements = ElementManager.getInstance().getElements(type);
        if (elements == null) return;
        for (SuperElement e : elements) {
            if (e.isVisible()) e.show(g2d);
        }
    }

    private void drawHUD(Graphics2D g2d) {
        if (gameThread != null) {
            remainingTime = gameThread.getRemainingTime();
        }
        int seconds = (int)(remainingTime / 1000);
        String timeStr = String.format("%02d:%02d", seconds / 60, seconds % 60);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 24));
        g2d.drawString(timeStr, GameConfig.WINDOW_WIDTH / 2 - 30, 30);

        if (player1 != null && player1.isVisible()) {
            drawTankHUD(g2d, player1, 10, GameConfig.WINDOW_HEIGHT - 100, "P1");
        }
        if (player2 != null && player2.isVisible()) {
            String label = player2 instanceof Boss ? "AI" : "P2";
            drawTankHUD(g2d, player2, GameConfig.WINDOW_WIDTH - 210, GameConfig.WINDOW_HEIGHT - 100, label);
        }
    }

    private void drawTankHUD(Graphics2D g2d, Players tank, int x, int y, String label) {
        g2d.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        g2d.setColor(Color.WHITE);
        g2d.drawString(label + " " + tank.getCustomName(), x, y - 5);

        int barW = GameConfig.HUD_BAR_WIDTH;
        int barH = GameConfig.HUD_BAR_HEIGHT;
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x, y, barW, barH);

        float hpRatio = (float) tank.getHp() / tank.getMaxHp();
        if (hpRatio > 0.6f) g2d.setColor(Color.GREEN);
        else if (hpRatio > 0.3f) g2d.setColor(Color.YELLOW);
        else g2d.setColor(Color.RED);
        g2d.fillRect(x, y, (int)(barW * hpRatio), barH);

        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, barW, barH);
        g2d.drawString("HP: " + tank.getHp() + "/" + tank.getMaxHp(), x + 5, y + 14);

        g2d.drawString("弹药: " + tank.getAmmo() + "/" + tank.getMaxAmmo(), x, y + 35);
        if (tank.isReloading()) {
            g2d.setColor(Color.ORANGE);
            g2d.drawString("换弹中...", x, y + 55);
        }
    }

    public GameThread getGameThread() { return gameThread; }

    class PauseOverlay {
        private JPanel parent;
        private GameThread gameThread;
        private int btnX, btnW, btnH;
        private int resumeY, restartY, endY;

        public PauseOverlay(JPanel parent, GameThread gameThread) {
            this.parent = parent;
            this.gameThread = gameThread;
            btnW = 200;
            btnH = 40;
            btnX = GameConfig.WINDOW_WIDTH / 2 - btnW / 2;
            resumeY = GameConfig.WINDOW_HEIGHT / 2 - 80;
            restartY = GameConfig.WINDOW_HEIGHT / 2 - 20;
            endY = GameConfig.WINDOW_HEIGHT / 2 + 40;
        }

        public void paint(Graphics2D g2d) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("微软雅黑", Font.BOLD, 36));
            g2d.drawString("暂停", GameConfig.WINDOW_WIDTH / 2 - 40, GameConfig.WINDOW_HEIGHT / 2 - 120);

            drawButton(g2d, btnX, resumeY, btnW, btnH, "继续");
            drawButton(g2d, btnX, restartY, btnW, btnH, "重新开始");
            drawButton(g2d, btnX, endY, btnW, btnH, "结束对战");
        }

        private void drawButton(Graphics2D g2d, int x, int y, int w, int h, String text) {
            g2d.setColor(new Color(80, 80, 80));
            g2d.fillRect(x, y, w, h);
            g2d.setColor(Color.WHITE);
            g2d.drawRect(x, y, w, h);
            g2d.setFont(new Font("微软雅黑", Font.PLAIN, 18));
            java.awt.FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (w - fm.stringWidth(text)) / 2;
            int textY = y + (h + fm.getAscent()) / 2 - 2;
            g2d.drawString(text, textX, textY);
        }

        public boolean handleClick(int mx, int my) {
            if (mx >= btnX && mx <= btnX + btnW) {
                if (my >= resumeY && my <= resumeY + btnH) {
                    paused = false;
                    gameThread.resumeGame();
                    pauseOverlay = null;
                    parent.requestFocusInWindow();
                    return true;
                } else if (my >= restartY && my <= restartY + btnH) {
                    cleanup();
                    startGame(GameConfig.DEFAULT_MATCH_DURATION);
                    paused = false;
                    pauseOverlay = null;
                    return true;
                } else if (my >= endY && my <= endY + btnH) {
                    gameThread.stopGame();
                    GameContext.battleEnded = true;
                    GameContext.playerWin = false;
                    battleFinished = true;
                    paused = false;
                    pauseOverlay = null;
                    onBattleEnd();
                    return true;
                }
            }
            return false;
        }
    }
}
