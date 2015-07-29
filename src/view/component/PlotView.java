package view.component;

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
    private static final Dimension PREFERRED_SIZE = new Dimension(750, 300);

    private Map<String, Color> colorMapping;

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
        this.colorMapping = new HashMap<>();

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

    public void setDataSource(StreamingDataSource dataSource) {

        if (this.dataSource != null) {
            this.setXTo(0);
            this.dataSource.removePresentedDataChangedListener(this);
        }
        this.dataSource = dataSource;
        this.dataSource.addPresentedDataChangedListener(this);

        this.fireResetPlugin();
        this.refresh();
    }

    private void fireResetPlugin() {
        for (int i = this.plugins.size() - 1; i >= 0; i--) {
            this.plugins.get(i).reset();
        }
    }

    public StreamingDataSource getDataSource() {
        return this.dataSource;
    }

    public boolean isDataSourceSet() {
        return this.dataSource != null;
    }

    public void addPlugin(PlotPlugin plugin) {

        this.plugins.add(plugin);
        this.rangeListeners.add(plugin);
        plugin.setPlot(this);
        if (this.isDataSourceSet()) {
            plugin.reset();
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

        this.xBuffer = new int[windowSize];
        this.yBuffer = new int[windowSize];

        updateXBuffer();
        fireOnXRangeChanged();
        this.refresh();
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
        g2.setColor(hashStringToColor(tag));
        int length = PlottingUtils.loadYBuffer(this.baseline, this.getPeakValue(), this.getHeight(), dataSource.getDataOf(tag), (int) this.getPlotLowerBound(), yBuffer);
        g2.drawPolyline(xBuffer, yBuffer, length);
    }

    private Color hashStringToColor(String string) {
        if (this.colorMapping.containsKey(string)) {
            return this.colorMapping.get(string);
        }

        int hash = saltString(string).hashCode();

        int h = (hash & 0xFF0000) >> 16;
        int s = (hash & 0x00FF00) >> 8;
        int b = hash & 0x0000FF;
        Color newColor = new Color(h / 255f, s / 255f, b / 255f);
        this.colorMapping.put(string, newColor);
        return newColor;
    }

    private static String saltString(String string) {
        final String SALT = "RGB?HSL";
        float strHash = string.hashCode();
        StringBuilder sb = new StringBuilder(string);
        for (int i = 0; i < SALT.length(); i++) {
            sb.append(SALT.charAt(Math.abs(((Float)(strHash / SALT.charAt(i))).hashCode() % SALT.length())));
            sb.append(string);
        }

        return sb.toString();
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
    public void onDataChanged() {
        this.refresh();
    }

    @Override
    public void onDataChanged(String tag) {
        this.refresh();
    }

    public Collection<String> getVisibleStreams() {
        return this.dataSource.getTags();
    }

    public interface CoordinatesRangeChangedListener {
        public void onYRangeChanged(float topPeakValue, float bottomPeakValue);
        public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize);
    }
}
