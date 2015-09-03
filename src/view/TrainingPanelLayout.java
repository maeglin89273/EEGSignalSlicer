package view;

import view.component.dataview.MessageBar;

import javax.swing.*;
import java.awt.*;

/**
 * Created by maeglin89273 on 8/19/15.
 */
public class TrainingPanelLayout {

    private JPanel this2015;
    private JCheckBox trainingCkBox;
    private JButton newCategoryBtn;
    private JTextField actionField;
    private JButton addDataBtn;
    private JButton trainBtn;
    private JButton saveDatasetBtn;
    private JScrollPane scrollPane;
    private JButton loadDatasetBtn;
    private JSpinner rangeStartSpinner;
    private MessageBar msgBar;
    private JButton plot2DBtn;
    private JButton plot3DBtn;
    private JCheckBox predictCkBox;
    private JButton renameBtn;
    private JCheckBox coordinatesCkBox;
    private JButton addDatasetBtn;

    private void createUIComponents() {
        // TODO: place custom component creation code here
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
        this2015 = new JPanel();
        this2015.setLayout(new GridBagLayout());
        actionField = new JTextField();
        actionField.setEnabled(false);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this2015.add(actionField, gbc);
        trainingCkBox = new JCheckBox();
        trainingCkBox.setText("Training");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this2015.add(trainingCkBox, gbc);
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
        this2015.add(scrollPane, gbc);
        newCategoryBtn = new JButton();
        newCategoryBtn.setEnabled(false);
        newCategoryBtn.setText("new category");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this2015.add(newCategoryBtn, gbc);
        rangeStartSpinner = new JSpinner();
        rangeStartSpinner.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this2015.add(rangeStartSpinner, gbc);
        addDataBtn = new JButton();
        addDataBtn.setEnabled(false);
        addDataBtn.setText("add data");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        this2015.add(addDataBtn, gbc);
        msgBar = new MessageBar();
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        this2015.add(msgBar, gbc);
        renameBtn = new JButton();
        renameBtn.setEnabled(false);
        renameBtn.setText("rename");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this2015.add(renameBtn, gbc);
        loadDatasetBtn = new JButton();
        loadDatasetBtn.setEnabled(false);
        loadDatasetBtn.setText("load...");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this2015.add(loadDatasetBtn, gbc);
        saveDatasetBtn = new JButton();
        saveDatasetBtn.setEnabled(false);
        saveDatasetBtn.setText("save");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this2015.add(saveDatasetBtn, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 5;
        gbc.gridheight = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        this2015.add(spacer1, gbc);
        trainBtn = new JButton();
        trainBtn.setEnabled(false);
        trainBtn.setText("train");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this2015.add(trainBtn, gbc);
        plot3DBtn = new JButton();
        plot3DBtn.setEnabled(false);
        plot3DBtn.setText("plot 3D");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this2015.add(plot3DBtn, gbc);
        plot2DBtn = new JButton();
        plot2DBtn.setEnabled(false);
        plot2DBtn.setText("plot 2D");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this2015.add(plot2DBtn, gbc);
        coordinatesCkBox = new JCheckBox();
        coordinatesCkBox.setEnabled(false);
        coordinatesCkBox.setText("coordinates");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this2015.add(coordinatesCkBox, gbc);
        predictCkBox = new JCheckBox();
        predictCkBox.setEnabled(false);
        predictCkBox.setText("predict");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this2015.add(predictCkBox, gbc);
        addDatasetBtn = new JButton();
        addDatasetBtn.setEnabled(false);
        addDatasetBtn.setText("add...");
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this2015.add(addDatasetBtn, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return this2015;
    }
}
