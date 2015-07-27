package view.component.plugin;

import model.datasource.*;
import view.component.PlottingUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by maeglin89273 on 7/27/15.
 */
public class SnapshotPlugin extends EmptyPlotPlugin {
    private final Stroke stroke;
    private static final Color SNAPSHOT_COLOR = new Color(0, 0, 0, 0.25f);

    CapturedDataSource capturedData;
    private long startingPtr;
    private int windowSize;
    private int[] yBuffer;


    public SnapshotPlugin() {
        this.stroke = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
        this.capturedData = new CapturedDataSource();
    }


    public FiniteLengthDataSource getCapturedData() {
        return this.capturedData;
    }

    @Override
    public void drawAfterPlot(Graphics2D g2) {
        g2.setStroke(stroke);
        g2.setColor(SNAPSHOT_COLOR);
        for (String tag : ) {
            PlottingUtils.loadYBuffer(plot.getBaseline(), plot.getPeakValue(), plot.getHeight(), capturedData.getDataOf(tag), (int) this.startingPtr, yBuffer);
            g2.drawPolyline(this.plot.getXPoints(), yBuffer, yBuffer.length);
        }
    }

    @Override
    public void reset() {

    }

    @Override
    public void onXRangeChanged(long plotLowerBound, long plotUpperBound, int windowSize) {
        if (this.isEnabled()) {
            adjustBuffers(windowSize);
        }
    }

    private void adjustBuffers(int size) {
        if (this.shouldResizeBuffer(size)) {
            this.yBuffer = new int[size];
        }
    }

    public void capture() {

        int startPos = (int) plot.getPlotLowerBound();
        int length = plot.getWindowSize();
        StreamingDataSource dataSource = plot.getDataSource();
        for (String tag: plot.getVisibleStreams()) {
            capturedData.captureStream(tag, dataSource.getDataOf(tag), startPos, length);
        }
    }

    private boolean shouldResizeBuffer(int size) {
        return this.yBuffer == null || this.yBuffer.length != size;
    }

    private class CapturedDataSource extends FiniteLengthDataSource {
        private HashMap<String, SimpleArrayStream> data;
        private Set<String> capturedTags;

        private CapturedDataSource() {
            this.data = new HashMap<>();
        }

        public void captureStream(String tag, Stream target, int startPos, int length) {
            if (!data.containsKey(tag)) {
                data.put(tag, new SimpleArrayStream(length));
            }

            data.get(tag).replacedBy(target, startPos, length);
        }

        public void setCapturedTags(List<String> tags) {
            capturedTags = new HashSet<>(tags);
        }

        @Override
        public FiniteLengthStream getFiniteDataOf(String tag) {
            if (this.capturedTags.contains(tag)) {
                return this.data.get(tag);
            }
            return null;
        }

        @Override
        public long getCurrentLength() {
            return 0;
        }

        @Override
        public void addPresentedDataChangedListener(PresentedDataChangedListener listener) {

        }

        @Override
        public void removePresentedDataChangedListener(PresentedDataChangedListener listener) {

        }
    }
}
