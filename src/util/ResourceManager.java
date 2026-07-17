package util;

public class ResourceManager {
    public static final String IMG_ROOT = "resource/image/";

    // ==================== Common UI (common/) ====================
    public static final String COMMON_BTN_NORMAL = IMG_ROOT + "common/btn_normal.png";
    public static final String COMMON_BTN_HOVER = IMG_ROOT + "common/btn_hover.png";
    public static final String COMMON_BTN_PRESSED = IMG_ROOT + "common/btn_pressed.png";
    public static final String COMMON_BTN_DISABLED = IMG_ROOT + "common/btn_disabled.png";
    public static final String COMMON_PANEL_BG = IMG_ROOT + "common/panel_bg.png";
    public static final String COMMON_ARROW_LEFT = IMG_ROOT + "common/arrow_left.png";
    public static final String COMMON_ARROW_RIGHT = IMG_ROOT + "common/arrow_right.png";
    public static final String COMMON_STAR_FILLED = IMG_ROOT + "common/star_filled.png";
    public static final String COMMON_STAR_EMPTY = IMG_ROOT + "common/star_empty.png";
    public static final String COMMON_CHECKBOX_ON = IMG_ROOT + "common/checkbox_on.png";
    public static final String COMMON_CHECKBOX_OFF = IMG_ROOT + "common/checkbox_off.png";

    // ==================== Login (login/) ====================
    public static final String LOGIN_BG = IMG_ROOT + "login/login_bg.jpg";
    public static final String LOGIN_LOGO = IMG_ROOT + "login/login_logo.png";
    public static final String LOGIN_BTN_LOGIN_NORMAL = IMG_ROOT + "login/btn_login_normal.png";
    public static final String LOGIN_BTN_LOGIN_HOVER = IMG_ROOT + "login/btn_login_hover.png";
    public static final String LOGIN_BTN_LOGIN_PRESSED = IMG_ROOT + "login/btn_login_pressed.png";
    public static final String LOGIN_BTN_EXIT_NORMAL = IMG_ROOT + "login/btn_exit_normal.png";
    public static final String LOGIN_BTN_EXIT_HOVER = IMG_ROOT + "login/btn_exit_hover.png";
    public static final String LOGIN_BTN_EXIT_PRESSED = IMG_ROOT + "login/btn_exit_pressed.png";
    public static final String LOGIN_BTN_NEW_SAVE = IMG_ROOT + "login/btn_new_save.png";
    public static final String LOGIN_SAVE_SLOT_BG = IMG_ROOT + "login/save_slot_bg.png";
    public static final String LOGIN_SAVE_SLOT_EMPTY = IMG_ROOT + "login/save_slot_empty.png";

    // ==================== Menu (menu/) ====================
    public static final String MENU_BG = IMG_ROOT + "menu/menu_bg.png";
    public static final String MENU_BTN_BATTLE = IMG_ROOT + "menu/btn_battle.png";
    public static final String MENU_BTN_TRAINING = IMG_ROOT + "menu/btn_training.png";
    public static final String MENU_BTN_GACHA = IMG_ROOT + "menu/btn_gacha.png";
    public static final String MENU_BTN_EXIT_GAME = IMG_ROOT + "menu/btn_exit_game.png";

    // ==================== Select (select/) ====================
    public static final String SELECT_BG = IMG_ROOT + "select/select_bg.png";
    public static final String SELECT_MAP_SELECTED_BORDER = IMG_ROOT + "select/map_selected_border.png";
    public static final String SELECT_TANK_CARD_SELECTED = IMG_ROOT + "select/tank_card_selected.png";
    public static final String SELECT_BTN_PVE = IMG_ROOT + "select/btn_pve.png";
    public static final String SELECT_BTN_PVP = IMG_ROOT + "select/btn_pvp.png";
    public static final String SELECT_BTN_EASY = IMG_ROOT + "select/btn_easy.png";
    public static final String SELECT_BTN_HARD = IMG_ROOT + "select/btn_hard.png";
    public static final String SELECT_BTN_SUPER = IMG_ROOT + "select/btn_super.png";
    public static final String SELECT_BTN_START_BATTLE = IMG_ROOT + "select/btn_start_battle.png";
    public static final String SELECT_P1_INDICATOR = IMG_ROOT + "select/p1_indicator.png";
    public static final String SELECT_P2_INDICATOR = IMG_ROOT + "select/p2_indicator.png";

