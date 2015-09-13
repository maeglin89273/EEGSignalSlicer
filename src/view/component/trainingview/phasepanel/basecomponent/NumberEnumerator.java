package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

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
        public double calculateSpacing(double start, double end, int sampleNum) {
            if (sampleNum <= 1) {
                return 0;
            }
            return (end - start) / (sampleNum - 1);
        }

        @Override
        public double calculateSample(double startValue, double spacing, int i) {
            return startValue + spacing * i;
        }
    };

    private static final Calculation LOG_CALCULATION = new Calculation() {

        @Override
        public double calculateSpacing(double start, double end, int sampleNum) {
            if (sampleNum <= 1) {
                return 1;
            }
            return Math.pow(end / start, 1.0 / (sampleNum - 1));
        }

        @Override
        public double calculateSample(double startValue, double spacing, int i) {
            return startValue * Math.pow(spacing, i);
        }
    };

    private static final SampleGenerator<Integer> INTEGER_GENERATOR = (calculation, startValue, endValue, sampleNum) -> {
        LinkedList<Integer> result = new LinkedList<>();

        if (sampleNum > 1) {

            double spacing = calculation.calculateSpacing(startValue, endValue, sampleNum);
            for (int i = 0; i < sampleNum - 1; i++) {
                result.add((int) calculation.calculateSample(startValue, spacing, i));
            }
            result.add(endValue);

        } else if (sampleNum == 1) {
            result.add(startValue);
        }

        return result;
    };

    private static final SampleGenerator<Double> FLOAT_GENERATOR = (calculation, startValue, endValue, sampleNum) -> {
        LinkedList<Double> result = new LinkedList<>();

        if (sampleNum > 1) {

            double spacing = calculation.calculateSpacing(startValue, endValue, sampleNum);
            for (int i = 0; i < sampleNum - 1; i++) {
                result.add(calculation.calculateSample(startValue, spacing, i));
            }
            result.add(endValue);
        } else if (sampleNum == 1) {
            result.add(startValue);
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
    private NumberFormatter startFormatter;
    private final NumberFormatter endFormatter;
    private JLabel tildeLbl;
    private JLabel numOfSamplesLbl;


    private NumberEnumerator(SampleGenerator<N> generator, NumberFormatter startFormatter, NumberFormatter endFormatter, N initValue) {
        this.generator = generator;
        this.startFormatter = startFormatter;
        this.endFormatter = endFormatter;

        this.setupComponents(this.startFormatter, this.endFormatter);
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
            spaceModeBtn.setText(SPACE_MODES[this.modeIdx]);
            updateSamples();
        });
    }

    public static NumberEnumerator<Double> getFloatInstance(Double value) {
        return getFloatInstance(null, value);
    }

    public static NumberEnumerator<Double> getFloatInstance(Double min, Double value) {
        NumberFormatter start = NumberField.makeFormatter(Double.class, min, null);
        NumberFormatter end = NumberField.makeFormatter(Double.class, min, null);
        return new NumberEnumerator<>(FLOAT_GENERATOR, start, end, value);
    }

    public static NumberEnumerator<Integer> getIntegerInstance(Integer value) {
        return getIntegerInstance(null, value);
    }

    public static NumberEnumerator<Integer> getIntegerInstance(Integer min, Integer value) {
        NumberFormatter start = NumberField.makeFormatter(Integer.class, min, null);
        NumberFormatter end = NumberField.makeFormatter(Integer.class, min, null);
        return new NumberEnumerator<>(INTEGER_GENERATOR, start, end, value);
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
//        String sampleString = sample.toString()
//        if (sampleString.length() > 8) {
//            double dValue = Math.abs(sample.doubleValue());
//            if (dValue < 1 || dValue >= 100000) {
//                return String.format("%.3g", sample);
//            }
//            return String.format("%.3f", sample);
//
//        }
//        return sampleString;
    }



    private LinkedList<? extends Number> generateSamples() {
        Calculation calculation = this.isLinear()? LINEAR_CALCULATION: LOG_CALCULATION;
        return this.generator.generate(calculation, this.getStartValue(), this.getEndValue(), this.getSampleNum());
    }

    private boolean isLinear() {
        return modeIdx == 0;
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
        return this.samples;
    }

    private interface Calculation {
        public abstract double calculateSpacing(double start, double end, int sampleNum);
        public abstract double calculateSample(double startValue, double spacing, int i);
    }

    private interface SampleGenerator<N extends Number> {
        public abstract LinkedList<N> generate(Calculation calculation, N startValue, N endValue, int sampleNum);
    }


}
