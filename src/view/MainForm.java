package view;

import model.datasource.FilteredDataSource;
import model.datasource.FiniteLengthDataSource;
import model.filter.ButterworthFilter;
import model.DataFileUtils;
import model.filter.EEGFilter;
import model.filter.Filter;
import view.component.PlaybackPlotControl;
import view.component.PlotView;
import view.component.PlottingUtils;
import view.component.plugin.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 7/20/15.
 */
public class MainForm extends JFrame {
    private static final String APPLICATION_NAME = "Signal Viewer";

    private JButton sliceBtn;
    private JCheckBox channel1CheckBox;
    private JCheckBox channel2CheckBox;
    private JCheckBox channel3CheckBox;
    private JCheckBox channel4CheckBox;
    private JCheckBox channel5CheckBox;
    private JCheckBox channel6CheckBox;
    private JCheckBox channel7CheckBox;
    private JCheckBox channel8CheckBox;
    private JLabel filterLbl;
    private JLabel channelLbl;
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
    private JLabel slicerLbl;
    private JButton templateMakeBtn;
    private JLabel templateLbl;
    private JCheckBox fftCheckBox;
    private PlotView fftRePlot;
    private JButton fftSnapshotBtn;
    private JPanel leftPanel;
    private JPanel fftPanel;
    private JPanel controlPanel;
    private JPanel centerPanel;
    private PlotView fftImPlot;
    private JLabel reLbl;
    private JCheckBox fftPowerCheckBox;
    private JLabel imLbl;
    private JButton fftClearSnapsotBtn;
    private JButton loadCSVBtn;

    private JCheckBox[] channelCheckBoxArray;
    private ButtonGroup filterChoiceGroup;
    private SpinnerNumberModel startSpinModel;
    private SpinnerNumberModel endSpinModel;

    private FiniteLengthDataSource data;


    private DTWPlugin dtwPlugin;
    private Map<String, Filter> filterTable;
    private FourierTransformPlugin fftPlugin;
    private SnapshotPlugin reSnapshotPlugin;
    private SnapshotPlugin imSnapshotPlugin;

