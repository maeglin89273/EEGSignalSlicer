package model.datasource;

import model.filter.Filter;

/**
 * Created by maeglin89273 on 7/28/15.
 */
public interface FilteredDataSource extends StreamingDataSource {
    void addFilter(Filter filter);

    void removeFilter(Filter filter);

    void replaceFilter(int i, Filter filter);

    Stream getOriginalDataOf(String tag);
}
