package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;

/**
 * Created by maeglin89273 on 9/6/15.
 */
public class RangeFieldPair<N extends Number & Comparable<N>> extends CompoundStructuredValueComponent {

    private static final int TEXT_FILED_COLUMN = 3;
    private JFormattedTextField startField;
    private JFormattedTextField endField;

    private RangeFieldPair(NumberFormatter startFormatter, NumberFormatter endFormatter) {
        this.setupComponents(startFormatter, endFormatter);
        this.setupListeners();
    }

    private void setupComponents(NumberFormatter startFormatter, NumberFormatter endFormatter) {
        this.setLayout(new GridBagLayout());

        this.startField = new JFormattedTextField(startFormatter);
        this.startField.setHorizontalAlignment(JTextField.RIGHT);
        this.startField.setColumns(TEXT_FILED_COLUMN);
        this.startField.setValue(startFormatter.getMinimum());
        JLabel tildeLbl = new JLabel("~");


        this.endField = new JFormattedTextField(endFormatter);
        this.endField.setHorizontalAlignment(JTextField.RIGHT);
        this.endField.setColumns(TEXT_FILED_COLUMN);
        this.endField.setValue(endFormatter.getMaximum());

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
    }

    private void setupListeners() {
        this.startField.addPropertyChangeListener("value", evt-> {
            if (isFieldValid(startField)) {
                setEndMin((N)startField.getValue());
            }
        });

        this.endField.addPropertyChangeListener("value", evt-> {
            if (isFieldValid(endField)) {
                setStartMax((N) endField.getValue());
            }
        });
    }


    public static RangeFieldPair<Integer> getIntegerInstance(Integer min, Integer max) {
        NumberFormatter start = NumberField.makeFormatter(Integer.class, min, max);
        NumberFormatter end = NumberField.makeFormatter(Integer.class, min, max);
        return new RangeFieldPair<>(start, end);
    }

    public static RangeFieldPair<Double> getDoubleInstance(Double min, Double max) {
        NumberFormatter start = NumberField.makeFormatter(Double.class, min, max);
        NumberFormatter end = NumberField.makeFormatter(Double.class, min, max);
        return new RangeFieldPair<>(start, end);
    }

    public static <N extends Number & Comparable<N>> N boundNumber(N value, N lower, N upper) {
        if (lower != null && value.compareTo(lower) < 0) {
            return lower;
        } else if (upper != null && value.compareTo(upper) > 0) {
            return upper;
        } else {
            return value;
        }
    }

    @Override
    public boolean isValueReady() {
        return this.isFieldValid(startField) && this.isFieldValid(endField);
    }

    private boolean isFieldValid(JFormattedTextField field) {
        return field.isEditValid() && field.getValue() != null;
    }

    private void setStartMax(Comparable max) {
        ((NumberFormatter)this.startField.getFormatter()).setMaximum(max);
    }

    private void setEndMin(Comparable min) {
        ((NumberFormatter)this.endField.getFormatter()).setMinimum(min);
    }


    public void setMinimum(N min) {
        ((NumberFormatter)this.startField.getFormatter()).setMinimum(min);
    }

    public void setMaximum(N max) {
        ((NumberFormatter)this.endField.getFormatter()).setMaximum(max);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.startField.setEnabled(enabled);
        this.endField.setEnabled(enabled);
    }

    @Override
    public Object getStructuredValue() {
        return null;
    }
}
