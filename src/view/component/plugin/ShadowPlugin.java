package view.component.plugin;

import view.component.PlottingUtils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public class ShadowPlugin extends EmptyPlotPlugin implements InteractivePlotPlugin.MousePlugin {
    private final Stroke stroke;
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 0.25f);



    private long startingPtr;
    private int[] yBuffer = null;
    private boolean shadowing;

    private Set<String> interestedActions;
    private boolean isMoivingShadow;
    private int lastX;


    public ShadowPlugin() {
        this(3);
    }

    public ShadowPlugin(float blur) {
        this.stroke = new BasicStroke(blur, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);

        this.interestedActions = new HashSet<String>(1);
        this.interestedActions.add("mouseDragged");
        this.interestedActions.add("mousePressed");
    }

    @Override
    public void drawAfterPlot(Graphics2D g2) {
        if (!this.shadowing) {
            return;
        }
        g2.setStroke(stroke);
        g2.setColor(SHADOW_COLOR);
        for (String tag : plot.getVisibleStreams()) {
            PlottingUtils.loadYBuffer(2 * plot.getPeakValue(), plot.getHeight(), plot.getDataSource().getDataOf(tag), yBuffer, (int) this.startingPtr);
            g2.drawPolyline(this.plot.getXPoints(), yBuffer, yBuffer.length);
        }
    }

    public void makeShadow() {
        int windowSize = plot.getWindowSize();
        this.startingPtr = plot.getPlotLowerBound();
        adjustBuffers(windowSize);
        this.shadowing = true;
        this.plot.refresh();
    }

    public void clear() {
        this.shadowing = false;
        this.setMouseInteractionEnabled(false);

        this.plot.refresh();
    }

    public void setMouseInteractionEnabled(boolean enabled) {
        this.isMoivingShadow = enabled;
    }

    public void moveShadow(int delta) {
        if (!this.shadowing) {
            return;
        }

        long newStartingPtr = this.startingPtr + delta;
        if (newStartingPtr < 0) {
            newStartingPtr = 0;
        } else if (newStartingPtr + this.plot.getWindowSize() - 1 >= this.plot.getDataSource().getMaxStreamLength()) {
            newStartingPtr = this.plot.getDataSource().getMaxStreamLength() - this.plot.getWindowSize();
        }

        this.startingPtr = newStartingPtr;

        this.plot.refresh();

    }

    @Override
    public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize) {
        if (this.shadowing) {
            adjustBuffers(windowSize);
        }
    }

    private void adjustBuffers(int size) {
        if (this.shouldResizeBuffer(size)) {
            this.yBuffer = new int[size];
        }
    }

    private boolean shouldResizeBuffer(int size) {
        return this.yBuffer == null || this.yBuffer.length != size;
    }

    @Override
    public boolean onMouseEvent(String action, MouseEvent event) {
        if (!this.isMoivingShadow) {
            return true;
        }

        switch (action) {
            case "mouseDragged":
                this.moveShadow(this.lastX - event.getX());
                this.lastX = event.getX();
            case "mousePressed":
                this.lastX = event.getX();
        }
        return false;
    }


    @Override
    public Set<String> getInterestedActions() {
        return this.interestedActions;
    }
}
