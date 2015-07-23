package view.component.plugin;

import view.component.PlottingUtils;

import java.awt.*;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public class ShadowPlugin extends PlotPlugin {
    private final Stroke STROKE = new BasicStroke(3f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    private final Color SHADOW_COLOR = new Color(0, 0, 0, 0.25f);

    private long startingPtr;
    private int[] yBuffer = null;
    private boolean shadowing;

    @Override
    public void drawAfterPlot(Graphics2D g2) {
        if (!this.shadowing) {
            return;
        }
        g2.setStroke(STROKE);
        g2.setColor(SHADOW_COLOR);
        for (String tag : plot.getVisibleStreams()) {
            PlottingUtils.loadYBuffer(2 * plot.getPeakValue(), plot.getHeight(), plot.getDataSource().getDataOf(tag), yBuffer, (int) this.startingPtr);
            g2.drawPolyline(this.plot.getXPoints(), yBuffer, yBuffer.length);
        }
    }

    public void makeShadow() {
        int windowSize = plot.getWindowSize();
        this.startingPtr = plot.getPlotLowerBound();
        adjustBuffers(windowSize);
        this.shadowing = true;
        this.plot.refresh();
    }

    public void clear() {
        this.shadowing = false;
        this.plot.refresh();
    }

    @Override
    public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize) {
        if (this.shadowing) {
            adjustBuffers(windowSize);
        }
    }

    private void adjustBuffers(int size) {
        if (this.shouldResizeBuffer(size)) {
            this.yBuffer = new int[size];
        }
    }

    private boolean shouldResizeBuffer(int size) {
        return this.yBuffer == null || this.yBuffer.length != size;
    }
}
