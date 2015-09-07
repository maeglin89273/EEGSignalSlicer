package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Created by maeglin89273 on 9/7/15.
 */
public interface OptionLabel {
    public boolean isSelected();
    public void setSelected(boolean selected);
    public String getText();
    public void setText(String text);
    public void setAlignmentX(float alignmentX);
    public float getAlignmentX();
    public void setAlignmentY(float alignmentY);
    public float getAlignmentY();
    public Font getFont();
    public void setFont(Font font);
    public void addChangeListener(ChangeListener listener);
    public void setEnabled(boolean enabled);
    public boolean isEnabled();

    public enum LabelType {
        LABEL, RADIO, CHECK_BOX
    }

    public static OptionLabel getInstance(LabelType type, String text) {
        OptionLabel label = null;
        switch (type) {
            case LABEL:
                label = new OptionJLabel(text);
                break;
            case RADIO:
                label = new OptionJRadioButton(text);
                label.setSelected(false);
                break;
            case CHECK_BOX:
                label = new OptionJCheckBox(text);
                label.setSelected(true);


        }
        return label;
    }


    public static class OptionJLabel extends JLabel implements OptionLabel {

        public OptionJLabel(String text) {
            super(text);
        }

        @Override
        public boolean isSelected() {
            return true;
        }

        @Override
        public void setSelected(boolean selected) {
            //ignore the operation
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            //ignore the operation
        }

        @Override
        public void setEnabled(boolean enabled) {
            //ignore the operation
        }
    }

    public static class OptionJRadioButton extends JRadioButton implements OptionLabel {
        public OptionJRadioButton(String text) {
            super(text);

        }

    }

    public static class OptionJCheckBox extends JCheckBox implements OptionLabel {
        public OptionJCheckBox(String text) {
            super(text);
        }
    }

}
