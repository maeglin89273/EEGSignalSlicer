package view.component.trainingview.phasepanel.basecomponent;

import java.util.Map;

/**
 * Created by maeglin89273 on 9/8/15.
 */
public interface NamedStructuredValueComponent extends HasStructuredValueComponent {
    @Override
    public Map<String, Object> getStructuredValue();
}
