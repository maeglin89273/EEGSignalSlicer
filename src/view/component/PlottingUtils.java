package view.component;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public final class PlottingUtils {
    public static void loadXBuffer(double coordinateWidth, int plotWidth, int[] xBuffer) {
        double interval = plotWidth / coordinateWidth;
        for (int i = 0; i < xBuffer.length; i++) {
            xBuffer[i] = (int) Math.round(i * interval);
        }
    }

    public static void loadYBuffer(double coordinateHeight, int plotHeight, double[] data, int startIndex, int[] yBuffer, int length) {
        for (int i = 0; i < yBuffer.length; i++) {
            yBuffer[i] = mapY(coordinateHeight, plotHeight, data[i + (int) startIndex]);
        }
    }

    public static void loadYBuffer(double coordinateHeight, int plotHeight, double[] data, int startIndex, int[] yBuffer) {
        loadYBuffer(coordinateHeight, plotHeight, data, startIndex, yBuffer, yBuffer.length);
    }

    public static int mapY(double coordinateHeight, int plotHeight, double value) {
        return panToPlotCoordinate(plotHeight, scaleIntoWindow(coordinateHeight, plotHeight, value));
    }

    public static int panToPlotCoordinate(int plotHeight, double value) {
        return (int) Math.round(-value + plotHeight / 2.0);
    }

    public static double scaleIntoWindow(double coordinateHeight, int plotHeight, double value) {
        return plotHeight * value / (coordinateHeight);
    }
}
