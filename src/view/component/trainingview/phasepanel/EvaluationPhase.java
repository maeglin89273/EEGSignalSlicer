package view.component.trainingview.phasepanel;

import view.component.trainingview.phasepanel.basecomponent.*;

/**
 * Created by maeglin89273 on 9/7/15.
 */
public class EvaluationPhase extends SectionPanel {
    public EvaluationPhase() {
        super("Evaluation");

        this.setupComponents();
    }

    private void setupComponents() {
        SectionPanel datasetSection = new SectionPanel("Dataset");
        NameValueTable datasetTable = new NameValueTable();
        datasetTable.addNameValue("Training Partition", UnitWrap.wrap(NumberField.getBoundedIntegerInstance(0, 100, 75), "%"));
        datasetTable.addNameValue(OptionLabel.LabelType.CHECK_BOX, "Cross Validation", UnitWrap.wrap(new IntegerValueSpinner(2, null), "folds"));
        datasetSection.append(datasetTable);
        this.append(datasetSection);
    }
}
