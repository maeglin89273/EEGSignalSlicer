package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;

/**
 * Created by maeglin89273 on 9/4/15.
 */
public class BorderedPanel extends JPanel {

    public static int DEFAULT_PADDING = SpacingStandard.PADDING;

    public BorderedPanel() {
        this(DEFAULT_PADDING);
    }

    public BorderedPanel(int padding) {
        this.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
    }
}
