package model.datasource;

import java.util.Iterator;

/**
 * Created by maeglin89273 on 7/26/15.
 */
public interface Stream extends Iterable<Double> {
    public double get(long i);
    public long getCurrentLength();


}
