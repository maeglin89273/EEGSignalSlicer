package model.datasource;

import java.util.Iterator;

/**
 * Created by maeglin89273 on 7/26/15.
 */
public abstract class FiniteLengthStream implements Stream {
    public abstract int intLength();

    @Override
    public long getCurrentLength() {
        return this.intLength();
    }

    public abstract double[] toArray();

    @Override
    public Iterator<Double> iterator() {
        return new RandomAccessIteratorImpl(this);
    }


}
