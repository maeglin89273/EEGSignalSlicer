package model.datasource;

import model.filter.Filter;

/**
 * Created by maeglin89273 on 7/28/15.
 */
public interface FilteredDataSource extends StreamingDataSource {
    void addFilters(Filter... filters);

    void removeFilter(Filter filter);

    void replaceFilter(int i, Filter filter);

    Stream getOriginalDataOf(String tag);
}
