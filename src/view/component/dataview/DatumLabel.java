package view.component.dataview;

import model.datasource.FilteredFiniteDataSource;
import model.datasource.FiniteLengthDataSource;
import model.datasource.FragmentDataSource;
import model.filter.DomainTransformFilter;

import javax.swing.*;

/**
 * Created by maeglin89273 on 8/19/15.
 */
public class DatumLabel extends JPanel {

    private final FragmentDataSource data;

    private JCheckBox labelCkBox;
    private JButton removeBtn;

    public DatumLabel(FragmentDataSource data) {
        this.data = data;
        this.initComponents("t=" + data.getStartingPosition());
    }

    private void initComponents(String text) {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.labelCkBox = new JCheckBox(text);

        this.labelCkBox.setAlignmentX(LEFT_ALIGNMENT);
        this.add(labelCkBox);

        this.add(Box.createHorizontalGlue());

        removeBtn = new JButton("×");
        removeBtn.setAlignmentX(LEFT_ALIGNMENT);
        this.add(removeBtn);

        this.setSize(this.getPreferredSize());
    }

    public FragmentDataSource getData() {
        return this.data;
    }

    public boolean isDataSelected() {
        return this.labelCkBox.isSelected();
    }


    public void discard() {
        this.data.setViewingSource(false);
    }

    public void setTag(String tag) {
        this.data.setFrangmentTag(tag);
    }

    public JCheckBox getShowBtn() {
        return labelCkBox;
    }

    public JButton getRemoveBtn() {
        return removeBtn;
    }
}
