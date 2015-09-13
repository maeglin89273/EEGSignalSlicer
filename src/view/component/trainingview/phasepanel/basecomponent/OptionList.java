package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.*;

/**
 * Created by maeglin89273 on 9/6/15.
 */
public class OptionList extends CompoundStructuredValueComponent {
    public enum OptionType {
        MULTI, SINGLE
    }

    private ButtonGroup radioGroup;
    private CheckBoxGroup ckBoxGroup;

    private OptionType type;

    public OptionList(OptionType type, Collection<String> options) {
        this(type, (Iterable<String>)options);
    }

    public OptionList(OptionType type, String... options) {
        this(type, Arrays.asList(options));
    }

    public OptionList(OptionType type, Iterable<String> options) {
        ActionListener btnListener = evt-> {
          firePropertyChange("value", null, getStructuredValue());
        };
        this.type = type;
        if (type == OptionType.SINGLE) {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.radioGroup = new ButtonGroup();
            for (String option : options) {
                JRadioButton optionBtn = new JRadioButton(option);
                optionBtn.setActionCommand(option);
                radioGroup.add(optionBtn);
                optionBtn.addActionListener(btnListener);
                this.add(optionBtn);
            }
            this.selectFirstOption();
        } else {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.ckBoxGroup = new CheckBoxGroup();
            for (String option : options) {
                JCheckBox optionBtn = new JCheckBox(option);
                ckBoxGroup.add(optionBtn);
                optionBtn.addActionListener(btnListener);
                this.add(optionBtn);
                optionBtn.setSelected(true);
            }
        }

    }

    private void selectFirstOption() {
        radioGroup.setSelected(radioGroup.getElements().nextElement().getModel(), true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (type == OptionType.SINGLE) {
            Enumeration<AbstractButton> elements = radioGroup.getElements();
            for (; elements.hasMoreElements(); ) {
                elements.nextElement().setEnabled(enabled);
            }
        } else {
            ckBoxGroup.setEnabled(enabled);
        }
    }

    @Override
    public boolean isValueReady() {
        return true;
    }

    @Override
    public Object getStructuredValue() {
        if (type == OptionType.SINGLE) {
            return HasStructuredValueComponent.encodeToDictKey(radioGroup.getSelection().getActionCommand());
        } else {
            List<String> selectedOptions = new LinkedList<>();
            for (AbstractButton button: this.ckBoxGroup.getButtons()) {
                if (button.isSelected()) {
                    selectedOptions.add(HasStructuredValueComponent.encodeToDictKey(button.getText()));
                }
            }
            return selectedOptions;
        }
    }

    public static class CheckBoxGroup {
        private List<AbstractButton> buttons;
        private AbstractButton freezedButton;
        private int selectedCount = 0;
        private ItemListener listener;


        public CheckBoxGroup() {
            buttons = new LinkedList<>();
            listener = e-> {
                AbstractButton button = (AbstractButton) e.getSource();
                if (button.isSelected()) {
                    if (selectedCount == 1) {
                        unfreezeCkBox();
                    }
                    selectedCount++;
                } else {
                    selectedCount--;
                }

                if (selectedCount == 1) {
                    freezeLastCkBox();
                }

            };
        }

        public void add(AbstractButton button) {

            buttons.add(button);
            button.addItemListener(listener);
            if (button.isSelected()) {
                if (selectedCount == 1) {
                    this.unfreezeCkBox();
                }
                selectedCount++;
                if (selectedCount == 1) {
                    this.freezeLastCkBox();
                }
            }
        }

        public Collection<AbstractButton> getButtons() {
            return this.buttons;
        }

        private void unfreezeCkBox() {
            this.freezedButton.setEnabled(true);
            this.freezedButton = null;
        }

        private void freezeLastCkBox() {
            for (AbstractButton button : buttons) {

                if (button.isSelected()) {
                    button.setEnabled(false);
                    this.freezedButton = button;
                    break;
                }
            }
        }

        public void setEnabled(boolean enabled) {
            for (AbstractButton button : buttons) {
                button.setEnabled(enabled);
            }
            if (enabled && freezedButton != null) {
                freezedButton.setEnabled(false);
            }
        }
    }
}
