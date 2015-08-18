package view.component.plugin;

import model.datasource.StreamingDataSource;
import net.razorvine.pyro.PyroProxy;
import oracle.PyOracle;
import view.component.PlotView;

import java.awt.*;

/**
 * Created by maeglin89273 on 8/17/15.
 */
public class FrequencySpectrumPlugin extends RangePlugin implements InterestedStreamVisibilityPlugin {

    private final int samplingFrequency;
    private final int transformRange;

    private PyroProxy transformOracle;

    public FrequencySpectrumPlugin(int samplingFrequency, int range) {
        super(Color.GREEN, true);
        this.samplingFrequency = samplingFrequency;
        this.transformRange = range;


    }


    @Override
    public void setPlot(PlotView plot) {
        super.setPlot(plot);

        this.transformOracle = PyOracle.getInstance().getOracle("oracle.transform");

        this.setEnabled(true);
        this.setRange(this.transformRange);
        this.setFixedRange(true);
        this.setEnabled(false);
    }

    @Override
    protected void onRangeChanged() {
        super.onRangeChanged();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public void onPresentedDataChanged() {
        super.onPresentedDataChanged();
    }

    @Override
    public void onStreamVisibilityChanged(String tag, boolean isVisible) {

    }

    public StreamingDataSource getFFTDataSource() {
        return null;
    }

    public StreamingDataSource getDWTDataSource() {
        return null;
    }
}
