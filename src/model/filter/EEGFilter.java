package model.filter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by maeglin89273 on 7/24/15.
 */
public class EEGFilter extends BandpassFilter {

    public static final Map<String, Filter> EEG_BANDPASS_FILTER_TABLE = new HashMap<String, Filter>();

    private static final double[] ALPHA_A = {1.0, -2.355934631131582e+000, 1.941257088655214e+000, -7.847063755334187e-001, 1.999076052968340e-001};
    private static final double[] ALPHA_B = {2.001387256580675e-001, 0.0, -4.002774513161350e-001, 0.0, 2.001387256580675e-001};
    public static final BandpassFilter ALPHA = new BandpassFilter(ALPHA_A, ALPHA_B);

    private static final double[] BETA_A = {1.0, -2.355934631131582e+000, 1.941257088655214e+000, -7.847063755334187e-001, 1.999076052968340e-001};
    private static final double[] BETA_B = {2.001387256580675e-001, 0.0, -4.002774513161350e-001, 0.0, 2.001387256580675e-001};
    public static final BandpassFilter BETA = new BandpassFilter(BETA_A, BETA_B);

    private static final double[] THETA_A = {1.0, -2.355934631131582e+000, 1.941257088655214e+000, -7.847063755334187e-001, 1.999076052968340e-001};
    private static final double[] THETA_B = {2.001387256580675e-001, 0.0, -4.002774513161350e-001, 0.0, 2.001387256580675e-001};
    public static final BandpassFilter THETA = new BandpassFilter(THETA_A, THETA_B);

    private static final double[] GAMMA_A = {1.0, -2.355934631131582e+000, 1.941257088655214e+000, -7.847063755334187e-001, 1.999076052968340e-001};
    private static final double[] GAMMA_B = {2.001387256580675e-001, 0.0, -4.002774513161350e-001, 0.0, 2.001387256580675e-001};
    public static final BandpassFilter GAMMA = new BandpassFilter(GAMMA_A, GAMMA_B);

    static {
        EEG_BANDPASS_FILTER_TABLE.put("Alpha", ALPHA);
        EEG_BANDPASS_FILTER_TABLE.put("Beta", BETA);
        EEG_BANDPASS_FILTER_TABLE.put("THETA", THETA);
        EEG_BANDPASS_FILTER_TABLE.put("Gamma", GAMMA);
    }

    private EEGFilter(double[] a, double[] b) {
        super(a, b);
    }


}
