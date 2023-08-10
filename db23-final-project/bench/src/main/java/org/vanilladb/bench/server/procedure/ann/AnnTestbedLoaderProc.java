package org.vanilladb.bench.server.procedure.ann;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.vanilladb.bench.benchmarks.ann.AnnBenchConstants;
import org.vanilladb.bench.server.param.ann.AnnTestbedLoaderParamHelper;
import org.vanilladb.bench.server.procedure.StoredProcedureUtils;
import org.vanilladb.core.server.VanillaDb;
import org.vanilladb.core.sql.VectorConstant;
import org.vanilladb.core.sql.storedprocedure.StoredProcedure;
import org.vanilladb.core.storage.tx.Transaction;
import org.vanilladb.core.storage.tx.recovery.CheckpointTask;
import org.vanilladb.core.storage.tx.recovery.RecoveryMgr;
// 109062233
import java.util.LinkedList;
import java.util.HashMap;

public class AnnTestbedLoaderProc extends StoredProcedure<AnnTestbedLoaderParamHelper> {
    // 109062233 [debug -> add logger]
    private static Logger logger = Logger.getLogger(AnnTestbedLoaderProc.class.getName());
    private static final int k_cluster = 400; // 109062233 [note] extremely important hyperparameter !!!!
    public AnnTestbedLoaderProc() {
        super(new AnnTestbedLoaderParamHelper());
    }

    @Override
    protected void executeSql() {
        if (logger.isLoggable(Level.INFO))
            logger.info("Start loading testbed...");

        // turn off logging set value to speed up loading process
        RecoveryMgr.enableLogging(false);

        dropOldData();
        createSchemas();

        // Generate item records
        generateItems(1, getHelper().getNumberOfItems());
        

        if (logger.isLoggable(Level.INFO))
            logger.info("Loading completed. Flush all loading data to disks...");

        RecoveryMgr.enableLogging(true);

        // Create a checkpoint
        CheckpointTask cpt = new CheckpointTask();
        cpt.createCheckpoint();

        // Delete the log file and create a new one
        VanillaDb.logMgr().removeAndCreateNewLog();

        if (logger.isLoggable(Level.INFO))
            logger.info("Loading procedure finished.");
    }

    private void dropOldData() {
        if (logger.isLoggable(Level.WARNING))
            logger.warning("Dropping is skipped.");
    }

    private void createSchemas() {
        AnnTestbedLoaderParamHelper paramHelper = getHelper();
        Transaction tx = getTransaction();

        if (logger.isLoggable(Level.INFO))
            logger.info("Creating tables...");

        for (String sql : paramHelper.getTableSchemas())
            StoredProcedureUtils.executeUpdate(sql, tx);
        
        // 109062233 [add relative table]
        for (String sql : paramHelper.getClusterSchemas())
            StoredProcedureUtils.executeUpdate(sql, tx);

        for (String sql : paramHelper.getClusterCenter())
            StoredProcedureUtils.executeUpdate(sql, tx);
            

        // Skip adding indexes

        // if (logger.isLoggable(Level.INFO))
        //     logger.info("Creating indexes...");

        // // Create indexes
        // for (String sql : paramHelper.getIndexSchemas())
        //     StoredProcedureUtils.executeUpdate(sql, tx);
        
        if (logger.isLoggable(Level.FINE))
            logger.info("Finish creating schemas.");
    }

