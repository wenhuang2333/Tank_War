package model.vo;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import model.load.ElementLoad;
import util.ResourceManager;

public class Brick extends SuperElement {
    private BufferedImage brickImage;

    public Brick(int x, int y) {
        super(x, y, 40, 40);
        brickImage = ElementLoad.getInstance().getImage(ResourceManager.WALL_BRICK);
    }

    @Override
    public void show(Graphics2D g) {
        if (!visible) return;
        if (brickImage != null) {
            g.drawImage(brickImage, x, y, width, height, null);
        }
    }

    @Override
    public void update() {}
}
