package model.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by maeglin89273 on 8/21/15.
 */
public class FiniteListStream extends MutableFiniteStream {

    private List<Double> buffer;
    private int validLength;

    public FiniteListStream(List<Double> buffer) {
        this.buffer = buffer;
        this.validLength = this.buffer.size();
    }

    public FiniteListStream(int length) {
        this(new ArrayList<>(Collections.nCopies(length, 0.0)));
    }

    @Override
    public void set(int i, double value) {
        this.buffer.set(i, value);
    }

    @Override
    public void replacedBy(Stream stream, int start, int length) {
        this.validLength = length > this.buffer.size()? this.buffer.size(): (int) length;
        for (int i = 0; i < this.validLength; i++) {
            this.set(i, stream.get(i + start));
        }
    }

    @Override
    public int intLength() {
        return buffer.size();
    }

    @Override
    public double[] toArray() {
        double[] target = new double[buffer.size()];
        for (int i = 0; i < target.length; i++) {
            target[i] = buffer.get(i);
        }

        return target;
    }

    @Override
    public double get(long i) {
        return this.buffer.get((int) i);
    }

    public FiniteListStream setUnderlyingBuffer(List<Double> buffer) {
        this.buffer = buffer;
        this.validLength = this.buffer.size();
        return this;
    }
}
