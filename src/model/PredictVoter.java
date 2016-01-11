package model;

import java.util.*;

/**
 * Created by maeglin89273 on 9/24/15.
 */
public class PredictVoter {
    public static final Comparator<Map.Entry<String, Integer>> ENTRY_COMPARATOR = (o1, o2) -> o1.getValue().compareTo(o2.getValue());
    private int voteNum;
    Map<String, Integer> voteTable;
    Queue<String> voteSequence;

    public PredictVoter(int voteNum) {
        this.voteTable = new HashMap<>();
        this.voteSequence = new LinkedList<>();
        reset(voteNum);
    }

    public String vote(String prediction) {
        voteTable.put(prediction, voteTable.getOrDefault(prediction, 0) + 1);
        voteSequence.offer(prediction);
        if (voteSequence.size() < voteNum) {
            return null;
        }

        if (voteSequence.size() > voteNum) {
            String toRemove = voteSequence.poll();
            voteTable.put(toRemove, voteTable.get(toRemove) - 1);
        }

        String voteResult = this.gatherVoteResult();

        return voteResult;
    }

    private String gatherVoteResult() {
        return Collections.max(this.voteTable.entrySet(), ENTRY_COMPARATOR).getKey();
    }

    public void reset(int voteNum) {
        this.voteNum = voteNum;
        this.reset();
    }


    public void reset() {
        voteTable.clear();
        voteSequence.clear();
    }
}
