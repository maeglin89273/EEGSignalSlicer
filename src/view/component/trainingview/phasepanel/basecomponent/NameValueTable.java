package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import javax.swing.text.html.parser.Entity;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 9/4/15.
 */
public class NameValueTable extends BorderedCompoundStructuredValueComponent implements NamedStructuredValueComponent {
    private static Insets CELL_PADDING = SpacingStandard.AROUND_PADDING;

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

    public <C extends JComponent & HasStructuredValueComponent> OptionLabel addNameValue(String name, C component) {
        return this.addNameValue(OptionLabel.LabelType.LABEL, name, component);
    }

    public <C extends JComponent & HasStructuredValueComponent> OptionLabel addNameValue(OptionLabel.LabelType type, String name, C component) {
        OptionLabel label = makeOptionLabel(type, name);
        this.tableModel.put(label, component);
        this.checkIsRadioBtn(label);

        this.makeOptionLabelCell(label, tableModel.size() - 1);
        this.makeValueCell(component, tableModel.size() - 1);


        return label;

    }

    private void checkIsRadioBtn(OptionLabel label) {
        if (label instanceof JRadioButton) {
            this.checkBtnGroupNotNull();
            this.optionGroup.add((AbstractButton) label);
            if (this.optionGroup.getButtonCount() == 1) {
                label.setSelected(true);
            } else {
                this.tableModel.get(label).setEnabled(false);
            }
        }
    }

    public <C extends JComponent & HasStructuredValueComponent> void replaceValue(OptionLabel nameLabel, C newComponent) {
        int i = 0;
        HasStructuredValueComponent valueComponent = null;
        for (Map.Entry<OptionLabel, HasStructuredValueComponent> pair: tableModel.entrySet()) {
            if (pair.getKey().equals(nameLabel)) {
                valueComponent = pair.getValue();
                break;
            }
            i++;
        }
        if (valueComponent != null) {
            this.tablePanel.remove((Component) valueComponent);
            this.tableModel.replace(nameLabel, newComponent);
            this.makeValueCell(newComponent, i);
        }

    }

    private OptionLabel makeOptionLabel(OptionLabel.LabelType type, String name) {
        OptionLabel label = OptionLabel.getInstance(type, name + ":");
        label.addItemListener(evt -> {
            tableModel.get(label).setEnabled(((OptionLabel) evt.getSource()).isSelected());
        });

        return label;
    }

    private void makeOptionLabelCell(OptionLabel label, int index) {

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = index;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.insets = CELL_PADDING;
        this.tablePanel.add((Component) label, gbc);

    }

    private void checkBtnGroupNotNull() {
        if (optionGroup == null) {
            optionGroup = new ButtonGroup();
        }
    }

    private void makeValueCell(JComponent component, int index) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = index;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.insets = CELL_PADDING;
        this.tablePanel.add(component, gbc);
    }

    @Override
    public void setEnabled(boolean enabled) {
        for (Map.Entry<OptionLabel, HasStructuredValueComponent> pair: tableModel.entrySet()) {
            OptionLabel key = pair.getKey();
            key.setEnabled(enabled);
            pair.getValue().setEnabled(enabled && key.isSelected());
        }
    }

    @Override
    public boolean isValueReady() {
        for (Map.Entry<OptionLabel, HasStructuredValueComponent> pair: tableModel.entrySet()) {
            if (pair.getKey().isSelected() && !pair.getValue().isValueReady()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Map<String, Object> getStructuredValue() {
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<OptionLabel, HasStructuredValueComponent> pair: this.tableModel.entrySet()) {
            OptionLabel key = pair.getKey();
            if (key.isSelected()) {
                values.put(HasStructuredValueComponent.encodeToDictKey(key.getText()), pair.getValue().getStructuredValue());
            }
        }

        return values;
    }
}
