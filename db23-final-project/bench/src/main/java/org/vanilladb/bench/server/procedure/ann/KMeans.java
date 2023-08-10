// 109062233 [add class]

package org.vanilladb.bench.server.procedure.ann;
import java.util.LinkedList;
import java.util.ArrayList;
import org.vanilladb.bench.server.procedure.ann.DataSet;

//109062320 set all LinkedList<HashMap<Integer,Double>> to LinkedList<Double[]>
public class KMeans {

    static final Double PRECISION = 1000.0; // [109062233] it's hyperparameter that can be changed
    private int num_items;
    //109062320 comment the following line
    //private LinkedList<Double[]> final_centroids;
    private int[][] k_center;
    /* K-Means++ implementation, initializes K centroids from data */
    static ArrayList<Double[]> kmeanspp(DataSet data, int K){
        // this is k means++ algorithm

        /*LinkedList<HashMap<String,Double>> centroids = new LinkedList<>();
        centroids.add(data.randomFromDataSet());
        for(int i=1; i<K; i++){
            centroids.add(data.calculateWeighedCentroid());
        }*/
        // implement k means++|| algorithm
        ArrayList<Double[]> centroids = data.scalar_kmeans_init(K);

        return centroids;
    }

    public int[][] Get_k_center(){
        return k_center;
    }

    /* K-Means itself, it takes a dataset and a number K and adds class numbers
    * to records in the dataset */
    private ArrayList<Double[]> kmeans(DataSet data, int K){
        // select K initial centroids
        ArrayList<Double[]> centroids = kmeanspp(data, K);
        // 109062233 [debug used]
        System.out.println(centroids.size());
        for(var centroid : centroids){
            System.out.println(centroid.toString());
        }
        // initialize Sum of Squared Errors to max, we'll lower it at each iteration
        Double SSE = Double.MAX_VALUE;
        int iter = 0;
        while (iter < 100) {
            System.out.println("iteration : " + iter++);
            // assign observations to centroids
            org.vanilladb.bench.server.procedure.ann.DataSet.Record[] records = data.getRecords();
            // for each record
            for(int i = 0 ; i < num_items ; i++){
                Double minDist = Double.MAX_VALUE;
                // find the centroid at a minimum distance from it and add the record to its cluster
                for(int j=0; j<centroids.size(); j++){
                    Double dist = DataSet.euclideanDistance(centroids.get(j), records[i].getRecord());
                    // System.out.println("dist : " + dist);
                    if(dist<minDist){
                        minDist = dist;
                        records[i].setClusterNo(j);
                    }
                }
                //System.out.println("records' cluster no : " + record.get_clusterNo());
            }

            // recompute centroids according to new cluster assignments
            centroids = data.recomputeCentroids(K);

            // exit condition, SSE changed less than PRECISION parameter

            Double newSSE = data.calculateTotalSSE(centroids);
            System.out.println("old sse : " + SSE + " new sse : " + newSSE + " diff : " + (SSE-newSSE));
            if(SSE-newSSE <= PRECISION){
                break;
            }
            SSE = newSSE;
        }
        return centroids;
    }

    public int[][] start_up_kMeans(int total_record_num , int input_k , int[] attrNames , int[][] input_data, int dim) {
        num_items = total_record_num;
        DataSet data = new DataSet(attrNames , input_data , total_record_num, dim);
        // cluster
        //System.out.println(data.getRecords().size());
        ArrayList<Double[]> final_centroids = kmeans(data, input_k);
        k_center = new int[input_k][attrNames.length];
        for(int i=0; i<input_k; i++){
            for(int j=0; j<attrNames.length; j++){
                //109062320 apply double ver
                Double[] arr = final_centroids.get(i);
                k_center[i][j] = arr[j].intValue();
                //k_center[i][j] = final_centroids.get(i).get(attrNames.get(j)).intValue();
            }
        }
        return data.Return_arr_output(attrNames.length , total_record_num);
    }

}
