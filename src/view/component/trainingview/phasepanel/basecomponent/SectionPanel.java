package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by maeglin89273 on 9/4/15.
 */
public class SectionPanel extends BorderedCompoundStructuredValueComponent implements NamedStructuredValueComponent {


    protected Insets CAPTION_PADDING = SpacingStandard.AROUND_PADDING;
    protected Insets CONTENT_INDENT = new Insets(0, 5 * SpacingStandard.PADDING, 0, 0);

    protected OptionLabel captionLabel;
    protected JPanel contentPanel;
    protected List<NamedStructuredValueComponent> valueComponents;

    private ButtonGroup radioCaptionGroup;

    public SectionPanel(String caption) {
        this(caption, OptionLabel.LabelType.LABEL);
    }

    public SectionPanel(String caption, OptionLabel.LabelType captionType) {
        this.initOutline(caption, captionType);
        this.valueComponents = new LinkedList<>();
    }

    protected void initOutline(String caption, OptionLabel.LabelType captionType) {
        this.setLayout(new GridBagLayout());
        this.captionLabel = this.makeCaptionLabel(caption, captionType);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = CAPTION_PADDING ;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add((Component) this.captionLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(new JSeparator(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = CONTENT_INDENT;
        this.add(contentPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        this.add(new JPanel(), gbc);

    }

    protected OptionLabel makeCaptionLabel(String caption, OptionLabel.LabelType labelType) {
        OptionLabel label = OptionLabel.getInstance(labelType, caption);

        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.addItemListener(e -> setContentEnabled(captionLabel.isSelected()));

        return label;
    }

    public JLabel appendDescription(String description) {
        JLabel descLbl = new JLabel(String.format("<html>%s</html>", description));

        this.append(descLbl);
        return descLbl;
    }

    public void append(JComponent component) {
        if (component instanceof NamedStructuredValueComponent) {
            this.valueComponents.add((NamedStructuredValueComponent) component);

            if (component instanceof SectionPanel) {
                OptionLabel label = ((SectionPanel)component).getCaptionLabel();

                if (label instanceof JRadioButton) {
                    this.checkBRadioGroup();
                    this.radioCaptionGroup.add((AbstractButton) label);
                    if (this.radioCaptionGroup.getButtonCount() == 1) {
                        label.setSelected(true);
                    } else {
                        ((SectionPanel)component).setContentEnabled(false);
                    }
                }
            }
        }


        component.setAlignmentX(LEFT_ALIGNMENT);
        component.setEnabled(this.captionLabel.isSelected());
        this.contentPanel.add(component);
    }

    public void removeAppendedComponent(JComponent component) {
        if (component instanceof NamedStructuredValueComponent) {
            this.valueComponents.remove(component);

            if (component instanceof SectionPanel) {
                OptionLabel label = ((SectionPanel)component).getCaptionLabel();

                if (label instanceof JRadioButton) {
                    this.checkBRadioGroup();
                    this.radioCaptionGroup.remove((AbstractButton) label);
                    if (this.radioCaptionGroup.getSelection() == null) {
                        Enumeration<AbstractButton> elements = this.radioCaptionGroup.getElements();
                        if (elements.hasMoreElements()) {
                            elements.nextElement().setSelected(true);
                        }
                    }
                }
            }
        }

        this.contentPanel.remove(component);
    }

    private void checkBRadioGroup() {
        if (this.radioCaptionGroup == null) {
            this.radioCaptionGroup = new ButtonGroup();
        }
    }


    private void setContentEnabled(boolean enabled) {
        for (Component component: contentPanel.getComponents()) {
            component.setEnabled(enabled);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.captionLabel.setEnabled(enabled);
        this.setContentEnabled(enabled && this.captionLabel.isSelected());
    }

    public <C extends JComponent & OptionLabel> C getCaptionLabel() {
        return (C) this.captionLabel;
    }

    @Override
    public boolean isValueReady() {
        if (!this.captionLabel.isSelected()) {
            return true;
        }
        for (NamedStructuredValueComponent component : valueComponents) {
            if (!component.isValueReady()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Map<String, Object> getStructuredValue() {
        Map<String, Object> value = new LinkedHashMap<>();
        if (this.captionLabel.isSelected()) {
            Map<String, Object> innerValue = new LinkedHashMap<>();
            for (NamedStructuredValueComponent component : valueComponents) {
                innerValue.putAll(component.getStructuredValue());
            }

            value.put(HasStructuredValueComponent.encodeToDictKey(this.captionLabel.getText()), innerValue);
        }
        return value;
    }
}
