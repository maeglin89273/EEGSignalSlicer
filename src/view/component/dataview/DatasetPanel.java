package view.component.dataview;

import model.DataFileUtils;
import model.LearnerProxy;
import model.datasource.FragmentDataSource;
import view.component.BusyDialog;
import view.component.plot.PlotView;
import view.component.plugin.*;
import view.component.trainingview.TrainingDialog;
import view.component.trainingview.TrainingReportDialog;
import view.component.trainingview.phasepanel.FeatureExtractionPhase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

/**
 * Created by maeglin89273 on 8/20/15.
 */
public class DatasetPanel extends JPanel {


    private final CategoryGroupManager datasetManager;

    private TrainingDialog trainingDialog;

    private Box datasetBox;
    private JButton addDataBtn;
    private JButton trainBtn;
    private JButton newCategoryBtn;
    private JCheckBox trainingCkBox;
    private JTextField actionField;

    private RangePlugin transformRange;
    private CategoryPanel selectedCategoryPanel;
    private JScrollPane scrollPane;
    private JButton saveDatasetBtn;
    private JButton loadDatasetBtn;
    private JSpinner rangeStartSpinner;
    private SpinnerNumberModel rangeSpinnerModel;
    private MessageBar msgBar;

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

    private JButton moveLeftBtn;
    private JButton moveRightBtn;


    public DatasetPanel() {
        this.initComponents();
        this.trainingDialog = new TrainingDialog(()-> collectDatasets(true));
        this.initPlots();
        this.datasetManager = new CategoryGroupManager();
        this.setupListeners();
    }

