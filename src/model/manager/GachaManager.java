package model.manager;

import model.vo.PlayerSaveData;
import model.vo.Modification;
import util.GameContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class GachaManager {
    private int tankPityCounter;
    private int modPityCounter;
    private Random random = new Random();

    public GachaManager() {}

    public GachaManager(int tankPity, int modPity) {
        this.tankPityCounter = tankPity;
        this.modPityCounter = modPity;
    }

    public static class GachaResult {
        public List<Reward> rewards = new ArrayList<>();
        public boolean isJackpot;
    }

    public static class Reward {
        public String type;
        public int amount;
        public boolean isJackpot;

        public Reward(String type, int amount, boolean isJackpot) {
            this.type = type;
            this.amount = amount;
            this.isJackpot = isJackpot;
        }
    }

    public GachaResult drawTank(int times) {
        GachaResult result = new GachaResult();
        PlayerSaveData save = GameContext.currentSave;
        if (save == null) return result;

        for (int i = 0; i < times; i++) {
            tankPityCounter++;
            int pityBonus = calculatePityBonus(tankPityCounter);
            Reward reward = rollTankPool(pityBonus);
            result.rewards.add(reward);
            if (reward.isJackpot) {
                result.isJackpot = true;
                tankPityCounter = 0;
            }
        }
        save.getGachaState().getTankPool().setPityCounter(tankPityCounter);
        return result;
    }

    private Reward rollTankPool(int pityBonus) {
        if (tankPityCounter >= 90) {
            return drawNewTank();
        }

        int[] weights = {200, 100, 50, 20, 15, 10, 5, 5 + pityBonus};
        String[] types = {"iron_10", "iron_20", "iron_100", "iron_200", "steel_100", "steel_200", "steel_500", "new_tank"};
        int total = 0;
        for (int w : weights) total += w;
        int r = random.nextInt(total);
        int cumulative = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (r < cumulative) {
                if ("new_tank".equals(types[i])) {
                    return drawNewTank();
                }
                return parseResourceReward(types[i]);
            }
        }
        return new Reward("iron_10", 10, false);
    }

    private Reward drawNewTank() {
        PlayerSaveData save = GameContext.currentSave;
        List<Integer> ownedIds = new ArrayList<>();
        for (PlayerSaveData.OwnedTank ot : save.getOwnedTanks()) {
            ownedIds.add(ot.getTankId());
        }

        List<Integer> unowned = new ArrayList<>();
        for (int id = 1; id <= 8; id++) {
            if (!ownedIds.contains(id)) unowned.add(id);
        }

        if (unowned.isEmpty()) {
            save.getResources().setSteel(save.getResources().getSteel() + 500);
            return new Reward("steel_500", 500, true);
        }

        int newId = unowned.get(random.nextInt(unowned.size()));
        PlayerSaveData.OwnedTank newTank = PlayerSaveData.OwnedTank.createNew(newId);
        save.getOwnedTanks().add(newTank);

        Map<Integer, Boolean> bonus = save.getGachaState().getTankPool().getFirstTimeBonus();
        if (!bonus.containsKey(newId) || !bonus.get(newId)) {
            save.getResources().setSteel(save.getResources().getSteel() + 100);
            bonus.put(newId, true);
        }

        return new Reward("tank_" + newId, newId, true);
    }

    private Reward parseResourceReward(String type) {
        PlayerSaveData save = GameContext.currentSave;
        switch (type) {
            case "iron_10": save.getResources().setIron(save.getResources().getIron() + 10); return new Reward("iron", 10, false);
            case "iron_20": save.getResources().setIron(save.getResources().getIron() + 20); return new Reward("iron", 20, false);
            case "iron_100": save.getResources().setIron(save.getResources().getIron() + 100); return new Reward("iron", 100, false);
            case "iron_200": save.getResources().setIron(save.getResources().getIron() + 200); return new Reward("iron", 200, false);
            case "steel_100": save.getResources().setSteel(save.getResources().getSteel() + 100); return new Reward("steel", 100, false);
            case "steel_200": save.getResources().setSteel(save.getResources().getSteel() + 200); return new Reward("steel", 200, false);
            case "steel_500": save.getResources().setSteel(save.getResources().getSteel() + 500); return new Reward("steel", 500, false);
            default: return new Reward("iron", 10, false);
        }
    }

    public GachaResult drawModification(int times) {
        GachaResult result = new GachaResult();
        PlayerSaveData save = GameContext.currentSave;
        if (save == null) return result;

        for (int i = 0; i < times; i++) {
            modPityCounter++;
            int pityBonus = calculatePityBonus(modPityCounter);
            Reward reward = rollModPool(pityBonus);
            result.rewards.add(reward);
            if (reward.isJackpot) {
                result.isJackpot = true;
                modPityCounter = 0;
            }
        }
        save.getGachaState().getModPool().setPityCounter(modPityCounter);
        return result;
    }

    private Reward rollModPool(int pityBonus) {
        int[] weights = {100, 100, 50, 20, 20 + pityBonus, 10 + pityBonus, 5 + pityBonus, 10 + pityBonus, 5 + pityBonus};
        String[] types = {"iron_10", "iron_20", "iron_100", "iron_200", "specific_1", "specific_5", "specific_10", "universal_10", "universal_20"};
        int total = 0;
        for (int w : weights) total += w;
        int r = random.nextInt(total);
        int cumulative = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (r < cumulative) {
                return applyModReward(types[i]);
            }
        }
        return new Reward("universal_10", 10, true);
    }

    private Reward applyModReward(String type) {
        PlayerSaveData save = GameContext.currentSave;
        boolean isFrag = false;
        switch (type) {
            case "iron_10": save.getResources().setIron(save.getResources().getIron() + 10); return new Reward("iron", 10, false);
            case "iron_20": save.getResources().setIron(save.getResources().getIron() + 20); return new Reward("iron", 20, false);
            case "iron_100": save.getResources().setIron(save.getResources().getIron() + 100); return new Reward("iron", 100, false);
            case "iron_200": save.getResources().setIron(save.getResources().getIron() + 200); return new Reward("iron", 200, false);
            case "specific_1": isFrag = true; addFragment(save, 1); return new Reward("specific_fragment", 1, isFrag);
            case "specific_5": isFrag = true; addFragment(save, 5); return new Reward("specific_fragment", 5, isFrag);
            case "specific_10": isFrag = true; addFragment(save, 10); return new Reward("specific_fragment", 10, isFrag);
            case "universal_10": save.getModificationInv().setUniversalFragments(save.getModificationInv().getUniversalFragments() + 10); return new Reward("universal_fragment", 10, true);
            case "universal_20": save.getModificationInv().setUniversalFragments(save.getModificationInv().getUniversalFragments() + 20); return new Reward("universal_fragment", 20, true);
            default: return new Reward("iron", 10, false);
        }
    }

    private void addFragment(PlayerSaveData save, int amount) {
        Modification.Type[] types = Modification.Type.values();
        String modType = types[random.nextInt(types.length)].name();
        Map<String, Integer> frags = save.getModificationInv().getSpecificFragments();
        frags.put(modType, frags.getOrDefault(modType, 0) + amount);
    }

    private int calculatePityBonus(int counter) {
        int bonus = 0;
        if (counter >= 50) bonus += (counter - 49) * 5;
        if (counter >= 70) bonus += (counter - 69) * 5;
        return bonus;
    }

    public int getTankPityCounter() { return tankPityCounter; }
    public void setTankPityCounter(int c) { this.tankPityCounter = c; }
    public int getModPityCounter() { return modPityCounter; }
    public void setModPityCounter(int c) { this.modPityCounter = c; }
}
