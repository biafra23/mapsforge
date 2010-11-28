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
package org.mapsforge.preprocessing.graph.osm2rg.osmxml;

import java.sql.Timestamp;

import org.mapsforge.core.GeoCoordinate;

/**
 * This object represents a osm node element. Only used as callback parameter by the osm xml
 * parser. This allows for parsing on an object level by hiding some xml specific details.
 */
public class OsmNode extends OsmElement {

	private final double longitude, latitude;

	/**
	 * @param id
	 *            the osm id of this node.
	 * @param longitude
	 *            the longitude in degrees.
	 * @param latitude
	 *            the latitude in degrees.
	 */
	public OsmNode(long id, double longitude, double latitude) {
		super(id);
		this.longitude = longitude;
		this.latitude = latitude;
	}

	/**
	 * @param id
	 *            the osm id of this node.
	 * @param timestamp
	 *            time of last modification.
	 * @param user
	 *            user of last modification.
	 * @param visible
	 *            the visibility.
	 * @param longitude
	 *            the longitude in degrees.
	 * @param latitude
	 *            the latitude in degrees.
	 */
	public OsmNode(long id, Timestamp timestamp, String user, boolean visible,
			double longitude, double latitude) {
		super(id, timestamp, user, visible);
		this.longitude = longitude;
		this.latitude = latitude;
	}

	/**
	 * @return returns longitude in degrees.
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @return returns latitude in degrees.
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param other
	 *            another node.
	 * @return the vincenty distance to another node in meters.
	 */
	public double distance(OsmNode other) {
		GeoCoordinate a = new GeoCoordinate(latitude, longitude);
		GeoCoordinate b = new GeoCoordinate(other.latitude, other.longitude);
		return a.sphericalDistance(b);
	}
}
