import view.MainForm;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 7/20/15.
 */
public class Launcher {

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new MainForm();

    }
}
