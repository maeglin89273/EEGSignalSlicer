package view.component.dataview;

import model.LearnerProxy;
import model.datasource.*;
import view.component.plot.InteractivePlotView;
import view.component.plot.PlottingUtils;
import view.component.plugin.CoordinatePlugin;
import view.component.plugin.NavigationPlugin;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
/**
 * Created by maeglin89273 on 12/1/15.
 */
public class PredictionPanel extends JPanel {

    private static final String PREDICTION_STREAM_TAG = "predict";
    private static int VIEWED_STREAM_SIZE = 300;
    private static int RESERVED_STREAM_SIZE = 10 * VIEWED_STREAM_SIZE;

    private Map<String, Integer> classEncoder;

    private JCheckBox autoScrollCkBox;
    private JCheckBox predictCkBox;
    private JLabel msgLabel;
    private InteractivePlotView predictionPlot;
    private final LearnerProxy learner;
    private InfiniteLengthDataSource predictionDataSource;
    private List<String> classes;
    private JButton clearBtn;

    public PredictionPanel(LearnerProxy learner) {
        this.learner = learner;
        this.learner.addTrainingCompleteCallback(new LearnerProxy.EvaluationCompleteCallback() {

            @Override
            public void evaluationDone(Map<String, Object> evaluationReport) {
                if (evaluationReport != null) {
                    setupEncoderAndPlot((List<String>) evaluationReport.get("Classes"));
                    uncheckedEnabled(true);
                }
            }

            @Override
            public void evaluationFail() {
                uncheckedEnabled(false);
            }
        });

        this.classEncoder = new HashMap<>();
        this.initPlot();
        this.initComponents();
        this.setupListener();

    }

    private void initPlot() {
        predictionPlot = new InteractivePlotView("Prediction Plot", VIEWED_STREAM_SIZE, 5, 450, 50);
        predictionPlot.setBaseline(PlottingUtils.Baseline.BOTTOM);
        NavigationPlugin nvgPlugin = new NavigationPlugin();
        LabeledCoordinatePlugin labalPlugin = new LabeledCoordinatePlugin();

        predictionPlot.addPlugin(nvgPlugin);
        predictionPlot.addPlugin(labalPlugin);

        nvgPlugin.setZoomingMode(NavigationPlugin.ZoomingMode.ZOOM_X);
        nvgPlugin.setMinimumZoomingWindowSize(VIEWED_STREAM_SIZE);
        nvgPlugin.setMaximumZoomingWindowSize(RESERVED_STREAM_SIZE);

        labalPlugin.setEnabled(true);
        resetStream();
        predictionPlot.setViewAllStreams(true);
    }

    private void initComponents() {
        this.setLayout(new GridBagLayout());
        predictCkBox = new JCheckBox("predict");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(predictCkBox, gbc);

        msgLabel = new JLabel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        this.add(msgLabel, gbc);

        autoScrollCkBox = new JCheckBox("auto scroll");
        autoScrollCkBox.setSelected(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        this.add(autoScrollCkBox, gbc);

        clearBtn = new JButton("clear");
        clearBtn.setSelected(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        this.add(clearBtn, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        gbc.weighty = 1;
        this.add(predictionPlot, gbc);


    }

    private void setupListener() {
        this.predictCkBox.addActionListener(e -> {
            autoScrollCkBox.setEnabled(this.predictCkBox.isSelected());
            msgLabel.setText("");
            if (isPredicting() && !learner.hasTrainedModel()) {
                goError();
            }
        });

        this.clearBtn.addActionListener(e -> {
            resetStream();
        });
    }

    private void resetStream() {
        Map<String, InfiniteLengthStream> sourceModel = new HashMap<>();
        sourceModel.put(PREDICTION_STREAM_TAG, new CyclicStream(RESERVED_STREAM_SIZE));
        this.predictionDataSource = new SimpleInfiniteDataSource(sourceModel);
        predictionPlot.setDataSource(this.predictionDataSource);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled && !learner.hasTrainedModel()) {
            enabled = false;
        }
        uncheckedEnabled(enabled);
    }

    private void uncheckedEnabled(boolean enabled) {
        predictCkBox.setEnabled(enabled);
        autoScrollCkBox.setEnabled(enabled ? predictCkBox.isSelected() : false);
        clearBtn.setEnabled(enabled);
//        predictionPlot.setEnabled(enabled ? predictCkBox.isSelected() : false);
    }

    public boolean isPredicting() {
        return predictCkBox.isEnabled() && predictCkBox.isSelected();
    }

    public void predict(FiniteLengthDataSource data) {
        String result = this.learner.predict(data);

        if (result == null) {
            goError();
        }

        this.predictionDataSource.updateValueTo(PREDICTION_STREAM_TAG, this.encodePrediction(result));
        if (autoScrollCkBox.isSelected()) {
            this.predictionPlot.setXTo(this.predictionDataSource.getCurrentLength());
        }
    }

    private int encodePrediction(String result) {
        return this.classEncoder.get(result);
    }

    private void setupEncoderAndPlot(List<String> classes) {
        this.classes = classes;
        classEncoder.clear();
        int i = 0;
        for(String element: classes) {
            classEncoder.put(element, ++i);
        }

        predictionPlot.setPeakValue(classes.size() + 1);
    }

    private void goError() {
        msgLabel.setText("ERROR: please re-train the model");
        predictCkBox.setSelected(false);
        this.uncheckedEnabled(false);
    }

    private class LabeledCoordinatePlugin extends CoordinatePlugin {


        public LabeledCoordinatePlugin() {
            super(new Color(255, 0 ,105));
        }

        @Override
        protected String positionText() {
            if (classes != null) {
                return super.positionText();
            }

            return "";
        }

        @Override
        protected String getXText(double posX) {
            return String.format("%.1f", posX);
        }

        @Override
        protected String getYText(double posY) {
            int index = Math.round((float)posY) - 1;
            if (index >= classes.size()) {
                index = classes.size() - 1;
            } else if (index < 0) {
                index = 0;
            }

            return classes.get(index);
        }
    }
}
