package view.component.plugin;

import model.datasource.*;
import view.component.plot.PlotView;
import view.component.plot.PlottingUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by maeglin89273 on 8/20/15.
 */
public class SimilarStreamsPlottingPlugin extends StreamPlottingPlugin implements StreamingDataSource.PresentedDataChangedListener {
    private static final Stroke STROKE = new BasicStroke(0.7f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    private static final Stroke MEAN_STROKE = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);;

    private SimilarStreamsDataSource dataSource;
    private boolean meanShowed;
    private boolean samplesShowed = true;

    @Override
    public void drawBeforePlot(Graphics2D g2) {

        if (this.dataSource == null || dataSource.getTags().size() == 0) {
            return;
        }

        long startingPos = plot.getPlotLowerBound();
        if (this.samplesShowed) {
            g2.setStroke(STROKE);
            for (String tag : plot.getVisibleStreams()) {
                g2.setColor(PlottingUtils.hashStringToColor(tag, PlottingUtils.COLOR_TRANSLUCENT));
                for (Stream data : dataSource.getStreamsOf(tag)) {
                    this.plotStream(g2, data, startingPos);
                }
            }
        }
        if (this.meanShowed) {
            g2.setStroke(MEAN_STROKE);
            Stream meanStream;
            for (String tag : plot.getVisibleStreams()) {
                g2.setColor(PlottingUtils.hashStringToColor(tag, PlottingUtils.COLOR_DARKER));
                this.plotStream(g2, dataSource.getMeanStream(tag), startingPos);

            }
        }
    }

    public void setMeanShowed(boolean show) {
        if (this.isEnabled() && this.meanShowed != show) {
            this.meanShowed = show;
            plot.refresh();
        }
    }

    public void setSamplesShowed(boolean show) {
        if (this.isEnabled() && this.samplesShowed != show) {
            this.samplesShowed = show;
            plot.refresh();
        }
    }

    @Override
    public void onSourceReplaced(StreamingDataSource oldSource) {
        this.setDataSource(null);
    }

    public void setDataSource(SimilarStreamsDataSource dataSource) {
        if (this.isEnabled()) {
            if (this.dataSource != null) {
                this.dataSource.removePresentedDataChangedListener(this);
            }

            this.dataSource = dataSource;

            if (this.dataSource != null) {
                this.dataSource.addPresentedDataChangedListener(this);
            }

            this.plot.refresh();
        }
    }

    @Override
    public void setPlot(PlotView plot) {
        super.setPlot(plot);
        this.adjustBuffer(plot.getWindowSize());
    }

