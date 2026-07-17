package frame.ui;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Font;

import frame.MyJFrame;
import model.manager.SaveManager;
import util.GameContext;
import util.ResourceManager;

public class SettingsPanel extends BasePanel {
    private JSpinner matchTimeSpinner;
    private JCheckBox overtimeModeCb, overtimePenaltyCb;
    private JCheckBox cheatP1InvincibleCb, cheatP2InvincibleCb;
    private JCheckBox cheatP1WallPassCb, cheatP2WallPassCb;
    private JCheckBox cheatNoDurabilityCb, cheatBulletReboundCb;
    private JCheckBox cheatFriendlyFireCb;

    public SettingsPanel(MyJFrame frame) {
        super(frame);
    }

    @Override
    protected String getBackgroundImagePath() {
        return ResourceManager.SETTINGS_BG;
    }

    @Override
    protected JComponent buildContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 20, 20));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(50, 200, 50, 200));

        // Audio settings
        JPanel audioPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        audioPanel.setOpaque(false);

        JLabel bgmLabel = new JLabel("背景音乐音量:");
        bgmLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        bgmLabel.setForeground(Color.WHITE);
        JSlider bgmSlider = new JSlider(0, 100, 80);
        bgmSlider.setOpaque(false);

        JLabel sfxLabel = new JLabel("音效音量:");
        sfxLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        sfxLabel.setForeground(Color.WHITE);
        JSlider sfxSlider = new JSlider(0, 100, 100);
        sfxSlider.setOpaque(false);

        JLabel langLabel = new JLabel("语言:");
        langLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        langLabel.setForeground(Color.WHITE);
        javax.swing.JComboBox<String> langCombo = new javax.swing.JComboBox<>(new String[]{"简体中文", "English"});

        JButton saveBtn = new JButton("保存设置");
        saveBtn.setFont(new Font("微软雅黑", Font.BOLD, 18));
        saveBtn.setBackground(new Color(60, 100, 60));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            if (GameContext.currentSave != null) {
                GameContext.currentSave.getSettings().setBgmVolume(bgmSlider.getValue() / 100.0);
                GameContext.currentSave.getSettings().setSfxVolume(sfxSlider.getValue() / 100.0);
                SaveManager.getInstance().save(GameContext.currentSave);
            }
        });

        audioPanel.add(bgmLabel);
        audioPanel.add(bgmSlider);
        audioPanel.add(sfxLabel);
        audioPanel.add(sfxSlider);
        audioPanel.add(langLabel);
        audioPanel.add(langCombo);
        audioPanel.add(new JLabel());
        audioPanel.add(saveBtn);

        // Battle settings (PVP only)
        JPanel battlePanel = new JPanel(new GridLayout(0, 2, 8, 8));
        battlePanel.setOpaque(false);

        JLabel battleTitle = new JLabel("—— PVP 比赛设置 ——");
        battleTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        battleTitle.setForeground(Color.ORANGE);
        battlePanel.add(battleTitle);
        battlePanel.add(new JLabel());

        battlePanel.add(makeLabel("比赛时间 (分钟):"));
        matchTimeSpinner = new JSpinner(new SpinnerNumberModel(5, 3, 10, 1));
        matchTimeSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        battlePanel.add(matchTimeSpinner);

        battlePanel.add(makeLabel("加时赛模式:"));
        overtimeModeCb = new JCheckBox();
        overtimeModeCb.setOpaque(false);
        overtimeModeCb.addActionListener(e -> {
            if (overtimeModeCb.isSelected()) overtimePenaltyCb.setSelected(false);
        });
        battlePanel.add(overtimeModeCb);

        battlePanel.add(makeLabel("超时惩罚:"));
        overtimePenaltyCb = new JCheckBox();
        overtimePenaltyCb.setOpaque(false);
        overtimePenaltyCb.addActionListener(e -> {
            if (overtimePenaltyCb.isSelected()) overtimeModeCb.setSelected(false);
        });
        battlePanel.add(overtimePenaltyCb);

        // Cheats
        JLabel cheatTitle = new JLabel("—— 作弊设置 ——");
        cheatTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        cheatTitle.setForeground(Color.ORANGE);
        battlePanel.add(cheatTitle);
        battlePanel.add(new JLabel());

        battlePanel.add(makeLabel("P1 无敌:"));
        cheatP1InvincibleCb = new JCheckBox();
        cheatP1InvincibleCb.setOpaque(false);
        battlePanel.add(cheatP1InvincibleCb);

        battlePanel.add(makeLabel("P2 无敌:"));
        cheatP2InvincibleCb = new JCheckBox();
        cheatP2InvincibleCb.setOpaque(false);
        battlePanel.add(cheatP2InvincibleCb);

        battlePanel.add(makeLabel("P1 穿墙:"));
        cheatP1WallPassCb = new JCheckBox();
        cheatP1WallPassCb.setOpaque(false);
        battlePanel.add(cheatP1WallPassCb);

        battlePanel.add(makeLabel("P2 穿墙:"));
        cheatP2WallPassCb = new JCheckBox();
        cheatP2WallPassCb.setOpaque(false);
        battlePanel.add(cheatP2WallPassCb);

        battlePanel.add(makeLabel("取消耐久消耗:"));
        cheatNoDurabilityCb = new JCheckBox();
        cheatNoDurabilityCb.setOpaque(false);
        battlePanel.add(cheatNoDurabilityCb);

        battlePanel.add(makeLabel("子弹遇墙反弹:"));
        cheatBulletReboundCb = new JCheckBox();
        cheatBulletReboundCb.setOpaque(false);
        battlePanel.add(cheatBulletReboundCb);

        battlePanel.add(makeLabel("友伤惩罚:"));
        cheatFriendlyFireCb = new JCheckBox();
        cheatFriendlyFireCb.setOpaque(false);
        battlePanel.add(cheatFriendlyFireCb);

        mainPanel.add(audioPanel);
        mainPanel.add(new javax.swing.JScrollPane(battlePanel));

        // Apply button for battle settings
        JButton applyBtn = new JButton("应用比赛设置");
        applyBtn.setFont(new Font("微软雅黑", Font.BOLD, 18));
        applyBtn.setBackground(new Color(60, 100, 60));
        applyBtn.setForeground(Color.WHITE);
        applyBtn.addActionListener(e -> applyBattleSettings());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.add(applyBtn);

        panel.add(mainPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        label.setForeground(Color.LIGHT_GRAY);
        return label;
    }

    private void applyBattleSettings() {
        GameContext.matchDuration = ((Number) matchTimeSpinner.getValue()).intValue() * 60;
        GameContext.overtimeMode = overtimeModeCb.isSelected();
        GameContext.overtimePenalty = overtimePenaltyCb.isSelected();
        GameContext.cheatP1Invincible = cheatP1InvincibleCb.isSelected();
        GameContext.cheatP2Invincible = cheatP2InvincibleCb.isSelected();
        GameContext.cheatP1WallPass = cheatP1WallPassCb.isSelected();
        GameContext.cheatP2WallPass = cheatP2WallPassCb.isSelected();
        GameContext.cheatNoDurability = cheatNoDurabilityCb.isSelected();
        GameContext.cheatBulletRebound = cheatBulletReboundCb.isSelected();
        GameContext.cheatFriendlyFire = cheatFriendlyFireCb.isSelected();
    }
}
