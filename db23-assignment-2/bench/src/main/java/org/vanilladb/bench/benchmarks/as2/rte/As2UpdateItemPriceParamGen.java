package org.vanilladb.bench.benchmarks.as2.rte;

import java.util.ArrayList;

import org.vanilladb.bench.benchmarks.as2.As2BenchConstants;
import org.vanilladb.bench.benchmarks.as2.As2BenchTransactionType;
import org.vanilladb.bench.rte.TxParamGenerator;
import org.vanilladb.bench.util.BenchProperties;
import org.vanilladb.bench.util.RandomValueGenerator;
public class As2UpdateItemPriceParamGen implements TxParamGenerator<As2BenchTransactionType> {
	
	// 109062320 add
	private static final double READ_WRITE_TX_RATE;
	private static final int TOTAL_UPDATE_COUNT;
	static {
		READ_WRITE_TX_RATE = BenchProperties.getLoader()
				.getPropertyAsDouble(As2BenchmarkRte.class.getName() + ".READ_WRITE_TX_RATE", 0.5);
		TOTAL_UPDATE_COUNT = BenchProperties.getLoader()
				.getPropertyAsInteger(As2UpdateItemPriceParamGen.class.getName() + ".TOTAL_UPDATE_COUNT", 10);
	}

	@Override
	public As2BenchTransactionType getTxnType() {
		return As2BenchTransactionType.UPDATE_ITEM_PRICE;
	}

	@Override
	public Object[] generateParameter() {
		RandomValueGenerator rvg = new RandomValueGenerator();
		ArrayList<Object> paramList = new ArrayList<Object>();
		
		// Set read count
		paramList.add(TOTAL_UPDATE_COUNT);
		for (int i = 0; i < TOTAL_UPDATE_COUNT; i++)
			paramList.add(rvg.number(1, As2BenchConstants.NUM_ITEMS));

		return paramList.toArray(new Object[0]);
	}
}