    // Map thumbnails (pic.md says map/, actual is mapimage/)
    public static String mapThumb(int level) {
        return IMG_ROOT + "mapimage/level" + String.format("%02d", level) + ".png";
    }

    // ==================== Battle (battle/) ====================
    public static final String BATTLE_BG = IMG_ROOT + "battle/battle_bg.png";
    public static final String TILE_GROUND = IMG_ROOT + "battle/tile_ground.png";
    public static final String WALL_BRICK = IMG_ROOT + "battle/wall_brick.png";
    public static final String WALL_BRICK_DAMAGED = IMG_ROOT + "battle/wall_brick_damaged.png";
    public static final String WALL_IRON = IMG_ROOT + "battle/wall_iron.png";
    public static final String BATTLE_PAUSE_OVERLAY = IMG_ROOT + "battle/pause_overlay.png";
    public static final String BATTLE_BTN_CONTINUE = IMG_ROOT + "battle/btn_continue.png";
    public static final String BATTLE_BTN_RESTART = IMG_ROOT + "battle/btn_restart.png";
    public static final String BATTLE_BTN_END = IMG_ROOT + "battle/btn_end_battle.png";
    public static final String BATTLE_COUNTDOWN_3 = IMG_ROOT + "battle/countdown_3.png";
    public static final String BATTLE_COUNTDOWN_2 = IMG_ROOT + "battle/countdown_2.png";
    public static final String BATTLE_COUNTDOWN_1 = IMG_ROOT + "battle/countdown_1.png";
    public static final String BATTLE_COUNTDOWN_GO = IMG_ROOT + "battle/countdown_go.png";

    // ==================== Tank (tank/) ====================
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

    public static String tankIcon(int tankId) {
        if (tankId >= 1 && tankId < TANK_NAMES.length) {
            return IMG_ROOT + "tank/" + TANK_NAMES[tankId] + "_icon.png";
        }
        return null;
    }

    // ==================== Bullet (bullet/) ====================
    public static final String BULLET_ALL = IMG_ROOT + "bullet/bullet_all.png";

    // ==================== Mod (mod/) ====================
    public static final String MOD_ANTI_FRIENDLY_FIRE = IMG_ROOT + "mod/mod_anti_friendly_fire.png";
    public static final String MOD_INSTANT_TURN = IMG_ROOT + "mod/mod_instant_turn.png";
    public static final String MOD_EXPLOSION_PROOF = IMG_ROOT + "mod/mod_explosion_proof.png";
    public static final String MOD_LIGHT_MG = IMG_ROOT + "mod/mod_light_mg.png";
    public static final String MOD_HEAVY_MG = IMG_ROOT + "mod/mod_heavy_mg.png";
    public static final String MOD_LASER_CANNON = IMG_ROOT + "mod/mod_laser_cannon.png";
    public static final String MOD_DENSE_LASER = IMG_ROOT + "mod/mod_dense_laser.png";
    public static final String MOD_AUTO_LOADER = IMG_ROOT + "mod/mod_auto_loader.png";
    public static final String MOD_EXTRA_AMMO = IMG_ROOT + "mod/mod_extra_ammo.png";
    public static final String MOD_FRAGMENT = IMG_ROOT + "mod/mod_fragment.png";

    public static String modIcon(String modTypeName) {
        return IMG_ROOT + "mod/mod_" + modTypeName.toLowerCase() + ".png";
    }

