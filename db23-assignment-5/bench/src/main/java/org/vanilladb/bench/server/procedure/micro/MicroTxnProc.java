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
package org.vanilladb.bench.server.procedure.micro;

import org.vanilladb.bench.server.param.micro.MicroTxnProcParamHelper;
import org.vanilladb.bench.server.procedure.StoredProcedureHelper;
import org.vanilladb.core.query.algebra.Scan;
import org.vanilladb.core.sql.storedprocedure.StoredProcedure;
import org.vanilladb.core.storage.tx.Transaction;
//Modify stored procedures of the micro-benchmark 
//to accommodate read-/write-sets and use your concurrency manager
//109062320 import
import org.vanilladb.core.storage.tx.concurrency.conservative.ConservativeConcurrencyMgr;
import org.vanilladb.core.server.VanillaDb;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.vanilladb.core.sql.storedprocedure.PrimaryKey;
import java.util.Map;
import org.vanilladb.core.sql.Constant;
import org.vanilladb.core.sql.IntegerConstant;
import java.util.concurrent.locks.ReentrantLock;
public class MicroTxnProc extends StoredProcedure<MicroTxnProcParamHelper> {
	private static final ReentrantLock Latch = new ReentrantLock();
	public MicroTxnProc() {
		super(new MicroTxnProcParamHelper());
	}
	//109062320 add getReadKeys()
	public List<PrimaryKey> getReadKeys(){
		MicroTxnProcParamHelper paramHelper = getParamHelper();
		List<PrimaryKey> ReadKeyList = new ArrayList<>(paramHelper.getReadCount());
		for(int idx = 0; idx < paramHelper.getReadCount(); idx++){
			Map<String, Constant> keyEntryMap = new HashMap<String, Constant>();
			keyEntryMap.put("i_id", new IntegerConstant(paramHelper.getReadItemId(idx)));
			ReadKeyList.add(new PrimaryKey("item", keyEntryMap));
		}

		return ReadKeyList;
	}
	//109062320 add getWriteKeys()
	public List<PrimaryKey> getWriteKeys(){
		MicroTxnProcParamHelper paramHelper = getParamHelper();
		List<PrimaryKey> WriteKeyList = new ArrayList<>(paramHelper.getWriteCount());
		for(int idx = 0; idx < paramHelper.getWriteCount(); idx++){
			Map<String, Constant> keyEntryMap = new HashMap<String, Constant>();
			keyEntryMap.put("i_id", new IntegerConstant(paramHelper.getWriteItemId(idx)));
			WriteKeyList.add(new PrimaryKey("item", keyEntryMap));
		}
		return WriteKeyList;

	}

	//109062320 override prepare
	// 109062233 [delete] old method need to set the original abstract , 
	// but when we do so , the original tpcc prepare will cause error
	/*public void prepare(Object... pars) {
		// prepare parameters
		MicroTxnProcParamHelper paramHelper = getParamHelper();
		
		paramHelper.prepareParameters(pars);
		
		// create a transaction
		boolean isReadOnly = paramHelper.isReadOnly();
		Transaction tx = getTransaction();
		
	}*/
	@Override
	protected void executeSql() {
		MicroTxnProcParamHelper paramHelper = getParamHelper();
		Transaction tx = getTransaction();
		//need to be critical section
		Latch.lock();
		// 19062233 [fix] get the ConservativeConcurrencyMgr construct correctly
		ConservativeConcurrencyMgr ConservConcurMgr = new ConservativeConcurrencyMgr(tx.getTransactionNumber());
		List<PrimaryKey> readKeys = getReadKeys();
		List<PrimaryKey> writKeys = getWriteKeys();
		//System.out.printf("read: " + readKeys.size() + " write: " + writKeys.size() + "\n");
		boolean finish = ConservConcurMgr.lockAllItems(readKeys, writKeys);
		Latch.unlock();

		for (int idx = 0; idx < paramHelper.getReadCount(); idx++) {
			int iid = paramHelper.getReadItemId(idx);
			Scan s = StoredProcedureHelper.executeQuery(
				"SELECT i_name, i_price FROM item WHERE i_id = " + iid,
				tx
			);
			s.beforeFirst();
			if (s.next()) {
				String name = (String) s.getVal("i_name").asJavaVal();
				double price = (Double) s.getVal("i_price").asJavaVal();

				paramHelper.setItemName(name, idx);
				paramHelper.setItemPrice(price, idx);
			} else
				throw new RuntimeException("Cloud not find item record with i_id = " + iid);

			s.close();
		}
		
		// UPDATE
		for (int idx = 0; idx < paramHelper.getWriteCount(); idx++) {
			int iid = paramHelper.getWriteItemId(idx);
			double newPrice = paramHelper.getNewItemPrice(idx);
			StoredProcedureHelper.executeUpdate(
				"UPDATE item SET i_price = " + newPrice + " WHERE i_id =" + iid,
				tx
			);
		}
	}
}
