package model.datasource;

import java.util.Iterator;

/**
 * Created by maeglin89273 on 7/26/15.
 */
public abstract class FiniteLengthStream implements Stream {
    public int intLength() {
        return (int) this.getCurrentLength();
    }

    public abstract double[] toArray();

    @Override
    public Iterator<Double> iterator() {
        return new IteratorImpl();
    }

    protected class IteratorImpl implements Iterator<Double> {
        int ptr = 0;

        @Override
        public boolean hasNext() {
            return ptr < intLength() - 1;
        }

        @Override
        public Double next() {
            return get(ptr++);
        }
    }
}
