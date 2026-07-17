package model.vo;

public class TankData {
    public static final double TANK_SPEED_FACTOR = 0.04;
    public static final double TURN_SPEED_FACTOR = 0.01;
    public static final double BULLET_SPEED_FACTOR = 0.06;
    public static final double LASER_SPEED_FACTOR = 0.6;

    private int id;
    private String name;
    private int baseHp, baseAttack, baseDefense, baseSpeed;
    private int baseTurnSpeed;
    private double baseFireRate;
    private int baseBulletSpeed;
    private double baseBulletDuration;
    private int baseAmmo;
    private int baseReloadTime;
    private int baseDurability;
    private int upgradeAtkGain = 3, upgradeDefGain = 2, upgradeSpdGain = 1, upgradeHpGain = 20;
    private int evolveAtkGain = 10, evolveDefGain = 8, evolveSpdGain = 3, evolveHpGain = 60;

    public TankData() {}

    public TankData(int id, String name, int hp, int atk, int def, int spd, int turnSpd,
                    double fireRate, int bulletSpd, double bulletDur, int ammo, int reload, int dura) {
        this.id = id;
        this.name = name;
        this.baseHp = hp;
        this.baseAttack = atk;
        this.baseDefense = def;
        this.baseSpeed = spd;
        this.baseTurnSpeed = turnSpd;
        this.baseFireRate = fireRate;
        this.baseBulletSpeed = bulletSpd;
        this.baseBulletDuration = bulletDur;
        this.baseAmmo = ammo;
        this.baseReloadTime = reload;
        this.baseDurability = dura;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getBaseHp() { return baseHp; }
    public int getBaseAttack() { return baseAttack; }
    public int getBaseDefense() { return baseDefense; }
    public int getBaseSpeed() { return baseSpeed; }
    public int getBaseTurnSpeed() { return baseTurnSpeed; }
    public double getBaseFireRate() { return baseFireRate; }
    public int getBaseBulletSpeed() { return baseBulletSpeed; }
    public double getBaseBulletDuration() { return baseBulletDuration; }
    public int getBaseAmmo() { return baseAmmo; }
    public int getBaseReloadTime() { return baseReloadTime; }
    public int getBaseDurability() { return baseDurability; }
    public int getUpgradeAtkGain() { return upgradeAtkGain; }
    public int getUpgradeDefGain() { return upgradeDefGain; }
    public int getUpgradeSpdGain() { return upgradeSpdGain; }
    public int getUpgradeHpGain() { return upgradeHpGain; }
    public int getEvolveAtkGain() { return evolveAtkGain; }
    public int getEvolveDefGain() { return evolveDefGain; }
    public int getEvolveSpdGain() { return evolveSpdGain; }
    public int getEvolveHpGain() { return evolveHpGain; }
}
