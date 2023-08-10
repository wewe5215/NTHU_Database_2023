// 109062233 add class for kmeans dataset
package org.vanilladb.bench.server.procedure.ann;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
// 109062233 kmeans++||
import java.lang.Math;
import java.util.Arrays;
import java.util.Comparator;

public class DataSet {
    public static class Record{
        Double[] record;
        int clusterNo;
        public Record(Double[] record){
            this.record = record;
        }

        public void setClusterNo(Integer clusterNo) {
            this.clusterNo = clusterNo;
        }

        public int get_clusterNo(){
            return clusterNo;
        }

        public Double[] getRecord() { // HashMap<String, Double> record;
            return record;
        }
    }

    private int[] attrNames;
    private final  Record[] records;
    private final ArrayList<Integer> indicesOfCentroids = new ArrayList<>();
    private static final Random random = new Random();
    // 109062233 
    private final int num_items;
    private final int total_dim;

    public DataSet(int[] attrName , int[][] input , int total_record_num, int dim) {

        /*for(int i = 0; i < attrName.size();i ++){
            attrNames.add(i);
        }*/
        num_items = total_record_num;
        total_dim = dim;
        records = new Record[total_record_num];
        attrNames = new int[dim];

        for(int i = 0 ;  i < total_record_num ; i++){
            Double[] record = new Double[total_dim];
            for(int j = 0 ; j < total_dim ; j++){
                //Integer name = attrNames.get(j);
                Double val = Double.valueOf(input[j][i]);
                //System.out.println("name : " + name + " val : " + val);
                //record.put(name, val);
                record[j] = val;
            }
            records[i] = new Record(record);
        }
    }


    public int[][] Return_arr_output(int dim , int num_size){
        int return_arr[][] = new int[dim + 1][num_size];
        for(int i = 0 ; i < num_items ; i++){
            for(int j=0; j<total_dim; j++){
                //109062320 modify
                Double[] target = records[i].getRecord();
                return_arr[j][i] = (int) Math.round(target[j]);
            }
            return_arr[dim][i] = records[i].clusterNo;
        }

        return return_arr;
    }


    public Double meanOfAttr(int attrName, ArrayList<Integer> indices){
        Double sum = 0.0;
        for(int i : indices){
            Double[] target = records[i].getRecord();
            sum += target[attrName];
        }
        return sum / indices.size();
    }
    //109062320 modify controid to int[]
    public Double[] calculateCentroid(int clusterNo){ 
        Double[] centroid = new Double[total_dim];

        ArrayList<Integer> recsInCluster = new ArrayList<>();
        for(int i=0; i< num_items; i++){
            var record = records[i];
            if(record.clusterNo == clusterNo){
                recsInCluster.add(i);
            }
        }

        for(int i = 0;i < total_dim; i ++){
            centroid[i] = meanOfAttr(i, recsInCluster);
        }
        return centroid;
    }

    public ArrayList<Double[]> recomputeCentroids(int K){
        ArrayList<Double[]> centroids = new ArrayList<>();
        for(int i=0; i<K; i++){
            centroids.add(calculateCentroid(i));
        }
        return centroids;
    }

    public Double[] randomFromDataSet(){
        int index = random.nextInt(num_items);
        return records[index].getRecord();
    }

