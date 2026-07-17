package frame.ui;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Font;

import frame.MyJFrame;
import model.manager.SaveManager;
import util.GameContext;
import util.ResourceManager;

public class SettingsPanel extends BasePanel {
    public SettingsPanel(MyJFrame frame) {
        super(frame);
    }

    @Override
    protected String getBackgroundImagePath() {
        return ResourceManager.SETTINGS_BG;
    }

    @Override
    protected JComponent buildContent() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 20, 20));
        panel.setOpaque(false);
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(100, 200, 100, 200));

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

        panel.add(bgmLabel);
        panel.add(bgmSlider);
        panel.add(sfxLabel);
        panel.add(sfxSlider);
        panel.add(langLabel);
        panel.add(langCombo);
        panel.add(new JLabel());
        panel.add(saveBtn);

        return panel;
    }
}
