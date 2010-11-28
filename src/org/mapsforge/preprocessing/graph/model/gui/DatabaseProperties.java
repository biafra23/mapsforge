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
package org.mapsforge.preprocessing.graph.model.gui;

public class DatabaseProperties {

	private final String host;
	private final String username;
	private final String password;
	private final int port;
	private String dbname;

	public DatabaseProperties(String host, int port, String dbname, String username,
			String password) {
		this.host = host;
		this.username = username;
		this.password = password;
		this.port = port;
		this.dbname = dbname;
	}

	public String getHost() {
		return host;
	}

	public String getDbName() {
		return dbname;
	}

	public void setDbName(String dbname) {
		this.dbname = dbname;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public int getPort() {
		return port;
	}
}
