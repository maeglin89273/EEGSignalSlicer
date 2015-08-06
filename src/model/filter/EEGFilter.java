package model.filter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 7/24/15.
 */
public class EEGFilter extends ButterworthFilter {

    public static final Map<String, Filter> EEG_BANDPASS_FILTER_TABLE = new HashMap<String, Filter>();

    private static final double[] ALPHA_A = {1.0, -3.610590586940321, 5.022589156046926, -3.186702614795855, 7.797394590280967e-1};
    private static final double[] ALPHA_B = {6.867865728682671e-3, 0.0, -1.373573145736534e-2, 0.0, 6.867865728682671e-3};
    public static final ButterworthFilter ALPHA = new ButterworthFilter(ALPHA_A, ALPHA_B);

    private static final double[] BETA_A = {1.0, -2.938773563478346, 3.665078807091217, -2.240193871259719, 5.869195080611904e-1};
    private static final double[] BETA_B = {2.785976611713600e-2 , 0.0, -5.571953223427201e-2, 0.0, 2.785976611713600e-2};
    public static final ButterworthFilter BETA = new ButterworthFilter(BETA_A, BETA_B);

    private static final double[] THETA_A = {1.0, -3.859068022716297, 5.619078037334554, -3.658573035851941, 8.988589941552524e-1};
    private static final double[] THETA_B = {1.348711948356344e-3, 0.0, -2.697423896712688e-3, 0.0, 1.348711948356344e-3};
    public static final ButterworthFilter THETA = new ButterworthFilter(THETA_A, THETA_B);

    private static final double[] GAMMA_A = {1.0, -1.781138993126241, 2.182743773137794, -1.282583738175701, 5.277817298110120e-1};
    private static final double[] GAMMA_B = {3.842170469295831e-2, 0.0, -7.684340938591662e-2, 0.0, 3.842170469295831e-2};
    public static final ButterworthFilter GAMMA = new ButterworthFilter(GAMMA_A, GAMMA_B);


    static {
        EEG_BANDPASS_FILTER_TABLE.put("Alpha", Filter.EMPTY_FILTER);
        EEG_BANDPASS_FILTER_TABLE.put("Beta", BETA);
        EEG_BANDPASS_FILTER_TABLE.put("Theta", THETA);
        EEG_BANDPASS_FILTER_TABLE.put("Gamma", GAMMA);
    }

    private EEGFilter(double[] a, double[] b) {
        super(a, b);
    }


}
