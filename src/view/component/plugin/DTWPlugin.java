package view.component.plugin;

import view.component.PlotView;
import view.component.PlottingUtils;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Created by maeglin89273 on 7/24/15.
 */
public class DTWPlugin extends SlicerPlugin {
    private static final Stroke STROKE = new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Color KNIFE_COLOR = Color.RED;
    private static final Color STROKE_COLOR = new Color(255, 0, 0, 45);
    private static final int DTW_COMPUTABLE_RANGE = 300;
    public static final float ACCEPTABLE_DTW_DISTANCE_UPPERBOUND = 45;

    private final double[] dtwSourceBufferFront;
    private final double[] dtwSourceBufferBack;
    private ShadowPlugin shadow;

    private int[][] dtwWarpingPath;
    private double dtwWarpingDistance = -1;
    private boolean renderDTW;
    private DTWDistanceListener distanceListener;

    public DTWPlugin() {
        super(KNIFE_COLOR);
        this.shadow = new ShadowPlugin(2);
        this.dtwSourceBufferFront = new double[DTW_COMPUTABLE_RANGE];
        this.dtwSourceBufferBack = new double[DTW_COMPUTABLE_RANGE];
    }

    @Override
    public void setPlot(PlotView plot) {
        super.setPlot(plot);
        this.shadow.setPlot(plot);
    }

    @Override
    public void drawBeforePlot(Graphics2D g2) {
        if (!this.isDTWOn()) {
            return;
        }

        super.drawBeforePlot(g2);
        this.drawDTW(g2);
    }

    @Override
    public void drawAfterPlot(Graphics2D g2) {
        if (!this.isDTWOn()) {
            return;
        }
        this.shadow.drawAfterPlot(g2);
        super.drawAfterPlot(g2);
    }

    private void drawDTW(Graphics2D g2) {
        if (this.getRenderDTW()) {
            g2.setStroke(STROKE);
            g2.setColor(STROKE_COLOR);

            int startX, endX, startY, endY;
            int xOffset = (int)(this.getStartPosition() - this.plot.getPlotLowerBound());
            int[] xBuffer = this.plot.getXPoints();
            float cHeight = 2 * this.plot.getPeakValue();
            int pHeight = this.plot.getHeight();
            for (int[] pointMapping: dtwWarpingPath) {
                startX = xBuffer[xOffset + pointMapping[0]];
                endX = xBuffer[xOffset + pointMapping[1]];
                startY = PlottingUtils.mapY(cHeight, pHeight, this.dtwSourceBufferBack[pointMapping[0]]);
                endY = PlottingUtils.mapY(cHeight, pHeight, this.dtwSourceBufferFront[pointMapping[1]]);
                g2.drawLine(startX, startY, endX, endY);
            }
        }
    }

    @Override
    public boolean onMouseEvent(String action, MouseEvent event) {
        if (!this.isDTWOn()) {
            return true;
        }
        boolean isOnSlicing = !super.onMouseEvent(action, event);
        boolean returnVal = isOnSlicing? false: this.shadow.onMouseEvent(action, event);

        if (isMouseDragged(action)) {
            updateDTW();
        }

        return returnVal;
    }

    private boolean isMouseDragged(String action) {
        return action.equals("mouseDragged");
    }

    @Override
    public void onYRangeChanged(float topPeakValue, float bottomPeakValue) {
        super.onYRangeChanged(topPeakValue, bottomPeakValue);
        this.shadow.onYRangeChanged(topPeakValue, bottomPeakValue);
    }

    @Override
    public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize) {
        super.onXRangeChanged(plotLowerBound, plotUpperBound, windowSize);
        this.shadow.onXRangeChanged(plotLowerBound, plotUpperBound, windowSize);
        this.updateDTW();
    }

    public void updateDTW() {
        if (this.isAbleComputeDTW()) {
            double[] visibleStream = this.getSingleVisibleStream();
            int computingSize = this.getSliceSize();
            System.arraycopy(visibleStream, (int) projectSliceToShadow(), this.dtwSourceBufferBack, 0, computingSize);
            System.arraycopy(visibleStream, (int)this.getStartPosition(), this.dtwSourceBufferFront, 0, computingSize);

            DTWAlgorithm dtw = new DTWAlgorithm(this.dtwSourceBufferBack, computingSize, this.dtwSourceBufferFront, computingSize);
            if (dtw.getDistance() <= ACCEPTABLE_DTW_DISTANCE_UPPERBOUND) {
                this.dtwWarpingDistance = dtw.getDistance();
                this.dtwWarpingPath = dtw.getWarpingPath();
                this.setRenderDTW(true);
            } else {
                setRenderDTW(false);
            }
            fireDistanceLisener(this.getDTWDistance());
            this.setRenderRangeBackground(true);

        } else {
            setRenderDTW(false);
            this.setRenderRangeBackground(false);
        }
    }

    private void fireDistanceLisener(double distance) {
        if (this.distanceListener != null) {
            this.distanceListener.onDistanceChanged(distance);
        }
    }

    private long projectSliceToShadow() {
        return (int)(this.getRelativeStartPosition() * this.plot.getWindowSize()) + this.shadow.getStartingPosition();
    }

    private boolean getRenderDTW() {
        return this.renderDTW;
    }

    private void setRenderDTW(boolean wantRender) {
        if (!wantRender) {
            this.dtwWarpingDistance = -1;
        }
        this.renderDTW = wantRender;
    }

    private double[] getSingleVisibleStream() {
        return this.plot.getDataSource().getDataOf(this.plot.getVisibleStreams().get(0));
    }

    private boolean isAbleComputeDTW() {
        return this.shadow.isShadowing() && this.plot.getVisibleStreams().size() == 1 && this.getSliceSize() < DTW_COMPUTABLE_RANGE;
    }

    public void startDTW() {
        this.shadow.makeShadow();
        this.updateDTW();
    }

    public void closeDTW() {
        this.setRenderDTW(false);
        this.shadow.clear();
    }

    public boolean isDTWOn() {
        return this.shadow.isShadowing();
    }

    public double getDTWDistance() {
        return this.dtwWarpingDistance;
    }

    public void setBackStreamControl(boolean control) {
        this.shadow.setMouseInteractionEnabled(control);
    }

    public void setDTWDistanceListener (DTWDistanceListener listener) {
        this.distanceListener = listener;
    }

    public interface DTWDistanceListener {
        public void onDistanceChanged(double distnace);
    }
}