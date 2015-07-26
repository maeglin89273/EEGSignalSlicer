package model.datasource;

import model.filter.ButterworthFilter;
import model.filter.Filter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 7/21/15.
 */
public class EEGChannels extends FilteredDataSource {

    public EEGChannels(double[][] originalRawData) {
        super(convertToMap(originalRawData));

        this.addFilter(ButterworthFilter.NOTCH_60HZ);
        this.addFilter(ButterworthFilter.BANDPASS_1_50HZ);

    }

    private static Map<String, FiniteLengthStream> convertToMap(double[][] originalRawData) {
        Map<String, FiniteLengthStream> dataMap = new HashMap<>(originalRawData.length);
        for (int i = 1; i <= originalRawData.length; i++) {
            dataMap.put(Integer.toString(i), new SimpleArrayStream(originalRawData[i - 1]));
        }
        return dataMap;
    }

    public void setNotchFilter(Filter filter) {
        this.replaceFilter(0, filter);
    }

    public void setBandpassFilter(Filter filter) {
        this.replaceFilter(1, filter);
    }

    public double[][] getOriginalRawDataInArray() {
        double[][] dataArray = new double[this.originalData.size()][];
        for (int i = 1; i <= dataArray.length; i++) {
            dataArray[i - 1] = ((SimpleArrayStream)(this.originalData.get(Integer.toString(i)))).toArray();
        }
        return dataArray;
    }
}
