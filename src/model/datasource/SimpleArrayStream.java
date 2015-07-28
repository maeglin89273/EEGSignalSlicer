package model.datasource;

/**
 * Created by maeglin89273 on 7/26/15.
 */
public class SimpleArrayStream extends MutableFiniteLengthStream {
    private final double[] buffer;
    private int validLength;
    public SimpleArrayStream(int length) {
        this(new double[length]);
    }

    public SimpleArrayStream(double[] array) {
        this.buffer = array;
        this.validLength = this.buffer.length;
    }

    @Override
    public double get(long i) {
        if (i < this.validLength) {
            return this.buffer[((int) i)];
        }
        throw new IndexOutOfBoundsException("invalid index to stream: " + i);
    }

    @Override
    public int intLength() {
        return this.validLength;
    }

    @Override
    public void set(int i, double value) {
        if (i >= this.validLength) {
            this.validLength = i + 1;
        }
        this.buffer[i] = value;
    }

    @Override
    public void replacedBy(Stream stream, int start, int length) {
        this.validLength = buffer.length >= length? length: buffer.length;
        if (stream instanceof FiniteLengthStream) {
            FiniteLengthStream arrayStream = (FiniteLengthStream) stream;

            System.arraycopy(arrayStream.toArray(), start, buffer, 0,  this.validLength);
        } else {
            for (int i = 0; i < this.validLength; i++) {
                this.buffer[i] = stream.get(i + start);
            }
        }
    }

    @Override
    public double[] toArray() {
        return this.buffer;
    }

}
