package view.component.dataview;

import model.DataFileUtils;
import model.LearnerProxy;
import model.datasource.FilteredFiniteDataSource;
import model.datasource.FragmentDataSource;
import model.datasource.StreamingDataSource;
import model.filter.DomainTransformFilter;
import view.component.plot.InteractivePlotView;
import view.component.plot.PlotView;
import view.component.plot.PlottingUtils;
import view.component.plugin.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

/**
 * Created by maeglin89273 on 8/20/15.
 */
public class TrainingPanel extends JPanel {

    private static final String ANALYSIS_SOURCE_AND_DATA = "source and data";
    private static final String ANALYSIS_DATA = "data";
    private static final String ANALYSIS_SOURCE = "source";

    private static final String[] ANALYSIS_PLOT_MODES = new String[] {ANALYSIS_SOURCE_AND_DATA, ANALYSIS_DATA, ANALYSIS_SOURCE};

    private int plotModeIdx =0;
    private static final int SAMPLE_WINDOW_SIZE = 736;
    private final DatasetViewGroupManager datasetManager;

    private Box datasetBox;
    private JButton addDataBtn;
    private JButton trainBtn;
    private JButton newActionBtn;
    private JCheckBox trainingCkBox;
    private JTextField actionField;
    private CustomPlotView fftSpectrumPlot;
    private CustomPlotView dwtSpectrumPlot;

    private RangePlugin transformRange;
    private DatasetView selectedDatasetView;
    private JScrollPane scrollPane;
    private SimilarStreamsPlottingPlugin fftBgPlugin;
    private SimilarStreamsPlottingPlugin dwtBgPlugin;
    private JButton plotModeBtn;
    private JButton saveDatasetBtn;
    private JButton loadDatasetBtn;
    private JSpinner rangeStartSpinner;
    private SpinnerNumberModel rangeSpinnerModel;
    private JLabel msgLbl;
    private JButton plot2DBtn;
    private JButton plot3DBtn;
    private JCheckBox predictCkBox;
    private JButton renameBtn;
    private JCheckBox coordinatesCkBox;

    private boolean predicting = false;

    LearnerProxy learner = new LearnerProxy(new LearnerProxy.TrainingCompleteCallback() {
        @Override
        public void trainDone(double score) {
            msgLbl.setText("Train done. Score: " + String.format("%.2f%%", 100 * score));
            predictCkBox.setEnabled(true);
        }

        @Override
        public void trainFail() {
            msgLbl.setText("Train fail");
            closePrediction();
        }
    });
    private PointPositionPlugin fftPPPlugin;
    private PointPositionPlugin mainPlotPPPlugin;


    public TrainingPanel() {
        this.initComponents();
        this.initPlots();
        this.datasetManager = new DatasetViewGroupManager();
        this.setupListeners();
    }

