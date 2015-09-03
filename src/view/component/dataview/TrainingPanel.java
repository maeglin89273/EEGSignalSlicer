package view.component.dataview;

import model.DataFileUtils;
import model.LearnerProxy;
import model.datasource.FragmentDataSource;
import view.component.plot.PlotView;
import view.component.plugin.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

/**
 * Created by maeglin89273 on 8/20/15.
 */
public class TrainingPanel extends JPanel {


    private final DatasetViewGroupManager datasetManager;

    private Box datasetBox;
    private JButton addDataBtn;
    private JButton trainBtn;
    private JButton newCategoryBtn;
    private JCheckBox trainingCkBox;
    private JTextField actionField;

    private RangePlugin transformRange;
    private DatasetView selectedDatasetView;
    private JScrollPane scrollPane;
    private JButton saveDatasetBtn;
    private JButton loadDatasetBtn;
    private JSpinner rangeStartSpinner;
    private SpinnerNumberModel rangeSpinnerModel;
    private MessageBar msgBar;
    private JButton plot2DBtn;
    private JButton plot3DBtn;
    private JCheckBox predictCkBox;
    private JButton renameBtn;
    private JCheckBox coordinatesCkBox;
    private JButton addDatasetBtn;
    private FeaturePanel featurePanel;

    private boolean predicting = false;

