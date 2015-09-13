package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import java.awt.*;

/**
 * Created by maeglin89273 on 9/6/15.
 */
public class PostfixWrap<C extends JComponent & HasStructuredValueComponent> extends CompoundStructuredValueComponent {

    private final C wrappedComponent;
    private final String postfix;
    private JLabel postfixLbl;

    public static <C extends JComponent & HasStructuredValueComponent> PostfixWrap wrap(C wrappedComponent, String unit) {
        return new PostfixWrap<>(wrappedComponent, unit);
    }

    private PostfixWrap(C wrappedComponent, String postfix) {
        this.wrappedComponent = wrappedComponent;
        this.postfix = postfix;
        this.setupLayout();
    }

    private void setupLayout() {
        this.setLayout(new GridBagLayout());
        this.postfixLbl = new JLabel(postfix);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(this.wrappedComponent, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(postfixLbl, gbc);
    }

    public JLabel getPostfixLabel() {
        return this.postfixLbl;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.postfixLbl.setEnabled(enabled);
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
