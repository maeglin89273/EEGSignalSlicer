package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;

import java.util.*;
import java.util.List;

/**
 * Created by maeglin89273 on 9/5/15.
 */
public class NumberEnumerator<N extends Number & Comparable<N>> extends CompoundStructuredValueComponent {
    private static final int TEXT_FILED_COLUMN = 5;
    private static final int SPINNER_COLUMN = 3;
    private static final int MAX_NUM_PRINT = 6;

    private static Insets ENUM_LABEL_PADDING = new Insets(SpacingStandard.PADDING, 0, 0, 0);
    private static String[] SPACE_MODES = {"linear", "log"};


    private static final Calculation LINEAR_CALCULATION = new Calculation() {
        @Override
        public double calculateSample(double startValue, double spacing, int i) {
            return startValue + spacing * i;
        }

        @Override
        public double calculateEndValue(double endValue) {
            return endValue;
        }
    };

    private static final Calculation LOG_CALCULATION = new Calculation() {
        @Override
        public double calculateSample(double startValue, double spacing, int i) {
            return Math.pow(10, startValue + spacing * i);
        }

        @Override
        public double calculateEndValue(double endValue) {
            return Math.pow(10, endValue);
        }
    };

    private static final SampleGenerator<Integer> INTEGER_GENERATOR = (calculation, startValue, endValue, sampleNum) -> {
        LinkedList<Integer> result = new LinkedList<>();

        if (sampleNum > 1) {
            double spacing = (endValue - startValue) / (sampleNum - 1);
            if (sampleNum <= MAX_NUM_PRINT) {
                for (int i = 0; i < sampleNum - 1; i++) {
                    result.add((int) calculation.calculateSample(startValue, spacing, i));
                }

            } else {
                int halfNum = MAX_NUM_PRINT / 2;
                for (int i = 0; i <= halfNum; i++) {
                    result.add((int) calculation.calculateSample(startValue, spacing, i));
                }
                for (int i = sampleNum - halfNum; i < sampleNum - 1; i++) {
                    result.add((int) calculation.calculateSample(startValue, spacing, i));
                }
            }

            result.add((int)calculation.calculateEndValue(endValue));
        } else if (sampleNum == 1) {
            result.add((int)calculation.calculateSample(startValue, 0, 0));
        }

        return result;
    };

    private static final SampleGenerator<Double> FLOAT_GENERATOR = (calculation, startValue, endValue, sampleNum) -> {
        LinkedList<Double> result = new LinkedList<>();

        if (sampleNum > 1) {
            double spacing = (endValue - startValue) / (sampleNum - 1);
            if (sampleNum <= MAX_NUM_PRINT) {
                for (int i = 0; i < sampleNum - 1; i++) {
                    result.add(calculation.calculateSample(startValue, spacing, i));
                }

            } else {
                int halfNum = MAX_NUM_PRINT / 2;
                for (int i = 0; i <= halfNum; i++) {
                    result.add(calculation.calculateSample(startValue, spacing, i));
                }
                for (int i = sampleNum - halfNum; i < sampleNum - 1; i++) {
                    result.add(calculation.calculateSample(startValue, spacing, i));
                }
            }

            result.add(calculation.calculateEndValue(endValue));
        } else if (sampleNum == 1) {
            result.add(calculation.calculateSample(startValue, 0, 0));
        }
        return result;
    };

    private int modeIdx = 0;

    private int sampleNumLimit = 100;
    private JFormattedTextField startField;
    private JFormattedTextField endField;
    private JSpinner sampleNumSpinner;
    private JButton spaceModeBtn;
    private JLabel enumLbl;

    private LinkedList<? extends Number> samples;
    private final SampleGenerator<N> generator;
    private NumberFormatter linStartFormatter;
    private NumberFormatter linEndFormatter;
    private NumberFormatter logStartFormatter;
    private NumberFormatter logEndFormatter;

    private JLabel tildeLbl;
    private JLabel numOfSamplesLbl;


