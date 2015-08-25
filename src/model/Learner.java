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
public class Learner extends Thread {

    private final Collection<FragmentDataSource> data;
    private final Collection<String> trainTags;
    private TrainingCompleteCallback callback;
    private PyroProxy oracle;

    public Learner(Collection<FragmentDataSource> data, Collection<String> trainTags, TrainingCompleteCallback callback) {
        this.data = data;
        this.trainTags = trainTags;
        this.callback = callback;
        this.setDaemon(true);
        this.oracle = PyOracle.getInstance().getOracle("learning");
    }

    @Override
    public void run() {
        List<Map<String, double[]>> transferData = new LinkedList<>();
        List<String> transferTarget = new LinkedList<>();

        for(FragmentDataSource source: data) {
            transferTarget.add(source.getFragmentTag());
            Map<String, double[]> sample = new HashMap<>();
            for (String tag: this.trainTags) {
                sample.put(tag, source.getFiniteDataOf(tag).toArray());
            }
            transferData.add(sample);
        }


        try {
            this.oracle.call("train", transferData, transferTarget);
        } catch (IOException e) {
            e.printStackTrace();
            this.callback.trainFail();
        }

        this.callback.trainDone();
    }

    public String predict(FiniteLengthDataSource data) {

        Map<String, double[]> unknownSignal = new HashMap<>();
        for (String tag: this.trainTags) {
            unknownSignal.put(tag, data.getFiniteDataOf(tag).toArray());
        }

        try {
            return (String) this.oracle.call("predict", unknownSignal);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public interface TrainingCompleteCallback {
        public void trainDone();
        public void trainFail();
    }
}