    LearnerProxy learner = new LearnerProxy(new LearnerProxy.TrainingCompleteCallback() {
        @Override
        public void trainDone(Map<String, Object> trainingReport) {
            msgBar.setMessage("");
            TrainingReportDialog.showReport(trainingReport);

            predictCkBox.setEnabled(true);
        }

        @Override
        public void trainFail() {
            msgBar.setMessage("ERROR: Train fail");
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
        gbc.gridwidth = 9;
        gbc.gridheight = 9;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(scrollPane, gbc);
        newCategoryBtn = new JButton();
        newCategoryBtn.setEnabled(false);
        newCategoryBtn.setText("new category");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(newCategoryBtn, gbc);
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
        msgBar = new MessageBar();
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(msgBar, gbc);
        renameBtn = new JButton();
        renameBtn.setEnabled(false);
        renameBtn.setText("rename");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(renameBtn, gbc);
        loadDatasetBtn = new JButton();
        loadDatasetBtn.setEnabled(false);
        loadDatasetBtn.setText("load...");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(loadDatasetBtn, gbc);
        saveDatasetBtn = new JButton();
        saveDatasetBtn.setEnabled(false);
        saveDatasetBtn.setText("save");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(saveDatasetBtn, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 5;
        gbc.gridheight = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        this.add(spacer1, gbc);
        trainBtn = new JButton();
        trainBtn.setEnabled(false);
        trainBtn.setText("train");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(trainBtn, gbc);
        plot3DBtn = new JButton();
        plot3DBtn.setEnabled(false);
        plot3DBtn.setText("plot 3D");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(plot3DBtn, gbc);
        plot2DBtn = new JButton();
        plot2DBtn.setEnabled(false);
        plot2DBtn.setText("plot 2D");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(plot2DBtn, gbc);
        coordinatesCkBox = new JCheckBox();
        coordinatesCkBox.setEnabled(false);
        coordinatesCkBox.setText("coordinates");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(coordinatesCkBox, gbc);
        predictCkBox = new JCheckBox();
        predictCkBox.setEnabled(false);
        predictCkBox.setText("predict");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(predictCkBox, gbc);
        addDatasetBtn = new JButton();
        addDatasetBtn.setEnabled(false);
        addDatasetBtn.setText("add...");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(addDatasetBtn, gbc);


        datasetBox = SingleDirectionBox.createHorizontalBox();

        scrollPane.setViewportView(datasetBox);
        rangeSpinnerModel = (SpinnerNumberModel)rangeStartSpinner.getModel();
        rangeSpinnerModel.setMinimum(0);
    }

    public JPanel getFeaturePanel() {
        return this.featurePanel;
    }

    private void initPlots() {
        this.fftPPPlugin = new PointPositionPlugin(SignalConstants.OPENBCI_SAMPLING_RATE / SignalConstants.SAMPLE_WINDOW_SIZE);
        this.mainPlotPPPlugin = new PointPositionPlugin(1 / SignalConstants.OPENBCI_SAMPLING_RATE);

        this.transformRange = new RangePlugin(Color.CYAN, SignalConstants.SAMPLE_WINDOW_SIZE);
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

        this.featurePanel = new FeaturePanel(this.transformRange.getRangedDataSource());
        this.featurePanel.getFFTPlot().addPlugin(this.fftPPPlugin);
    }

    private void setupListeners() {
        this.trainingCkBox.addActionListener(e -> {
            boolean trainingOn = trainingCkBox.isSelected();
            featurePanel.setEnabled(trainingOn);
            newCategoryBtn.setEnabled(trainingOn);
            addDataBtn.setEnabled(trainingOn && selectedDatasetView != null);
            loadDatasetBtn.setEnabled(trainingOn);
            saveDatasetBtn.setEnabled(trainingOn);
            trainBtn.setEnabled(trainingOn);
            actionField.setEnabled(trainingOn);
            transformRange.setEnabled(trainingOn);
            rangeStartSpinner.setEnabled(trainingOn);
            plot2DBtn.setEnabled(trainingOn);
            plot3DBtn.setEnabled(trainingOn);
            renameBtn.setEnabled(trainingOn && selectedDatasetView != null);
            predictCkBox.setEnabled(trainingOn && predicting);
            coordinatesCkBox.setEnabled(trainingOn);
            addDatasetBtn.setEnabled(trainingOn);
        });

        this.coordinatesCkBox.addActionListener(e -> {
            boolean enabled = coordinatesCkBox.isSelected();
            fftPPPlugin.setEnabled(enabled);
            mainPlotPPPlugin.setEnabled(enabled);
        });



        this.newCategoryBtn.addActionListener(e -> {
            String newAction = actionField.getText().trim();
            if (!newAction.isEmpty()) {
                datasetBox.add(new DatasetView(newAction, datasetManager), 0);
                layoutDatasetPanel();
            }
        });

        this.renameBtn.addActionListener(e -> selectedDatasetView.setTag(actionField.getText().trim()));

        this.rangeSpinnerModel.addChangeListener(e -> {
            int newPos = rangeSpinnerModel.getNumber().intValue();
            if (newPos != transformRange.getStartPosition()) {
                PlotView plot = transformRange.getPlot();

                int offset = (int) (transformRange.getStartPosition() - plot.getPlotLowerBound());
                plot.setXTo(newPos - offset);
            }
        });

        this.addDataBtn.addActionListener(e -> {
            selectedDatasetView.addNewData(transformRange.makeFragmentDataSource(selectedDatasetView.getTag()));
            layoutDatasetPanel();
        });

        this.saveDatasetBtn.addActionListener(e -> {
            msgBar.setMessage("Saving new dataset...");
            Thread saver = new Thread() {
                @Override
                public void run() {
                    Collection<FragmentDataSource> fullDataset = collectDatasets(false);
                    String datasetName = "training data-" + new Date().toString();
                    DataFileUtils.getInstance().saveFragmentDataSources(datasetName, fullDataset);
                    msgBar.setMessage("Saved as \"" + datasetName + "\"");
                }
            };
            saver.setDaemon(true);
            saver.start();

        });

        this.loadDatasetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File dir = DataFileUtils.getInstance().loadFileDialog(TrainingPanel.this, "dir");
                if (dir != null) {
                    Thread loader = new Thread() {
                        @Override
                        public void run() {
                            msgBar.setProgressIndeterminate();
                            discardAllDatasets();
                            loadAndBuildDataset(dir);
                        }
                    };
                    loader.setDaemon(true);
                    loader.start();

                }
            }

        });

        this.addDatasetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File dir = DataFileUtils.getInstance().loadFileDialog(TrainingPanel.this, "dir");
                if (dir != null) {
                    Thread loader = new Thread() {
                        @Override
                        public void run() {
                            msgBar.setProgressIndeterminate();
                            loadAndBuildDataset(dir);
                        }
                    };
                    loader.setDaemon(true);
                    loader.start();
                }
            }

        });

        this.plot2DBtn.addActionListener(e -> {
            learner.prepareData(featurePanel.getFeatureSelections(), collectDatasets(true), transformRange.getPlot().getVisibleStreams());
            learner.pcaPlot2D();
        });

        this.plot3DBtn.addActionListener(e -> {
            learner.prepareData(featurePanel.getFeatureSelections(), collectDatasets(true), transformRange.getPlot().getVisibleStreams());
            learner.pcaPlot3D();
        });

        this.trainBtn.addActionListener(e -> {
            learner.prepareData(featurePanel.getFeatureSelections(), collectDatasets(true), transformRange.getPlot().getVisibleStreams());
            msgBar.setMessage("Training...");
            learner.train();
        });

        this.predictCkBox.addActionListener(e -> {
            if (predictCkBox.isSelected()) {
                predicting = true;
                predictSignal();
            } else {
                predicting = false;
            }
        });
    }

    private void discardAllDatasets() {
        for (int i = 0; i < datasetBox.getComponentCount(); i++) {
            DatasetView view = (DatasetView) datasetBox.getComponent(i);
            view.discardDataset();
        }

        datasetBox.removeAll();
        unselectDataset();
    }

    private void loadAndBuildDataset(File dir) {
        Map<String, Collection<FragmentDataSource>> dataset = DataFileUtils.getInstance().loadFragmentDataSources(dir);
        int dataAmount = 0;
        int progress = 1;
        for (Collection<FragmentDataSource> category: dataset.values()) {
            dataAmount += category.size();
        }
        msgBar.setProgressMax(dataAmount);

        for (Map.Entry<String, Collection<FragmentDataSource>> pair : dataset.entrySet()) {
            DatasetView actionView = new DatasetView(pair.getKey(), datasetManager);

            for (FragmentDataSource dataSource : pair.getValue()) {
                msgBar.setProgress(progress);
                actionView.addNewData(dataSource);
                progress++;
            }
            datasetBox.add(actionView, 0);
        }

        layoutDatasetPanel();
        msgBar.setMessage("");
    }

    private void unselectDataset() {
        selectedDatasetView = null;
        featurePanel.setDataset(null);
        addDataBtn.setEnabled(false);
        renameBtn.setEnabled(false);
    }

    private void predictSignal() {
        String result = learner.predict(transformRange.makeFragmentDataSource("predict"));

        if (result == null) {
            msgBar.setMessage("ERROR: please retrain the model");
            closePrediction();
        } else {
            msgBar.setMessage("predict result: " + result);
        }
    }

    private void closePrediction() {
        predicting = false;
        predictCkBox.setSelected(false);
        predictCkBox.setEnabled(false);
    }

    private Collection<FragmentDataSource> collectDatasets(boolean selectedDataOnly) {
        Collection<FragmentDataSource> fullDataset = new LinkedList<>();
        for (int i = 0; i < datasetBox.getComponentCount(); i++) {
            DatasetView view = (DatasetView) datasetBox.getComponent(i);
            fullDataset.addAll(view.getAllData(selectedDataOnly));
        }
        return fullDataset;
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
                    unselectDataset();
                }
                layoutDatasetPanel();
            }

        }

        private void handleDatasetSelected(DatasetView view) {
            selectedDatasetView = view;
            featurePanel.setDataset(view);
            addDataBtn.setEnabled(true);
            renameBtn.setEnabled(true);
        }


    }

    private void layoutDatasetPanel() {
        scrollPane.validate();
        scrollPane.repaint();
    }


}
