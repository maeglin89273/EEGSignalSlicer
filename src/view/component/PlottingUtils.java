package view.component;

import model.datasource.Stream;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public final class PlottingUtils {

    public enum Baseline {
        BOTTOM, MIDDLE
    }

    public static void loadXBuffer(double coordinateWidth, int plotWidth, int[] xBuffer) {
        double interval = plotWidth / coordinateWidth;
        for (int i = 0; i < xBuffer.length; i++) {
            xBuffer[i] = (int) Math.round(i * interval);
        }
    }

    public static void loadYBuffer(Baseline baseline, double coordinatePeak, int plotHeight, Stream data, int startIndex, int[] yBuffer, int length) {
        for (int i = 0; i < length; i++) {
            yBuffer[i] = mapY(baseline, coordinatePeak, plotHeight, data.get(i + startIndex));
        }
    }

    public static int loadYBuffer(Baseline baseline, double coordinatePeak, int plotHeight, Stream data, int startIndex, int[] yBuffer) {
        int length = (int) (data.getCurrentLength() - startIndex);
        length = length > yBuffer.length? yBuffer.length: length;
        loadYBuffer(baseline, coordinatePeak, plotHeight, data, startIndex, yBuffer, length);
        return length;
    }

    public static int mapY(Baseline baseline, double coordinatePeak, int plotHeight, double value) {
        return panToPlotCoordinate(baseline, plotHeight, scaleIntoWindow(baseline, coordinatePeak, plotHeight, value));
    }

    public static int panToPlotCoordinate(Baseline baseline, int plotHeight, double value) {
        if (baseline == Baseline.BOTTOM) {
            return (int) Math.round(-Math.abs(value) + plotHeight);
        } else {
            return (int) Math.round(-value + plotHeight / 2.0);
        }
    }

    public static double scaleIntoWindow(Baseline baseline, double coordinatePeak, int plotHeight, double value) {
        if (baseline == Baseline.BOTTOM) {
            return plotHeight * value / (coordinatePeak);
        } else {
            return plotHeight * value / (2 * coordinatePeak);
        }
    }

}
