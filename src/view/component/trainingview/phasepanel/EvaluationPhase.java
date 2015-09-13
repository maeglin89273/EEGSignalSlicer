package view.component.trainingview.phasepanel;

import view.component.trainingview.phasepanel.basecomponent.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Created by maeglin89273 on 9/7/15.
 */
public class EvaluationPhase extends PhasePanel {
    public EvaluationPhase(ActionListener evaluator) {
        super("Evaluation");

        this.setupComponents(evaluator);
    }

    private void setupComponents(ActionListener evaluator) {

        SectionPanel trainSection = new SectionPanel("Train", OptionLabel.LabelType.RADIO);
        NameValueTable datasetTable = new NameValueTable();
        datasetTable.addNameValue("Training Partition", PostfixWrap.wrap(NumberField.getBoundedIntegerInstance(0, 100, 75), "%"));
        datasetTable.addNameValue("Cross Validation", PostfixWrap.wrap(new IntegerValueSpinner(3, 2, null), "folds"));

        trainSection.append(datasetTable);


        trainSection.append(new ValuedCheckBox("Evaluate test set"));
        JLabel note = trainSection.appendDescription("NOTE: you should not modify any model parameters after seeing the test score");
        note.setFont(note.getFont().deriveFont(Font.BOLD));
        this.append(trainSection);

        SectionPanel plotSection = new SectionPanel("Plot", OptionLabel.LabelType.RADIO);
        NameValueTable evalTable = new NameValueTable();
        evalTable.addNameValue(OptionLabel.LabelType.RADIO, "PCA", new OptionList(OptionList.OptionType.SINGLE, "2D", "3D"));
        plotSection.append(evalTable);
        this.append(plotSection);


        JButton evalBtn = new JButton("Evaluate");
        evalBtn.addActionListener(evaluator);
        this.append(evalBtn);

    }

}
