package view.component.plugin;

import model.datasource.Stream;
import model.datasource.StreamingDataSource;
import model.datasource.ViewDataSource;
import view.component.plot.PlotView;
import view.component.plot.PlottingUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by maeglin89273 on 8/20/15.
 */
public class SimilarStreamsPlottingPlugin extends StreamPlottingPlugin implements StreamingDataSource.PresentedDataChangedListener {
    private static final Stroke STROKE = new BasicStroke(0.7f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);;
    private SimilarStreamsDataSource dataSource;

    @Override
    public void drawBeforePlot(Graphics2D g2) {

        if (this.dataSource == null) {
            return;
        }

        long startingPos = plot.getPlotLowerBound();
        for (String tag: plot.getVisibleStreams()) {
            g2.setColor(PlottingUtils.hashStringToColor(tag, true));
            for (Stream data: dataSource.getSetofStreamsOf(tag)) {
                this.plotStream(g2, data, startingPos);
            }
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
        private List<PresentedDataChangedListener> listeners;

        public SimilarStreamsDataSource() {
            this.classifier = new HashMap<>();
            this.listeners = new ArrayList<>();
        }

        @Override
        public Stream getDataOf(String tag) {
            throw new UnsupportedOperationException("there are multiple streams in this tag");
        }

        public Collection<Stream> getSetofStreamsOf(String tag) {
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
                }
                streams.put(data, data.getDataOf(tag));
            }
        }

        public void removeDataSource(StreamingDataSource data) {
            removeDataSourceCore(data);
            data.removePresentedDataChangedListener(this);
            firePresentedDataChanged();
        }

        private void removeDataSourceCore(StreamingDataSource data) {
            for (String tag: data.getTags()) {
                this.classifier.get(tag).remove(data);
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
            this.addDataSourceCore(source);
            firePresentedDataChanged();

        }

        @Override
        public void onDataChanged(StreamingDataSource source, String tag) {
            Stream stream = source.getDataOf(tag);
            if (stream != null) {
                this.classifier.get(tag).replace(source, stream);
            } else {
                this.classifier.get(tag).remove(source);
            }
            firePresentedDataChanged(tag);
        }

        @Override
        public void stopViewingSource() {
            //not support yet
        }
    }
}
