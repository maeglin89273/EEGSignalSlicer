package model.datasource;

/**
 * Created by maeglin89273 on 7/26/15.
 */
public class SimpleArrayStream extends MutableFiniteStream {
    private double[] buffer;
    private int validLength;
    public SimpleArrayStream(int length) {
        this(new double[length]);
    }

    public SimpleArrayStream(double[] array) {
        this.setUnderlyingBuffer(array);
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
//        if (i >= this.validLength) {
//            this.validLength = i + 1;
//        }

        if (i < this.validLength) {
            this.buffer[i] = value;
            return;
        }
        throw new IndexOutOfBoundsException("invalid index to stream: " + i);
    }

    @Override
    public void replacedBy(Stream stream, int start, int length) {
        this.validLength = buffer.length >= length? (int)length: buffer.length;

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

    public SimpleArrayStream setUnderlyingBuffer(double[] buffer) {
        this.buffer = buffer;
        this.validLength = this.buffer.length;
        return this;
    }
}
