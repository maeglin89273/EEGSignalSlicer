package view.component.plugin;

import model.datasource.*;
import view.component.plot.PlotView;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static view.component.plugin.NavigationPlugin.projectXDeltaToDataAmount;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public class RangePlugin extends EmptyPlotPlugin implements InteractivePlotPlugin.MousePlugin, InterestedStreamVisibilityPlugin {

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
    private boolean rangeOverPlot = false;
    private boolean fixedRange;

    private RangeChangedListener listener;
    private Set<String> interestedActions;
    private RangedDataSource rangedDataSource;


    public RangePlugin() {
        this(Color.CYAN);
    }

    public RangePlugin(Color knifeColor) {
        this.knifeColor = knifeColor;
        this.bgColor = new Color(knifeColor.getRed(), knifeColor.getGreen(), knifeColor.getBlue(), bgAlpha);
        this.rangedDataSource = new RangedDataSource();
        initRelativePoses(MARGIN_PERCENTAGE);
        initActionSet();

    }

    public RangePlugin(Color knifeColor, int fixedRange) {
        this.knifeColor = knifeColor;
        this.bgColor = new Color(knifeColor.getRed(), knifeColor.getGreen(), knifeColor.getBlue(), bgAlpha);
        this.rangedDataSource = new RangedDataSource();
        initFixedRange(fixedRange);
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
        if (this.isEnabled() && !this.fixedRange && this.renderBg != wantRender) {
            this.renderBg = wantRender;
            this.plot.refresh();
        }
    }

    @Override
    public void drawBeforePlot(Graphics2D g2) {
        if (this.renderBg && !this.rangeOverPlot) {
            int startX = (int) (plot.getWidth() * this.getRelativeStartPosition());
            int endX = (int) (plot.getWidth() * this.getRelativeEndPosition());
            g2.setColor(this.bgColor);
            g2.fillRect(startX, 0, endX - startX, this.plot.getHeight());
        }
    }

    @Override
    public void drawAfterPlot(Graphics2D g2) {
        if (rangeOverPlot) {
            return;
        }

        g2.setStroke(STROKE);
        g2.setColor(this.knifeColor);

        int startKnifeX = (int) (plot.getWidth() * this.getRelativeStartPosition());
        int endKnifeX = (int) (plot.getWidth() * this.getRelativeEndPosition());

        g2.drawLine(startKnifeX, 0, startKnifeX, plot.getHeight());
        g2.drawLine(endKnifeX, 0, endKnifeX, plot.getHeight());
    }

    public FragmentDataSource makeFragmentDataSource(String tag) {
        long startingPos = this.getStartPosition();
        int length = this.getRange();
        StreamingDataSource dataSource = plot.getDataSource();
        return new FragmentDataSource(tag, startingPos, length, dataSource);
    }

    public long getStartPosition() {
        return this.startPos;
    }

    public long getEndPosition() {
        return this.endPos;
    }

    public void setStartPosition(long startPosition) {
        if (!this.isEnabled() || rangeOverPlot) {
            return;
        }

        startPosition = boundStartPosition(startPosition);

        if (this.fixedRange) {
            long newEnd = this.getRange() + startPosition - 1;
            if (newEnd > plot.getPlotUpperBound()) {
                return;
            }
            this.endPos = newEnd;
            this.relativeEndPos = computeRelativePos(this.getEndPosition());
            fireEndChanged();
        }

        this.startPos = startPosition;
        this.relativeStartPos = computeRelativePos(this.getStartPosition());
        fireStartChanged();

        onRangeChanged();
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
        if (!this.isEnabled() || rangeOverPlot) {
            return;
        }

        endPosition = boundEndPosition(endPosition);

        if (this.fixedRange) {
            long newStart = endPosition - this.getRange() + 1;
            if (newStart < plot.getPlotLowerBound()) {
                return;
            }
            this.startPos = newStart;
            this.relativeStartPos = computeRelativePos(this.getStartPosition());
            fireStartChanged();
        }

        this.endPos = endPosition;
        this.relativeEndPos = computeRelativePos(this.getEndPosition());
        fireEndChanged();

        onRangeChanged();
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
        if (!this.isEnabled() || this.fixedRange) {
            return;
        }

        boundRange(plot.getWindowSize(), plot.getPlotLowerBound(), plot.getPlotUpperBound(), range);

        fireStartChanged();
        fireEndChanged();
        onRangeChanged();
        plot.refresh();
    }

    private void boundRange(int windowSize, long plotLowerBound, long plotUpperBound, int range) {
        long middle = (int)(((this.getRelativeStartPosition() + this.getRelativeEndPosition()) / 2) * windowSize) + plotLowerBound;

        int leftHalf = range / 2;
        int rightHalf = (int)Math.ceil(range / 2.0f);
        int middleToLower = (int) (middle - plotLowerBound);
        int upperToMiddle = (int) (plotUpperBound - middle);
        if (leftHalf > middleToLower) {
            rightHalf += leftHalf - middleToLower;
            leftHalf = middleToLower;
        } else if (rightHalf > upperToMiddle) {
            leftHalf += rightHalf - upperToMiddle;
            rightHalf = upperToMiddle;
        }

        this.startPos = middle - leftHalf + 1;
        this.relativeStartPos = computeRelativePos(this.getStartPosition());
        this.endPos = middle + rightHalf;
        this.relativeEndPos = computeRelativePos(this.getEndPosition());
    }

    public int getRange() {
        return (int)(this.getEndPosition() - this.getStartPosition()) + 1;
    }

    public void setFixedRange(boolean fixedRange) {
        if (!this.isEnabled()) {
            return;
        }

        this.fixedRange = fixedRange;
        this.setRenderRangeBackground(this.fixedRange);
        if (this.rangeOverPlot && !this.fixedRange) {
            this.initRelativePoses(0);
            this.syncRangeToPlot(plot.getPlotLowerBound(), plot.getPlotUpperBound(), plot.getWindowSize());
        }
    }

    private void initFixedRange(int fixedRange) {
        this.startPos = 0;
        this.endPos = fixedRange - 1;
        this.fixedRange = true;
        this.renderBg = true;
    }

    public boolean isRangeOverPlot() {
        return this.rangeOverPlot;
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
            syncPos(plot.getWindowSize(), plot.getPlotLowerBound(), plot.getPlotUpperBound());
        }

    }

    private void syncPos(int windowSize, long plotLowerBound, long plotUpperBound) {

        if (!this.fixedRange) {
            this.startPos = boundStartPosition((int) (this.getRelativeStartPosition() * windowSize) + plotLowerBound);
            this.relativeStartPos = computeRelativePos(getStartPosition());
            this.endPos = boundEndPosition((int) (this.getRelativeEndPosition() * windowSize) + plotLowerBound);
            this.relativeEndPos = computeRelativePos(getEndPosition());
        } else {
            if (plotUpperBound - plotLowerBound + 1 < this.getRange()) {
                this.rangeOverPlot = true;
                return;
            }

            this.boundRange(windowSize, plotLowerBound, plotUpperBound, this.getRange());
        }
        this.rangeOverPlot = false;
    }

    private double computeRelativePos(long pos) {
        return (pos - plot.getPlotLowerBound()) / (double) plot.getWindowSize();
    }

    private void syncRangeToPlot(long plotLowerBound, long plotUpperBound, int windowSize) {
        if (!this.isEnabled()) {
            return;
        }

        syncPos(windowSize, plotLowerBound, plotUpperBound);

        fireStartChanged();
        fireEndChanged();

        onRangeChanged();
    }


    @Override
    public void onSourceReplaced(StreamingDataSource oldSource) {
        initRelativePoses(MARGIN_PERCENTAGE);
        syncPos(plot.getWindowSize(), plot.getPlotLowerBound(), plot.getPlotUpperBound());
        if (oldSource != null) {
            oldSource.removePresentedDataChangedListener(this.rangedDataSource);
        }
        plot.getDataSource().addPresentedDataChangedListener(this.rangedDataSource);
    }

    @Override
    public Set<String> getInterestedActions() {
        return this.interestedActions;
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
        if (rangeOverPlot) {
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

    protected void onRangeChanged() {
        this.rangedDataSource.firePresentedDataChanged();
    }

    @Override
    public void onStreamVisibilityChanged(String tag, boolean isVisible) {
        this.rangedDataSource.firePresentedDataChanged(tag);
    }

    public FiniteLengthDataSource getRangedDataSource() {
        return this.rangedDataSource;
    }

    public interface RangeChangedListener {
        public void onStartChanged(long lowerBound, long value, long upperBound);
        public void onEndChanged(long lowerBound, long value, long upperBound);
    }

    private class RangedDataSource extends FiniteLengthDataSource implements ViewDataSource {

        @Override
        public FiniteLengthStream getFiniteDataOf(String tag) {
            return new RangedStream(plot.getDataSource().getDataOf(tag));
        }

        @Override
        public int intLength() {
            return getRange();
        }

        @Override
        public Collection<String> getTags() {
            return plot.getVisibleStreams();
        }

        @Override
        public void firePresentedDataChanged() {
            super.firePresentedDataChanged();
        }

        @Override
        public void firePresentedDataChanged(String tag) {
            super.firePresentedDataChanged(tag);
        }

        @Override
        public void onDataChanged(StreamingDataSource source) {
            this.firePresentedDataChanged();
        }

        @Override
        public void onDataChanged(StreamingDataSource source, String tag) {
            this.firePresentedDataChanged(tag);
        }

        @Override
        public void setViewingSource(boolean viewing) {
            if (viewing) {
                plot.getDataSource().addPresentedDataChangedListener(this);
            } else {
                plot.getDataSource().removePresentedDataChangedListener(this);
            }
        }

        private class RangedStream extends FiniteLengthStream {
            private final Stream source;

            public RangedStream(Stream source) {
                this.source = source;
            }

            @Override
            public int intLength() {
                return getRange();
            }

            @Override
            public double[] toArray() {
                double[] buffer = new double[this.intLength()];
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = source.get(sourceIndex(i));
                }
                return buffer;
            }

            @Override
            public double get(long i) {
                return source.get(sourceIndex(i));
            }

            private long sourceIndex(long i) {
                return i + getStartPosition();
            }
        }
    }

}
