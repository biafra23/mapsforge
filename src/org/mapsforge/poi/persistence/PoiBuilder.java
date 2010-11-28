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

import org.mapsforge.poi.PoiCategory;
import org.mapsforge.poi.PointOfInterest;

/**
 * Can be uses to build instances of {@link PointOfInterest}. Implements the builder pattern.
 * 
 * @author weise
 * 
 */
public class PoiBuilder {

	private final double latitude;
	private final double longitude;
	private final long id;
	private String name;
	private String url;
	private final PoiCategory category;

	/**
	 * @param id
	 *            of the point of interest
	 * @param latitude
	 *            of the point of interest
	 * @param longitude
	 *            of the point of interest
	 * @param category
	 *            of the point of interest
	 */
	public PoiBuilder(long id, double latitude, double longitude, PoiCategory category) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.id = id;
		this.category = category;
	}

	/**
	 * @param id
	 *            of the point of interest
	 * @param latitude
	 *            of the point of interest
	 * @param longitude
	 *            of the point of interest
	 * @param name
	 *            of the point of interest
	 * @param url
	 *            of the point of interest
	 * @param category
	 *            of the point of interest
	 */
	public PoiBuilder(long id, double latitude, double longitude, String name, String url,
			PoiCategory category) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.id = id;
		this.category = category;
		this.name = name;
		this.url = url;
	}

	/**
	 * @param name
	 *            of the point of interest
	 * @return this {@link PoiBuilder} to allow method chaining.
	 */
	public PoiBuilder setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * @param url
	 *            of the point of interest
	 * @return this {@link PoiBuilder} to allow method chaining.
	 */
	public PoiBuilder setUrl(String url) {
		this.url = url;
		return this;
	}

	/**
	 * @return a newly created PointOfInterest object.
	 */
	public PointOfInterest build() {
		return new PostGisPoi(id, latitude, longitude, name, url, category);
	}

}
