package frame.ui;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.ImageIcon;
import javax.swing.DefaultComboBoxModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.util.List;
import java.util.ArrayList;

import frame.MyJFrame;
import model.load.ElementLoad;
import model.vo.*;
import model.manager.TankDataManager;
import model.manager.ElementFactory;
import thread.ai.EasyAI;
import thread.ai.HardAI;
import thread.ai.SuperAI;
import util.GameConfig;
import util.GameContext;
import util.ResourceManager;

public class TankSelectPanel extends BasePanel {
    private int selectedMap = 1;
    private int selectedPlayerTank = 1;
    private int selectedPlayer2Tank = 2;
    private int selectedBossTank = 2;
    private String selectedDifficulty = "easy";
    private boolean selectingP2;
    private JLabel mapLabel, tankLabel, tank2Label;
    private JComboBox<String> difficultyCombo;
    private List<Integer> ownedTankIds;

    public TankSelectPanel(MyJFrame frame) {
        super(frame);
    }

    @Override
    protected String getBackgroundImagePath() {
        return ResourceManager.SELECT_BG;
    }

    @Override
    protected JComponent buildContent() {
        initOwnedTanks();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);

        JPanel center = new JPanel(new GridLayout(3, 1, 10, 10));
        center.setBackground(Color.DARK_GRAY);
        center.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 200, 30, 200));

        // Map selection with thumbnail
        JPanel mapPanel = new JPanel();
        mapPanel.setBackground(Color.DARK_GRAY);
        JLabel mapThumbLabel = new JLabel("", JLabel.CENTER);
        mapThumbLabel.setPreferredSize(new Dimension(320, 220));
        updateMapThumb(mapThumbLabel);
        JButton prevMap = createImageButton(ResourceManager.COMMON_ARROW_LEFT, 36, 36);
        JButton nextMap = createImageButton(ResourceManager.COMMON_ARROW_RIGHT, 36, 36);
        prevMap.addActionListener(e -> { selectedMap = Math.max(1, selectedMap - 1); updateLabels(); updateMapThumb(mapThumbLabel); });
        nextMap.addActionListener(e -> { selectedMap = Math.min(6, selectedMap + 1); updateLabels(); updateMapThumb(mapThumbLabel); });
        mapLabel = new JLabel("地图: 1 - 开阔平原", JLabel.CENTER);
        mapLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        mapLabel.setForeground(Color.WHITE);
        mapPanel.add(prevMap);
        mapPanel.add(mapThumbLabel);
        mapPanel.add(nextMap);
        JPanel mapLabelRow = new JPanel();
        mapLabelRow.setBackground(Color.DARK_GRAY);
        mapLabelRow.add(mapLabel);
        JPanel mapWrap = new JPanel(new BorderLayout());
        mapWrap.setBackground(Color.DARK_GRAY);
        mapWrap.add(mapPanel, BorderLayout.CENTER);
        mapWrap.add(mapLabelRow, BorderLayout.SOUTH);

        boolean isPvp = "pvp".equals(GameContext.battleMode);

        // Tank 1 selection with icon
        JPanel tank1Panel = new JPanel();
        tank1Panel.setBackground(Color.DARK_GRAY);
        JLabel tankIconLabel = new JLabel("", JLabel.CENTER);
        tankIconLabel.setPreferredSize(new Dimension(140, 160));
        updateTankIcon(tankIconLabel, selectedPlayerTank);
        tankLabel = new JLabel((isPvp ? "P1坦克: " : "我方坦克: ") + "1 - 克伦威尔", JLabel.CENTER);
        tankLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        tankLabel.setForeground(Color.WHITE);
        JButton prevTank = createImageButton(ResourceManager.COMMON_ARROW_LEFT, 36, 36);
        JButton nextTank = createImageButton(ResourceManager.COMMON_ARROW_RIGHT, 36, 36);
        prevTank.addActionListener(e -> { selectedPlayerTank = prevOwned(selectedPlayerTank); updateLabels(); updateTankIcon(tankIconLabel, selectedPlayerTank); });
        nextTank.addActionListener(e -> { selectedPlayerTank = nextOwned(selectedPlayerTank); updateLabels(); updateTankIcon(tankIconLabel, selectedPlayerTank); });
        tank1Panel.add(prevTank);
        tank1Panel.add(tankIconLabel);
        tank1Panel.add(nextTank);
        JPanel tank1LabelRow = new JPanel();
        tank1LabelRow.setBackground(Color.DARK_GRAY);
        tank1LabelRow.add(tankLabel);

        // Tank 2 selection (PVP only)
        JPanel tank2Section = null;
        JLabel tank2IconLabel = null;
        if (isPvp) {
            tank2Section = new JPanel(new BorderLayout());
            tank2Section.setBackground(Color.DARK_GRAY);
            JPanel tank2Panel = new JPanel();
            tank2Panel.setBackground(Color.DARK_GRAY);
            tank2IconLabel = new JLabel("", JLabel.CENTER);
            tank2IconLabel.setPreferredSize(new Dimension(140, 160));
            updateTankIcon(tank2IconLabel, selectedPlayer2Tank);
            tank2Label = new JLabel("P2坦克: 2 - M24霞飞", JLabel.CENTER);
            tank2Label.setFont(new Font("微软雅黑", Font.PLAIN, 20));
            tank2Label.setForeground(Color.WHITE);
            JButton prevTank2 = createImageButton(ResourceManager.COMMON_ARROW_LEFT, 36, 36);
            JButton nextTank2 = createImageButton(ResourceManager.COMMON_ARROW_RIGHT, 36, 36);
            final JLabel t2IconRef = tank2IconLabel;
            prevTank2.addActionListener(e -> { selectedPlayer2Tank = prevOwned(selectedPlayer2Tank); updateLabels(); updateTankIcon(t2IconRef, selectedPlayer2Tank); });
            nextTank2.addActionListener(e -> { selectedPlayer2Tank = nextOwned(selectedPlayer2Tank); updateLabels(); updateTankIcon(t2IconRef, selectedPlayer2Tank); });
            tank2Panel.add(prevTank2);
            tank2Panel.add(tank2IconLabel);
            tank2Panel.add(nextTank2);
            JPanel tank2LabelRow = new JPanel();
            tank2LabelRow.setBackground(Color.DARK_GRAY);
            tank2LabelRow.add(tank2Label);
            tank2Section.add(tank2Panel, BorderLayout.CENTER);
            tank2Section.add(tank2LabelRow, BorderLayout.SOUTH);
        }

        // Difficulty (PVE only) or Settings button (PVP)
        JPanel bottomRow = new JPanel();
        bottomRow.setBackground(Color.DARK_GRAY);
        if (isPvp) {
            JButton settingsBtn = new JButton("比赛设置");
            settingsBtn.setFont(new Font("微软雅黑", Font.BOLD, 18));
            settingsBtn.setBackground(new Color(60, 80, 100));
            settingsBtn.setForeground(Color.WHITE);
            settingsBtn.setFocusPainted(false);
            settingsBtn.addActionListener(e -> frame.showPanel("settings"));
            bottomRow.add(settingsBtn);
        } else {
            JLabel diffLabel = new JLabel("难度:", JLabel.CENTER);
            diffLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
            diffLabel.setForeground(Color.WHITE);
            JButton easyBtn = createImageButton(ResourceManager.SELECT_BTN_EASY, 180, 55);
            JButton hardBtn = createImageButton(ResourceManager.SELECT_BTN_HARD, 180, 55);
            JButton superBtn = createImageButton(ResourceManager.SELECT_BTN_SUPER, 180, 55);
            easyBtn.addActionListener(e -> selectedDifficulty = "easy");
            hardBtn.addActionListener(e -> selectedDifficulty = "hard");
            superBtn.addActionListener(e -> selectedDifficulty = "super");
            bottomRow.add(diffLabel);
            bottomRow.add(easyBtn);
            bottomRow.add(hardBtn);
            bottomRow.add(superBtn);
        }

        JPanel tankWrap = new JPanel(new BorderLayout());
        tankWrap.setBackground(Color.DARK_GRAY);
        tankWrap.add(tank1Panel, BorderLayout.CENTER);
        tankWrap.add(tank1LabelRow, BorderLayout.SOUTH);

        if (isPvp) {
            // GridLayout of 4 rows: map, tank1, tank2, bottomRow
            JPanel newCenter = new JPanel(new GridLayout(4, 1, 10, 10));
            newCenter.setBackground(Color.DARK_GRAY);
            newCenter.setBorder(javax.swing.BorderFactory.createEmptyBorder(30, 200, 30, 200));
            newCenter.add(mapWrap);
            newCenter.add(tankWrap);
            newCenter.add(tank2Section);
            newCenter.add(bottomRow);
            // Remove old center, add new one
            panel.remove(center);
            panel.add(newCenter, BorderLayout.CENTER);
        } else {
            center.add(mapWrap);
            center.add(tankWrap);
            center.add(bottomRow);
        }

        JButton startBtn = createImageButton(ResourceManager.SELECT_BTN_START_BATTLE, 240, 70);
        startBtn.addActionListener(e -> startBattle());

        JPanel bottom = new JPanel();
        bottom.setBackground(Color.DARK_GRAY);
        bottom.add(startBtn);

        panel.add(center, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void updateMapThumb(JLabel label) {
        java.awt.image.BufferedImage thumb = ElementLoad.getInstance().getImage(ResourceManager.mapThumb(selectedMap));
        if (thumb != null && thumb.getWidth() > 10) {
            label.setIcon(new ImageIcon(thumb.getScaledInstance(320, 220, java.awt.Image.SCALE_SMOOTH)));
        }
    }

    private void updateTankIcon(JLabel label, int tankId) {
        String path = ResourceManager.tankBody(tankId);
        java.awt.image.BufferedImage img = ElementLoad.getInstance().getImage(path);
        if (img != null && img.getWidth() > 10) {
            int displayW = Math.min(img.getWidth(), 120);
            int displayH = Math.min(img.getHeight(), 100);
            label.setIcon(new ImageIcon(img.getScaledInstance(displayW, displayH, java.awt.Image.SCALE_SMOOTH)));
        }
    }

    private boolean isPvp() { return "pvp".equals(GameContext.battleMode); }

    private void initOwnedTanks() {
        ownedTankIds = new ArrayList<>();
        if (GameContext.currentSave == null || GameContext.currentSave.getOwnedTanks() == null) return;
        for (PlayerSaveData.OwnedTank ot : GameContext.currentSave.getOwnedTanks()) {
            ownedTankIds.add(ot.getTankId());
        }
        if (!ownedTankIds.isEmpty()) {
            selectedPlayerTank = ownedTankIds.get(0);
            selectedPlayer2Tank = ownedTankIds.size() > 1 ? ownedTankIds.get(1) : ownedTankIds.get(0);
        }
    }

    private int nextOwned(int currentId) {
        if (ownedTankIds.isEmpty()) return currentId;
        int idx = ownedTankIds.indexOf(currentId);
        if (idx < 0 || idx >= ownedTankIds.size() - 1) return ownedTankIds.get(0);
        return ownedTankIds.get(idx + 1);
    }

    private int prevOwned(int currentId) {
        if (ownedTankIds.isEmpty()) return currentId;
        int idx = ownedTankIds.indexOf(currentId);
        if (idx <= 0) return ownedTankIds.get(ownedTankIds.size() - 1);
        return ownedTankIds.get(idx - 1);
    }

    private void updateLabels() {
        String[] mapNames = {"", "开阔平原", "中门对狙", "迷宫回廊", "长枪直道", "十字要冲", "回字堡垒"};
        mapLabel.setText("地图: " + selectedMap + " - " + mapNames[selectedMap]);
        TankData td = TankDataManager.getInstance().getTankData(selectedPlayerTank);
        boolean pvp = isPvp();
        tankLabel.setText((pvp ? "P1坦克: " : "我方坦克: ") + selectedPlayerTank + " - " + (td != null ? td.getName() : ""));
        if (pvp && tank2Label != null) {
            TankData td2 = TankDataManager.getInstance().getTankData(selectedPlayer2Tank);
            tank2Label.setText("P2坦克: " + selectedPlayer2Tank + " - " + (td2 != null ? td2.getName() : ""));
        }
    }

    private void startBattle() {
        GameContext.selectedMap = selectedMap;
        GameContext.player1TankId = selectedPlayerTank;
        GameContext.player2TankId = selectedPlayer2Tank;
        GameContext.difficulty = selectedDifficulty;
        GameContext.isInBattle = true;
        GameContext.battleEnded = false;

        ElementFactory factory = ElementFactory.getInstance();
        Players p1 = factory.createPlayer(selectedPlayerTank, GameConfig.P1_SPAWN_X, GameConfig.P1_SPAWN_Y);
        applySavedTankData(p1, selectedPlayerTank);

        boolean isPvp = "pvp".equals(GameContext.battleMode);
        int duration = isPvp ? GameContext.matchDuration : GameConfig.DEFAULT_MATCH_DURATION;
        if (isPvp) {
            Players p2 = factory.createPlayer(selectedPlayer2Tank, GameConfig.P2_SPAWN_X, GameConfig.P2_SPAWN_Y);
            applySavedTankData(p2, selectedPlayer2Tank);
            frame.showGamePanel(p1, p2, selectedMap, "pvp", duration);
        } else {
            Boss boss = factory.createBoss(selectedBossTank, GameConfig.P2_SPAWN_X, GameConfig.P2_SPAWN_Y,
                selectedDifficulty.equals("hard") ? new HardAI() :
                selectedDifficulty.equals("super") ? new SuperAI() : new EasyAI());
            frame.showGamePanel(p1, boss, selectedMap, selectedDifficulty, duration);
        }
    }

    private void applySavedTankData(Players p, int tankId) {
        if (GameContext.currentSave == null) return;
        for (PlayerSaveData.OwnedTank ot : GameContext.currentSave.getOwnedTanks()) {
            if (ot.getTankId() == tankId) {
                p.applySaveData(ot);
                return;
            }
        }
    }
}
