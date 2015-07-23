package view.component;

import model.StreamingDataSource;
import sun.reflect.generics.factory.CoreReflectionFactory;
import view.component.plugin.PlotPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public class PlotView extends JComponent {
    private final Dimension PREFERRED_SIZE = new Dimension(750, 250);

    private final Stroke STROKE = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);

    private Collection<String> visibleStreamTags;
    private Map<String, Color> colorMapping;

    private float peakValue;

    private int[] xBuffer;
    private int[] yBuffer;

    private long startingPtr = 0;

    private List<PlotPlugin> plugins;
    private List<CoordinatesRangeChangedListener> rangeListeners;
    private StreamingDataSource dataSource;


    public PlotView(int windowSize, float peakValue) {

        this.plugins = new ArrayList<PlotPlugin>();
        this.rangeListeners = new ArrayList<CoordinatesRangeChangedListener>();
        this.colorMapping = new HashMap<String, Color>();
        this.visibleStreamTags = new LinkedList<String>();

        this.setPreferredSize(PREFERRED_SIZE);

        this.setWindowSize(windowSize);
        this.setPeakValue(peakValue);

        this.setAdapters();

        this.setBackground(new Color(-1));
        this.setOpaque(true);

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

    public void setDataSource(StreamingDataSource dataSource) {
        this.dataSource = dataSource;
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
        this.refresh();
    }

    public void addCoordinatesRangeChangedListener(CoordinatesRangeChangedListener listener) {
        this.rangeListeners.add(listener);
    }


    public long getPlotLowerBound() {
        return this.startingPtr;
    }

    public long getPlotUpperBound() {
        return this.startingPtr + getWindowSize() - 1;
    }

    public void movePlot(int delta) {
        this.setPlotTo(delta + this.startingPtr);
    }

    public void setPlotTo(long startingPoint) {
        if (startingPoint < 0) {
            startingPoint = 0;
        } else if (startingPoint + this.getWindowSize() - 1 >= dataSource.getMaxStreamLength()) {
            startingPoint = dataSource.getMaxStreamLength() - this.getWindowSize();
        }
        this.startingPtr = startingPoint;
        fireOnXRangeChanged();
        this.refresh();
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

        if (this.isDataSourceSet()) {
            drawBackPlugins(g2);
            g2.setStroke(STROKE);
            drawStreams(g2);
            drawFrontPlugins(g2);

        }
        g2.dispose();
    }

    private void drawBackPlugins(Graphics2D g2) {
        for (int i = plugins.size() - 1; i >= 0; i--) {
            plugins.get(i).drawBeforePlot(g2);
        }

    }

    private void drawFrontPlugins(Graphics2D g2) {
        for (int i = plugins.size() - 1; i >= 0; i--) {
            plugins.get(i).drawAfterPlot(g2);
        }
    }

    private void drawStreams(Graphics2D g2) {
        for (String tag : visibleStreamTags) {
            drawStream(g2, tag);
        }
    }

    private void drawStream(Graphics2D g2, String tag) {
        g2.setColor(hashStringToColor(tag));
        PlottingUtils.loadYBuffer(2 * this.getPeakValue(), this.getHeight(), dataSource.getDataOf(tag), yBuffer, (int) this.getPlotLowerBound());
        g2.drawPolyline(xBuffer, yBuffer, getWindowSize());
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

    public void setStreamVisible(String tag, boolean isVisible) {
        if (isVisible) {
            this.visibleStreamTags.add(tag);
        } else {
            this.visibleStreamTags.remove(tag);
        }
        this.refresh();
    }

    public Collection<String> getVisibleStreams() {
        return this.visibleStreamTags;
    }

    public void setPeakValue(float peakValue) {
        this.peakValue = peakValue;
        fireOnYRangeChanged();
        this.refresh();

    }

    public float getPeakValue() {
        return this.peakValue;
    }

    public void setWindowSize(int windowSize) {
        this.xBuffer = new int[windowSize];
        this.yBuffer = new int[windowSize];

        updateXBuffer();
        fireOnXRangeChanged();
        this.refresh();
    }

    public int getWindowSize() {
        return this.xBuffer.length;
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

    public interface CoordinatesRangeChangedListener {
        public void onYRangeChanged(float topPeakValue, float bottomPeakValue);
        public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize);
    }
}