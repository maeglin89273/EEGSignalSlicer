package view.component.plot;

import model.DataFileUtils;
import model.datasource.FragmentDataSource;
import model.datasource.StreamingDataSource;
import view.component.plugin.InteractivePlotPlugin;
import view.component.plugin.InterestedStreamVisibilityPlugin;
import view.component.plugin.PlotPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Created by maeglin89273 on 7/23/15.
 */
public class InteractivePlotView extends PlotView {
    private final List<String> visibleStreamTags;
    private final String plotName;
    private List<InteractivePlotPlugin.MousePlugin> mousePlugins;
    private List<InterestedStreamVisibilityPlugin> visibilityPlugins;
    private MouseEventHandler mouseHandler;
    private boolean viewAllStreams;
    private JPopupMenu additionalOptions;
    private OptionClickHandler optionClickHandler;

    public InteractivePlotView(String plotName, int windowSize, float peakValue, int plotWidth, int plotHeight) {
        this(plotName, windowSize, peakValue, new Dimension(plotWidth, plotHeight));
    }

    private void initAdditionalOptions() {
        this.optionClickHandler = new OptionClickHandler();
        this.additionalOptions = new JPopupMenu();
        JMenuItem saveImageItem = this.makeMenuItem("save plot image");
        JMenuItem saveSignalItem = this.makeMenuItem("save signals");
        this.additionalOptions.add(saveImageItem);
        this.additionalOptions.add(saveSignalItem);
    }

    private JMenuItem makeMenuItem(String text) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.setActionCommand(menuItem.getText());
        menuItem.addActionListener(this.optionClickHandler);
        return menuItem;
    }

    public InteractivePlotView(String plotName, int windowSize, float peakValue, Dimension dim) {
        super(windowSize, peakValue, dim);
        this.plotName = plotName;
        this.visibleStreamTags = new LinkedList<>();
        this.visibilityPlugins = new ArrayList<>();
        this.initAdditionalOptions();
        this.prepareInteraction();
    }

    public InteractivePlotView(String plotName, int windowSize, float peakValue) {
        this(plotName, windowSize, peakValue, PREFERRED_SIZE);
    }

    private void prepareInteraction() {
        this.mousePlugins = new ArrayList<>();
        this.mouseHandler = new MouseEventHandler();
        this.addMouseListener(this.mouseHandler);
        this.addMouseMotionListener(this.mouseHandler);
        this.addMouseWheelListener(this.mouseHandler);
    }

    @Override
    public StreamingDataSource setDataSource(StreamingDataSource dataSource) {
        this.visibleStreamTags.clear();
        return super.setDataSource(dataSource);
    }

    @Override
    protected void drawStreams(Graphics2D g2) {
        if (this.viewAllStreams) {
            super.drawStreams(g2);
        } else {
            for (String tag : this.visibleStreamTags) {
                this.drawStream(g2, tag);
            }
        }
    }

    public void setViewAllStreams(boolean viewAll) {
        this.viewAllStreams = viewAll;
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
        if (!this.isDataSourceSet() || this.viewAllStreams) {
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
        return this.viewAllStreams? super.getVisibleStreams(): this.visibleStreamTags;
    }

    private class MouseEventHandler implements MouseListener, MouseMotionListener, MouseWheelListener {
        private boolean blockMouseEnter = false;

        @Override
        public void mouseClicked(MouseEvent e) {
            this.checkAndShowPopupMenu(e);

        }

        private boolean checkAndShowPopupMenu(MouseEvent e) {
            if (e.isPopupTrigger() && InteractivePlotView.this.isEnabled()) {
                additionalOptions.show(InteractivePlotView.this, e.getX(), e.getY());
                return true;
            }
            return false;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (!this.checkAndShowPopupMenu(e)) {
                fireMouseAction("mousePressed", e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            this.checkAndShowPopupMenu(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (!this.blockMouseEnter) {
                fireMouseAction("mouseEntered", e);
            } else {
                this.blockMouseEnter = false;
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (!additionalOptions.isVisible()) {
                fireMouseAction("mouseExited", e);
            } else {
                this.blockMouseEnter = true;
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            fireMouseAction("mouseDragged", e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            fireMouseAction("mouseMoved", e);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            fireMouseAction("mouseWheelMoved", e);
        }
    }

    private class OptionClickHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            switch(e.getActionCommand()) {
                case "save plot image":
                    this.savePlotImage();
                    break;
                case "save signals":
                    this.saveSignal();
            }
        }

        private void savePlotImage() {
            BufferedImage buffer = this.createImageBuffer();
            InteractivePlotView.this.paint(buffer.getGraphics());
            DataFileUtils.getInstance().saveImage(buffer, getFileName());
        }

        private BufferedImage createImageBuffer() {
            return new BufferedImage(InteractivePlotView.this.getWidth(), InteractivePlotView.this.getHeight(), BufferedImage.TYPE_INT_ARGB);
        }

        private void saveSignal() {
            StreamingDataSource source = getDataSource();
            DataFileUtils.getInstance().saveDataSource("signal from " + getFileName() + ".csv", new FragmentDataSource("signal", 0, (int) source.getCurrentLength(), source));
        }

        private String getFileName() {
            return plotName + "_" + new Date().toString();
        }
    }
}
