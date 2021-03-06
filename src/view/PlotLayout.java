package view;

import javax.swing.*;
import java.awt.*;

/**
 * Created by maeglin89273 on 7/21/15.
 */
public class PlotLayout {
    private JPanel componentHolder;
    private JLabel startLbl;
    private JLabel endLbl;
    private JPanel plot;
    private JButton playBtn;
    private JButton backBtn;
    private JButton forthBtn;
    private JLabel negPeakLbl;
    private JLabel posPeakLbl;
    private JSlider playbackSlider;
    private JLabel speedLbl;

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
        componentHolder = new JPanel();
        componentHolder.setLayout(new GridBagLayout());
        componentHolder.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null));
        startLbl = new JLabel();
        startLbl.setText("0");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        componentHolder.add(startLbl, gbc);
        endLbl = new JLabel();
        endLbl.setText("250");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        componentHolder.add(endLbl, gbc);
        plot = new JPanel();
        plot.setLayout(new GridBagLayout());
        plot.setBackground(new Color(-1));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        componentHolder.add(plot, gbc);
        playBtn = new JButton();
        playBtn.setEnabled(false);
        playBtn.setText("Play");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        componentHolder.add(playBtn, gbc);
        backBtn = new JButton();
        backBtn.setLabel("<<");
        backBtn.setText("<<");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        componentHolder.add(backBtn, gbc);
        forthBtn = new JButton();
        forthBtn.setLabel(">>");
        forthBtn.setText(">>");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        componentHolder.add(forthBtn, gbc);
        negPeakLbl = new JLabel();
        negPeakLbl.setText("-Vp");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        componentHolder.add(negPeakLbl, gbc);
        posPeakLbl = new JLabel();
        posPeakLbl.setText("+Vp");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        componentHolder.add(posPeakLbl, gbc);
        playbackSlider = new JSlider();
        playbackSlider.setValue(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        componentHolder.add(playbackSlider, gbc);
        speedLbl = new JLabel();
        speedLbl.setHorizontalAlignment(0);
        speedLbl.setHorizontalTextPosition(0);
        speedLbl.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        componentHolder.add(speedLbl, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return componentHolder;
    }
}
