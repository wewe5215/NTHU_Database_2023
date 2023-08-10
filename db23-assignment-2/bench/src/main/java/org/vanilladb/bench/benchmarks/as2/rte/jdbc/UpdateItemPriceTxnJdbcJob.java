package org.vanilladb.bench.benchmarks.as2.rte.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vanilladb.bench.benchmarks.as2.As2BenchConstants;
import org.vanilladb.bench.remote.SutResultSet;
import org.vanilladb.bench.remote.jdbc.VanillaDbJdbcResultSet;
import org.vanilladb.bench.rte.jdbc.JdbcJob;
import org.vanilladb.bench.server.param.as2.UpdateItemPriceProcParamHelper;
public class UpdateItemPriceTxnJdbcJob implements JdbcJob {
	private static Logger logger = Logger.getLogger(UpdateItemPriceTxnJdbcJob.class
			.getName());
	
	@Override
	public SutResultSet execute(Connection conn, Object[] pars) throws SQLException {
		UpdateItemPriceProcParamHelper paramHelper = new UpdateItemPriceProcParamHelper();
		paramHelper.prepareParameters(pars);
		
		// Output message
		StringBuilder outputMsg = new StringBuilder("[");
		
		// Execute logic
		try {
			Statement statement = conn.createStatement();
			ResultSet rs = null;
			String name;
			double price;
			// SELECT
			for (int i = 0; i < paramHelper.getUpdateCount(); i++) {
				int iid = paramHelper.getUpdateItemId(i);
				String sql = "SELECT i_name, i_price FROM item WHERE i_id = " + iid;
				//`SELECT` the name and the price of the item
				
				//109062320 add begin
				rs = statement.executeQuery(sql);
				rs.beforeFirst();
				if (rs.next()) {
					name = rs.getString("i_name");
					price = rs.getDouble("i_price");
					
				} else
					throw new RuntimeException("cannot find the record with i_id = " + iid);
				// UPDATE
				double raise_price = paramHelper.getRaisePrice(i);
				double final_price;
				if(price > As2BenchConstants.MAX_PRICE){
					final_price = As2BenchConstants.MIN_PRICE;
				}
				else{
					final_price = price + raise_price;
				}
					
				sql = "UPDATE item SET i_price = " + final_price + " WHERE i_id = " + iid;
				int result = statement.executeUpdate(sql);
				if(result != 1){
					throw new RuntimeException("cannot UPDATE the item with i_id = " + iid + "i_price = " + final_price);
				}
				
				outputMsg.append(String.format("%s 's price change from %f to %f",name, price, final_price));
				//add end -----------------------------
				rs.close();
			}
			
			conn.commit();
			
			outputMsg.deleteCharAt(outputMsg.length() - 2);
			outputMsg.append("]");
			
			return new VanillaDbJdbcResultSet(true, outputMsg.toString());
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING))
				logger.warning(e.toString());
			return new VanillaDbJdbcResultSet(false, "");
		}
	}
}
