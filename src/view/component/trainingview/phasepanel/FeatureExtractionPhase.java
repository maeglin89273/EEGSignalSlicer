package view.component.trainingview.phasepanel;

import net.razorvine.pyro.PyroProxy;
import oracle.PyOracle;
import view.component.trainingview.phasepanel.basecomponent.*;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by maeglin89273 on 9/5/15.
 */
public class FeatureExtractionPhase extends PhasePanel {
    private static int DEFAULT_SAMPLE_RATE = 250;
    private static int DEFAULT_WINDOW_SIZE = 512;

    private Collection<String> streamCache;

    private PyroProxy transformOracle;

    private NumberField<Integer> sampleRateField;
    private NumberField<Integer> windowSizeField;
    private RangeSpinnerPair fftFreqRange;
    private RangeSpinnerPair wtLevelRange;
    private OptionList streamsList;
    private NameValueTable sectionlessTable;
    private OptionLabel streamsLbl;
    private OptionList wtTypeList;
    private WaveletOptions waveletOpts;
    private IntegerValueSpinner pcaValueSpinner;
    private ValuedLabel featureSizeLbl;
    private PropertyChangeListener featureSizeUpdater;
    private SectionPanel fftSection;
    private SectionPanel wtSection;

    public FeatureExtractionPhase() {
        super("Feature Extraction");
        this.setupComponents();
        this.setupListeners();
        this.transformOracle = PyOracle.getInstance().getOracle("transform");
        this.sampleRateField.setValue(DEFAULT_WINDOW_SIZE);
        this.sampleRateField.setValue(DEFAULT_SAMPLE_RATE);
    }

