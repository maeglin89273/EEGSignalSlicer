package view;

import model.filter.BandpassFilter;
import model.datasource.EEGChannels;
import model.RawDataFileUtils;
import view.component.PlotControl;
import view.component.plugin.ShadowPlugin;
import view.component.plugin.SlicerPlugin;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

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
    private JRadioButton a150HzRadioButton;
    private JRadioButton a713HzRadioButton;
    private JRadioButton a1550HzRadioButton;
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
    private JButton shadowBtn;
    private JButton clearShadowBtn;

    private JCheckBox[] channelCheckBoxArray;
    private ButtonGroup filterChoiceGroup;
    private SpinnerNumberModel startSpinModel;
    private SpinnerNumberModel endSpinModel;

    private EEGChannels data;
    private SlicerPlugin slicePlugin;
    private ShadowPlugin shadowPlugin;


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
                if (value != slicePlugin.getStartPosition()) {
                    slicePlugin.setStartPosition(value);
                }
            }
        });

        this.endSpinModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = ((Number) endSpinModel.getValue()).intValue();
                if (value != slicePlugin.getEndPosition()) {
                    slicePlugin.setEndPosition(value);

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
            }
        };

        for (int i = 0; i < this.channelCheckBoxArray.length; i++) {
            this.channelCheckBoxArray[i].addActionListener(checkBoxListener);
        }

        ActionListener filterBtnListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                data.setBandpassFilter(mapFilter(filterChoiceGroup.getSelection().getActionCommand()));
                plotControl.refreshPlot();
            }

            private BandpassFilter mapFilter(String actionCommand) {
                switch (actionCommand) {
                    case "1~50Hz":
                        return BandpassFilter.BANDPASS_1_50HZ;
                    case "7~13Hz":
                        return BandpassFilter.BANDPASS_7_13HZ;
                    case "15~50Hz":
                        return BandpassFilter.BANDPASS_15_50HZ;
                }

                return null;
            }
        };

        Enumeration<AbstractButton> filterBtns = this.filterChoiceGroup.getElements();
        AbstractButton filterBtn;

        for (; filterBtns.hasMoreElements(); ) {
            filterBtn = filterBtns.nextElement();
            filterBtn.setActionCommand(filterBtn.getText());
            filterBtn.addActionListener(filterBtnListener);
        }

        shadowBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shadowPlugin.makeShadow();
            }
        });

        clearShadowBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shadowPlugin.clear();
            }
        });

        slicePlugin.setRangeChangedListener(new SlicerPlugin.RangeChangedListener() {
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
    }

    private void setupOthers() {
        this.startSpinModel = (SpinnerNumberModel) this.sliceStartSpinner.getModel();
        this.endSpinModel = (SpinnerNumberModel) this.sliceEndSpinner.getModel();
        this.channelCheckBoxArray = new JCheckBox[]
                {this.channel1CheckBox, this.channel2CheckBox, this.channel3CheckBox, this.channel4CheckBox,
                        this.channel5CheckBox, this.channel6CheckBox, this.channel7CheckBox, this.channel8CheckBox};


        filterChoiceGroup = new ButtonGroup();
        filterChoiceGroup.add(a150HzRadioButton);
        filterChoiceGroup.add(a713HzRadioButton);
        filterChoiceGroup.add(a1550HzRadioButton);

        this.startSpinModel.setValue((int) this.slicePlugin.getStartPosition());
        this.endSpinModel.setValue((int) this.slicePlugin.getEndPosition());

    }

    private void setupPlugins() {
        this.slicePlugin = new SlicerPlugin();
        this.shadowPlugin = new ShadowPlugin();
        this.plotControl.addPluginToPlot(this.slicePlugin);
        this.plotControl.addPluginToPlot(this.shadowPlugin);
    }

    private void createUIComponents() {
        plotControl = new PlotControl(600, 50);
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
        slicerPanel = new JPanel();
        slicerPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        controlPanel.add(slicerPanel, gbc);
        sliceLbl = new JLabel();
        sliceLbl.setText("Slicer");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        slicerPanel.add(sliceLbl, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("~");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        slicerPanel.add(label1, gbc);
        loadBtn = new JButton();
        loadBtn.setText("Load EEG Raw Data...");
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        slicerPanel.add(loadBtn, gbc);
        tagLbl = new JLabel();
        tagLbl.setText("Tag:");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        slicerPanel.add(tagLbl, gbc);
        sliceStartSpinner = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        slicerPanel.add(sliceStartSpinner, gbc);
        tagField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        slicerPanel.add(tagField, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 3;
        gbc.weightx = 2.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        slicerPanel.add(spacer1, gbc);
        savedHintLbl = new JLabel();
        savedHintLbl.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        slicerPanel.add(savedHintLbl, gbc);
        shadowBtn = new JButton();
        shadowBtn.setText("Make Shadow");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        slicerPanel.add(shadowBtn, gbc);
        sliceButton = new JButton();
        sliceButton.setText("Slice!");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        slicerPanel.add(sliceButton, gbc);
        sliceEndSpinner = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        slicerPanel.add(sliceEndSpinner, gbc);
        clearShadowBtn = new JButton();
        clearShadowBtn.setText("Clear Shadow");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        slicerPanel.add(clearShadowBtn, gbc);
        channelPanel = new JPanel();
        channelPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
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
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        channelPanel.add(spacer2, gbc);
        filterPanel = new JPanel();
        filterPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        controlPanel.add(filterPanel, gbc);
        a150HzRadioButton = new JRadioButton();
        a150HzRadioButton.setSelected(true);
        a150HzRadioButton.setText("1~50Hz");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(a150HzRadioButton, gbc);
        a713HzRadioButton = new JRadioButton();
        a713HzRadioButton.setText("7~13Hz");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(a713HzRadioButton, gbc);
        a1550HzRadioButton = new JRadioButton();
        a1550HzRadioButton.setText("15~50Hz");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(a1550HzRadioButton, gbc);
        filterLbl = new JLabel();
        filterLbl.setText("Filter");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        filterPanel.add(filterLbl, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filterPanel.add(spacer3, gbc);
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
