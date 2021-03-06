package view.component.plot;

import model.datasource.StreamingDataSource;
import view.component.plugin.PlotPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.util.List;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public class PlotView extends JComponent implements StreamingDataSource.PresentedDataChangedListener {
    protected static final Dimension PREFERRED_SIZE = new Dimension(750, 280);

    private float peakValue;

    private int[] xBuffer;
    private int[] yBuffer;

    private long startingPtr = 0;

    private List<PlotPlugin> plugins;
    private List<CoordinatesRangeChangedListener> rangeListeners;
    private StreamingDataSource dataSource;
    private Stroke stroke;
    private PlottingUtils.Baseline baseline;

    public PlotView(int windowSize, float peakValue, Dimension dim) {
        this.setPreferredSize(dim);

        this.plugins = new ArrayList<>();
        this.rangeListeners = new ArrayList<>();

        this.baseline = PlottingUtils.Baseline.MIDDLE;

        this.setWindowSize(windowSize);
        this.setPeakValue(peakValue);
        this.setLineWidth(1.5f);

        this.setAdapters();

        this.setBackground(new Color(-1));
        this.setOpaque(true);
    }

    public PlotView(int windowSize, float peakValue, int plotWidth, int plotHeight) {
        this(windowSize, peakValue, new Dimension(plotWidth, plotHeight));
    }
    public PlotView(int windowSize, float peakValue) {
        this(windowSize, peakValue, PREFERRED_SIZE);
    }

    private void setAdapters() {
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateXBuffer();
                refresh();
            }
        });
    }

    public void setLineWidth(float width) {
        this.stroke = new BasicStroke(width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    }

    public StreamingDataSource setDataSource(StreamingDataSource dataSource) {

        if (this.isDataSourceSet()) {
            this.setXTo(0);
            this.dataSource.removePresentedDataChangedListener(this);
        }
        StreamingDataSource oldSource = this.dataSource;
        this.dataSource = dataSource;
        this.dataSource.addPresentedDataChangedListener(this);

        this.fireSourceReplacedToPlugin(oldSource);
        this.refresh();
        return oldSource;
    }

    public StreamingDataSource getDataSource() {
        return this.dataSource;
    }

    public boolean isDataSourceSet() {
        return this.dataSource != null;
    }

    private void fireSourceReplacedToPlugin(StreamingDataSource oldSource) {
        for (int i = this.plugins.size() - 1; i >= 0; i--) {
            this.plugins.get(i).onSourceReplaced(oldSource);
        }
    }

    public void addPlugin(PlotPlugin plugin) {

        this.plugins.add(plugin);
        this.rangeListeners.add(plugin);
        plugin.setPlot(this);
        if (this.isDataSourceSet()) {
            plugin.onSourceReplaced(dataSource);
            this.refresh();
        }

    }

    public void addCoordinatesRangeChangedListener(CoordinatesRangeChangedListener listener) {
        this.rangeListeners.add(listener);
    }

    public long getPlotLowerBound() {
        return this.startingPtr;
    }

    public long getPlotUpperBound() {
        long upper = this.startingPtr + getWindowSize() - 1;
        if (isDataSourceSet()) {
            if (upper > dataSource.getCurrentLength() - 1) {
                return dataSource.getCurrentLength() - 1;
            }
        }
        return upper;
    }

    public void setWindowSize(int windowSize) {
        if (windowSize < 2) {
            return;
        }

        this.xBuffer = new int[windowSize];
        this.yBuffer = new int[windowSize];
        updateXBuffer();

        if (dataSource != null && windowSize < dataSource.getCurrentLength() && getPlotLowerBound() + windowSize > dataSource.getCurrentLength()) {
            setXTo(dataSource.getCurrentLength() - this.getWindowSize());
        } else {
            fireOnXRangeChanged();
            this.refresh();
        }
    }

    public void moveX(int delta) {
        this.setXTo(-delta + this.getPlotLowerBound());
    }

    public void setXTo(long startingPoint) {

        this.startingPtr = boundStartingPtr(startingPoint);

        fireOnXRangeChanged();
        this.refresh();
    }

    private long boundStartingPtr(long startingPoint) {
        if (startingPoint + this.getWindowSize() - 1 >= dataSource.getCurrentLength()) {
            startingPoint = dataSource.getCurrentLength() - this.getWindowSize();
        }

        if (startingPoint < 0) {
            startingPoint = 0;
        }
        return startingPoint;
    }

    public float getPeakValue() {
        return this.peakValue;
    }

    public void setPeakValue(float peakValue) {

        this.peakValue = peakValue;
        fireOnYRangeChanged();
        this.refresh();
    }

    public int getWindowSize() {
        return this.xBuffer.length;
    }

    private void fireOnXRangeChanged() {
        for (int i = rangeListeners.size() - 1; i >= 0; i--) {
            rangeListeners.get(i).onXRangeChanged(this.getPlotLowerBound(), this.getPlotUpperBound(), this.getWindowSize());
        }

    }

    private void fireOnYRangeChanged() {
        for (int i = rangeListeners.size() - 1; i >= 0; i--) {
            rangeListeners.get(i).onYRangeChanged(this.getPeakValue(), -this.getPeakValue());
        }
    }

    private void fireOnPresentedDataChanged() {
        for (int i = plugins.size() - 1; i >= 0; i--) {
            plugins.get(i).onPresentedDataChanged();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = prepareGraphics(g);
        g2.setBackground(this.getBackground());
        g2.clearRect(0, 0, this.getWidth(), this.getHeight());

        if (this.isDataSourceSet() && this.isEnabled()) {
            drawBackPlugins(g2);
            g2.setStroke(this.stroke);
            drawStreams(g2);
            drawFrontPlugins(g2);

        }
        g2.dispose();
    }

    private void drawBackPlugins(Graphics2D g2) {
        PlotPlugin plugin;
        for (int i = plugins.size() - 1; i >= 0; i--) {
            plugin = plugins.get(i);
            if (plugin.isEnabled()) {
                plugin.drawBeforePlot(g2);
            }
        }
    }

    private void drawFrontPlugins(Graphics2D g2) {
        PlotPlugin plugin;
        for (int i = plugins.size() - 1; i >= 0; i--) {
            plugin = plugins.get(i);
            if (plugin.isEnabled()) {
                plugins.get(i).drawAfterPlot(g2);
            }
        }
    }

    protected void drawStreams(Graphics2D g2) {
        for (String tag : this.dataSource.getTags()) {
            drawStream(g2, tag);
        }
    }

    protected void drawStream(Graphics2D g2, String tag) {
        g2.setColor(PlottingUtils.hashStringToColor(tag));

        int length = xBuffer.length;
        length = PlottingUtils.loadYBuffer(this.baseline, this.getPeakValue(), this.getHeight(), dataSource.getDataOf(tag), (int) this.getPlotLowerBound(), yBuffer, length);
        g2.drawPolyline(xBuffer, yBuffer, length);
    }

    private Graphics2D prepareGraphics(Graphics g) {
        if (!(g instanceof Graphics2D)) {
            return null;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        return g2;
    }

    public void setBaseline(PlottingUtils.Baseline mode) {
        if (this.baseline != mode) {
            this.baseline = mode;
            this.refresh();
        }
    }

    public PlottingUtils.Baseline getBaseline() {
        return this.baseline;
    }

    private void updateXBuffer() {
        PlottingUtils.loadXBuffer(this.getWindowSize(), this.getWidth(), this.xBuffer);
    }

    public int[] getXPoints() {
        return this.xBuffer;
    }

    public void refresh() {
        this.repaint();
    }

    @Override
    public void onDataChanged(StreamingDataSource source) {
        this.fireOnPresentedDataChanged();
        this.refresh();
    }

    @Override
    public void onDataChanged(StreamingDataSource source, String tag) {
        this.refresh();
    }

    public Collection<String> getVisibleStreams() {
        if (!this.isDataSourceSet()) {
            return Collections.emptyList();
        }
        return this.dataSource.getTags();
    }

    public interface CoordinatesRangeChangedListener {
        public void onYRangeChanged(float topPeakValue, float bottomPeakValue);
        public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize);
    }
}
