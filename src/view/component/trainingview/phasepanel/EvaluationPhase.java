package view.component.trainingview.phasepanel;

import view.component.trainingview.phasepanel.basecomponent.*;

import javax.lang.model.element.Name;
import javax.swing.*;
import java.awt.*;

/**
 * Created by maeglin89273 on 9/7/15.
 */
public class EvaluationPhase extends PhasePanel {
    public EvaluationPhase() {
        super("Evaluation");

        this.setupComponents();
    }

    private void setupComponents() {

        SectionPanel trainSection = new SectionPanel("Train", OptionLabel.LabelType.RADIO);
        NameValueTable datasetTable = new NameValueTable();
        final OptionLabel trainingPartitionCkBox = datasetTable.addNameValue(OptionLabel.LabelType.CHECK_BOX, "Training Partition", TextWrap.postfixWrap(NumberField.getBoundedIntegerInstance(0, 100, 75), "%"));
        NameValueTable cvOptionTable = new NameValueTable();
        cvOptionTable.addNameValue(OptionLabel.LabelType.RADIO, "k-fold", TextWrap.prefixWrap("k=", new IntegerValueSpinner(3, 2, null)));
        cvOptionTable.addNameValue(OptionLabel.LabelType.RADIO, "leave-p-out", TextWrap.prefixWrap("p=", new IntegerValueSpinner(1, 1, null)));
        datasetTable.addNameValue("Cross Validation", cvOptionTable);

        trainSection.append(datasetTable);

        ValuedCheckBox testSetCkBox = new ValuedCheckBox("Evaluate test set");
        trainSection.append(testSetCkBox);
        JLabel note = trainSection.appendDescription("NOTE: you should not modify any model parameters after seeing the test score");
        note.setFont(note.getFont().deriveFont(Font.BOLD));
        this.append(trainSection);

        SectionPanel plotSection = new SectionPanel("Plot", OptionLabel.LabelType.RADIO);
        NameValueTable evalTable = new NameValueTable();
        evalTable.addNameValue(OptionLabel.LabelType.RADIO, "PCA", new OptionList(OptionList.OptionType.SINGLE, "2D", "3D"));
        plotSection.append(evalTable);
        this.append(plotSection);

        trainingPartitionCkBox.addItemListener(evt-> {;
            boolean selected = trainingPartitionCkBox.isSelected();
            if (!selected) {
                testSetCkBox.setSelected(false);
            }
            testSetCkBox.setEnabled(selected);
        });
    }

}
