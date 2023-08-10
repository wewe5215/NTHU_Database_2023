/*******************************************************************************
 * Copyright 2016, 2017 vanilladb.org contributors
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
package org.vanilladb.core.query.algebra.materialize;
import org.vanilladb.core.query.algebra.Plan;
import org.vanilladb.core.query.algebra.Scan;
// 109062143 add
import org.vanilladb.core.sql.Schema;
import org.vanilladb.core.storage.metadata.statistics.Histogram;
import org.vanilladb.core.sql.Type;

/**
 * The interface implemented by each query plan. There is a Plan class for each
 * relational algebra operator.
 */
public class ExplainPlan implements Plan {

    private Plan p;
	private Schema schema;
	//private Histogram hist;
    /**
	 * Creates a new project node in the query tree, having the specified
	 * subquery and field list.
	 * 
	 * @param p
	 *            the subquery
	 */
	public ExplainPlan(Plan p) {
		this.p = p;
		 
		//this.hist = p.histogram();
		schema = new Schema();
	}

	/**
	 * Creates a project scan for this query.
	 * 
	 * @see Plan#open()
	 */
	@Override
	public Scan open() {
		
		Scan s = p.open();
		
		return new ExplainScan(s,toString());
	}

	/**
	 * Estimates the number of block accesses in the projection, which is the
	 * same as in the underlying query.
	 * 
	 * @see Plan#blocksAccessed()
	 */
	@Override
	public long blocksAccessed() {
		return p.blocksAccessed();
	}

	/**
	 * Returns the schema of the projection, which is taken from the field list.
	 * 
	 * @see Plan#schema()
	 */
	@Override
	public Schema schema() {
		//109062320 move addfield
		// add the query-plan
		this.schema.addField("query-plan", Type.VARCHAR(500));
		return this.schema;
	}

	/**
	 * Returns the histogram that approximates the join distribution of the
	 * field values of query results.
	 * 
	 * @see Plan#histogram()
	 */
	@Override
	public Histogram histogram() {
		return p.histogram();
	}

	/**
	 * Returns an estimate of the number of records in the query's output table.
	 * 
	 * @see Plan#recordsOutput()
	 */
	@Override
	public long recordsOutput() {
		// 109062233 [mod]
		return 1;
	}

	@Override
	public String toString(){
		StringBuilder result = new StringBuilder();
		Scan s = p.open();
		int rec_num = 0;
		s.beforeFirst();
		while(s.next())rec_num++;
		s.close();
		result.append("\n" + p.toString() + "Actual #recs: " + rec_num);
		return result.toString(); 
	}

}
