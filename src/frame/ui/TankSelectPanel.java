package frame.ui;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.util.List;
import java.util.ArrayList;

import frame.MyJFrame;
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
        center.setBorder(javax.swing.BorderFactory.createEmptyBorder(50, 200, 50, 200));

        JPanel mapPanel = new JPanel();
        mapPanel.setBackground(Color.DARK_GRAY);
        mapLabel = new JLabel("地图: 1 - 开阔平原", JLabel.CENTER);
        mapLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        mapLabel.setForeground(Color.WHITE);
        JButton prevMap = new JButton("<");
        JButton nextMap = new JButton(">");
        prevMap.addActionListener(e -> { selectedMap = Math.max(1, selectedMap - 1); updateLabels(); });
        nextMap.addActionListener(e -> { selectedMap = Math.min(6, selectedMap + 1); updateLabels(); });
        mapPanel.add(prevMap);
        mapPanel.add(mapLabel);
        mapPanel.add(nextMap);

        JPanel tankPanel = new JPanel();
        tankPanel.setBackground(Color.DARK_GRAY);
        tankLabel = new JLabel("我方坦克: 1 - 克伦威尔", JLabel.CENTER);
        tankLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        tankLabel.setForeground(Color.WHITE);
        JButton prevTank = new JButton("<");
        JButton nextTank = new JButton(">");
        prevTank.addActionListener(e -> { selectedPlayerTank = Math.max(1, selectedPlayerTank - 1); updateLabels(); });
        nextTank.addActionListener(e -> { selectedPlayerTank = Math.min(8, selectedPlayerTank + 1); updateLabels(); });
        tankPanel.add(prevTank);
        tankPanel.add(tankLabel);
        tankPanel.add(nextTank);

        JPanel diffPanel = new JPanel();
        diffPanel.setBackground(Color.DARK_GRAY);
        JLabel diffLabel = new JLabel("难度:", JLabel.CENTER);
        diffLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        diffLabel.setForeground(Color.WHITE);
        difficultyCombo = new JComboBox<>(new DefaultComboBoxModel<>(new String[]{"简单", "困难", "超级"}));
        difficultyCombo.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        difficultyCombo.addActionListener(e -> {
            int idx = difficultyCombo.getSelectedIndex();
            selectedDifficulty = idx == 0 ? "easy" : idx == 1 ? "hard" : "super";
        });
        diffPanel.add(diffLabel);
        diffPanel.add(difficultyCombo);

        center.add(mapPanel);
        center.add(tankPanel);
        center.add(diffPanel);

        JButton startBtn = new JButton("开始对战");
        startBtn.setFont(new Font("微软雅黑", Font.BOLD, 24));
        startBtn.setBackground(new Color(60, 120, 60));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFocusPainted(false);
        startBtn.setPreferredSize(new Dimension(200, 50));
        startBtn.addActionListener(e -> startBattle());

        JPanel bottom = new JPanel();
        bottom.setBackground(Color.DARK_GRAY);
        bottom.add(startBtn);

        panel.add(center, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
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
