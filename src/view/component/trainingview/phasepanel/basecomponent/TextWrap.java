package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import java.awt.*;

/**
 * Created by maeglin89273 on 9/6/15.
 */
public class TextWrap<C extends JComponent & HasStructuredValueComponent> extends CompoundStructuredValueComponent {

    private final C wrappedComponent;
    private final String text;
    private JLabel textLbl;

    public static <C extends JComponent & HasStructuredValueComponent> TextWrap postfixWrap(C wrappedComponent, String text) {
        return new TextWrap<>(wrappedComponent, text, true);
    }

    public static <C extends JComponent & HasStructuredValueComponent> TextWrap prefixWrap(String text, C wrappedComponent) {
        return new TextWrap<>(wrappedComponent, text, false);
    }

    private TextWrap(C wrappedComponent, String text, boolean isPostfix) {
        this.wrappedComponent = wrappedComponent;
        this.text = text;
        this.setupLayout(isPostfix);
    }

    private void setupLayout(boolean isPostfix) {
        this.setLayout(new GridBagLayout());
        this.textLbl = new JLabel(text);

        GridBagConstraints frontGbc = new GridBagConstraints();
        frontGbc.gridx = 0;
        frontGbc.gridy = 0;
        frontGbc.anchor = GridBagConstraints.WEST;



        GridBagConstraints backGbc = new GridBagConstraints();
        backGbc.gridx = 1;
        backGbc.gridy = 0;
        backGbc.anchor = GridBagConstraints.WEST;
        this.add(textLbl, backGbc);

        JComponent frontComponent = wrappedComponent;
        JComponent backComponent = textLbl;
        if (!isPostfix) {
            frontComponent = textLbl;
            backComponent = wrappedComponent;
        }

        this.add(frontComponent, frontGbc);
        this.add(backComponent, backGbc);

    }

    public JLabel getPostfixLabel() {
        return this.textLbl;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.textLbl.setEnabled(enabled);
        this.wrappedComponent.setEnabled(enabled);
    }

    @Override
    public boolean isValueReady() {
        return wrappedComponent.isValueReady();
    }

    @Override
    public Object getStructuredValue() {
        return wrappedComponent.getStructuredValue();
    }
}
