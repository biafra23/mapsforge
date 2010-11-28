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

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.poi.PoiCategory;
import org.mapsforge.poi.PointOfInterest;

class PostGisPoi implements PointOfInterest {

	final long id;
	final double latitude;
	final double longitude;
	final String name;
	final String url;
	final PoiCategory category;

	public PostGisPoi(long id, double latitude, double longitude, String name, String url,
			PoiCategory category) {
		super();
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.name = name;
		this.url = url;
		this.category = category;
	}

	@Override
	public PoiCategory getCategory() {
		return category;
	}

	@Override
	public GeoCoordinate getGeoCoordinate() {
		return new GeoCoordinate(latitude, longitude);
	}

	@Override
	public GeoPoint getGeoPoint() {
		return new GeoPoint(latitude, longitude);
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public double getLatitude() {
		return latitude;
	}

	@Override
	public double getLongitude() {
		return longitude;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof PointOfInterest)) {
			return false;
		}
		PointOfInterest other = (PointOfInterest) obj;
		if (id != other.getId())
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(category.toString()).append(": ").append("name=").append(name).append(
				' ').append("url=").append(url).append(' ').append("lat=").append(latitude)
				.append(" lng=").append(longitude);
		return builder.toString();
	}

}
