package view.component.plugin;

import view.component.PlotView;

import java.awt.*;

/**
 * Created by maeglin89273 on 7/24/15.
 */
public abstract class EmptyPlotPlugin implements PlotPlugin {
    protected PlotView plot;
    private boolean enabled = false;

    public void drawAfterPlot(Graphics2D g2) {

    }

    public void drawBeforePlot(Graphics2D g2) {

    }

    @Override
    public boolean isEnabled() {
        return this.enabled;

    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.plot == null) {
            this.enabled = false;
            return;
        }
        this.enabled = enabled;
        this.plot.refresh();
    }

    public void setPlot(PlotView plot) {
        this.plot = plot;
    }

    @Override
    public void onYRangeChanged(float topPeakValue, float bottomPeakValue) {

    }

    @Override
    public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize) {

    }

    @Override
    public void onPresentedDataChanged() {

    }
}
