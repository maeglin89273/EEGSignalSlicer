package view.component;

import model.StreamingDataSource;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

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
    private JLabel progressLbl;
    private JSlider progressSlider;

    private StreamingDataSource dataSource;

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

        setEnableButtons(false);
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
        refreshPlot();
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

        this.progressSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = progressSlider.getValue();
                if (plotView.getPlotUpperBound() != value) {
                    setWindow(value - plotView.getWindowSize() + 1);
                }
            }
        });
    }

    public void setDataSource(StreamingDataSource dataSource) {
        this.dataSource = dataSource;
        this.plotView.setDataSource(dataSource);

        this.progressSlider.setMaximum((int) this.dataSource.getMaxStreamLength() - 1);
        this.progressSlider.setMinimum(plotView.getWindowSize() - 1);
        this.setEnableButtons(true);
        this.refreshPlot();
    }

    private void setEnableButtons(boolean enabled) {
        this.playBtn.setEnabled(enabled);
        this.forthBtn.setEnabled(enabled);
        this.backBtn.setEnabled(enabled);
        this.progressSlider.setEnabled(enabled);
    }

    public StreamingDataSource getDataSource() {
        return this.dataSource;
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

    public void setStreamVisible(String tag, boolean isVisible) {
        this.plotView.setStreamVisible(tag, isVisible);
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
        this.refreshPlot();
    }

    public void setRangeEndPosition(long pos) {
        this.slicePlugin.setEndPosition(pos);
        this.refreshPlot();
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
        endLbl.setText("250");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        this.add(endLbl, gbc);
        plotView = new PlotView(windowSize, peakValue);
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
        backBtn.setLabel("<<");
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
        progressLbl = new JLabel();
        progressLbl.setEnabled(true);
        progressLbl.setHorizontalAlignment(0);
        progressLbl.setHorizontalTextPosition(0);
        progressLbl.setText("0%");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(progressLbl, gbc);
        progressSlider = new JSlider();
        progressSlider.setValue(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(progressSlider, gbc);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        moveWindow(4);
        if (this.plotView.getPlotUpperBound() == this.dataSource.getMaxStreamLength() - 1) {
            this.pause();
        }

    }

    private void setWindow(int startingPos) {
        this.plotView.setPlotTo(startingPos);
        this.updateBoundLabels();
    }

    private void moveWindow(int delta) {
        this.plotView.movePlot(delta);
        this.updateBoundLabels();
        this.progressSlider.setValue((int)plotView.getPlotUpperBound());
    }

    private void updateBoundLabels() {
        long lower = this.plotView.getPlotLowerBound();
        long upper = this.plotView.getPlotUpperBound();
        this.startLbl.setText(Long.toString(lower));
        this.endLbl.setText(Long.toString(upper));
        if (this.dataSource != null) {
            this.progressLbl.setText(String.format("%.1f%%", 100 * upper / (double) dataSource.getMaxStreamLength()));

        }
    }

    private void updatePeakLabels() {
        this.posPeakLbl.setText("+" + String.format("%.1f", this.plotView.getPeakValue()));
        this.negPeakLbl.setText("-" + String.format("%.1f", this.plotView.getPeakValue()));
    }
    
    public void refreshPlot() {
        this.plotView.repaint();
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
                progressSlider.setMinimum(plotView.getWindowSize() - 1);
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
                    refreshPlot();
                    break;
                case DRAG_END_SLICER:
                    slicePlugin.setEndPosition(moveRecordAmount);
                    refreshPlot();
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


    public interface RangeChangedListener {
        public void onStartChanged(long lowerBound, long value, long upperBound);
        public void onEndChanged(long lowerBound, long value, long upperBound);
    }

}
