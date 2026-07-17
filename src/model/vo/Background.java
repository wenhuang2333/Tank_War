package model.vo;

import java.awt.Graphics2D;
import java.awt.Color;

public class Background extends SuperElement {
    public Background(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void show(Graphics2D g) {
        g.setColor(new Color(30, 30, 30));
        g.fillRect(x, y, width, height);
        g.setColor(new Color(50, 50, 50));
        for (int row = 0; row <= height / 40; row++) {
            g.drawLine(x, y + row * 40, x + width, y + row * 40);
        }
        for (int col = 0; col <= width / 40; col++) {
            g.drawLine(x + col * 40, y, x + col * 40, y + height);
        }
    }

    @Override
    public void update() {}
}
