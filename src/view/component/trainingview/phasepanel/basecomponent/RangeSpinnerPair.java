package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by maeglin89273 on 9/8/15.
 */
public class RangeSpinnerPair extends CompoundStructuredValueComponent {

    private static final int TEXT_FILED_COLUMN = 3;
    private SpinnerNumberModel startModel;
    private SpinnerNumberModel endModel;
    private JSpinner startSpinner;
    private JSpinner endSpinner;


    public RangeSpinnerPair(Integer min, Integer max) {
        this.setupComponents(min, max);
        this.setupListeners();
    }

    private void setupComponents(Integer min, Integer max) {
        this.setLayout(new GridBagLayout());

        startSpinner = new JSpinner();
        JTextField field = IntegerValueSpinner.getSpinnerTextField(startSpinner);
        field.setHorizontalAlignment(JTextField.RIGHT);
        field.setColumns(TEXT_FILED_COLUMN);
        this.startModel = (SpinnerNumberModel) startSpinner.getModel();

        JLabel tildeLbl = new JLabel("~");

        endSpinner = new JSpinner();
        field = IntegerValueSpinner.getSpinnerTextField(endSpinner);
        field.setHorizontalAlignment(JTextField.RIGHT);
        field.setColumns(TEXT_FILED_COLUMN);
        this.endModel = (SpinnerNumberModel) endSpinner.getModel();

        this.initModels(min, max);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(startSpinner, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(tildeLbl, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(endSpinner, gbc);
    }

    private void initModels(Integer min, Integer max) {
        if (max != null) {
            this.startModel.setMaximum(max);
            this.endModel.setMaximum(max);
            this.endModel.setValue(max);
        } else {
            if (min != null) {
                this.endModel.setValue(min);
            }
        }
        if (min != null) {
            this.startModel.setMinimum(min);
            this.endModel.setMinimum(min);
            this.startModel.setValue(min);
        } else {
            if (max != null) {
                this.startModel.setValue(max);
            }
        }
    }

    private void setupListeners() {
        this.startModel.addChangeListener(evt -> {
            setEndMin((Integer) startModel.getValue());
            firePropertyChange("start_value", null, startModel.getValue());
        });

        this.endModel.addChangeListener(evt -> {
            setStartMax((Integer) endModel.getValue());
            firePropertyChange("end_value", null, endModel.getValue());
        });
    }


    @Override
    public boolean isValueReady() {
        return true;
    }

    private void setStartMax(Integer max) {

        if (max < (Integer) this.startModel.getValue()) {
            this.startModel.setValue(max);
        }

        this.startModel.setMaximum(max);
    }


    private void setEndMin(Integer min) {

        if (min > (Integer) this.endModel.getValue()) {
            this.endModel.setValue(min);
        }

        this.endModel.setMinimum(min);

    }


    public Integer getStartValue() {
        return (Integer) startModel.getValue();
    }


    public Integer getEndValue() {
        return (Integer) endModel.getValue();
    }

    public Integer getMinimum() {
        return (Integer) this.startModel.getMinimum();
    }

    public Integer getMaximum() {
        return (Integer) this.endModel.getMaximum();
    }

    public void setMinimum(Integer min) {
        if (min > this.getMaximum()) {
            return;
        }

        if (min > (Integer) this.startModel.getValue()) {
            this.startModel.setValue(min);
        }

        this.startModel.setMinimum(min);
    }

    public void setMaximum(Integer max) {
        if (max < this.getMinimum()) {
            return;
        }

        if (max < (Integer) this.endModel.getValue()) {
            this.endModel.setValue(max);
        }

        this.endModel.setMaximum(max);

    }

    @Override
    public void setEnabled(boolean enabled) {
        this.startSpinner.setEnabled(enabled);
        this.endSpinner.setEnabled(enabled);
    }

    @Override
    public Object getStructuredValue() {
        java.util.List<Integer> pair = new ArrayList<>(2);
        pair.add(getStartValue());
        pair.add((Integer) endModel.getValue());
        return pair;
    }
}
