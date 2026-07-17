package main;

import javax.swing.SwingUtilities;
import frame.MyJFrame;
import model.load.ElementLoad;
import model.manager.ElementManager;
import model.manager.SaveManager;

public class Main {
    public static void main(String[] args) {
        ElementLoad.getInstance().init();
        ElementManager.getInstance();
        SaveManager.getInstance();

        SwingUtilities.invokeLater(() -> {
            MyJFrame frame = new MyJFrame();
            frame.showLoginPanel();
            frame.setVisible(true);
        });
    }
}
