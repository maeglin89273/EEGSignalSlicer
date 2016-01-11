package model.datasource;

import java.util.Iterator;

/**
 * Created by maeglin89273 on 12/2/15.
 */

class RandomAccessIteratorImpl implements Iterator<Double> {

    private int ptr = 0;
    private final Stream wrapped;

    RandomAccessIteratorImpl(Stream wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean hasNext() {
        return ptr < this.wrapped.getCurrentLength() - 1;
    }

    @Override
    public Double next() {
        return this.wrapped.get(ptr++);
    }
}

