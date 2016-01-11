package model;

import model.datasource.FiniteLengthDataSource;
import model.datasource.FragmentDataSource;
import net.razorvine.pyro.PyroProxy;
import oracle.PyOracle;

import java.io.IOException;
import java.util.*;

/**
 * Created by maeglin89273 on 8/25/15.
 */
public class LearnerProxy {

    private List<Map<String, double[]>> transferData;
    private List<String> transferTarget;
    private Collection<String> trainTags;
    private List<EvaluationCompleteCallback> callbacks;
    private PyroProxy oracle;
    private Map<String, Object> trainingSettings;
    private Thread worker;

    public LearnerProxy() {
        this.callbacks = new LinkedList<>();
        this.oracle = PyOracle.getInstance().getOracle("learning");
    }

    public void addTrainingCompleteCallback(EvaluationCompleteCallback callback) {
        this.callbacks.add(callback);
    }

    public void prepareData(Map<String, Object> trainingSettings, Collection<FragmentDataSource> data, Collection<String> trainTags) {
        this.trainingSettings = trainingSettings;
        this.trainTags = new LinkedHashSet<>(trainTags);
        this.transferData = new LinkedList<>();
        this.transferTarget = new LinkedList<>();

        for(FragmentDataSource source: data) {
            transferTarget.add(source.getFragmentTag());
            Map<String, double[]> sample = new HashMap<>();
            for (String tag: this.trainTags) {
                sample.put(tag, source.getFiniteDataOf(tag).toArray());
            }
            transferData.add(sample);
        }
    }

    public void evaluate() {
        this.worker = new Thread() {
            @Override
            public void run() {
                try {
                    Map<String, Object> report = (Map<String, Object>) oracle.call("evaluate", trainingSettings, transferData, transferTarget);
                    callbacks.forEach(callback -> callback.evaluationDone(report));
                } catch (Exception e) {
                    e.printStackTrace();
                    callbacks.forEach(callback -> callback.evaluationFail());

                }
            }
        };
        worker.setDaemon(true);
        worker.start();
    }

    public String predict(FiniteLengthDataSource data) {

        try {
            Map<String, double[]> unknownSignal = new HashMap<>();
            for (String tag: this.trainTags) {
                unknownSignal.put(tag, data.getFiniteDataOf(tag).toArray());
            }
            return (String) this.oracle.call("predict", unknownSignal);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean hasTrainedModel() {
        try {
            return (boolean) this.oracle.call("hasModel");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public interface EvaluationCompleteCallback {
        public void evaluationDone(Map<String, Object> evaluationReport);
        public void evaluationFail();
    }
}