    private void generateItems(int startIId, int endIId) {
        // 109062233 [add debug]

        if (true)
            logger.info("Start populating items from i_id " + startIId + " to " + endIId + "...");
        // 109062233 [new instance]
        KMeans k_mean = new KMeans();

        if (logger.isLoggable(Level.FINE))
            logger.info("Start populating items from i_id " + startIId + " to " + endIId + "...");

        Transaction tx = getTransaction();

        int dim = getHelper().getVecDimension();
        
        VectorConstant.seed(AnnBenchConstants.DATASET_SEED); // Pseudonumber random generator

        // 109062233 setup parameter
        // 109062320 set attrName to 1d array
        int[] attrNames = new int[dim];
        /* for(int i = 0; i < dim; i++) {
            attrNames.add(i);
        }*/
        int [][] passed_into = new int[dim + 1][endIId - startIId + 1]; 
        int cnt_ann = 0;
        String sql;
        for (int i = startIId; i <= endIId; i++) {
            int iid = i;
            String iname = "'item" + iid + "'";
            VectorConstant vec = new VectorConstant(dim);
            // 109062233 [add parametere to do knn]
            int[] vec_in_vec = vec.asJavaVal();
            for(int j = 0; j < dim; j++) {
                passed_into[j][cnt_ann] = vec_in_vec[j]; // dim 
            }
            cnt_ann++;
            sql = "INSERT INTO items(i_id, i_emb, i_name) VALUES (" + iid + ", " + vec.toString() + ", " + iname + ")";
            // 109062233 the output looks like this: 
            //INSERT INTO items(i_id, i_emb, i_name) VALUES (98, [3964, 5269, 9357, 6574, 4733, 4112, 1352, 3779, 4532, 9694, 3562, 4529, 4492, 5224, 3342, 6880], 'item98')
            // logger.info(sql);
            StoredProcedureUtils.executeUpdate(sql, tx);
        }

        // 109062233 [we can add index upon insertion ...]
        int input_data_without_id[][] = k_mean.start_up_kMeans(endIId - startIId + 1, k_cluster , attrNames , passed_into, dim);
        // 109062233 [uncomment to see the result of k-means]
        /*for(int i = 0 ; i < endIId - startIId + 1 ; i++) {
            String print_out = "Item : ";
            print_out +=  (i + startIId) + " ";
            for(int j = 0 ; j < dim; j++) {
                print_out += input_data_without_id[j][i] + " ";
            }
            print_out += "is at cluster : " + input_data_without_id[dim][i];
            logger.info(print_out);
        }*/

        for(int i = startIId ; i <= endIId ; i++) { // 1 to num , but we start from 0 
            int iid = i;
            String iname = "'item" + iid + "'";
            int[] rawVector = new int[dim];
            for (int j = 0; j < dim; j++) {
                rawVector[j] = input_data_without_id[j][iid-1]; // thus we need as -1 here
            }
            VectorConstant vec = new VectorConstant(rawVector);
            int cluster_num = input_data_without_id[dim][iid-1];
            String sql_cluster = "INSERT INTO cluster_" + String.valueOf(cluster_num) +
            "(i_id, i_emb, i_name) VALUES (" + iid + ", " + vec.toString() + ", " + iname + ")";
            //System.out.println(sql_cluster);
            StoredProcedureUtils.executeUpdate(sql_cluster, tx);
        }


        int center_info[][] = k_mean.Get_k_center(); // [input_k][attrNames.size()];

        // 109062233 [uncomment to see the result of k-means]
        for(int i=0; i< k_cluster ; i++){
            String print_center = " ";
            print_center += "Cluster " + i + " : ";
            for(int j = 0 ; j < attrNames.length ; j++){
                print_center += center_info[i][j] + " ";
            }
            System.out.println(print_center);
        }

        for(int i=0; i< k_cluster ; i++){
            int[] rawVector = new int[dim];
            for(int j = 0 ; j < dim ; j++){
                rawVector[j] = center_info[i][j]; // fuck ... i , j 
            }
            VectorConstant vec = new VectorConstant(rawVector);
            String sql_center = "INSERT INTO cluster_center(c_id, i_emb) VALUES (" + i + ", " + vec.toString() + ")";
            System.out.println(sql_center);
            StoredProcedureUtils.executeUpdate(sql_center, tx);
        }


        if (logger.isLoggable(Level.FINE))
            logger.info("Finish populating items.");
    }
}
