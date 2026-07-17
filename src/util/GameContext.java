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
}
