package view.component.plugin;

import model.datasource.*;
import view.component.plot.PlotView;

import java.awt.*;
import java.util.*;

/**
 * Created by maeglin89273 on 7/27/15.
 */
public class SnapshotPlugin extends StreamPlottingPlugin {
    private final Stroke stroke;
    private static final Color SNAPSHOT_COLOR = new Color(0, 0, 0, 0.25f);

    CapturedDataSource capturedData;
    private long startingPtr;
    private int windowSize;
    private boolean plotSnapshot;
    private boolean hasSnapshot;

    public SnapshotPlugin() {
        this.stroke = new BasicStroke(1.2f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
        this.capturedData = new CapturedDataSource();
        this.plotSnapshot = true;
        this.hasSnapshot = false;
    }

    @Override
    public void setPlot(PlotView plot) {
        super.setPlot(plot);
        this.setEnabled(true);
    }

    public void setPlotSnapshot(boolean plotIt) {
        this.plotSnapshot = plotIt;
    }

    public boolean isPlotSnapshot() {
        return this.plotSnapshot;
    }

    public AbstractDataSource getCapturedData() {
        return this.capturedData;
    }

    @Override
    public void drawBeforePlot(Graphics2D g2) {
        if (!this.plotSnapshot || !this.hasSnapshot) {
            return;
        }
        g2.setStroke(stroke);
        g2.setColor(SNAPSHOT_COLOR);
        FiniteLengthStream stream;
        for (String tag : this.capturedData.getCapturedTags()) {
            stream = capturedData.getFiniteDataOf(tag);
            this.plotStream(g2, stream, this.startingPtr);
        }
    }

    @Override
    public void onSourceReplaced(StreamingDataSource oldSource) {
        this.clear();
    }

    public void capture() {
        if (!this.isEnabled()) {
            return;
        }

        int startPos = (int) plot.getPlotLowerBound();
        int length = plot.getWindowSize();
        StreamingDataSource dataSource = plot.getDataSource();
        Collection<String> visibleStreams = plot.getVisibleStreams();
        this.capturedData.setCapturedTags(visibleStreams);
        for (String tag: visibleStreams) {
            this.capturedData.captureStream(tag, dataSource.getDataOf(tag), startPos, length);
        }

        this.adjustBuffer(length);
        this.hasSnapshot = true;
        this.capturedData.firePresentedDataChanged();

        if (this.plotSnapshot) {
            this.plot.refresh();
        }
    }

    public void clear() {
        if (!this.hasSnapshot) {
            return;
        }
        this.hasSnapshot = false;
        capturedData.clear();
        this.plot.refresh();
    }

    private class CapturedDataSource extends FiniteCachedDataSource<MutableFiniteStream> {
        private Collection<String> capturedTags;
        private int length;

        private CapturedDataSource() {
            this.clear();
        }

        public void captureStream(String tag, Stream target, int startPos, int length) {
            this.length = (int) Math.max(target.getCurrentLength(), length);
            MutableFiniteStream capturedStream;
            if (this.cachedData.containsKey(tag)) {
                capturedStream = cachedData.get(tag);
                if (capturedStream.intLength() >= length) {
                    capturedStream = new SimpleArrayStream(length);
                    this.cachedData.put(tag, capturedStream);
                }
            } else {
                capturedStream = new SimpleArrayStream(length);
                this.cachedData.put(tag, capturedStream);
            }

            capturedStream.replacedBy(target, startPos, length);
        }

        public void setCapturedTags(Collection<String> tags) {
            this.capturedTags = new HashSet<>(tags);
        }

        public Collection<String> getCapturedTags() {
            return this.capturedTags;
        }

        @Override
        public FiniteLengthStream getFiniteDataOf(String tag) {
            if (this.capturedTags.contains(tag)) {
                return super.getFiniteDataOf(tag);
            }
            return null;
        }

        @Override
        public int intLength() {
            return length;
        }

        @Override
        public Collection<String> getTags() {
            return capturedTags;
        }

        @Override
        public void firePresentedDataChanged() {
            super.firePresentedDataChanged();
        }

        public void clear() {
            this.capturedTags = new LinkedList();
            this.length = 0;
        }
    }
}