    @Override
    public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize) {
        if (this.isEnabled()) {
            this.adjustBuffer(windowSize);
        }
    }

    @Override
    public void onDataChanged(StreamingDataSource source) {
        if (this.isEnabled()) {
            this.plot.refresh();
        }
    }

    @Override
    public void onDataChanged(StreamingDataSource source, String tag) {
        if (this.isEnabled()) {
            this.plot.refresh();
        }
    }

    public static class SimilarStreamsDataSource implements StreamingDataSource, ViewDataSource {

        private Map<String, Map<StreamingDataSource, Stream>> classifier;
        private Map<String, MutableFiniteStream> meanStreams;
        private List<PresentedDataChangedListener> listeners;

        public SimilarStreamsDataSource() {
            this.classifier = new HashMap<>();
            this.meanStreams = new HashMap<>();
            this.listeners = new ArrayList<>();
        }

        @Override
        public Stream getDataOf(String tag) {
            throw new UnsupportedOperationException("there are multiple streams in this tag");
        }

        public Collection<Stream> getStreamsOf(String tag) {
            Map<StreamingDataSource, Stream> map = this.classifier.get(tag);
            if (map == null) {
                return Collections.emptySet();
            }

            return map.values();
        }

        @Override
        public Collection<String> getTags() {
            return classifier.keySet();
        }

        @Override
        public long getCurrentLength() {
            throw new UnsupportedOperationException("current length of data is highly changeable");
        }


        public void addDataSource(StreamingDataSource data) {
            addDataSourceCore(data);
            data.addPresentedDataChangedListener(this);
            firePresentedDataChanged();
        }

        private void addDataSourceCore(StreamingDataSource data) {
            for (String tag: data.getTags()) {
                Map<StreamingDataSource, Stream> streams = this.classifier.get(tag);
                if (streams == null) {
                    streams = new HashMap<>();
                    this.classifier.put(tag, streams);
                    //use SimpleArrayStream for the urgent
                    this.meanStreams.put(tag, new SimpleArrayStream((int)data.getCurrentLength()));
                }
                Stream stream = data.getDataOf(tag);
                Stream old = streams.put(data, stream);
                if (old != null) {
                    this.removeFromMean(tag, old, streams.size() - 1);
                }
                this.addToMean(tag, stream, streams.size());
            }
        }

        private void addToMean(String tag, Stream stream, int streamsAmount) {
            MutableFiniteStream meanStream = this.meanStreams.get(tag);

            double mean;
            for (int i = 0; i < meanStream.intLength(); i++) {
                mean = meanStream.get(i);
                meanStream.set(i, mean + (stream.get(i) - mean) / streamsAmount);
            }
        }

        public void removeDataSource(StreamingDataSource data) {
            if (removeDataSourceCore(data)) {
                data.removePresentedDataChangedListener(this);
                firePresentedDataChanged();
            }
        }

        private boolean removeDataSourceCore(StreamingDataSource data) {
            Map<StreamingDataSource, Stream> streams;
            Stream stream;
            boolean hasData = false;
            for (String tag: data.getTags()) {
                streams = this.classifier.get(tag);
                stream = streams.remove(data);
                if (stream != null) {
                    hasData = true;
                    this.removeFromMean(tag, stream, streams.size());
                }
            }

            return hasData;
        }

        private void removeFromMean(String tag, Stream stream, int streamsAmount) {
            MutableFiniteStream meanStream = this.meanStreams.get(tag);
            double mean;
            if (streamsAmount > 0) {
                for (int i = 0; i < meanStream.intLength(); i++) {
                    mean = meanStream.get(i);
                    meanStream.set(i, mean + (mean - stream.get(i)) / streamsAmount);
                }
            } else {
                for (int i = 0; i < meanStream.intLength(); i++) {
                    meanStream.set(i, 0);
                }
            }
        }

        @Override
        public void addPresentedDataChangedListener(PresentedDataChangedListener listener) {
            listeners.add(listener);
        }

        @Override
        public void removePresentedDataChangedListener(PresentedDataChangedListener listener) {
            listeners.remove(listener);
        }

        private void firePresentedDataChanged() {
            for (int i = this.listeners.size() - 1; i >= 0; i--) {
                this.listeners.get(i).onDataChanged(this);
            }
        }

        private void firePresentedDataChanged(String tag) {
            for (int i = this.listeners.size() - 1; i >= 0; i--) {
                this.listeners.get(i).onDataChanged(this, tag);
            }
        }

        @Override
        public void onDataChanged(StreamingDataSource source) {
            this.recalculateMean();
            this.firePresentedDataChanged();
        }

        private void recalculateMean() {
            for (Map.Entry<String, MutableFiniteStream> entry: meanStreams.entrySet()) {
                MutableFiniteStream mean = entry.getValue();
                for (int i = 0; i < mean.intLength(); i ++) {
                    Collection<Stream> streams = this.classifier.get(entry.getKey()).values();
                    double elementMean = 0;
                    for (Stream stream: streams){
                        elementMean += stream.get(i) / streams.size();
                    }

                    mean.set(i, elementMean);
                }
            }
        }

        @Override
        public void onDataChanged(StreamingDataSource source, String tag) {

            Stream stream = source.getDataOf(tag);
            if (stream != null) {
                this.classifier.get(tag).put(source, stream);
            } else {
                this.classifier.get(tag).remove(source);
            }
            this.firePresentedDataChanged(tag);
        }

        @Override
        public void setViewingSource(boolean viewing) {
            //not support yet
        }

        public Stream getMeanStream(String tag) {
            return this.meanStreams.get(tag);
        }
    }
}
