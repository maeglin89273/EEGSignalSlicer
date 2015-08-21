package model.datasource;

/**
 * Created by maeglin89273 on 7/26/15.
 */
public abstract class MutableFiniteStream extends FiniteLengthStream {
    public abstract void set(int i, double value);
    public void replacedBy(Stream stream, int start) {
        this.replacedBy(stream, start, (int) Long.min(stream.getCurrentLength(), Integer.MAX_VALUE));
    }

    public abstract void replacedBy(Stream stream, int start, int length);
}
