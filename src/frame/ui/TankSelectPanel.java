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
    private int selectedBossTank = 2;
    private String selectedDifficulty = "easy";
    private JLabel mapLabel, tankLabel;
    private JComboBox<String> difficultyCombo;

    public TankSelectPanel(MyJFrame frame) {
        super(frame);
    }

    @Override
    protected String getBackgroundImagePath() {
        return ResourceManager.SELECT_BG;
    }

    @Override
    protected JComponent buildContent() {
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

        // Tank selection with icon
        JPanel tankPanel = new JPanel();
        tankPanel.setBackground(Color.DARK_GRAY);
        JLabel tankIconLabel = new JLabel("", JLabel.CENTER);
        tankIconLabel.setPreferredSize(new Dimension(140, 160));
        updateTankIcon(tankIconLabel);
        tankLabel = new JLabel("我方坦克: 1 - 克伦威尔", JLabel.CENTER);
        tankLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        tankLabel.setForeground(Color.WHITE);
        JButton prevTank = createImageButton(ResourceManager.COMMON_ARROW_LEFT, 36, 36);
        JButton nextTank = createImageButton(ResourceManager.COMMON_ARROW_RIGHT, 36, 36);
        prevTank.addActionListener(e -> { selectedPlayerTank = Math.max(1, selectedPlayerTank - 1); updateLabels(); updateTankIcon(tankIconLabel); });
        nextTank.addActionListener(e -> { selectedPlayerTank = Math.min(8, selectedPlayerTank + 1); updateLabels(); updateTankIcon(tankIconLabel); });
        tankPanel.add(prevTank);
        tankPanel.add(tankIconLabel);
        tankPanel.add(nextTank);
        JPanel tankLabelRow = new JPanel();
        tankLabelRow.setBackground(Color.DARK_GRAY);
        tankLabelRow.add(tankLabel);

        // Difficulty
        JPanel diffPanel = new JPanel();
        diffPanel.setBackground(Color.DARK_GRAY);
        JLabel diffLabel = new JLabel("难度:", JLabel.CENTER);
        diffLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        diffLabel.setForeground(Color.WHITE);
        JButton easyBtn = createImageButton(ResourceManager.SELECT_BTN_EASY, 180, 55);
        JButton hardBtn = createImageButton(ResourceManager.SELECT_BTN_HARD, 180, 55);
        JButton superBtn = createImageButton(ResourceManager.SELECT_BTN_SUPER, 180, 55);
        easyBtn.addActionListener(e -> selectedDifficulty = "easy");
        hardBtn.addActionListener(e -> selectedDifficulty = "hard");
        superBtn.addActionListener(e -> selectedDifficulty = "super");
        diffPanel.add(diffLabel);
        diffPanel.add(easyBtn);
        diffPanel.add(hardBtn);
        diffPanel.add(superBtn);

        JPanel tankWrap = new JPanel(new BorderLayout());
        tankWrap.setBackground(Color.DARK_GRAY);
        tankWrap.add(tankPanel, BorderLayout.CENTER);
        tankWrap.add(tankLabelRow, BorderLayout.SOUTH);

        center.add(mapWrap);
        center.add(tankWrap);
        center.add(diffPanel);

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

    private void updateTankIcon(JLabel label) {
        String path = ResourceManager.tankBody(selectedPlayerTank);
        java.awt.image.BufferedImage img = ElementLoad.getInstance().getImage(path);
        if (img != null && img.getWidth() > 10) {
            int displayW = Math.min(img.getWidth(), 120);
            int displayH = Math.min(img.getHeight(), 100);
            label.setIcon(new ImageIcon(img.getScaledInstance(displayW, displayH, java.awt.Image.SCALE_SMOOTH)));
        }
    }

    private void updateLabels() {
        String[] mapNames = {"", "开阔平原", "中门对狙", "迷宫回廊", "长枪直道", "十字要冲", "回字堡垒"};
        mapLabel.setText("地图: " + selectedMap + " - " + mapNames[selectedMap]);
        TankData td = TankDataManager.getInstance().getTankData(selectedPlayerTank);
        tankLabel.setText("我方坦克: " + selectedPlayerTank + " - " + (td != null ? td.getName() : ""));
    }

    private void startBattle() {
        GameContext.selectedMap = selectedMap;
        GameContext.player1TankId = selectedPlayerTank;
        GameContext.difficulty = selectedDifficulty;
        GameContext.isInBattle = true;
        GameContext.battleEnded = false;

        ElementFactory factory = ElementFactory.getInstance();
        Players p1 = factory.createPlayer(selectedPlayerTank, GameConfig.P1_SPAWN_X, GameConfig.P1_SPAWN_Y);
        Boss boss = factory.createBoss(selectedBossTank, GameConfig.P2_SPAWN_X, GameConfig.P2_SPAWN_Y,
            selectedDifficulty.equals("hard") ? new HardAI() :
            selectedDifficulty.equals("super") ? new SuperAI() : new EasyAI());

        frame.showGamePanel(p1, boss, selectedMap, selectedDifficulty, GameConfig.DEFAULT_MATCH_DURATION);
    }
}
