package model.datasource;

import view.component.PlotView;

import java.util.Collection;

/**
 * Created by maeglin89273 on 7/26/15.
 */
public interface StreamingDataSource {
    Stream getDataOf(String tag);
    Collection<String> getTags();

    long getCurrentLength();

    void addPresentedDataChangedListener(PresentedDataChangedListener listener);

    void removePresentedDataChangedListener(PresentedDataChangedListener listener);

    public interface PresentedDataChangedListener {
        public void onDataChanged();
        public void onDataChanged(String tag);
    }

}
