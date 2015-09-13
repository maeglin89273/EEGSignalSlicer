package view.component.dataview;

import model.datasource.FilteredFiniteDataSource;
import model.datasource.FiniteLengthDataSource;
import model.datasource.StreamingDataSource;
import model.filter.DomainTransformFilter;
import net.razorvine.pyro.PyroProxy;
import view.component.plot.InteractivePlotView;
import view.component.plot.PlottingUtils;
import view.component.plugin.NavigationPlugin;
import view.component.plugin.SimilarStreamsPlottingPlugin;
import view.component.trainingview.phasepanel.FeatureExtractionPhase;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by maeglin89273 on 9/1/15.
 */
public class FeaturePanel extends JPanel {

    private static final String ANALYSIS_SOURCE_AND_DATA = "source and data";
    private static final String ANALYSIS_DATA = "data";
    private static final String ANALYSIS_SOURCE = "source";
    private static final String NONE = "none";

    private static final String[] ANALYSIS_PLOT_MODES = new String[] {NONE, ANALYSIS_SOURCE, ANALYSIS_DATA, ANALYSIS_SOURCE_AND_DATA};
    private final FiniteLengthDataSource analysisSource;
    private int plotModeIdx = 3;

    private JCheckBox fftCkBox;
    private CustomPlotView fftPlot;
    private JCheckBox wtCkBox;
    private CustomPlotView wtPlot;
    private JButton plotModeBtn;
    private JCheckBox meanCkBox;
    private SimilarStreamsPlottingPlugin fftBgPlugin;
    private SimilarStreamsPlottingPlugin wtBgPlugin;
    private FilteredFiniteDataSource wtSource;
    private FilteredFiniteDataSource fftSource;

    public FeaturePanel(FiniteLengthDataSource analysisSource) {
        this.analysisSource = analysisSource;

        this.initComponents();
        this.setupListeners();
    }

    private void initPlots() {
        fftPlot = new CustomPlotView("Fast Fourier Transform Plot", 60, 5f, 300, 125);
        fftPlot.setBaseline(PlottingUtils.Baseline.BOTTOM);
        fftPlot.setViewAllStreams(true);
        fftPlot.setLineWidth(1.3f);

        fftSource = new FilteredFiniteDataSource(analysisSource);
        fftSource.addFilters(DomainTransformFilter.FFT);
        fftPlot.setDataSource(fftSource);

        NavigationPlugin navigationPlugin = new NavigationPlugin();
        fftPlot.addPlugin(navigationPlugin);
        navigationPlugin.setZoomingMode(NavigationPlugin.ZoomingMode.ZOOM_Y);

        this.fftBgPlugin = new SimilarStreamsPlottingPlugin();
        fftPlot.addPlugin(this.fftBgPlugin);
        this.fftBgPlugin.setEnabled(true);

        this.fftPlot.setEnabled(false);

        wtPlot = new CustomPlotView("Wavelet Transform Plot", SignalConstants.SAMPLE_WINDOW_SIZE, 50f, 300, 125);
        wtPlot.setViewAllStreams(true);
        wtPlot.setLineWidth(1.3f);

        wtSource = new FilteredFiniteDataSource(analysisSource);
        wtSource.addFilters(DomainTransformFilter.WT);
        wtPlot.setDataSource(wtSource);

        navigationPlugin = new NavigationPlugin();
        wtPlot.addPlugin(navigationPlugin);
        navigationPlugin.setZoomingMode(NavigationPlugin.ZoomingMode.ZOOM_Y);

        this.wtBgPlugin = new SimilarStreamsPlottingPlugin();
        wtPlot.addPlugin(this.wtBgPlugin);
        this.wtBgPlugin.setEnabled(true);

        this.wtPlot.setEnabled(false);
    }

    private void initComponents() {
        this.initPlots();

        this.setLayout(new GridBagLayout());
        fftCkBox = new JCheckBox();
        fftCkBox.setEnabled(false);
        fftCkBox.setSelected(true);
        fftCkBox.setText("Fast Fourier Transform");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(fftCkBox, gbc);
        fftPlot.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(fftPlot, gbc);
        wtCkBox = new JCheckBox();
        wtCkBox.setEnabled(false);
        wtCkBox.setSelected(true);
        wtCkBox.setText("Stationary Wavelet Transform");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(wtCkBox, gbc);
        wtPlot.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(wtPlot, gbc);
        plotModeBtn = new JButton();
        plotModeBtn.setEnabled(false);
        plotModeBtn.setText("source and data");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(plotModeBtn, gbc);
        meanCkBox = new JCheckBox();
        meanCkBox.setEnabled(false);
        meanCkBox.setText("mean");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        this.add(meanCkBox, gbc);
    }
    