    private void initComponents() {
        this.setLayout(new GridBagLayout());
        actionField = new JTextField();
        actionField.setColumns(8);
        actionField.setEnabled(false);
        actionField.setHorizontalAlignment(10);
        actionField.setText("");
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
        gbc.gridheight = 4;
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
        msgBar = new MessageBar();
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
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
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(loadDatasetBtn, gbc);
        coordinatesCkBox = new JCheckBox();
        coordinatesCkBox.setEnabled(false);
        coordinatesCkBox.setText("coordinates");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(coordinatesCkBox, gbc);
        addDatasetBtn = new JButton();
        addDatasetBtn.setEnabled(false);
        addDatasetBtn.setText("add...");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(addDatasetBtn, gbc);
        saveDatasetBtn = new JButton();
        saveDatasetBtn.setEnabled(false);
        saveDatasetBtn.setText("save");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(saveDatasetBtn, gbc);
        addDataBtn = new JButton();
        addDataBtn.setEnabled(false);
        addDataBtn.setText("add data");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(addDataBtn, gbc);
        rangeStartSpinner = new JSpinner();
        rangeStartSpinner.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(rangeStartSpinner, gbc);
        predictCkBox = new JCheckBox();
        predictCkBox.setEnabled(false);
        predictCkBox.setText("predict");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(predictCkBox, gbc);
        moveLeftBtn = new JButton();
        moveLeftBtn.setEnabled(false);
        moveLeftBtn.setText("<");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(moveLeftBtn, gbc);
        moveRightBtn = new JButton();
        moveRightBtn.setEnabled(false);
        moveRightBtn.setText(">");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(moveRightBtn, gbc);
        trainBtn = new JButton();
        trainBtn.setEnabled(false);
        trainBtn.setText("Train");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(trainBtn, gbc);


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
            addDataBtn.setEnabled(trainingOn && selectedCategoryPanel != null);
            loadDatasetBtn.setEnabled(trainingOn);
            saveDatasetBtn.setEnabled(trainingOn);
            actionField.setEnabled(trainingOn);
            transformRange.setEnabled(trainingOn);
            rangeStartSpinner.setEnabled(trainingOn);
            renameBtn.setEnabled(trainingOn && selectedCategoryPanel != null);
            predictCkBox.setEnabled(trainingOn && predicting);
            coordinatesCkBox.setEnabled(trainingOn);
            addDatasetBtn.setEnabled(trainingOn);
            moveLeftBtn.setEnabled(trainingOn && selectedCategoryPanel != null);
            moveRightBtn.setEnabled(trainingOn && selectedCategoryPanel != null);
            trainBtn.setEnabled(trainingOn);
            if (trainingOn) {
                this.updateMainUIBySettings();
            }
        });

        this.coordinatesCkBox.addActionListener(e -> {
            boolean enabled = coordinatesCkBox.isSelected();
            fftPPPlugin.setEnabled(enabled);
            mainPlotPPPlugin.setEnabled(enabled);
        });



        this.newCategoryBtn.addActionListener(e -> {
            String newAction = actionField.getText().trim();
            if (!newAction.isEmpty()) {
                datasetManager.addCategory(new CategoryPanel(newAction));
                layoutDatasetPanel();
            }
        });

        this.renameBtn.addActionListener(e -> selectedCategoryPanel.setCategory(actionField.getText().trim()));

        this.rangeSpinnerModel.addChangeListener(e -> {
            int newPos = rangeSpinnerModel.getNumber().intValue();
            if (newPos != transformRange.getStartPosition()) {
                PlotView plot = transformRange.getPlot();

                int offset = (int) (transformRange.getStartPosition() - plot.getPlotLowerBound());
                plot.setXTo(newPos - offset);
            }
        });

        this.addDataBtn.addActionListener(e -> {
            selectedCategoryPanel.addNewData(transformRange.makeFragmentDataSource(selectedCategoryPanel.getCategory()));
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

        this.loadDatasetBtn.addActionListener(e -> {
            File dir = DataFileUtils.getInstance().loadFileDialog(DatasetPanel.this, "dir");
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
        });

        this.addDatasetBtn.addActionListener(e -> {
            File dir = DataFileUtils.getInstance().loadFileDialog(DatasetPanel.this, "dir");
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
        });

        this.moveLeftBtn.addActionListener(e -> {
            int index = datasetBox.getComponentZOrder(selectedCategoryPanel);
            datasetBox.remove(index);
            datasetBox.add(selectedCategoryPanel, --index);
            updateMoveBtns(index);
            layoutDatasetPanel();
        });

        this.moveRightBtn.addActionListener(e -> {
            int index = datasetBox.getComponentZOrder(selectedCategoryPanel);
            datasetBox.remove(index);
            datasetBox.add(selectedCategoryPanel, ++index);
            updateMoveBtns(index);
            layoutDatasetPanel();
        });



        this.trainBtn.addActionListener(e -> {
            prepareTraining();
            trainingDialog.setVisible(true);
            updateMainUIBySettings();
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

    private void prepareTraining() {
        FeatureExtractionPhase phase = trainingDialog.getFeatureExtractionPhase();
        phase.setStreams(transformRange.getPlot().getDataSource().getTags());
        if (!this.hasAnyData()) {
            phase.setWindowSizeModifiable(true);
            phase.setWindowSizeMax(transformRange.getPlot().getWindowSize());
        } else {
            phase.setWindowSizeModifiable(false);
        }
    }


    private void updateMainUIBySettings() {

        Map<String, Object> oldSettings = this.featureSettings;
        if ( this.isFeatureChange((Map<String, Object>) this.trainingDialog.getProfile().structuredGet("feature_extraction"))) {
            final BusyDialog blocker = new BusyDialog("applying transformation...");
            Thread setter = new Thread() {
                @Override
                public void run() {
                    FeatureExtractionPhase phase = trainingDialog.getFeatureExtractionPhase();
                    int windowSize = phase.getWindowSize();

                    boolean fftEnabled = featurePanel.getFFTPlot().isEnabled();
                    boolean wtEnabled = featurePanel.getWTPlot().isEnabled();

                    featurePanel.setFFTEnabled(false);
                    featurePanel.setWTEnabled(false);

                    if (transformRange.getRange() != windowSize) {
                        transformRange.setFixedRange(false);
                        transformRange.setRange(windowSize);
                        transformRange.setFixedRange(true);
                    }

                    boolean refilter = featurePanel.setTransformationSettings(phase, oldSettings, featureSettings);
                    fftPPPlugin.setXUnit(phase.getSampleRate() / (double) phase.getWindowSize());

                    if (refilter) {
                        refilterCategorySource(blocker);
                    }

                    featurePanel.setFFTEnabled(fftEnabled);
                    featurePanel.setWTEnabled(wtEnabled);

                    blocker.setVisible(false);
                    blocker.dispose();

                }
            };

            setter.setDaemon(true);
            setter.start();
            blocker.setVisible(true);
        }
    }

    private Map<String, Object> featureSettings = null;
    private boolean isFeatureChange(Map<String, Object> featureSettings) {
        featureSettings.remove("streams");
        featureSettings.remove("after_transformation");

        if (!Objects.equals(this.featureSettings, featureSettings)) {
            this.featureSettings = featureSettings;
            return true;
        }
        return false;
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

    private void refilterCategorySource(BusyDialog blocker) {
        blocker.setMaxProgress(datasetBox.getComponentCount());;
        for (int i = 0; i < datasetBox.getComponentCount(); i++) {
            CategoryPanel category = (CategoryPanel) datasetBox.getComponent(i);
            category.refilterTransformationSoruce();
            blocker.setProgress(i + 1);
        }
    }

    private void discardAllDatasets() {
        for (int i = 0; i < datasetBox.getComponentCount(); i++) {
            CategoryPanel category = (CategoryPanel) datasetBox.getComponent(i);
            category.discardDataset();
        }

        datasetBox.removeAll();
        unselectCategory();
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
            CategoryPanel categoryPanel = new CategoryPanel(pair.getKey());

            for (FragmentDataSource dataSource : pair.getValue()) {
                msgBar.setProgress(progress);
                categoryPanel.addNewData(dataSource);
                progress++;
            }
            datasetManager.addCategory(categoryPanel);
        }

        layoutDatasetPanel();
        msgBar.setMessage("");
    }

    private boolean hasAnyData() {
        for (int i = 0; i < datasetBox.getComponentCount(); i++) {
            CategoryPanel category = (CategoryPanel) datasetBox.getComponent(i);
            if (category.getAllData(false).size() > 0) {
                return true;
            }
        }
        return false;
    }

    private Collection<FragmentDataSource> collectDatasets(boolean selectedDataOnly) {
        Collection<FragmentDataSource> fullDataset = new LinkedList<>();
        for (int i = 0; i < datasetBox.getComponentCount(); i++) {
            CategoryPanel category = (CategoryPanel) datasetBox.getComponent(i);
            fullDataset.addAll(category.getAllData(selectedDataOnly));
        }
        return fullDataset;
    }

    public PlotPlugin getFrequencySpectrumPlugin() {
        return this.transformRange;
    }

    public PlotPlugin getPointPositionPlugin() {
        return this.mainPlotPPPlugin;
    }

    class CategoryGroupManager implements ActionListener {
        private final String CATEGORY_KEY = "dataset_category";
        private final String RDO_BTN_KEY = "tag_rdo_box";

        private ButtonGroup categoryGroup;

        private CategoryGroupManager() {
            this.categoryGroup = new ButtonGroup();
        }

        public void addCategory(CategoryPanel category) {
            JRadioButton tagRdoBtn = category.getCategoryRdoBtn();
            JButton removeBtn = category.getRemoveBtn();
            this.categoryGroup.add(tagRdoBtn);
            tagRdoBtn.putClientProperty(CATEGORY_KEY, category);
            removeBtn.putClientProperty(CATEGORY_KEY, category);
            removeBtn.putClientProperty(RDO_BTN_KEY, tagRdoBtn);
            removeBtn.addActionListener(this);
            tagRdoBtn.addActionListener(this);
            datasetBox.add(category, 0);
            tagRdoBtn.setSelected(true);
            selectCategory(category);

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JComponent source = (JComponent) e.getSource();
            CategoryPanel category = (CategoryPanel) source.getClientProperty(CATEGORY_KEY);
            if (source instanceof JRadioButton) {
                selectedCategoryPanel = category;
                selectCategory(category);
            } else {
                this.categoryGroup.remove((AbstractButton) source.getClientProperty(RDO_BTN_KEY));
                category.discardDataset();
                datasetBox.remove(category);
                if (category == selectedCategoryPanel) {
                    unselectCategory();
                }
                layoutDatasetPanel();
            }

        }
    }

    private void updateMoveBtns(int datasetIndex) {
        if (datasetIndex == 0) {
            moveLeftBtn.setEnabled(false);
            if (!moveRightBtn.isEnabled()) {
                moveRightBtn.setEnabled(true);
            }
        } else if (datasetIndex == datasetBox.getComponentCount() - 1) {
            moveRightBtn.setEnabled(false);
            if (!moveLeftBtn.isEnabled()) {
                moveLeftBtn.setEnabled(true);
            }
        } else {
            if (!moveLeftBtn.isEnabled()) {
                moveLeftBtn.setEnabled(true);
            }

            if (!moveRightBtn.isEnabled()) {
                moveRightBtn.setEnabled(true);
            }
        }
    }

    private void unselectCategory() {
        selectedCategoryPanel = null;
        featurePanel.setDataset(null);
        addDataBtn.setEnabled(false);
        renameBtn.setEnabled(false);
        moveLeftBtn.setEnabled(false);
        moveRightBtn.setEnabled(false);
    }

    private void selectCategory(CategoryPanel category) {
        selectedCategoryPanel = category;
        featurePanel.setDataset(category);
        addDataBtn.setEnabled(true);
        renameBtn.setEnabled(true);
        updateMoveBtns(datasetBox.getComponentZOrder(category));
    }

    private void layoutDatasetPanel() {
        scrollPane.validate();
        scrollPane.repaint();
    }


}
