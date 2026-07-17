package model.load;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import javax.imageio.ImageIO;

import util.ResourceManager;

public class ElementLoad {
    private static ElementLoad instance;
    private Map<Integer, MapData> mapDataCache;
    private Map<String, BufferedImage> imageCache;
    private static BufferedImage PLACEHOLDER;

    static {
        PLACEHOLDER = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = PLACEHOLDER.createGraphics();
        g.setColor(java.awt.Color.MAGENTA);
        g.fillRect(0, 0, 40, 40);
        g.setColor(java.awt.Color.BLACK);
        g.drawLine(0, 0, 40, 40);
        g.drawLine(40, 0, 0, 40);
        g.dispose();
    }

    private ElementLoad() {
        mapDataCache = new HashMap<>();
        imageCache = new HashMap<>();
    }

    public static ElementLoad getInstance() {
        if (instance == null) {
            synchronized (ElementLoad.class) {
                if (instance == null) {
                    instance = new ElementLoad();
                }
            }
        }
        return instance;
    }

    public void init() {
        for (int i = 1; i <= 6; i++) {
            try {
                loadMap(i);
            } catch (Exception e) {
                System.err.println("[WARN] Failed to load map " + i + ": " + e.getMessage());
            }
        }
        preloadBattleImages();
    }

    /** Load image only if file exists — no warning on missing. */
    private void preloadIfExists(String path) {
        if (path != null && new java.io.File(path).exists()) {
            loadImage(path);
        }
    }

