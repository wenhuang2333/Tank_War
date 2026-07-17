package model.vo;

import java.awt.Graphics2D;
import java.awt.Color;

public class Iron extends SuperElement {
    public Iron(int x, int y) {
        super(x, y, 40, 40);
    }

    @Override
    public void show(Graphics2D g) {
        if (!visible) return;
        g.setColor(new Color(128, 128, 128));
        g.fillRect(x, y, width, height);
        g.setColor(new Color(180, 180, 180));
        g.fillRect(x + 2, y + 2, width - 4, height - 4);
        g.setColor(new Color(100, 100, 100));
        g.drawRect(x + 2, y + 2, width - 5, height - 5);
        g.setColor(new Color(160, 160, 160));
        g.drawLine(x + 6, y + 6, x + width - 7, y + height - 7);
        g.drawLine(x + width - 7, y + 6, x + 6, y + height - 7);
    }

    @Override
    public void update() {}
}