    // ==================== Gacha (gacha/) ====================
    public static final String GACHA_BG = IMG_ROOT + "gacha/gacha_bg.jpg";
    public static final String GACHA_TANK_POOL_BG = IMG_ROOT + "gacha/gacha_tank_pool_bg.png";
    public static final String GACHA_MOD_POOL_BG = IMG_ROOT + "gacha/gacha_mod_pool_bg.png";
    public static final String GACHA_BTN_PULL_ONE = IMG_ROOT + "gacha/btn_pull_one.png";
    public static final String GACHA_BTN_PULL_TEN = IMG_ROOT + "gacha/btn_pull_ten.png";
    public static final String GACHA_ANIMATION_BG = IMG_ROOT + "gacha/gacha_animation_bg.jpg";
    public static final String GACHA_CARD_BACK = IMG_ROOT + "gacha/gacha_card_back.png";
    public static final String GACHA_BTN_FLIP_ALL = IMG_ROOT + "gacha/btn_flip_all.png";
    public static final String GACHA_LIGHT_1 = IMG_ROOT + "gacha/gacha_light_1.png";
    public static final String GACHA_LIGHT_2 = IMG_ROOT + "gacha/gacha_light_2.png";
    public static final String GACHA_CARD_TANK = IMG_ROOT + "gacha/gacha_card_tank.png";
    public static final String GACHA_CARD_MOD = IMG_ROOT + "gacha/gacha_card_mod.jpg";
    public static final String GACHA_CARD_RESOURCE = IMG_ROOT + "gacha/gacha_card_resource.png";
    public static final String GACHA_PITY_BAR_BG = IMG_ROOT + "gacha/gacha_pity_bar_bg.png";
    public static final String GACHA_PITY_BAR_FILL = IMG_ROOT + "gacha/gacha_pity_bar_fill.png";

    // ==================== Result (result/) ====================
    public static final String RESULT_VICTORY = IMG_ROOT + "result/result_victory_bg.png";
    public static final String RESULT_DEFEAT = IMG_ROOT + "result/result_defeat_bg.jpg";
    public static final String RESULT_DRAW = IMG_ROOT + "result/result_draw_bg.jpg";
    public static final String RESULT_VICTORY_TEXT = IMG_ROOT + "result/result_victory_text.png";
    public static final String RESULT_DEFEAT_TEXT = IMG_ROOT + "result/result_defeat_text.png";
    public static final String RESULT_BTN_RETURN_MENU = IMG_ROOT + "result/btn_return_menu.png";

    // ==================== Effect (effect/) ====================
    public static final String LASER_BEAM = IMG_ROOT + "effect/laser_beam_segment.png";
    public static final String SHIELD = IMG_ROOT + "effect/shield_effect.png";

    public static String[] explosionFrames() {
        String[] frames = new String[8];
        for (int i = 0; i < 8; i++) {
            frames[i] = IMG_ROOT + "effect/explosion_" + String.format("%02d", i + 1) + ".png";
        }
        return frames;
    }

    public static String[] hitSparkFrames() {
        String[] frames = new String[4];
        for (int i = 0; i < 4; i++) {
            frames[i] = IMG_ROOT + "effect/hit_spark_" + String.format("%02d", i + 1) + ".png";
        }
        return frames;
    }

    public static String[] wallDestroyFrames() {
        String[] frames = new String[4];
        for (int i = 0; i < 4; i++) {
            frames[i] = IMG_ROOT + "effect/wall_destroy_" + String.format("%02d", i + 1) + ".png";
        }
        return frames;
    }

    // ==================== HUD (hud/) ====================
    public static final String HUD_ALL = IMG_ROOT + "hud/hud_all.png";

