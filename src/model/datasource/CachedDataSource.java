package model.datasource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 8/19/15.
 */
public abstract class CachedDataSource<T extends Stream> extends AbstractDataSource {

    protected final Map<String, T> cachedData;
    protected long currentLength;

    protected CachedDataSource(Map<String, T> data) {
        this.cachedData = data;
        this.currentLength = findMaxLength(data.values());
    }

    protected CachedDataSource() {
        this(new HashMap<>());
    }

    @Override
    public Stream getDataOf(String tag)  {
        return this.getTypedDataOf(tag);
    }

    public T getTypedDataOf(String tag) {
        return this.cachedData.get(tag);
    }

    @Override
    public Collection<String> getTags() {
        return this.cachedData.keySet();
    }

    @Override
    public long getCurrentLength() {
        return this.currentLength;
    }

    private static long findMaxLength(Collection<? extends Stream> data) {
        long maxLength = 0;
        for (Stream stream: data) {
            maxLength = Math.max(stream.getCurrentLength(), maxLength);
        }

        return maxLength;
    }

}
