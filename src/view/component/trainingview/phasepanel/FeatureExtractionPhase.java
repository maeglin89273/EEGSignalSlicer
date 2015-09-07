package view.component.trainingview.phasepanel;

import view.component.trainingview.phasepanel.basecomponent.*;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 9/5/15.
 */
public class FeatureExtractionPhase extends SectionPanel {
    private NumberField<Integer> sampleRateField;
    private NumberField<Integer> windowSizeField;
    private RangeFieldPair<Integer> fftFreqRange;
    private RangeFieldPair<Integer> wavletLevelRange;

    public FeatureExtractionPhase(boolean windowSizeModifiable) {
        super("Feature Extraction");
        this.setupComponents(windowSizeModifiable);

    }

    private void setupComponents(boolean windowSizeModifiable) {
        this.appendDescription("We extract time and frequency information from signals as features.");
        NameValueTable table = new NameValueTable();

        this.sampleRateField = NumberField.getBoundedIntegerInstance(1, null);
        table.addNameValue("Sample-rate", UnitWrap.wrap(this.sampleRateField, "Hz"));
        this.windowSizeField = NumberField.getBoundedIntegerInstance(1, null);
        this.windowSizeField.setEnabled(windowSizeModifiable);
        table.addNameValue("Window Size", UnitWrap.wrap(this.windowSizeField, "samples"));

        this.append(table);

        SectionPanel fftSection = new SectionPanel("Fast Fourier Transform", OptionLabel.LabelType.CHECK_BOX);
        table = new NameValueTable();
        this.fftFreqRange = RangeFieldPair.getIntegerInstance(1, 256);
        table.addNameValue("Frequency Range", UnitWrap.wrap(this.fftFreqRange, "Hz"));
        fftSection.append(table);
        this.append(fftSection);

        SectionPanel wtSection = new SectionPanel("Wavelet Transform", OptionLabel.LabelType.CHECK_BOX);
        table = new NameValueTable();
        table.addNameValue("Type", new OptionList("Stationary", "Discrete"));
        table.addNameValue("Wavelet", new WaveletOptions("coif", 4));
        this.wavletLevelRange = RangeFieldPair.getIntegerInstance(0, 10);
        table.addNameValue("Level Range", this.wavletLevelRange);
        wtSection.append(table);
        wtSection.setEnabled(false);
        this.append(wtSection);


    }

    private static class WaveletOptions extends CompoundStructuredValueComponent {
        public static final Map<String, int[]> WAVELET_TABLE = new HashMap<>();
        public static final int MIN = 0;
        public static final int MAX = 1;
        static {
            WAVELET_TABLE.put("db", new int[]{1, 20});
            WAVELET_TABLE.put("coif", new int[]{1, 5});
            WAVELET_TABLE.put("sym", new int[]{2, 20});
        }

        private JComboBox<String> waveletTypeCombo;
        private SpinnerNumberModel waveletNumModel;
        private JSpinner waveletNumSpinner;

        public WaveletOptions(String defaultWavelet, int defaultWaveletNum) {
            this.setupComponents(defaultWavelet, defaultWaveletNum);
            this.setupListeners();
        }

        private void setupListeners() {
            this.waveletTypeCombo.addActionListener(evt -> {
                waveletTypeSelected(waveletTypeCombo.getSelectedItem());
            });

        }

        private void waveletTypeSelected(Object type) {
            int[] waveletNumRange = WAVELET_TABLE.get(type);

            waveletNumModel.setMinimum(waveletNumRange[MIN]);
            waveletNumModel.setMaximum(waveletNumRange[MAX]);
            waveletNumModel.setValue(RangeFieldPair.boundNumber((Integer)waveletNumModel.getValue(), waveletNumRange[MIN], waveletNumRange[MAX]));
        }

        private void setupComponents(String defaultWavelet, int defaultWaveletNum) {
            this.setLayout(new GridBagLayout());

            this.waveletTypeCombo = new JComboBox<String>(WAVELET_TABLE.keySet().toArray(new String[0]));
            this.waveletTypeCombo.setEditable(false);
            this.waveletTypeCombo.setSelectedItem(defaultWavelet);

            this.waveletNumSpinner = new JSpinner();
            ((JSpinner.DefaultEditor)this.waveletNumSpinner.getEditor()).getTextField().setColumns(3);
            waveletNumModel = (SpinnerNumberModel)this.waveletNumSpinner.getModel();
            waveletTypeSelected(defaultWavelet);
            waveletNumModel.setValue(defaultWaveletNum);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            this.add(this.waveletTypeCombo, gbc);

            gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            this.add(this.waveletNumSpinner, gbc);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            this.waveletTypeCombo.setEnabled(enabled);
            this.waveletNumSpinner.setEnabled(enabled);
        }

        @Override
        public boolean isValueReady() {
            return true;
        }

        @Override
        public Object getStructuredValue() {
            return this.waveletTypeCombo.getSelectedItem().toString() + this.waveletNumModel.getValue().toString();
        }
    }
}
