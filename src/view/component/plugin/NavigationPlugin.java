package view.component.plugin;

import model.datasource.StreamingDataSource;
import view.component.plot.PlotView;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by maeglin89273 on 7/23/15.
 */
public class NavigationPlugin extends EmptyPlotPlugin implements InteractivePlotPlugin.MousePlugin {

    public enum ZoomingMode {
        ZOOM_X, ZOOM_Y, ZOOM_XY;
    }

    private ZoomingMode zoomingMode;

    private Set<String> interestedActions;
    private MouseInteractionHandler mouseHandler;

    public NavigationPlugin() {
        this.zoomingMode = ZoomingMode.ZOOM_XY;
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
    public void onSourceReplaced(StreamingDataSource oldSource) {
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

    public void setZoomingMode(ZoomingMode mode) {
        if (this.isEnabled()) {
            this.zoomingMode = mode;
        }
    }

    public void setMinimumZoomingWindowSize(int size) {
        this.mouseHandler.setMinWindowSize(size);
    }

    public void setMaximumZoomingWindowSize(int size) {
        this.mouseHandler.setMaxWindowSize(size);
    }

    @Override
    public Set<String> getInterestedActions() {
        return this.interestedActions;
    }

    private class MouseInteractionHandler extends MouseAdapter {
        private static final float SCALING_FACTOR = 1.1f;
        private int maxWindowSize = 2000;
        private int minWindowSize = 650;

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

            if(zoomingMode != ZoomingMode.ZOOM_X) {
                this.scalingLevel = this.scalingLevel + e.getWheelRotation();
                double scale = Math.pow(SCALING_FACTOR, this.scalingLevel);
                plot.setPeakValue((float) (this.originalPeakValue * scale));
            }

            if(zoomingMode != ZoomingMode.ZOOM_Y) {
                plot.setWindowSize(limitWindowSize((int) (plot.getWindowSize() * Math.pow(SCALING_FACTOR, e.getPreciseWheelRotation()))));
            }

        }

        private int limitWindowSize(int newWindowSize) {
            if (newWindowSize > maxWindowSize) {
                return this.maxWindowSize;
            } else if (newWindowSize < minWindowSize) {
                return this.minWindowSize;
            }
            return newWindowSize;
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

        public void setMinWindowSize(int minWindowSize) {

            this.minWindowSize = minWindowSize;
            if (this.minWindowSize > plot.getWindowSize()) {
                plot.setWindowSize(this.minWindowSize);
            }
        }

        public void setMaxWindowSize(int maxWindowSize) {
            this.maxWindowSize = maxWindowSize;
            if (this.maxWindowSize < plot.getWindowSize()) {
                plot.setWindowSize(this.maxWindowSize);
            }
        }
    }
}
