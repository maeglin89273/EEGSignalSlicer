package model.filter;

import model.datasource.FiniteLengthStream;
import model.datasource.MutableFiniteStream;

/**
 * Created by maeglin89273 on 7/22/15.
 */
public interface Filter {
    public static final Filter EMPTY_FILTER = new Filter() {
        @Override
        public MutableFiniteStream filter(FiniteLengthStream input, MutableFiniteStream output) {
            output.replacedBy(input, 0, input.intLength());
            return output;
        }

        @Override
        public int calculateLengthAfterFiltering(int originalLength) {
            return originalLength;
        }
    };

    public MutableFiniteStream filter(FiniteLengthStream input, MutableFiniteStream output);
    public int calculateLengthAfterFiltering(int originalLength);
}
