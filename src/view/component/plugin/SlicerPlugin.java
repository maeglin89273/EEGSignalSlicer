package view.component.plugin;

import view.component.PlotView;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import static view.component.plugin.NavigationPlugin.projectXDeltaToDataAmount;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public class SlicerPlugin extends EmptyPlotPlugin implements InteractivePlotPlugin.MousePlugin {

    private long startPos;
    private double relativeStartPos;
    private long endPos;
    private double relativeEndPos;
    private static final Stroke STROKE = new BasicStroke(1.2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    private final Color knifeColor;
    private final int bgAlpha = 17;
    private final Color bgColor;

    private boolean renderBg = false;

    private RangeChangedListener listener;
    private Set<String> interestedActions;

    public SlicerPlugin() {
        this(Color.CYAN);
    }

    public SlicerPlugin(Color knifeColor) {
        this.knifeColor = knifeColor;
        this.bgColor = new Color(knifeColor.getRed(), knifeColor.getGreen(), knifeColor.getBlue(), bgAlpha);

        this.interestedActions = new HashSet<String>(2);
        this.interestedActions.add("mouseDragged");
        this.interestedActions.add("mousePressed");
    }

    public void setRangeChangedListener(RangeChangedListener listener) {
        this.listener = listener;
    }

    public void setRenderRangeBackground(boolean wantRender) {
        if (this.renderBg != wantRender) {
            this.renderBg = wantRender;
            this.plot.refresh();
        }
    }



    @Override
    public void drawBeforePlot(Graphics2D g2) {
        if (this.renderBg) {
            int startX = (int) (plot.getWidth() * this.getRelativeStartPosition());
            int endX = (int) (plot.getWidth() * this.getRelativeEndPosition());
            g2.setColor(this.bgColor);
            g2.fillRect(startX, 0, endX - startX, this.plot.getHeight());
        }
    }

    @Override
    public void drawAfterPlot(Graphics2D g2) {
        g2.setStroke(STROKE);
        g2.setColor(this.knifeColor);

        int startKnifeX = (int) (plot.getWidth() * this.getRelativeStartPosition());
        int endKnifeX = (int) (plot.getWidth() * this.getRelativeEndPosition());

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
        if (!this.isEnabled()) {
            return;
        }

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
        this.plot.refresh();
    }

    public void setEndPosition(long endPosition) {
        if (!this.isEnabled()) {
            return;
        }

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
        this.plot.refresh();
    }

    protected double getRelativeStartPosition() {
        return this.relativeStartPos;
    }

    protected double getRelativeEndPosition() {
        return this.relativeEndPos;
    }

    @Override
    public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize) {
        if (!this.isEnabled()) {
            return;
        }

        this.startPos = (int) (this.relativeStartPos * windowSize) + plotLowerBound;
        this.endPos = (int) (this.relativeEndPos * windowSize) + plotLowerBound;

        if (this.listener != null) {
            this.listener.onStartChanged(plot.getPlotLowerBound(), this.getStartPosition(), this.getEndPosition());
            this.listener.onEndChanged(this.getStartPosition(), this.getEndPosition(), plot.getPlotUpperBound());
        }
    }

    public int getSliceSize() {
        return (int)(this.getEndPosition() - this.getStartPosition()) + 1;
    }

    private enum OperatingMode {
        NOT_SLICING, START_SLICER, END_SLICER, MOVE_RANGE
    }

    private OperatingMode operatingMode;
    private int tmpX;

    private static final double SLICER_TOUCH_RANGE = 7f;

    private boolean isOnSlicer(int pos, double slicerX) {
        return Math.abs(pos - slicerX) <= SLICER_TOUCH_RANGE;
    }

    private boolean isOnWindow(int pos, double startX, double endX) {
        return startX < pos && pos < endX;
    }

    @Override
    public boolean onMouseEvent(String action, MouseEvent event) {
        if (!this.isEnabled()) {
            return true;
        }

        switch (action) {
            case "mouseDragged":
                return handleMouseDragged(event);

            case "mousePressed":
                return handleMousePressed(event);
        }
        return true;
    }

    private boolean handleMousePressed(MouseEvent event) {
        int mouseX = event.getX();
        double startX = this.plot.getWidth() * this.relativeStartPos;
        double endX = this.plot.getWidth() * this.relativeEndPos;
        if (isOnSlicer(mouseX, startX)) {
            this.operatingMode = OperatingMode.START_SLICER;
            return false;
        } else if (isOnSlicer(mouseX, endX)) {
            this.operatingMode = OperatingMode.END_SLICER;
            return false;
        } else if (this.renderBg && this.isOnWindow(mouseX, startX, endX)) {
            this.operatingMode = OperatingMode.MOVE_RANGE;
            tmpX = mouseX;
            return false;
        }
        this.operatingMode = OperatingMode.NOT_SLICING;
        return true;
    }

    private boolean handleMouseDragged(MouseEvent event) {
        if (operatingMode == OperatingMode.NOT_SLICING) {
            return true;
        }

        long moveKnifeToHere = NavigationPlugin.projectMouseXToDataXIndex(this.plot, event.getX());
        switch (operatingMode) {
            case START_SLICER:
                this.setStartPosition(moveKnifeToHere);
                break;

            case END_SLICER:
                this.setEndPosition(moveKnifeToHere);
                break;

            case MOVE_RANGE:
                int delta = projectXDeltaToDataAmount(plot, event.getX(), this.tmpX);
                tmpX = event.getX();
                this.setStartPosition(this.getStartPosition() + delta);
                this.setEndPosition(this.getEndPosition() + delta);

        }
        return false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            this.reset();
        }
    }

    @Override
    public void reset() {
        // careful the start bound
        this.endPos = plot.getPlotUpperBound();
        this.setStartPosition(plot.getPlotLowerBound() + 50);
        this.setEndPosition(plot.getPlotUpperBound() - 50);
    }

    @Override
    public Set<String> getInterestedActions() {
        return this.interestedActions;
    }

    public interface RangeChangedListener {
        public void onStartChanged(long lowerBound, long value, long upperBound);
        public void onEndChanged(long lowerBound, long value, long upperBound);
    }
}
