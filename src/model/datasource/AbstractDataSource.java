package model.datasource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maeglin89273 on 12/2/15.
 */
public abstract class AbstractDataSource implements StreamingDataSource {
    protected List<StreamingDataSource.PresentedDataChangedListener> listeners;

    public AbstractDataSource() {
        this.listeners = new ArrayList<>();
    }

    @Override
    public void addPresentedDataChangedListener(StreamingDataSource.PresentedDataChangedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removePresentedDataChangedListener(StreamingDataSource.PresentedDataChangedListener listener) {
        listeners.remove(listener);
    }

    protected void firePresentedDataChanged() {
        for (int i = this.listeners.size() - 1; i >= 0; i--) {
            this.listeners.get(i).onDataChanged(this);
        }
    }

    protected void firePresentedDataChanged(String tag) {
        for (int i = this.listeners.size() - 1; i >= 0; i--) {
            this.listeners.get(i).onDataChanged(this, tag);
        }
    }
}
