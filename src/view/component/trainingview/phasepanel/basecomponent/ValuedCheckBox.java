package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 9/7/15.
 */
public class ValuedCheckBox extends JCheckBox implements NamedStructuredValueComponent {

    public ValuedCheckBox(String text) {
        super(text);
    }



    @Override
    public boolean isValueReady() {
        return true;
    }

    @Override
    public Map<String, Object> getStructuredValue() {
        Map<String, Object> value = new HashMap<>();
        value.put(HasStructuredValueComponent.encodeToDictKey(this.getText()), this.isSelected());
        return value;
    }
}
