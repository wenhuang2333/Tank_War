package model.vo;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import model.manager.ElementManager;
import model.manager.ElementFactory;
import model.load.ElementLoad;
import util.CollisionUtil;
import util.GameConfig;
import util.ResourceManager;

public class Players extends SuperElement {
    protected String customName;
    protected String modelName;
    protected int tankId;
    protected int rank;
    protected int level;

    protected int hp;
    protected int maxHp;
    protected int attack;
    protected int defense;
    protected int speed;
    protected int turnSpeed;
    protected double fireRate;
    protected long lastFireTime;
    protected int bulletSpeed;
    protected double bulletDuration;
    protected int ammo;
    protected int maxAmmo;
    protected int reloadTime;
    protected boolean isReloading;
    protected long reloadStartTime;
    protected int durability;
    protected int maxDurability;

    protected int upgradeAtkGain = 3, upgradeDefGain = 2, upgradeSpdGain = 1, upgradeHpGain = 20;
    protected int evolveAtkGain = 10, evolveDefGain = 8, evolveSpdGain = 3, evolveHpGain = 60;

    protected int unlockedSlots;
    protected Modification[] installedMods;
    protected int[] cooldownSlots;

    protected int shotsFired;
    protected int damageDealt;
    protected int damageReceived;

    protected boolean explosionProofUsed;
    protected long invincibleUntil;

    private boolean movingForward;
    private boolean movingBackward;
    private boolean rotatingCW;
    private boolean rotatingCCW;

    public Players() {
        super(0, 0, GameConfig.TANK_SIZE, GameConfig.TANK_SIZE);
        this.installedMods = new Modification[GameConfig.MOD_SLOTS];
        this.cooldownSlots = new int[GameConfig.MOD_SLOTS];
    }

    public Players(TankData data) {
        super(0, 0, GameConfig.TANK_SIZE, GameConfig.TANK_SIZE);
        this.tankId = data.getId();
        this.customName = data.getName();
        this.rank = 0;
        this.level = 1;
        this.hp = data.getBaseHp();
        this.maxHp = data.getBaseHp();
        this.attack = data.getBaseAttack();
        this.defense = data.getBaseDefense();
        this.speed = data.getBaseSpeed();
        this.turnSpeed = data.getBaseTurnSpeed();
        this.fireRate = data.getBaseFireRate() * 1000;
        this.lastFireTime = 0;
        this.bulletSpeed = data.getBaseBulletSpeed();
        this.bulletDuration = data.getBaseBulletDuration();
        this.ammo = data.getBaseAmmo();
        this.maxAmmo = data.getBaseAmmo();
        this.reloadTime = data.getBaseReloadTime() * 1000;
        this.isReloading = false;
        this.durability = data.getBaseDurability();
        this.maxDurability = data.getBaseDurability();
        this.upgradeAtkGain = data.getUpgradeAtkGain();
        this.upgradeDefGain = data.getUpgradeDefGain();
        this.upgradeSpdGain = data.getUpgradeSpdGain();
        this.upgradeHpGain = data.getUpgradeHpGain();
        this.evolveAtkGain = data.getEvolveAtkGain();
        this.evolveDefGain = data.getEvolveDefGain();
        this.evolveSpdGain = data.getEvolveSpdGain();
        this.evolveHpGain = data.getEvolveHpGain();
        this.installedMods = new Modification[GameConfig.MOD_SLOTS];
        this.cooldownSlots = new int[GameConfig.MOD_SLOTS];
        this.unlockedSlots = 0;
        this.explosionProofUsed = false;
        this.invincibleUntil = 0;
    }

    public void initFromTankData(TankData data) {
        this.tankId = data.getId();
        this.customName = data.getName();
        this.rank = 0;
        this.level = 1;
        this.hp = data.getBaseHp();
        this.maxHp = data.getBaseHp();
        this.attack = data.getBaseAttack();
        this.defense = data.getBaseDefense();
        this.speed = data.getBaseSpeed();
        this.turnSpeed = data.getBaseTurnSpeed();
        this.fireRate = data.getBaseFireRate() * 1000;
        this.bulletSpeed = data.getBaseBulletSpeed();
        this.bulletDuration = data.getBaseBulletDuration();
        this.ammo = data.getBaseAmmo();
        this.maxAmmo = data.getBaseAmmo();
        this.reloadTime = data.getBaseReloadTime() * 1000;
        this.durability = data.getBaseDurability();
        this.maxDurability = data.getBaseDurability();
        this.installedMods = new Modification[GameConfig.MOD_SLOTS];
        this.cooldownSlots = new int[GameConfig.MOD_SLOTS];
    }

