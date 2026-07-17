package model.vo;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import model.load.ElementLoad;
import util.ResourceManager;

public class Background extends SuperElement {
    private BufferedImage bgImage;
    private BufferedImage tileImage;

    public Background(int x, int y, int width, int height) {
        super(x, y, width, height);
        ElementLoad el = ElementLoad.getInstance();
        bgImage = el.getImage(ResourceManager.BATTLE_BG);
        tileImage = el.getImage(ResourceManager.TILE_GROUND);
    }

    @Override
    public void show(Graphics2D g) {
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, 1380, 820, null);
        }
        if (tileImage != null) {
            int tileSize = 40;
            for (int row = 0; row <= height / tileSize; row++) {
                for (int col = 0; col <= width / tileSize; col++) {
                    g.drawImage(tileImage, x + col * tileSize, y + row * tileSize, tileSize, tileSize, null);
                }
            }
        }
    }

    @Override
    public void update() {}
}
