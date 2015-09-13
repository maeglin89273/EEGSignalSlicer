package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;

/**
 * Created by maeglin89273 on 9/10/15.
 */
public class ValuedLabel extends JLabel implements HasStructuredValueComponent {
    public ValuedLabel(String text) {
        super(text);
    }

    @Override
    public boolean isValueReady() {
        return true;
    }

    @Override
    public Object getStructuredValue() {
        return this.getText();
    }
}
