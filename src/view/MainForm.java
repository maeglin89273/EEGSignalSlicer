package view;

import model.filter.ButterworthFilter;
import model.datasource.EEGChannels;
import model.RawDataFileUtils;
import model.filter.EEGFilter;
import model.filter.Filter;
import view.component.PlotControl;
import view.component.plugin.DTWPlugin;
import view.component.plugin.SlicerPlugin;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 7/20/15.
 */
public class MainForm extends JFrame {
    private JLabel sliceLbl;
    private JButton sliceButton;
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
    private JPanel controlPanel;
    private JPanel slicerPanel;
    private JPanel channelPanel;
    private JPanel filterPanel;
    private JPanel mainPanel;
    private JButton loadBtn;
    private JTextField tagField;
    private JLabel tagLbl;
    private JSpinner sliceStartSpinner;
    private JSpinner sliceEndSpinner;
    private PlotControl plotControl;
    private JLabel savedHintLbl;
    private JButton dtwBtn;
    private JButton clearDtwBtn;
    private JCheckBox moveShadowCheckBox;
    private JLabel dtwDistanceLbl;
    private JLabel dtwValueLbl;
    private JPanel dtwPanel;
    private JRadioButton thetaFilterRadioBtn;
    private JRadioButton a1to50HzFilterRadioBtn;

    private JCheckBox[] channelCheckBoxArray;
    private ButtonGroup filterChoiceGroup;
    private SpinnerNumberModel startSpinModel;
    private SpinnerNumberModel endSpinModel;

