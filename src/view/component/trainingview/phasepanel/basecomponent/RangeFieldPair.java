package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.util.*;

/**
 * Created by maeglin89273 on 9/6/15.
 */
public class RangeFieldPair<N extends Number & Comparable<N>> extends CompoundStructuredValueComponent {

    private static final int TEXT_FILED_COLUMN = 3;
    private final NumberFormatter startFormatter;
    private final NumberFormatter endFormatter;
    private JFormattedTextField startField;
    private JFormattedTextField endField;

    private RangeFieldPair(NumberFormatter startFormatter, NumberFormatter endFormatter) {
        this.startFormatter = startFormatter;
        this.endFormatter = endFormatter;
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
            setEndMin((N)startField.getValue());

        });

        this.endField.addPropertyChangeListener("value", evt-> {
            setStartMax((N) endField.getValue());

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

    private void setStartMax(Comparable<N> max) {
        if (max.compareTo((N) this.startField.getValue()) < 0) {
            this.startField.setValue(max);
        }

        this.startFormatter.setMaximum(max);

    }

    private N getStartMax() {
        return (N) this.startFormatter.getMaximum();
    }

    private void setEndMin(Comparable<N> min) {

        if (min.compareTo((N) this.endField.getValue()) > 0) {
            this.endField.setValue(min);
        }

        this.endFormatter.setMinimum(min);

    }

    private N getEndMin() {
        return (N) this.endFormatter.getMinimum();
    }

    public N getMinimum() {
        return (N) this.startFormatter.getMinimum();
    }

    public N getMaximum() {
        return (N) this.endFormatter.getMaximum();
    }

    public void setMinimum(N min) {
        if (min.compareTo(getMaximum()) > 0) {
            return;
        }

        if (min.compareTo((N) this.startField.getValue()) > 0) {
            this.startField.setValue(min);
        }

        this.startFormatter.setMinimum(min);

    }

    public void setMaximum(N max) {
        if (max.compareTo(getMinimum()) < 0) {
            return;
        }

        if (max.compareTo((N) this.endField.getValue()) < 0) {
            this.endField.setValue(max);
        }

        this.endFormatter.setMaximum(max);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.startField.setEnabled(enabled);
        this.endField.setEnabled(enabled);
    }

    @Override
    public Object getStructuredValue() {
        java.util.List<Number> pair = new ArrayList<>(2);
        pair.add((Number) startField.getValue());
        pair.add((Number) endField.getValue());
        return pair;
    }
}
