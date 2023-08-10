package org.vanilladb.bench.server.param.ann;

import org.vanilladb.core.sql.Schema;
import org.vanilladb.core.sql.storedprocedure.SpResultRecord;
import org.vanilladb.core.sql.storedprocedure.StoredProcedureHelper;

public class AnnTestbedLoaderParamHelper implements StoredProcedureHelper {

    private static final String TABLES_DDL[] = new String[1];
    // 109062233 add the following three variables
    private static final String Cluster_center[] = new String[1];
    private static final int K_cluster = 400; // 109062233 [note] extremely important hyperparameter !!!!
    private static final String Cluster_table[] = new String[K_cluster];
    
    private int numOfItems, numDimension;

    public String[] getTableSchemas() {
        return TABLES_DDL;
    }

    /* 1090622233 [add] */
    public String[] getClusterSchemas() {
        return Cluster_table;
    }

    public String[] getClusterCenter() {
        return Cluster_center;
    }

    /* 109062233 end adding */

    public int getNumberOfItems() {
        return numOfItems;
    }

    public int getVecDimension() {
        return numDimension;
    }

    @Override
    public void prepareParameters(Object... pars) {
        numOfItems = (Integer) pars[0];
        numDimension = (Integer) pars[1];
        TABLES_DDL[0] = "CREATE TABLE items (i_id INT, i_emb VECTOR(" + numDimension + "), i_name VARCHAR(24))";
        // 109062233 add the following few lines
        Cluster_center[0] = "CREATE TABLE cluster_center (c_id INT, i_emb VECTOR(" + numDimension + "))";
        for(int i = 0 ; i < K_cluster ; i++) {
            Cluster_table[i] = "CREATE TABLE cluster_" + String.valueOf(i) + " (i_id INT , i_emb VECTOR(" + numDimension + ") , i_name VARCHAR(24))";
        }
    }

    @Override
    public Schema getResultSetSchema() {
        return new Schema();
    }

    @Override
    public SpResultRecord newResultSetRecord() {
        return new SpResultRecord();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }
}
