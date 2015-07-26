package model.datasource;

/**
 * Created by maeglin89273 on 7/26/15.
 */
public abstract class MutableFiniteLengthStream extends FiniteLengthStream {
    public abstract void set(int i, double value);
    public abstract void replacedBy(Stream stream, int start, int length);
}
