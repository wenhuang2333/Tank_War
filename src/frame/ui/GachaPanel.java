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

import frame.MyJFrame;
import model.manager.GachaManager;
import model.manager.SaveManager;
import model.vo.PlayerSaveData;
import model.vo.Modification;
import util.GameContext;
import util.ResourceManager;

public class GachaPanel extends BasePanel {
    private JLabel infoLabel;
    private GachaManager gacha;

    public GachaPanel(MyJFrame frame) {
        super(frame);
        gacha = new GachaManager(
            GameContext.currentSave != null ? GameContext.currentSave.getGachaState().getTankPool().getPityCounter() : 0,
            GameContext.currentSave != null ? GameContext.currentSave.getGachaState().getModPool().getPityCounter() : 0
        );
    }

    @Override
    protected String getBackgroundImagePath() {
        return ResourceManager.GACHA_BG;
    }

    @Override
    protected JComponent buildContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY);

        infoLabel = new JLabel("", JLabel.CENTER);
        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        infoLabel.setForeground(Color.WHITE);
        updateInfo();
        panel.add(infoLabel, BorderLayout.NORTH);

        JPanel poolsPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        poolsPanel.setBackground(Color.DARK_GRAY);
        poolsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(50, 80, 50, 80));

        // Tank pool
        JPanel tankPool = new JPanel(new BorderLayout());
        tankPool.setBackground(Color.DARK_GRAY);
        JLabel tankPoolBg = createImageLabel(ResourceManager.GACHA_TANK_POOL_BG, 580, 520);
        tankPoolBg.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 400));
        JButton tankSingle = createImageButton(ResourceManager.GACHA_BTN_PULL_ONE, 200, 60);
        tankSingle.addActionListener(e -> drawTank(1));
        JButton tankTen = createImageButton(ResourceManager.GACHA_BTN_PULL_TEN, 220, 60);
        tankTen.addActionListener(e -> drawTank(10));
        tankPoolBg.add(tankSingle);
        tankPoolBg.add(tankTen);
        tankPool.add(tankPoolBg, BorderLayout.CENTER);
        poolsPanel.add(tankPool);

        // Mod pool
        JPanel modPool = new JPanel(new BorderLayout());
        modPool.setBackground(Color.DARK_GRAY);
        JLabel modPoolBg = createImageLabel(ResourceManager.GACHA_MOD_POOL_BG, 580, 520);
        modPoolBg.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 400));
        JButton modSingle = createImageButton(ResourceManager.GACHA_BTN_PULL_ONE, 200, 60);
        modSingle.addActionListener(e -> drawMod(1));
        JButton modTen = createImageButton(ResourceManager.GACHA_BTN_PULL_TEN, 220, 60);
        modTen.addActionListener(e -> drawMod(10));
        modPoolBg.add(modSingle);
        modPoolBg.add(modTen);
        modPool.add(modPoolBg, BorderLayout.CENTER);
        poolsPanel.add(modPool);

        panel.add(poolsPanel, BorderLayout.CENTER);

        JPanel resourcePanel = new JPanel();
        resourcePanel.setBackground(Color.DARK_GRAY);
        JButton craftSpecificBtn = new JButton("合成特定碎片 (25→装备)");
        JButton craftUniversalBtn = new JButton("合成通用碎片 (50→装备)");
        craftSpecificBtn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        craftUniversalBtn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        resourcePanel.add(craftSpecificBtn);
        resourcePanel.add(craftUniversalBtn);

        JButton exchangeBtn = new JButton("碎片兑换 (2换1通用)");
        exchangeBtn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        resourcePanel.add(exchangeBtn);

        craftSpecificBtn.addActionListener(e -> craftSpecific());
        craftUniversalBtn.addActionListener(e -> craftUniversal());
        exchangeBtn.addActionListener(e -> exchangeFragments());

        panel.add(resourcePanel, BorderLayout.SOUTH);
        return panel;
    }

    private void styleGachaButton(JButton btn) {
        btn.setFont(new Font("微软雅黑", Font.BOLD, 20));
        btn.setBackground(new Color(80, 60, 120));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
    }

    private void drawTank(int times) {
        int cost = times == 1 ? 3 : 27;
        PlayerSaveData.Resources r = GameContext.currentSave.getResources();
        if (r.getBlueprints() < cost) {
            JOptionPane.showMessageDialog(frame, "蓝图不足！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        r.setBlueprints(r.getBlueprints() - cost);
        GachaManager.GachaResult result = gacha.drawTank(times);
        showResult(result);
    }

    private void drawMod(int times) {
        int cost = times == 1 ? 3 : 27;
        PlayerSaveData.Resources r = GameContext.currentSave.getResources();
        if (r.getBlueprints() < cost) {
            JOptionPane.showMessageDialog(frame, "蓝图不足！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        r.setBlueprints(r.getBlueprints() - cost);
        GachaManager.GachaResult result = gacha.drawModification(times);
        showResult(result);
    }

    private void craftSpecific() {
        showFragmentCraft();
    }

    private void craftUniversal() {
        int universal = GameContext.currentSave.getModificationInv().getUniversalFragments();
        if (universal < 50) {
            JOptionPane.showMessageDialog(frame, "通用碎片不足50个！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Let user choose which equipment type to craft
        String[] modTypes = new String[Modification.Type.values().length];
        for (int i = 0; i < Modification.Type.values().length; i++) {
            modTypes[i] = Modification.Type.values()[i].getChineseName();
        }
        String choice = (String) JOptionPane.showInputDialog(frame,
            "选择要合成的装备类型 (消耗50通用碎片):", "通用碎片合成",
            JOptionPane.PLAIN_MESSAGE, null, modTypes, modTypes[0]);
        if (choice == null) return;

        GameContext.currentSave.getModificationInv().setUniversalFragments(universal - 50);
        GameContext.currentSave.getModificationInv().getCraftedEquipments().add(choice);
        SaveManager.getInstance().save(GameContext.currentSave);
        JOptionPane.showMessageDialog(frame, "合成成功！获得 " + choice, "成功", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showFragmentCraft() {
        java.util.Map<String, Integer> frags = GameContext.currentSave.getModificationInv().getSpecificFragments();
        StringBuilder sb = new StringBuilder("选择要合成的装备类型 (>25碎片):\n");
        java.util.List<String> available = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, Integer> e : frags.entrySet()) {
            if (e.getValue() >= 25) {
                available.add(e.getKey());
                sb.append(e.getKey()).append(": ").append(e.getValue()).append("碎片\n");
            }
        }
        if (available.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "没有足够的特定碎片（需要25个）", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String choice = (String) JOptionPane.showInputDialog(frame, sb.toString(), "合成特定碎片",
            JOptionPane.PLAIN_MESSAGE, null, available.toArray(), available.get(0));
        if (choice != null) {
            frags.put(choice, frags.get(choice) - 25);
            GameContext.currentSave.getModificationInv().getCraftedEquipments().add(choice);
            SaveManager.getInstance().save(GameContext.currentSave);
            JOptionPane.showMessageDialog(frame, "合成成功！获得 " + choice, "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showResult(GachaManager.GachaResult result) {
        StringBuilder sb = new StringBuilder("抽卡结果:\n");
        for (GachaManager.Reward r : result.rewards) {
            sb.append(r.type).append(" x").append(r.amount);
            if (r.isJackpot) sb.append(" ★大奖★");
            sb.append("\n");
        }
        SaveManager.getInstance().save(GameContext.currentSave);
        frame.updateResourceBar();
        updateInfo();
        JOptionPane.showMessageDialog(frame, sb.toString(), "抽卡结果", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exchangeFragments() {
        java.util.Map<String, Integer> frags = GameContext.currentSave.getModificationInv().getSpecificFragments();
        java.util.List<String> available = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, Integer> e : frags.entrySet()) {
            if (e.getValue() >= 2) available.add(e.getKey());
        }
        if (available.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "没有足够的特定碎片（需要至少2个）", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String choice = (String) JOptionPane.showInputDialog(frame,
            "选择要兑换的碎片类型 (2碎片→1通用碎片):", "碎片兑换",
            JOptionPane.PLAIN_MESSAGE, null, available.toArray(), available.get(0));
        if (choice != null) {
            int count = frags.get(choice);
            int maxExchange = count / 2;
            String amtStr = JOptionPane.showInputDialog(frame,
                choice + " 当前: " + count + "个, 最多可兑换: " + maxExchange + "次\n输入兑换次数:", "1");
            if (amtStr != null && !amtStr.trim().isEmpty()) {
                try {
                    int times = Integer.parseInt(amtStr.trim());
                    if (times < 1 || times > maxExchange) {
                        JOptionPane.showMessageDialog(frame, "无效的兑换次数", "提示", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    frags.put(choice, count - times * 2);
                    GameContext.currentSave.getModificationInv().setUniversalFragments(
                        GameContext.currentSave.getModificationInv().getUniversalFragments() + times);
                    SaveManager.getInstance().save(GameContext.currentSave);
                    JOptionPane.showMessageDialog(frame, "兑换成功！获得 " + times + " 个通用碎片", "成功", JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "请输入有效数字", "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }

    private void updateInfo() {
        if (GameContext.currentSave == null) return;
        infoLabel.setText("坦克池保底: " + gacha.getTankPityCounter() + "抽 | 改装池保底: " + gacha.getModPityCounter() + "抽");
    }
}
