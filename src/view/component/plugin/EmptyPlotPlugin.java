package view.component.plugin;

import view.component.PlotView;

import java.awt.*;

/**
 * Created by maeglin89273 on 7/24/15.
 */
public class EmptyPlotPlugin implements PlotPlugin {
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
