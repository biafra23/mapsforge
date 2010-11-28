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
package org.mapsforge.poi.exchange;

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapsforge.poi.PoiCategory;
import org.mapsforge.poi.PointOfInterest;
import org.mapsforge.poi.persistence.PoiBuilder;

/**
 * Provides methods for generating points of interest from their geo json representation.
 * 
 * @author weise
 * 
 */
public class GeoJsonPoiReader implements IPoiReader {

	private final String geoJsonString;
	private final PoiCategory category;

	/**
	 * @param geoJsonString
	 *            geo json string containg the point of interest data.
	 * @param category
	 *            category the point of interests belong to.
	 */
	public GeoJsonPoiReader(String geoJsonString, PoiCategory category) {
		this.geoJsonString = geoJsonString;
		this.category = category;
	}

	@Override
	public Collection<PointOfInterest> read() {
		final Collection<PointOfInterest> pois;

		try {
			JSONObject geoJson = new JSONObject(geoJsonString);

			String type = geoJson.getString("type");

			if (type.equals("FeatureCollection")) {
				pois = fromFeatureCollection(geoJson.getJSONArray("features"));
			} else if (type.equals("GeometryCollection")) {
				pois = fromGeometryCollection(geoJson.getJSONArray("geometries"));
			} else {
				throw new IllegalArgumentException("GeoJson of unknown type");
			}
		} catch (JSONException e) {
			throw new IllegalArgumentException(e.getMessage());
		}

		return pois;
	}

	Collection<PointOfInterest> fromFeatureCollection(JSONArray features) throws JSONException {
		ArrayList<PointOfInterest> pois = new ArrayList<PointOfInterest>(features.length());

		for (int i = 0; i < features.length(); i++) {
			pois.add(fromFeature(features.getJSONObject(i)));
		}

		return pois;
	}

	Collection<PointOfInterest> fromGeometryCollection(JSONArray geometries)
			throws JSONException {
		ArrayList<PointOfInterest> pois = new ArrayList<PointOfInterest>(geometries.length());

		for (int i = 0; i < geometries.length(); i++) {
			pois.add(fromFeature(geometries.getJSONObject(i)));
		}

		return pois;
	}

	PointOfInterest fromFeature(JSONObject feature) throws JSONException {
		String type = feature.getString("type").toString();
		PointOfInterest point;
		String name = null;
		String url = null;
		Long id = null;

		if (type.equals("Feature")) {
			point = fromPoint(feature.getJSONObject("geometry"));

			if (feature.has("properties")) {
				JSONObject properties = feature.getJSONObject("properties");
				if (properties.has("name")) {
					name = properties.getString("name");
				}
				if (properties.has("url")) {
					url = properties.getString("url");
				}
				if (properties.has("id")) {
					id = properties.getLong("id");
				}
			}

		} else {
			throw new IllegalArgumentException();
		}

		return new PoiBuilder(id, point.getLatitude(), point.getLongitude(), this.category)
				.setName(name).setUrl(url).build();
	}

	PointOfInterest fromPoint(JSONObject point) throws JSONException {
		String type = point.getString("type");
		double lat;
		double lng;

		if (type.equals("Point")) {
			JSONArray coords = point.getJSONArray("coordinates");
			lng = coords.getDouble(0);
			lat = coords.getDouble(1);
		} else {
			throw new IllegalArgumentException();
		}

		return new PoiBuilder(0, lat, lng, this.category).build();
	}
}
