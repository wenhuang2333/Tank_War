package util;

public class ResourceManager {
    public static final String IMG_ROOT = "resource/image/";

    // Battle
    public static final String BATTLE_BG = IMG_ROOT + "battle/battle_bg.png";
    public static final String TILE_GROUND = IMG_ROOT + "battle/tile_ground.png";
    public static final String WALL_BRICK = IMG_ROOT + "battle/wall_brick.png";
    public static final String WALL_BRICK_DAMAGED = IMG_ROOT + "battle/wall_brick_damaged.png";
    public static final String WALL_IRON = IMG_ROOT + "battle/wall_iron.png";

    // Bullet
    public static final String BULLET_ALL = IMG_ROOT + "bullet/bullet_all.png";

    // Effects
    public static final String LASER_BEAM = IMG_ROOT + "effect/laser_beam_segment.png";
    public static final String SHIELD = IMG_ROOT + "effect/shield_effect.png";

    public static String[] explosionFrames() {
        String[] frames = new String[8];
        for (int i = 0; i < 8; i++) {
            frames[i] = IMG_ROOT + "effect/explosion_" + String.format("%02d", i + 1) + ".png";
        }
        return frames;
    }

    // Tank body images
    private static final String[] TANK_NAMES = {
        "", "tank_cromwell", "tank_chaffee", "tank_firefly",
        "tank_joseph_is", "tank_jagdpanther", "tank_t34", "tank_panther", "tank_tiger"
    };

    public static String tankBody(int tankId) {
        if (tankId >= 1 && tankId < TANK_NAMES.length) {
            String bodyPath = IMG_ROOT + "tank/" + TANK_NAMES[tankId] + ".png";
            if (new java.io.File(bodyPath).exists()) return bodyPath;
            String iconPath = IMG_ROOT + "tank/" + TANK_NAMES[tankId] + "_icon.png";
            if (new java.io.File(iconPath).exists()) return iconPath;
            return bodyPath;
        }
        return null;
    }

    // UI backgrounds
    public static final String LOGIN_BG = IMG_ROOT + "login/login_bg.jpg";
    public static final String MENU_BG = IMG_ROOT + "menu/menu_bg.png";
    public static final String SELECT_BG = IMG_ROOT + "select/select_bg.png";
    public static final String GACHA_BG = IMG_ROOT + "gacha/gacha_bg.jpg";
    public static final String TRAINING_BG = IMG_ROOT + "training/training_bg.png";
    public static final String RESULT_VICTORY = IMG_ROOT + "result/result_victory_bg.png";
    public static final String RESULT_DEFEAT = IMG_ROOT + "result/result_defeat_bg.jpg";
    public static final String SETTINGS_BG = IMG_ROOT + "settings/settings_panel_bg.png";
}
