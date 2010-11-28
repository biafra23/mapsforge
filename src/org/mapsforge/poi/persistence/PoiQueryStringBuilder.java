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

class PoiQueryStringBuilder {

	private static final String DESCENDANT_QUERY = "WITH RECURSIVE q AS ("
			+ "SELECT title FROM categories WHERE title = '{{title}}' " + "UNION ALL "
			+ "SELECT child.title FROM q " + "JOIN categories child ON child.parent = q.title "
			+ ") " + "SELECT title FROM q";

	private static final String QUERY_STRING = "SELECT id, Y(ST_AsText(location)) as lat, "
			+ "X(ST_AsText(location)) as lng, "
			+ "ST_Distance(location, ST_GeographyFromText('SRID=4326;POINT({{lng}} {{lat}})')) as distance, "
			+ "name, url, category " + "FROM pois WHERE category IN ({{categories}}) "
			+ "AND ST_DWithin(location, ST_GeographyFromText('SRID=4326;"
			+ "POINT({{lng}} {{lat}})'), {{distance}}) " + "ORDER BY distance ASC";

	private final String queryString;

	public PoiQueryStringBuilder(Double lat, Double lng, String categoryName, Integer distance) {
		this(lat, lng, categoryName, distance, null);
	}

	public PoiQueryStringBuilder(Double lat, Double lng, String categoryName, Integer distance,
			Integer limit) {
		queryString = QUERY_STRING.replaceAll("\\Q{{lng}}\\E", lng.toString()).replaceAll(
				"\\Q{{lat}}\\E", lat.toString()).replaceAll("\\Q{{categories}}\\E",
				DESCENDANT_QUERY.replaceAll("\\Q{{title}}\\E", categoryName.toString()))
				.replaceAll("\\Q{{distance}}\\E", distance.toString()).concat(
						limit != null ? " LIMIT " + limit.toString() : "");
	}

	public String queryString() {
		return queryString;
	}

}
