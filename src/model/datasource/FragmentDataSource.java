package model.datasource;

import java.util.Collection;

/**
 * Created by maeglin89273 on 8/19/15.
 */
public class FragmentDataSource extends FiniteLengthDataSource implements ViewDataSource {
    private String tag;
    private final long startingPos;

    private final int idealLength;
    private final StreamingDataSource source;

    public FragmentDataSource(String tag, long startingPos, int length, StreamingDataSource source) {
        this.tag = tag;
        this.startingPos = startingPos;
        this.source = source;
        this.idealLength = length;
        this.source.addPresentedDataChangedListener(this);
    }

    @Override
    public FiniteLengthStream getFiniteDataOf(String tag) {
        return new FragmentStream(this.source.getDataOf(tag));
    }

    @Override
    public int intLength() {
        return properLength(this.source.getCurrentLength());
    }

    @Override
    public Collection<String> getTags() {
        return this.source.getTags();
    }

    public void setFrangmentTag(String tag) {
        this.tag = tag;
    }

    public String getFragmentTag() {
        return this.tag;
    }

    public long getStartingPosition() {
        return this.startingPos;
    }

    private int properLength(long sourceLength) {
        if (this.startingPos + this.idealLength - 1 < sourceLength) {
            return this.idealLength;
        } else {
            return (int) (sourceLength - this.startingPos);
        }
    }

    @Override
    public void onDataChanged(StreamingDataSource source) {
        this.firePresentedDataChanged();
    }

    @Override
    public void onDataChanged(StreamingDataSource source, String tag) {
        this.firePresentedDataChanged(tag);
    }

    @Override
    public void stopViewingSource() {
        this.source.removePresentedDataChangedListener(this);
    }

    private class FragmentStream extends FiniteLengthStream {
        private final Stream source;


        public FragmentStream(Stream source) {
            this.source = source;
        }

        @Override
        public int intLength() {
           return properLength(source.getCurrentLength());
        }

        @Override
        public double[] toArray() {
            double[] buffer = new double[this.intLength()];
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = source.get(sourceIndex(i));
            }
            return buffer;
        }

        @Override
        public double get(long i) {
            return source.get(sourceIndex(i));
        }

        private long sourceIndex(long i) {
            return i + startingPos;
        }
    }
}
