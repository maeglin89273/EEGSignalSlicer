package view.component.trainingview.phasepanel.basecomponent;

import java.util.Map;

/**
 * Created by maeglin89273 on 9/4/15.
 */
public interface HasStructuredValueComponent {
    public abstract void setEnabled(boolean enabled);
    public abstract boolean isValueReady();
    public abstract Object getStructuredValue();

    public static String encodeToDictKey(String name) {
        return name.toLowerCase().replace(":", "").replaceAll("[\\s\\-]", "_");
    }
}
