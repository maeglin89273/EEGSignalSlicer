package model.datasource;

/**
 * Created by maeglin89273 on 12/2/15.
 */
public interface InfiniteLengthDataSource extends StreamingDataSource {
    public void updateValueTo(String tag, double value);
}
