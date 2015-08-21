package model.datasource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maeglin89273 on 7/26/15.
 */
public abstract class FiniteLengthDataSource implements StreamingDataSource {

    private List<PresentedDataChangedListener> listeners;


    public FiniteLengthDataSource() {
        this.listeners = new ArrayList<>();
    }

    @Override
    public Stream getDataOf(String tag) {
        return this.getFiniteDataOf(tag);
    }

    public abstract FiniteLengthStream getFiniteDataOf(String tag);

    public abstract int intLength();

    @Override
    public long getCurrentLength() {
        return this.intLength();
    }

    @Override
    public void addPresentedDataChangedListener(PresentedDataChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removePresentedDataChangedListener(PresentedDataChangedListener listener) {
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
