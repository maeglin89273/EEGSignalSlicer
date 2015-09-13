package model.filter;

import model.datasource.FiniteLengthStream;
import model.datasource.FiniteListStream;
import model.datasource.MutableFiniteStream;
import model.filter.Filter;
import net.razorvine.pyro.PyroProxy;
import oracle.PyOracle;

import java.io.IOException;
import java.util.List;

/**
 * Created by maeglin89273 on 8/21/15.
 */
public class DomainTransformFilter implements Filter {

    public static final DomainTransformFilter FFT = new DomainTransformFilter("fft");
    public static final DomainTransformFilter WT = new DomainTransformFilter("wt");

    private PyroProxy oracle;
    private final FiniteListStream adapter;
    private final String transformationLengthCalculator;
    private String transformationName;

    public DomainTransformFilter(String transformationName) {
        this.setTransformation(transformationName);
        this.transformationLengthCalculator = "length_after_" + transformationName;
        this.oracle = PyOracle.getInstance().getOracle("transform");
        this.adapter = new FiniteListStream(0);
    }

    public PyroProxy getTransformationOracle() {
        return this.oracle;
    }

    public void setTransformation(String transformationName) {
        this.transformationName = transformationName + "_transform";
    }

    public String getTransformation() {
        return this.transformationName;
    }

    @Override
    public MutableFiniteStream filter(FiniteLengthStream input, MutableFiniteStream output) {

        try {
            List<Double> result = (List<Double>) oracle.call(this.transformationName, input.toArray());
            output.replacedBy(adapter.setUnderlyingBuffer(result), 0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return output;
    }

    @Override
    public int calculateLengthAfterFiltering(int originalLength) {
        try {
            return (Integer) oracle.call(this.transformationLengthCalculator, originalLength);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return originalLength;
    }
}
