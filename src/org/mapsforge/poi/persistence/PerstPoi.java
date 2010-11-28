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

import java.io.IOException;

import org.garret.perst.Persistent;
import org.garret.perst.PerstInputStream;
import org.garret.perst.PerstOutputStream;
import org.garret.perst.SelfSerializable;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.poi.PoiCategory;
import org.mapsforge.poi.PointOfInterest;

/**
 * Implementation of the {@link PointOfInterest} interface for perst embedded database.
 * 
 * @author weise
 * 
 */
class PerstPoi extends Persistent implements PointOfInterest, SelfSerializable {

	long id;
	int latitude;
	int longitude;
	String name;
	String url;
	PerstCategory category;

	private GeoCoordinate geoCoordinate;

	PerstPoi() {
		/* required by perst */
	}

	PerstPoi(PointOfInterest poi, PerstCategory category) {
		this.geoCoordinate = new GeoCoordinate(poi.getLatitude(), poi.getLongitude());
		this.id = poi.getId();
		this.latitude = this.geoCoordinate.getLatitudeE6();
		this.longitude = this.geoCoordinate.getLongitudeE6();
		this.name = poi.getName();
		this.url = poi.getUrl();
		this.category = category;

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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

	@Override
	public void pack(PerstOutputStream out) throws IOException {
		out.writeLong(this.id);
		out.writeInt(this.latitude);
		out.writeInt(this.longitude);
		out.writeString(this.name);
		out.writeString(this.url);
		out.writeObject(this.category);
	}

	@Override
	public void unpack(PerstInputStream in) throws IOException {
		this.id = in.readLong();
		this.latitude = in.readInt();
		this.longitude = in.readInt();
		this.name = in.readString();
		this.url = in.readString();
		this.category = (PerstCategory) in.readObject();
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
		if (geoCoordinate == null) {
			this.geoCoordinate = new GeoCoordinate(latitude, longitude);
		}
		return geoCoordinate.getLatitude();
	}

	@Override
	public double getLongitude() {
		if (geoCoordinate == null) {
			this.geoCoordinate = new GeoCoordinate(latitude, longitude);
		}
		return geoCoordinate.getLongitude();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getUrl() {
		return url;
	}

}
