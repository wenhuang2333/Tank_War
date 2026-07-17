package frame;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.CardLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;

import frame.ui.*;
import util.GameConfig;
import util.GameContext;
import model.vo.PlayerSaveData;

public class MyJFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentArea;
    private ResourceBar resourceBar;
    private Map<String, JPanel> panelCache = new HashMap<>();

    private MyGameJPanel gamePanel;

    public MyJFrame() {
        setTitle("坦克大战");
        setSize(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new BorderLayout());

        resourceBar = new ResourceBar();
        add(resourceBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        add(contentArea, BorderLayout.CENTER);
    }

    public void showPanel(String name) {
        if (!panelCache.containsKey(name)) {
            JPanel panel = createPanel(name);
            if (panel != null) {
                contentArea.add(panel, name);
                panelCache.put(name, panel);
            }
        }
        cardLayout.show(contentArea, name);
        if (!"battle".equals(name)) {
            updateResourceBar();
        }
    }

    private JPanel createPanel(String name) {
        switch (name) {
            case "login": return new LoginPanel(this);
            case "menu": return new MainMenuPanel(this);
            case "tankSelect": return new TankSelectPanel(this);
            case "develop": return new DevelopPanel(this);
            case "gacha": return new GachaPanel(this);
            case "battleResult": return new BattleResultPanel(this);
            case "settings": return new SettingsPanel(this);
            default: return null;
        }
    }

    public void showLoginPanel() { showPanel("login"); }
    public void showMainMenu() { showPanel("menu"); }
    public void showTankSelect() { showPanel("tankSelect"); }
    public void showDevelopPanel() { showPanel("develop"); }
    public void showGachaPanel() { showPanel("gacha"); }
    public void showBattleResult() { showPanel("battleResult"); }

    public void showGamePanel(model.vo.Players p1, model.vo.Players p2, int mapId, String difficulty, int duration) {
        gamePanel = new MyGameJPanel(this, p1, p2, mapId, difficulty);
        contentArea.add(gamePanel, "battle");
        cardLayout.show(contentArea, "battle");
        gamePanel.requestFocusInWindow();
        gamePanel.startGame(duration);
    }

    public void switchTo(JComponent panel) {
        contentArea.add(panel, "temp");
        cardLayout.show(contentArea, "temp");
    }

    public void updateResourceBar() {
        if (GameContext.currentSave != null) {
            PlayerSaveData.Resources r = GameContext.currentSave.getResources();
            resourceBar.update(r.getIron(), r.getSteel(), r.getBlueprints());
        }
    }

    public MyGameJPanel getGamePanel() { return gamePanel; }

    public class ResourceBar extends JPanel {
        private JLabel ironLabel, steelLabel, blueprintLabel;

        public ResourceBar() {
            setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 40, 5));
            setBackground(new Color(40, 40, 40));
            setPreferredSize(new Dimension(1380, 44));

            ironLabel = new JLabel();
            steelLabel = new JLabel();
            blueprintLabel = new JLabel();
            styleLabel(ironLabel);
            styleLabel(steelLabel);
            styleLabel(blueprintLabel);

            add(ironLabel);
            add(steelLabel);
            add(blueprintLabel);
            update(0, 0, 0);
        }

        private void styleLabel(JLabel label) {
            label.setFont(new Font("微软雅黑", Font.BOLD, 16));
            label.setForeground(Color.WHITE);
        }

        public void update(int iron, int steel, int blueprints) {
            ironLabel.setText("粗铁: " + iron);
            steelLabel.setText("钢铁: " + steel);
            blueprintLabel.setText("蓝图: " + blueprints);
        }
    }
}
