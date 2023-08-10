package org.vanilladb.core.storage.tx.concurrency.conservative;

import java.util.List;

import org.vanilladb.core.sql.storedprocedure.PrimaryKey;
import org.vanilladb.core.storage.file.BlockId;
import org.vanilladb.core.storage.record.RecordId;
import org.vanilladb.core.storage.tx.Transaction;
import org.vanilladb.core.storage.tx.concurrency.ConcurrencyMgr;
// 109062233 [add] debug and inheritate from SerializableConcurrencyMgr
import org.vanilladb.core.storage.tx.concurrency.SerializableConcurrencyMgr;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConservativeConcurrencyMgr extends SerializableConcurrencyMgr {
	//109062320
	//every transactions locks all objects at once
	//To acquire the read/write set atomically, 
	//you may want to let this process execute serially 
	//(e.g. in a critical section)
	// 109062233 [add]
	// 109062233 add debug
	//private static Logger logger = Logger.getLogger(ConcurrencyMgr.class.getName());
	//private static long nextTxnNumber = 1;

	public ConservativeConcurrencyMgr(long txNumber) {
		super(txNumber);
	}
	// 109062233 [mod] remove unnecessary critical section
	public boolean lockAllItems(List<PrimaryKey> readKeys, List<PrimaryKey> writKeys){
		for(PrimaryKey rKey : readKeys){
			/*if (logger.isLoggable(Level.INFO))
				logger.info("readKeys : " + rKey.getHashCode()  + " ");*/
			lockTbl.sLock(rKey, txNum);
		}


		for(PrimaryKey wKey : writKeys){
			lockTbl.xLock(wKey, txNum);
			/*if (logger.isLoggable(Level.INFO))
				logger.info("readKeys : " + wKey.getHashCode()  + " ");*/
		}
		// 109062233 to be removed later on 
		/*if (logger.isLoggable(Level.INFO))
			logger.info("txNumber : " + txNum  + "\n");*/
		return true;
	}
	@Override
	public void onTxCommit(Transaction tx) {
		// TODO releaseAll locks
		lockTbl.releaseAll(txNum, false);
	}

	@Override
	public void onTxRollback(Transaction tx) {
		// TODO Auto-generated method stub
		lockTbl.releaseAll(txNum, false);
		
	}

	@Override
	public void onTxEndStatement(Transaction tx) {
		// do nothing
		
	}

	@Override
	public void modifyFile(String fileName) {
		// do nothing
		
	}

	@Override
	public void readFile(String fileName) {
		// do nothing
		
	}

	@Override
	public void insertBlock(BlockId blk) {
		// do nothing
		
	}

	@Override
	public void modifyBlock(BlockId blk) {
		// do nothing
		
	}

	@Override
	public void readBlock(BlockId blk) {
		// do nothing
		
	}

	@Override
	public void modifyRecord(RecordId recId) {
		// do nothing
		
	}

	@Override
	public void readRecord(RecordId recId) {
		// do nothing
		
	}

	@Override
	public void modifyIndex(String dataFileName) {
		// do nothing
	}

	@Override
	public void readIndex(String dataFileName) {
		// do nothing
	}

	//109062320 add modifyLeafBlock
	@Override
	public void modifyLeafBlock(BlockId blk){
		//do nothing
	}

	//109062320 add readLeafBlock
	@Override 
	public void readLeafBlock(BlockId blk){
		//do nothing
	}

}
