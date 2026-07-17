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

public class MainMenuPanel extends BasePanel {
    public MainMenuPanel(MyJFrame frame) {
        super(frame);
    }

    @Override
    protected JComponent buildContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 40, 40));
        buttonPanel.setBackground(Color.DARK_GRAY);
        buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(150, 200, 150, 200));

        buttonPanel.add(createMenuButton("对战", () -> {
            GameContext.battleMode = "pve";
            frame.showTankSelect();
        }));
        buttonPanel.add(createMenuButton("坦克养成", () -> frame.showDevelopPanel()));
        buttonPanel.add(createMenuButton("坦克获取", () -> frame.showGachaPanel()));
        buttonPanel.add(createMenuButton("退出游戏", this::onExit));

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
