package view.component;

import view.component.plot.InteractivePlotView;

import javax.swing.*;
import java.awt.*;

/**
 * Created by maeglin89273 on 9/1/15.
 */
public class FeaturePanelLayout {
    private JPanel this12345;
    private JCheckBox fftCkBox;
    private InteractivePlotView fftPlot;
    private JCheckBox swtCkBox;
    private InteractivePlotView swtPlot;
    private JButton plotModeBtn;
    private JCheckBox meanCkBox;


    private void createUIComponents() {

    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
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
        this12345 = new JPanel();
        this12345.setLayout(new GridBagLayout());
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
        this12345.add(fftCkBox, gbc);
        fftPlot.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this12345.add(fftPlot, gbc);
        swtCkBox = new JCheckBox();
        swtCkBox.setEnabled(false);
        swtCkBox.setSelected(true);
        swtCkBox.setText("Stationary Wavelet Transform");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        this12345.add(swtCkBox, gbc);
        swtPlot.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this12345.add(swtPlot, gbc);
        plotModeBtn = new JButton();
        plotModeBtn.setEnabled(false);
        plotModeBtn.setText("source and data");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        this12345.add(plotModeBtn, gbc);
        meanCkBox = new JCheckBox();
        meanCkBox.setEnabled(false);
        meanCkBox.setText("mean");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        this12345.add(meanCkBox, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return this12345;
    }
}
