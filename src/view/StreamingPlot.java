package view;

import model.EEGChannels;
import model.Filter;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Created by maeglin89273 on 7/21/15.
 */
public class StreamingPlot extends JPanel implements ActionListener {
    private JLabel startLbl;
    private JLabel endLbl;
    private PlotView plotView;
    private JButton playBtn;
    private JButton backBtn;
    private JButton forthBtn;

    private JLabel negPeakLbl;
    private JLabel posPeakLbl;
    
    private EEGChannels eegChannels;


    private Timer animator;

    private boolean playing;

    private static final int ANIMATION_INIT_INTERVAL = 20;
    private static final int ANIMATION_SPEED_GAP = 10;
    private SlicerPlugin slicePlugin;
    private ShadowPlugin shadowPlugin;

    public StreamingPlot(int windowSize, float peakValue) {

        this.animator = new Timer(ANIMATION_INIT_INTERVAL, this);

        this.playing = false;
        setupUI(windowSize, peakValue);
        setupPlugins();
        updatePeakLabels();
        updateBoundLabels();

        setupListeners(windowSize, peakValue);
    }

    public void wantShadow(boolean want) {
        if (want) {
            shadowPlugin.makeShadow();
        } else {
            shadowPlugin.clear();
        }
        plotView.repaint();
    }

    private void setupPlugins() {
        this.slicePlugin = new SlicerPlugin();
        this.shadowPlugin = new ShadowPlugin();
        this.plotView.addPlugin(this.slicePlugin);
        this.plotView.addPlugin(this.shadowPlugin);
    }

