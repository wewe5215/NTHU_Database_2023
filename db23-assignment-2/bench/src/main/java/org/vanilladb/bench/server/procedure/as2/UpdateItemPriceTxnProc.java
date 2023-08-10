package org.vanilladb.bench.server.procedure.as2;

import org.vanilladb.bench.server.param.as2.UpdateItemPriceProcParamHelper;
import org.vanilladb.bench.server.procedure.StoredProcedureHelper;
import org.vanilladb.core.query.algebra.Scan;
import org.vanilladb.core.sql.storedprocedure.StoredProcedure;
import org.vanilladb.core.storage.tx.Transaction;
import org.vanilladb.bench.benchmarks.as2.As2BenchConstants;
public class UpdateItemPriceTxnProc extends StoredProcedure<UpdateItemPriceProcParamHelper> {

	public UpdateItemPriceTxnProc() {
		super(new UpdateItemPriceProcParamHelper());
	}

	@Override
	protected void executeSql() {
		UpdateItemPriceProcParamHelper paramHelper = getParamHelper();
		Transaction tx = getTransaction();
		
		// SELECT
		for (int idx = 0; idx < paramHelper.getUpdateCount(); idx++) {
			int iid = paramHelper.getUpdateItemId(idx);
			Scan s = StoredProcedureHelper.executeQuery(
				"SELECT i_name, i_price FROM item WHERE i_id = " + iid,
				tx
			);
			//109062320 add
			double final_price, raise_price, price;
			String name;
			s.beforeFirst();
			if (s.next()) {
				name = (String) s.getVal("i_name").asJavaVal();
				price = (Double) s.getVal("i_price").asJavaVal();
				raise_price = paramHelper.getRaisePrice(idx);
				
				
			} else
				throw new RuntimeException("Cloud not find item record with i_id = " + iid);

			s.close();
			//update
			if(price > As2BenchConstants.MAX_PRICE){
				final_price = As2BenchConstants.MIN_PRICE;
			}
			else{
				final_price = price + raise_price;
			}
			StoredProcedureHelper.executeUpdate("UPDATE item SET i_price = " + final_price + " WHERE i_id = " + iid,
			 tx);
			paramHelper.setItemName(name, idx);
			paramHelper.setItemPrice(price, idx);
		}
	}
}