    private void initComponents() {
        this.setLayout(new GridBagLayout());
        actionField = new JTextField();
        actionField.setEnabled(false);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(actionField, gbc);
        trainingCkBox = new JCheckBox();
        trainingCkBox.setText("Training");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(trainingCkBox, gbc);
        scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(21);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 8;
        gbc.gridheight = 8;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(scrollPane, gbc);
        newActionBtn = new JButton();
        newActionBtn.setEnabled(false);
        newActionBtn.setText("new action");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(newActionBtn, gbc);
        rangeStartSpinner = new JSpinner();
        rangeStartSpinner.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(rangeStartSpinner, gbc);
        addDataBtn = new JButton();
        addDataBtn.setEnabled(false);
        addDataBtn.setText("add data");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(addDataBtn, gbc);
        msgLbl = new JLabel();
        msgLbl.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(msgLbl, gbc);
        trainBtn = new JButton();
        trainBtn.setEnabled(false);
        trainBtn.setText("train");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(trainBtn, gbc);
        predictCkBox = new JCheckBox();
        predictCkBox.setEnabled(false);
        predictCkBox.setText("predict");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        this.add(predictCkBox, gbc);
        renameBtn = new JButton();
        renameBtn.setEnabled(false);
        renameBtn.setText("rename");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(renameBtn, gbc);
        plotModeBtn = new JButton();
        plotModeBtn.setEnabled(false);
        plotModeBtn.setSelected(true);
        plotModeBtn.setText("source and data");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(plotModeBtn, gbc);
        coordinatesCkBox = new JCheckBox();
        coordinatesCkBox.setEnabled(false);
        coordinatesCkBox.setText("coordinates");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(coordinatesCkBox, gbc);
        loadDatasetBtn = new JButton();
        loadDatasetBtn.setEnabled(false);
        loadDatasetBtn.setText("load...");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(loadDatasetBtn, gbc);
        saveDatasetBtn = new JButton();
        saveDatasetBtn.setEnabled(false);
        saveDatasetBtn.setText("save");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(saveDatasetBtn, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 4;
        gbc.gridheight = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        this.add(spacer1, gbc);
        plot3DBtn = new JButton();
        plot3DBtn.setEnabled(false);
        plot3DBtn.setText("plot 3D");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(plot3DBtn, gbc);
        plot2DBtn = new JButton();
        plot2DBtn.setEnabled(false);
        plot2DBtn.setText("plot 2D");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(plot2DBtn, gbc);


        datasetBox = SingleDirectionBox.createHorizontalBox();

        scrollPane.setViewportView(datasetBox);
        rangeSpinnerModel = (SpinnerNumberModel)rangeStartSpinner.getModel();
        rangeSpinnerModel.setMinimum(0);
    }

    private void initPlots() {
        this.fftPPPlugin = new PointPositionPlugin(250.0 / SAMPLE_WINDOW_SIZE);
        this.mainPlotPPPlugin = new PointPositionPlugin(0.004);

        this.transformRange = new RangePlugin(Color.CYAN, SAMPLE_WINDOW_SIZE);
        this.transformRange.setRangeChangedListener(new RangePlugin.RangeChangedListener() {
            @Override
            public void onStartChanged(long lowerBound, long value, long upperBound) {
                if ((Integer) rangeSpinnerModel.getValue() != value) {
                    rangeSpinnerModel.setValue((int) (value));
                    if (predicting) {
                        predictSignal();
                    }
                }
            }

            @Override
            public void onEndChanged(long lowerBound, long value, long upperBound) {

            }
        });

        fftSpectrumPlot = new CustomPlotView(60, 1250f, 300, 125);
        fftSpectrumPlot.setBaseline(PlottingUtils.Baseline.BOTTOM);
        fftSpectrumPlot.setViewAllStreams(true);
        fftSpectrumPlot.setLineWidth(1.3f);

        FilteredFiniteDataSource fftSource = new FilteredFiniteDataSource(this.transformRange.getRangedDataSource());
        fftSource.addFilter(DomainTransformFilter.FFT);
        fftSpectrumPlot.setDataSource(fftSource);


        NavigationPlugin navigationPlugin = new NavigationPlugin();
        fftSpectrumPlot.addPlugin(navigationPlugin);
        navigationPlugin.setZoomingMode(NavigationPlugin.ZoomingMode.ZOOM_Y);

        fftSpectrumPlot.addPlugin(this.fftPPPlugin);

        this.fftBgPlugin = new SimilarStreamsPlottingPlugin();
        fftSpectrumPlot.addPlugin(this.fftBgPlugin);
        this.fftBgPlugin.setEnabled(true);

        this.fftSpectrumPlot.setEnabled(false);

        dwtSpectrumPlot = new CustomPlotView(SAMPLE_WINDOW_SIZE, 50f, 300, 125);
        dwtSpectrumPlot.setViewAllStreams(true);
        dwtSpectrumPlot.setLineWidth(1.3f);

        FilteredFiniteDataSource dwtSource = new FilteredFiniteDataSource(this.transformRange.getRangedDataSource());
        dwtSource.addFilter(DomainTransformFilter.DWT_COIF4);
        dwtSpectrumPlot.setDataSource(dwtSource);

        navigationPlugin = new NavigationPlugin();
        dwtSpectrumPlot.addPlugin(navigationPlugin);
        navigationPlugin.setZoomingMode(NavigationPlugin.ZoomingMode.ZOOM_Y);

        this.dwtBgPlugin = new SimilarStreamsPlottingPlugin();
        dwtSpectrumPlot.addPlugin(this.dwtBgPlugin);
        this.dwtBgPlugin.setEnabled(true);

        this.dwtSpectrumPlot.setEnabled(false);


    }

    private void setupListeners() {
        this.trainingCkBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean trainingOn = trainingCkBox.isSelected();
                fftSpectrumPlot.setEnabled(trainingOn);
                dwtSpectrumPlot.setEnabled(trainingOn);
                newActionBtn.setEnabled(trainingOn);
                addDataBtn.setEnabled(trainingOn && selectedDatasetView != null);
                loadDatasetBtn.setEnabled(trainingOn);
                saveDatasetBtn.setEnabled(trainingOn);
                trainBtn.setEnabled(trainingOn);
                actionField.setEnabled(trainingOn);
                plotModeBtn.setEnabled(trainingOn);
                transformRange.setEnabled(trainingOn);
                rangeStartSpinner.setEnabled(trainingOn);
                plot2DBtn.setEnabled(trainingOn);
                plot3DBtn.setEnabled(trainingOn);
                renameBtn.setEnabled(trainingOn && selectedDatasetView != null);
                predictCkBox.setEnabled(trainingOn && predicting);
                coordinatesCkBox.setEnabled(trainingOn);
            }
        });

        this.coordinatesCkBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = coordinatesCkBox.isSelected();
                fftPPPlugin.setEnabled(enabled);
                mainPlotPPPlugin.setEnabled(enabled);
            }
        });

        this.plotModeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                plotModeIdx = (plotModeIdx + 1) % ANALYSIS_PLOT_MODES.length;
                String mode = ANALYSIS_PLOT_MODES[plotModeIdx];
                plotModeBtn.setText(mode);

                switch (mode) {
                    case ANALYSIS_SOURCE:
                        fftSpectrumPlot.setShowSource(true);
                        dwtSpectrumPlot.setShowSource(true);
                        fftBgPlugin.setEnabled(false);
                        dwtBgPlugin.setEnabled(false);
                        break;
                    case ANALYSIS_DATA:
                        fftSpectrumPlot.setShowSource(false);
                        dwtSpectrumPlot.setShowSource(false);
                        fftBgPlugin.setEnabled(true);
                        dwtBgPlugin.setEnabled(true);
                        break;
                    case ANALYSIS_SOURCE_AND_DATA:
                        fftSpectrumPlot.setShowSource(true);
                        dwtSpectrumPlot.setShowSource(true);
                        fftBgPlugin.setEnabled(true);
                        dwtBgPlugin.setEnabled(true);
                }
            }
        });

        this.newActionBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newAction = actionField.getText().trim();
                if (!newAction.isEmpty()) {
                    datasetBox.add(new DatasetView(newAction, datasetManager), 0);
                    layoutDatasetPanel();
                }
            }
        });

        this.renameBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedDatasetView.setTag(actionField.getText().trim());
            }
        });

        this.rangeSpinnerModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int newPos = rangeSpinnerModel.getNumber().intValue();
                if (newPos != transformRange.getStartPosition()) {
                    PlotView plot = transformRange.getPlot();

                    int offset = (int) (transformRange.getStartPosition() - plot.getPlotLowerBound());
                    plot.setXTo(newPos - offset);
                }
            }
        });

        this.addDataBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedDatasetView.addNewData(makeFragmentDataSource(selectedDatasetView.getTag()));
                layoutDatasetPanel();
            }
        });

        this.saveDatasetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Collection<FragmentDataSource> fullDataset = collectDatasets(false);
                DataFileUtils.getInstance().saveFragmentDataSources("traning data-" + new Date().toString(), fullDataset);
            }
        });

        this.loadDatasetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File dir = DataFileUtils.getInstance().loadFileDialog(TrainingPanel.this, "dir");
                if (dir != null) {
                    discardAllDatasets();
                    loadAndBuildDataset(dir);
                }
            }

            private void discardAllDatasets() {
                for (int i = 0; i < datasetBox.getComponentCount(); i++) {
                    DatasetView view = (DatasetView) datasetBox.getComponent(i);
                    view.discardDataset();
                }

                datasetBox.removeAll();
                selectedDatasetView = null;
                fftBgPlugin.setDataSource(null);
                dwtBgPlugin.setDataSource(null);
                addDataBtn.setEnabled(false);
            }

            private void loadAndBuildDataset(File dir) {
                Map<String, Collection<FragmentDataSource>> dataset = DataFileUtils.getInstance().loadFragmentDataSource(dir);
                for (Map.Entry<String, Collection<FragmentDataSource>> pair : dataset.entrySet()) {
                    DatasetView actionView = new DatasetView(pair.getKey(), datasetManager);

                    for (FragmentDataSource dataSource : pair.getValue()) {
                        actionView.addNewData(dataSource);
                    }
                    datasetBox.add(actionView, 0);
                }

                layoutDatasetPanel();
            }
        });

        this.plot2DBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                learner.prepareData(collectDatasets(true), dwtSpectrumPlot.getVisibleStreams());
                learner.pcaPlot2D();
            }
        });

        this.plot3DBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                learner.prepareData(collectDatasets(true), dwtSpectrumPlot.getVisibleStreams());
                learner.pcaPlot3D();
            }
        });

        this.trainBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                learner.prepareData(collectDatasets(true), dwtSpectrumPlot.getVisibleStreams());
                msgLbl.setText("Training...");
                learner.train();
            }
        });

        this.predictCkBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (predictCkBox.isSelected()) {
                    predicting = true;
                    predictSignal();
                } else {
                    predicting = false;
                }
            }
        });
    }

    private void predictSignal() {
        String result = learner.predict(makeFragmentDataSource("predict"));

        if (result == null) {
            msgLbl.setText("ERROR: please retrain the model");
            closePrediction();
        } else {
            msgLbl.setText("predict result: " + result);
        }
    }

    private void closePrediction() {
        predicting = false;
        predictCkBox.setSelected(false);
        predictCkBox.setEnabled(false);
    }


    private FragmentDataSource makeFragmentDataSource(String tag) {
        long startingPos = transformRange.getStartPosition();
        int length = transformRange.getRange();
        StreamingDataSource dataSource = transformRange.getPlot().getDataSource();
        return new FragmentDataSource(tag, startingPos, length, dataSource);
    }

    private Collection<FragmentDataSource> collectDatasets(boolean selectedDataOnly) {
        Collection<FragmentDataSource> fullDataset = new LinkedList<>();
        for (int i = 0; i < datasetBox.getComponentCount(); i++) {
            DatasetView view = (DatasetView) datasetBox.getComponent(i);
            fullDataset.addAll(view.getAllData(selectedDataOnly));
        }
        return fullDataset;
    }

    public InteractivePlotView getFFTSpectrumPlot() {
        return this.fftSpectrumPlot;
    }

    public InteractivePlotView getDWTSpectrumPlot() {
        return this.dwtSpectrumPlot;
    }

    public PlotPlugin getFrequencySpectrumPlugin() {
        return this.transformRange;
    }

    public PlotPlugin getPointPositionPlugin() {
        return this.mainPlotPPPlugin;
    }

    class DatasetViewGroupManager implements ActionListener {
        private final String VIEW_KEY = "dataset_view";
        private final String RDO_BTN_KEY = "tag_rdo_box";

        private ButtonGroup datasetViewGroup;

        private DatasetViewGroupManager() {
            this.datasetViewGroup = new ButtonGroup();
        }

        public void setupViewComponents(DatasetView view, JRadioButton tagRdoBox, JButton removeBtn) {
            this.datasetViewGroup.add(tagRdoBox);
            tagRdoBox.putClientProperty(VIEW_KEY, view);
            removeBtn.putClientProperty(VIEW_KEY, view);
            removeBtn.putClientProperty(RDO_BTN_KEY, tagRdoBox);
            removeBtn.addActionListener(this);
            tagRdoBox.addActionListener(this);
            tagRdoBox.setSelected(true);
            this.handleDatasetSelected(view);

        }

        private void handleDatasetSelected(DatasetView view) {
            selectedDatasetView = view;
            fftBgPlugin.setDataSource(view.getFFTDataSource());
            dwtBgPlugin.setDataSource(view.getDWTDataSource());
            addDataBtn.setEnabled(true);
            renameBtn.setEnabled(true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JComponent source = (JComponent) e.getSource();
            DatasetView view = (DatasetView) source.getClientProperty(VIEW_KEY);
            if (source instanceof JRadioButton) {
                selectedDatasetView = view;
                this.handleDatasetSelected(view);
            } else {
                this.datasetViewGroup.remove((AbstractButton) source.getClientProperty(RDO_BTN_KEY));
                view.discardDataset();
                datasetBox.remove(view);
                if (view == selectedDatasetView) {
                    this.handleDatasetUnselected();
                }
                layoutDatasetPanel();
            }

        }

        private void handleDatasetUnselected() {
            selectedDatasetView = null;
            fftBgPlugin.setDataSource(null);
            dwtBgPlugin.setDataSource(null);
            addDataBtn.setEnabled(false);
            renameBtn.setEnabled(false);
        }
    }

    private void layoutDatasetPanel() {
        scrollPane.validate();
        scrollPane.repaint();
    }

    private class CustomPlotView extends InteractivePlotView {
        private boolean showSource = true;
        public CustomPlotView(int windowSize, float peakValue, int plotWidth, int plotHeight) {
            super(windowSize, peakValue, plotWidth, plotHeight);
        }

        @Override
        public void setDataSource(StreamingDataSource dataSource) {
            super.setDataSource(dataSource);
            this.setWindowSize((int) dataSource.getCurrentLength());
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
