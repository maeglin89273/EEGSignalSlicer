package view.component;

import model.datasource.StreamingDataSource;
import view.component.plugin.InteractivePlotPlugin;
import view.component.plugin.InterestedStreamVisibilityPlugin;
import view.component.plugin.PlotPlugin;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by maeglin89273 on 7/23/15.
 */
public class InteractivePlotView extends PlotView {
    private final List<String> visibleStreamTags;
    private List<InteractivePlotPlugin.MousePlugin> mousePlugins;
    private List<InterestedStreamVisibilityPlugin> visibilityPlugins;
    private MouseEventHandler mouseHandler;

    public InteractivePlotView(int windowSize, float peakValue) {
        super(windowSize, peakValue);
        this.visibleStreamTags = new LinkedList<>();
        this.visibilityPlugins = new ArrayList<>();
        this.prepareInteraction();
    }

    private void prepareInteraction() {
        this.mousePlugins = new ArrayList<>();
        this.mouseHandler = new MouseEventHandler();
        this.addMouseListener(this.mouseHandler);
        this.addMouseMotionListener(this.mouseHandler);
        this.addMouseWheelListener(this.mouseHandler);
    }

    @Override
    public void setDataSource(StreamingDataSource dataSource) {
        this.visibleStreamTags.clear();
        super.setDataSource(dataSource);
    }

    @Override
    protected void drawStreams(Graphics2D g2) {
        for (String tag: this.visibleStreamTags) {
            this.drawStream(g2, tag);
        }
    }

    @Override
    public void addPlugin(PlotPlugin plugin) {
        if (plugin instanceof InterestedStreamVisibilityPlugin) {
            this.visibilityPlugins.add((InterestedStreamVisibilityPlugin) plugin);
        }
        if (plugin instanceof InteractivePlotPlugin) {
            addIneractivePlugin((InteractivePlotPlugin) plugin);
        }
        super.addPlugin(plugin);
    }

    private void addIneractivePlugin(InteractivePlotPlugin iPlugin) {
        if (iPlugin instanceof InteractivePlotPlugin.MousePlugin) {
            this.mousePlugins.add((InteractivePlotPlugin.MousePlugin) iPlugin);
        }
    }

    private void fireMouseAction(String action, MouseEvent e) {
        if (!this.isEnabled()) {
            return;
        }
        boolean wantPropagateAction;
        for (int i = this.mousePlugins.size() - 1; i >= 0; i--) {
            InteractivePlotPlugin.MousePlugin plugin = this.mousePlugins.get(i);

            if (plugin.isEnabled() && plugin.getInterestedActions().contains(action)) {
                wantPropagateAction = plugin.onMouseEvent(action, e);
                if (!wantPropagateAction) {
                    return;
                }
            }
        }
    }

    public void setStreamVisible(String tag, boolean isVisible) {
        if (!this.isDataSourceSet()) {
            return;
        }

        if (isVisible) {
            if (!this.visibleStreamTags.contains(tag)) {
                this.visibleStreamTags.add(tag);
                this.fireStreamVisibilityChangedToPlugins(tag, isVisible);
                this.refresh();
            }
        } else {
            this.visibleStreamTags.remove(tag);
            this.fireStreamVisibilityChangedToPlugins(tag, isVisible);
            this.refresh();
        }

    }

    private void fireStreamVisibilityChangedToPlugins(String tag, boolean isVisible) {
        for (int i = this.visibilityPlugins.size() - 1; i >= 0; i--) {
            InterestedStreamVisibilityPlugin plugin = this.visibilityPlugins.get(i);
            if (plugin.isEnabled()) {
                plugin.onStreamVisibilityChanged(tag, isVisible);
            }
        }
    }

    @Override
    public Collection<String> getVisibleStreams() {
        return this.visibleStreamTags;
    }

    private class MouseEventHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            fireMouseAction("mousePressed", e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mouseDragged(MouseEvent e) {
            fireMouseAction("mouseDragged", e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            fireMouseAction("mouseWheelMoved", e);
        }
    }
}
