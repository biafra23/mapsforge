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
package org.mapsforge.preprocessing.graph.gui.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class create a connection to the embedded sqlite database by using the jdbc wrapper.
 * 
 * @author kunis
 */
public class JDBCConnection {

	private Connection con;

	/**
	 * The constructor to create a jdbc connection to the embedded sqlite database.
	 */
	public JDBCConnection() {
		try {
			Class.forName("org.sqlite.JDBC");
			// the sqlite .db file is placed at mapsforge\res\con\gui\
			this.con = DriverManager.getConnection("jdbc:sqlite:sqlite_conf.db");
			// 
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Returns the JDBC database connection.
	 * 
	 * @return JDBCConnection the JDBC database connection.
	 */
	public Connection getConnection() {
		return this.con;
	}

	/**
	 * Sets the JDBC database connection.
	 * 
	 * @param con
	 *            the JDBC database connection that should be set.
	 */
	public void setConnection(Connection con) {
		this.con = con;
	}

}
