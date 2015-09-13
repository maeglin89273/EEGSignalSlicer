package view.component.trainingview;

import view.component.trainingview.phasepanel.basecomponent.HasStructuredValueComponent;
import view.component.trainingview.phasepanel.basecomponent.NameValueTable;
import view.component.trainingview.phasepanel.basecomponent.SectionPanel;
import view.component.trainingview.phasepanel.basecomponent.ValuedLabel;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class TrainingReportDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private SectionPanel reportPanel;
    private NameValueTable reportTable;

    private TrainingReportDialog(Map<String, Object> report) {
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Training Report");
        buttonOK.addActionListener(e -> onOK());

        this.fillReport(report);
        this.pack();
        this.setResizable(false);
    }

    private void fillReport(Map<String, Object> report) {
        if (reportTable != null) {
            this.reportPanel.removeAppendedComponent(reportTable);
        }
        this.reportTable = this.recursiveBuildTable(report);
        this.reportPanel.append(reportTable);
    }

    private static final TreeSet<Map.Entry<String, Object>> SORTER = new TreeSet<>((o1, o2) -> o1.getKey().compareTo(o2.getKey()));

    private NameValueTable recursiveBuildTable(Map<String, Object> model) {
        Queue<Map.Entry<NameValueTable, Map<String, Object>>> tableLayers = new LinkedList<>();
        NameValueTable rootTable = new NameValueTable();
        tableLayers.offer(new AbstractMap.SimpleEntry<>(rootTable, model));
        Map.Entry<NameValueTable, Map<String, Object>> tablePair;
        NameValueTable table;

        while (!tableLayers.isEmpty()) {
            tablePair = tableLayers.poll();
            table = tablePair.getKey();

            SORTER.addAll(tablePair.getValue().entrySet());
            for (Map.Entry<String, Object> nameValuePair : SORTER) {
                Object value = nameValuePair.getValue();
                if (value instanceof Map) {
                    NameValueTable innerTable = new NameValueTable();
                    tableLayers.offer(new AbstractMap.SimpleEntry<>(innerTable, (Map<String, Object>) value));
                    table.addNameValue(nameValuePair.getKey(), innerTable);
                } else if (value instanceof Double) {
                    table.addNameValue(nameValuePair.getKey(), new ValuedLabel(String.format("%.2g", value)));
                } else {
                    table.addNameValue(nameValuePair.getKey(), new ValuedLabel(value.toString()));
                }

            }
            SORTER.clear();
        }
        return rootTable;
    }


    private void onOK() {
        dispose();
    }

    public static void showReport(Map<String, Object> report) {
        TrainingReportDialog dialog = new TrainingReportDialog(report);
        dialog.setVisible(true);
    }


    private void createUIComponents() {
        this.reportPanel = new SectionPanel("Training Report");
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(panel1, gbc);
        buttonOK = new JButton();
        buttonOK.setText("OK");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(buttonOK, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(reportPanel, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */


}
