package view.component.plugin;

import model.datasource.*;
import org.jtransforms.fft.DoubleFFT_1D;
import view.component.PlotView;

import java.awt.*;
import java.util.*;

/**
 * Created by maeglin89273 on 7/26/15.
 */
public class FourierTransformPlugin extends RangePlugin implements InterestedStreamVisibilityPlugin {
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

    public StreamingDataSource getDataSourceOfRealPart() {
        return this.dataManager.getRealPart();
    }

    public StreamingDataSource getDataSourceOfImageryPart() {
        return this.dataManager.getImageryPart();
    }

    public StreamingDataSource getDataSourceOfPower() {
        return this.dataManager.getPower();
    }

    public StreamingDataSource getDataSourceOfPhase() {return this.dataManager.getPhase();}

    @Override
    public void reset() {
        super.reset();
        this.dataManager.reset();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled && !this.isRangeOverPlot()) {
            this.dataManager.setTags(plot.getVisibleStreams());
            this.updateTransformation();
        }
    }

    @Override
    protected void onRangeChanged() {
        updateTransformation();
    }

    @Override
    public void onPresentedDataChanged() {
        this.updateTransformation();
    }

    @Override
    public void onStreamVisibilityChanged(String tag, boolean isVisible) {
        if (this.isRangeOverPlot()) {
            return;
        }

        if (isVisible) {
            this.dataManager.addTag(tag);
            this.dataManager.transformData(tag, plot.getDataSource().getDataOf(tag), (int) this.getStartPosition());
        } else {
            this.dataManager.removeTag(tag);
        }

        this.dataManager.endTransformData();
    }

    public void updateTransformation() {
        if (!this.isEnabled() || this.isRangeOverPlot()) {
            return;
        }

        for (String tag: this.dataManager.getTags()) {
            this.dataManager.transformData(tag, plot.getDataSource().getDataOf(tag), (int) this.getStartPosition());

        }
        this.dataManager.endTransformData();
    }

    private class DataSourceManager {
        private FTDataSource power;
        private FTDataSource phase;

        private FTDataSource realPart;
        private FTDataSource imageryPart;

        private final DoubleFFT_1D fft;

        private final Map<String, MutableFiniteLengthStream> transformedData;
        private final Collection<String> validStreams;

        public DataSourceManager() {
            this.fft = new DoubleFFT_1D(transformRange);

            this.transformedData = new HashMap<>();
            this.validStreams = new LinkedList<>();

            this.realPart = new FTDataSource(this.transformedData,this.validStreams,  new RealPartSelector());
            this.imageryPart = new FTDataSource(this.transformedData, this.validStreams,  new ImageryPartSelector());
            this.power = new FTDataSource(this.transformedData, this.validStreams, new PowerSelector());
            this.phase = new FTDataSource(this.transformedData, this.validStreams, new PhaseSelector());

        }

        public StreamingDataSource getRealPart() {
            return realPart;
        }

        public StreamingDataSource getImageryPart() {
            return imageryPart;
        }

        public StreamingDataSource getPower() {return power;}

        public StreamingDataSource getPhase() {
            return phase;
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
            realPart.firePresentedDataChanged();
            imageryPart.firePresentedDataChanged();
            power.firePresentedDataChanged();
            phase.firePresentedDataChanged();
        }

        public void addTag(String tag) {
            this.validStreams.add(tag);
        }

        public void reset() {
            this.validStreams.clear();
            this.transformedData.clear();
        }

        public void removeTag(String tag) {
            this.validStreams.remove(tag);
        }

        public Collection<String> getTags() {
            return this.validStreams;
        }

        public void setTags(Collection<String> visibleStreams) {
            this.validStreams.clear();
            this.validStreams.addAll(visibleStreams);
        }
    }

    private class FTDataSource extends FiniteLengthDataSource {

        private final Map<String, MutableFiniteLengthStream> transformedData;
        private final Collection<String> validStreams;
        private final  FTStreamSelector streamElementSelector;

        public FTDataSource(Map<String, MutableFiniteLengthStream> transformedData, Collection<String> validStreams, FTStreamSelector streamElementSelector) {
            this.validStreams = validStreams;
            this.transformedData = transformedData;
            this.streamElementSelector = streamElementSelector;
        }

        @Override
        public Collection<String> getTags() {
            return this.validStreams;
        }

        @Override
        public FiniteLengthStream getFiniteDataOf(String tag) {
            return streamElementSelector.setFtStream(this.transformedData.get(tag));
        }

        @Override
        public int intLength() {
            return halfFFTRange;
        }

        @Override
        public void firePresentedDataChanged() {
            super.firePresentedDataChanged();
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
        public int intLength() {
            return halfFFTRange;
        }

        public FiniteLengthStream setFtStream(FiniteLengthStream ftStream) {
            this.ftStream = ftStream;
            return this;
        }

        protected double getReal(long i) {
            if (isRangeEven() && i == intLength() - 1) {
                return ftStream.get(1) / transformRange;
            }

            return ftStream.get(2 * i) / transformRange;
        }

        protected double getImagery(long i) {
            if (!isRangeEven() && i == intLength() - 1) {
                return ftStream.get(1) / transformRange;
            }
            if (i == 0 || i == intLength() - 1) {
                return 0;
            }

            return ftStream.get(2 * i + 1) / transformRange;
        }
    }

    private class RealPartSelector extends FTStreamSelector {

        @Override
        public double get(long i) {
            return getReal(i);
        }

    }

    private class ImageryPartSelector extends FTStreamSelector {

        @Override
        public double get(long i) {
            return getImagery(i);
        }
    }

    private class PowerSelector extends FTStreamSelector {

        @Override
        public double get(long i) {
            return Math.sqrt((Math.pow(getReal(i), 2) +  Math.pow(getImagery(i), 2)));
        }
    }

    private class PhaseSelector extends FTStreamSelector {

        private final double EPSILON = 1e-10;

        @Override
        public double get(long i) {
            double im = getImagery(i);
            double re = getReal(i);
            double powerSq = Math.pow(re, 2) + Math.pow(im, 2);

            return powerSq < EPSILON ? 0: Math.atan2(im, re);
        }
    }
}
