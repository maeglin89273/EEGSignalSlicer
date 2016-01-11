package model.datasource;

import model.filter.Filter;

import java.util.*;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public class FilteredFiniteDataSource extends FiniteCachedDataSource<FiniteLengthStream> implements FilteredDataSource, ViewDataSource {
    private final HashMap<String, MutableFiniteStream> backupDataSpace;

    protected final FiniteLengthDataSource rawSource;
    protected LinkedList<Filter> filters;

    private int oldSourceLength;

    public FilteredFiniteDataSource(FiniteLengthDataSource rawSource) {
        this.rawSource = rawSource;
        this.backupDataSpace = new HashMap<>();
        this.filters = new LinkedList<>();
        this.setViewingSource(true);
        this.currentLength = this.oldSourceLength = this.rawSource.intLength();
    }

    @Override
    public void addFilters(Filter... filters) {
        for (Filter filter: filters) {
            this.filters.add(filter);
        }

        this.recalculateFilteredInfo();
        this.refilterAllPresentedData();
        this.firePresentedDataChanged();
    }

    @Override
    public void removeFilter(Filter filter) {
        this.filters.remove(filter);
        this.recalculateFilteredInfo();
        this.refilterAllPresentedData();
        this.firePresentedDataChanged();
    }

    @Override
    public void replaceFilter(int i, Filter filter) {
        this.filters.set(i, filter);
        this.recalculateFilteredInfo();
        this.refilterAllPresentedData();
        this.firePresentedDataChanged();
    }

    @Override
    public FiniteLengthStream getTypedDataOf(String tag) {
        if (!this.cachedData.containsKey(tag)) {
            this.cachedData.put(tag, filterOriginalData(tag));
        }
        return super.getTypedDataOf(tag);
    }

    @Override
    public Stream getOriginalDataOf(String tag) {
        return this.rawSource.getDataOf(tag);
    }

    private FiniteLengthStream filterOriginalData(String streamTag) {
        MutableFiniteStream filteredStream = claimBackupSpace(streamTag);
        FiniteLengthStream originalStream = this.rawSource.getFiniteDataOf(streamTag);

        filterStream(originalStream, filteredStream);

        return filteredStream;
    }

    private void filterStream(FiniteLengthStream originalStream, MutableFiniteStream filteredStream) {
        if (filters.isEmpty()) {
            filteredStream.replacedBy(originalStream, 0, originalStream.intLength());

        } else {
            filters.getFirst().filter(originalStream, filteredStream);

            for (Filter filter: filters.subList(1, filters.size())) {
                filter.filter(filteredStream, filteredStream);
            }
        }

    }

    private MutableFiniteStream claimBackupSpace(String streamTag) {
        if (this.backupDataSpace.containsKey(streamTag)) {
            return this.backupDataSpace.get(streamTag);
        }
        MutableFiniteStream streamCache = new FiniteListStream(this.rawSource.getFiniteDataOf(streamTag).intLength());
        this.backupDataSpace.put(streamTag, streamCache);
        return streamCache;
    }



    private void refilterAllPresentedData() {
        for (String tag: this.cachedData.keySet()) {
            filterOriginalData(tag);
        }
    }

    @Override
    public Collection<String> getTags() {
        return this.rawSource.getTags();
    }

    @Override
    public void onDataChanged(StreamingDataSource source) {
        this.estimateLengthChanged();
        if (!source.getTags().equals(this.cachedData.keySet())) {
            this.cachedData.clear();
        } else {
            this.refilterAllPresentedData();
        }
        this.firePresentedDataChanged();
    }

    @Override
    public void onDataChanged(StreamingDataSource source, String tag) {
        this.estimateLengthChanged();
        if (source.getDataOf(tag) != null) {
            filterOriginalData(tag);
        }
        this.firePresentedDataChanged(tag);
    }

    private void estimateLengthChanged() {
        if (this.rawSource.intLength() != this.oldSourceLength) {
            this.oldSourceLength = this.rawSource.intLength();
            this.recalculateFilteredInfo();
        }
    }

    private void recalculateFilteredInfo() {
        int tempLength = rawSource.intLength();
        for (Filter filter: filters) {
            tempLength = filter.calculateLengthAfterFiltering(tempLength);
        }

        this.currentLength = tempLength;
    }

    @Override
    public void setViewingSource(boolean viewing) {
        if (viewing) {
            this.rawSource.addPresentedDataChangedListener(this);
            this.estimateLengthChanged();
            this.refilterAllPresentedData();
            this.firePresentedDataChanged();
        } else {
            this.rawSource.removePresentedDataChangedListener(this);
        }
    }
}
