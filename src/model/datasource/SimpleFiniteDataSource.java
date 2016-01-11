package model.datasource;

import java.util.Collection;
import java.util.Map;

/**
 * Created by maeglin89273 on 7/28/15.
 */
public class SimpleFiniteDataSource extends FiniteCachedDataSource<FiniteLengthStream> {

    public SimpleFiniteDataSource(Map<String, FiniteLengthStream> data) {
        super(data);

    }

}
