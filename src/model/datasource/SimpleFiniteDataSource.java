package model.datasource;

import java.util.Collection;
import java.util.Map;

/**
 * Created by maeglin89273 on 7/28/15.
 */
public class SimpleFiniteDataSource extends CachedFiniteDataSource<FiniteLengthStream> {

    private int maxStreamLength;

    public SimpleFiniteDataSource(Map<String, FiniteLengthStream> data) {
        super(data);
        maxStreamLength = findMaxLength(data);
    }

    private static int findMaxLength(Map<String, FiniteLengthStream> data) {
        int maxLength = 0;
        for (FiniteLengthStream stream: data.values()) {
            maxLength = Math.max(stream.intLength(), maxLength);
        }

        return maxLength;
    }

    @Override
    public int intLength() {
        return this.maxStreamLength;
    }

    @Override
    public Collection<String> getTags() {
        return this.cachedData.keySet();
    }
}
