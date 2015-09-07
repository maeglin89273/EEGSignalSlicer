package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import java.awt.*;

/**
 * Created by maeglin89273 on 9/6/15.
 */
public class UnitWrap<C extends JComponent & HasStructuredValueComponent> extends CompoundStructuredValueComponent {

    private final C wrappedComponent;
    private final String unit;

    public static <C extends JComponent & HasStructuredValueComponent> UnitWrap wrap(C wrappedComponent, String unit) {
        return new UnitWrap<>(wrappedComponent, unit);
    }

    private UnitWrap(C wrappedComponent, String unit) {
        this.wrappedComponent = wrappedComponent;
        this.unit = unit;
        this.setupLayout();
    }

    private void setupLayout() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(this.wrappedComponent, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(new JLabel(unit), gbc);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
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
