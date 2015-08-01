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
        private static final int MAX_WINDOW_SCALING_LEVEL = 7;
        private static final int MIN_WINDOW_SCALING_LEVEL = -6;

        private int scalingLevel = 1;

        private final int originalWindowSize;
        private final float originalPeakValue;

        private int lastX = 0;

        public MouseInteractionHandler(int initWindowSize, float initPeakValue) {
            this.originalPeakValue = initPeakValue;
            this.originalWindowSize = initWindowSize;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {

            this.scalingLevel = this.scalingLevel + e.getWheelRotation();
            double scale = Math.pow(SCALING_FACTOR, this.scalingLevel);
            if (this.scalingLevel >= MIN_WINDOW_SCALING_LEVEL && scalingLevel <= MAX_WINDOW_SCALING_LEVEL) {
                plot.setWindowSize((int) (this.originalWindowSize * scale));
            }
            plot.setPeakValue((float) (this.originalPeakValue * scale));
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
            this.scalingLevel = 1;
        }
    }
}
