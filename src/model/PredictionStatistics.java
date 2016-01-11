package model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 9/24/15.
 */
public class PredictionStatistics {

    Map<String, Integer> staticsticsTable;
    private int total;

    public PredictionStatistics() {
        staticsticsTable = new HashMap<>();
    }


    public void add(String prediction) {
        staticsticsTable.put(prediction, staticsticsTable.getOrDefault(prediction, 0) + 1);
        this.total++;
    }

    public void printStatistics() {

        for (Map.Entry<String, Integer> pair: staticsticsTable.entrySet()) {
            System.out.print(pair.getKey() + String.format(": %.2f%% ", pair.getValue() * 100 / (float)this.total));
        }

        System.out.println();
    }

    public void reset() {
        staticsticsTable.clear();
        total = 0;
    }
}
