package view.component.plugin;

import model.datasource.*;
import org.jtransforms.fft.DoubleFFT_1D;
import view.component.PlotView;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by maeglin89273 on 7/26/15.
 */
public class FourierTransformPlugin extends RangePlugin {
    private final int samplingFrequency;
    private final int transformRange;
    private final int halfFFTRange;

    private DataSourceManager dataManager;

    public FourierTransformPlugin(int samplingFrequency, int range) {
        super(Color.GREEN);
        this.samplingFrequency = samplingFrequency;
        this.transformRange = range;
        this.halfFFTRange = this.computeHalfRange();

    }

    @Override
    public void setPlot(PlotView plot) {
        super.setPlot(plot);

        this.dataManager = new DataSourceManager();

        this.setEnabled(true);
        this.setRenderRangeBackground(true);
        this.setRange(this.transformRange);
        this.setFixedRange(true);
        this.setEnabled(false);

    }

    private int computeHalfRange() {
        return this.isRangeEven()? this.transformRange / 2 + 1: (this.transformRange + 1) / 2;
    }

    private boolean isRangeEven() {
        return transformRange % 2 == 0;
    }

    public StreamingDataSource getRealPartDataSource() {
        return this.dataManager.getRealPart();
    }

    public StreamingDataSource getImageryPartDataSource() {
        return this.dataManager.getImageryPart();
    }

    @Override
    protected void syncRangeToPlot(long plotLowerBound, long plotUpperBound, int windowSize) {
        super.syncRangeToPlot(plotLowerBound, plotUpperBound, windowSize);
        updateTransformation();
    }

    @Override
    public void setStartPosition(long startPosition) {
        super.setStartPosition(startPosition);
        updateTransformation();
    }

    @Override
    public void setEndPosition(long endPosition) {
        super.setEndPosition(endPosition);
        updateTransformation();
    }

    @Override
    public void setRange(int range) {
        super.setRange(range);
        updateTransformation();
    }

    private void updateTransformation() {
        if (!this.isEnabled()) {
            return;
        }

        Collection<String> visibleStreams = plot.getVisibleStreams();
        for (String tag: visibleStreams) {
            this.dataManager.transformData(tag, plot.getDataSource().getDataOf(tag), (int) this.getStartPosition());

        }
        this.dataManager.endTransformData();
    }

    private class DataSourceManager {
        private FTDataSource realPart;
        private FTDataSource imageryPart;

        private final DoubleFFT_1D fft;


        private Map<String, MutableFiniteLengthStream> transformedData;

        public DataSourceManager() {
            this.fft = new DoubleFFT_1D(transformRange);
            this.transformedData = new HashMap<>();
            this.realPart = new FTDataSource(this.transformedData, new RealPartSelector());
            this.imageryPart = new FTDataSource(this.transformedData, new ImageryPartSelector());
        }

        public StreamingDataSource getRealPart() {
            return realPart;
        }

        public StreamingDataSource getImageryPart() {
            return imageryPart;
        }

        public void transformData(String tag, Stream data, int startPos) {
            if (!this.transformedData.containsKey(tag)) {
                this.transformedData.put(tag, new SimpleArrayStream(transformRange));
            }

            MutableFiniteLengthStream buffer = this.transformedData.get(tag);
            buffer.replacedBy(data, startPos, buffer.intLength());

            fft.realForward(buffer.toArray());
        }

        public void endTransformData() {
            realPart.fireDataChanged();
            imageryPart.fireDataChanged();
        }
    }

    private class FTDataSource extends FiniteLengthDataSource {

        private Map<String, MutableFiniteLengthStream> transformedData;
        private List<PresentedDataChangedListener> listeners;
        private FTStreamSelector streamElementSelector;
        protected FTDataSource(Map<String, MutableFiniteLengthStream> transformedData, FTStreamSelector streamElementSelector) {
            this.transformedData = transformedData;
            this.streamElementSelector = streamElementSelector;
            this.listeners = new ArrayList<>();
        }

        @Override
        public long getCurrentLength() {
            return halfFFTRange;
        }

        @Override
        public FiniteLengthStream getFiniteDataOf(String tag) {
            return streamElementSelector.setFtStream(this.transformedData.get(tag));
        }


        private void fireDataChanged() {
            for (int i = listeners.size() - 1; i >= 0; i--) {
                listeners.get(i).onDataChanged();
            }
        }

        @Override
        public void addPresentedDataChangedListener(PresentedDataChangedListener listener) {
            this.listeners.add(listener);
        }

    }

    private abstract class FTStreamSelector extends FiniteLengthStream {

        protected FiniteLengthStream ftStream;

        @Override
        public double[] toArray() {
            double[] copy = new double[intLength()];
            for (int i = 0; i < copy.length; i++) {
                copy[i] = this.get(i);
            }
            return copy;
        }

        @Override
        public long getCurrentLength() {
            return halfFFTRange;
        }

        public FiniteLengthStream setFtStream(FiniteLengthStream ftStream) {
            this.ftStream = ftStream;
            return this;
        }
    }

    private class RealPartSelector extends FTStreamSelector {

        @Override
        public double get(long i) {
            if (isRangeEven() && i == intLength() - 1) {
                return ftStream.get(1);
            }

            return Math.abs(ftStream.get(2 * i));
        }
    }

    private class ImageryPartSelector extends FTStreamSelector {

        @Override
        public double get(long i) {
            if (!isRangeEven() && i == intLength() - 1) {
                return ftStream.get(1);
            }
            if (i == 0 || i == intLength() - 1) {
                return 0;
            }

            return ftStream.get(2 * i + 1);
        }
    }

}
