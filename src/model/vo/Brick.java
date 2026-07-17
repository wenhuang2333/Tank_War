package model.vo;

import java.awt.Graphics2D;
import java.awt.Color;

public class Brick extends SuperElement {
    public Brick(int x, int y) {
        super(x, y, 40, 40);
    }

    @Override
    public void show(Graphics2D g) {
        if (!visible) return;
        g.setColor(new Color(139, 90, 43));
        g.fillRect(x, y, width, height);
        g.setColor(new Color(160, 110, 60));
        g.drawLine(x, y, x + width - 1, y);
        g.drawLine(x, y, x, y + height - 1);
        g.setColor(new Color(100, 60, 20));
        g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
        g.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
        g.setColor(new Color(120, 75, 30));
        g.drawLine(x, y + height / 2, x + width - 1, y + height / 2);
        g.drawLine(x + width / 2, y, x + width / 2, y + height / 2 - 1);
        g.drawLine(x + width / 4, y + height / 2, x + width / 4, y + height - 1);
        g.drawLine(x + 3 * width / 4, y + height / 2, x + 3 * width / 4, y + height - 1);
    }

    @Override
    public void update() {}
}
