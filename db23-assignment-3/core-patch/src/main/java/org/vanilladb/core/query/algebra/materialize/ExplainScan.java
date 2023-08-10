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

import org.vanilladb.core.sql.Record;
import org.vanilladb.core.sql.VarcharConstant;
import org.vanilladb.core.query.algebra.Scan;
import org.vanilladb.core.sql.Constant;

/**
 * The interface will be implemented by each query scan. There is a Scan class
 * for each relational algebra operator.
 * 
 * <p>
 * The {@link #beforeFirst()} method must be called before {@link #next()}.
 * </p>
 */
public class ExplainScan implements Scan {
	private Scan s;
	private String ans;
	// 1096062233 [mod]
	private Boolean isExplain = false;
	/**
	 * Creates a project scan having the specified underlying scan and field
	 * list.
	 * 
	 * @param s
	 *            the underlying scan
	 * @param fieldList
	 *            the list of field names
	 */
	public ExplainScan(Scan s,String ans) {
		this.s = s;
		this.ans = ans;
		this.isExplain = false;
	}

	@Override
	public void beforeFirst() {
		s.beforeFirst();
	}

	@Override
	public boolean next() {
		// 109062233 [mod]
		if(isExplain){
			return false;
		}
		else{
			isExplain = true;
			return true;
		}
	}

	@Override
	public void close() {
		s.close();
	}

	@Override
	public Constant getVal(String fldName) {
		if (fldName.equals("query-plan")) {
			return new VarcharConstant(ans);
		} else
			throw new RuntimeException("field " + fldName + " not found.");
	}

	/**
	 * Returns true if the specified field is query-plan.
	 * 
	 * @see Scan#hasField(java.lang.String)
	 */
	@Override
	public boolean hasField(String fldName) {
		// 109062233 [mod]
		return this.s.hasField(fldName);
	}
	
}