    @Override
    public void show(Graphics2D g) {
        if (!visible) return;
        int cx = x + width / 2;
        int cy = y + height / 2;
        java.awt.geom.AffineTransform old = g.getTransform();
        g.rotate(Math.toRadians(direction), cx, cy);

        BufferedImage tankImg = ElementLoad.getInstance().getImage(ResourceManager.tankBody(tankId));
        // Check if image loaded successfully (not the magenta placeholder)
        boolean hasImage = tankImg != null && tankImg.getWidth() > 40;

        if (hasImage) {
            g.drawImage(tankImg, x, y, width, height, null);
        } else {
            g.setColor(new Color(60, 120, 60));
            g.fillRect(x, y, width, height);
            g.setColor(new Color(80, 150, 80));
            g.fillRect(x + 2, y + 2, width - 4, height - 4);
            g.setColor(new Color(40, 90, 40));
            g.fillRect(x + width / 2 - 4, y + 2, 8, 10);

            g.setColor(new Color(50, 50, 50));
            int barrelW = 6;
            g.fillRect(x + width / 2 - barrelW / 2, y - 8, barrelW, 16);
        }

        g.setTransform(old);

        long now = System.currentTimeMillis();
        if (now < invincibleUntil) {
            BufferedImage shield = ElementLoad.getInstance().getImage(ResourceManager.SHIELD);
            if (shield != null && shield.getWidth() > 10) {
                g.drawImage(shield, x - 6, y - 6, width + 12, height + 12, null);
            } else {
                g.setColor(new Color(100, 200, 255, 120));
                g.setStroke(new BasicStroke(3));
                g.drawOval(x - 4, y - 4, width + 8, height + 8);
            }
        }

        if (installedMods != null) {
            for (int i = 0; i < installedMods.length; i++) {
                if (installedMods[i] != null) {
                    int iconX = x + i * 8;
                    int iconY = y - 12;
                    g.setColor(cooldownSlots[i] > 0 ? Color.GRAY : Color.YELLOW);
                    g.fillRect(iconX, iconY, 6, 6);
                }
            }
        }
    }

    @Override
    public void update() {
        long now = System.currentTimeMillis();

        if (isReloading) {
            if (now - reloadStartTime >= reloadTime) {
                ammo = maxAmmo;
                isReloading = false;
            }
        }

        if (movingForward) moveForward();
        if (movingBackward) moveBackward();
        if (rotatingCW) rotateCW();
        if (rotatingCCW) rotateCCW();
    }

    public void moveForward() {
        int actualSpeed = (int)(speed * TankData.TANK_SPEED_FACTOR);
        double rad = Math.toRadians(direction);
        int dx = (int)(actualSpeed * Math.sin(rad));
        int dy = -(int)(actualSpeed * Math.cos(rad));
        if (!CollisionUtil.wouldCollideWithWall(x + dx, y + dy, width, height, this)) {
            x += dx;
            y += dy;
        }
    }

    public void moveBackward() {
        int actualSpeed = (int)(speed * TankData.TANK_SPEED_FACTOR);
        double rad = Math.toRadians(direction);
        int dx = -(int)(actualSpeed * Math.sin(rad));
        int dy = (int)(actualSpeed * Math.cos(rad));
        if (!CollisionUtil.wouldCollideWithWall(x + dx, y + dy, width, height, this)) {
            x += dx;
            y += dy;
        }
    }

    public void rotateCW() {
        if (hasMod(Modification.Type.INSTANT_TURN)) {
            direction = ((direction + 90) % 360 + 360) % 360;
        } else {
            direction += turnSpeed * TankData.TURN_SPEED_FACTOR;
            direction = ((direction % 360) + 360) % 360;
        }
    }

    public void rotateCCW() {
        if (hasMod(Modification.Type.INSTANT_TURN)) {
            direction = ((direction - 90) % 360 + 360) % 360;
        } else {
            direction -= turnSpeed * TankData.TURN_SPEED_FACTOR;
            direction = ((direction % 360) + 360) % 360;
        }
    }