    private void setupListeners() {
        this.featureSizeUpdater = evt-> {
            updateFeatureSize();
        };

        this.fftSection.getCaptionLabel().addItemListener(evt-> {
            updateFeatureSize();
        });

        this.wtSection.getCaptionLabel().addItemListener(evt-> {
            updateFeatureSize();
        });

        sampleRateField.addPropertyChangeListener("value", evt -> {

            if (sampleRateField.isValueReady()) {
                int sampleRate = (Integer) evt.getNewValue();
                updateWindowSizeMin(sampleRate);
                updateFreqRangeMax(sampleRate);
            }
        });

        windowSizeField.addPropertyChangeListener("value", evt-> {
            if (windowSizeField.isValueReady()) {
                updateLavelRangeMax((Integer) evt.getNewValue());
                updateFeatureSize();
            }
        });

        waveletOpts.addPropertyChangeListener("value", evt -> {
            String newValue = evt.getNewValue().toString();
            try {
                transformOracle.call_oneway("set_wt_wavelet", newValue);
            } catch (IOException e) {
                e.printStackTrace();
            }
            updateLavelRangeMax(getWindowSize());
            updateFeatureSize();
        });

        wtTypeList.addPropertyChangeListener("value", evt -> {
            String newValue = evt.getNewValue().toString();
            try {
                transformOracle.call_oneway("set_wt_type", newValue);
            } catch (IOException e) {
                e.printStackTrace();
            }
            updateLavelRangeMax(getWindowSize());
            updateFeatureSize();
        });

        fftFreqRange.addPropertyChangeListener("start_value", featureSizeUpdater);

        fftFreqRange.addPropertyChangeListener("end_value", featureSizeUpdater);

        wtLevelRange.addPropertyChangeListener("start_value", evt -> {
            try {
                transformOracle.call_oneway("set_wt_level_min", evt.getNewValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
            updateFeatureSize();
        });

        wtLevelRange.addPropertyChangeListener("end_value", evt -> {
            try {
                transformOracle.call_oneway("set_wt_level_max", evt.getNewValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
            updateFeatureSize();
        });
    }

    private void setupComponents() {
        this.appendDescription("We extract time and frequency information from signals as features.");
        this.sectionlessTable = new NameValueTable();

        this.sampleRateField = NumberField.getBoundedIntegerInstance(1, null);
        sectionlessTable.addNameValue("Sample-rate", TextWrap.postfixWrap(this.sampleRateField, "Hz"));
        this.windowSizeField = NumberField.getBoundedIntegerInstance(1, null);
        sectionlessTable.addNameValue("Window Size", TextWrap.postfixWrap(this.windowSizeField, "samples"));
        this.streamsList = new OptionList(OptionList.OptionType.MULTI, new String[0]);
        this.streamsLbl = sectionlessTable.addNameValue("Streams", this.streamsList);
        this.append(sectionlessTable);

        OptionList.CheckBoxGroup group = new OptionList.CheckBoxGroup();
        
        fftSection = new SectionPanel("Fast Fourier Transform", OptionLabel.LabelType.CHECK_BOX);
        NameValueTable table = new NameValueTable();
        this.fftFreqRange = new RangeSpinnerPair(1, 60);
        table.addNameValue("Frequency Range", TextWrap.postfixWrap(this.fftFreqRange, "Hz"));
        fftSection.append(table);
        this.append(fftSection);

        wtSection = new SectionPanel("Wavelet Transform", OptionLabel.LabelType.CHECK_BOX);
        table = new NameValueTable();
        this.wtTypeList = new OptionList(OptionList.OptionType.SINGLE, "Stationary", "Discrete");
        table.addNameValue("Type", this.wtTypeList);
        this.waveletOpts = new WaveletOptions("coif", 4);
        table.addNameValue("Wavelet", this.waveletOpts);
        this.wtLevelRange = new RangeSpinnerPair(1, 4);
        table.addNameValue("Level Range", this.wtLevelRange);
        wtSection.append(table);
        this.append(wtSection);

        SectionPanel afterTransformationSection = new SectionPanel("After Transformation");

        table = new NameValueTable();
        this.featureSizeLbl = new ValuedLabel("0");
        this.featureSizeLbl.setFont(this.featureSizeLbl.getFont().deriveFont(Font.BOLD));
        this.pcaValueSpinner = new IntegerValueSpinner(500, 1, null);
        table.addNameValue("Dimension", this.featureSizeLbl);

        table.addNameValue(OptionLabel.LabelType.CHECK_BOX, "Scaler", new OptionList(OptionList.OptionType.SINGLE, "Mean Std Scaler", "Min Max Scaler")).setSelected(false);
        table.addNameValue(OptionLabel.LabelType.CHECK_BOX, "PCA", TextWrap.postfixWrap(this.pcaValueSpinner, "components")).setSelected(false);

        afterTransformationSection.append(table);
        this.append(afterTransformationSection);

        group.add(fftSection.getCaptionLabel());
        group.add(wtSection.getCaptionLabel());
    }

    public void setWindowSizeModifiable(boolean modifiable) {
        this.sampleRateField.setEnabled(modifiable);
        this.windowSizeField.setEnabled(modifiable);
    }

    public void setStreams(Collection<String> streams) {
        if (!Objects.equals(this.streamCache, streams)) {
            this.streamsList = new OptionList(OptionList.OptionType.MULTI, streams);
            this.streamsList.addPropertyChangeListener(featureSizeUpdater);
            this.sectionlessTable.replaceValue(streamsLbl, this.streamsList);
            this.streamCache = streams;
            this.updateFeatureSize();
        }
    }

    public void setWindowSizeMax(int max) {
        this.windowSizeField.setMaximum(max);
    }

    public int getWindowSize() {
        return (Integer) this.windowSizeField.getValue();
    }

    public int getSampleRate() {
        return (Integer) this.sampleRateField.getValue();
    }

    public List<Integer> getFFTFreqRange() {
        return (List<Integer>) this.fftFreqRange.getStructuredValue();
    }

    public String getWaveletTransformType() {
        return wtTypeList.getStructuredValue().toString();
    }

    private void updateLavelRangeMax(int windowSize) {
        try {
            this.wtLevelRange.setMaximum((Integer) transformOracle.call("wt_max_level", windowSize));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateFreqRangeMax(int sampleRate) {
        this.fftFreqRange.setMaximum(sampleRate / 2);
    }

    private void updateWindowSizeMin(int sampleRate) {
        this.windowSizeField.setMinimum(sampleRate);
    }


    private void updateFeatureSize() {

        int featureSize = ((Collection<String>)streamsList.getStructuredValue()).size();

        double windowOverRate = getWindowSize() / (double) getSampleRate();
        int lengthAfterFFT = 0;
        int lengthAfterWT = 0;

        if (fftSection.getCaptionLabel().isSelected()) {
            lengthAfterFFT = (int) ((fftFreqRange.getEndValue() - fftFreqRange.getStartValue() + 1) * windowOverRate);
        }

        if (wtSection.getCaptionLabel().isSelected()) {
            try {
                lengthAfterWT = (Integer) transformOracle.call("length_after_wt", getWindowSize());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        featureSize *= (lengthAfterFFT + lengthAfterWT);
        featureSizeLbl.setText(Integer.toString(featureSize));
        if (featureSize > 1) {
            pcaValueSpinner.setMaximum(featureSize - 1);
        }

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
                firePropertyChange("value", null, getStructuredValue());
            });
            this.waveletNumModel.addChangeListener(evt -> {
                firePropertyChange("value", null, getStructuredValue());
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
            JFormattedTextField spinnerField = ((JSpinner.DefaultEditor) this.waveletNumSpinner.getEditor()).getTextField();
            spinnerField.setHorizontalAlignment(JTextField.RIGHT);
            spinnerField.setColumns(3);
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
