package view.component.plugin;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Created by maeglin89273 on 7/23/15.
 */
public class NavigationPlugin extends PlotPlugin {

    private class MouseInteractionHandler extends MouseAdapter {
        private static final float SCALING_FACTOR = 1.2f;
        private static final int MAX_SCALING_LEVEL = 4;

        private int scalingLevel = 0;

        private final float originalPeakValue;
        private final int originalWindowSize;

        private int lastX = 0;

        public MouseInteractionHandler(int originalWindowSize, float originalPeakValue) {
            this.originalPeakValue = originalPeakValue;
            this.originalWindowSize = originalWindowSize;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (Math.abs(scalingLevel + e.getWheelRotation()) <= MAX_SCALING_LEVEL) {
                scalingLevel += e.getWheelRotation();
                double scale = Math.pow(SCALING_FACTOR, scalingLevel);
                plot.setPeakValue((float) (originalPeakValue * scale));
                plot.setWindowSize((int) (originalWindowSize * scale));
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            plot.moveX(this.lastX - e.getX());
            this.lastX = e.getX();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            this.lastX = e.getX();
        }

    }
}
