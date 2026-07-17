package util;

import model.vo.PlayerSaveData;

public class GameContext {
    public static PlayerSaveData currentSave;
    public static boolean isInBattle;
    public static String battleMode = "pve";
    public static String difficulty = "easy";
    public static int selectedMap = 1;
    public static int player1TankId = 1;
    public static int player2TankId = 2;
    public static boolean battleEnded = false;
    public static boolean playerWin = false;
    public static int p1ShotsFired, p2ShotsFired;
    public static int p1DamageDealt, p2DamageDealt;
    public static int p1DamageReceived, p2DamageReceived;

    // PVP battle settings
    public static int matchDuration = 300;
    public static boolean overtimeMode;
    public static boolean overtimePenalty;
    public static boolean cheatP1Invincible, cheatP2Invincible;
    public static boolean cheatP1WallPass, cheatP2WallPass;
    public static boolean cheatNoDurability;
    public static boolean cheatBulletRebound;
    public static boolean cheatFriendlyFire;

    // P1/P2 attribute cheat modifiers (0 = no modification)
    public static final AttributeMods cheatP1Mods = new AttributeMods();
    public static final AttributeMods cheatP2Mods = new AttributeMods();

    public static class AttributeMods {
        public int hp, attack, defense, speed, turnSpeed, bulletSpeed, ammo, reloadTime, durability;
    }
}
