package model.datasource;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maeglin89273 on 7/26/15.
 */
public interface FiniteLengthDataSource extends StreamingDataSource {

    public abstract FiniteLengthStream getFiniteDataOf(String tag);

    public abstract int intLength();


}
