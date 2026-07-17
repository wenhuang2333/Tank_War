package model.vo;

import java.awt.Graphics2D;
import java.awt.Rectangle;

public abstract class SuperElement {
    protected int x, y;
    protected int width, height;
    protected double direction;
    protected boolean visible = true;
    protected Rectangle rect;

    public SuperElement(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.direction = 0;
        this.rect = new Rectangle(x, y, width, height);
    }

    public abstract void show(Graphics2D g);
    public abstract void update();

    public void destroy() {
        this.visible = false;
    }

    public boolean isStrike(SuperElement other) {
        if (other == null || !other.isVisible()) return false;
        return getRect().intersects(other.getRect());
    }

    public Rectangle getRect() {
        rect.setBounds(x, y, width, height);
        return rect;
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public double getDirection() { return direction; }
    public void setDirection(double direction) {
        this.direction = ((direction % 360) + 360) % 360;
    }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public int getCenterX() { return x + width / 2; }
    public int getCenterY() { return y + height / 2; }
}
