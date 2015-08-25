package view.component.plot;

import model.datasource.FiniteLengthDataSource;
import view.component.plugin.NavigationPlugin;
import view.component.plugin.PlotPlugin;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by maeglin89273 on 7/21/15.
 */
public class PlaybackPlotControl extends JPanel implements ActionListener {
    private static final int SPEEED_FACTOR = -1;
    private static final int DEFAULT_SLIDING_SPEED = 3;

    private JLabel startLbl;
    private JLabel endLbl;
    private InteractivePlotView plot;
    private JButton playBtn;
    private JButton backBtn;
    private JButton forthBtn;

    private JLabel negPeakLbl;
    private JLabel posPeakLbl;
    private JLabel progressLbl;
    private JSlider playbackSlider;

    private Timer animator;

    private boolean playing;

    private static final int ANIMATION_INTERVAL = 20;
    private int plotSlidingSpeed = DEFAULT_SLIDING_SPEED;
    private JLabel speedLbl;

    public PlaybackPlotControl(int windowSize, float peakValue) {
        this(new InteractivePlotView(windowSize, peakValue));
    }

    public PlaybackPlotControl(InteractivePlotView plot) {
        this.animator = new Timer(ANIMATION_INTERVAL, this);

        this.playing = false;
        setupUI(plot);

        addPluginToPlot(new NavigationPlugin());

        setEnableControls(false);
        updateYDisplays(plot.getPeakValue(), -plot.getPeakValue());
        updateXDisplays(plot.getPlotLowerBound(), plot.getPlotUpperBound(), plot.getWindowSize());

        setupListeners(plot.getWindowSize(), plot.getPeakValue());
    }

    public void addPluginToPlot(PlotPlugin plugin) {
        this.plot.addPlugin(plugin);
    }

    private void setupListeners(int windowSize, float peakValue) {
        this.plot.addCoordinatesRangeChangedListener(new PlotView.CoordinatesRangeChangedListener() {
            @Override
            public void onYRangeChanged(float topPeakValue, float bottomPeakValue) {
                updateYDisplays(topPeakValue, bottomPeakValue);
            }

            @Override
            public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize) {
                updateXDisplays(plotLowerBound, plotUpperBound, windowSize);
            }
        });

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
                    plotSlidingSpeed += (plotSlidingSpeed == -1 ? 2 : 1);
                    updateSpeedLbl();
                } else {
                    plot.moveX(1);
                }

            }
        });

        this.backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPlaying()) {
                    plotSlidingSpeed -= (plotSlidingSpeed == 1 ? 2 : 1);
                    updateSpeedLbl();
                } else {
                    plot.moveX(-1);
                }
            }
        });

        this.playbackSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = playbackSlider.getValue();
                if (plot.getPlotUpperBound() != value) { // prevent setting X recursively
                    plot.setXTo(value - plot.getWindowSize() + 1);
                }
            }
        });
    }

    private void updateSpeedLbl() {
        if (this.isPlaying()) {
            this.speedLbl.setText(this.plotSlidingSpeed < 0? "<<" + (-this.plotSlidingSpeed): this.plotSlidingSpeed + ">>");
        } else {
            this.speedLbl.setText("||");
        }
    }

    public void setDataSource(FiniteLengthDataSource dataSource) {
        this.plot.setDataSource(dataSource);

        this.playbackSlider.setMaximum(dataSource.intLength() - 1);
        updateXDisplays(this.plot.getPlotLowerBound(), this.plot.getPlotUpperBound(), this.plot.getWindowSize());

        this.setEnableControls(true);
    }

    private void setEnableControls(boolean enabled) {
        this.playBtn.setEnabled(enabled);
        this.forthBtn.setEnabled(enabled);
        this.backBtn.setEnabled(enabled);
        this.playbackSlider.setEnabled(enabled);
    }

    public boolean isPlaying() {
        return this.playing;
    }

    public void play() {
        if (!this.isPlaying()) {
            this.animator.start();
            this.playing = true;
            this.playBtn.setText("Pause");
            this.updateSpeedLbl();
        }
    }

    public void pause() {
        if (this.isPlaying()) {
            this.animator.stop();
            this.playing = false;
            this.playBtn.setText("Play");
            this.updateSpeedLbl();
        }
    }

    public void setStreamVisible(String tag, boolean isVisible) {
        this.plot.setStreamVisible(tag, isVisible);
    }

    private void setupUI(InteractivePlotView plot) {
        
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
        this.plot = plot;
        this.plot.setBackground(new Color(-1));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(this.plot, gbc);
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
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(progressLbl, gbc);
        playbackSlider = new JSlider();
        playbackSlider.setValue(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(playbackSlider, gbc);
        speedLbl = new JLabel();
        speedLbl.setHorizontalAlignment(0);
        speedLbl.setHorizontalTextPosition(0);
        speedLbl.setText("||");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(speedLbl, gbc);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.plot.moveX(plotSlidingSpeed * SPEEED_FACTOR);
        if (this.playbackSlider.getValue() == this.playbackSlider.getMaximum() ||
            this.plot.getPlotLowerBound() == 0) {
            this.pause();
            this.plotSlidingSpeed = DEFAULT_SLIDING_SPEED;
        }
    }

    private void updateXDisplays(long plotLowerBound, long plotUpperBound, int windowSize) {

        this.startLbl.setText(Long.toString(plotLowerBound));
        this.endLbl.setText(Long.toString(plotUpperBound));

        this.playbackSlider.setValue((int) plotUpperBound);
        if (this.playbackSlider.getMinimum() != plot.getWindowSize() - 1) {
            this.playbackSlider.setMinimum(plot.getWindowSize() - 1);
        }

        this.progressLbl.setText(String.format("%.1f%%", 100 * plotUpperBound / (double) this.playbackSlider.getMaximum()));
    }

    private void updateYDisplays(float topPeakValue, float bottomPeakValue) {
        this.posPeakLbl.setText(String.format("%.1e", topPeakValue));
        this.negPeakLbl.setText(String.format("%.1e", bottomPeakValue));
    }
    
    public void refreshPlot() {
        this.plot.refresh();
    }

}
