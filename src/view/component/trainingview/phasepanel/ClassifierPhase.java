package view.component.trainingview.phasepanel;

import view.component.trainingview.phasepanel.basecomponent.*;

/**
 * Created by maeglin89273 on 9/7/15.
 */
public class ClassifierPhase extends PhasePanel {


    private static final java.lang.Double SMALLEST_NUM = 10e-20;

    public ClassifierPhase() {
        super("Classifier");
        this.setupComponents();
    }

    private void setupComponents() {
        this.appendDescription("We apply Grid Search for optimizing the best parameters of the classifier.");
        
        SectionPanel svmSection = new SectionPanel("SVM", OptionLabel.LabelType.RADIO);

        OptionList.CheckBoxGroup group = new OptionList.CheckBoxGroup();
        SectionPanel linearSection = new SectionPanel("Linear", OptionLabel.LabelType.CHECK_BOX);
        SectionPanel rbfSection = new SectionPanel("RBF", OptionLabel.LabelType.CHECK_BOX);
        SectionPanel polySection = new SectionPanel("Poly", OptionLabel.LabelType.CHECK_BOX);
        group.add(linearSection.getCaptionLabel());
        group.add(rbfSection.getCaptionLabel());
        group.add(polySection.getCaptionLabel());

        NameValueTable parameterTable = new NameValueTable();
        NumberEnumerator<Double> linearC = NumberEnumerator.getFloatInstance(SMALLEST_NUM, null, 1.0);
        parameterTable.addNameValue("C", linearC);
        linearSection.append(parameterTable);

        parameterTable = new NameValueTable();
        NumberEnumerator<Double> rbfC = NumberEnumerator.getFloatInstance(SMALLEST_NUM, null, 1.0);
        NumberEnumerator<Double> rbfGamma = NumberEnumerator.getFloatInstance(SMALLEST_NUM, null, 0.001);
        parameterTable.addNameValue("C", rbfC);
        parameterTable.addNameValue("gamma", rbfGamma);
        rbfSection.append(parameterTable);

        parameterTable = new NameValueTable();
        NumberEnumerator<Double> polyC = NumberEnumerator.getFloatInstance(SMALLEST_NUM, null, 1.0);
        NumberEnumerator<Double> polyGamma = NumberEnumerator.getFloatInstance(SMALLEST_NUM, null, 0.001);
        NumberEnumerator<Integer> polyDegree = NumberEnumerator.getIntegerInstance(1, 0, 3);
        NumberEnumerator<Double> polyCoef0 = NumberEnumerator.getFloatInstance(0.0, null, 0.0);
        parameterTable.addNameValue("C", polyC);
        parameterTable.addNameValue("gamma", polyGamma);
        parameterTable.addNameValue("degree", polyDegree);
        parameterTable.addNameValue("coef0", polyCoef0);
        polySection.append(parameterTable);

        svmSection.append(linearSection);
        svmSection.append(rbfSection);
        svmSection.append(polySection);

        this.append(svmSection);
    }
}
