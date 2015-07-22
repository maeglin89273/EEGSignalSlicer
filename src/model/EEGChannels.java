package model;

/**
 * Created by maeglin89273 on 7/21/15.
 */
public class EEGChannels {
    private final double[][] originalRawData;
    private double[][] presentedData;
    private double[][] cachedDataSpace;

    private Filter notchFilter;
    private Filter bandpassFilter;

    public EEGChannels(double[][] originalRawData) {
        this.originalRawData = originalRawData;
        System.out.println(this.originalRawData[0].length + " data loaded");
        this.cachedDataSpace = new double[8][originalRawData[0].length];
        this.presentedData = new double[8][];
        this.notchFilter = Filter.NOTCH_60HZ;
        this.bandpassFilter = Filter.BANDPASS_1_50HZ;
    }

    public long getDataLength() {
        return this.originalRawData[0].length;
    }


    public double[] getChannel(int channelNum) {
        channelNum--;
        if (presentedData[channelNum] == null) {
            presentedData[channelNum] = filterChannel(channelNum);
        }
        return presentedData[channelNum];

    }

    public double[][] getOriginalRawData() {
        return this.originalRawData;
    }

    public void setNotchFilter(Filter filter) {
        clearPresentedData();
        this.notchFilter = filter;
    }

    public void setBandpassFilter(Filter filter) {
        clearPresentedData();
        this.bandpassFilter = filter;
    }

    private double[] filterChannel(int channelIndex) {
        double[] filteredData = this.notchFilter.filter(this.originalRawData[channelIndex], claimCachedChannel(channelIndex));
        return this.bandpassFilter.filter(filteredData, filteredData);
    }

    private void clearPresentedData() {
        for (int i = 0; i < this.presentedData.length; i++) {
            this.presentedData[i] = null;
        }
    }

    private double[] claimCachedChannel(int i) {
        return this.cachedDataSpace[i];
    }
}
