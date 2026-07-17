package model.vo;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import model.load.ElementLoad;
import util.ResourceManager;

public class Iron extends SuperElement {
    private BufferedImage ironImage;

    public Iron(int x, int y) {
        super(x, y, 40, 40);
        ironImage = ElementLoad.getInstance().getImage(ResourceManager.WALL_IRON);
    }

    @Override
    public void show(Graphics2D g) {
        if (!visible) return;
        if (ironImage != null) {
            g.drawImage(ironImage, x, y, width, height, null);
        }
    }

    @Override
    public void update() {}
}
