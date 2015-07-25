package view.component.plugin;

import view.component.PlotView;
import view.component.PlottingUtils;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Created by maeglin89273 on 7/24/15.
 */
public class DTWPlugin extends SlicerPlugin {
    private static final Stroke STROKE = new BasicStroke(0.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Color KNIFE_COLOR = Color.RED;
    private static final Color STROKE_COLOR = new Color(255, 0, 0, 80);
    private static final int DTW_COMPUTABLE_RANGE = 400;
    public static final float ACCEPTABLE_DTW_DISTANCE_UPPERBOUND = 100;

    private final double[] dtwSourceBufferFront;
    private final double[] dtwSourceBufferBack;
    private ShadowPlugin streamTemplate;

    private int[][] dtwWarpingPath;
    private double dtwWarpingDistance = -1;
    private boolean renderDTW;
    private DTWDistanceListener distanceListener;

    public DTWPlugin() {
        super(KNIFE_COLOR);
        this.streamTemplate = new ShadowPlugin(2);
        this.dtwSourceBufferFront = new double[DTW_COMPUTABLE_RANGE];
        this.dtwSourceBufferBack = new double[DTW_COMPUTABLE_RANGE];
    }

    @Override
    public void setPlot(PlotView plot) {
        super.setPlot(plot);
        this.streamTemplate.setPlot(plot);
    }

    @Override
    public void drawBeforePlot(Graphics2D g2) {
        super.drawBeforePlot(g2);
        this.streamTemplate.drawAfterPlot(g2);
        this.drawDTW(g2);
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
                startX = xBuffer[xOffset + pointMapping[1]];
                endX = xBuffer[xOffset + pointMapping[0]];
                startY = PlottingUtils.mapY(cHeight, pHeight, this.dtwSourceBufferBack[pointMapping[1]]);
                endY = PlottingUtils.mapY(cHeight, pHeight, this.dtwSourceBufferFront[pointMapping[0]]);
                g2.drawLine(startX, startY, endX, endY);
            }
        }
    }

    @Override
    public boolean onMouseEvent(String action, MouseEvent event) {
        if (!this.isEnabled()) {
            return true;
        }
        boolean isOnSlicing = !super.onMouseEvent(action, event);
        boolean returnVal = isOnSlicing? false: this.streamTemplate.onMouseEvent(action, event);

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
        this.streamTemplate.onYRangeChanged(topPeakValue, bottomPeakValue);
    }

    @Override
    public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize) {
        super.onXRangeChanged(plotLowerBound, plotUpperBound, windowSize);
        this.streamTemplate.onXRangeChanged(plotLowerBound, plotUpperBound, windowSize);
        this.updateDTW();
    }

    public void updateDTW() {
        if (this.isAbleComputeDTW()) {
            double[] visibleStream = this.getSingleVisibleStream();
            int computingSize = this.getSliceSize();
            System.arraycopy(visibleStream, (int) projectSliceToShadow(), this.dtwSourceBufferBack, 0, computingSize);
            System.arraycopy(visibleStream, (int)this.getStartPosition(), this.dtwSourceBufferFront, 0, computingSize);

            DTWAlgorithm dtw = new DTWAlgorithm(this.dtwSourceBufferFront, computingSize, this.dtwSourceBufferBack, computingSize);
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
        return (int)(this.getRelativeStartPosition() * this.plot.getWindowSize()) + this.streamTemplate.getStartingPosition();
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
        return this.isEnabled() && this.plot.getVisibleStreams().size() == 1 && this.getSliceSize() < DTW_COMPUTABLE_RANGE;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        streamTemplate.setEnabled(enabled);
        this.updateDTW();
    }

    @Override
    public void reset() {
        super.reset();
        this.streamTemplate.reset();
    }

    public double getDTWDistance() {
        return this.dtwWarpingDistance;
    }

    public void setTemplateControl(boolean control) {
        this.streamTemplate.setMouseInteractionEnabled(control);
    }

    public void setDTWDistanceListener (DTWDistanceListener listener) {
        this.distanceListener = listener;
    }

    public void makeNewTemplate() {
        this.streamTemplate.makeNewShadow();
        this.updateDTW();
    }

    public interface DTWDistanceListener {
        public void onDistanceChanged(double distnace);
    }
}
