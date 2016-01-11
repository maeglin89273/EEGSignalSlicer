package model.datasource;

/**
 * Created by maeglin89273 on 12/2/15.
 */
public abstract class AbstractFiniteDataSource extends AbstractDataSource implements FiniteLengthDataSource {
    @Override
    public Stream getDataOf(String tag) {
        return this.getFiniteDataOf(tag);
    }

    @Override
    public long getCurrentLength() {
        return this.intLength();
    }
}