    // ==================== Training (training/) ====================
    public static final String TRAINING_BG = IMG_ROOT + "training/training_bg.png";
    public static final String TRAINING_TANK_DISPLAY_BG = IMG_ROOT + "training/training_tank_display_bg.png";
    public static final String TRAINING_BTN_UPGRADE = IMG_ROOT + "training/btn_upgrade.png";
    public static final String TRAINING_BTN_RANK_UP = IMG_ROOT + "training/btn_rank_up.png";
    public static final String TRAINING_BTN_UPGRADE_DISABLED = IMG_ROOT + "training/btn_upgrade_disabled.png";
    public static final String TRAINING_BTN_RANK_UP_DISABLED = IMG_ROOT + "training/btn_rank_up_disabled.png";
    public static final String TRAINING_BTN_VIEW_DETAIL = IMG_ROOT + "training/btn_view_detail.png";
    public static final String TRAINING_BTN_SYNTHESIZE = IMG_ROOT + "training/btn_synthesize.png";
    public static final String TRAINING_BTN_SYNTHESIZE_DISABLED = IMG_ROOT + "training/btn_synthesize_disabled.png";
    public static final String TRAINING_BTN_INSTALL_MOD = IMG_ROOT + "training/btn_install_mod.png";
    public static final String TRAINING_BTN_DISMANTLE_MOD = IMG_ROOT + "training/btn_dismantle_mod.png";
    public static final String TRAINING_DETAIL_TAB_STATS = IMG_ROOT + "training/detail_tab_stats.png";
    public static final String TRAINING_DETAIL_TAB_TRAINING = IMG_ROOT + "training/detail_tab_training.png";
    public static final String TRAINING_DETAIL_TAB_MOD = IMG_ROOT + "training/detail_tab_mod.png";
    public static final String TRAINING_ATTR_PANEL_BG = IMG_ROOT + "training/attr_panel_bg.png";
    public static final String TRAINING_PROGRESS_BAR_BG = IMG_ROOT + "training/progress_bar_bg.png";
    public static final String TRAINING_PROGRESS_BAR_FILL = IMG_ROOT + "training/progress_bar_fill.png";

    // ==================== Settings (settings/) ====================
    public static final String SETTINGS_BG = IMG_ROOT + "settings/settings_panel_bg.png";
    public static final String SETTINGS_BTN_TIME_3MIN = IMG_ROOT + "settings/btn_time_3min.png";
    public static final String SETTINGS_BTN_TIME_5MIN = IMG_ROOT + "settings/btn_time_5min.png";
    public static final String SETTINGS_BTN_TIME_10MIN = IMG_ROOT + "settings/btn_time_10min.png";
    public static final String SETTINGS_BTN_OVERTIME_ON = IMG_ROOT + "settings/btn_overtime_on.png";
    public static final String SETTINGS_BTN_OVERTIME_OFF = IMG_ROOT + "settings/btn_overtime_off.png";
    public static final String SETTINGS_BTN_PENALTY_ON = IMG_ROOT + "settings/btn_penalty_on.png";
    public static final String SETTINGS_BTN_PENALTY_OFF = IMG_ROOT + "settings/btn_penalty_off.png";
    public static final String SETTINGS_BTN_CHEAT_ON = IMG_ROOT + "settings/btn_cheat_on.png";
    public static final String SETTINGS_BTN_CHEAT_OFF = IMG_ROOT + "settings/btn_cheat_off.png";
    public static final String SETTINGS_CHEAT_PANEL_BG = IMG_ROOT + "settings/cheat_panel_bg.png";

    // ==================== Wall extras (wall/) ====================
    public static final String WALL_BASE = IMG_ROOT + "wall/base.png";
    public static final String WALL_BREAK_BASE = IMG_ROOT + "wall/break_base.png";
    public static final String WALL_BRICK_ALT = IMG_ROOT + "wall/brick.png";
    public static final String WALL_GRASS = IMG_ROOT + "wall/grass.png";
    public static final String WALL_IRON_ALT = IMG_ROOT + "wall/iron.png";
    public static final String WALL_RIVER = IMG_ROOT + "wall/river.png";

    // ==================== Boom (boom/) ====================
    public static final String BOOM = IMG_ROOT + "boom/boom.png";
}
