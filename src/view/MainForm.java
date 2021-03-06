package view;

import model.datasource.AbstractDataSource;
import model.datasource.FilteredDataSource;
import model.datasource.FiniteLengthDataSource;
import model.filter.ButterworthFilter;
import model.DataFileUtils;
import model.filter.EEGFilter;
import model.filter.Filter;
import view.component.dataview.DatasetPanel;
import view.component.plot.InteractivePlotView;
import view.component.plot.PlaybackPlotControl;
import view.component.plugin.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Created by maeglin89273 on 7/20/15.
 */
public class MainForm extends JFrame {
    private static final String APPLICATION_NAME = "Signal Viewer";

    private JButton sliceBtn;

    private JLabel filterLbl;
    private JLabel tagsLbl;
    private JRadioButton alphaFilterRadioBtn;
    private JRadioButton betaFilterRadioBtn;
    private JRadioButton gammaFilterRadioBtn;
    private JPanel dtwPanel;
    private JPanel channelPanel;
    private JPanel filterPanel;
    private JPanel mainPanel;
    private JButton loadOpenBCIBtn;
    private JTextField tagField;
    private JLabel tagLbl;
    private JSpinner sliceStartSpinner;
    private JSpinner sliceEndSpinner;
    private PlaybackPlotControl plotControl;
    private JLabel savedHintLbl;
    private JCheckBox moveTemplateCheckBox;
    private JLabel dtwDistanceLbl;
    private JLabel dtwValueLbl;
    private JRadioButton thetaFilterRadioBtn;
    private JRadioButton a1to50HzFilterRadioBtn;
    private JCheckBox dtwCheckBox;
    private JButton templateMakeBtn;
    private JLabel templateLbl;
    private InteractivePlotView fftSpectrumPlot;
    private JPanel leftPanel;
    private JPanel controlPanel;
    private InteractivePlotView swtSpectrumPlot;
    private JButton loadCSVBtn;
    private JPanel tagsPanel;
    private JRadioButton noneFilterRadioBtn;
    private DatasetPanel datasetPanel;
    private JRadioButton a5to50HzFilterRadioBtn;
    private JPanel featurePanel;
    private JPanel predictionPanel;

    private ButtonGroup filterChoiceGroup;
    private SpinnerNumberModel startSpinModel;
    private SpinnerNumberModel endSpinModel;

    private FiniteLengthDataSource data;

    private DTWPlugin dtwPlugin;
    private Map<String, Filter> filterTable;

