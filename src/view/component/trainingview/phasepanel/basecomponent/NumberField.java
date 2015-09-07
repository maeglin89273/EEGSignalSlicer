package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;

/**
 * Created by maeglin89273 on 9/6/15.
 */
public class NumberField<N extends Number & Comparable<N>> extends JFormattedTextField implements HasStructuredValueComponent {


    private NumberField(NumberFormatter formatter, N value) {
        super(formatter);
        this.setHorizontalAlignment(RIGHT);
        this.setColumns(3);
        this.setValue(value);
    }

    public static NumberField<Integer> getIntegerInstance(boolean bePositive) {
        return getBoundedIntegerInstance(bePositive ? 0 : null, null);
    }

    public static NumberField<Double> getFloatInstance(boolean bePositive) {
        return getBoundedFloatInstance(bePositive ? 0.0 : null, null);
    }

    public static NumberField<Integer> getBoundedIntegerInstance(Integer min, Integer max) {
        return getBoundedIntegerInstance(min, max, min);
    }

    public static NumberField<Double> getBoundedFloatInstance(Double min, Double max) {
        return getBoundedFloatInstance(min, max, min);
    }

    public static NumberField<Integer> getBoundedIntegerInstance(Integer min, Integer max, Integer value) {
        return new NumberField<>(makeFormatter(Integer.class, min, max), value);
    }

    public static NumberField<Double> getBoundedFloatInstance(Double min, Double max, Double value) {
        return new NumberField<>(makeFormatter(Double.class, min, max), value);
    }

    static <N extends Number> NumberFormatter makeFormatter(Class<N> nClass, N min, N max) {
        NumberFormat format = nClass.equals(Double.class) || nClass.equals(Float.class)? NumberFormat.getNumberInstance(): NumberFormat.getIntegerInstance();
        format.setGroupingUsed(false);
        NumberFormatter numFormatter = new NumberFormatter(format);
        numFormatter.setValueClass(nClass);
        if (min != null) {
            numFormatter.setMinimum((Comparable) min);
        }

        if (max != null) {
            numFormatter.setMaximum((Comparable) max);
        }

        return numFormatter;
    }


    public void setNumberValue(N value) {
        super.setValue(value);
    }

    public void setMinimum(N min) {
        ((NumberFormatter) this.getFormatter()).setMinimum(min);
    }

    public void setMaximum(N max) {
        ((NumberFormatter) this.getFormatter()).setMaximum(max);
    }

    @Override
    public boolean isValueReady() {
        return this.isEditValid() && this.getValue() != null;
    }

    @Override
    public Object getStructuredValue() {
        return this.getValue();
    }
}