    public void fire() {
        long now = System.currentTimeMillis();
        if (ammo <= 0 || isReloading) return;
        if (now - lastFireTime < fireRate) return;

        Modification weapon = getWeaponMod();
        int cx = x + width / 2;
        int cy = y + height / 2;

        if (weapon == null) {
            ElementFactory.getInstance().createNormalBullet(cx, cy, direction, this);
        } else {
            switch (weapon.getType()) {
                case LIGHT_MACHINE_GUN:
                    ElementFactory.getInstance().createSpreadBullets(cx, cy, direction, this, 3, 0.4f, 15);
                    break;
                case HEAVY_MACHINE_GUN:
                    ElementFactory.getInstance().createSpreadBullets(cx, cy, direction, this, 5, 0.3f, 15);
                    break;
                case LASER_CANNON:
                    ElementFactory.getInstance().createLaser(cx, cy, (int)direction, this, false, 0);
                    break;
                case DENSE_LASER:
                    ElementFactory.getInstance().createLaser(cx, cy, (int)direction, this, true, 5);
                    break;
                default:
                    ElementFactory.getInstance().createNormalBullet(cx, cy, direction, this);
                    break;
            }
        }

        ammo--;
        lastFireTime = now;
        shotsFired++;

        if (ammo <= 0 && !hasMod(Modification.Type.AUTO_LOADER)) {
            reload();
        }
    }

    public void reload() {
        if (isReloading) return;
        if (hasMod(Modification.Type.AUTO_LOADER)) {
            ammo = maxAmmo;
            return;
        }
        isReloading = true;
        reloadStartTime = System.currentTimeMillis();
    }

    public void takeDamage(int rawDamage) {
        long now = System.currentTimeMillis();
        if (now < invincibleUntil) return;

        float reduction = getDamageReduction();
        int actualDamage = rawDamage - (int)(defense * reduction);
        if (actualDamage < 0) actualDamage = 0;

        if (hp - actualDamage <= 0 && hasMod(Modification.Type.EXPLOSION_PROOF) && !explosionProofUsed) {
            hp = 1;
            invincibleUntil = now + 5000;
            explosionProofUsed = true;
            durability = Math.max(0, durability - rawDamage);
            damageReceived += rawDamage;
            return;
        }

        hp -= actualDamage;
        durability = Math.max(0, durability - rawDamage);
        damageReceived += rawDamage;

        if (hp <= 0) {
            hp = 0;
            destroy();
            ElementManager.getInstance().addElement("explosion",
                new Explosion(getCenterX(), getCenterY()));
        }
    }

    private float getDamageReduction() {
        float ratio = (float) durability / maxDurability;
        if (ratio > 0.75f) return 1.0f;
        if (ratio > 0.50f) return 0.5f;
        if (ratio > 0.25f) return 0.25f;
        return 0.0f;
    }

    public Modification getWeaponMod() {
        if (installedMods == null) return null;
        for (Modification mod : installedMods) {
            if (mod != null && Modification.isWeapon(mod.getType())) {
                return mod;
            }
        }
        return null;
    }

    public boolean hasMod(Modification.Type type) {
        if (installedMods == null) return false;
        for (Modification mod : installedMods) {
            if (mod != null && mod.getType() == type) return true;
        }
        return false;
    }

