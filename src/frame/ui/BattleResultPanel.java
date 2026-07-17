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

        JPanel center = new JPanel(new GridLayout(10, 1, 10, 10));
        center.setBackground(Color.DARK_GRAY);
        center.setBorder(javax.swing.BorderFactory.createEmptyBorder(50, 300, 50, 300));

        boolean win = GameContext.playerWin;
        JLabel resultLabel = createImageLabel(
            win ? ResourceManager.RESULT_VICTORY_TEXT : ResourceManager.RESULT_DEFEAT_TEXT, 340, 90);
        resultLabel.setHorizontalAlignment(JLabel.CENTER);
        center.add(resultLabel);

        // Battle stats comparison
        JLabel statsTitle = new JLabel("—— 战斗数据对比 ——", JLabel.CENTER);
        statsTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        statsTitle.setForeground(Color.ORANGE);
        center.add(statsTitle);

        String p1Label = "P1";
        String p2Label = "pvp".equals(GameContext.battleMode) ? "P2" : "AI";
        JLabel shotsLabel = new JLabel("开火数:  " + p1Label + ": " + GameContext.p1ShotsFired + "  |  " + p2Label + ": " + GameContext.p2ShotsFired, JLabel.CENTER);
        shotsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        shotsLabel.setForeground(Color.WHITE);
        JLabel dmgDealtLabel = new JLabel("造成伤害:  " + p1Label + ": " + GameContext.p1DamageDealt + "  |  " + p2Label + ": " + GameContext.p2DamageDealt, JLabel.CENTER);
        dmgDealtLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        dmgDealtLabel.setForeground(Color.WHITE);
        JLabel dmgRecvLabel = new JLabel("受到伤害:  " + p1Label + ": " + GameContext.p1DamageReceived + "  |  " + p2Label + ": " + GameContext.p2DamageReceived, JLabel.CENTER);
        dmgRecvLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        dmgRecvLabel.setForeground(Color.WHITE);

        center.add(shotsLabel);
        center.add(dmgDealtLabel);
        center.add(dmgRecvLabel);

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