    private void preloadBattleImages() {
        // Battle scene
        preloadIfExists(ResourceManager.BATTLE_BG);
        preloadIfExists(ResourceManager.TILE_GROUND);
        preloadIfExists(ResourceManager.WALL_BRICK);
        preloadIfExists(ResourceManager.WALL_IRON);
        preloadIfExists(ResourceManager.WALL_BRICK_DAMAGED);
        preloadIfExists(ResourceManager.BULLET_ALL);
        preloadIfExists(ResourceManager.LASER_BEAM);
        preloadIfExists(ResourceManager.SHIELD);
        preloadIfExists(ResourceManager.BOOM);
        preloadIfExists(ResourceManager.HUD_ALL);

        // Explosion frames
        for (String frame : ResourceManager.explosionFrames()) preloadIfExists(frame);
        // Hit spark frames
        for (String frame : ResourceManager.hitSparkFrames()) preloadIfExists(frame);
        // Wall destroy frames
        for (String frame : ResourceManager.wallDestroyFrames()) preloadIfExists(frame);

        // Tank images (check existence: tanks 1-3 have body PNGs, tanks 4-8 have icon PNGs)
        for (int i = 1; i <= 8; i++) {
            preloadIfExists(ResourceManager.tankBody(i));
            preloadIfExists(ResourceManager.tankIcon(i));
        }

        // UI panel backgrounds
        preloadIfExists(ResourceManager.LOGIN_BG);
        preloadIfExists(ResourceManager.LOGIN_LOGO);
        preloadIfExists(ResourceManager.MENU_BG);
        preloadIfExists(ResourceManager.SELECT_BG);
        preloadIfExists(ResourceManager.GACHA_BG);
        preloadIfExists(ResourceManager.TRAINING_BG);
        preloadIfExists(ResourceManager.SETTINGS_BG);
        preloadIfExists(ResourceManager.RESULT_VICTORY);
        preloadIfExists(ResourceManager.RESULT_DEFEAT);
        preloadIfExists(ResourceManager.RESULT_DRAW);

        // Common UI
        preloadIfExists(ResourceManager.COMMON_BTN_NORMAL);
        preloadIfExists(ResourceManager.COMMON_BTN_HOVER);
        preloadIfExists(ResourceManager.COMMON_BTN_PRESSED);
        preloadIfExists(ResourceManager.COMMON_BTN_DISABLED);
        preloadIfExists(ResourceManager.COMMON_PANEL_BG);
        preloadIfExists(ResourceManager.COMMON_ARROW_LEFT);
        preloadIfExists(ResourceManager.COMMON_ARROW_RIGHT);
        preloadIfExists(ResourceManager.COMMON_STAR_FILLED);
        preloadIfExists(ResourceManager.COMMON_STAR_EMPTY);

        // Battle UI
        preloadIfExists(ResourceManager.BATTLE_PAUSE_OVERLAY);
        preloadIfExists(ResourceManager.BATTLE_BTN_CONTINUE);
        preloadIfExists(ResourceManager.BATTLE_BTN_RESTART);
        preloadIfExists(ResourceManager.BATTLE_BTN_END);
        preloadIfExists(ResourceManager.BATTLE_COUNTDOWN_3);
        preloadIfExists(ResourceManager.BATTLE_COUNTDOWN_2);
        preloadIfExists(ResourceManager.BATTLE_COUNTDOWN_1);
        preloadIfExists(ResourceManager.BATTLE_COUNTDOWN_GO);

        // Login buttons
        preloadIfExists(ResourceManager.LOGIN_BTN_LOGIN_NORMAL);
        preloadIfExists(ResourceManager.LOGIN_BTN_LOGIN_HOVER);
        preloadIfExists(ResourceManager.LOGIN_BTN_EXIT_NORMAL);
        preloadIfExists(ResourceManager.LOGIN_BTN_EXIT_HOVER);
        preloadIfExists(ResourceManager.LOGIN_BTN_NEW_SAVE);
        preloadIfExists(ResourceManager.LOGIN_SAVE_SLOT_BG);
        preloadIfExists(ResourceManager.LOGIN_SAVE_SLOT_EMPTY);

        // Menu buttons
        preloadIfExists(ResourceManager.MENU_BTN_BATTLE);
        preloadIfExists(ResourceManager.MENU_BTN_TRAINING);
        preloadIfExists(ResourceManager.MENU_BTN_GACHA);
        preloadIfExists(ResourceManager.MENU_BTN_EXIT_GAME);

        // Select
        preloadIfExists(ResourceManager.SELECT_MAP_SELECTED_BORDER);
        preloadIfExists(ResourceManager.SELECT_TANK_CARD_SELECTED);
        preloadIfExists(ResourceManager.SELECT_BTN_PVE);
        preloadIfExists(ResourceManager.SELECT_BTN_PVP);
        preloadIfExists(ResourceManager.SELECT_BTN_EASY);
        preloadIfExists(ResourceManager.SELECT_BTN_HARD);
        preloadIfExists(ResourceManager.SELECT_BTN_SUPER);
        preloadIfExists(ResourceManager.SELECT_BTN_START_BATTLE);
        preloadIfExists(ResourceManager.SELECT_P1_INDICATOR);
        preloadIfExists(ResourceManager.SELECT_P2_INDICATOR);
        for (int i = 1; i <= 10; i++) preloadIfExists(ResourceManager.mapThumb(i));

        // Gacha
        preloadIfExists(ResourceManager.GACHA_TANK_POOL_BG);
        preloadIfExists(ResourceManager.GACHA_MOD_POOL_BG);
        preloadIfExists(ResourceManager.GACHA_BTN_PULL_ONE);
        preloadIfExists(ResourceManager.GACHA_BTN_PULL_TEN);
        preloadIfExists(ResourceManager.GACHA_ANIMATION_BG);
        preloadIfExists(ResourceManager.GACHA_CARD_BACK);
        preloadIfExists(ResourceManager.GACHA_BTN_FLIP_ALL);
        preloadIfExists(ResourceManager.GACHA_LIGHT_1);
        preloadIfExists(ResourceManager.GACHA_LIGHT_2);
        preloadIfExists(ResourceManager.GACHA_CARD_TANK);
        preloadIfExists(ResourceManager.GACHA_CARD_MOD);
        preloadIfExists(ResourceManager.GACHA_CARD_RESOURCE);
        preloadIfExists(ResourceManager.GACHA_PITY_BAR_BG);
        preloadIfExists(ResourceManager.GACHA_PITY_BAR_FILL);

        // Result
        preloadIfExists(ResourceManager.RESULT_VICTORY_TEXT);
        preloadIfExists(ResourceManager.RESULT_DEFEAT_TEXT);
        preloadIfExists(ResourceManager.RESULT_BTN_RETURN_MENU);

        // Training
        preloadIfExists(ResourceManager.TRAINING_TANK_DISPLAY_BG);
        preloadIfExists(ResourceManager.TRAINING_BTN_UPGRADE);
        preloadIfExists(ResourceManager.TRAINING_BTN_RANK_UP);
        preloadIfExists(ResourceManager.TRAINING_BTN_UPGRADE_DISABLED);
        preloadIfExists(ResourceManager.TRAINING_BTN_RANK_UP_DISABLED);
        preloadIfExists(ResourceManager.TRAINING_BTN_VIEW_DETAIL);
        preloadIfExists(ResourceManager.TRAINING_BTN_SYNTHESIZE);
        preloadIfExists(ResourceManager.TRAINING_BTN_SYNTHESIZE_DISABLED);
        preloadIfExists(ResourceManager.TRAINING_BTN_INSTALL_MOD);
        preloadIfExists(ResourceManager.TRAINING_BTN_DISMANTLE_MOD);
        preloadIfExists(ResourceManager.TRAINING_DETAIL_TAB_STATS);
        preloadIfExists(ResourceManager.TRAINING_DETAIL_TAB_TRAINING);
        preloadIfExists(ResourceManager.TRAINING_DETAIL_TAB_MOD);
        preloadIfExists(ResourceManager.TRAINING_ATTR_PANEL_BG);
        preloadIfExists(ResourceManager.TRAINING_PROGRESS_BAR_BG);
        preloadIfExists(ResourceManager.TRAINING_PROGRESS_BAR_FILL);

        // Settings
        preloadIfExists(ResourceManager.SETTINGS_BTN_TIME_3MIN);
        preloadIfExists(ResourceManager.SETTINGS_BTN_TIME_5MIN);
        preloadIfExists(ResourceManager.SETTINGS_BTN_TIME_10MIN);
        preloadIfExists(ResourceManager.SETTINGS_BTN_OVERTIME_ON);
        preloadIfExists(ResourceManager.SETTINGS_BTN_OVERTIME_OFF);
        preloadIfExists(ResourceManager.SETTINGS_BTN_PENALTY_ON);
        preloadIfExists(ResourceManager.SETTINGS_BTN_PENALTY_OFF);
        preloadIfExists(ResourceManager.SETTINGS_BTN_CHEAT_ON);
        preloadIfExists(ResourceManager.SETTINGS_BTN_CHEAT_OFF);
        preloadIfExists(ResourceManager.SETTINGS_CHEAT_PANEL_BG);

        // Mod icons
        preloadIfExists(ResourceManager.MOD_ANTI_FRIENDLY_FIRE);
        preloadIfExists(ResourceManager.MOD_INSTANT_TURN);
        preloadIfExists(ResourceManager.MOD_EXPLOSION_PROOF);
        preloadIfExists(ResourceManager.MOD_LIGHT_MG);
        preloadIfExists(ResourceManager.MOD_HEAVY_MG);
        preloadIfExists(ResourceManager.MOD_LASER_CANNON);
        preloadIfExists(ResourceManager.MOD_DENSE_LASER);
        preloadIfExists(ResourceManager.MOD_AUTO_LOADER);
        preloadIfExists(ResourceManager.MOD_EXTRA_AMMO);
        preloadIfExists(ResourceManager.MOD_FRAGMENT);
    }

    public MapData loadMap(int level) {
        if (mapDataCache.containsKey(level)) return mapDataCache.get(level);
        File file = new File("resource/map-now-data/" + level + ".map");
        MapData data = file.exists() ? MapData.parse(file) : MapData.createEmpty();
        mapDataCache.put(level, data);
        return data;
    }

    public BufferedImage loadImage(String path) {
        if (path == null) return PLACEHOLDER;
        if (imageCache.containsKey(path)) return imageCache.get(path);
        try {
            File file = new File(path);
            if (!file.exists()) {
                System.err.println("[WARN] Image not found: " + path);
                imageCache.put(path, PLACEHOLDER);
                return PLACEHOLDER;
            }
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                imageCache.put(path, PLACEHOLDER);
                return PLACEHOLDER;
            }
            imageCache.put(path, img);
            return img;
        } catch (IOException e) {
            System.err.println("[WARN] Image load error: " + path + " - " + e.getMessage());
            imageCache.put(path, PLACEHOLDER);
            return PLACEHOLDER;
        }
    }

    public BufferedImage getImage(String path) {
        return loadImage(path);
    }

    public void clearImageCache() {
        imageCache.clear();
    }
}
