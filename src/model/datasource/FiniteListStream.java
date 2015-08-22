package model.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by maeglin89273 on 8/21/15.
 */
public class FiniteListStream extends MutableFiniteStream {

    private List<Double> buffer;

    public FiniteListStream(List<Double> buffer) {
        setUnderlyingBuffer(buffer);
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

        int i = 0;
        if (this.buffer.size() < length) {
            if (this.buffer instanceof ArrayList) {
                ((ArrayList<Double>)this.buffer).ensureCapacity(length);
            }

            for (; i < buffer.size(); i++) {
                this.buffer.set(i, stream.get(i + start));
            }
            for (;i < length; i++) {
                this.buffer.add(stream.get(i + start));
            }
        } else {
            for (; i < length; i++) {
                this.buffer.set(i, stream.get(i + start));
            }
            this.buffer.subList(i, this.buffer.size()).clear();
        }

    }

    @Override
    public int intLength() {
        return buffer.size();
    }

    @Override
    public double[] toArray() {
        double[] target = new double[buffer.size()];
        int i = 0;
        for (double value: buffer) {
            target[i++] = value;
        }

        return target;
    }

    @Override
    public double get(long i) {
        return this.buffer.get((int) i);
    }

    public FiniteListStream setUnderlyingBuffer(List<Double> buffer) {
        this.buffer = buffer;
        return this;
    }
}
