package view.component.dataview;

import model.datasource.FilteredFiniteDataSource;
import model.datasource.FiniteLengthDataSource;
import model.datasource.StreamingDataSource;
import model.filter.DomainTransformFilter;
import view.component.plot.InteractivePlotView;
import view.component.plot.PlottingUtils;
import view.component.plugin.NavigationPlugin;
import view.component.plugin.SimilarStreamsPlottingPlugin;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.LinkedList;

/**
 * Created by maeglin89273 on 9/1/15.
 */
public class FeaturePanel extends JPanel {

    private static final String ANALYSIS_SOURCE_AND_DATA = "source and data";
    private static final String ANALYSIS_DATA = "data";
    private static final String ANALYSIS_SOURCE = "source";

    private static final String[] ANALYSIS_PLOT_MODES = new String[] {ANALYSIS_SOURCE_AND_DATA, ANALYSIS_DATA, ANALYSIS_SOURCE};
    private final FiniteLengthDataSource analysisSource;
    private int plotModeIdx = 0;

    private JCheckBox fftCkBox;
    private CustomPlotView fftPlot;
    private JCheckBox swtCkBox;
    private CustomPlotView swtPlot;
    private JButton plotModeBtn;
    private JCheckBox meanCkBox;
    private SimilarStreamsPlottingPlugin fftBgPlugin;
    private SimilarStreamsPlottingPlugin swtBgPlugin;
    private FilteredFiniteDataSource swtSource;
    private FilteredFiniteDataSource fftSource;


    public FeaturePanel(FiniteLengthDataSource analysisSource) {
        this.analysisSource = analysisSource;
        this.initComponents();
        this.setupListeners();
    }

    private void initPlots() {
        fftPlot = new CustomPlotView(60, 5f, 300, 125);
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

        swtPlot = new CustomPlotView(SignalConstants.SAMPLE_WINDOW_SIZE, 50f, 300, 125);
        swtPlot.setViewAllStreams(true);
        swtPlot.setLineWidth(1.3f);

        swtSource = new FilteredFiniteDataSource(analysisSource);
        swtSource.addFilters(DomainTransformFilter.SWT_COIF4);
        swtPlot.setDataSource(swtSource);

        navigationPlugin = new NavigationPlugin();
        swtPlot.addPlugin(navigationPlugin);
        navigationPlugin.setZoomingMode(NavigationPlugin.ZoomingMode.ZOOM_Y);

        this.swtBgPlugin = new SimilarStreamsPlottingPlugin();
        swtPlot.addPlugin(this.swtBgPlugin);
        this.swtBgPlugin.setEnabled(true);

        this.swtPlot.setEnabled(false);
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
        swtCkBox = new JCheckBox();
        swtCkBox.setEnabled(false);
        swtCkBox.setSelected(true);
        swtCkBox.setText("Stationary Wavelet Transform");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(swtCkBox, gbc);
        swtPlot.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(swtPlot, gbc);
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

        this.swtCkBox.addActionListener(e -> {
            setSWTEnabled(swtCkBox.isSelected());
        });


        this.meanCkBox.addActionListener(e -> {
            boolean enabled = meanCkBox.isSelected();
            fftBgPlugin.setMeanShowed(enabled);
            swtBgPlugin.setMeanShowed(enabled);
        });

        this.plotModeBtn.addActionListener(e -> {
            plotModeIdx = (plotModeIdx + 1) % ANALYSIS_PLOT_MODES.length;
            String mode = ANALYSIS_PLOT_MODES[plotModeIdx];
            plotModeBtn.setText(mode);

            switch (mode) {
                case ANALYSIS_SOURCE:
                    fftPlot.setShowSource(true);
                    swtPlot.setShowSource(true);
                    fftBgPlugin.setSamplesShowed(false);
                    swtBgPlugin.setSamplesShowed(false);
                    break;
                case ANALYSIS_DATA:
                    fftPlot.setShowSource(false);
                    swtPlot.setShowSource(false);
                    fftBgPlugin.setSamplesShowed(true);
                    swtBgPlugin.setSamplesShowed(true);
                    break;
                case ANALYSIS_SOURCE_AND_DATA:
                    fftPlot.setShowSource(true);
                    swtPlot.setShowSource(true);
                    fftBgPlugin.setSamplesShowed(true);
                    swtBgPlugin.setSamplesShowed(true);
            }
        });
    }

    public void setDataset(DatasetView dataset) {
        if (dataset == null) {
            this.fftBgPlugin.setDataSource(null);
            this.swtBgPlugin.setDataSource(null);
        } else {
            this.fftBgPlugin.setDataSource(dataset.getFFTDataSource());
            this.swtBgPlugin.setDataSource(dataset.getSWTDataSource());
        }
    }

    public void setEnabled(boolean enabled) {
        fftCkBox.setEnabled(enabled);
        swtCkBox.setEnabled(enabled);
        meanCkBox.setEnabled(enabled);
        plotModeBtn.setEnabled(enabled);
        if (enabled) {
            this.setFFTEnabled(fftCkBox.isSelected());
            this.setSWTEnabled(swtCkBox.isSelected());
        } else {
            this.setFFTEnabled(false);
            this.setSWTEnabled(false);
        }

    }

    private void setFFTEnabled(boolean enabled) {
        fftPlot.setEnabled(enabled);
        fftSource.setViewingSource(enabled);
    }


    private void setSWTEnabled(boolean enabled) {
        swtPlot.setEnabled(enabled);
        swtSource.setViewingSource(enabled);
    }


    public String[] getFeatureSelections() {
        List<String> option = new LinkedList<>();
        if (fftCkBox.isSelected()) {
            option.add("fft");
        }

        if (swtCkBox.isSelected()) {
            option.add("swt");
        }

        return option.toArray(new String[0]);
    }

    public InteractivePlotView getFFTPlot() {
        return this.fftPlot;
    }

    public InteractivePlotView getSWTPlot() {
        return this.swtPlot;
    }

    private class CustomPlotView extends InteractivePlotView {
        private boolean showSource = true;
        public CustomPlotView(int windowSize, float peakValue, int plotWidth, int plotHeight) {
            super(windowSize, peakValue, plotWidth, plotHeight);
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

