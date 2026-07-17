package model.load;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import javax.imageio.ImageIO;

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
    }

    public MapData loadMap(int level) {
        if (mapDataCache.containsKey(level)) return mapDataCache.get(level);
        File file = new File("resource/map-now-data/" + level + ".map");
        MapData data = file.exists() ? MapData.parse(file) : MapData.createEmpty();
        mapDataCache.put(level, data);
        return data;
    }

    public BufferedImage loadImage(String path) {
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
            System.err.println("[WARN] Image load error: " + path);
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