    public static Double euclideanDistance(Double[] a, Double[] b){ // 109062233 this can be improve by SIMD 
        // but since we finish all the stuff at the loading time , thus it is not necessary to do so
        //109062320 modify
        if(a.length != b.length){
            System.out.println("a:" + a.length + " b :" + b.length);
            return Double.POSITIVE_INFINITY;
        }

        double sum = 0.0;

        for(int i = 0;i < a.length;i ++){
            sum += Math.pow(a[i] - b[i], 2);
        }
        //109062320 add simd
        //109062320 convert hashmap to array
        //simd will decrease performance here
        /*Collection<Double> a_values = a.values();
        Collection<Double> b_values = b.values();
        
        double[] a_arr = new double[a_values.size()];
        double[] b_arr = new double[b_values.size()];
        int idx = 0;
        for(Double value : a_values) {
        	a_arr[idx] = value;
        	idx++;
        }
        idx = 0;
        for(Double value : b_values) {
        	b_arr[idx] = value;
        	idx++;
        }
        final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
        DoubleVector sum_t = DoubleVector.zero(SPECIES);
        for(int i = 0; i < SPECIES.loopBound(a_arr.length); i += SPECIES.length()){
            //VectorMask<Double> m = SPECIES.indexInRange(i, a_arr.length);
            DoubleVector va = DoubleVector.fromArray(SPECIES, a_arr, i);
            DoubleVector vb = DoubleVector.fromArray(SPECIES, b_arr, i);
            DoubleVector diff2 = va.sub(vb);
                sum_t = diff2.fma(diff2, sum_t);
        }
        sum = sum_t.reduceLanes(VectorOperators.ADD);*/

        return Math.sqrt(sum);
    }
    //109062320 modify to int[] centroid
    public Double calculateClusterSSE(Double[] centroid, int clusterNo){
        double SSE = 0.0;
        
        for(int i=0; i<num_items; i++){
            if(records[i].clusterNo == clusterNo){
                SSE += Math.pow(euclideanDistance(centroid, records[i].getRecord()), 2);
            }
        }
        return SSE;
    }

    public Double calculateTotalSSE(ArrayList<Double []> centroids){
        Double SSE = 0.0;
        for(int i = 0; i < centroids.size(); i++) {
            SSE += calculateClusterSSE(centroids.get(i), i);
        }
        return SSE;
    }

    public ArrayList<Double[]> scalar_kmeans_init(int desired_cluster){
        

        // step 1 : randomly select a point from the dataset
        ArrayList<Double []> centroids = new ArrayList<>();
        // select random from index
        int index = random.nextInt(num_items);
        System.out.println("select " + index);
        centroids.add(records[index].getRecord());
        indicesOfCentroids.add(index);

        double sum = 0.0; // this will be only called upon startup to calculate the initial centroid
        for(int i=0; i<num_items; i++){
            if(!indicesOfCentroids.contains(i)){ // point not in the center of cluster 
                double minDist = Double.MAX_VALUE;
                for(int ind : indicesOfCentroids){
                    double dist = euclideanDistance(records[i].getRecord(), records[ind].getRecord());
                    if(dist<minDist){
                        minDist = dist;
                    }
                }
                if(indicesOfCentroids.isEmpty())
                    sum = 0.0;
                sum += minDist;
            }
        }
        double phi = Math.log(sum); // this is the sum of the distance of all the points to the center of the cluster
        int phi_int = (int) Math.round(phi);
        int oversampling = (int) Math.round(Math.sqrt(desired_cluster)) * 2; // 109062233 heuristics
        System.out.println("total iter in phase 2 : " + phi_int + " sum :"  + sum + " oversampling : " + oversampling);

        for(int i=0; i<phi_int; i++){
             // compute d2 for each x_i
            System.out.println("phase 2 iter : " + i);
            double[] psi = new double[num_items];
            for(int j = 0; j < num_items; j ++) {
                if(!indicesOfCentroids.contains(j)){
                    double minDist = Double.MAX_VALUE;
                    for(int ind : indicesOfCentroids){
                        double dist = euclideanDistance(records[j].getRecord(), records[ind].getRecord());
                        if(dist<minDist){
                            minDist = dist;
                        }
                    }
                    psi[j]=minDist;
                }
            }

            // compute psi
            double phi_c = 0;
            for(int j =0; j< num_items; j++) phi_c += psi[j];

            // do the drawings
            for(int j =0; j < num_items; j++) {
                if(!indicesOfCentroids.contains(j)){
                    double p_x = oversampling*psi[i]/phi_c;
                    if(p_x >= random.nextDouble()) {
                        indicesOfCentroids.add(j);
                    }
                }
            }
        }

        System.out.println("indices of centroids : " + indicesOfCentroids.size());
        if(indicesOfCentroids.size() <= desired_cluster){
            int temp = 0;
            for(int i=0; i< indicesOfCentroids.size() ; i++){
                centroids.add(records[indicesOfCentroids.get(i)].getRecord());
                temp++;
            }
            List<Integer> random_list = new ArrayList<>();
            for (int i = 0; i < num_items ; i++) {
                random_list.add(i);
            }
            Collections.shuffle(random_list);

            for(int i=0; i< num_items; i++){
                int number = random_list.get(i);
                if(!indicesOfCentroids.contains(number)){
                    centroids.add(records[number].getRecord());
                    temp++;
                }
                if(temp >= desired_cluster) break;
            }
            return centroids;
        }
        else{
             // select initial points
            int LinkedList_idx = 0;
            int centroid_arr[] = new int[indicesOfCentroids.size()]; // 

            Map<Integer, Integer> indexToValueMap = new HashMap<>();

            for(int ind : indicesOfCentroids){
                indexToValueMap.put(ind , LinkedList_idx);
                centroid_arr[LinkedList_idx] = ind;
                LinkedList_idx++;
            }

            int[] w = new int[indicesOfCentroids.size()]; // by default all are zero

            for(int i=0; i< num_items; i++) {
                int idx = 0;
                if(!indicesOfCentroids.contains(i)){
                    double minDist = Double.MAX_VALUE;
                    for(int ind : indicesOfCentroids){
                        double dist = euclideanDistance(records[i].getRecord(), records[ind].getRecord());
                        if(dist<minDist){
                            minDist = dist;
                            idx = ind;
                        }
                    }
                    w[indexToValueMap.get(idx)]++;
                }
            }

            
            IndexedValue[] indexedValues = new IndexedValue[w.length];

            for (int i = 0; i < w.length; i++) {
                indexedValues[i] = new IndexedValue(w[i], i);
            }
            Arrays.sort(indexedValues, Comparator.comparingInt(IndexedValue::getValue));
            int temp = 0;
            for (IndexedValue indexedValue : indexedValues) {
                if(temp >= desired_cluster) break;
                int true_point = centroid_arr[indexedValue.getIndex()];
                centroids.add(records[true_point].getRecord());
                temp++;
            }
            return centroids;
        }
    }

