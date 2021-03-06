package view.component.plugin;

import model.datasource.MutableFiniteStream;
import model.datasource.SimpleArrayStream;
import model.datasource.Stream;
import model.datasource.StreamingDataSource;
import view.component.plot.PlotView;
import view.component.plot.PlottingUtils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collection;

/**
 * Created by maeglin89273 on 7/24/15.
 */
public class DTWPlugin extends RangePlugin implements InterestedStreamVisibilityPlugin {
    private static final Stroke STROKE = new BasicStroke(0.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final Color KNIFE_COLOR = Color.RED;
    private static final Color STROKE_COLOR = new Color(255, 0, 0, 80);
    private static final int DTW_COMPUTABLE_RANGE = 400;
    public static final float ACCEPTABLE_DTW_DISTANCE_UPPERBOUND = 100;

    private final MutableFiniteStream dtwSourceBufferFront;
    private final MutableFiniteStream dtwSourceBufferBack;
    private TracerPlugin streamTemplate;

    private int[][] dtwWarpingPath;
    private double dtwWarpingDistance = -1;
    private boolean renderDTW;
    private DTWDistanceListener distanceListener;

    public DTWPlugin() {
        super(KNIFE_COLOR);
        this.streamTemplate = new TracerPlugin(2);
        this.dtwSourceBufferFront = new SimpleArrayStream(DTW_COMPUTABLE_RANGE);
        this.dtwSourceBufferBack = new SimpleArrayStream(DTW_COMPUTABLE_RANGE);
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
            float cHeight = this.plot.getPeakValue();
            int pHeight = this.plot.getHeight();
            for (int[] pointMapping: dtwWarpingPath) {
                startX = xBuffer[xOffset + pointMapping[1]];
                endX = xBuffer[xOffset + pointMapping[0]];
                startY = PlottingUtils.mapY(plot.getBaseline(), cHeight, pHeight, this.dtwSourceBufferBack.get(pointMapping[1]));
                endY = PlottingUtils.mapY(plot.getBaseline(), cHeight, pHeight, this.dtwSourceBufferFront.get(pointMapping[0]));
                g2.drawLine(startX, startY, endX, endY);
            }
        }
    }

    @Override
    public boolean onMouseEvent(String action, MouseEvent event) {

        if (super.onMouseEvent(action, event)) {
            if (!this.streamTemplate.onMouseEvent(action, event)) {
                this.updateDTW();
                return false;
            }
            return true;
        }
        return false;
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
    }

    @Override
    public void onPresentedDataChanged() {
        this.updateDTW();
    }

    public void updateDTW() {
        if (this.isAbleComputeDTW()) {
            int computingSize = this.getRange();
            Stream visibleStream = plot.getDataSource().getDataOf(this.visibleTag);
            this.dtwSourceBufferBack.replacedBy(visibleStream, (int) projectSliceToTrace(), computingSize);
            this.dtwSourceBufferFront.replacedBy(visibleStream, (int) this.getStartPosition(), computingSize);

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

    private long projectSliceToTrace() {
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


    private boolean isAbleComputeDTW() {
        return this.isEnabled() && visibleTag != null && this.getRange() < DTW_COMPUTABLE_RANGE;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        streamTemplate.setEnabled(enabled);
        updateVisibleStream();
        updateDTW();
    }

    @Override
    protected void onRangeChanged() {
        updateDTW();
    }

    private String visibleTag;

    @Override
    public void onStreamVisibilityChanged(String tag, boolean isVisible) {
        updateVisibleStream();
        updateDTW();
    }

    private void updateVisibleStream() {
        Collection<String> visibleStreams = plot.getVisibleStreams();
        if (visibleStreams.size() == 1) {
            for (String theTag: visibleStreams) {
                visibleTag = theTag;
            }
        } else {
            visibleTag = null;
        }
    }

    @Override
    public void onSourceReplaced(StreamingDataSource oldSource) {
        super.onSourceReplaced(oldSource);
        this.streamTemplate.onSourceReplaced(oldSource);
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
        this.streamTemplate.trace();
        this.updateDTW();
    }

    public interface DTWDistanceListener {
        public void onDistanceChanged(double distnace);
    }
}