    private void setupListeners(int windowSize, float peakValue) {
        MouseInteractionHandler mouseHandler = new MouseInteractionHandler(windowSize, peakValue);
        this.plotView.addMouseListener(mouseHandler);
        this.plotView.addMouseWheelListener(mouseHandler);
        this.plotView.addMouseMotionListener(mouseHandler);

        this.playBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPlaying()) {
                    pause();
                } else {
                    play();
                }
            }
        });

        this.forthBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPlaying()) {
                    if (animator.getDelay() > ANIMATION_SPEED_GAP) {
                        animator.setDelay(animator.getDelay() - ANIMATION_SPEED_GAP);
                    }
                } else {
                    moveWindow(2);
                }

            }
        });

        this.backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPlaying()) {
                    animator.setDelay(animator.getDelay() + ANIMATION_SPEED_GAP);
                } else {
                    moveWindow(-2);
                }
            }
        });
    }

    public void setEEGChannels(EEGChannels eegChannels) {
        this.eegChannels = eegChannels;
        this.playBtn.setEnabled(true);
        this.plotView.repaint();
    }

    public EEGChannels getEegChannels() {
        return this.eegChannels;
    }

    public void setBandpassFilter(Filter filter) {
        this.eegChannels.setBandpassFilter(filter);
        plotView.repaint();
    }

    public boolean isPlaying() {
        return this.playing;
    }

    public void play() {
        if (!this.isPlaying()) {
            this.animator.start();
            this.playing = true;
            this.playBtn.setText("Pause");
        }
    }

    public void pause() {
        if (this.isPlaying()) {
            this.animator.stop();
            this.playing = false;
            this.playBtn.setText("Play");
        }
    }

    public void setChannelVisible(int channelNum, boolean isVisible) {
        this.plotView.setChannelVisible(channelNum, isVisible);
    }

    public void setRangeChangedListener(RangeChangedListener rangeChangedListener) {
        this.slicePlugin.setRangeChangeListener(rangeChangedListener);
    }

    public long getSliceStartPosition() {
        return this.slicePlugin.getStartPosition();
    }

    public long getSliceEndPosition() {
        return this.slicePlugin.getEndPosition();
    }

    public void setRangeStartPosition(long pos) {
        this.slicePlugin.setStartPosition(pos);
        this.plotView.repaint();
    }

    public void setRangeEndPosition(long pos) {
        this.slicePlugin.setEndPosition(pos);
        this.plotView.repaint();
    }

    private void setupUI(int windowSize, float peakValue) {

        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null));
        startLbl = new JLabel();
        startLbl.setText("0");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(startLbl, gbc);
        endLbl = new JLabel();
        endLbl.setText(Long.toString(windowSize));
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        this.add(endLbl, gbc);
        plotView = new PlotView(windowSize, peakValue); // new
        plotView.setLayout(new GridBagLayout());
        plotView.setBackground(new Color(-1));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(plotView, gbc);
        playBtn = new JButton();
        playBtn.setEnabled(false);
        playBtn.setText("Play");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(playBtn, gbc);
        backBtn = new JButton();
        backBtn.setText("<<");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(backBtn, gbc);
        forthBtn = new JButton();
        forthBtn.setLabel(">>");
        forthBtn.setText(">>");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(forthBtn, gbc);
        negPeakLbl = new JLabel();
        negPeakLbl.setText("-Vp");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        this.add(negPeakLbl, gbc);
        posPeakLbl = new JLabel();
        posPeakLbl.setText("+Vp");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        this.add(posPeakLbl, gbc);


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        moveWindow(4);
        if (this.plotView.getPlotUpperBound() == this.eegChannels.getDataLength() - 1) {
            this.pause();
        }

    }

    private void moveWindow(int delta) {
        this.plotView.movePlot(delta);
        this.updateBoundLabels();

    }

    private void updateBoundLabels() {
        this.startLbl.setText(Long.toString(this.plotView.getPlotLowerBound()));
        this.endLbl.setText(Long.toString(this.plotView.getPlotUpperBound()));
    }

    private void updatePeakLabels() {
        this.posPeakLbl.setText("+" + String.format("%.1f", this.plotView.getPeakValue()));
        this.negPeakLbl.setText("-" + String.format("%.1f", this.plotView.getPeakValue()));
    }

    private class MouseInteractionHandler implements MouseWheelListener, MouseMotionListener, MouseListener {
        private static final float SCALING_FACTOR = 1.2f;
        private static final int MAX_SCALING_LEVEL = 4;

        private int scalingLevel = 0;

        private final float originalPeakValue;
        private final int originalWindowSize;

        private final int DRAG_PLOT = 0;
        private final int DRAG_START_SLICER = 1;
        private final int DRAG_END_SLICER = 2;

        private int lastX = 0;
        private int mouseMode = DRAG_PLOT;

        public MouseInteractionHandler(int originalWindowSize, float originalPeakValue) {
            this.originalPeakValue = originalPeakValue;
            this.originalWindowSize = originalWindowSize;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (Math.abs(scalingLevel + e.getWheelRotation()) <= MAX_SCALING_LEVEL) {
                scalingLevel += e.getWheelRotation();
                double scale = Math.pow(SCALING_FACTOR, scalingLevel);
                plotView.setPeakValue((float) (originalPeakValue * scale));
                updatePeakLabels();
                plotView.setWindowSize((int) (originalWindowSize * scale));
                updateBoundLabels();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            int delta = this.lastX - e.getX();
            long moveRecordAmount = plotView.getPlotLowerBound() + (int)(plotView.getWindowSize() * e.getX() / (double) plotView.getWidth());
            switch (this.mouseMode) {
                case DRAG_PLOT:
                    moveWindow(delta);
                    break;
                case DRAG_START_SLICER:
                    slicePlugin.setStartPosition(moveRecordAmount);
                    plotView.repaint();
                    break;
                case DRAG_END_SLICER:
                    slicePlugin.setEndPosition(moveRecordAmount);
                    plotView.repaint();
                    break;
            }
            this.lastX = e.getX();

        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            this.lastX = e.getX();
            if (slicePlugin.isOnStartSlicer(e.getX())) {
                this.mouseMode = DRAG_START_SLICER;
            } else if (slicePlugin.isOnEndSlicer(e.getX())) {
                this.mouseMode = DRAG_END_SLICER;
            } else {
                this.mouseMode = DRAG_PLOT;
            }
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
    }


    public static abstract class Plugin {
        protected PlotView plot;
        public abstract void drawAfterPlot(Graphics2D g2);
        public abstract void drawBeforePlot(Graphics2D g2);
        public void setPlot(PlotView plot) {
            this.plot = plot;
        }

        public void onYRangeChanged(float topPeakValue, float bottomPeakValue) {

        }

        public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize) {

        }


    }


    private class PlotView extends JComponent implements ComponentListener {
        private final Dimension PREFERRED_SIZE = new Dimension(750, 250);

        private final Color[] CHANNEL_COLORS = new Color[] {Color.DARK_GRAY, Color.MAGENTA, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PINK, Color.RED, Color.BLACK};
        private final Stroke STROKE = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);

        private Collection<Integer> visibleChannelNum;

        private float peakValue;

        private int[] xBuffer;
        private int[] yBuffer;

        private long startingPtr = 0;

        private List<Plugin> plugins;

        public PlotView(int windowSize, float peakValue) {
            this.plugins = new ArrayList<Plugin>();
            this.addComponentListener(this);


            this.setWindowSize(windowSize);
            this.setPeakValue(peakValue);

            this.setPreferredSize(PREFERRED_SIZE);

            this.visibleChannelNum = new LinkedList<Integer>();

            this.setBackground(new Color(-1));
            this.setOpaque(true);

        }

        public void addPlugin(Plugin plugin) {
            this.plugins.add(plugin);
            plugin.setPlot(this);
            this.repaint();
        }

        public long getPlotLowerBound() {
            return this.startingPtr;
        }

        public long getPlotUpperBound() {
            return this.startingPtr + getWindowSize();
        }

        public void movePlot(int delta) {
            if (delta + this.startingPtr < 0) {
                this.startingPtr = 0;
            } else if (delta + this.getPlotUpperBound() >= eegChannels.getDataLength()) {
                this.startingPtr = eegChannels.getDataLength() - this.getWindowSize();
            } else {
                this.startingPtr += delta;
            }
            fireOnXRangeChanged();
            this.repaint();
        }

        private void fireOnXRangeChanged() {
            for (int i = plugins.size() - 1; i >= 0; i--) {
                plugins.get(i).onXRangeChanged(this.getPlotLowerBound(), this.getPlotUpperBound(), this.getWindowSize());
            }

        }

        private void fireOnYRangeChanged() {
            for (int i = plugins.size() - 1; i >= 0; i--) {
                plugins.get(i).onYRangeChanged(this.getPeakValue(), -this.getPeakValue());
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = prepareGraphics(g);
            g2.setBackground(this.getBackground());
            g2.clearRect(0, 0, this.getWidth(), this.getHeight());

            if (getEegChannels() != null) {
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
            for (Integer channelNum: visibleChannelNum) {
                drawStream(g2, channelNum);
            }
        }

        private void drawStream(Graphics2D g2, int channelNum) {
            g2.setColor(this.CHANNEL_COLORS[channelNum - 1]);
            PlottingUtils.loadYBuffer(2 * this.getPeakValue(), this.getHeight(), eegChannels.getChannel(channelNum), yBuffer, (int)this.getPlotLowerBound());
            g2.drawPolyline(xBuffer, yBuffer, getWindowSize());

        }

        private Graphics2D prepareGraphics(Graphics g) {
            if (!(g instanceof Graphics2D)) {
                return null;
            }

            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_COLOR_RENDERING, java.awt.RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2.setRenderingHint(java.awt.RenderingHints.KEY_STROKE_CONTROL, java.awt.RenderingHints.VALUE_STROKE_PURE);

            return g2;
        }

        public void setChannelVisible(int channelNum, boolean isVisible) {
            if (isVisible) {
                this.visibleChannelNum.add(channelNum);
            } else {
                this.visibleChannelNum.remove(channelNum);
            }
            this.repaint();
        }

        public Collection<Integer> getVisibleChannels() {
            return this.visibleChannelNum;
        }

        public void setPeakValue(float peakValue) {
            this.peakValue = peakValue;
            fireOnYRangeChanged();
            this.repaint();

        }

        public float getPeakValue() {
            return this.peakValue;
        }

        public void setWindowSize(int windowSize) {
            this.xBuffer = new int[windowSize];
            this.yBuffer = new int[windowSize];

            updateXBuffer();
            fireOnXRangeChanged();
            this.repaint();
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


        @Override
        public void componentResized(ComponentEvent e) {
            this.updateXBuffer();
            this.repaint();
        }

        @Override
        public void componentMoved(ComponentEvent e) {

        }

        @Override
        public void componentShown(ComponentEvent e) {

        }

        @Override
        public void componentHidden(ComponentEvent e) {

        }

    }

    public static final class PlottingUtils {
        public static void loadXBuffer(double coordinateWidth, int plotWidth, int[] xBuffer) {
            double interval = plotWidth / coordinateWidth;
            for (int i = 0; i < xBuffer.length; i++) {
                xBuffer[i] = (int)Math.round(i * interval);
            }

        }

        public static void loadYBuffer(double coordinateHeight, int plotHeight, double[] data, int[] yBuffer, int startIndex) {
            for (int i = 0; i < yBuffer.length; i++) {
                yBuffer[i] = panToPlotCoordinate(plotHeight, scaleIntoWindow(coordinateHeight, plotHeight, data[i + (int) startIndex]));
            }
        }

        public static int panToPlotCoordinate(int plotHeight, double value) {
            return (int)Math.round(-value + plotHeight / 2.0);
        }

        public static double scaleIntoWindow(double coordinateHeight, int plotHeight, double value) {
            return plotHeight * value / (coordinateHeight);
        }
    }

    public interface RangeChangedListener {
        public void onStartChanged(long lowerBound, long value, long upperBound);
        public void onEndChanged(long lowerBound, long value, long upperBound);
    }

    private class SlicerPlugin extends Plugin {
        private long startPos;
        private double relativeStartPos;
        private long endPos;
        private double relativeEndPos;
        private final Stroke STROKE = new BasicStroke(1.2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);

        private RangeChangedListener listener;

        public void setRangeChangeListener(RangeChangedListener listener) {
            this.listener = listener;
        }

        @Override
        public void setPlot(PlotView plot) {
            super.setPlot(plot);
            // careful the start bound
            this.endPos = plot.getPlotUpperBound();
            this.setStartPosition(plot.getPlotLowerBound());
            this.setEndPosition(plot.getPlotUpperBound());
        }

        @Override
        public void drawAfterPlot(Graphics2D g2) {
            g2.setStroke(STROKE);
            g2.setColor(Color.CYAN);

            int startKnifeX = (int)(plot.getWidth() * this.relativeStartPos);
            int endKnifeX = (int)(plot.getWidth() * this.relativeEndPos);

            g2.drawLine(startKnifeX, 0, startKnifeX, plot.getHeight());
            g2.drawLine(endKnifeX, 0, endKnifeX, plot.getHeight());
        }

        @Override
        public void drawBeforePlot(Graphics2D g2) {

        }

        public long getStartPosition() {
            return this.startPos;
        }

        public long getEndPosition() {
            return this.endPos;
        }

        public void setStartPosition(long startPosition) {
            if (startPosition < plot.getPlotLowerBound()) {
                this.startPos = plot.getPlotLowerBound();
            } else if (startPosition >= this.getEndPosition()) {
                this.startPos = endPos - 1;
            } else {
                this.startPos = startPosition;
            }
            this.relativeStartPos = (this.startPos - plot.getPlotLowerBound()) / (double)plot.getWindowSize();
            if (this.listener != null) {
                this.listener.onStartChanged(plot.getPlotLowerBound(), this.getStartPosition(), this.getEndPosition());
            }
        }

        public void setEndPosition(long endPosition) {
            if (endPosition > plot.getPlotUpperBound()) {
                this.endPos = plot.getPlotUpperBound();
            } else if (endPosition <= this.getStartPosition()) {
                this.endPos = startPos + 1;
            } else {
                this.endPos = endPosition;
            }
            this.relativeEndPos = (this.endPos - plot.getPlotLowerBound()) / (double)plot.getWindowSize();
            if (this.listener != null) {
                this.listener.onEndChanged(this.getStartPosition(), this.getEndPosition(), plot.getPlotUpperBound());
            }
        }

        @Override
        public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize) {
            this.startPos = (int)(this.relativeStartPos * windowSize) + plotLowerBound;
            this.endPos = (int)(this.relativeEndPos * windowSize) + plotLowerBound;

            if (this.listener != null) {
                this.listener.onStartChanged(plot.getPlotLowerBound(), this.getStartPosition(), this.getEndPosition());
                this.listener.onEndChanged(this.getStartPosition(), this.getEndPosition(), plot.getPlotUpperBound());
            }
        }

        private final float SLICER_TOUCH_RANGE = 5f;

        public boolean isOnStartSlicer(int pos) {
            return Math.abs(pos - (this.plot.getWidth() * this.relativeStartPos)) <= SLICER_TOUCH_RANGE;
        }

        public boolean isOnEndSlicer(int pos) {
            return Math.abs(pos - (this.plot.getWidth() * this.relativeEndPos)) <= SLICER_TOUCH_RANGE;
        }
    }

    private class ShadowPlugin extends Plugin {
        private final Stroke STROKE = new BasicStroke(3f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
        private final Color SHADOW_COLOR = new Color(0, 0, 0, 0.35f);

        private long startingPtr;
        private int[] yBuffer = null;
        private boolean shadowing;

        @Override
        public void drawAfterPlot(Graphics2D g2) {
            if (!this.shadowing) {
                return;
            }
            g2.setStroke(STROKE);
            g2.setColor(SHADOW_COLOR);
            for (Integer channelNum: plot.getVisibleChannels()) {
                PlottingUtils.loadYBuffer(2 * plot.getPeakValue(), plot.getHeight(), eegChannels.getChannel(channelNum), yBuffer, (int) this.startingPtr);
                g2.drawPolyline(this.plot.getXPoints(), yBuffer, yBuffer.length);
            }
        }

        @Override
        public void drawBeforePlot(Graphics2D g2) {

        }

        public void makeShadow() {
            int windowSize = plot.getWindowSize();
            this.startingPtr = plot.getPlotLowerBound();
            adjustBuffers(windowSize);
            this.shadowing = true;
        }

        public void clear() {
            this.shadowing = false;
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
    }
}
