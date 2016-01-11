package model.datasource;

import java.util.Map;

/**
 * Created by maeglin89273 on 12/2/15.
 */
public abstract class FiniteCachedDataSource<T extends FiniteLengthStream> extends CachedDataSource<T> implements FiniteLengthDataSource {

    protected FiniteCachedDataSource() {
        super();
    }

    protected FiniteCachedDataSource(Map<String, T> data) {
        super(data);
    }

    @Override
    public Stream getDataOf(String tag) {
        return this.getTypedDataOf(tag);
    }

    @Override
    public FiniteLengthStream getFiniteDataOf(String tag) {
        return this.getTypedDataOf(tag);
    }

    @Override
    public int intLength() {
        return (int)this.getCurrentLength();
    }

}