    // used in itialization
    static class IndexedValue {
        private final int value;
        private final int index;

        public IndexedValue(int value, int index) {
            this.value = value;
            this.index = index;
        }

        public int getValue() {
            return value;
        }

        public int getIndex() {
            return index;
        }
    }

    public Double[] calculateWeighedCentroid(){ 
        double sum = 0.0; // this will be only called upon startup to calculate the initial centroid
        for(int i=0; i< num_items ; i++){
            if(!indicesOfCentroids.contains(i)){ // point not in the center of cluster 
                double minDist = Double.MAX_VALUE;
                for(int ind : indicesOfCentroids){
                    double dist = euclideanDistance(records[i].getRecord(), records[ind].getRecord());
                    if(dist<minDist){
                        minDist = dist;
                    }
                }
                if(indicesOfCentroids.isEmpty())
                    sum = 0.0;
                sum += minDist;
            }
        }
        

        double threshold = sum * random.nextDouble(); // it will generate (0,sum) -> at it's first all , sum will be inf
        // the purpose for this is to find a point that is far away from the center of the cluster

        // 109062233 [optimization] add randomness
        List<Integer> random_list = new ArrayList<>();
        for (int i = 0; i < num_items ; i++) {
            random_list.add(i);
        }
        Collections.shuffle(random_list);

        for(int i=0; i< num_items ; i++){
            int number = random_list.get(i);
            if(!indicesOfCentroids.contains(number)){
                double minDist = Double.MAX_VALUE;
                for(int ind : indicesOfCentroids){
                    double dist = euclideanDistance(records[number].getRecord(), records[ind].getRecord());
                    if(dist<minDist)
                        minDist = dist;
                }
                threshold -= minDist;

                if(threshold < 0){
                    indicesOfCentroids.add(number);
                    return records[number].getRecord();
                }
            }
        }
        //109062320 add null double
        //I'not sure if this is okay or not
        Double[] ret = new Double[total_dim];
        return ret;
    }

    public int[] getAttrNames() {
        return attrNames;
    }

    public Record[] getRecords() {
        return records;
    }

    public ArrayList<Integer> getindicesOfCentroids(){
        return indicesOfCentroids;
    }
}
