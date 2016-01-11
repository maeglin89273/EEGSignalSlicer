package model.datasource;

import java.util.Map;

/**
 * Created by maeglin89273 on 12/2/15.
 */
public class SimpleInfiniteDataSource extends CachedDataSource<InfiniteLengthStream> implements InfiniteLengthDataSource {

    public SimpleInfiniteDataSource(Map<String, InfiniteLengthStream> data) {
        super(data);

    }

    @Override
    public void updateValueTo(String tag, double value) {
        InfiniteLengthStream stream = this.getTypedDataOf(tag);
        stream.update(value);
        if (this.currentLength < stream.getCurrentLength()) {
            this.currentLength++;
        }
        this.firePresentedDataChanged(tag);
    }

}
