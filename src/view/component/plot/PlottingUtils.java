package view.component.plot;

import model.datasource.Stream;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public final class PlottingUtils {

    private static Map<String, Color[]> colorMapping = new HashMap<>();
    public static final int COLOR_NORMAL = 0;
    public static final int COLOR_TRANSLUCENT = 1;
    public static final int COLOR_DARKER = 2;

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

    public static Color hashStringToColor(String string, int type) {
        if (!colorMapping.containsKey(string)) {

            int hash = PlottingUtils.saltString(string).hashCode();

            int r = (hash & 0xFF0000) >> 16;
            int g = (hash & 0x00FF00) >> 8;
            int b = hash & 0x0000FF;
            Color newColor = new Color(r / 255f, g / 255f, b / 255f);
            Color translucentColor = new Color(r / 255f, g / 255f, b / 255f, 0.4f);
            colorMapping.put(string, new Color[] {newColor, translucentColor, newColor.darker()});
        }

        return colorMapping.get(string)[type];
    }

    public static Color hashStringToColor(String string) {
        return hashStringToColor(string, COLOR_NORMAL);
    }

    private static String saltString(String string) {
        final String SALT = "RGB?HSL";
        float strHash = string.hashCode();
        StringBuilder sb = new StringBuilder(string);
        for (int i = 0; i < SALT.length(); i++) {
            sb.append(SALT.charAt(Math.abs(((Float)(strHash / SALT.charAt(i))).hashCode() % SALT.length())));
            sb.append(string);
        }

        return sb.toString();
    }


}
