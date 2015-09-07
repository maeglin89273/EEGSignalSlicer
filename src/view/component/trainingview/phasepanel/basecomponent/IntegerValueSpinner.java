package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;

/**
 * Created by maeglin89273 on 9/7/15.
 */
public class IntegerValueSpinner extends JSpinner implements HasStructuredValueComponent {

    private static final int SPINNER_COLUMN = 3;

    public IntegerValueSpinner(Integer min, Integer max) {
        if (min != null) {
            this.setMinimum(min);
        }
        if (max != null) {
            this.setMaximum(max);
        }
        this.setValue(RangeFieldPair.boundNumber(0, min, max));
        ((JSpinner.DefaultEditor)this.getEditor()).getTextField().setColumns(SPINNER_COLUMN);
    }

    public void setMaximum(int max) {
        ((SpinnerNumberModel)this.getModel()).setMaximum(max);
    }

    public void setMinimum(int min) {
        ((SpinnerNumberModel)this.getModel()).setMinimum(min);
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
