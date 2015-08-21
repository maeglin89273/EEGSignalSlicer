package view.component.plugin;

import model.datasource.Stream;
import view.component.plot.PlottingUtils;

import java.awt.*;

/**
 * Created by maeglin89273 on 8/21/15.
 */
public abstract class StreamPlottingPlugin extends EmptyPlotPlugin {
    private int[] yBuffer;

    protected void adjustBuffer(int size) {
        if (this.shouldResizeBuffer(size)) {
            this.yBuffer = new int[size];
        }
    }

    private boolean shouldResizeBuffer(int size) {
        return this.yBuffer == null || this.yBuffer.length < size;
    }

    public void plotStream(Graphics2D g2, Stream stream, long startingPtr) {
        int length = PlottingUtils.loadYBuffer(plot.getBaseline(), plot.getPeakValue(), plot.getHeight(), stream, (int) startingPtr, yBuffer);
        g2.drawPolyline(this.plot.getXPoints(), this.yBuffer, length);
    }
}
