package frame.ui;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.util.List;
import java.util.ArrayList;

import frame.MyJFrame;
import model.load.ElementLoad;
import model.vo.*;
import model.manager.TankDataManager;
import model.manager.SaveManager;
import util.GameConfig;
import util.GameContext;
import util.ResourceManager;

public class DevelopPanel extends BasePanel {
    private int currentTankIndex;
    private List<Players> playerTanks;

    public DevelopPanel(MyJFrame frame) {
        super(frame);
        loadTanks();
    }

    @Override
    protected String getBackgroundImagePath() {
        return ResourceManager.TRAINING_BG;
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

        JLabel tankDisplay = createImageLabel(ResourceManager.TRAINING_TANK_DISPLAY_BG, 320, 340);
        tankDisplay.setLayout(new BorderLayout());
        if (playerTanks != null && !playerTanks.isEmpty() && currentTankIndex < playerTanks.size()) {
            Players tank = playerTanks.get(currentTankIndex);
            java.awt.image.BufferedImage tankImg = ElementLoad.getInstance().getImage(
                ResourceManager.tankBody(tank.getTankId()));
            if (tankImg != null && tankImg.getWidth() > 10) {
                JLabel tankPic = new JLabel(new ImageIcon(tankImg.getScaledInstance(280, 280, java.awt.Image.SCALE_SMOOTH)));
                tankPic.setHorizontalAlignment(JLabel.CENTER);
                tankDisplay.add(tankPic, BorderLayout.CENTER);
            }
        }

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.DARK_GRAY);
        leftPanel.add(tankDisplay, BorderLayout.CENTER);
        leftPanel.add(buildTankList(), BorderLayout.SOUTH);
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(buildDetailPanel(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTankList() {
        JPanel panel = new JPanel(new GridLayout(8, 1, 5, 5));
        panel.setBackground(Color.DARK_GRAY);
        panel.setPreferredSize(new java.awt.Dimension(200, 300));
        if (playerTanks == null) return panel;
        for (int i = 0; i < playerTanks.size(); i++) {
            Players p = playerTanks.get(i);
            String label = p.getCustomName();
            String model = p.computeModelName();
            if (!model.isEmpty()) label += " " + model;
            JButton btn = new JButton(label);
            btn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
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

        JLabel attrBg = createImageLabel(ResourceManager.TRAINING_ATTR_PANEL_BG, 800, 520);
        attrBg.setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel(new GridLayout(13, 2, 10, 5));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 40, 20, 40));

        String model = tank.computeModelName();
        addInfoRow(infoPanel, "名称:", tank.getCustomName() + (model.isEmpty() ? "" : " " + model));
        addInfoRow(infoPanel, "阶数:", tank.getRank() + " / " + GameConfig.MAX_RANK);
        addInfoRow(infoPanel, "等级:", tank.getLevel() + " / " + GameConfig.MAX_LEVEL);
        addInfoRow(infoPanel, "HP:", tank.getHp() + " / " + tank.getMaxHp());
        addInfoRow(infoPanel, "攻击:", String.valueOf(tank.getAttack()));
        addInfoRow(infoPanel, "防御:", String.valueOf(tank.getDefense()));
        addInfoRow(infoPanel, "速度:", String.valueOf(tank.getSpeed()));
        addInfoRow(infoPanel, "耐久:", tank.getDurability() + " / " + tank.getMaxDurability());
        addInfoRow(infoPanel, "改装槽:", buildSlotStatus(tank));
        addInfoRow(infoPanel, "型号:", model.isEmpty() ? "无" : model);

        // Installed mods
        StringBuilder modsStr = new StringBuilder();
        Modification[] mods = tank.getInstalledMods();
        int[] cooldowns = tank.getCooldownSlots();
        for (int i = 0; i < mods.length; i++) {
            if (mods[i] != null) {
                if (modsStr.length() > 0) modsStr.append(", ");
                modsStr.append("[").append(i + 1).append("]").append(mods[i].getType().getChineseName());
            } else if (i < tank.getUnlockedSlots() && cooldowns[i] > 0) {
                if (modsStr.length() > 0) modsStr.append(", ");
                modsStr.append("[").append(i + 1).append("]冷却:").append(cooldowns[i]).append("场");
            }
        }
        if (modsStr.length() == 0) modsStr.append("无");
        addInfoRow(infoPanel, "已安装:", modsStr.toString());

        attrBg.add(infoPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        actionPanel.setOpaque(false);

        JButton renameBtn = new JButton("改名");
        renameBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        renameBtn.setBackground(new Color(80, 80, 120));
        renameBtn.setForeground(Color.WHITE);
        renameBtn.addActionListener(e -> onRename(tank));

        JButton upgradeBtn = createImageButton(ResourceManager.TRAINING_BTN_UPGRADE, 150, 45);
        upgradeBtn.addActionListener(e -> onUpgrade(tank));
        JButton evolveBtn = createImageButton(ResourceManager.TRAINING_BTN_RANK_UP, 150, 45);
        evolveBtn.addActionListener(e -> onEvolve(tank));
        JButton installBtn = new JButton("安装改装");
        installBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        installBtn.setBackground(new Color(60, 100, 60));
        installBtn.setForeground(Color.WHITE);
        installBtn.addActionListener(e -> onInstallMod(tank));
        JButton dismantleBtn = new JButton("拆卸改装");
        dismantleBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        dismantleBtn.setBackground(new Color(160, 60, 60));
        dismantleBtn.setForeground(Color.WHITE);
        dismantleBtn.addActionListener(e -> onDismantleMod(tank));

        actionPanel.add(renameBtn);
        actionPanel.add(upgradeBtn);
        actionPanel.add(evolveBtn);
        actionPanel.add(installBtn);
        actionPanel.add(dismantleBtn);
        attrBg.add(actionPanel, BorderLayout.SOUTH);

        panel.add(attrBg, BorderLayout.CENTER);
        return panel;
    }

    private String buildSlotStatus(Players tank) {
        int unlocked = tank.getUnlockedSlots();
        int installed = 0;
        int cooling = 0;
        if (tank.getInstalledMods() != null) {
            for (Modification m : tank.getInstalledMods()) {
                if (m != null) installed++;
            }
        }
        if (tank.getCooldownSlots() != null) {
            for (int i = 0; i < unlocked; i++) {
                if (tank.getInstalledMods()[i] == null && tank.getCooldownSlots()[i] > 0) cooling++;
            }
        }
        int free = unlocked - installed - cooling;
        return unlocked + " 个 (空闲:" + free + " 已装:" + installed + (cooling > 0 ? " 冷却:" + cooling : "") + ")";
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

    private void onRename(Players tank) {
        String newName = JOptionPane.showInputDialog(frame, "输入新名称:", tank.getCustomName());
        if (newName != null && !newName.trim().isEmpty()) {
            tank.setCustomName(newName.trim());
            syncToSave(tank);
            saveAndRefresh();
        }
    }

    private void onUpgrade(Players tank) {
        int cost = tank.getUpgradeCost();
        PlayerSaveData.Resources r = GameContext.currentSave.getResources();
        if (r.getIron() < cost) {
            JOptionPane.showMessageDialog(frame, "粗铁不足！需要 " + cost, "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!tank.upgrade(r.getIron())) {
            JOptionPane.showMessageDialog(frame, "无法升级（已满级或资源不足）", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        r.setIron(r.getIron() - cost);
        syncToSave(tank);
        saveAndRefresh();
    }

    private void onEvolve(Players tank) {
        int cost = tank.getEvolveCost();
        PlayerSaveData.Resources r = GameContext.currentSave.getResources();
        if (r.getSteel() < cost) {
            JOptionPane.showMessageDialog(frame, "钢铁不足！需要 " + cost, "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!tank.evolve(r.getSteel())) {
            JOptionPane.showMessageDialog(frame, "无法进阶（需要满级或已满阶）", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        r.setSteel(r.getSteel() - cost);
        syncToSave(tank);
        saveAndRefresh();
    }

    private void onInstallMod(Players tank) {
        if (GameContext.currentSave == null) return;
        List<String> crafted = GameContext.currentSave.getModificationInv().getCraftedEquipments();
        if (crafted == null || crafted.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "库存中没有已合成的装备，请先去抽卡页面合成！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int unlocked = tank.getUnlockedSlots();
        if (unlocked == 0) {
            JOptionPane.showMessageDialog(frame, "该坦克尚未解锁改装槽（需要进阶到3阶）", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Find available slots
        List<Integer> freeSlots = new ArrayList<>();
        Modification[] mods = tank.getInstalledMods();
        int[] cooldowns = tank.getCooldownSlots();
        for (int i = 0; i < unlocked; i++) {
            if (mods[i] == null && cooldowns[i] == 0) freeSlots.add(i);
        }
        if (freeSlots.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "没有可用的改装槽（可能都在冷却中）", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Choose equipment
        String equipChoice = (String) JOptionPane.showInputDialog(frame,
            "选择要安装的装备:", "安装改装",
            JOptionPane.PLAIN_MESSAGE, null, crafted.toArray(), crafted.get(0));
        if (equipChoice == null) return;

        // Choose slot
        String[] slotOptions = freeSlots.stream().map(i -> "槽位 " + (i + 1)).toArray(String[]::new);
        String slotChoice = (String) JOptionPane.showInputDialog(frame,
            "选择安装到哪个槽位:", "选择槽位",
            JOptionPane.PLAIN_MESSAGE, null, slotOptions, slotOptions[0]);
        if (slotChoice == null) return;

        int slotIndex = freeSlots.get(java.util.Arrays.asList(slotOptions).indexOf(slotChoice));

        // Parse equipment type
        Modification.Type modType = null;
        for (Modification.Type t : Modification.Type.values()) {
            if (t.getChineseName().equals(equipChoice) || t.name().equals(equipChoice)) {
                modType = t;
                break;
            }
        }
        if (modType == null) {
            JOptionPane.showMessageDialog(frame, "无效的装备类型: " + equipChoice, "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check weapon conflict
        if (Modification.isWeapon(modType)) {
            Modification existingWeapon = tank.getWeaponMod();
            if (existingWeapon != null) {
                JOptionPane.showMessageDialog(frame,
                    "该坦克已安装武器: " + existingWeapon.getType().getChineseName() + "，武器不可共存！",
                    "冲突", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        Modification mod = new Modification(modType, true);
        tank.installMod(mod, slotIndex);
        crafted.remove(equipChoice);
        syncToSave(tank);
        saveAndRefresh();
        JOptionPane.showMessageDialog(frame,
            "成功安装 " + modType.getChineseName() + " 到槽位 " + (slotIndex + 1), "成功", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onDismantleMod(Players tank) {
        Modification[] mods = tank.getInstalledMods();
        List<Integer> installedSlots = new ArrayList<>();
        for (int i = 0; i < mods.length; i++) {
            if (mods[i] != null) installedSlots.add(i);
        }
        if (installedSlots.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "该坦克没有已安装的改装", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] options = installedSlots.stream()
            .map(i -> "槽位 " + (i + 1) + ": " + mods[i].getType().getChineseName())
            .toArray(String[]::new);
        String choice = (String) JOptionPane.showInputDialog(frame,
            "选择要拆卸的改装（拆卸后将销毁装备，槽位进入5场冷却）:", "拆卸改装",
            JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if (choice == null) return;

        int idx = java.util.Arrays.asList(options).indexOf(choice);
        int slotIndex = installedSlots.get(idx);
        String modName = mods[slotIndex].getType().getChineseName();
        tank.removeMod(slotIndex);
        syncToSave(tank);
        saveAndRefresh();
        JOptionPane.showMessageDialog(frame,
            "已拆卸 " + modName + "，槽位 " + (slotIndex + 1) + " 进入 " + GameConfig.MOD_COOLDOWN_BATTLES + " 场冷却",
            "成功", JOptionPane.INFORMATION_MESSAGE);
    }

    private void syncToSave(Players tank) {
        if (GameContext.currentSave == null) return;
        for (PlayerSaveData.OwnedTank ot : GameContext.currentSave.getOwnedTanks()) {
            if (ot.getTankId() == tank.getTankId()) {
                ot.setCustomName(tank.getCustomName());
                ot.setRank(tank.getRank());
                ot.setLevel(tank.getLevel());

                PlayerSaveData.CombatStats cs = ot.getCombatStats();
                if (cs == null) {
                    cs = new PlayerSaveData.CombatStats();
                    ot.setCombatStats(cs);
                }
                cs.setHp(tank.getMaxHp());
                cs.setAttack(tank.getAttack());
                cs.setDefense(tank.getDefense());
                cs.setSpeed(tank.getSpeed());
                cs.setTurnSpeed(tank.getTurnSpeed());
                cs.setBulletSpeed(tank.getBulletSpeed());
                cs.setMaxDurability(tank.getMaxDurability());

                // Sync installed mods
                List<PlayerSaveData.InstalledMod> imList = new ArrayList<>();
                Modification[] mods = tank.getInstalledMods();
                if (mods != null) {
                    for (int i = 0; i < mods.length; i++) {
                        if (mods[i] != null) {
                            PlayerSaveData.InstalledMod im = new PlayerSaveData.InstalledMod();
                            im.setSlotIndex(i);
                            im.setModType(mods[i].getType().name());
                            im.setSource(mods[i].isUniversal() ? "CRAFTED_SPECIFIC" : "DEFAULT");
                            imList.add(im);
                        }
                    }
                }
                ot.setInstalledMods(imList);

                // Sync cooldowns
                List<PlayerSaveData.CooldownSlot> cdList = new ArrayList<>();
                int[] cooldowns = tank.getCooldownSlots();
                if (cooldowns != null) {
                    for (int i = 0; i < cooldowns.length; i++) {
                        if (cooldowns[i] > 0) {
                            PlayerSaveData.CooldownSlot cs2 = new PlayerSaveData.CooldownSlot();
                            cs2.setSlotIndex(i);
                            cs2.setRemainingBattles(cooldowns[i]);
                            cdList.add(cs2);
                        }
                    }
                }
                ot.setCooldownSlots(cdList);
                return;
            }
        }
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
