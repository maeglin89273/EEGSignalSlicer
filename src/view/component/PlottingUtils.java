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

    public static void loadYBuffer(double coordinateHeight, int plotHeight, double[] data, int[] yBuffer, int startIndex) {
        for (int i = 0; i < yBuffer.length; i++) {
            yBuffer[i] = panToPlotCoordinate(plotHeight, scaleIntoWindow(coordinateHeight, plotHeight, data[i + (int) startIndex]));
        }
    }

    public static int panToPlotCoordinate(int plotHeight, double value) {
        return (int) Math.round(-value + plotHeight / 2.0);
    }

    public static double scaleIntoWindow(double coordinateHeight, int plotHeight, double value) {
        return plotHeight * value / (coordinateHeight);
    }
}
