package model.datasource;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 8/19/15.
 */
public abstract class CachedFiniteDataSource<T extends FiniteLengthStream> extends FiniteLengthDataSource {

    protected final Map<String, T> cachedData;

    protected CachedFiniteDataSource(Map<String, T> data) {
        this.cachedData = data;
    }

    protected CachedFiniteDataSource() {
        this(new HashMap<>());
    }

    @Override
    public FiniteLengthStream getFiniteDataOf(String tag) {
        return this.cachedData.get(tag);
    }
}
