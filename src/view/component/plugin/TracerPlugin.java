package view.component.plugin;

import model.datasource.StreamingDataSource;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import static view.component.plugin.NavigationPlugin.projectXDeltaToDataAmount;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public class TracerPlugin extends StreamPlottingPlugin implements InteractivePlotPlugin.MousePlugin {
    private final Stroke stroke;
    private static final Color TRACE_COLOR = new Color(0, 0, 0, 0.25f);

    private long startingPtr;

    private Set<String> interestedActions;
    private boolean movingTrace = false;
    private boolean lastMovingTrace = false;
    private int lastX;

    public TracerPlugin() {
        this(3);
    }

    public TracerPlugin(float blur) {
        this.stroke = new BasicStroke(blur, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);

        this.interestedActions = new HashSet<>(1);
        this.interestedActions.add("mouseDragged");
        this.interestedActions.add("mousePressed");
    }

    @Override
    public void drawAfterPlot(Graphics2D g2) {
        g2.setStroke(stroke);
        g2.setColor(TRACE_COLOR);
        for (String tag : plot.getVisibleStreams()) {
            this.plotStream(g2, plot.getDataSource().getDataOf(tag), this.startingPtr);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            this.trace();
            this.setMouseInteractionEnabled(this.lastMovingTrace);
        } else {
            this.lastMovingTrace = this.isMouseInteractionEnabled();
            this.setMouseInteractionEnabled(false);
        }
    }

    public void setMouseInteractionEnabled(boolean enabled) {
        this.movingTrace = enabled;

    }

    public boolean isMouseInteractionEnabled() {
        return this.isEnabled()? this.movingTrace : false;
    }

    private void moveTrace(int delta) {

        delta = -delta;
        this.startingPtr = boundStartingPtr(this.startingPtr + delta);

        this.plot.refresh();

    }

    private long boundStartingPtr(long startingPtr) {
        if (startingPtr + this.plot.getWindowSize() - 1 >= this.plot.getDataSource().getCurrentLength()) {
            startingPtr = this.plot.getDataSource().getCurrentLength() - this.plot.getWindowSize();
        }

        if (startingPtr < 0) {
            startingPtr = 0;
        }
        return startingPtr;
    }

    public long getStartingPosition() {
        return this.startingPtr;
    }

    @Override
    public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize) {
        if (this.isEnabled()) {
            this.adjustBuffer(windowSize);
        }
    }


    @Override
    public boolean onMouseEvent(String action, MouseEvent event) {
        if (!this.isMouseInteractionEnabled()) {
            return true;
        }

        int newX = event.getX();
        switch (action) {
            case "mouseDragged":
                this.moveTrace(projectXDeltaToDataAmount(plot, event.getX(), this.lastX));
            case "mousePressed":
                this.lastX = newX;
        }
        return false;
    }

    public void onSourceReplaced(StreamingDataSource oldSource) {
        this.trace();
    }

    @Override
    public Set<String> getInterestedActions() {
        return this.interestedActions;
    }

    public void trace() {
        if (!this.isEnabled()) {
            return;
        }
        int windowSize = plot.getWindowSize();
        this.startingPtr = plot.getPlotLowerBound();
        adjustBuffer(windowSize);

        this.plot.refresh();
    }
}
