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
package org.vanilladb.bench;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatisticMgr {
	private static Logger logger = Logger.getLogger(StatisticMgr.class.getName());

	private static class TxnStatistic {
		private BenchTransactionType mType;
		private int txnCount = 0;
		private long totalResponseTimeNs = 0;

		public TxnStatistic(BenchTransactionType txnType) {
			this.mType = txnType;
		}

		public BenchTransactionType getmType() {
			return mType;
		}

		public void addTxnResponseTime(long responseTime) {
			txnCount++;
			totalResponseTimeNs += responseTime;
		}

		public int getTxnCount() {
			return txnCount;
		}

		public long getTotalResponseTime() {
			return totalResponseTimeNs;
		}
	}

	private File outputDir;
	private int timelineGranularity;
	private List<TxnResultSet> resultSets = new ArrayList<TxnResultSet>();
	private List<BenchTransactionType> allTxTypes;
	private String fileNamePostfix = "";
	private long recordStartTime = -1;

	public StatisticMgr(Collection<BenchTransactionType> txTypes, File outputDir, int timelineGranularity) {
		this.allTxTypes = new LinkedList<BenchTransactionType>(txTypes);
		this.outputDir = outputDir;
		this.timelineGranularity = timelineGranularity;
	}

	public StatisticMgr(Collection<BenchTransactionType> txTypes, File outputDir, String namePostfix,
			int timelineGranularity) {
		this.allTxTypes = new LinkedList<BenchTransactionType>(txTypes);
		this.outputDir = outputDir;
		this.fileNamePostfix = namePostfix;
		this.timelineGranularity = timelineGranularity;
	}

	/**
	 * We use the time that this method is called at as the start time for
	 * recording.
	 */
	public synchronized void setRecordStartTime() {
		if (recordStartTime == -1)
			recordStartTime = System.nanoTime();
	}

	public synchronized void processTxnResult(TxnResultSet trs) {
		if (recordStartTime == -1)
			recordStartTime = trs.getTxnEndTime();
		resultSets.add(trs);
	}

	public synchronized void outputReport() {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss"); // E.g. "20220324-200824"
			String fileName = formatter.format(Calendar.getInstance().getTime());
			if (fileNamePostfix != null && !fileNamePostfix.isEmpty())
				fileName += "-" + fileNamePostfix; // E.g. "20220324-200824-postfix"

			outputDetailReport(fileName);
			//109062320 add
			outputDetailReportCSV(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (logger.isLoggable(Level.INFO))
			logger.info("Finish creating benchmark report.");
	}

	private void outputDetailReport(String fileName) throws IOException {
		Map<BenchTransactionType, TxnStatistic> txnStatistics = new HashMap<BenchTransactionType, TxnStatistic>();
		Map<BenchTransactionType, Integer> abortedCounts = new HashMap<BenchTransactionType, Integer>();

		for (BenchTransactionType type : allTxTypes) {
			txnStatistics.put(type, new TxnStatistic(type));
			abortedCounts.put(type, 0);
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputDir, fileName + ".txt")))) {
			// First line: total transaction count
			writer.write("# of txns (including aborted) during benchmark period: " + resultSets.size());
			writer.newLine();

			// Detail latency report
			for (TxnResultSet resultSet : resultSets) {
				if (resultSet.isTxnIsCommited()) {
					// Write a line: {[Tx Type]: [Latency]}
					writer.write(resultSet.getTxnType() + ": "
							+ TimeUnit.NANOSECONDS.toMillis(resultSet.getTxnResponseTime()) + " ms");
					writer.newLine();

					// Count transaction for each type
					TxnStatistic txnStatistic = txnStatistics.get(resultSet.getTxnType());
					txnStatistic.addTxnResponseTime(resultSet.getTxnResponseTime());

				} else {
					writer.write(resultSet.getTxnType() + ": ABORTED");
					writer.newLine();

					// Count transaction for each type
					Integer count = abortedCounts.get(resultSet.getTxnType());
					abortedCounts.put(resultSet.getTxnType(), count + 1);
				}
			}
			writer.newLine();

			// Last few lines: show the statistics for each type of transactions
			int abortedTotal = 0;
			for (Entry<BenchTransactionType, TxnStatistic> entry : txnStatistics.entrySet()) {
				TxnStatistic value = entry.getValue();
				int abortedCount = abortedCounts.get(entry.getKey());
				abortedTotal += abortedCount;
				long avgResTimeMs = 0;

				if (value.txnCount > 0) {
					avgResTimeMs = TimeUnit.NANOSECONDS.toMillis(value.getTotalResponseTime() / value.txnCount);
				}

				writer.write(value.getmType() + " - committed: " + value.getTxnCount() + ", aborted: " + abortedCount
						+ ", avg latency: " + avgResTimeMs + " ms");

				writer.newLine();
			}

			// Last line: Total statistics
			int finishedCount = resultSets.size() - abortedTotal;
			double avgResTimeMs = 0;
			if (finishedCount > 0) { // Avoid "Divide By Zero"
				for (TxnResultSet rs : resultSets)
					avgResTimeMs += rs.getTxnResponseTime() / finishedCount;
			}
			writer.write(String.format("TOTAL - committed: %d, aborted: %d, avg latency: %d ms", finishedCount,
					abortedTotal, Math.round(avgResTimeMs / 1000000)));
		}
	}


	private void outputDetailReportCSV(String fileName) throws IOException {
		Map<BenchTransactionType, TxnStatistic> txnStatistics = new HashMap<BenchTransactionType, TxnStatistic>();
		Map<BenchTransactionType, Integer> abortedCounts = new HashMap<BenchTransactionType, Integer>();

		for (BenchTransactionType type : allTxTypes) {
			txnStatistics.put(type, new TxnStatistic(type));
			abortedCounts.put(type, 0);
		}
		//109062320 change sub-filename
		//goal : produce **another report** with average, minimum, 
		//maximum, 25th, median, 75th latency 
		//along with throughput "in every 5 seconds"
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputDir, fileName + ".csv")))) {
			// First line: total transaction count
			//writer.write("# of txns (including aborted) during benchmark period: " + resultSets.size());
			writer.write("time(sec), throughput(txs), avg_latency(ms), min(ms), max(ms), 25th_lat(ms), median_lat(ms), 75th_lat(ms)");
			writer.newLine();
			//--------109062320 add more information start ----------
			//revise latency
			long time_sec = 0;
			int throughput = 0; //need to initialize after every period
			//average latency = total_latency / committed_amount
			float ResponseTime = 0;//ms
			float Total_latency = 0;//need to initialize after every period
			float lat_ave = 0;//need to initialize after every period
			int committed_amount = 0;//need to initialize after every period
			//need a list to record latency within desired time
			List<Float>record_latency = new ArrayList<Float>();
			//minimum latency, maximum latency
			float lat_min = Float.MAX_VALUE;//need to initialize after every period
			float lat_max = Float.MIN_VALUE;//need to initialize after every period
			
			//25th,50th,75th latency
			float lat_25th = 0;//need to initialize after every period
			float lat_median = 0;//need to initialize after every period
			float lat_75th = 0;//need to initialize after every period

			//record the amount we iterate through resultSet
			//for fear of the results in resultSet run out and 
			//it has passed less than 5 sec
			int resultSet_count = 0;
			long fiveSec_in_ms = 5 * 1000;
			for (TxnResultSet resultSet : resultSets) {
				resultSet_count++;
				if (resultSet.isTxnIsCommited()) {
					// Count transaction for each type
					TxnStatistic txnStatistic = txnStatistics.get(resultSet.getTxnType());
					txnStatistic.addTxnResponseTime(resultSet.getTxnResponseTime());
					// Write a line: {[Tx Type]: [Latency]}
					//writer.write(resultSet.getTxnType() + ": "
					//		+ TimeUnit.NANOSECONDS.toMillis(resultSet.getTxnResponseTime()) + " ms");
					
					//for calculating average latency(ms)
					ResponseTime = TimeUnit.NANOSECONDS.toMillis(resultSet.getTxnResponseTime());
					Total_latency += ResponseTime;
					committed_amount++;
					//update record_latency
					record_latency.add(ResponseTime);
					//update min/max latency
					if(ResponseTime > lat_max)lat_max = ResponseTime;
					if(ResponseTime < lat_min)lat_min = ResponseTime;
					//if 5 sec pass or its the last element in the resultSet
					//[v] we have to generate a new record 
					//[v] and restart a new period --> initialize all the variables
					if(Total_latency >= fiveSec_in_ms || resultSet_count >= resultSets.size()){
						//time
						time_sec += 5;
						//throughput
						throughput = committed_amount;
						//latency
						Collections.sort(record_latency);
						//average latency
						lat_ave = Total_latency / committed_amount;
						//min/max --> already calculated 
						//25th_lat
						lat_25th = record_latency.get((record_latency.size() - 1) / 4);
						//median_lat
						lat_median = record_latency.get((record_latency.size() - 1) / 2);
						//75th_lat
						lat_75th = record_latency.get((record_latency.size() - 1) * 3 / 4);

						//write back data
						writer.write(time_sec + "," + throughput + "," + lat_ave + "," + lat_min + "," + lat_max 
						+ "," + lat_25th + "," + lat_median + "," + lat_75th);
						writer.newLine();

						//initialize
						//time --> no need to initialize it
						//throughput
						throughput = 0;
						committed_amount = 0;
						//latency
						Total_latency = 0;
						//clear list
						record_latency.clear();
						//average latency
						lat_ave = 0;
						//min/max
						lat_min = Long.MAX_VALUE;
						lat_max = Long.MIN_VALUE;
						//25th_lat
						lat_25th = 0;
						//median_lat
						lat_median = 0;
						//75th_lat
						lat_75th = 0;

					}
					
					
					

				} else {
					writer.write(resultSet.getTxnType() + ": ABORTED");
					writer.newLine();

					// Count transaction for each type
					Integer count = abortedCounts.get(resultSet.getTxnType());
					abortedCounts.put(resultSet.getTxnType(), count + 1);
				}
			}


			//--------109062320 add more information end ---------------------------
			
			/*
			// Detail latency report
			for (TxnResultSet resultSet : resultSets) {
				if (resultSet.isTxnIsCommited()) {
					// Write a line: {[Tx Type]: [Latency]}
					writer.write(resultSet.getTxnType() + ": "
							+ TimeUnit.NANOSECONDS.toMillis(resultSet.getTxnResponseTime()) + " ms");
					writer.newLine();

					// Count transaction for each type
					TxnStatistic txnStatistic = txnStatistics.get(resultSet.getTxnType());
					txnStatistic.addTxnResponseTime(resultSet.getTxnResponseTime());

				} else {
					writer.write(resultSet.getTxnType() + ": ABORTED");
					writer.newLine();

					// Count transaction for each type
					Integer count = abortedCounts.get(resultSet.getTxnType());
					abortedCounts.put(resultSet.getTxnType(), count + 1);
				}
			}
			writer.newLine();*/
			/* 
			// Last few lines: show the statistics for each type of transactions
			int abortedTotal = 0;
			for (Entry<BenchTransactionType, TxnStatistic> entry : txnStatistics.entrySet()) {
				TxnStatistic value = entry.getValue();
				int abortedCount = abortedCounts.get(entry.getKey());
				abortedTotal += abortedCount;
				long avgResTimeMs = 0;

				if (value.txnCount > 0) {
					avgResTimeMs = TimeUnit.NANOSECONDS.toMillis(value.getTotalResponseTime() / value.txnCount);
				}

				writer.write(value.getmType() + " - committed: " + value.getTxnCount() + ", aborted: " + abortedCount
						+ ", avg latency: " + avgResTimeMs + " ms");

				writer.newLine();
			}
			*/
			/* 
			// Last line: Total statistics
			int finishedCount = resultSets.size() - abortedTotal;
			double avgResTimeMs = 0;
			if (finishedCount > 0) { // Avoid "Divide By Zero"
				for (TxnResultSet rs : resultSets)
					avgResTimeMs += rs.getTxnResponseTime() / finishedCount;
			}
			writer.write(String.format("TOTAL - committed: %d, aborted: %d, avg latency: %d ms", finishedCount,
					abortedTotal, Math.round(avgResTimeMs / 1000000)));
			*/
		}
	}
}
