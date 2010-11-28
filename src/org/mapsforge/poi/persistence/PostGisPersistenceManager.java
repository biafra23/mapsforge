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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.poi.PoiCategory;
import org.mapsforge.poi.PointOfInterest;
import org.mapsforge.poi.exchange.IPoiReader;

class PostGisPersistenceManager implements IPersistenceManager {

	protected static final String REPLACE_REGEX = "\\Q{{?}}\\E";
	protected static final String INSERT_POI = "INSERT INTO pois (\"location\", category, name, url) VALUES "
			+ "(ST_GeographyFromText('SRID=4326;POINT({{?}} {{?}})'), "
			+ "'{{?}}', {{?}}, {{?}});";
	protected static final String INSERT_CATEGORY = "INSERT INTO categories(title, parent) VALUES "
			+ "('{{?}}', {{?}});";

	private final PostGisConnection pgConnection;
	private PostGisPoiCategoryManager categoryManager;

	public PostGisPersistenceManager(Connection connection) {
		if (connection == null)
			throw new NullPointerException();
		this.pgConnection = new PostGisConnection(connection);
		this.categoryManager = new PostGisPoiCategoryManager(pgConnection);
	}

	@Override
	public Collection<PoiCategory> allCategories() {
		return categoryManager.allCategories();
	}

	@Override
	public void close() {
		pgConnection.close();
	}

	@Override
	public Collection<PoiCategory> descendants(String category) {
		return categoryManager.descendants(category);
	}

	@Override
	public boolean insertCategory(PoiCategory category) {
		if (!categoryManager.contains(category.getTitle())) {
			String sql = INSERT_CATEGORY.replaceFirst(REPLACE_REGEX, category.getTitle())
					.replaceFirst(
							REPLACE_REGEX,
							category.getParent() != null ? "'"
									+ category.getParent().getTitle() + "'" : "null");
			if (pgConnection.executeInsertStatement(sql)) {
				categoryManager = new PostGisPoiCategoryManager(pgConnection);
			}
			return true;
		}
		return false;
	}

	protected String sanitize(String argument) {
		return (argument == null ? "null" : "'" + argument.replaceAll("\\Q'\\E", "") + "'");
	}

	@Override
	public void insertPointOfInterest(PointOfInterest poi) {
		String sql = insertPoiString(poi);
		pgConnection.executeInsertStatement(sql);
	}

	@Override
	public void insertPointsOfInterest(Collection<PointOfInterest> pois) {
		for (PointOfInterest poi : pois) {
			pgConnection.addToBatch(insertPoiString(poi));
		}

		pgConnection.executeBatch();
	}

	private String insertPoiString(PointOfInterest poi) {
		return INSERT_POI.replaceFirst(REPLACE_REGEX, "" + poi.getLongitude()).replaceFirst(
				REPLACE_REGEX, "" + poi.getLatitude()).replaceFirst(REPLACE_REGEX,
				poi.getCategory().getTitle()).replaceFirst(REPLACE_REGEX,
				sanitize(poi.getName())).replaceFirst(REPLACE_REGEX, sanitize(poi.getUrl()));
	}

	@Override
	public void insertPointsOfInterest(IPoiReader poiReader) {
		Collection<PointOfInterest> pois = poiReader.read();

		for (PointOfInterest poi : pois) {
			insertPointOfInterest(poi);
		}
	}

	@Override
	public void removeCategory(PoiCategory category) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePointOfInterest(PointOfInterest poi) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<PointOfInterest> findNearPosition(GeoCoordinate point, int distance,
			String categoryName, int limit) {
		ArrayList<PointOfInterest> pois = new ArrayList<PointOfInterest>();

		try {
			String queryString = new PoiQueryStringBuilder(point.getLatitude(), point
					.getLongitude(), categoryName, distance, limit).queryString();

			System.out.println(queryString);

			ResultSet result = pgConnection.executeQuery(queryString);

			if (result.first()) {
				while (!result.isLast()) {
					pois.add(new PostGisPoi(result.getLong("id"), new Double(result
							.getDouble("lat")).intValue(), new Double(result.getDouble("lng"))
							.intValue(), result.getString("name"), result.getString("url"),
							categoryManager.get(result.getString("category"))));
					result.next();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return pois;
	}

	@Override
	public Collection<PointOfInterest> findInRect(GeoCoordinate p1, GeoCoordinate p2,
			String categoryName) {
		// TODO implement findInRect
		throw new UnsupportedOperationException();
	}

	@Override
	public PointOfInterest getPointById(long poiId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reopen() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clusterStorage() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void packIndex() {
		throw new UnsupportedOperationException();
	}

}
