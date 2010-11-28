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
package org.mapsforge.core;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.mapsforge.preprocessing.graph.model.gui.DatabaseProperties;

public class DBConnection {

	public final static int DEFAULT_FETCH_SIZE = 1000;

	private Connection conn;

	public DBConnection(String propertiesFile) throws Exception {
		Properties props = new Properties();
		props.load(new FileInputStream(propertiesFile));

		conn = DriverManager.getConnection("jdbc:postgresql://" + props.getProperty("db.host")
				+ "/" + props.getProperty("db.database"), props.getProperty("db.user"), props
				.getProperty("db.password"));
		conn.setAutoCommit(false);
	}

	public DBConnection(String hostName, String dbName, String username, String password,
			int port) throws SQLException {
		this.conn = getJdbcConnectionPg(hostName, port, dbName, username, password);
		conn.setAutoCommit(false);
	}

	public DBConnection(DatabaseProperties dbProperties) throws SQLException {
		this.conn = getJdbcConnectionPg(dbProperties.getHost(), dbProperties.getPort(),
				dbProperties.getDbName(), dbProperties.getUsername(), dbProperties
						.getPassword());
		conn.setAutoCommit(false);
	}

	public Connection getConnection() {
		return conn;
	}

	public static Connection getJdbcConnectionPg(String hostName, int port, String dbName,
			String username, String password) throws SQLException {
		String url = "jdbc:postgresql://" + hostName + "/" + dbName;
		return DriverManager.getConnection(url, username, password);
	}

	public static PreparedStatement getResultStreamingPreparedStatemet(Connection conn,
			String sql) throws SQLException {
		return getResultStreamingPreparedStatemet(conn, sql, DEFAULT_FETCH_SIZE);
	}

	public static PreparedStatement getResultStreamingPreparedStatemet(Connection conn,
			String sql, int fetchSize) throws SQLException {
		PreparedStatement pstmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		pstmt.setFetchSize(fetchSize);
		return pstmt;
	}

	public static Statement getResultStreamingStatemet(Connection conn) throws SQLException {
		return getResultStreamingStatemet(conn, DEFAULT_FETCH_SIZE);
	}

	public static Statement getResultStreamingStatemet(Connection conn, int fetchSize)
			throws SQLException {
		Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY);
		stmt.setFetchSize(fetchSize);
		return stmt;
	}

}
