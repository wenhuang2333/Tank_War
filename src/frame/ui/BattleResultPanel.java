package frame.ui;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Font;

import frame.MyJFrame;
import model.manager.SaveManager;
import model.vo.PlayerSaveData;
import util.GameContext;
import util.ResourceManager;

public class BattleResultPanel extends BasePanel {
    public BattleResultPanel(MyJFrame frame) {
        super(frame);
    }

    @Override
    protected String getBackgroundImagePath() {
        return GameContext.playerWin ? ResourceManager.RESULT_VICTORY : ResourceManager.RESULT_DEFEAT;
    }

    @Override
    protected JComponent buildContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);

        JPanel center = new JPanel(new GridLayout(6, 1, 10, 10));
        center.setBackground(Color.DARK_GRAY);
        center.setBorder(javax.swing.BorderFactory.createEmptyBorder(80, 300, 80, 300));

        boolean win = GameContext.playerWin;
        JLabel resultLabel = new JLabel(win ? "胜 利！" : "失 败！", JLabel.CENTER);
        resultLabel.setFont(new Font("微软雅黑", Font.BOLD, 48));
        resultLabel.setForeground(win ? new Color(50, 200, 50) : new Color(200, 50, 50));
        center.add(resultLabel);

        PlayerSaveData.Resources r = GameContext.currentSave.getResources();
        JLabel ironLabel = new JLabel("粗铁: " + r.getIron(), JLabel.CENTER);
        ironLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        ironLabel.setForeground(Color.WHITE);
        JLabel steelLabel = new JLabel("钢铁: " + r.getSteel(), JLabel.CENTER);
        steelLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        steelLabel.setForeground(Color.WHITE);
        JLabel bpLabel = new JLabel("蓝图: " + r.getBlueprints(), JLabel.CENTER);
        bpLabel.setFont(new Font("微软雅黑", Font.PLAIN, 20));
        bpLabel.setForeground(Color.WHITE);

        center.add(ironLabel);
        center.add(steelLabel);
        center.add(bpLabel);

        JButton backBtn = new JButton("返回主菜单");
        backBtn.setFont(new Font("微软雅黑", Font.BOLD, 22));
        backBtn.setBackground(new Color(60, 100, 60));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> {
            GameContext.isInBattle = false;
            frame.showMainMenu();
        });
        center.add(backBtn);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }
}
