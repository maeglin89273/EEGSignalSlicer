package model.datasource;

import java.util.Iterator;

/**
 * Created by maeglin89273 on 12/2/15.
 */
public class CyclicStream implements InfiniteLengthStream {

    private double[] buffer;
    private int currentLength;
    private int headIndex;

    public CyclicStream(int maxLength) {
        this.buffer = new double[maxLength];
        this.currentLength = this.headIndex = 0;
    }


    @Override
    public double get(long i) {
        return this.buffer[this.cyclicIndex(i)];
    }

    private int cyclicIndex(long i) {
        return ((int) ((this.headIndex + i) % this.buffer.length));
    }

    @Override
    public long getCurrentLength() {
        return this.currentLength;
    }

    @Override
    public void update(double value) {

        if (this.currentLength + 1 <= buffer.length) {
            this.buffer[this.currentLength] = value;
            this.currentLength++;
        } else {
            this.buffer[this.headIndex] = value;
            this.headIndex = (this.headIndex + 1) % this.buffer.length;
        }

    }

    @Override
    public Iterator<Double> iterator() {
        return new RandomAccessIteratorImpl(this);
    }
}
