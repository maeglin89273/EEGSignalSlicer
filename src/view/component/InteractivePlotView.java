package view.component;

import view.component.plugin.InteractivePlotPlugin;
import view.component.plugin.PlotPlugin;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maeglin89273 on 7/23/15.
 */
public class InteractivePlotView extends PlotView {
    private List<InteractivePlotPlugin.MousePlugin> mousePlugins;
    private MouseEventHandler mouseHandler;

    public InteractivePlotView(int windowSize, float peakValue) {
        super(windowSize, peakValue);
        this.prepareInteraction();
    }

    private void prepareInteraction() {
        this.mousePlugins = new ArrayList<InteractivePlotPlugin.MousePlugin>();
        this.mouseHandler = new MouseEventHandler();
        this.addMouseListener(this.mouseHandler);
        this.addMouseMotionListener(this.mouseHandler);
        this.addMouseWheelListener(this.mouseHandler);
    }

    @Override
    public void addPlugin(PlotPlugin plugin) {
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
        boolean wantPropagateAction;
        for (int i = this.mousePlugins.size() - 1; i >= 0; i--) {
            InteractivePlotPlugin.MousePlugin plugin = this.mousePlugins.get(i);
            if (plugin.getInterestedActions().contains(action)) {
                wantPropagateAction = plugin.onMouseEvent(action, e);
                if (!wantPropagateAction) {
                    return;
                }
            }
        }
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
