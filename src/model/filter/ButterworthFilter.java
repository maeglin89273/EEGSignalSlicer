package model.filter;

import model.datasource.FiniteLengthStream;
import model.datasource.MutableFiniteLengthStream;

/**
 * Created by maeglin89273 on 7/21/15.
 */
public class ButterworthFilter implements Filter {
    private static final double[] NOTCH_60HZ_A = {1.000000000000000e+000, -2.467782611297853e-001, 1.944171784691352e+000, -2.381583792217435e-001, 9.313816821269039e-001};
    private static final double[] NOTCH_60HZ_B = {9.650809863447347e-001, -2.424683201757643e-001, 1.945391494128786e+000, -2.424683201757643e-001, 9.650809863447347e-001};
    public static final ButterworthFilter NOTCH_60HZ = new ButterworthFilter(NOTCH_60HZ_A, NOTCH_60HZ_B);

    private static final double[] BANDPASS_1_50HZ_A = {1.0, -2.355934631131582e+000, 1.941257088655214e+000, -7.847063755334187e-001, 1.999076052968340e-001};
    private static final double[] BANDPASS_1_50HZ_B = {2.001387256580685e-1, 0.0, -4.002774513161369e-1, 0.0, 2.001387256580685e-1};
    public static final ButterworthFilter BANDPASS_1_50HZ = new ButterworthFilter(BANDPASS_1_50HZ_A, BANDPASS_1_50HZ_B);

    private static final double[] BANDPASS_7_13HZ_A = {1.0, -3.678895469764040e+000, 5.179700413522124e+000, -3.305801890016702e+000, 8.079495914209149e-001};
    private static final double[] BANDPASS_7_13HZ_B = {5.129268366104263e-003, 0.0, -1.025853673220853e-002, 0.0, 5.129268366104263e-003};
    public static final ButterworthFilter BANDPASS_7_13HZ = new ButterworthFilter(BANDPASS_7_13HZ_A, BANDPASS_7_13HZ_B);

    private static final double[] BANDPASS_15_50HZ_A = {1.0, -2.137430180172061e+000, 2.038578008108517e+000, -1.070144399200925e+000, 2.946365275879138e-001};
    private static final double[] BANDPASS_15_50HZ_B = {1.173510367246093e-001, 0.0, -2.347020734492186e-001, 0.0, 1.173510367246093e-001};
    public static final ButterworthFilter BANDPASS_15_50HZ = new ButterworthFilter(BANDPASS_15_50HZ_A, BANDPASS_15_50HZ_B);

    private double[] a;
    private double[] b;
    protected ButterworthFilter(double[] a, double[] b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public MutableFiniteLengthStream filter(FiniteLengthStream data, MutableFiniteLengthStream output) {
        int Nback = this.b.length;
        double[] prev_y = new double[Nback];
        double[] prev_x = new double[Nback];

        //step through presentedData points
        for (int i = 0; i < data.getCurrentLength(); i++) {
            //shift the previous outputs
            for (int j = Nback-1; j > 0; j--) {
                prev_y[j] = prev_y[j-1];
                prev_x[j] = prev_x[j-1];
            }

            //add in the new point
            prev_x[0] = data.get(i);

            //compute the new presentedData point
            double out = 0;
            for (int j = 0; j < Nback; j++) {
                out += this.b[j]*prev_x[j];
                if (j > 0) {
                    out -= this.a[j]*prev_y[j];
                }
            }

            //save output value
            prev_y[0] = out;
            output.set(i, out);
        }
        return output;
    }
}