    private void setupListeners() {
        this.fftCkBox.addActionListener(e -> {
            setFFTEnabled(fftCkBox.isSelected());
        });

        this.wtCkBox.addActionListener(e -> {
            setWTEnabled(wtCkBox.isSelected());
        });


        this.meanCkBox.addActionListener(e -> {
            boolean enabled = meanCkBox.isSelected();
            fftBgPlugin.setMeanShowed(enabled);
            wtBgPlugin.setMeanShowed(enabled);
        });

        this.plotModeBtn.addActionListener(e -> {
            plotModeIdx = (plotModeIdx + 1) % ANALYSIS_PLOT_MODES.length;
            plotModeBtn.setText(ANALYSIS_PLOT_MODES[plotModeIdx]);

            boolean showSource = (plotModeIdx & 1) != 0;
            boolean showSamples = (plotModeIdx & 2) != 0;

            fftPlot.setShowSource(showSource);
            wtPlot.setShowSource(showSource);
            fftBgPlugin.setSamplesShowed(showSamples);
            wtBgPlugin.setSamplesShowed(showSamples);

        });
    }

    public void setDataset(CategoryPanel dataset) {
        if (dataset == null) {
            this.fftBgPlugin.setDataSource(null);
            this.wtBgPlugin.setDataSource(null);
        } else {
            this.fftBgPlugin.setDataSource(dataset.getFFTDataSource());
            this.wtBgPlugin.setDataSource(dataset.getWTDataSource());
        }
    }

    public void setEnabled(boolean enabled) {
        fftCkBox.setEnabled(enabled);
        wtCkBox.setEnabled(enabled);
        meanCkBox.setEnabled(enabled);
        plotModeBtn.setEnabled(enabled);
        if (enabled) {
            this.setFFTEnabled(fftCkBox.isSelected());
            this.setWTEnabled(wtCkBox.isSelected());
        } else {
            this.setFFTEnabled(false);
            this.setWTEnabled(false);
        }

    }

    void setFFTEnabled(boolean enabled) {
        fftPlot.setEnabled(enabled);
        fftSource.setViewingSource(enabled);
    }


    void setWTEnabled(boolean enabled) {
        wtPlot.setEnabled(enabled);
        wtSource.setViewingSource(enabled);
    }

    public InteractivePlotView getFFTPlot() {
        return this.fftPlot;
    }

    public InteractivePlotView getWTPlot() {
        return this.wtPlot;
    }

    private static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    public boolean setTransformationSettings(FeatureExtractionPhase phase, Map<String, Object> oldSettings, Map<String, Object> newSettings) {
        boolean updateFFT = false;
        boolean updateWT = false;
        if (oldSettings == null || !Objects.equals(oldSettings.get("window_size"), newSettings.get("window_size"))) {
            updateFFT = updateWT = true;
        } else {
            if (!Objects.equals(oldSettings.get("fast_fourier_transform"), newSettings.get("fast_fourier_transform"))) {
                updateFFT = true;
            }

            if (!Objects.equals(oldSettings.get("wavelet_transform"), newSettings.get("wavelet_transform"))) {
                updateWT = true;
            }
        }

        if (updateFFT) {
            List<Integer> freqRange = phase.getFFTFreqRange();
            int sampleRate = phase.getSampleRate();
            int windowSize = phase.getWindowSize();

            int plotFrom = (int) (freqRange.get(0) * windowSize / (double) sampleRate);
            int plotTo = (int) (freqRange.get(1) * windowSize / (double) sampleRate);

            fftPlot.setXTo(plotFrom);
            fftPlot.setWindowSize(plotTo - plotFrom + 1);
        }

        if (updateWT) {
            String wtType = phase.getWaveletTransformType();

            wtCkBox.setText(capitalize(wtType) + " Wavelet Transform");
            DomainTransformFilter wtFilter = DomainTransformFilter.WT;

            wtSource.replaceFilter(0, wtFilter);
            wtPlot.setWindowSize(wtSource.intLength());
        }

        return updateWT;
    }


    private class CustomPlotView extends InteractivePlotView {
        private boolean showSource = true;
        public CustomPlotView(String plotName, int windowSize, float peakValue, int plotWidth, int plotHeight) {
            super(plotName, windowSize, peakValue, plotWidth, plotHeight);

        }

        @Override
        public StreamingDataSource setDataSource(StreamingDataSource dataSource) {
            StreamingDataSource oldSource = super.setDataSource(dataSource);
            this.setWindowSize((int) dataSource.getCurrentLength());
            return oldSource;
        }

        public void setShowSource(boolean show) {
            if (this.showSource != show) {
                this.showSource = show;
                this.refresh();
            }
        }
        @Override
        protected void drawStreams(Graphics2D g2) {
            if (this.showSource) {
                super.drawStreams(g2);
            }
        }
    }
}

