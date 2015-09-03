package view.component.dataview;

import model.datasource.FilteredFiniteDataSource;
import model.datasource.FiniteLengthDataSource;
import model.datasource.FragmentDataSource;
import model.filter.DomainTransformFilter;

import javax.swing.*;

/**
 * Created by maeglin89273 on 8/19/15.
 */
public class DataLabel extends JPanel {

    private final FragmentDataSource data;
    private final FilteredFiniteDataSource fftData;
    private final FilteredFiniteDataSource dwtData;

    private JCheckBox labelCkBox;

    public DataLabel(FragmentDataSource data, DatasetView.DataLabelGroupManager groupManager) {
        this.data = data;
        this.fftData = new FilteredFiniteDataSource(data);
        this.fftData.addFilters(DomainTransformFilter.FFT);
        this.dwtData = new FilteredFiniteDataSource(data);
        this.dwtData.addFilters(DomainTransformFilter.SWT_COIF4);
        this.initComponents("t=" + data.getStartingPosition(), groupManager);
    }

    private void initComponents(String text, DatasetView.DataLabelGroupManager groupManager) {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.labelCkBox = new JCheckBox(text);

        this.labelCkBox.setAlignmentX(LEFT_ALIGNMENT);
        this.add(labelCkBox);

        this.add(Box.createHorizontalGlue());

        JButton removeBtn = new JButton("×");
        removeBtn.setAlignmentX(LEFT_ALIGNMENT);
        this.add(removeBtn);

        this.setSize(this.getPreferredSize());
        groupManager.setupComponents(this, this.labelCkBox, removeBtn);
    }

    public FiniteLengthDataSource getFFTData() {
        return this.fftData;
    }

    public FiniteLengthDataSource getDWTData() {
        return this.dwtData;
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
}