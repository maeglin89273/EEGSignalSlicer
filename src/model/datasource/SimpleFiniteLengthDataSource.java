package model.datasource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by maeglin89273 on 7/28/15.
 */
public class SimpleFiniteLengthDataSource extends FiniteLengthDataSource {
    protected final Map<String, FiniteLengthStream> originalData;

    private int maxStreamLength;

    public SimpleFiniteLengthDataSource(Map<String, FiniteLengthStream> data) {
        this.originalData = data;
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
    public FiniteLengthStream getFiniteDataOf(String tag) {
        return this.originalData.get(tag);
    }

    @Override
    public int intLength() {
        return this.maxStreamLength;
    }

    @Override
    public Collection<String> getTags() {
        return originalData.keySet();
    }
}
