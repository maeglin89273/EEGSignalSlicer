package model.datasource;

import java.util.Iterator;

/**
 * Created by maeglin89273 on 7/26/15.
 */
public abstract class FiniteLengthDataSource implements StreamingDataSource {

    @Override
    public Stream getDataOf(String tag) {
        return this.getFiniteDataOf(tag);
    }

    public abstract FiniteLengthStream getFiniteDataOf(String tag);

    public int intLength() {
        return (int) this.getCurrentLength();
    }
}
