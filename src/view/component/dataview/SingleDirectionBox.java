package view.component.dataview;

import javax.swing.*;
import java.awt.*;

/**
 * Created by maeglin89273 on 8/20/15.
 */
public class SingleDirectionBox extends Box implements Scrollable {

    private boolean scrollableTracksViewportHeight = true;
    private boolean scrollableTracksViewportWidth = true;

    public static Box createHorizontalBox() {
        return new SingleDirectionBox(BoxLayout.X_AXIS);
    }

    public static Box createVerticalBox() {
        return new SingleDirectionBox(BoxLayout.Y_AXIS);
    }

    public SingleDirectionBox(int axis) {
        super(axis);

        if (axis == BoxLayout.X_AXIS || axis == BoxLayout.LINE_AXIS) {
            this.scrollableTracksViewportWidth = false;
        } else {
            this.scrollableTracksViewportHeight = false;
        }
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return this.getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 10;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 10;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return this.scrollableTracksViewportWidth;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return this.scrollableTracksViewportHeight;
    }
}
