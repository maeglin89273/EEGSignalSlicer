package model.datasource;

/**
 * Created by maeglin89273 on 8/21/15.
 */
public class ReconstructedFragmentDataSource extends FragmentDataSource {
    private final long pastStartingPos;

    public ReconstructedFragmentDataSource(String tag, long timestamp, FiniteLengthDataSource source) {
        super(tag, 0, source.intLength(), source);
        this.pastStartingPos = timestamp;
    }

    @Override
    public long getStartingPosition() {
        return this.pastStartingPos;
    }
}
