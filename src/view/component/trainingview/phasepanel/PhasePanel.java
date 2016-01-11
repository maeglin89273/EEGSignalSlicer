package view.component.trainingview.phasepanel;

import view.component.trainingview.phasepanel.basecomponent.OptionLabel;
import view.component.trainingview.phasepanel.basecomponent.SectionPanel;
import view.component.trainingview.phasepanel.basecomponent.SpacingStandard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by maeglin89273 on 9/11/15.
 */
public class PhasePanel extends SectionPanel {
    public PhasePanel(String caption) {
        super(caption);
    }

    @Override
    protected void initOutline(String caption, OptionLabel.LabelType captionType) {
        this.setLayout(new GridBagLayout());
        this.captionLabel = this.makeCaptionLabel(caption, captionType);

        contentPanel = new VerticalScrollPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(0, 5 * SpacingStandard.PADDING, 0, 0));
        scrollPane.setBackground(contentPanel.getBackground());

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
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(scrollPane, gbc);

    }

    private static class VerticalScrollPanel extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            Dimension preferredSize = (Dimension) this.getPreferredSize().clone();
            preferredSize.setSize(preferredSize.getWidth() + 3 * SpacingStandard.PADDING, preferredSize.getHeight());
            return preferredSize;
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 10;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 10;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
