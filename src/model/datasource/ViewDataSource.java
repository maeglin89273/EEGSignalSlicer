package model.datasource;

/**
 * Created by maeglin89273 on 8/21/15.
 */
public interface ViewDataSource extends StreamingDataSource, StreamingDataSource.PresentedDataChangedListener {
    public abstract void stopViewingSource();
}
