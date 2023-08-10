/*******************************************************************************
 * Copyright 2016, 2018 vanilladb.org contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.vanilladb.bench.benchmarks.as2.rte;

import org.vanilladb.bench.StatisticMgr;
import org.vanilladb.bench.benchmarks.as2.As2BenchTransactionType;
import org.vanilladb.bench.remote.SutConnection;
import org.vanilladb.bench.rte.RemoteTerminalEmulator;
import org.vanilladb.bench.util.BenchProperties;

public class As2BenchmarkRte extends RemoteTerminalEmulator<As2BenchTransactionType> {
	//add 109062320
	private static final double READ_WRITE_TX_RATE;
	static {
		READ_WRITE_TX_RATE = BenchProperties.getLoader()
				.getPropertyAsDouble(As2BenchmarkRte.class.getName() + ".READ_WRITE_TX_RATE", 0.5);
	}
	
	private As2BenchmarkTxExecutor read_executor;
	private As2BenchmarkTxExecutor update_executor;

	public As2BenchmarkRte(SutConnection conn, StatisticMgr statMgr, long sleepTime) {
		super(conn, statMgr, sleepTime);
		read_executor = new As2BenchmarkTxExecutor(new As2ReadItemParamGen());
		update_executor = new As2BenchmarkTxExecutor(new As2UpdateItemPriceParamGen());
	}
	
	protected As2BenchTransactionType getNextTxType() {
		double random_val = Math.random();
		//109062320 (different from 109062233's version)
		//if READ_WRITE_TX_RATE == 1 --> 100% read
		//then READ_WRITE_TX_RATE == 0.25 --> 0.25 read,0.75 write
		if(random_val <= READ_WRITE_TX_RATE){
			return As2BenchTransactionType.READ_ITEM;
		}
		else{
			return As2BenchTransactionType.UPDATE_ITEM_PRICE;
		}
		
	}
	
	protected As2BenchmarkTxExecutor getTxExeutor(As2BenchTransactionType type) {
		if(type == As2BenchTransactionType.READ_ITEM)
			return read_executor;
		else
			return update_executor;
	}
}
