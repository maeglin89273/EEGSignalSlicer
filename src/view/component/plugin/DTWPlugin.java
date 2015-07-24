package view.component.plugin;

import java.awt.event.MouseEvent;
import java.util.Set;

/**
 * Created by maeglin89273 on 7/24/15.
 */
public class DTWPlugin extends EmptyPlotPlugin implements InteractivePlotPlugin.MousePlugin {
    @Override
    public boolean onMouseEvent(String action, MouseEvent event) {
        return false;
    }

    @Override
    public Set<String> getInterestedActions() {
        return null;
    }
}
