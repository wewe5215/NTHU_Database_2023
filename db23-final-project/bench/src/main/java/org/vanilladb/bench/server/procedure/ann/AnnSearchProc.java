package org.vanilladb.bench.server.procedure.ann;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.vanilladb.bench.server.param.ann.AnnSearchParamHelper;
import org.vanilladb.bench.server.procedure.StoredProcedureUtils;
import org.vanilladb.core.query.algebra.Scan;
import org.vanilladb.core.sql.VectorConstant;
import org.vanilladb.core.sql.distfn.DistanceFn;
import org.vanilladb.core.sql.distfn.EuclideanFn;
import org.vanilladb.core.sql.storedprocedure.StoredProcedure;
import org.vanilladb.core.storage.tx.Transaction;


// add comparable 
class Pair implements Comparable<Pair> {
    Integer key;
    float value;

    public Pair(Integer key, float value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public int compareTo(Pair other) {
        return Float.compare(this.value, other.value);
    }
}



public class AnnSearchProc extends StoredProcedure<AnnSearchParamHelper> {

    private static final int top_K_cluster = 20; // 109062233 [note] extremely important hyperparameter !!!!
    private static final int each_in_K_cluster = 2; // 109062233 [note] extremely important hyperparameter !!!!
    private static final int top_k = 20;
    public AnnSearchProc() {
        super(new AnnSearchParamHelper());
    }
    
    @Override
    protected void executeSql() {
        AnnSearchParamHelper paramHelper = getHelper();
        VectorConstant query = paramHelper.getQuery();
        Transaction tx = getTransaction();

        // calculate clusters
        Set<Integer> nearestNeighbors = new HashSet<>();
        int[] query_emb = query.asJavaVal();
        int count = 0;
        int num_dim = paramHelper.getDim();
        int clusters_id[] =  new int[top_K_cluster];
        String CenterQuery = "SELECT c_id , i_emb FROM " + paramHelper.getCenterTableName();
        Scan near_center = StoredProcedureUtils.executeQuery(CenterQuery, tx);
        HashMap<Integer, int[]> id_iemb_list = new HashMap<>();
        near_center.beforeFirst();
        while (near_center.next()) {
            id_iemb_list.put((int) near_center.getVal("c_id").asJavaVal() , (int[]) near_center.getVal("i_emb").asJavaVal());
        }
        near_center.close(); // we avoid close such that the tx doesn't end 

        List<Pair> pairs = new ArrayList<>(); 
        for (Integer key : id_iemb_list.keySet()) {
            int[] value = id_iemb_list.get(key);
            float diff = 0;
            float cluster_sum = 0;
            // [important] reminder "use SIMD to accelerate "  6/19
            // version1
            /*final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_256;
            for(int i = 0 ; i < SPECIES.loopBound(value.length) ; i += SPECIES.length()){
                IntVector va = IntVector.fromArray(SPECIES, value, i);
                IntVector vb = IntVector.fromArray(SPECIES, query_emb, i);
                IntVector diff2 = va.sub(vb);
                IntVector square = diff2.mul(diff2);
                long red_sum = square.reduceLanesToLong(VectorOperators.ADD);
                cluster_sum += (double)red_sum;
            }*/
            //109062320
            /*final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_256;
            double[] a_arr = Arrays.stream(value).asDoubleStream().toArray();
            double[] b_arr = Arrays.stream(query_emb).asDoubleStream().toArray();
            DoubleVector sum = DoubleVector.zero(SPECIES);
            for(int i = 0 ; i < SPECIES.loopBound(value.length) ; i += SPECIES.length()){
                DoubleVector va = DoubleVector.fromArray(SPECIES, a_arr, i);
                DoubleVector vb = DoubleVector.fromArray(SPECIES, b_arr, i);
                DoubleVector diff2 = va.sub(vb);
                sum = diff2.fma(diff2, sum);

            }
            cluster_sum = sum.reduceLanes(VectorOperators.ADD);*/
            for(int i = 0; i < num_dim; i++) {
                diff = (value[i] - query_emb[i]);
                cluster_sum += diff * diff;
            } 
            //System.out.println("cluster " + key + " sum " + cluster_sum);
            // Add elements to the list
            pairs.add(new Pair(key, cluster_sum));
        }
        // Sort the list based on the second int value
        Collections.sort(pairs);
        int cnt_k = 0;
        //System.out.println("start output k nearest clusters");
        for (Pair pair : pairs) {
            if (cnt_k >= top_K_cluster) break;
            clusters_id[cnt_k++] = pair.key;
            //System.out.println("cluster " + pair.key + " sum " + pair.value);
        }

        // in top k clusters

        for(int i = 0 ; i < top_K_cluster ; i++) {
            int cluster_num = clusters_id[i];
            //System.out.println("cluster " + cluster_num );
            String newQuery = "SELECT i_id , i_emb FROM " + "cluster_" + cluster_num ;
            Scan nearestNeighborScan = StoredProcedureUtils.executeQuery(newQuery, tx);
            nearestNeighborScan.beforeFirst();
            float sum = Float.MAX_VALUE;
            int id = -1;
            while (nearestNeighborScan.next()) {
                int value[] = (int[]) nearestNeighborScan.getVal("i_emb").asJavaVal();
                int temp_id = (int) nearestNeighborScan.getVal("i_id").asJavaVal();
                float temp_sum = 0;
                // since we want to select 1 nearest neighbor in each cluster , we need to calculate the distance between query and each item in the cluster
                // and we use O(n) to do so
                // [important] reminder "use SIMD to accelerate " 6/19
                 //109062320
                //performance will decrease if we apply simd here
                /*final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_256;
                double[] a_arr = Arrays.stream(value).asDoubleStream().toArray();
                double[] b_arr = Arrays.stream(query_emb).asDoubleStream().toArray();
                DoubleVector sum_t = DoubleVector.zero(SPECIES);
                for(int j = 0 ; j < SPECIES.loopBound(value.length) ; j += SPECIES.length()){
                    DoubleVector va = DoubleVector.fromArray(SPECIES, a_arr, j);
                    DoubleVector vb = DoubleVector.fromArray(SPECIES, b_arr, j);
                    DoubleVector diff2 = va.sub(vb);
                    sum_t = diff2.fma(diff2, sum_t);

                }
                temp_sum = sum_t.reduceLanes(VectorOperators.ADD);*/

                for(int j = 0; j < num_dim; j++) {
                    float diff = (value[j] - query_emb[j]);
                    temp_sum += diff * diff;
                }
                if(sum > temp_sum) {
                    sum = temp_sum;
                    id = temp_id;
                }
            }
            /*System.out.println("cluster " + cluster_num + "sum " + sum + " id " + id);
            System.out.println("query : " + query.toString() + " best value : " + best_value.toString());*/
            count++;
            nearestNeighbors.add(id);
            nearestNeighborScan.close();
        }

        if (count == 0)
            throw new RuntimeException("True nearest neighbor query execution failed for " + query.toString());
        
        paramHelper.setNearestNeighbors(nearestNeighbors);
    }
}
