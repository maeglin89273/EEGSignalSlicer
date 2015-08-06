package model.filter;

import model.datasource.FiniteLengthDataSource;
import model.datasource.FiniteLengthStream;
import model.datasource.MutableFiniteLengthStream;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public interface Filter {
    public static final Filter EMPTY_FILTER = new Filter() {
        @Override
        public MutableFiniteLengthStream filter(FiniteLengthStream input, MutableFiniteLengthStream output) {
            output.replacedBy(input, 0, input.intLength());
            return output;
        }
    };

    public MutableFiniteLengthStream filter(FiniteLengthStream input, MutableFiniteLengthStream output);
}
