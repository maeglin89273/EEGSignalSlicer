package view.component.dataview;

import model.DataFileUtils;
import model.datasource.FilteredFiniteDataSource;
import model.datasource.FragmentDataSource;
import model.datasource.StreamingDataSource;
import model.filter.DomainTransformFilter;
import view.component.plot.InteractivePlotView;
import view.component.plot.PlotView;
import view.component.plot.PlottingUtils;
import view.component.plugin.NavigationPlugin;
import view.component.plugin.PlotPlugin;
import view.component.plugin.RangePlugin;
import view.component.plugin.SimilarStreamsPlottingPlugin;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 8/20/15.
 */
public class TrainingPanel extends JPanel {

    private final DatasetViewGroupManager datasetManager;

    private Box datasetBox;
    private JButton addDataBtn;
    private JButton trainBtn;
    private JButton newActionBtn;
    private JCheckBox trainingCkBox;
    private JTextField actionField;
    private SourceHidablePlotView fftSpectrumPlot;
    private SourceHidablePlotView dwtSpectrumPlot;

    private RangePlugin transformRange;
    private DatasetView selectedDatasetView;
    private JScrollPane scrollPane;
    private SimilarStreamsPlottingPlugin fftBgPlugin;
    private SimilarStreamsPlottingPlugin dwtBgPlugin;
    private JCheckBox showSourceCkBox;
    private JButton saveDatasetBtn;
    private JButton loadDatasetBtn;
    private JSpinner rangeStartSpinner;
    private SpinnerNumberModel rangeSpinnerModel;

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
        trainBtn = new JButton();
        trainBtn.setEnabled(false);
        trainBtn.setText("train");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        this.add(trainBtn, gbc);
        showSourceCkBox = new JCheckBox();
        showSourceCkBox.setEnabled(false);
        showSourceCkBox.setSelected(true);
        showSourceCkBox.setText("show source");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(showSourceCkBox, gbc);
        scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(21);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 9;
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
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(rangeStartSpinner, gbc);
        addDataBtn = new JButton();
        addDataBtn.setEnabled(false);
        addDataBtn.setText("add data");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        this.add(addDataBtn, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(spacer1, gbc);
        loadDatasetBtn = new JButton();
        loadDatasetBtn.setEnabled(false);
        loadDatasetBtn.setText("load...");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(loadDatasetBtn, gbc);
        saveDatasetBtn = new JButton();
        saveDatasetBtn.setEnabled(false);
        saveDatasetBtn.setText("save");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(saveDatasetBtn, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(spacer2, gbc);


        datasetBox = SingleDirectionBox.createHorizontalBox();

        scrollPane.setViewportView(datasetBox);
        rangeSpinnerModel = (SpinnerNumberModel)rangeStartSpinner.getModel();
        rangeSpinnerModel.setMinimum(0);
    }

    private void initPlots() {
        this.transformRange = new RangePlugin(Color.CYAN, 256);
        this.transformRange.setRangeChangedListener(new RangePlugin.RangeChangedListener() {
            @Override
            public void onStartChanged(long lowerBound, long value, long upperBound) {
                if ((Integer)rangeSpinnerModel.getValue() != value) {
                    rangeSpinnerModel.setValue((int)(value));
                }
            }

            @Override
            public void onEndChanged(long lowerBound, long value, long upperBound) {

            }
        });

        fftSpectrumPlot = new SourceHidablePlotView(60, 1250f, 300, 125);
        fftSpectrumPlot.setBaseline(PlottingUtils.Baseline.BOTTOM);
        fftSpectrumPlot.setViewAllStreams(true);
        fftSpectrumPlot.setLineWidth(1.3f);

        FilteredFiniteDataSource fftSource = new FilteredFiniteDataSource(this.transformRange.getRangedDataSource());
        fftSource.addFilter(DomainTransformFilter.FFT);
        fftSpectrumPlot.setDataSource(fftSource);

        NavigationPlugin navigationPlugin = new NavigationPlugin();
        fftSpectrumPlot.addPlugin(navigationPlugin);
        navigationPlugin.setZoomingMode(NavigationPlugin.ZoomingMode.ZOOM_Y);

        this.fftBgPlugin = new SimilarStreamsPlottingPlugin();
        fftSpectrumPlot.addPlugin(this.fftBgPlugin);
        this.fftBgPlugin.setEnabled(true);

        this.fftSpectrumPlot.setEnabled(false);

        dwtSpectrumPlot = new SourceHidablePlotView(256, 50f, 256, 125);
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
                showSourceCkBox.setEnabled(trainingOn);
                transformRange.setEnabled(trainingOn);
                rangeStartSpinner.setEnabled(trainingOn);
            }
        });

        this.showSourceCkBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fftSpectrumPlot.setShowSource(showSourceCkBox.isSelected());
                dwtSpectrumPlot.setShowSource(showSourceCkBox.isSelected());
            }
        });

        this.newActionBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newAction = actionField.getText().trim();
                if (!newAction.isEmpty()) {
                    datasetBox.add(new DatasetView(newAction, datasetManager));
                    layoutDatasetPanel();
                }
            }
        });

        this.rangeSpinnerModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int newPos = rangeSpinnerModel.getNumber().intValue();
                PlotView plot = transformRange.getPlot();

                int offset = (int) (transformRange.getStartPosition() - plot.getPlotLowerBound());
                plot.setXTo(newPos - offset);

            }
        });

        this.addDataBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tag = selectedDatasetView.getTag();
                long startingPos = transformRange.getStartPosition();
                int length = transformRange.getRange();
                StreamingDataSource dataSource = transformRange.getPlot().getDataSource();

                selectedDatasetView.addNewData(new FragmentDataSource(tag, startingPos, length, dataSource));
                layoutDatasetPanel();
            }
        });

        this.saveDatasetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Map<String, Collection<FragmentDataSource>> fullDataset = CollectDatasets();
                DataFileUtils.getInstance().saveFragmentDataSources("traning data-" + new Date().toString(), fullDataset);
            }

            private Map<String, Collection<FragmentDataSource>> CollectDatasets() {
                Map<String, Collection<FragmentDataSource>> fullDataset = new HashMap<>();
                System.out.println(datasetBox.getComponentCount());
                for (int i = 0; i < datasetBox.getComponentCount(); i++) {
                    DatasetView view = (DatasetView) datasetBox.getComponent(i);
                    fullDataset.put(view.getTag(), view.getAllData());
                }
                return fullDataset;
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
                for (Map.Entry<String, Collection<FragmentDataSource>> pair: dataset.entrySet()) {
                    DatasetView actionView = new DatasetView(pair.getKey(), datasetManager);

                    for (FragmentDataSource dataSource: pair.getValue()) {
                        actionView.addNewData(dataSource);
                    }
                    datasetBox.add(actionView);
                }

                layoutDatasetPanel();
            }
        });

        this.trainBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //todo: call training oracle
            }
        });
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
                    selectedDatasetView = null;
                    fftBgPlugin.setDataSource(null);
                    dwtBgPlugin.setDataSource(null);
                    addDataBtn.setEnabled(false);
                }
                layoutDatasetPanel();
            }

        }
    }

    private void layoutDatasetPanel() {
        scrollPane.validate();
        scrollPane.repaint();
    }

    private class SourceHidablePlotView extends InteractivePlotView {
        private boolean showSource = true;
        public SourceHidablePlotView(int windowSize, float peakValue, int plotWidth, int plotHeight) {
            super(windowSize, peakValue, plotWidth, plotHeight);
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