    private NumberEnumerator(SampleGenerator<N> generator, NumberFormatter linStartFormatter, NumberFormatter linEndFormatter, NumberFormatter logStartFormatter, NumberFormatter logEndFormatter, N initValue) {
        this.generator = generator;
        this.linStartFormatter = linStartFormatter;
        this.linEndFormatter = linEndFormatter;
        this.logStartFormatter = logStartFormatter;
        this.logEndFormatter = logEndFormatter;


        this.setupComponents(this.linStartFormatter, this.linEndFormatter);
        this.setupListeners();

        this.startField.setValue(initValue);
        this.endField.setValue(initValue);
    }

    private void setupListeners() {
        startField.addPropertyChangeListener("value", evt -> {
            updateSamples();
        });
        endField.addPropertyChangeListener("value", evt -> {
            updateSamples();
        });
        sampleNumSpinner.addChangeListener(evt -> {
            updateSamples();
        });
        spaceModeBtn.addActionListener(evt -> {
            this.modeIdx = (this.modeIdx + 1) % 2;
            spaceModeBtn.setText(this.getSpaceMode());
            if (isLinear()) {
                startField.setFormatterFactory(new DefaultFormatterFactory(linStartFormatter));
                endField.setFormatterFactory(new DefaultFormatterFactory(linEndFormatter));
            } else {
                startField.setFormatterFactory(new DefaultFormatterFactory(logStartFormatter));
                endField.setFormatterFactory(new DefaultFormatterFactory(logEndFormatter));
            }
            updateSamples();
        });
    }

    public static NumberEnumerator<Double> getFloatInstance(Double value) {
        return getFloatInstance(null, null, value);
    }

    public static NumberEnumerator<Double> getFloatInstance(Double linMin, Double logMin, Double value) {
        NumberFormatter linStart = NumberField.makeFormatter(Double.class, linMin, null);
        NumberFormatter linEnd = NumberField.makeFormatter(Double.class, linMin, null);
        NumberFormatter logStart = NumberField.makeFormatter(Double.class, logMin, null);
        NumberFormatter logEnd = NumberField.makeFormatter(Double.class, logMin, null);
        return new NumberEnumerator<>(FLOAT_GENERATOR, linStart, linEnd, logStart, logEnd, value);
    }

    public static NumberEnumerator<Integer> getIntegerInstance(Integer value) {
        return getIntegerInstance(null, null, value);
    }

    public static NumberEnumerator<Integer> getIntegerInstance(Integer linMin, Integer logMin, Integer value) {
        NumberFormatter linStart = NumberField.makeFormatter(Integer.class, linMin, null);
        NumberFormatter linEnd = NumberField.makeFormatter(Integer.class, linMin, null);
        NumberFormatter logStart = NumberField.makeFormatter(Integer.class, logMin, null);
        NumberFormatter logEnd = NumberField.makeFormatter(Integer.class, logMin, null);
        return new NumberEnumerator<>(INTEGER_GENERATOR, linStart, linEnd, logStart, logEnd, value);
    }

