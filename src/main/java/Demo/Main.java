package Demo;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

/**
 * @ClassName Test
 * @Description
 * @Author 15014
 * @Time 2023/2/9 16:33
 * @Version 1.0
 */
public class Main {
    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        Utils.init();
        JFrame frame = new JFrame("openAI 在线发问");
        UIManager.setLookAndFeel(new NimbusLookAndFeel());
        frame.setContentPane(new TVIew().$$$getRootComponent$$$());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