    private EEGChannels data;

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
        plotControl.setStreamVisible("3", true);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

    }

    private void setupListeners() {
        this.loadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
                fileChooser.setFileFilter(filter);
                fileChooser.setMultiSelectionEnabled(false);
                if (fileChooser.showDialog(MainForm.this, "Load") == JFileChooser.APPROVE_OPTION) {
                    data = RawDataFileUtils.getInstance().load(fileChooser.getSelectedFile());
                    plotControl.setDataSource(data);
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

        this.sliceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filename = RawDataFileUtils.getInstance().saveSlice(tagField.getText(), data,
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
                plotControl.setStreamVisible(channelCheckBox.getText(), channelCheckBox.isSelected());
                dtwPlugin.updateDTW();
            }
        };

        for (int i = 0; i < this.channelCheckBoxArray.length; i++) {
            this.channelCheckBoxArray[i].addActionListener(checkBoxListener);
        }

        ActionListener filterBtnListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                data.setBandpassFilter(filterTable.get(filterChoiceGroup.getSelection().getActionCommand()));
                dtwPlugin.updateDTW();
            }


        };

        Enumeration<AbstractButton> filterBtns = this.filterChoiceGroup.getElements();
        AbstractButton filterBtn;

        for (; filterBtns.hasMoreElements(); ) {
            filterBtn = filterBtns.nextElement();
            filterBtn.setActionCommand(filterBtn.getText());
            filterBtn.addActionListener(filterBtnListener);
        }

        dtwBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dtwPlugin.startDTW();

                moveShadowCheckBox.setEnabled(true);
                dtwPlugin.setBackStreamControl(moveShadowCheckBox.isSelected());
            }
        });

        clearDtwBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dtwPlugin.closeDTW();
                moveShadowCheckBox.setEnabled(false);
            }
        });

        dtwPlugin.setRangeChangedListener(new SlicerPlugin.RangeChangedListener() {
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

        moveShadowCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dtwPlugin.setBackStreamControl(moveShadowCheckBox.isSelected());
            }
        });

        dtwPlugin.setDTWDistanceListener(new DTWPlugin.DTWDistanceListener() {


            private double dtwPlugin;

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

    private void setupOthers() {
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
        this.filterTable = new HashMap<String, Filter>(EEGFilter.EEG_BANDPASS_FILTER_TABLE);
        this.filterTable.put(a1to50HzFilterRadioBtn.getText(), ButterworthFilter.BANDPASS_1_50HZ);

        this.startSpinModel.setValue((int) this.dtwPlugin.getStartPosition());
        this.endSpinModel.setValue((int) this.dtwPlugin.getEndPosition());

    }

    private void setupPlugins() {
        this.dtwPlugin = new DTWPlugin();
        this.dtwPlugin = dtwPlugin;
//        this.shadowPlugin = new ShadowPlugin();
//        this.plotControl.addPluginToPlot(this.shadowPlugin);
//        this.plotControl.addPluginToPlot(this.dtwPlugin);
        this.plotControl.addPluginToPlot(this.dtwPlugin);
    }

    private void createUIComponents() {
        plotControl = new PlotControl(600, 60);
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
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(controlPanel, gbc);
        dtwPanel = new JPanel();
        dtwPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        controlPanel.add(dtwPanel, gbc);
        clearDtwBtn = new JButton();
        clearDtwBtn.setText("Clear DTW");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        dtwPanel.add(clearDtwBtn, gbc);
        dtwDistanceLbl = new JLabel();
        dtwDistanceLbl.setText("DTW distance:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        dtwPanel.add(dtwDistanceLbl, gbc);
        dtwBtn = new JButton();
        dtwBtn.setText("DTW Analysis");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        dtwPanel.add(dtwBtn, gbc);
        moveShadowCheckBox = new JCheckBox();
        moveShadowCheckBox.setEnabled(false);
        moveShadowCheckBox.setText("move shadow");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        dtwPanel.add(moveShadowCheckBox, gbc);
        dtwValueLbl = new JLabel();
        dtwValueLbl.setFont(new Font(dtwValueLbl.getFont().getName(), Font.BOLD, dtwValueLbl.getFont().getSize()));
        dtwValueLbl.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        dtwPanel.add(dtwValueLbl, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dtwPanel.add(spacer1, gbc);
        slicerPanel = new JPanel();
        slicerPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        controlPanel.add(slicerPanel, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("~");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        slicerPanel.add(label1, gbc);
        tagLbl = new JLabel();
        tagLbl.setText("Tag:");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        slicerPanel.add(tagLbl, gbc);
        sliceStartSpinner = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        slicerPanel.add(sliceStartSpinner, gbc);
        tagField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        slicerPanel.add(tagField, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 2;
        gbc.weightx = 2.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        slicerPanel.add(spacer2, gbc);
        savedHintLbl = new JLabel();
        savedHintLbl.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        slicerPanel.add(savedHintLbl, gbc);
        sliceButton = new JButton();
        sliceButton.setText("Slice!");
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        slicerPanel.add(sliceButton, gbc);
        sliceEndSpinner = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        slicerPanel.add(sliceEndSpinner, gbc);
        loadBtn = new JButton();
        loadBtn.setText("Load EEG Raw Data...");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        slicerPanel.add(loadBtn, gbc);
        sliceLbl = new JLabel();
        sliceLbl.setText("Slicer");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        slicerPanel.add(sliceLbl, gbc);
        filterPanel = new JPanel();
        filterPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        controlPanel.add(filterPanel, gbc);
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
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filterPanel.add(spacer3, gbc);
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
        channelPanel = new JPanel();
        channelPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        controlPanel.add(channelPanel, gbc);
        channelLbl = new JLabel();
        channelLbl.setText("Channels");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channelLbl, gbc);
        channel1CheckBox = new JCheckBox();
        channel1CheckBox.setText("1");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channel1CheckBox, gbc);
        channel2CheckBox = new JCheckBox();
        channel2CheckBox.setText("2");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channel2CheckBox, gbc);
        channel3CheckBox = new JCheckBox();
        channel3CheckBox.setSelected(true);
        channel3CheckBox.setText("3");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channel3CheckBox, gbc);
        channel4CheckBox = new JCheckBox();
        channel4CheckBox.setText("4");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channel4CheckBox, gbc);
        channel5CheckBox = new JCheckBox();
        channel5CheckBox.setText("5");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channel5CheckBox, gbc);
        channel6CheckBox = new JCheckBox();
        channel6CheckBox.setText("6");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channel6CheckBox, gbc);
        channel7CheckBox = new JCheckBox();
        channel7CheckBox.setText("7");
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channel7CheckBox, gbc);
        channel8CheckBox = new JCheckBox();
        channel8CheckBox.setLabel("8");
        channel8CheckBox.setText("8");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        channelPanel.add(channel8CheckBox, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        channelPanel.add(spacer4, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(plotControl, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
