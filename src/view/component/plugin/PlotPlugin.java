package view.component.plugin;

import view.component.PlotView;

import java.awt.*;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public interface PlotPlugin extends PlotView.CoordinatesRangeChangedListener {

    public void drawAfterPlot(Graphics2D g2);

    public void drawBeforePlot(Graphics2D g2);

    public void setPlot(PlotView plot);

    public void setEnabled(boolean enabled);

    public boolean isEnabled();

    public void reset();

    public void onYRangeChanged(float topPeakValue, float bottomPeakValue);

    public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize);

}
