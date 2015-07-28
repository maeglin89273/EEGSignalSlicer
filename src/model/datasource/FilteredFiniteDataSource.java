package model.datasource;

import model.filter.Filter;

import java.util.*;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public class FilteredFiniteDataSource extends FiniteLengthDataSource implements FilteredDataSource {
    private final HashMap<String, MutableFiniteLengthStream> cachedDataSpace;

    protected final FiniteLengthDataSource rawSource;
    protected final Map<String, FiniteLengthStream> presentedData;
    protected LinkedList<Filter> filters;

    public FilteredFiniteDataSource(FiniteLengthDataSource rawSource) {
        this.rawSource = rawSource;
        this.presentedData = new HashMap<>();
        this.cachedDataSpace = new HashMap<>();
        this.filters = new LinkedList<>();
    }

    @Override
    public void addFilter(Filter filter) {
        this.filters.add(filter);
        this.clearPresentedData();
    }

    @Override
    public void removeFilter(Filter filter) {
        this.filters.remove(filter);
        this.clearPresentedData();
    }

    @Override
    public void replaceFilter(int i, Filter filter) {
        this.filters.set(i, filter);
        this.clearPresentedData();
    }

    @Override
    public FiniteLengthStream getFiniteDataOf(String tag) {
        if (!this.presentedData.containsKey(tag)) {
            this.presentedData.put(tag, filterOriginalData(tag));
        }

        return presentedData.get(tag);
    }

    @Override
    public Stream getOriginalDataOf(String tag) {
        return this.rawSource.getDataOf(tag);
    }

    private FiniteLengthStream filterOriginalData(String streamTag) {
        MutableFiniteLengthStream filteredStream = claimCachedSpace(streamTag);
        FiniteLengthStream originalStream = this.rawSource.getFiniteDataOf(streamTag);

        if (filters.isEmpty()) {
            filteredStream.replacedBy(originalStream, 0, originalStream.intLength());

        } else {
            filters.getFirst().filter(originalStream, filteredStream);
            ListIterator<Filter> filterListIterator = filters.listIterator(1);
            for (;filterListIterator.hasNext();) {
                filterListIterator.next().filter(filteredStream, filteredStream);
            }
        }

        return filteredStream;
    }

    private MutableFiniteLengthStream claimCachedSpace(String streamTag) {
        if (this.cachedDataSpace.containsKey(streamTag)) {
            return this.cachedDataSpace.get(streamTag);
        }
        MutableFiniteLengthStream streamCache = new SimpleArrayStream(this.rawSource.getFiniteDataOf(streamTag).intLength());
        this.cachedDataSpace.put(streamTag, streamCache);
        return streamCache;
    }

    private void clearPresentedData() {
        this.presentedData.clear();
        this.firePresentedDataChanged();
    }

    @Override
    public int intLength() {
        return this.rawSource.intLength();
    }

    @Override
    public Collection<String> getTags() {
        return this.rawSource.getTags();
    }
}
