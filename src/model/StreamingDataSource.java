package model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public abstract class StreamingDataSource {
    private final HashMap<String, double[]> cachedDataSpace;
    protected final Map<String, double[]> originalData;
    protected final Map<String, double[]> presentedData;
    protected LinkedList<Filter> filters;

    private long maxStreamLength;

    protected StreamingDataSource(Map<String, double[]> originalData) {
        this.originalData = originalData;
        this.presentedData = new HashMap<String, double[]>();
        this.cachedDataSpace = new HashMap<String, double[]>();
        this.filters = new LinkedList<Filter>();

        this.maxStreamLength = fillCachedDataSpaceAndFindMaxLength(this.originalData);
    }

    private long fillCachedDataSpaceAndFindMaxLength(Map<String, double[]> originalData) {
        long streamLength = 0;
        int tempLength;
        for (Map.Entry<String, double[]> streamPair: originalData.entrySet()) {
            tempLength = streamPair.getValue().length;
            this.cachedDataSpace.put(streamPair.getKey(), new double[tempLength]);
            streamLength = Math.max(tempLength, streamLength);
        }

        return streamLength;
    }

    public void addFilter(Filter filter) {
        this.clearPresentedData();
        this.filters.add(filter);
    }

    public void removeFilter(Filter filter) {
        this.clearPresentedData();
        this.filters.remove(filter);
    }

    public void replaceFilter(int i, Filter filter) {
        this.clearPresentedData();
        this.filters.set(i, filter);
    }

    public long getMaxStreamLength() {
        return this.maxStreamLength;
    }

    public double[] getDataOf(String tag) {
        if (!this.presentedData.containsKey(tag)) {
            this.presentedData.put(tag, prepareData(tag));
        }

        return presentedData.get(tag);
    }

    private double[] prepareData(String streamTag) {
        double[] filteredStream = claimCachedSpace(streamTag);
        double[] originalStream = this.originalData.get(streamTag);

        if (filters.isEmpty()) {
            System.arraycopy(originalStream, 0, filteredStream, 0, filteredStream.length);

        } else {
            filters.getFirst().filter(originalStream, filteredStream);
            ListIterator<Filter> filterListIterator = filters.listIterator(1);
            for (;filterListIterator.hasNext();) {
                filterListIterator.next().filter(filteredStream, filteredStream);
            }
        }

        return filteredStream;
    }

    private double[] claimCachedSpace(String streamTag) {
        return this.cachedDataSpace.get(streamTag);
    }

    private void clearPresentedData() {
        presentedData.clear();
    }
}
