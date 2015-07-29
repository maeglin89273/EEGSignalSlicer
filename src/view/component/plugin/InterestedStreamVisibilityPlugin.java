package view.component.plugin;

import java.util.Collection;

/**
 * Created by maeglin89273 on 7/28/15.
 */
public interface InterestedStreamVisibilityPlugin extends PlotPlugin {
    void onStreamVisibilityChanged(String tag, boolean isVisible);
}