    public void setupComponents(NumberFormatter startFormatter, NumberFormatter endFormatter) {
        this.setLayout(new GridBagLayout());

        this.startField = new JFormattedTextField(startFormatter);
        this.startField.setHorizontalAlignment(JTextField.RIGHT);
        this.startField.setColumns(TEXT_FILED_COLUMN);

        this.tildeLbl = new JLabel("~");

        this.endField = new JFormattedTextField(endFormatter);
        this.endField.setHorizontalAlignment(JTextField.RIGHT);
        this.endField.setColumns(TEXT_FILED_COLUMN);

        this.numOfSamplesLbl = new JLabel(", number of samples:");

        this.sampleNumSpinner = new JSpinner();
        SpinnerNumberModel model = (SpinnerNumberModel) this.sampleNumSpinner.getModel();
        model.setValue(1);
        model.setMinimum(1);
        model.setMaximum(sampleNumLimit);
        JFormattedTextField spinnerField = ((JSpinner.DefaultEditor) this.sampleNumSpinner.getEditor()).getTextField();
        spinnerField.setHorizontalAlignment(JTextField.RIGHT);
        spinnerField.setColumns(SPINNER_COLUMN);

        this.spaceModeBtn = new JButton(SPACE_MODES[this.modeIdx]);
        this.enumLbl = new JLabel();
        this.enumLbl.setVisible(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(startField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(tildeLbl, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(endField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(numOfSamplesLbl, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(sampleNumSpinner, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(spaceModeBtn, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 6;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = ENUM_LABEL_PADDING;
        this.add(enumLbl, gbc);

    }

    private void updateSamples() {

        if (!this.checkIsValid()) {
            this.samples = null;
            enumLbl.setVisible(false);
        } else {
            this.samples = this.generateSamples();
            enumLbl.setVisible(true);
            enumLbl.setText(makeSampleText(this.samples));
        }
    }

    private boolean checkIsValid() {
        return isFieldValueReady(startField) && isFieldValueReady(endField);
    }

    private static boolean isFieldValueReady(JFormattedTextField field) {
        return field.isEditValid() && field.getValue() != null;
    }

    private static String makeSampleText(LinkedList<? extends Number> samples) {
        String[] numStrings;
        int counter = 0;
        if (samples.size() > MAX_NUM_PRINT) {
            numStrings = new String[MAX_NUM_PRINT + 1];
            int halfNum = MAX_NUM_PRINT / 2;
            Iterator<? extends Number> iterator;

            for (iterator = samples.iterator(); iterator.hasNext() && counter < halfNum; counter++) {
                numStrings[counter] = prettyFormat(iterator.next());

            }

            numStrings[counter] = "...";
            counter = 0;
            for (iterator = samples.descendingIterator(); iterator.hasNext() && counter < halfNum; counter++) {
                numStrings[MAX_NUM_PRINT - counter] = prettyFormat(iterator.next());
            }
        } else {
            numStrings = new String[samples.size()];
            for (Number sample: samples) {
                numStrings[counter++] = prettyFormat(sample);
            }
        }

        return String.format("<html>%s</html>", String.join(", ", numStrings));
    }

    private static String prettyFormat(Number sample) {
        if (sample instanceof Integer) {
            return sample.toString();
        }
        return String.format("%.3g", sample);
    }



    private LinkedList<? extends Number> generateSamples() {
        Calculation calculation = this.isLinear()? LINEAR_CALCULATION: LOG_CALCULATION;
        return this.generator.generate(calculation, this.getStartValue(), this.getEndValue(), this.getSampleNum());
    }

    private boolean isLinear() {
        return modeIdx == 0;
    }

    private String getSpaceMode() {
        return SPACE_MODES[this.modeIdx];
    }

    private N getStartValue() {
        return (N) this.startField.getValue();
    }

    private N getEndValue() {
        return (N) this.endField.getValue();
    }

    private int getSampleNum() {
        return (Integer)sampleNumSpinner.getValue();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.startField.setEnabled(enabled);
        this.endField.setEnabled(enabled);
        this.sampleNumSpinner.setEnabled(enabled);
        this.spaceModeBtn.setEnabled(enabled);
        this.tildeLbl.setEnabled(enabled);
        this.numOfSamplesLbl.setEnabled(enabled);
        this.enumLbl.setEnabled(enabled);
    }

    @Override
    public boolean isValueReady() {
        return this.samples != null;
    }

    @Override
    public Object getStructuredValue() {
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("start", this.getStartValue());
        settings.put("stop", this.getEndValue());
        settings.put("num", this.getSampleNum());
        settings.put("mode", this.getSpaceMode());
        return settings;
    }

    private interface Calculation {
        public abstract double calculateSample(double startValue, double spacing, int i);
        public abstract double calculateEndValue(double endValue);
    }

    private interface SampleGenerator<N extends Number> {
        public abstract LinkedList<N> generate(Calculation calculation, N startValue, N endValue, int sampleNum);
    }


}
