package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;

/**
 * Created by maeglin89273 on 9/7/15.
 */
public class IntegerValueSpinner extends JSpinner implements HasStructuredValueComponent {

    private static final int SPINNER_COLUMN = 3;

    public IntegerValueSpinner(Integer value, Integer min, Integer max) {
        if (min != null) {
            this.setMinimum(min);
        }
        if (max != null) {
            this.setMaximum(max);
        }
        this.setValue(RangeFieldPair.boundNumber(value == null? 0: value, min, max));
        JTextField spinnerField = getSpinnerTextField(this);
        spinnerField.setHorizontalAlignment(JTextField.RIGHT);
        spinnerField.setColumns(SPINNER_COLUMN);
    }

    public static JTextField getSpinnerTextField(JSpinner spinner) {
        return ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField();
    }

    public void setMaximum(int max) {
        if (max < this.getIntegerValue()) {
            this.setValue(max);
        }

        ((SpinnerNumberModel)this.getModel()).setMaximum(max);
    }

    public void setMinimum(int min) {
        if (min > this.getIntegerValue()) {
            this.setValue(min);
        }

        ((SpinnerNumberModel)this.getModel()).setMinimum(min);
    }

    public int getIntegerValue() {
        return (Integer) this.getValue();
    }

    @Override
    public boolean isValueReady() {
        return true;
    }

    @Override
    public Object getStructuredValue() {
        return this.getValue();
    }
}