    public void installMod(Modification mod, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= GameConfig.MOD_SLOTS) return;
        if (cooldownSlots[slotIndex] > 0) return;
        if (slotIndex >= unlockedSlots) return;
        if (mod != null && Modification.isWeapon(mod.getType())) {
            for (int i = 0; i < installedMods.length; i++) {
                if (i != slotIndex && installedMods[i] != null && Modification.isWeapon(installedMods[i].getType())) {
                    return;
                }
            }
        }
        installedMods[slotIndex] = mod;
        if (mod != null && mod.getType() == Modification.Type.EXTRA_AMMO) {
            maxAmmo = getBaseAmmoFromId() * 2;
        }
    }

    private int getBaseAmmoFromId() {
        int[] baseAmmos = {5, 7, 10, 3, 5, 15, 4, 4};
        return (tankId >= 1 && tankId <= 8) ? baseAmmos[tankId - 1] : maxAmmo;
    }

    public void removeMod(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= GameConfig.MOD_SLOTS) return;
        if (installedMods[slotIndex] != null && installedMods[slotIndex].getType() == Modification.Type.EXTRA_AMMO) {
            maxAmmo = getBaseAmmoFromId();
            if (ammo > maxAmmo) ammo = maxAmmo;
        }
        installedMods[slotIndex] = null;
        cooldownSlots[slotIndex] = GameConfig.MOD_COOLDOWN_BATTLES;
    }

    public boolean upgrade(int playerIron) {
        if (level >= GameConfig.MAX_LEVEL) return false;
        int cost = rank * 100 + level * 10;
        if (playerIron < cost) return false;
        level++;
        attack += upgradeAtkGain;
        defense += upgradeDefGain;
        speed += upgradeSpdGain;
        maxHp += upgradeHpGain;
        hp = maxHp;
        return true;
    }

    public int getUpgradeCost() {
        return rank * 100 + level * 10;
    }

    public boolean evolve(int playerSteel) {
        if (level != GameConfig.MAX_LEVEL || rank >= GameConfig.MAX_RANK) return false;
        int cost = (rank + 1) * 10;
        if (playerSteel < cost) return false;
        rank++;
        level = 1;
        attack += evolveAtkGain;
        defense += evolveDefGain;
        speed += evolveSpdGain;
        maxHp += evolveHpGain;
        hp = maxHp;
        checkUnlockModSlot();
        return true;
    }

    public int getEvolveCost() {
        return (rank + 1) * 10;
    }

    private void checkUnlockModSlot() {
        if (rank < 3) unlockedSlots = 0;
        else if (rank <= 4) unlockedSlots = 1;
        else if (rank <= 6) unlockedSlots = 2;
        else if (rank <= 9) unlockedSlots = 3;
        else unlockedSlots = 5;
    }

    public String computeModelName() {
        if (unlockedSlots == 0) return "";
        char slotLetter = (char)('A' + unlockedSlots - 1);
        int installedCount = 0;
        if (installedMods != null) {
            for (Modification m : installedMods) {
                if (m != null) installedCount++;
            }
        }
        String[] romans = {"", "I", "II", "III", "IV", "V"};
        return slotLetter + "-" + romans[Math.min(installedCount, 5)];
    }

    public void advanceCooldowns() {
        for (int i = 0; i < cooldownSlots.length; i++) {
            if (cooldownSlots[i] > 0) cooldownSlots[i]--;
        }
    }

    public void resetBattleState() {
        explosionProofUsed = false;
        invincibleUntil = 0;
        shotsFired = 0;
        damageDealt = 0;
        damageReceived = 0;
    }

    // Getters and setters
    public int getTankId() { return tankId; }
    public void setTankId(int tankId) { this.tankId = tankId; }
    public String getCustomName() { return customName; }
    public void setCustomName(String name) { this.customName = name; }
    public String getModelName() { return modelName; }
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }
    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }
    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
    public int getTurnSpeed() { return turnSpeed; }
    public void setTurnSpeed(int turnSpeed) { this.turnSpeed = turnSpeed; }
    public double getFireRate() { return fireRate; }
    public long getLastFireTime() { return lastFireTime; }
    public void setLastFireTime(long t) { this.lastFireTime = t; }
    public int getBulletSpeed() { return bulletSpeed; }
    public void setBulletSpeed(int s) { this.bulletSpeed = s; }
    public double getBulletDuration() { return bulletDuration; }
    public int getAmmo() { return ammo; }
    public void setAmmo(int ammo) { this.ammo = ammo; }
    public int getMaxAmmo() { return maxAmmo; }
    public int getReloadTime() { return reloadTime; }
    public boolean isReloading() { return isReloading; }
    public long getReloadStartTime() { return reloadStartTime; }
    public void setReloading(boolean r) { this.isReloading = r; }
    public void setReloadStartTime(long t) { this.reloadStartTime = t; }
    public int getDurability() { return durability; }
    public void setDurability(int d) { this.durability = d; }
    public int getMaxDurability() { return maxDurability; }
    public int getUnlockedSlots() { return unlockedSlots; }
    public void setUnlockedSlots(int s) { this.unlockedSlots = s; }
    public Modification[] getInstalledMods() { return installedMods; }
    public int[] getCooldownSlots() { return cooldownSlots; }
    public int getShotsFired() { return shotsFired; }
    public void setShotsFired(int s) { this.shotsFired = s; }
    public int getDamageDealt() { return damageDealt; }
    public void setDamageDealt(int d) { this.damageDealt = d; }
    public int getDamageReceived() { return damageReceived; }
    public void setDamageReceived(int d) { this.damageReceived = d; }
    public boolean isMovingForward() { return movingForward; }
    public void setMovingForward(boolean f) { this.movingForward = f; }
    public boolean isMovingBackward() { return movingBackward; }
    public void setMovingBackward(boolean b) { this.movingBackward = b; }
    public boolean isRotatingCW() { return rotatingCW; }
    public void setRotatingCW(boolean cw) { this.rotatingCW = cw; }
    public boolean isRotatingCCW() { return rotatingCCW; }
    public void setRotatingCCW(boolean ccw) { this.rotatingCCW = ccw; }
    public boolean isExplosionProofUsed() { return explosionProofUsed; }
    public void setExplosionProofUsed(boolean u) { this.explosionProofUsed = u; }
    public long getInvincibleUntil() { return invincibleUntil; }
    public void setInvincibleUntil(long t) { this.invincibleUntil = t; }
}
