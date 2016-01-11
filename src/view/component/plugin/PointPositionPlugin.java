package view.component.plugin;

import view.component.plot.PlottingUtils;

import java.awt.*;

/**
 * Created by maeglin89273 on 8/28/15.
 */
public class PointPositionPlugin extends CoordinatePlugin {

    private double xUnit;

    public PointPositionPlugin(double xUnit) {
        this(new Color(255, 0, 0), xUnit);
    }

    public PointPositionPlugin(Color color, double xUnit) {
        super(color);
        this.setXUnit(xUnit);

    }

    @Override
    protected String getYText(double posY) {
        return  String.format("%.1f", posY);
    }

    @Override
    protected String getXText(double posX) {
        return String.format("%.1f", toXUnit(posX));
    }


    private double toXUnit(double x) {
        return this.getXUnit() * x;
    }

    public double getXUnit() {
        return this.xUnit;
    }

    public void setXUnit(double xUnit) {
        this.xUnit = xUnit;
    }

}
