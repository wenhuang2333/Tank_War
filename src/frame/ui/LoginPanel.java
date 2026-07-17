package frame.ui;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.io.File;
import java.util.List;

import frame.MyJFrame;
import model.manager.SaveManager;
import model.vo.PlayerSaveData;
import util.GameContext;
import util.ResourceManager;

public class LoginPanel extends BasePanel {
    public LoginPanel(MyJFrame frame) {
        super(frame);
    }

    @Override
    protected String getBackgroundImagePath() {
        return ResourceManager.LOGIN_BG;
    }

    @Override
    protected JComponent buildContent() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.DARK_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(10, 0, 10, 0);

        // Logo
        JLabel logoLabel = createImageLabel(ResourceManager.LOGIN_LOGO, 480, 140);
        panel.add(logoLabel, gbc);

        // Login button with image
        JButton loginBtn = createImageButton(ResourceManager.LOGIN_BTN_LOGIN_NORMAL,
            ResourceManager.LOGIN_BTN_LOGIN_HOVER, 220, 60);
        loginBtn.addActionListener(e -> onLogin());
        panel.add(loginBtn, gbc);

        // Exit button with image
        JButton exitBtn = createImageButton(ResourceManager.LOGIN_BTN_EXIT_NORMAL,
            ResourceManager.LOGIN_BTN_EXIT_HOVER, 220, 60);
        exitBtn.addActionListener(e -> System.exit(0));
        panel.add(exitBtn, gbc);

        return panel;
    }

    private void onLogin() {
        SaveManager sm = SaveManager.getInstance();
        List<File> saveFiles = sm.listSaveFiles();

        if (saveFiles.isEmpty()) {
            String name = JOptionPane.showInputDialog(frame, "请输入玩家名：", "创建新档", JOptionPane.PLAIN_MESSAGE);
            if (name == null || name.trim().isEmpty()) name = "Player";
            GameContext.currentSave = PlayerSaveData.createNew(name.trim());
            SaveManager.getInstance().save(GameContext.currentSave);
            frame.showMainMenu();
        } else {
            DefaultListModel<String> model = new DefaultListModel<>();
            for (File f : saveFiles) {
                model.addElement(f.getName());
            }
            JList<String> list = new JList<>(model);
            list.setFont(new Font("Monospaced", Font.PLAIN, 14));
            JScrollPane scroll = new JScrollPane(list);
            scroll.setPreferredSize(new Dimension(400, 200));

            Object[] options = {"加载选中存档", "创建新档"};
            int choice = JOptionPane.showOptionDialog(frame, scroll, "选择存档",
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (choice == 0) {
                int idx = list.getSelectedIndex();
                if (idx >= 0 && idx < saveFiles.size()) {
                    GameContext.currentSave = sm.load(saveFiles.get(idx));
                    frame.showMainMenu();
                } else {
                    JOptionPane.showMessageDialog(frame, "请先选择一个存档", "提示", JOptionPane.WARNING_MESSAGE);
                }
            } else if (choice == 1) {
                String name = JOptionPane.showInputDialog(frame, "请输入玩家名：", "创建新档", JOptionPane.PLAIN_MESSAGE);
                if (name == null || name.trim().isEmpty()) name = "Player";
                GameContext.currentSave = PlayerSaveData.createNew(name.trim());
                SaveManager.getInstance().save(GameContext.currentSave);
                frame.showMainMenu();
            }
        }
    }
}
