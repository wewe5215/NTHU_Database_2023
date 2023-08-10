package org.vanilladb.core.sql.storedprocedure;
import java.util.Map;
import org.vanilladb.core.sql.Constant;
//109062320 add PrimaryKey
//take reference on SearchKey
// 109062233 [mod] deprived old method and try the new one
public class PrimaryKey {
    private String tableName;
    private Map<String, Constant> keyEntryMap;
    private int hashCode;
    public PrimaryKey(String tableName, Map<String, Constant> keyEntryMap) {
        this.tableName = tableName;
        this.keyEntryMap = keyEntryMap;
        genHashCode();
    }

    public String getTableName() {
        return tableName;
    }

    public Map<String, Constant> getKeyEntryMap(){
        return keyEntryMap;
    }

    public int getHashCode(){
        return hashCode;
    }

    public Constant getKeyVal(String fld) {
        return keyEntryMap.get(fld);
    }

    @Override
    public boolean equals(Object obj) {
        
        if (obj == null)
			return false;
		
		if (this == obj)
			return true;
		
		if (!obj.getClass().equals(PrimaryKey.class))
			return false;
        PrimaryKey targetKey = (PrimaryKey) obj;
        // 109062233 [mod] rewrite the equals method
        if(!targetKey.getTableName().equals(this.tableName)){
            return false;
        }
        if(!targetKey.getKeyEntryMap().equals(this.keyEntryMap)){
            return false;
        }

        return true;
    }

    private void genHashCode() {
        //take reference on query.planner.opt.AccessPath
        //this.hashCode = preAp.hashCode() + newTp.hashCode();
        this.hashCode = tableName.hashCode() + keyEntryMap.hashCode();
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

}