    public MainForm() {
        super("EEG Channel Slicer");

        $$$setupUI$$$();
        this.setContentPane(this.mainPanel);
        this.pack();
        this.setMinimumSize(this.getSize());

        this.setupPlugins();
        this.setupOthers();
        this.setupListeners();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private void setupListeners() {
        this.loadOpenBCIBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = loadData("txt");
                if (file != null) {
                    setData(DataFileUtils.getInstance().loadOpenBCIRawFile(file));
                }
            }
        });

        this.loadCSVBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = loadData("csv");
                if (file != null) {
                    setData(DataFileUtils.getInstance().loadGeneralCSVFile(file));
                }
            }
        });


        this.startSpinModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = ((Number) startSpinModel.getValue()).intValue();
                if (value != dtwPlugin.getStartPosition()) {
                    dtwPlugin.setStartPosition(value);
                }
            }
        });

        this.endSpinModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = ((Number) endSpinModel.getValue()).intValue();
                if (value != dtwPlugin.getEndPosition()) {
                    dtwPlugin.setEndPosition(value);

                }
            }
        });

        this.sliceBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filename = DataFileUtils.getInstance().saveSlice(tagField.getText(), data,
                        ((Number) startSpinModel.getValue()).intValue(), ((Number) endSpinModel.getValue()).intValue());
                if (filename != null) {
                    savedHintLbl.setText(filename + " saved!");
                } else {
                    savedHintLbl.setText("<- not saved!");
                }
            }
        });

        ActionListener checkBoxListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox channelCheckBox = (JCheckBox) e.getSource();

                setStreamVisible(channelCheckBox.getText(), channelCheckBox.isSelected());
                fftPlugin.updateTransformation();
                dtwPlugin.updateDTW();
            }
        };

        for (int i = 0; i < this.channelCheckBoxArray.length; i++) {
            this.channelCheckBoxArray[i].addActionListener(checkBoxListener);
        }

        ActionListener filterBtnListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (data instanceof FilteredDataSource) {
                    ((FilteredDataSource) data).replaceFilter(1, filterTable.get(filterChoiceGroup.getSelection().getActionCommand()));
                    dtwPlugin.updateDTW();
                    fftPlugin.updateTransformation();
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

        fftCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean fftOn = fftCheckBox.isSelected();
                fftRePlot.setEnabled(fftOn);
                fftImPlot.setEnabled(fftOn);
                fftPowerCheckBox.setEnabled(fftOn);
                fftSnapshotBtn.setEnabled(fftOn);
                fftClearSnapsotBtn.setEnabled(fftOn);
                fftPlugin.setEnabled(fftOn);

                if (!fftOn) {
                    reSnapshotPlugin.clear();
                    imSnapshotPlugin.clear();
                }
            }
        });

        fftSnapshotBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reSnapshotPlugin.capture();
                imSnapshotPlugin.capture();
            }
        });

        fftClearSnapsotBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reSnapshotPlugin.clear();
                imSnapshotPlugin.clear();
            }
        });

        fftPowerCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fftPowerCheckBox.isSelected()) {
                    fftRePlot.setDataSource(fftPlugin.getDataSourceOfPower());
                    fftRePlot.setBaseline(PlottingUtils.Baseline.BOTTOM);
                    reLbl.setText("Power");
                    fftImPlot.setDataSource(fftPlugin.getDataSourceOfPhase());
                    fftImPlot.setPeakValue((float) Math.PI);
                    imLbl.setText("Phase");
                } else {
                    fftRePlot.setDataSource(fftPlugin.getDataSourceOfRealPart());
                    fftRePlot.setBaseline(PlottingUtils.Baseline.MIDDLE);
                    reLbl.setText("Real");
                    fftImPlot.setDataSource(fftPlugin.getDataSourceOfImageryPart());
                    fftImPlot.setPeakValue(4);
                    imLbl.setText("Imagery");
                }

            }
        });
    }

    private void setData(FiniteLengthDataSource data) {
        this.data = data;
        MainForm.this.setTitle(APPLICATION_NAME + " - " + DataFileUtils.getInstance().getWorkingDir().getPath());
        plotControl.setDataSource(data);

        for (int i = 0; i < channelCheckBoxArray.length; i++) {
            setStreamVisible(channelCheckBoxArray[i].getText(), channelCheckBoxArray[i].isSelected());
        }
    }

    private File loadData(String fileExtension) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("data Files", fileExtension);
        fileChooser.setFileFilter(filter);
        fileChooser.setMultiSelectionEnabled(false);
        if (fileChooser.showDialog(MainForm.this, "Load") == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    private void setStreamVisible(String tag, boolean visible) {
        plotControl.setStreamVisible(tag, visible);
        fftRePlot.setStreamVisible(tag, visible);
        fftImPlot.setStreamVisible(tag, visible);
    }

    private void setupOthers() {
        this.setTitle(APPLICATION_NAME);
        this.startSpinModel = (SpinnerNumberModel) this.sliceStartSpinner.getModel();
        this.endSpinModel = (SpinnerNumberModel) this.sliceEndSpinner.getModel();
        this.channelCheckBoxArray = new JCheckBox[]
                {this.channel1CheckBox, this.channel2CheckBox, this.channel3CheckBox, this.channel4CheckBox,
                        this.channel5CheckBox, this.channel6CheckBox, this.channel7CheckBox, this.channel8CheckBox};


        filterChoiceGroup = new ButtonGroup();
        filterChoiceGroup.add(thetaFilterRadioBtn);
        filterChoiceGroup.add(alphaFilterRadioBtn);
        filterChoiceGroup.add(betaFilterRadioBtn);
        filterChoiceGroup.add(gammaFilterRadioBtn);
        filterChoiceGroup.add(a1to50HzFilterRadioBtn);
        this.filterTable = new HashMap<>(EEGFilter.EEG_BANDPASS_FILTER_TABLE);
        this.filterTable.put(a1to50HzFilterRadioBtn.getText(), ButterworthFilter.BANDPASS_1_50HZ);

        this.startSpinModel.setValue((int) this.dtwPlugin.getStartPosition());
        this.endSpinModel.setValue((int) this.dtwPlugin.getEndPosition());

        this.fftRePlot.setLineWidth(1f);
        this.fftImPlot.setLineWidth(1f);

        this.fftRePlot.setEnabled(false);
        this.fftImPlot.setEnabled(false);
    }

    private void setupPlugins() {
        this.dtwPlugin = new DTWPlugin();
        this.fftPlugin = new FourierTransformPlugin(250, 256);
        this.reSnapshotPlugin = new SnapshotPlugin();
        this.imSnapshotPlugin = new SnapshotPlugin();

        this.plotControl.addPluginToPlot(this.dtwPlugin);
        this.plotControl.addPluginToPlot(this.fftPlugin);

        this.fftRePlot.addPlugin(this.reSnapshotPlugin);
        this.fftImPlot.addPlugin(this.imSnapshotPlugin);

        this.fftRePlot.setDataSource(this.fftPlugin.getDataSourceOfRealPart());
        this.fftImPlot.setDataSource(this.fftPlugin.getDataSourceOfImageryPart());
    }

    private void createUIComponents() {
        plotControl = new PlaybackPlotControl(600, 60);
        fftRePlot = new PlotView(60, 5f, 300, 125);
        fftImPlot = new PlotView(60, 5f, 300, 125);
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
        splitPane1.setDividerLocation(750);
        splitPane1.setDividerSize(5);
        splitPane1.setResizeWeight(0.5);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 2.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(splitPane1, gbc);
        splitPane1.setLeftComponent(plotControl);
        fftPanel = new JPanel();
        fftPanel.setLayout(new GridBagLayout());
        splitPane1.setRightComponent(fftPanel);
        fftSnapshotBtn = new JButton();
        fftSnapshotBtn.setEnabled(false);
        fftSnapshotBtn.setText("Snapshot");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        fftPanel.add(fftSnapshotBtn, gbc);
        fftCheckBox = new JCheckBox();
        fftCheckBox.setText("FFT Analysis");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        fftPanel.add(fftCheckBox, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        fftPanel.add(fftRePlot, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        fftPanel.add(fftImPlot, gbc);
        reLbl = new JLabel();
        reLbl.setText("Real");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        fftPanel.add(reLbl, gbc);
        imLbl = new JLabel();
        imLbl.setText("Imagery");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        fftPanel.add(imLbl, gbc);
        fftClearSnapsotBtn = new JButton();
        fftClearSnapsotBtn.setEnabled(false);
        fftClearSnapsotBtn.setText("Clear");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        fftPanel.add(fftClearSnapsotBtn, gbc);
        fftPowerCheckBox = new JCheckBox();
        fftPowerCheckBox.setEnabled(false);
        fftPowerCheckBox.setSelected(false);
        fftPowerCheckBox.setText("Power/Phase");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        fftPanel.add(fftPowerCheckBox, gbc);
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(controlPanel, gbc);
        leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        controlPanel.add(leftPanel, gbc);
        channelPanel = new JPanel();
        channelPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        leftPanel.add(channelPanel, gbc);
        channel1CheckBox = new JCheckBox();
        channel1CheckBox.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channel1CheckBox, gbc);
        channel2CheckBox = new JCheckBox();
        channel2CheckBox.setText("2");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channel2CheckBox, gbc);
        channel3CheckBox = new JCheckBox();
        channel3CheckBox.setSelected(true);
        channel3CheckBox.setText("3");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channel3CheckBox, gbc);
        channel4CheckBox = new JCheckBox();
        channel4CheckBox.setText("4");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channel4CheckBox, gbc);
        channel5CheckBox = new JCheckBox();
        channel5CheckBox.setText("5");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channel5CheckBox, gbc);
        channel6CheckBox = new JCheckBox();
        channel6CheckBox.setText("6");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channel6CheckBox, gbc);
        channel7CheckBox = new JCheckBox();
        channel7CheckBox.setText("7");
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channel7CheckBox, gbc);
        channel8CheckBox = new JCheckBox();
        channel8CheckBox.setLabel("8");
        channel8CheckBox.setText("8");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channel8CheckBox, gbc);
        channelLbl = new JLabel();
        channelLbl.setText("Channels");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channelLbl, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        channelPanel.add(spacer1, gbc);
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
        dtwPanel = new JPanel();
        dtwPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
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
        slicerLbl = new JLabel();
        slicerLbl.setText("Slice range:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        dtwPanel.add(slicerLbl, gbc);
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
        filterPanel = new JPanel();
        filterPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        leftPanel.add(filterPanel, gbc);
        alphaFilterRadioBtn = new JRadioButton();
        alphaFilterRadioBtn.setSelected(false);
        alphaFilterRadioBtn.setText("Alpha");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(alphaFilterRadioBtn, gbc);
        betaFilterRadioBtn = new JRadioButton();
        betaFilterRadioBtn.setText("Beta");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(betaFilterRadioBtn, gbc);
        gammaFilterRadioBtn = new JRadioButton();
        gammaFilterRadioBtn.setText("Gamma");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(gammaFilterRadioBtn, gbc);
        filterLbl = new JLabel();
        filterLbl.setText("Filter");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(filterLbl, gbc);
        thetaFilterRadioBtn = new JRadioButton();
        thetaFilterRadioBtn.setSelected(false);
        thetaFilterRadioBtn.setText("Theta");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
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
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filterPanel.add(spacer3, gbc);
        centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.8;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        controlPanel.add(centerPanel, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
