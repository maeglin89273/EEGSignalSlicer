package model.datasource;

import model.filter.Filter;

import java.util.*;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public abstract class FilteredDataSource extends FiniteLengthDataSource {
    private final HashMap<String, MutableFiniteLengthStream> cachedDataSpace;
    protected final Map<String, FiniteLengthStream> originalData;
    protected final Map<String, FiniteLengthStream> presentedData;
    protected LinkedList<Filter> filters;

    private long maxStreamLength;

    private List<PresentedDataChangedListener> listeners;

    protected FilteredDataSource(Map<String, FiniteLengthStream> originalData) {
        this.originalData = originalData;
        this.presentedData = new HashMap<>();
        this.cachedDataSpace = new HashMap<>();
        this.filters = new LinkedList<>();

        this.maxStreamLength = fillCachedDataSpaceAndFindMaxLength(this.originalData);
        this.listeners = new ArrayList<>();
    }

    private int fillCachedDataSpaceAndFindMaxLength(Map<String, FiniteLengthStream> originalData) {
        int streamLength = 0;
        int tempLength;
        for (Map.Entry<String, FiniteLengthStream> streamPair: originalData.entrySet()) {
            tempLength = streamPair.getValue().intLength();
            this.cachedDataSpace.put(streamPair.getKey(), new SimpleArrayStream(tempLength));
            streamLength = Math.max(tempLength, streamLength);
        }

        return streamLength;
    }

    public void addFilter(Filter filter) {
        this.filters.add(filter);
        this.clearPresentedData();
    }

    public void removeFilter(Filter filter) {
        this.filters.remove(filter);
        this.clearPresentedData();
    }

    public void replaceFilter(int i, Filter filter) {
        this.filters.set(i, filter);
        this.clearPresentedData();
    }

    @Override
    public long getCurrentLength() {
        return this.maxStreamLength;
    }

    @Override
    public FiniteLengthStream getFiniteDataOf(String tag) {
        if (!this.presentedData.containsKey(tag)) {
            this.presentedData.put(tag, filterOriginalData(tag));
        }

        return presentedData.get(tag);
    }

    private FiniteLengthStream filterOriginalData(String streamTag) {
        MutableFiniteLengthStream filteredStream = claimCachedSpace(streamTag);
        FiniteLengthStream originalStream = this.originalData.get(streamTag);

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
        return this.cachedDataSpace.get(streamTag);
    }

    private void clearPresentedData() {
        this.presentedData.clear();
        this.firePresentedDataChanged();
    }

    @Override
    public void addPresentedDataChangedListener(PresentedDataChangedListener listener) {
        this.listeners.add(listener);
    }

    private void firePresentedDataChanged() {
        for (int i = this.listeners.size() - 1; i >= 0; i--) {
            this.listeners.get(i).onDataChanged();
        }
    }

}
