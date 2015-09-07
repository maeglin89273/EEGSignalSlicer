package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

/**
 * Created by maeglin89273 on 9/6/15.
 */
public class OptionList extends CompoundStructuredValueComponent {


    private final ButtonGroup btnGroup;

    public OptionList(String... options) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.btnGroup = new ButtonGroup();

        for (String option: options) {
            JRadioButton optionBtn = new JRadioButton(option);
            optionBtn.setActionCommand(option);
            btnGroup.add(optionBtn);
            this.add(optionBtn);
        }
        this.selectFirstOption();
    }

    private void selectFirstOption() {
        btnGroup.setSelected(btnGroup.getElements().nextElement().getModel(), true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Enumeration<AbstractButton> elements = btnGroup.getElements();
        for (;elements.hasMoreElements();) {
            elements.nextElement().setEnabled(enabled);
        }
    }

    @Override
    public boolean isValueReady() {
        return true;
    }

    @Override
    public Object getStructuredValue() {
        return btnGroup.getSelection().getActionCommand();
    }
}
