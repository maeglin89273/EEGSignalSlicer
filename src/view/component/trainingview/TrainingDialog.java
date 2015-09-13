package view.component.trainingview;

import model.LearnerProxy;
import model.datasource.FragmentDataSource;
import view.component.BusyDialog;
import view.component.trainingview.phasepanel.ClassifierPhase;
import view.component.trainingview.phasepanel.EvaluationPhase;
import view.component.trainingview.phasepanel.FeatureExtractionPhase;
import view.component.trainingview.phasepanel.basecomponent.SectionPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class TrainingDialog extends JDialog {
    private final DatasetGetter datasetGetter;
    private final BusyDialog trainingDialog;
    private JPanel contentPane;
    private JButton buttonOK;
    private JList phaseList;
    private JPanel phasePlaceholder;

    private Map<String, Object> profile;

    private Map<String, SectionPanel> phasesTable;
    private FeatureExtractionPhase fePhase;

    private TrainingProfile profileProxy;
    private LearnerProxy learner;

    public TrainingDialog(DatasetGetter getter) {
        this.initPhases();
        $$$setupUI$$$();
        setupListeners();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        this.setTitle("Training Configuration");
        this.trainingDialog = new BusyDialog("Evaluating...");
        this.datasetGetter = getter;
        this.profileProxy = new TrainingProfile();
        this.learner = new LearnerProxy(new LearnerProxy.TrainingCompleteCallback() {
            @Override
            public void trainDone(Map<String, Object> trainingReport) {
                trainingDialog.setVisible(false);
                if (trainingReport != null) {
                    TrainingReportDialog.showReport(trainingReport);
                }
            }

            @Override
            public void trainFail() {
                trainingDialog.setVisible(false);
                JOptionPane.showConfirmDialog(TrainingDialog.this, "There are some errors when training", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        });

        phaseList.setSelectedIndex(1);
        this.PhaseSelected("Feature Extraction");

    }

    public void setCVMaxFold(int folds) {

    }

    private void initPhases() {
        phasesTable = new LinkedHashMap<>();
        phasesTable.put("Evaluation", new EvaluationPhase(evt -> {
            evaluate();
        }));

        this.fePhase = new FeatureExtractionPhase();
        phasesTable.put("Feature Extraction", fePhase);
        phasesTable.put("Classifier", new ClassifierPhase());
    }

    private void evaluate() {
        this.profile = this.buildProfile();

        learner.prepareData(this.profile, datasetGetter.getDataset(), (Collection<String>) this.profileProxy.structuredGet("feature_extraction", "streams"));
        learner.evaluate();
        this.trainingDialog.setVisible(true);
    }

    private Map<String, Object> buildProfile() {
        Map<String, Object> fullParameters = new LinkedHashMap<>();
        for (SectionPanel phase : phasesTable.values()) {
            if (phase.isValueReady()) {
                fullParameters.putAll(phase.getStructuredValue());
            } else {
                JOptionPane.showConfirmDialog(this, phase.getCaptionLabel().getText() + " phase is not ready", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }

        return fullParameters;
    }

    public FeatureExtractionPhase getFeatureExtractionPhase() {
        return this.fePhase;
    }

    private void setupListeners() {
        buttonOK.addActionListener(e -> this.onOK());
        phaseList.addListSelectionListener(e -> this.PhaseSelected((String) phaseList.getSelectedValue()));
    }

    private void PhaseSelected(String phaseName) {
        if (phasePlaceholder.getComponentCount() > 0) {
            phasePlaceholder.remove(0);
        }
        phasePlaceholder.add(phasesTable.get(phaseName));
        this.pack();
    }

    private void onOK() {
        this.profile = this.buildProfile();
        this.setVisible(false);

    }

    public TrainingProfile getProfile() {
        if (this.profile == null) {
            this.profile = this.buildProfile();
        }
        return this.profileProxy;
    }

    public static void main(String... args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        TrainingDialog dialog = new TrainingDialog(null);

        dialog.setVisible(true);
        dialog.dispose();

    }

    private void createUIComponents() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String pahseText : phasesTable.keySet()) {
            listModel.addElement(pahseText);
        }

        phaseList = new JList(listModel);
        phaseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


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
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(buttonOK, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(panel2, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel2.add(phaseList, gbc);
        phasePlaceholder = new JPanel();
        phasePlaceholder.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(phasePlaceholder, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    public class TrainingProfile {
        public Object structuredGet(String... propertySeries) {
            Map<String, Object> structure = profile;
            int i = 0;
            for (; i < propertySeries.length - 1; i++) {
                structure = (Map<String, Object>) structure.get(propertySeries[i]);
            }
            return structure.get(propertySeries[i]);
        }

        public Map<String, Object> getUnderlyingMap() {
            return profile;
        }
    }

    public interface DatasetGetter {
        public Collection<FragmentDataSource> getDataset();
    }
}
