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


public class Profile {

	private String name;
	private String osm;
	private Transport transport;
	private String heuristic;
	private DatabaseProperties dbprops;

	public Profile(String name, String url, Transport transport, String heuristic) {
		this.name = name;
		this.osm = url;
		this.transport = transport;
		this.heuristic = heuristic;
		this.dbprops = null;
	}

	public Profile(String name, String url, Transport transport, String heuristic,
			DatabaseProperties dbProbs) {
		this.name = name;
		this.osm = url;
		this.transport = transport;
		this.heuristic = heuristic;
		this.setDbProberties(dbProbs);
	}

	// Getter

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return osm;
	}

	/**
	 * @return the transport
	 */
	public Transport getTransport() {
		return transport;
	}

	/**
	 * @return the heuristic
	 */
	public String getHeuristic() {
		return heuristic;
	}

	// Setter

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.osm = url;
	}

	/**
	 * @param transport
	 *            the transport to set
	 */
	public void setTransport(Transport transport) {
		this.transport = transport;
	}

	/**
	 * @param heuristic
	 *            the heuristic to set
	 */
	public void setHeuristik(String heuristic) {
		this.heuristic = heuristic;
	}

	@Override
	public String toString() {
		return name;
	}

	public void setDbProberties(DatabaseProperties dbprobs) {
		this.dbprops = dbprobs;
	}

	public DatabaseProperties getDbProberties() {
		return dbprops;
	}

}
