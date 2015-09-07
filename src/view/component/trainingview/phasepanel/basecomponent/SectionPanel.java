package view.component.trainingview.phasepanel.basecomponent;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by maeglin89273 on 9/4/15.
 */
public class SectionPanel extends BorderedCompoundStructuredValueComponent {


    protected Insets CAPTION_PADDING = SpacingStandard.AROUND_PADDING;
    protected Insets CONTENT_INDENT = new Insets(0, 5 * SpacingStandard.PADDING, 0, 0);

    protected OptionLabel captionLabel;
    protected Box contentBox;
    protected List<HasStructuredValueComponent> valueComponents;

    public SectionPanel(String caption) {
        this(caption, OptionLabel.LabelType.LABEL);
    }

    public SectionPanel(String caption, OptionLabel.LabelType captionType) {
        this.initOutline(caption, captionType);
        this.valueComponents = new LinkedList<>();
    }

    private void initOutline(String caption, OptionLabel.LabelType captionType) {
        this.setLayout(new GridBagLayout());
        this.captionLabel = this.makeCaptionLabel(caption, captionType);

        contentBox = Box.createVerticalBox();

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
        this.add(contentBox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        this.add(new JPanel(), gbc);

    }

    private OptionLabel makeCaptionLabel(String caption, OptionLabel.LabelType labelType) {
        OptionLabel label = OptionLabel.getInstance(labelType, caption);

        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setAlignmentY(CENTER_ALIGNMENT);
        label.addChangeListener(evt->{
            setContentEnabled(captionLabel.isSelected());
        });

        return label;
    }

    public JLabel appendDescription(String description) {
        JLabel descLbl = new JLabel(String.format("<html>%s</html>", description));

        this.append(descLbl);
        return descLbl;
    }


    public void append(JComponent component) {
        if (component instanceof HasStructuredValueComponent) {
            this.valueComponents.add((HasStructuredValueComponent) component);
        }
        component.setAlignmentX(LEFT_ALIGNMENT);
        component.setEnabled(this.captionLabel.isSelected());
        this.contentBox.add(component);
    }

    private void setContentEnabled(boolean enabled) {
        for (Component component: contentBox.getComponents()) {
            component.setEnabled(enabled);
        }
    }


    public <C extends JComponent> C getCaptionLabel() {
        return (C) this.captionLabel;
    }

    @Override
    public boolean isValueReady() {
        return false;
    }

    @Override
    public Object getStructuredValue() {
        return null;
    }
}
