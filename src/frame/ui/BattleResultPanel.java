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
        JLabel resultLabel = createImageLabel(
            win ? ResourceManager.RESULT_VICTORY_TEXT : ResourceManager.RESULT_DEFEAT_TEXT, 340, 90);
        resultLabel.setHorizontalAlignment(JLabel.CENTER);
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

        JButton backBtn = createImageButton(ResourceManager.RESULT_BTN_RETURN_MENU, 220, 60);
        backBtn.addActionListener(e -> {
            GameContext.isInBattle = false;
            frame.showMainMenu();
        });
        center.add(backBtn);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }
}
