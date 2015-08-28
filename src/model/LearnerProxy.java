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
    private TrainingCompleteCallback callback;
    private PyroProxy oracle;

    public LearnerProxy(TrainingCompleteCallback callback) {
        this.callback = callback;
        this.oracle = PyOracle.getInstance().getOracle("learning");
    }

    public void prepareData(Collection<FragmentDataSource> data, Collection<String> trainTags) {
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

    public void train() {
        Thread worker = new Thread() {
            @Override
            public void run() {
                try {
                    double score = (Double) oracle.call("train", transferData, transferTarget);
                    callback.trainDone(score);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.trainFail();
                }
            }
        };
        worker.setDaemon(true);
        worker.start();
    }

    public String predict(FiniteLengthDataSource data) {

        Map<String, double[]> unknownSignal = new HashMap<>();
        for (String tag: this.trainTags) {
            unknownSignal.put(tag, data.getFiniteDataOf(tag).toArray());
        }

        try {
            return (String) this.oracle.call("predict", unknownSignal);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void pcaPlot2D() {
        try {
            this.oracle.call_oneway("pca_plot2d", this.transferData, this.transferTarget);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pcaPlot3D() {
        try {
            this.oracle.call_oneway("pca_plot3d", this.transferData, this.transferTarget);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface TrainingCompleteCallback {
        public void trainDone(double score);
        public void trainFail();
    }
}
