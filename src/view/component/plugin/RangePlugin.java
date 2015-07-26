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
public class RangePlugin extends EmptyPlotPlugin implements InteractivePlotPlugin.MousePlugin {

    private static final double MARGIN_PERCENTAGE = 0.1;
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
    private boolean fixedRange;

    public RangePlugin() {
        this(Color.CYAN);
    }

    public RangePlugin(Color knifeColor) {
        this.knifeColor = knifeColor;
        this.bgColor = new Color(knifeColor.getRed(), knifeColor.getGreen(), knifeColor.getBlue(), bgAlpha);

        initRelativePoses(MARGIN_PERCENTAGE);
        initActionSet();
    }

    private void initActionSet() {
        this.interestedActions = new HashSet<>(2);
        this.interestedActions.add("mouseDragged");
        this.interestedActions.add("mousePressed");
    }

    private void initRelativePoses(double marginPercentage) {
        this.relativeStartPos = marginPercentage;
        this.relativeEndPos = 1 - marginPercentage;

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
        int oldRange = this.getRange();

        this.startPos = boundStartPosition(startPosition);

        this.relativeStartPos = computeRelativePos(this.getStartPosition());
        fireStartChanged();

        if (this.fixedRange) {
            this.endPos = oldRange + this.getStartPosition() - 1;
            this.relativeEndPos = computeRelativePos(this.getEndPosition());
            fireEndChanged();
        }

        this.plot.refresh();
    }

    private long boundStartPosition(long startPosition) {
        if (startPosition >= this.getEndPosition()) {
            startPosition = this.getEndPosition() - 1;
        }
        if (startPosition < plot.getPlotLowerBound()) {
            startPosition = plot.getPlotLowerBound();
        }

        return startPosition;
    }

    public void setEndPosition(long endPosition) {
        if (!this.isEnabled()) {
            return;
        }
        int oldRange = this.getRange();
        this.endPos = boundEndPosition(endPosition);

        this.relativeEndPos = computeRelativePos(this.getEndPosition());

        if (this.fixedRange) {
            this.startPos = this.getEndPosition() - oldRange + 1;
            this.relativeStartPos = computeRelativePos(this.getStartPosition());
            fireStartChanged();
        }

        fireEndChanged();
        this.plot.refresh();
    }

    private long boundEndPosition(long endPosition) {
        if (endPosition <= this.getStartPosition()) {
            endPosition = getStartPosition() + 1;
        }

        if (endPosition > plot.getPlotUpperBound()) {
            endPosition = plot.getPlotUpperBound();
        }

        return endPosition;
    }

    protected double getRelativeStartPosition() {
        return this.relativeStartPos;
    }

    protected double getRelativeEndPosition() {
        return this.relativeEndPos;
    }

    @Override
    public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize) {
        this.syncRangeToPlot(plotLowerBound, plotUpperBound, windowSize);
    }

    public void setRange(int range) {
        if (!this.isEnabled()) {
            return;
        }

        // todo: find the proper space the extend range
    }

    public int getRange() {
        return (int)(this.getEndPosition() - this.getStartPosition()) + 1;
    }

    public void setFixedRange(boolean fixedRange) {
        this.fixedRange = fixedRange;
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
        double startX = this.plot.getWidth() * this.getRelativeStartPosition();
        double endX = this.plot.getWidth() * this.getRelativeEndPosition();
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
                if (!this.fixedRange) { //if it is fixed range, set start pos is enough.
                    this.setEndPosition(this.getEndPosition() + delta);
                }

        }
        return false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        syncRangeToPlot(plot.getPlotLowerBound(), plot.getPlotUpperBound(), plot.getWindowSize());
    }

    @Override
    public void setPlot(PlotView plot) {
        super.setPlot(plot);
        if (this.isEnabled()) {
            syncRangeToPlot(plot.getPlotLowerBound(), plot.getPlotUpperBound(), plot.getWindowSize());
        } else {
            syncPos(plot.getWindowSize(), plot.getPlotLowerBound());
        }
    }

    private void syncPos(int windowSize, long plotLowerBound) {
        if (!this.fixedRange) {
            this.startPos = (int) (this.getRelativeStartPosition() * windowSize) + plotLowerBound;
            this.endPos = (int) (this.getRelativeEndPosition() * windowSize) + plotLowerBound;
        } else {
            if (windowSize > this.getRange()) {
                this.setEnabled(false);
                return;
            }

            //todo: align to middle
        }
    }

    private double computeRelativePos(long pos) {
        return (pos - plot.getPlotLowerBound()) / (double) plot.getWindowSize();
    }

    protected void syncRangeToPlot(long plotLowerBound, long plotUpperBound, int windowSize) {
        if (!this.isEnabled()) {
            return;
        }

        syncPos(windowSize, plotLowerBound);

        fireStartChanged();
        fireEndChanged();
    }

    private void fireStartChanged() {
        if (this.listener != null) {
            this.listener.onStartChanged(plot.getPlotLowerBound(), this.getStartPosition(), this.getEndPosition());

        }
    }

    private void fireEndChanged() {
        if (this.listener != null) {
            this.listener.onEndChanged(this.getStartPosition(), this.getEndPosition(), plot.getPlotUpperBound());
        }
    }

    @Override
    public void reset() {
        initRelativePoses(MARGIN_PERCENTAGE);
        syncPos(plot.getWindowSize(), plot.getPlotLowerBound());
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
