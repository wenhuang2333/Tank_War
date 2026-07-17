package frame.ui;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.util.Map;
import java.util.HashMap;
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
    private Map<String, JSpinner> attrSpinners = new HashMap<>();

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

        // P1 attribute mods
        JLabel p1ModTitle = new JLabel("P1 属性修改 (0=不改):");
        p1ModTitle.setFont(new Font("微软雅黑", Font.BOLD, 14));
        p1ModTitle.setForeground(Color.CYAN);
        battlePanel.add(p1ModTitle);
        battlePanel.add(new JLabel());
        addAttrModRow(battlePanel, "HP", "p1Hp", "攻击", "p1Atk", "防御", "p1Def");
        addAttrModRow(battlePanel, "速度", "p1Spd", "转向", "p1Turn", "弹速", "p1BulSpd");
        addAttrModRow(battlePanel, "弹药", "p1Ammo", "换弹", "p1Reload", "耐久", "p1Dur");

        // P2 attribute mods
        JLabel p2ModTitle = new JLabel("P2 属性修改 (0=不改):");
        p2ModTitle.setFont(new Font("微软雅黑", Font.BOLD, 14));
        p2ModTitle.setForeground(Color.CYAN);
        battlePanel.add(p2ModTitle);
        battlePanel.add(new JLabel());
        addAttrModRow(battlePanel, "HP", "p2Hp", "攻击", "p2Atk", "防御", "p2Def");
        addAttrModRow(battlePanel, "速度", "p2Spd", "转向", "p2Turn", "弹速", "p2BulSpd");
        addAttrModRow(battlePanel, "弹药", "p2Ammo", "换弹", "p2Reload", "耐久", "p2Dur");

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

    private void addAttrModRow(JPanel panel, String l1, String k1, String l2, String k2, String l3, String k3) {
        addAttrModCell(panel, l1, k1);
        addAttrModCell(panel, l2, k2);
        addAttrModCell(panel, l3, k3);
    }

    private void addAttrModCell(JPanel panel, String label, String key) {
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        lbl.setForeground(Color.LIGHT_GRAY);
        panel.add(lbl);
        JSpinner sp = new JSpinner(new SpinnerNumberModel(0, -999, 999, 1));
        sp.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setColumns(4);
        attrSpinners.put(key, sp);
        panel.add(sp);
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

        // Apply attribute cheat modifiers
        readAttrMods("p1", GameContext.cheatP1Mods);
        readAttrMods("p2", GameContext.cheatP2Mods);
    }

    private void readAttrMods(String prefix, GameContext.AttributeMods mods) {
        mods.hp = getSpinnerVal(prefix + "Hp");
        mods.attack = getSpinnerVal(prefix + "Atk");
        mods.defense = getSpinnerVal(prefix + "Def");
        mods.speed = getSpinnerVal(prefix + "Spd");
        mods.turnSpeed = getSpinnerVal(prefix + "Turn");
        mods.bulletSpeed = getSpinnerVal(prefix + "BulSpd");
        mods.ammo = getSpinnerVal(prefix + "Ammo");
        mods.reloadTime = getSpinnerVal(prefix + "Reload");
        mods.durability = getSpinnerVal(prefix + "Dur");
    }

    private int getSpinnerVal(String key) {
        JSpinner sp = attrSpinners.get(key);
        return sp != null ? ((Number) sp.getValue()).intValue() : 0;
    }
}
