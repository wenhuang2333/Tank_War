package frame.ui;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.ArrayList;

import frame.MyJFrame;
import model.vo.*;
import model.manager.TankDataManager;
import model.manager.SaveManager;
import util.GameContext;

public class DevelopPanel extends BasePanel {
    private int currentTankIndex;
    private List<Players> playerTanks;

    public DevelopPanel(MyJFrame frame) {
        super(frame);
        loadTanks();
    }

    private void loadTanks() {
        playerTanks = new ArrayList<>();
        if (GameContext.currentSave == null) return;
        for (PlayerSaveData.OwnedTank ot : GameContext.currentSave.getOwnedTanks()) {
            TankData td = TankDataManager.getInstance().getTankData(ot.getTankId());
            if (td == null) continue;
            Players p = new Players(td);
            p.setTankId(ot.getTankId());
            p.setRank(ot.getRank());
            p.setLevel(ot.getLevel());
            p.setCustomName(ot.getCustomName());
            PlayerSaveData.CombatStats cs = ot.getCombatStats();
            if (cs != null) {
                p.setHp(cs.getHp());
                p.setMaxHp(cs.getHp());
                p.setAttack(cs.getAttack());
                p.setDefense(cs.getDefense());
                p.setSpeed(cs.getSpeed());
                p.setTurnSpeed(cs.getTurnSpeed());
                p.setBulletSpeed(cs.getBulletSpeed());
                p.setDurability(cs.getMaxDurability());
            }
            if (ot.getInstalledMods() != null) {
                for (PlayerSaveData.InstalledMod im : ot.getInstalledMods()) {
                    if (im != null) {
                        try {
                            Modification.Type type = Modification.Type.valueOf(im.getModType());
                            p.getInstalledMods()[im.getSlotIndex()] = new Modification(type, "CRAFTED_SPECIFIC".equals(im.getSource()));
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
            }
            if (ot.getCooldownSlots() != null) {
                for (PlayerSaveData.CooldownSlot cs2 : ot.getCooldownSlots()) {
                    if (cs2 != null) {
                        p.getCooldownSlots()[cs2.getSlotIndex()] = cs2.getRemainingBattles();
                    }
                }
            }
            playerTanks.add(p);
        }
    }

    @Override
    protected JComponent buildContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);
        panel.add(buildTankList(), BorderLayout.WEST);
        panel.add(buildDetailPanel(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTankList() {
        JPanel panel = new JPanel(new GridLayout(8, 1, 5, 5));
        panel.setBackground(Color.DARK_GRAY);
        panel.setPreferredSize(new java.awt.Dimension(200, 600));
        if (playerTanks == null) return panel;
        for (int i = 0; i < playerTanks.size(); i++) {
            Players p = playerTanks.get(i);
            JButton btn = new JButton(p.getCustomName());
            btn.setBackground(currentTankIndex == i ? new Color(60, 120, 60) : new Color(70, 70, 70));
            btn.setForeground(Color.WHITE);
            final int idx = i;
            btn.addActionListener(e -> { currentTankIndex = idx; refreshPanel(); });
            panel.add(btn);
        }
        return panel;
    }

    private JPanel buildDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);

        if (playerTanks == null || playerTanks.isEmpty() || currentTankIndex >= playerTanks.size()) {
            panel.add(new JLabel("暂无坦克", JLabel.CENTER), BorderLayout.CENTER);
            return panel;
        }

        Players tank = playerTanks.get(currentTankIndex);
        JPanel infoPanel = new JPanel(new GridLayout(12, 2, 10, 5));
        infoPanel.setBackground(Color.DARK_GRAY);
        infoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 20, 40));

        addInfoRow(infoPanel, "名称:", tank.getCustomName());
        addInfoRow(infoPanel, "阶数:", tank.getRank() + " / " + util.GameConfig.MAX_RANK);
        addInfoRow(infoPanel, "等级:", tank.getLevel() + " / " + util.GameConfig.MAX_LEVEL);
        addInfoRow(infoPanel, "HP:", tank.getHp() + " / " + tank.getMaxHp());
        addInfoRow(infoPanel, "攻击:", String.valueOf(tank.getAttack()));
        addInfoRow(infoPanel, "防御:", String.valueOf(tank.getDefense()));
        addInfoRow(infoPanel, "速度:", String.valueOf(tank.getSpeed()));
        addInfoRow(infoPanel, "耐久:", tank.getDurability() + " / " + tank.getMaxDurability());
        addInfoRow(infoPanel, "改装槽:", tank.getUnlockedSlots() + " 个");
        addInfoRow(infoPanel, "型号:", tank.computeModelName());

        JPanel actionPanel = new JPanel();
        actionPanel.setBackground(Color.DARK_GRAY);
        JButton upgradeBtn = new JButton("升级 (" + tank.getUpgradeCost() + " 粗铁)");
        upgradeBtn.addActionListener(e -> onUpgrade(tank));
        JButton evolveBtn = new JButton("进阶 (" + tank.getEvolveCost() + " 钢铁)");
        evolveBtn.addActionListener(e -> onEvolve(tank));
        actionPanel.add(upgradeBtn);
        actionPanel.add(evolveBtn);

        panel.add(infoPanel, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        l.setForeground(Color.LIGHT_GRAY);
        JLabel v = new JLabel(value);
        v.setFont(new Font("微软雅黑", Font.BOLD, 16));
        v.setForeground(Color.WHITE);
        panel.add(l);
        panel.add(v);
    }

    private void onUpgrade(Players tank) {
        int cost = tank.getUpgradeCost();
        PlayerSaveData.Resources r = GameContext.currentSave.getResources();
        if (r.getIron() < cost) {
            JOptionPane.showMessageDialog(frame, "粗铁不足！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!tank.upgrade(r.getIron())) {
            JOptionPane.showMessageDialog(frame, "无法升级（已满级或资源不足）", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        r.setIron(r.getIron() - cost);
        saveAndRefresh();
    }

    private void onEvolve(Players tank) {
        int cost = tank.getEvolveCost();
        PlayerSaveData.Resources r = GameContext.currentSave.getResources();
        if (r.getSteel() < cost) {
            JOptionPane.showMessageDialog(frame, "钢铁不足！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!tank.evolve(r.getSteel())) {
            JOptionPane.showMessageDialog(frame, "无法进阶（需要满级或已满阶）", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        r.setSteel(r.getSteel() - cost);
        saveAndRefresh();
    }

    private void saveAndRefresh() {
        SaveManager.getInstance().save(GameContext.currentSave);
        frame.updateResourceBar();
        refreshPanel();
    }

    private void refreshPanel() {
        removeAll();
        add(createHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
