package model.datasource;

/**
 * Created by maeglin89273 on 7/26/15.
 */
public interface StreamingDataSource {
    Stream getDataOf(String tag);
    long getCurrentLength();

    void addPresentedDataChangedListener(PresentedDataChangedListener listener);

    public interface PresentedDataChangedListener {
        public void onDataChanged();
        public void onDataChanged(String tag);
    }

}
