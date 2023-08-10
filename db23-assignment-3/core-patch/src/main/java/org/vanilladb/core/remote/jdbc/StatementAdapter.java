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
package org.vanilladb.core.remote.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

/**
 * This class implements all of the methods of the Statement interface, by
 * throwing an exception for each one. Subclasses (such as {@link JdbcStatement}
 * ) can override those methods that it want to implement.
 */
public abstract class StatementAdapter implements Statement {
	@Override
	public void addBatch(String sql) throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public void cancel() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public void clearBatch() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public void clearWarnings() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public void close() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public boolean execute(String sql) throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public boolean execute(String sql, int autoGeneratedKeys)
			throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public boolean execute(String sql, String[] columnNames)
			throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public int[] executeBatch() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys)
			throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public int executeUpdate(String sql, int[] columnIndexes)
			throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public int executeUpdate(String sql, String[] columnNames)
			throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public Connection getConnection() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public int getFetchDirection() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public int getFetchSize() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public int getMaxRows() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public boolean getMoreResults(int current) throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public int getResultSetType() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public int getUpdateCount() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public boolean isClosed() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public boolean isPoolable() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public void setCursorName(String name) throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public void setEscapeProcessing(boolean enable) {
	}

	@Override
	public void setFetchDirection(int direction) {
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public void closeOnCompletion() throws SQLException {
		throw new SQLException("operation not implemented");
	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		throw new SQLException("operation not implemented");
	}
}