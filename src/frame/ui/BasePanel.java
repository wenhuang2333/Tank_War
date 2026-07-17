package frame.ui;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Color;
import frame.MyJFrame;

public abstract class BasePanel extends JPanel {
    protected MyJFrame frame;

    public BasePanel(MyJFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        JPanel header = createHeader();
        if (header != null) add(header, BorderLayout.NORTH);

        JComponent content = buildContent();
        if (content != null) add(content, BorderLayout.CENTER);

        JComponent footer = buildFooter();
        if (footer != null) add(footer, BorderLayout.SOUTH);
    }

    protected JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(Color.DARK_GRAY);
        header.setPreferredSize(new Dimension(1380, 44));
        header.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));

        JButton backBtn = createStyledButton("返回主菜单");
        backBtn.addActionListener(e -> frame.showMainMenu());
        header.add(backBtn);
        return header;
    }

    protected abstract JComponent buildContent();
    protected JComponent buildFooter() { return null; }

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
}