    public MainForm() {
        super("EEG Channel Slicer");

        $$$setupUI$$$();
        this.setContentPane(this.mainPanel);
        this.pack();
        this.setMinimumSize(this.getSize());

        this.setupPlugins();
        this.setupOthers();
        this.setupListeners();

        dtwPanel.setVisible(false);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private void setupListeners() {
        this.loadOpenBCIBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = DataFileUtils.getInstance().loadFileDialog(MainForm.this, "txt");
                if (file != null) {
                    setData(DataFileUtils.getInstance().loadOpenBCIRawFile(file));
                }
            }
        });

        this.loadCSVBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = DataFileUtils.getInstance().loadFileDialog(MainForm.this, "csv");
                if (file != null) {
                    setData(DataFileUtils.getInstance().loadGeneralCSVFile(file));
                }
            }
        });


        this.startSpinModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = startSpinModel.getNumber().intValue();
                if (value != dtwPlugin.getStartPosition()) {
                    dtwPlugin.setStartPosition(value);
                }
            }
        });

        this.endSpinModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = endSpinModel.getNumber().intValue();
                if (value != dtwPlugin.getEndPosition()) {
                    dtwPlugin.setEndPosition(value);

                }
            }
        });

        this.sliceBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filename = DataFileUtils.getInstance().saveSlice(dtwPlugin.makeFragmentDataSource(tagField.getText()));

                if (filename != null) {
                    savedHintLbl.setText(filename + " saved!");
                } else {
                    savedHintLbl.setText("<- not saved!");
                }
            }
        });

        ActionListener filterBtnListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (data instanceof FilteredDataSource) {
                    ((FilteredDataSource) data).replaceFilter(1, filterTable.get(filterChoiceGroup.getSelection().getActionCommand()));
                }
            }
        };

        Enumeration<AbstractButton> filterBtns = this.filterChoiceGroup.getElements();
        AbstractButton filterBtn;

        for (; filterBtns.hasMoreElements(); ) {
            filterBtn = filterBtns.nextElement();
            filterBtn.setActionCommand(filterBtn.getText());
            filterBtn.addActionListener(filterBtnListener);
        }

        dtwCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = dtwCheckBox.isSelected();
                dtwPlugin.setEnabled(enabled);
                moveTemplateCheckBox.setEnabled(enabled);
                sliceStartSpinner.setEnabled(enabled);
                sliceEndSpinner.setEnabled(enabled);
                templateMakeBtn.setEnabled(enabled);
                sliceBtn.setEnabled(enabled);
                tagField.setEnabled(enabled);
                dtwValueLbl.setForeground(Color.BLACK);
                dtwValueLbl.setText("None");
            }
        });

        templateMakeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dtwPlugin.makeNewTemplate();
            }
        });

        dtwPlugin.setRangeChangedListener(new RangePlugin.RangeChangedListener() {
            @Override
            public void onStartChanged(long lowerBound, long value, long upperBound) {
                this.setValues(startSpinModel, lowerBound, value, upperBound);
            }

            @Override
            public void onEndChanged(long lowerBound, long value, long upperBound) {
                this.setValues(endSpinModel, lowerBound, value, upperBound);
            }

            private void setValues(SpinnerNumberModel model, long lowerBound, long value, long upperBound) {
                model.setMinimum((int) lowerBound);
                model.setMaximum((int) upperBound);
                model.setValue((int) value);
            }
        });

        moveTemplateCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dtwPlugin.setTemplateControl(moveTemplateCheckBox.isSelected());
            }
        });

        dtwPlugin.setDTWDistanceListener(new DTWPlugin.DTWDistanceListener() {

            public static final float MAGENTA_DISTANCE = 0.35f * DTWPlugin.ACCEPTABLE_DTW_DISTANCE_UPPERBOUND;
            private static final float BLUE_DISTANCE = 0.55f * DTWPlugin.ACCEPTABLE_DTW_DISTANCE_UPPERBOUND;
            private static final float GREEN_DISTANCE = 0.05f * DTWPlugin.ACCEPTABLE_DTW_DISTANCE_UPPERBOUND;
            public static final float RED_DISTANCE = 0.1f * DTWPlugin.ACCEPTABLE_DTW_DISTANCE_UPPERBOUND;

            public void setLevelColor(double distance) {
                if (distance <= GREEN_DISTANCE) {
                    dtwValueLbl.setForeground(Color.GREEN);
                } else if (distance <= RED_DISTANCE) {
                    dtwValueLbl.setForeground(Color.RED);
                } else if (distance <= MAGENTA_DISTANCE) {
                    dtwValueLbl.setForeground(Color.MAGENTA);
                } else if (distance <= BLUE_DISTANCE) {
                    dtwValueLbl.setForeground(Color.BLUE);
                } else {
                    dtwValueLbl.setForeground(Color.BLACK);
                }
            }

            @Override
            public void onDistanceChanged(double distance) {
                if (distance >= 0) {
                    setLevelColor(distance);
                    dtwValueLbl.setText(String.format("%.2f", distance));
                } else {
                    dtwValueLbl.setForeground(Color.BLACK);
                    dtwValueLbl.setText(String.format("> %.1f", DTWPlugin.ACCEPTABLE_DTW_DISTANCE_UPPERBOUND));
                }

            }
        });

    }


    private void setData(FiniteLengthDataSource data) {
        this.data = data;
        MainForm.this.setTitle(APPLICATION_NAME + " - " + DataFileUtils.getInstance().getWorkingFile().getPath());
        plotControl.setDataSource(data);

        updateTagsCheckBoxes(data);

    }

    private ActionListener tagCkBoxListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBox channelCheckBox = (JCheckBox) e.getSource();
            plotControl.setStreamVisible(channelCheckBox.getText(), channelCheckBox.isSelected());
        }
    };

    private void updateTagsCheckBoxes(FiniteLengthDataSource data) {
        JCheckBox tagCkBox;
        for (Component oldComp : tagsPanel.getComponents()) {
            tagCkBox = (JCheckBox) oldComp;
            tagCkBox.removeActionListener(tagCkBoxListener);
        }

        tagsPanel.removeAll();
        List<String> tags = new ArrayList<>(data.getTags());
        Collections.sort(tags);
        for (String tag : tags) {
            tagCkBox = new JCheckBox(tag);
            tagCkBox.addActionListener(tagCkBoxListener);

            tagsPanel.add(tagCkBox);

        }
        tagCkBox = (JCheckBox) tagsPanel.getComponent(0);
        tagCkBox.setSelected(true);
        plotControl.setStreamVisible(tagCkBox.getText(), true);


        this.pack();
    }

    private void setupOthers() {
        this.setTitle(APPLICATION_NAME);
        this.startSpinModel = (SpinnerNumberModel) this.sliceStartSpinner.getModel();
        this.endSpinModel = (SpinnerNumberModel) this.sliceEndSpinner.getModel();

        filterChoiceGroup = new ButtonGroup();
        filterChoiceGroup.add(thetaFilterRadioBtn);
        filterChoiceGroup.add(alphaFilterRadioBtn);
        filterChoiceGroup.add(betaFilterRadioBtn);
        filterChoiceGroup.add(gammaFilterRadioBtn);
        filterChoiceGroup.add(a5to50HzFilterRadioBtn);
        filterChoiceGroup.add(a1to50HzFilterRadioBtn);
        filterChoiceGroup.add(noneFilterRadioBtn);
        this.filterTable = new HashMap<>(EEGFilter.EEG_BANDPASS_FILTER_TABLE);
        this.filterTable.put(noneFilterRadioBtn.getText(), Filter.EMPTY_FILTER);
        this.filterTable.put(a1to50HzFilterRadioBtn.getText(), ButterworthFilter.BANDPASS_1_50HZ);
        this.filterTable.put(a5to50HzFilterRadioBtn.getText(), ButterworthFilter.BANDPASS_5_50HZ);
        this.startSpinModel.setValue((int) this.dtwPlugin.getStartPosition());
        this.endSpinModel.setValue((int) this.dtwPlugin.getEndPosition());

    }

    private void setupPlugins() {
        this.dtwPlugin = new DTWPlugin();

        this.plotControl.addPluginToPlot(this.dtwPlugin);
        this.plotControl.addPluginToPlot(this.datasetPanel.getFrequencySpectrumPlugin());
        this.plotControl.addPluginToPlot(this.datasetPanel.getPointPositionPlugin());

    }

    private void createUIComponents() {
        plotControl = new PlaybackPlotControl(800, 60f);
        this.datasetPanel = new DatasetPanel();
        this.featurePanel = this.datasetPanel.getFeaturePanel();
        this.predictionPanel = this.datasetPanel.getPredictionPanel();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null));
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setContinuousLayout(true);
        splitPane1.setDividerLocation(680);
        splitPane1.setDividerSize(5);
        splitPane1.setResizeWeight(0.5);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.4;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(splitPane1, gbc);
        splitPane1.setLeftComponent(plotControl);
        splitPane1.setRightComponent(featurePanel);
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.6;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(controlPanel, gbc);
        leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        controlPanel.add(leftPanel, gbc);
        channelPanel = new JPanel();
        channelPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        leftPanel.add(channelPanel, gbc);
        tagsLbl = new JLabel();
        tagsLbl.setText("Streams");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(tagsLbl, gbc);
        loadCSVBtn = new JButton();
        loadCSVBtn.setText("Load CSV Data...");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        channelPanel.add(loadCSVBtn, gbc);
        loadOpenBCIBtn = new JButton();
        loadOpenBCIBtn.setText("Load OpebBCI Raw Data...");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(loadOpenBCIBtn, gbc);
        tagsPanel = new JPanel();
        tagsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 10;
        gbc.fill = GridBagConstraints.BOTH;
        channelPanel.add(tagsPanel, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        channelPanel.add(spacer1, gbc);
        filterPanel = new JPanel();
        filterPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        leftPanel.add(filterPanel, gbc);
        alphaFilterRadioBtn = new JRadioButton();
        alphaFilterRadioBtn.setSelected(false);
        alphaFilterRadioBtn.setText("Alpha");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(alphaFilterRadioBtn, gbc);
        betaFilterRadioBtn = new JRadioButton();
        betaFilterRadioBtn.setText("Beta");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(betaFilterRadioBtn, gbc);
        gammaFilterRadioBtn = new JRadioButton();
        gammaFilterRadioBtn.setText("Gamma");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(gammaFilterRadioBtn, gbc);
        filterLbl = new JLabel();
        filterLbl.setText("Filter");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(filterLbl, gbc);
        thetaFilterRadioBtn = new JRadioButton();
        thetaFilterRadioBtn.setSelected(false);
        thetaFilterRadioBtn.setText("Theta");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(thetaFilterRadioBtn, gbc);
        a1to50HzFilterRadioBtn = new JRadioButton();
        a1to50HzFilterRadioBtn.setSelected(true);
        a1to50HzFilterRadioBtn.setText("1~50Hz");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(a1to50HzFilterRadioBtn, gbc);
        noneFilterRadioBtn = new JRadioButton();
        noneFilterRadioBtn.setText("None");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(noneFilterRadioBtn, gbc);
        a5to50HzFilterRadioBtn = new JRadioButton();
        a5to50HzFilterRadioBtn.setText("5~50Hz");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(a5to50HzFilterRadioBtn, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        leftPanel.add(predictionPanel, gbc);
        dtwPanel = new JPanel();
        dtwPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        leftPanel.add(dtwPanel, gbc);
        tagLbl = new JLabel();
        tagLbl.setText("Tag:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        dtwPanel.add(tagLbl, gbc);
        savedHintLbl = new JLabel();
        savedHintLbl.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        dtwPanel.add(savedHintLbl, gbc);
        dtwCheckBox = new JCheckBox();
        dtwCheckBox.setText("DTW Analysis");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        dtwPanel.add(dtwCheckBox, gbc);
        sliceBtn = new JButton();
        sliceBtn.setEnabled(false);
        sliceBtn.setText("Slice");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dtwPanel.add(sliceBtn, gbc);
        sliceEndSpinner = new JSpinner();
        sliceEndSpinner.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dtwPanel.add(sliceEndSpinner, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("~");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        dtwPanel.add(label1, gbc);
        sliceStartSpinner = new JSpinner();
        sliceStartSpinner.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dtwPanel.add(sliceStartSpinner, gbc);
        tagField = new JTextField();
        tagField.setEnabled(false);
        tagField.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dtwPanel.add(tagField, gbc);
        dtwDistanceLbl = new JLabel();
        dtwDistanceLbl.setText("DTW distance:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        dtwPanel.add(dtwDistanceLbl, gbc);
        templateLbl = new JLabel();
        templateLbl.setText("Template:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        dtwPanel.add(templateLbl, gbc);
        dtwValueLbl = new JLabel();
        dtwValueLbl.setFont(new Font(dtwValueLbl.getFont().getName(), Font.BOLD, dtwValueLbl.getFont().getSize()));
        dtwValueLbl.setText("None");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dtwPanel.add(dtwValueLbl, gbc);
        moveTemplateCheckBox = new JCheckBox();
        moveTemplateCheckBox.setEnabled(false);
        moveTemplateCheckBox.setText("control");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        dtwPanel.add(moveTemplateCheckBox, gbc);
        templateMakeBtn = new JButton();
        templateMakeBtn.setEnabled(false);
        templateMakeBtn.setText("make");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dtwPanel.add(templateMakeBtn, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dtwPanel.add(spacer2, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        controlPanel.add(datasetPanel, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
