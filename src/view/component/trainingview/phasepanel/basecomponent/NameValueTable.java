package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 9/4/15.
 */
public class NameValueTable extends BorderedCompoundStructuredValueComponent {
    private static Insets CELL_PADDING = SpacingStandard.AROUND_PADDING;
    private int rowIndex = 0;

    private JPanel tablePanel;
    private Map<OptionLabel, HasStructuredValueComponent> tableModel;
    private ButtonGroup optionGroup;

    public NameValueTable() {
        this.setupLayout();
        this.tableModel = new LinkedHashMap<>();
    }

    private void setupLayout() {
        this.setLayout(new GridBagLayout());

        this.tablePanel = new JPanel();
        this.tablePanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        this.add(this.tablePanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        this.add(new JPanel(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 1;
        this.add(new JPanel(), gbc);
    }

    public <C extends JComponent & HasStructuredValueComponent> void addNameValue(String name, C component) {
        this.addNameValue(OptionLabel.LabelType.LABEL, name, component);

    }

    public <C extends JComponent & HasStructuredValueComponent> void addNameValue(OptionLabel.LabelType type, String name, C component) {
        OptionLabel label = this.makeOptionLabelCell(type, name, component);
        this.makeValueCell(component);
        this.tableModel.put(label, component);
        rowIndex++;

    }

    private OptionLabel makeOptionLabelCell(OptionLabel.LabelType type, String name, final JComponent valueComponent) {
        OptionLabel label = OptionLabel.getInstance(type, name + ":");
        label.addChangeListener(evt-> {
            valueComponent.setEnabled(((OptionLabel)evt.getSource()).isSelected());
        });
        if (type == OptionLabel.LabelType.RADIO) {
            this.optionGroup.add((AbstractButton) label);
            if (this.optionGroup.getButtonCount() == 1) {
                label.setSelected(true);
            }
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = rowIndex;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.insets = CELL_PADDING;
        this.tablePanel.add((Component) label, gbc);
        return label;
    }

    private void makeValueCell(JComponent component) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = rowIndex;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.insets = CELL_PADDING;
        this.tablePanel.add(component, gbc);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (Map.Entry<OptionLabel, HasStructuredValueComponent> pair: tableModel.entrySet()) {
            pair.getKey().setEnabled(enabled);
            pair.getValue().setEnabled(enabled);
        }
    }

    @Override
    public boolean isValueReady() {
        for (HasStructuredValueComponent valueComponent: tableModel.values()) {
            if (!valueComponent.isValueReady()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object getStructuredValue() {
        return null;
    }
}
