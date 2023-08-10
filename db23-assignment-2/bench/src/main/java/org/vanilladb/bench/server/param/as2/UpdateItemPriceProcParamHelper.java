package org.vanilladb.bench.server.param.as2;

import java.security.KeyStore.PrivateKeyEntry;

import org.vanilladb.core.sql.DoubleConstant;
import org.vanilladb.core.sql.IntegerConstant;
import org.vanilladb.core.sql.Schema;
import org.vanilladb.core.sql.Type;
import org.vanilladb.core.sql.VarcharConstant;
import org.vanilladb.core.sql.storedprocedure.SpResultRecord;
import org.vanilladb.core.sql.storedprocedure.StoredProcedureHelper;

public class UpdateItemPriceProcParamHelper implements StoredProcedureHelper {

	private int UpdateCount;
	private int[] UpdateItemId;
	private String[] itemName;
	private double[] itemPrice;
	private double[] raise_price;
	public int getUpdateCount() {
		return UpdateCount;
	}

	public int getUpdateItemId(int index) {
		return UpdateItemId[index];
	}

	public void setItemName(String s, int idx) {
		itemName[idx] = s;
	}

	public void setItemPrice(double d, int idx) {
		itemPrice[idx] = d;
	}
	//109062320
	//add raise_price
	public double getRaisePrice(int idx) {
		return raise_price[idx];
	}
	@Override
	public void prepareParameters(Object... pars) {

		// Show the contents of paramters
	   //System.out.println("Params: " + Arrays.toString(pars));

		int indexCnt = 0;

		UpdateCount = (Integer) pars[indexCnt++];
		UpdateItemId = new int[UpdateCount];
		itemName = new String[UpdateCount];
		itemPrice = new double[UpdateCount];
		//109062320 add
		raise_price = new double[UpdateCount];

		for (int i = 0; i < UpdateCount; i++){
			UpdateItemId[i] = (Integer) pars[indexCnt++];
			//109062320 add
			raise_price[i] = 5 * Math.random();
			
		}
        
	}

	@Override
	public Schema getResultSetSchema() {
		Schema sch = new Schema();
		Type intType = Type.INTEGER;
		Type itemPriceType = Type.DOUBLE;
		Type itemNameType = Type.VARCHAR(24);
		sch.addField("rc", intType);
		for (int i = 0; i < itemName.length; i++) {
			sch.addField("i_name_" + i, itemNameType);
			sch.addField("i_price_" + i, itemPriceType);
		}
		return sch;
	}

	@Override
	public SpResultRecord newResultSetRecord() {
		SpResultRecord rec = new SpResultRecord();
		rec.setVal("rc", new IntegerConstant(itemName.length));
		for (int i = 0; i < itemName.length; i++) {
			rec.setVal("i_name_" + i, new VarcharConstant(itemName[i], Type.VARCHAR(24)));
			rec.setVal("i_price_" + i, new DoubleConstant(itemPrice[i]));
		}
		return rec;
	}
	//109062320 set false
	@Override
	public boolean isReadOnly() {
		return false;
	}

}
