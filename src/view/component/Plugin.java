package view.component;

import java.awt.*;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public abstract class Plugin {
    protected PlotView plot;

    public void drawAfterPlot(Graphics2D g2) {

    }

    public void drawBeforePlot(Graphics2D g2) {

    }

    public void setPlot(PlotView plot) {
        this.plot = plot;
    }

    public void onYRangeChanged(float topPeakValue, float bottomPeakValue) {

    }

    public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize) {

    }
}
