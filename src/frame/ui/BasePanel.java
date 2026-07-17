package frame.ui;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import frame.MyJFrame;
import model.load.ElementLoad;
import util.ResourceManager;

public abstract class BasePanel extends JPanel {
    protected MyJFrame frame;
    private BufferedImage backgroundImage;

    public BasePanel(MyJFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        String bgPath = getBackgroundImagePath();
        if (bgPath != null) {
            backgroundImage = ElementLoad.getInstance().getImage(bgPath);
        }

        JPanel header = createHeader();
        if (header != null) {
            header.setOpaque(false);
            add(header, BorderLayout.NORTH);
        }

        JComponent content = buildContent();
        if (content != null) {
            if (content instanceof JPanel) ((JPanel) content).setOpaque(false);
            add(content, BorderLayout.CENTER);
        }

        JComponent footer = buildFooter();
        if (footer != null) {
            if (footer instanceof JPanel) ((JPanel) footer).setOpaque(false);
            add(footer, BorderLayout.SOUTH);
        }
    }

    protected String getBackgroundImagePath() { return null; }

    @Override
    protected void paintComponent(Graphics g) {
        if (backgroundImage != null && backgroundImage.getWidth() > 40) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            super.paintComponent(g);
        }
    }

    protected JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(new Color(40, 40, 40, 200));
        header.setPreferredSize(new Dimension(1380, 44));
        header.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));

        JButton backBtn = createImageButton(ResourceManager.COMMON_BTN_NORMAL,
            ResourceManager.COMMON_BTN_HOVER, 200, 40);
        backBtn.setText("返回主菜单");
        backBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        backBtn.setForeground(Color.WHITE);
        backBtn.setHorizontalTextPosition(JButton.CENTER);
        backBtn.addActionListener(e -> frame.showMainMenu());
        header.add(backBtn);
        return header;
    }

    protected abstract JComponent buildContent();
    protected JComponent buildFooter() { return null; }

    /** Create a button with image icons. Falls back to styled text button if image missing. */
    protected JButton createImageButton(String normalPath, String hoverPath, int w, int h) {
        JButton btn = new JButton();
        BufferedImage normalImg = ElementLoad.getInstance().getImage(normalPath);
        if (normalImg != null && normalImg.getWidth() > 10) {
            btn.setIcon(new ImageIcon(normalImg.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH)));
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setPreferredSize(new Dimension(w, h));
            if (hoverPath != null) {
                BufferedImage hoverImg = ElementLoad.getInstance().getImage(hoverPath);
                if (hoverImg != null && hoverImg.getWidth() > 10) {
                    btn.setRolloverIcon(new ImageIcon(hoverImg.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH)));
                }
            }
        } else {
            btn.setPreferredSize(new Dimension(w, h));
            btn.setBackground(new Color(70, 70, 70));
            btn.setForeground(Color.WHITE);
        }
        btn.setFocusPainted(false);
        return btn;
    }

    /** Create a button with only normal image (no hover). */
    protected JButton createImageButton(String normalPath, int w, int h) {
        return createImageButton(normalPath, null, w, h);
    }

    protected JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        btn.setPreferredSize(new Dimension(200, 40));
        btn.setBackground(new Color(70, 70, 70));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    protected JLabel createLabel(String text, int fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.PLAIN, fontSize));
        label.setForeground(Color.WHITE);
        return label;
    }

    /** Create a JLabel with an image icon. */
    protected JLabel createImageLabel(String imagePath, int w, int h) {
        BufferedImage img = ElementLoad.getInstance().getImage(imagePath);
        if (img != null && img.getWidth() > 10) {
            return new JLabel(new ImageIcon(img.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH)));
        }
        return new JLabel();
    }
}
