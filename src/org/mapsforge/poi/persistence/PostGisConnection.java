/*
 * Copyright 2010 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.poi.persistence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class PostGisConnection {

	private final Connection dbConnection;
	private final Statement batchStatement;

	public PostGisConnection(Connection dbConnection) {
		this.dbConnection = dbConnection;
		try {
			this.batchStatement = dbConnection.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		} catch (SQLException e) {
			throw new IllegalArgumentException(
					"Unable to create statement for given connection");
		}
	}

	public boolean executeInsertStatement(String queryString) {
		Statement statement;
		try {
			statement = dbConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			System.out.println(queryString);
			return statement.execute(queryString);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to execute insert statement");
		}
	}

	public void addToBatch(String queryString) {
		try {
			batchStatement.addBatch(queryString);
		} catch (SQLException e) {
			throw new IllegalArgumentException("Unable to add the given queryString to batch");
		}
	}

	public int executeBatch() {
		try {
			return batchStatement.executeBatch().length;
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public ResultSet executeQuery(String queryString) {
		Statement statement;
		try {
			statement = dbConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			return statement.executeQuery(queryString);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to execute query");
		}
	}

	public void close() {
		try {
			if (!dbConnection.isClosed()) {
				dbConnection.close();
			}
		} catch (SQLException e) {
			throw new RuntimeException("Unable to close connection");
		}
	}

}
