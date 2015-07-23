package view.component.plugin;

import java.awt.event.MouseEvent;
import java.util.Set;

/**
 * Created by maeglin89273 on 7/23/15.
 */
public interface InteractivePlotPlugin extends PlotPlugin {
    public Set<String> getInterestedActions();

    public interface MouseInteractionPlugin extends InteractivePlotPlugin {
        public boolean onMouseEvent(String action, MouseEvent event);
    }
}
