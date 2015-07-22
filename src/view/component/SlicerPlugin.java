package view.component;

import java.awt.*;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public class SlicerPlugin extends Plugin {
    private long startPos;
    private double relativeStartPos;
    private long endPos;
    private double relativeEndPos;
    private final Stroke STROKE = new BasicStroke(1.2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);

    private StreamingPlot.RangeChangedListener listener;

    public void setRangeChangeListener(StreamingPlot.RangeChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public void setPlot(PlotView plot) {
        super.setPlot(plot);
        // careful the start bound
        this.endPos = plot.getPlotUpperBound();
        this.setStartPosition(plot.getPlotLowerBound());
        this.setEndPosition(plot.getPlotUpperBound());
    }

    @Override
    public void drawAfterPlot(Graphics2D g2) {
        g2.setStroke(STROKE);
        g2.setColor(Color.CYAN);

        int startKnifeX = (int) (plot.getWidth() * this.relativeStartPos);
        int endKnifeX = (int) (plot.getWidth() * this.relativeEndPos);

        g2.drawLine(startKnifeX, 0, startKnifeX, plot.getHeight());
        g2.drawLine(endKnifeX, 0, endKnifeX, plot.getHeight());
    }

    public long getStartPosition() {
        return this.startPos;
    }

    public long getEndPosition() {
        return this.endPos;
    }

    public void setStartPosition(long startPosition) {
        if (startPosition < plot.getPlotLowerBound()) {
            this.startPos = plot.getPlotLowerBound();
        } else if (startPosition >= this.getEndPosition()) {
            this.startPos = endPos - 1;
        } else {
            this.startPos = startPosition;
        }
        this.relativeStartPos = (this.startPos - plot.getPlotLowerBound()) / (double) plot.getWindowSize();
        if (this.listener != null) {
            this.listener.onStartChanged(plot.getPlotLowerBound(), this.getStartPosition(), this.getEndPosition());
        }
    }

    public void setEndPosition(long endPosition) {
        if (endPosition > plot.getPlotUpperBound()) {
            this.endPos = plot.getPlotUpperBound();
        } else if (endPosition <= this.getStartPosition()) {
            this.endPos = startPos + 1;
        } else {
            this.endPos = endPosition;
        }
        this.relativeEndPos = (this.endPos - plot.getPlotLowerBound()) / (double) plot.getWindowSize();
        if (this.listener != null) {
            this.listener.onEndChanged(this.getStartPosition(), this.getEndPosition(), plot.getPlotUpperBound());
        }
    }

    @Override
    public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize) {
        this.startPos = (int) (this.relativeStartPos * windowSize) + plotLowerBound;
        this.endPos = (int) (this.relativeEndPos * windowSize) + plotLowerBound;

        if (this.listener != null) {
            this.listener.onStartChanged(plot.getPlotLowerBound(), this.getStartPosition(), this.getEndPosition());
            this.listener.onEndChanged(this.getStartPosition(), this.getEndPosition(), plot.getPlotUpperBound());
        }
    }

    private final float SLICER_TOUCH_RANGE = 7f;

    public boolean isOnStartSlicer(int pos) {
        return Math.abs(pos - (this.plot.getWidth() * this.relativeStartPos)) <= SLICER_TOUCH_RANGE;
    }

    public boolean isOnEndSlicer(int pos) {
        return Math.abs(pos - (this.plot.getWidth() * this.relativeEndPos)) <= SLICER_TOUCH_RANGE;
    }
}
