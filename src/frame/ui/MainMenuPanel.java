package frame.ui;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.io.File;

import frame.MyJFrame;
import model.manager.SaveManager;
import util.GameContext;
import util.ResourceManager;

public class MainMenuPanel extends BasePanel {
    public MainMenuPanel(MyJFrame frame) {
        super(frame);
    }

    @Override
    protected String getBackgroundImagePath() {
        return ResourceManager.MENU_BG;
    }

    @Override
    protected JComponent buildContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 40, 40));
        buttonPanel.setBackground(Color.DARK_GRAY);
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(150, 200, 150, 200));

        JButton battleBtn = createImageButton(ResourceManager.MENU_BTN_BATTLE, 260, 90);
        battleBtn.addActionListener(e -> {
            GameContext.battleMode = "pve";
            frame.showTankSelect();
        });
        buttonPanel.add(battleBtn);

        JButton trainingBtn = createImageButton(ResourceManager.MENU_BTN_TRAINING, 260, 90);
        trainingBtn.addActionListener(e -> frame.showDevelopPanel());
        buttonPanel.add(trainingBtn);

        JButton gachaBtn = createImageButton(ResourceManager.MENU_BTN_GACHA, 260, 90);
        gachaBtn.addActionListener(e -> frame.showGachaPanel());
        buttonPanel.add(gachaBtn);

        JButton exitBtn = createImageButton(ResourceManager.MENU_BTN_EXIT_GAME, 260, 90);
        exitBtn.addActionListener(e -> onExit());
        buttonPanel.add(exitBtn);

        panel.add(buttonPanel, BorderLayout.CENTER);
        return panel;
    }

    private JButton createMenuButton(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.BOLD, 28));
        btn.setBackground(new Color(60, 80, 100));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void onExit() {
        int choice = JOptionPane.showConfirmDialog(frame,
            "是否保存当前存档？", "退出游戏",
            JOptionPane.YES_NO_CANCEL_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            SaveManager.getInstance().save(GameContext.currentSave);
            System.exit(0);
        } else if (choice == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
    }
}
