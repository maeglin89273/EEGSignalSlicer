package view.component.trainingview;

import model.DataFileUtils;
import model.LearnerProxy;
import model.datasource.FragmentDataSource;
import view.component.trainingview.phasepanel.ClassifierPhase;
import view.component.trainingview.phasepanel.EvaluationPhase;
import view.component.trainingview.phasepanel.FeatureExtractionPhase;
import view.component.trainingview.phasepanel.basecomponent.SectionPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class TrainingDialog extends JDialog {
    private final DatasetGetter datasetGetter;

    private JPanel contentPane;
    private JButton okBtn;
    private JList phaseList;
    private JPanel phasePlaceholder;
    private JButton evalBtn;
    private JButton saveProfileBtn;
    private JButton loadProfileBtn;

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
        getRootPane().setDefaultButton(okBtn);
        this.setTitle("Training Configuration");

        this.datasetGetter = getter;
        this.profileProxy = new TrainingProfile();
        this.learner = new LearnerProxy();
        this.learner.addTrainingCompleteCallback(new LearnerProxy.EvaluationCompleteCallback() {
            @Override
            public void evaluationDone(Map<String, Object> evaluationReport) {
                evalBtn.setEnabled(true);
                if (evaluationReport != null) {
                    TrainingReportDialog.showReport(evaluationReport);
                }
            }

            @Override
            public void evaluationFail() {
                evalBtn.setEnabled(true);
                JOptionPane.showConfirmDialog(TrainingDialog.this, "There are some errors when training, please check out any value that is inappropriate.", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        });

        phaseList.setSelectedIndex(1);
        this.PhaseSelected("Feature Extraction");

    }

    private void initPhases() {
        phasesTable = new LinkedHashMap<>();
        phasesTable.put("Evaluation", new EvaluationPhase());

        this.fePhase = new FeatureExtractionPhase();
        phasesTable.put("Feature Extraction", fePhase);
        phasesTable.put("Classifier", new ClassifierPhase());
    }

    private synchronized void evaluate(Map<String, Object> profile) {

        this.profile = profile;
        Collection<FragmentDataSource> dataset = datasetGetter.getDataset();
        if (dataset.isEmpty()) {
            JOptionPane.showConfirmDialog(TrainingDialog.this, "Please supply the training data.", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            return;
        }
        learner.prepareData(this.profile, dataset, (Collection<String>) this.profileProxy.structuredGet("feature_extraction", "streams"));

        learner.evaluate();
        evalBtn.setEnabled(false);

    }

    private Map<String, Object> buildProfileFromPanels() {
        Map<String, Object> fullConfigurations = new LinkedHashMap<>();
        for (SectionPanel phase : phasesTable.values()) {
            if (phase.isValueReady()) {
                fullConfigurations.putAll(phase.getStructuredValue());
            } else {
                JOptionPane.showConfirmDialog(this, phase.getCaptionLabel().getText() + " phase is not ready", "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }

        return fullConfigurations;
    }

    public FeatureExtractionPhase getFeatureExtractionPhase() {
        return this.fePhase;
    }

    private void setupListeners() {
        loadProfileBtn.addActionListener(e -> this.loadProfileToPanels());
        saveProfileBtn.addActionListener(e -> this.saveProfile());
        evalBtn.addActionListener(e -> this.evaluate(this.buildProfileFromPanels()));
        okBtn.addActionListener(e -> this.onOK());
        phaseList.addListSelectionListener(e -> this.PhaseSelected((String) phaseList.getSelectedValue()));
    }

    private void loadProfileToPanels() {
        DataFileUtils utils = DataFileUtils.getInstance();
        File profileFile = utils.loadFileDialog(this, "json");
        if (profileFile == null) {
            return;
        }

        Map<String, Object> profile = utils.loadJsonAsStructure(profileFile);

        //todo: should present configs on UI
        this.evaluate(profile);
    }

    private void saveProfile() {
        DataFileUtils utils = DataFileUtils.getInstance();
        String path = utils.saveStructureAsJson(this.buildProfileFromPanels(), "training_profiles", "profile_" + new Date().toString());
        DataFileUtils.getInstance().showSavedDialog(this, path);
    }

    private void PhaseSelected(String phaseName) {
        if (phasePlaceholder.getComponentCount() > 0) {
            phasePlaceholder.remove(0);
        }
        phasePlaceholder.add(phasesTable.get(phaseName));
        this.pack();
    }

    private void onOK() {
        this.profile = this.buildProfileFromPanels();
        this.setVisible(false);
    }

    public TrainingProfile getProfile() {
        if (this.profile == null) {
            this.profile = this.buildProfileFromPanels();
        }
        return this.profileProxy;
    }

    public LearnerProxy getLearner() {
        return this.learner;
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
        okBtn = new JButton();
        okBtn.setText("OK");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel1.add(okBtn, gbc);
        evalBtn = new JButton();
        evalBtn.setHorizontalAlignment(0);
        evalBtn.setHorizontalTextPosition(11);
        evalBtn.setText("Evaluate");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panel1.add(evalBtn, gbc);
        saveProfileBtn = new JButton();
        saveProfileBtn.setText("Save");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(saveProfileBtn, gbc);
        loadProfileBtn = new JButton();
        loadProfileBtn.setText("Load");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(loadProfileBtn, gbc);
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
