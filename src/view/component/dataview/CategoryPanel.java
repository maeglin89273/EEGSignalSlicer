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
public class CategoryPanel extends JPanel {

    ;
    private JRadioButton headerRdoBtn;
    private Box datasetBox;
    private JButton removeBtn;

    private final SimilarStreamsPlottingPlugin.SimilarStreamsDataSource fftPlottingData;
    private final SimilarStreamsPlottingPlugin.SimilarStreamsDataSource dwtPlottingData;
    private final DataLabelGroupManager dataManager;
    private JScrollPane scrollPane;
    private JLabel countLbl;


    public CategoryPanel(String category) {
        this.fftPlottingData = new SimilarStreamsPlottingPlugin.SimilarStreamsDataSource();
        this.dwtPlottingData = new SimilarStreamsPlottingPlugin.SimilarStreamsDataSource();

        this.dataManager = new DataLabelGroupManager();

        this.initComponents(category);
    }

    private void initComponents(String category) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Box headerBox = Box.createHorizontalBox();

        this.headerRdoBtn = new JRadioButton();
        this.headerRdoBtn.setText(category);
        this.headerRdoBtn.setAlignmentX(LEFT_ALIGNMENT);
        headerBox.add(this.headerRdoBtn);

        headerBox.add(Box.createHorizontalGlue());
        this.countLbl = new JLabel("(0)");
        countLbl.setAlignmentX(RIGHT_ALIGNMENT);

        headerBox.add(this.countLbl);

        removeBtn = new JButton();
        removeBtn.setText("×");
        removeBtn.setAlignmentX(RIGHT_ALIGNMENT);
        headerBox.add(removeBtn);

        this.add(headerBox);

        this.datasetBox = SingleDirectionBox.createVerticalBox();
        scrollPane = new JScrollPane(this.datasetBox, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(scrollPane);


    }

    public JRadioButton getCategoryRdoBtn() {
        return this.headerRdoBtn;
    }

    public JButton getRemoveBtn() {
        return this.removeBtn;
    }

    private void updateCountLabel() {
        this.countLbl.setText(String.format("(%d)", datasetBox.getComponentCount()));
    }

    public String getCategory() {
        return this.headerRdoBtn.getText();
    }

    public void addNewData(FragmentDataSource data) {
        data.setFrangmentTag(this.getCategory());
        this.dataManager.addDatum(new DatumLabel(data));
        this.updateCountLabel();
        this.layoutDatasetPanel();
    }


    public SimilarStreamsPlottingPlugin.SimilarStreamsDataSource getFFTDataSource() {
        return this.fftPlottingData;
    }

    public SimilarStreamsPlottingPlugin.SimilarStreamsDataSource getSWTDataSource() {
        return this.dwtPlottingData;
    }

    public Collection<FragmentDataSource> getAllData(boolean selectedDataOnly) {
        Collection<FragmentDataSource> allData = new TreeSet<>((o1, o2) -> {
            return (int) (o1.getStartingPosition() - o2.getStartingPosition());
        });

        DatumLabel label;
        for (int i = 0; i < this.datasetBox.getComponentCount(); i++) {
            label = (DatumLabel) this.datasetBox.getComponent(i);
            if (!selectedDataOnly || label.isDataSelected()) {
                allData.add(label.getData());
            }
        }
        return allData;
    }

    public void discardDataset() {
        for (int i = 0; i < datasetBox.getComponentCount(); i++) {
            DatumLabel label = (DatumLabel) datasetBox.getComponent(i);
            this.fftPlottingData.removeDataSource(label.getFFTData());
            this.dwtPlottingData.removeDataSource(label.getDWTData());

            label.discard();
        }
        datasetBox.removeAll();

    }

    public void setCategory(String category) {
        this.headerRdoBtn.setText(category);
        for (int i = 0; i < datasetBox.getComponentCount(); i++) {
            DatumLabel label = (DatumLabel) datasetBox.getComponent(i);
            label.setTag(category);
        }
    }


    class DataLabelGroupManager implements ActionListener {

        private final String LABEL_KEY = "data_label";

        public void addDatum(DatumLabel label) {
            JCheckBox showBox = label.getShowBtn();
            JButton removeBtn = label.getRemoveBtn();
            showBox.putClientProperty(LABEL_KEY, label);
            removeBtn.putClientProperty(LABEL_KEY, label);

            showBox.addActionListener(this);
            removeBtn.addActionListener(this);
            datasetBox.add(label);
            showBox.setSelected(true);
            showDataSource(label);
        }

        private void showDataSource(DatumLabel label) {
            fftPlottingData.addDataSource(label.getFFTData());
            dwtPlottingData.addDataSource(label.getDWTData());
        }

        private void hideDataSource(DatumLabel label) {
            fftPlottingData.removeDataSource(label.getFFTData());
            dwtPlottingData.removeDataSource(label.getDWTData());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DatumLabel label = (DatumLabel) ((JComponent)e.getSource()).getClientProperty(LABEL_KEY);
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
                updateCountLabel();
                layoutDatasetPanel();
            }
        }
    }

    private void layoutDatasetPanel() {
        scrollPane.validate();
        scrollPane.repaint();
    }
}
