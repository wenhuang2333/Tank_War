package main;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("坦克大战");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1380, 820);
            frame.setLocationRelativeTo(null);

            // 中文显示示例面板
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            // 标题
            JLabel title = new JLabel("欢迎来到坦克大战");
            title.setFont(new Font("PingFang SC", Font.BOLD, 32));
            gbc.gridy = 0;
            panel.add(title, gbc);

            // 副标题
            JLabel subtitle = new JLabel("请选择你的坦克，准备战斗！");
            subtitle.setFont(new Font("PingFang SC", Font.PLAIN, 20));
            gbc.gridy = 1;
            panel.add(subtitle, gbc);

            // 带中文的按钮
            JButton startBtn = new JButton("开始对战");
            startBtn.setFont(new Font("PingFang SC", Font.PLAIN, 18));
            gbc.gridy = 2;
            panel.add(startBtn, gbc);

            frame.add(panel);
            frame.setVisible(true);
        });
    }
}
