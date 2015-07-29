package view.component.plugin;

import view.component.PlotView;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by maeglin89273 on 7/23/15.
 */
public class NavigationPlugin extends EmptyPlotPlugin implements InteractivePlotPlugin.MousePlugin {
    private Set<String> interestedActions;
    private MouseInteractionHandler mouseHandler;

    public NavigationPlugin() {
        this.interestedActions = new HashSet<String>();
        this.interestedActions.add("mouseWheelMoved");
        this.interestedActions.add("mouseDragged");
        this.interestedActions.add("mousePressed");
    }

    public static int projectXDeltaToDataAmount(PlotView plot, int newX, int lastX) {
        return (int)(plot.getWindowSize() * (newX - lastX) / (double) plot.getWidth());
    }

    public static long projectMouseXToDataXIndex(PlotView plot, int mouseX) {
        return plot.getPlotLowerBound() + (int)(plot.getWindowSize() * mouseX / (double) plot.getWidth());
    }

    @Override
    public void setPlot(PlotView plot) {
        super.setPlot(plot);
        this.mouseHandler = new MouseInteractionHandler(plot.getWindowSize(), plot.getPeakValue());
        this.setEnabled(true);
    }

    @Override
    public void reset() {
        this.mouseHandler.resetCoordinates();
    }

    @Override
    public boolean onMouseEvent(String action, MouseEvent event) {

        switch (action) {
            case "mouseWheelMoved":
                this.mouseHandler.mouseWheelMoved((MouseWheelEvent) event);
                break;

            case "mouseDragged":
                this.mouseHandler.mouseDragged(event);
                break;

            case "mousePressed":
                this.mouseHandler.mousePressed(event);

        }
        return false;
    }

    @Override
    public Set<String> getInterestedActions() {
        return this.interestedActions;
    }

    private class MouseInteractionHandler extends MouseAdapter {
        private static final float SCALING_FACTOR = 1.1f;
        private static final int MAX_SCALING_LEVEL = 10;
        private static final int MIN_SCALING_LEVEL = -9;

        private int scalingLevel = 1;

        private final float originalPeakValue;
        private final int originalWindowSize;

        private int lastX = 0;

        public MouseInteractionHandler(int initWindowSize, float initPeakValue) {
            this.originalPeakValue = initPeakValue;
            this.originalWindowSize = initWindowSize;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int newScaleLevel = this.scalingLevel + e.getWheelRotation();
            if (newScaleLevel >= MIN_SCALING_LEVEL && newScaleLevel <= MAX_SCALING_LEVEL) {
                this.scalingLevel = newScaleLevel;
                double scale = Math.pow(SCALING_FACTOR, this.scalingLevel);
                plot.setPeakValue((float) (this.originalPeakValue * scale));
                plot.setWindowSize((int) (this.originalWindowSize * scale));
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            plot.moveX(projectXDeltaToDataAmount(plot, e.getX(), this.lastX));
            this.lastX = e.getX();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            this.lastX = e.getX();
        }

        public void resetCoordinates() {
            plot.setPeakValue(this.originalPeakValue);
            plot.setWindowSize(this.originalWindowSize);
            plot.setXTo(0);
        }
    }
}
