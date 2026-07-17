package model.vo;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class PlayerSaveData {
    private SaveMeta meta;
    private Resources resources;
    private List<OwnedTank> ownedTanks;
    private ModificationInventory modificationInv;
    private GachaState gachaState;
    private BattleHistory battleHistory;
    private GameSettings settings;

    public PlayerSaveData() {}

    public static PlayerSaveData createNew(String playerName) {
        PlayerSaveData data = new PlayerSaveData();
        data.meta = new SaveMeta(playerName);
        data.resources = new Resources();
        data.ownedTanks = new ArrayList<>();
        data.ownedTanks.add(OwnedTank.createNew(1));
        data.ownedTanks.add(OwnedTank.createNew(2));
        data.modificationInv = new ModificationInventory();
        data.gachaState = new GachaState();
        data.battleHistory = new BattleHistory();
        data.settings = new GameSettings();
        return data;
    }

    public SaveMeta getMeta() { return meta; }
    public void setMeta(SaveMeta meta) { this.meta = meta; }
    public Resources getResources() { return resources; }
    public void setResources(Resources resources) { this.resources = resources; }
    public List<OwnedTank> getOwnedTanks() { return ownedTanks; }
    public void setOwnedTanks(List<OwnedTank> tanks) { this.ownedTanks = tanks; }
    public ModificationInventory getModificationInv() { return modificationInv; }
    public void setModificationInv(ModificationInventory inv) { this.modificationInv = inv; }
    public GachaState getGachaState() { return gachaState; }
    public void setGachaState(GachaState state) { this.gachaState = state; }
    public BattleHistory getBattleHistory() { return battleHistory; }
    public void setBattleHistory(BattleHistory history) { this.battleHistory = history; }
    public GameSettings getSettings() { return settings; }
    public void setSettings(GameSettings settings) { this.settings = settings; }

    // Inner classes
    public static class SaveMeta {
        private String version = "1.0";
        private long saveTime;
        private String playerName;
        private long totalPlayTime;
        private int totalBattles;

        public SaveMeta() {}
        public SaveMeta(String playerName) {
            this.playerName = playerName;
            this.saveTime = System.currentTimeMillis();
            this.version = "1.0";
        }
        public String getVersion() { return version; }
        public void setVersion(String v) { this.version = v; }
        public long getSaveTime() { return saveTime; }
        public void setSaveTime(long t) { this.saveTime = t; }
        public String getPlayerName() { return playerName; }
        public void setPlayerName(String n) { this.playerName = n; }
        public long getTotalPlayTime() { return totalPlayTime; }
        public void setTotalPlayTime(long t) { this.totalPlayTime = t; }
        public int getTotalBattles() { return totalBattles; }
        public void setTotalBattles(int b) { this.totalBattles = b; }
    }

    public static class Resources {
        private int iron;
        private int steel;
        private int blueprints;

        public int getIron() { return iron; }
        public void setIron(int iron) { this.iron = iron; }
        public int getSteel() { return steel; }
        public void setSteel(int steel) { this.steel = steel; }
        public int getBlueprints() { return blueprints; }
        public void setBlueprints(int b) { this.blueprints = b; }
    }

    public static class OwnedTank {
        private int tankId;
        private String customName;
        private int rank;
        private int level;
        private CombatStats combatStats;
        private List<InstalledMod> installedMods;
        private List<CooldownSlot> cooldownSlots;

        public static OwnedTank createNew(int tankId) {
            OwnedTank ot = new OwnedTank();
            ot.tankId = tankId;
            ot.rank = 0;
            ot.level = 1;
            ot.installedMods = new ArrayList<>();
            ot.cooldownSlots = new ArrayList<>();
            model.manager.TankDataManager tdm = model.manager.TankDataManager.getInstance();
            model.vo.TankData td = tdm.getTankData(tankId);
            if (td != null) {
                ot.customName = td.getName();
                ot.combatStats = new CombatStats(td);
            }
            return ot;
        }

        public int getTankId() { return tankId; }
        public void setTankId(int id) { this.tankId = id; }
        public String getCustomName() { return customName; }
        public void setCustomName(String n) { this.customName = n; }
        public int getRank() { return rank; }
        public void setRank(int r) { this.rank = r; }
        public int getLevel() { return level; }
        public void setLevel(int l) { this.level = l; }
        public CombatStats getCombatStats() { return combatStats; }
        public void setCombatStats(CombatStats s) { this.combatStats = s; }
        public List<InstalledMod> getInstalledMods() { return installedMods; }
        public void setInstalledMods(List<InstalledMod> mods) { this.installedMods = mods; }
        public List<CooldownSlot> getCooldownSlots() { return cooldownSlots; }
        public void setCooldownSlots(List<CooldownSlot> slots) { this.cooldownSlots = slots; }
    }

    public static class CombatStats {
        private int hp, attack, defense, speed, turnSpeed;
        private double fireRate;
        private int bulletSpeed;
        private double bulletDuration;
        private int ammo, reloadTime, maxDurability;

        public CombatStats() {}
        public CombatStats(TankData td) {
            this.hp = td.getBaseHp();
            this.attack = td.getBaseAttack();
            this.defense = td.getBaseDefense();
            this.speed = td.getBaseSpeed();
            this.turnSpeed = td.getBaseTurnSpeed();
            this.fireRate = td.getBaseFireRate();
            this.bulletSpeed = td.getBaseBulletSpeed();
            this.bulletDuration = td.getBaseBulletDuration();
            this.ammo = td.getBaseAmmo();
            this.reloadTime = td.getBaseReloadTime();
            this.maxDurability = td.getBaseDurability();
        }
        public int getHp() { return hp; }
        public void setHp(int v) { this.hp = v; }
        public int getAttack() { return attack; }
        public void setAttack(int v) { this.attack = v; }
        public int getDefense() { return defense; }
        public void setDefense(int v) { this.defense = v; }
        public int getSpeed() { return speed; }
        public void setSpeed(int v) { this.speed = v; }
        public int getTurnSpeed() { return turnSpeed; }
        public void setTurnSpeed(int v) { this.turnSpeed = v; }
        public double getFireRate() { return fireRate; }
        public void setFireRate(double v) { this.fireRate = v; }
        public int getBulletSpeed() { return bulletSpeed; }
        public void setBulletSpeed(int v) { this.bulletSpeed = v; }
        public double getBulletDuration() { return bulletDuration; }
        public void setBulletDuration(double v) { this.bulletDuration = v; }
        public int getAmmo() { return ammo; }
        public void setAmmo(int v) { this.ammo = v; }
        public int getReloadTime() { return reloadTime; }
        public void setReloadTime(int v) { this.reloadTime = v; }
        public int getMaxDurability() { return maxDurability; }
        public void setMaxDurability(int v) { this.maxDurability = v; }
    }

    public static class InstalledMod {
        private int slotIndex;
        private String modType;
        private String source;

        public int getSlotIndex() { return slotIndex; }
        public void setSlotIndex(int i) { this.slotIndex = i; }
        public String getModType() { return modType; }
        public void setModType(String t) { this.modType = t; }
        public String getSource() { return source; }
        public void setSource(String s) { this.source = s; }
    }

    public static class CooldownSlot {
        private int slotIndex;
        private int remainingBattles;

        public int getSlotIndex() { return slotIndex; }
        public void setSlotIndex(int i) { this.slotIndex = i; }
        public int getRemainingBattles() { return remainingBattles; }
        public void setRemainingBattles(int r) { this.remainingBattles = r; }
    }

    public static class ModificationInventory {
        private Map<String, Integer> specificFragments = new HashMap<>();
        private int universalFragments;
        private List<String> craftedEquipments = new ArrayList<>();

        public Map<String, Integer> getSpecificFragments() { return specificFragments; }
        public void setSpecificFragments(Map<String, Integer> f) { this.specificFragments = f; }
        public int getUniversalFragments() { return universalFragments; }
        public void setUniversalFragments(int f) { this.universalFragments = f; }
        public List<String> getCraftedEquipments() { return craftedEquipments; }
        public void setCraftedEquipments(List<String> e) { this.craftedEquipments = e; }
    }

    public static class GachaState {
        private TankPoolState tankPool = new TankPoolState();
        private ModPoolState modPool = new ModPoolState();

        public TankPoolState getTankPool() { return tankPool; }
        public void setTankPool(TankPoolState p) { this.tankPool = p; }
        public ModPoolState getModPool() { return modPool; }
        public void setModPool(ModPoolState p) { this.modPool = p; }
    }

    public static class TankPoolState {
        private int pityCounter;
        private Map<Integer, Boolean> firstTimeBonus = new HashMap<>();

        public int getPityCounter() { return pityCounter; }
        public void setPityCounter(int c) { this.pityCounter = c; }
        public Map<Integer, Boolean> getFirstTimeBonus() { return firstTimeBonus; }
        public void setFirstTimeBonus(Map<Integer, Boolean> b) { this.firstTimeBonus = b; }
    }

    public static class ModPoolState {
        private int pityCounter;
        public int getPityCounter() { return pityCounter; }
        public void setPityCounter(int c) { this.pityCounter = c; }
    }

    public static class BattleHistory {
        private int totalPlayed, totalWins, totalLosses;
        private int totalShotsFired, totalDamageDealt, totalDamageReceived;
        private Map<String, ModeStats> byMode = new HashMap<>();
        {
            byMode.put("easy", new ModeStats());
            byMode.put("hard", new ModeStats());
            byMode.put("super", new ModeStats());
            byMode.put("pvp", new ModeStats());
        }
        public int getTotalPlayed() { return totalPlayed; }
        public void setTotalPlayed(int v) { this.totalPlayed = v; }
        public int getTotalWins() { return totalWins; }
        public void setTotalWins(int v) { this.totalWins = v; }
        public int getTotalLosses() { return totalLosses; }
        public void setTotalLosses(int v) { this.totalLosses = v; }
        public int getTotalShotsFired() { return totalShotsFired; }
        public void setTotalShotsFired(int v) { this.totalShotsFired = v; }
        public int getTotalDamageDealt() { return totalDamageDealt; }
        public void setTotalDamageDealt(int v) { this.totalDamageDealt = v; }
        public int getTotalDamageReceived() { return totalDamageReceived; }
        public void setTotalDamageReceived(int v) { this.totalDamageReceived = v; }
        public Map<String, ModeStats> getByMode() { return byMode; }
        public void setByMode(Map<String, ModeStats> m) { this.byMode = m; }
    }

    public static class ModeStats {
        private int played, wins;
        public int getPlayed() { return played; }
        public void setPlayed(int v) { this.played = v; }
        public int getWins() { return wins; }
        public void setWins(int v) { this.wins = v; }
    }

    public static class GameSettings {
        private double bgmVolume = 0.8;
        private double sfxVolume = 1.0;
        private String language = "zh_CN";
        private String defaultDifficulty = "hard";

        public double getBgmVolume() { return bgmVolume; }
        public void setBgmVolume(double v) { this.bgmVolume = v; }
        public double getSfxVolume() { return sfxVolume; }
        public void setSfxVolume(double v) { this.sfxVolume = v; }
        public String getLanguage() { return language; }
        public void setLanguage(String l) { this.language = l; }
        public String getDefaultDifficulty() { return defaultDifficulty; }
        public void setDefaultDifficulty(String d) { this.defaultDifficulty = d; }
    }
}
