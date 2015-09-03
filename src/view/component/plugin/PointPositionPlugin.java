package view.component.plugin;

import model.datasource.StreamingDataSource;
import view.component.plot.PlotView;
import view.component.plot.PlottingUtils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by maeglin89273 on 8/28/15.
 */
public class PointPositionPlugin extends EmptyPlotPlugin implements InteractivePlotPlugin.MousePlugin {

    private static final Stroke LINE_STROKE = new BasicStroke(1.2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, new float[]{5, 4, 2, 4}, 0);
    private static final int TEXT_MARGIN = 5;
    private final Set<String> interestedActions;
    private Color color;
    private int posX;
    private int posY;
    private double xUnit;
    private boolean mouseIn = false;

    public PointPositionPlugin(double xUnit) {
        this(new Color(255, 0, 0, 255), xUnit);
    }

    public PointPositionPlugin(Color color, double xUnit) {
        this.setXUnit(xUnit);
        this.color = color;
        this.interestedActions = new HashSet<>(4);
        this.interestedActions.add("mouseMoved");
        this.interestedActions.add("mouseEntered");
        this.interestedActions.add("mouseExited");
        this.interestedActions.add("mouseDragged");
    }

    @Override
    public void drawAfterPlot(Graphics2D g2) {
        if (this.mouseIn) {
            g2.setColor(this.color);
            g2.setStroke(LINE_STROKE);
            g2.drawLine(0, posY, plot.getWidth(), posY);
            g2.drawLine(posX, 0, posX, plot.getHeight());
            String posText = this.positionText();

            g2.drawString(posText, this.computeTextX(g2, posText), this.computeTextY(g2));
        }
    }

    private int computeTextY(Graphics2D g2) {
        FontMetrics metrics = g2.getFontMetrics();
        int textHeight = metrics.getHeight() - metrics.getLeading();
        if (posY - TEXT_MARGIN - textHeight <= 0) {
            return posY + textHeight;
        }
        return posY - TEXT_MARGIN;
    }

    private int computeTextX(Graphics2D g2, String posText) {
        FontMetrics metrics = g2.getFontMetrics();
        int textWidth = metrics.stringWidth(posText);
        if (posX - TEXT_MARGIN - textWidth <= 0) {
            return posX + TEXT_MARGIN;
        }
        return posX - TEXT_MARGIN - textWidth;
    }

    private String positionText() {
        return String.format("%.1f, %.1f", translateX(), translateY());
    }

    private double translateY() {
        float coordHeight = plot.getBaseline() == PlottingUtils.Baseline.BOTTOM? plot.getPeakValue(): 2 * plot.getPeakValue();
        return  plot.getPeakValue() - this.posY * coordHeight / plot.getHeight();
    }

    private double translateX() {
        return this.toXUnit(plot.getPlotLowerBound() + this.posX * (double) plot.getWindowSize() / plot.getWidth());
    }

    private double toXUnit(double x) {
        return this.getXUnit() * x;
    }

    public double getXUnit() {
        return this.xUnit;
    }

    public void setXUnit(double xUnit) {
        this.xUnit = xUnit;
    }

    @Override
    public Set<String> getInterestedActions() {
        return interestedActions;
    }

    @Override
    public void onSourceReplaced(StreamingDataSource oldSource) {

    }

    @Override
    public boolean onMouseEvent(String action, MouseEvent event) {
        switch (action) {
            case "mouseExited":
                this.mouseIn = false;
                plot.refresh();
                break;
            case "mouseEntered":
                this.mouseIn = true;
                break;

            default:
                this.posX = event.getX();
                this.posY = event.getY();
                plot.refresh();
        }


        return true;
    }
}
