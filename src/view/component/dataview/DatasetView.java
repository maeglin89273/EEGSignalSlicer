package view.component.dataview;

import model.datasource.*;
import view.component.plugin.SimilarStreamsPlottingPlugin;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * Created by maeglin89273 on 8/19/15.
 */
public class DatasetView extends JPanel {

    ;
    private JRadioButton headerRdoBtn;
    private Box datasetBox;

    private final SimilarStreamsPlottingPlugin.SimilarStreamsDataSource fftPlottingData;
    private final SimilarStreamsPlottingPlugin.SimilarStreamsDataSource dwtPlottingData;
    private final DataLabelGroupManager labelManager;
    private JScrollPane scrollPane;

    public DatasetView(String tag, TrainingPanel.DatasetViewGroupManager datasetManager) {
        this.fftPlottingData = new SimilarStreamsPlottingPlugin.SimilarStreamsDataSource();
        this.dwtPlottingData = new SimilarStreamsPlottingPlugin.SimilarStreamsDataSource();

        this.labelManager = new DataLabelGroupManager();

        this.initComponents(tag, datasetManager);
    }

    private void initComponents(String tag, TrainingPanel.DatasetViewGroupManager datasetManager) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Box headerBox = Box.createHorizontalBox();

        this.headerRdoBtn = new JRadioButton();
        this.headerRdoBtn.setText(tag);
        this.headerRdoBtn.setAlignmentX(LEFT_ALIGNMENT);
        headerBox.add(this.headerRdoBtn);

        headerBox.add(Box.createHorizontalGlue());

        JButton removeBtn = new JButton();
        removeBtn.setText("×");
        removeBtn.setAlignmentX(RIGHT_ALIGNMENT);
        headerBox.add(removeBtn);

        this.add(headerBox);

        this.datasetBox = SingleDirectionBox.createVerticalBox();
        scrollPane = new JScrollPane(this.datasetBox, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(scrollPane);

        datasetManager.setupViewComponents(this, headerRdoBtn, removeBtn);

    }

    public String getTag() {
        return this.headerRdoBtn.getText();
    }

    public void addNewData(FragmentDataSource data) {
        data.setFrangmentTag(this.getTag());
        this.datasetBox.add(new DataLabel(data, this.labelManager));
        this.layoutDatasetPanel();
    }


    public SimilarStreamsPlottingPlugin.SimilarStreamsDataSource getFFTDataSource() {
        return this.fftPlottingData;
    }

    public SimilarStreamsPlottingPlugin.SimilarStreamsDataSource getDWTDataSource() {
        return this.dwtPlottingData;
    }

    public Collection<FragmentDataSource> getAllData(boolean selectedDataOnly) {
        Collection<FragmentDataSource> allData = new TreeSet<>((o1, o2) -> {
            return (int) (o1.getStartingPosition() - o2.getStartingPosition());
        });

        DataLabel label;
        for (int i = 0; i < this.datasetBox.getComponentCount(); i++) {
            label = (DataLabel) this.datasetBox.getComponent(i);
            if (!selectedDataOnly || label.isDataSelected()) {
                allData.add(label.getData());
            }
        }
        return allData;
    }

    public void discardDataset() {
        for (int i = 0; i < datasetBox.getComponentCount(); i++) {
            DataLabel label = (DataLabel) datasetBox.getComponent(i);
            this.fftPlottingData.removeDataSource(label.getFFTData());
            this.dwtPlottingData.removeDataSource(label.getDWTData());
            label.discard();
        }
        datasetBox.removeAll();

    }

    public void setTag(String tag) {
        this.headerRdoBtn.setText(tag);
        for (int i = 0; i < datasetBox.getComponentCount(); i++) {
            DataLabel label = (DataLabel) datasetBox.getComponent(i);
            label.setTag(tag);
        }
    }


    class DataLabelGroupManager implements ActionListener {

        private final String LABEL_KEY = "data_label";

        public void setupComponents(DataLabel label, JCheckBox showBox, JButton removeBtn) {
            showBox.putClientProperty(LABEL_KEY, label);
            removeBtn.putClientProperty(LABEL_KEY, label);

            showBox.addActionListener(this);
            removeBtn.addActionListener(this);
            showBox.setSelected(true);
            showDataSource(label);
        }

        private void showDataSource(DataLabel label) {
            fftPlottingData.addDataSource(label.getFFTData());
            dwtPlottingData.addDataSource(label.getDWTData());
        }

        private void hideDataSource(DataLabel label) {
            fftPlottingData.removeDataSource(label.getFFTData());
            dwtPlottingData.removeDataSource(label.getDWTData());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DataLabel label = (DataLabel) ((JComponent)e.getSource()).getClientProperty(LABEL_KEY);
            if (e.getSource() instanceof JCheckBox) {
                if (label.isDataSelected()) {
                    showDataSource(label);
                } else {
                    hideDataSource(label);
                }
            } else {
                hideDataSource(label);
                label.discard();
                datasetBox.remove(label);
                layoutDatasetPanel();
            }
        }
    }

    private void layoutDatasetPanel() {
        scrollPane.validate();
        scrollPane.repaint();
    }
}
