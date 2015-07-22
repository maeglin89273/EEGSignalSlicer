package view;

import model.EEGChannels;
import model.Filter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

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

    private static final int ANIMATION_INTERVAL = 20;

    private SlicerPlugin plugin;

    public StreamingPlot(int windowSize, float peakValue) {

        this.animator = new Timer(ANIMATION_INTERVAL, this);

        this.playing = false;
        setupUI(windowSize, peakValue);
        this.plugin = new SlicerPlugin();
        this.plotView.setPlugin(this.plugin);
        updatePeakLabels();
        updateBoundLabels();

        setupListeners(windowSize, peakValue);
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
                    if (animator.getDelay() > 20) {
                        animator.setDelay(animator.getDelay() - 10);
                    }
                } else {
                    moveWindow(50);
                }

            }
        });

        this.backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPlaying()) {
                    animator.setDelay(animator.getDelay() + 10);
                } else {
                    moveWindow(-50);
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
        this.plugin.setRangeChangeListener(rangeChangedListener);
    }

    public long getSliceStartPosition() {
        return this.plugin.getStartPosition();
    }

    public long getSliceEndPosition() {
        return this.plugin.getEndPosition();
    }

    public void setRangeStartPosition(long pos) {
        this.plugin.setStartPosition(pos);
        this.plotView.repaint();
    }

    public void setRangeEndPosition(long pos) {
        this.plugin.setEndPosition(pos);
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
                    plugin.setStartPosition(moveRecordAmount);
                    plotView.repaint();
                    break;
                case DRAG_END_SLICER:
                    plugin.setEndPosition(moveRecordAmount);
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
            if (plugin.isOnStartSlicer(e.getX())) {
                this.mouseMode = DRAG_START_SLICER;
            } else if (plugin.isOnEndSlicer(e.getX())) {
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
        public abstract void draw(Graphics2D g2);
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

        private SortedSet<Integer> visibleChannelNum;

        private float peakValue;

        private int[] xBuffer;
        private int[] yBuffer;

        private long startingPtr = 0;

        private Plugin plugin;

        public PlotView(int windowSize, float peakValue) {
            this.addComponentListener(this);


            this.setWindowSize(windowSize);
            this.setPeakValue(peakValue);

            this.setPreferredSize(PREFERRED_SIZE);

            this.visibleChannelNum = new TreeSet<Integer>(Collections.reverseOrder());

            this.setBackground(new Color(-1));
            this.setOpaque(true);

        }

        public void setPlugin(Plugin plugin) {
            this.plugin = plugin;
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
            if (this.plugin != null) {
                this.plugin.onXRangeChanged(this.getPlotLowerBound(), this.getPlotUpperBound(), this.getWindowSize());
            }
            this.repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = prepareGraphics(g);
            g2.setBackground(this.getBackground());
            g2.clearRect(0, 0, this.getWidth(), this.getHeight());

            if (getEegChannels() != null) {
                drawStreams(g2);
                if (plugin != null) {
                    plugin.draw(g2);
                }
            }
            g2.dispose();
        }

        private void drawStreams(Graphics2D g2) {
            for (Integer channelNum: visibleChannelNum) {
                drawStream(g2, channelNum);
            }
        }

        private void drawStream(Graphics2D g2, int channelNum) {
            g2.setColor(this.CHANNEL_COLORS[channelNum - 1]);
            loadYBuffer(channelNum);
            g2.drawPolyline(xBuffer, yBuffer, getWindowSize());

        }

        private void loadYBuffer(int channelNum) {
            double[] data = eegChannels.getChannel(channelNum);
            for (int i = 0; i < yBuffer.length; i++) {
                yBuffer[i] = panToPlotCoordinate(scaleIntoWindow(data[i + (int) this.startingPtr]));
            }

        }

        private int panToPlotCoordinate(double value) {
            return (int)(-value + this.getHeight() / 2.0);
        }

        private double scaleIntoWindow(double value) {
            return this.getHeight() * value / ( 2 * this.peakValue);
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
            g2.setStroke(STROKE);
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

        public void setPeakValue(float peakValue) {
            this.peakValue = peakValue;
            if (this.plugin != null) {
                this.plugin.onYRangeChanged(this.getPeakValue(), -this.getPeakValue());
            }
            this.repaint();

        }

        public float getPeakValue() {
            return this.peakValue;
        }

        public void setWindowSize(int windowSize) {
            this.xBuffer = new int[windowSize];
            this.yBuffer = new int[windowSize];
            updateXBuffer();
            if (this.plugin != null) {
                this.plugin.onXRangeChanged(this.getPlotLowerBound(), this.getPlotUpperBound(), this.getWindowSize());
            }
            this.repaint();
        }

        public int getWindowSize() {
            return this.xBuffer.length;
        }

        private void updateXBuffer() {
            double interval = this.getWidth() / (double)this.getWindowSize();
            for (int i = 0; i < this.xBuffer.length; i++) {
                this.xBuffer[i] = (int)(i * interval);
            }
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


    public interface RangeChangedListener {
        public void onStartChanged(long lowerBound, long value, long upperBound);
        public void onEndChanged(long lowerBound, long value, long upperBound);
    }

    private class SlicerPlugin extends Plugin {
        private long startPos;
        private double relativeStartPos;
        private long endPos;
        private double relativeEndPos;
        private final Stroke STROKE = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);

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
        public void draw(Graphics2D g2) {
            g2.setColor(Color.CYAN);

            int startKnifeX = (int)(plot.getWidth() * this.relativeStartPos);
            int endKnifeX = (int)(plot.getWidth() * this.relativeEndPos);

            g2.drawLine(startKnifeX, 0, startKnifeX, plot.getHeight());
            g2.drawLine(endKnifeX, 0, endKnifeX, plot.getHeight());
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
}
