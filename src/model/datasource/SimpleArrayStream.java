package model.datasource;

import java.util.Iterator;

/**
 * Created by maeglin89273 on 7/26/15.
 */
public class SimpleArrayStream extends MutableFiniteLengthStream {
    private final double[] delegate;

    public SimpleArrayStream(int length) {
        this(new double[length]);
    }

    public SimpleArrayStream(double[] array) {
        this.delegate = array;
    }

    @Override
    public double get(long i) {
        return this.delegate[((int) i)];
    }

    @Override
    public long getCurrentLength() {
        return this.delegate.length;
    }

    @Override
    public void set(int i, double value) {
        this.delegate[i] = value;
    }

    @Override
    public void replacedBy(Stream stream, int start, int length) {
        if (stream instanceof FiniteLengthStream) {
            FiniteLengthStream arrayStream = (FiniteLengthStream) stream;
            int localLength = intLength();
            System.arraycopy(arrayStream.toArray(), start, delegate, 0,  localLength >= length? length: localLength);
        } else {
            int end = start + length;
            for (int i = start; i < end; i++) {
                this.delegate[i] = stream.get(i);
            }
        }
    }

    @Override
    public double[] toArray() {
        return this.delegate;
    }

}
