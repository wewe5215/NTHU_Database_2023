package org.vanilladb.core.sql.distfn;

import org.vanilladb.core.sql.VectorConstant;

public class EuclideanFn extends DistanceFn {

    public EuclideanFn(String fld) {
        super(fld);
    }

    @Override
    protected double calculateDistance(VectorConstant vec) {
        double sum = 0;
        for (int i = 0; i < vec.dimension(); i++) {
            double diff = query.get(i) - vec.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
    /* simd will affect performance here
        @Override
    protected double calculateDistance(VectorConstant vec) {
        double sum = 0;
        double[] a_arr = Arrays.stream(vec.asJavaVal()).asDoubleStream().toArray();
        double[] b_arr = Arrays.stream(this.query.asJavaVal()).asDoubleStream().toArray();
        final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_256;
        DoubleVector sum_t = DoubleVector.zero(SPECIES);
        for(int i = 0; i < SPECIES.loopBound(a_arr.length); i += SPECIES.length()){
            DoubleVector va = DoubleVector.fromArray(SPECIES, a_arr, i);
            DoubleVector vb = DoubleVector.fromArray(SPECIES, b_arr, i);
            DoubleVector diff2 = va.sub(vb);
            sum_t = diff2.fma(diff2, sum_t);
        }
        sum = sum_t.reduceLanes(VectorOperators.ADD);
        return Math.sqrt(sum);
    }
    */
    
}
