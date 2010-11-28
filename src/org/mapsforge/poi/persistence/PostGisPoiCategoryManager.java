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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.mapsforge.poi.PoiCategory;

class PostGisPoiCategoryManager implements IPoiCategoryManager {

	private static final String TITLE_QUERY_STRING = "SELECT * FROM categories WHERE title = '?'";
	private static final String ALL_CATEGORIES_STRING = "SELECT * FROM categories;";

	private final PostGisConnection pgConnection;
	private final HashMap<String, PoiCategory> categories;

	public PostGisPoiCategoryManager(PostGisConnection pgConnection) {
		this.pgConnection = pgConnection;
		this.categories = new HashMap<String, PoiCategory>();
	}

	private PoiCategory fetch(String categoryName) {
		if (categoryName == null || categoryName.isEmpty())
			return null;
		return fetchCategory(TITLE_QUERY_STRING.replaceFirst("\\?", categoryName));
	}

	private PoiCategory fetchCategory(String queryString) {
		PoiCategory category = null;
		try {
			category = getSingleResult(queryString);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return category;
	}

	@Override
	public Collection<PoiCategory> allCategories() {
		Collection<PoiCategory> result = null;
		try {
			result = getResultCollection(ALL_CATEGORIES_STRING);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public boolean contains(String categoryName) {
		return (get(categoryName) != null);
	}

	@Override
	public Collection<PoiCategory> descendants(String categoryName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PoiCategory get(String categoryName) {
		PoiCategory result = categories.get(categoryName);
		if (result == null) {
			result = fetch(categoryName);
			if (result != null) {
				categories.put(result.getTitle(), result);
			}
		}
		return result;
	}

	private PoiCategory getSingleResult(String queryString) throws SQLException {
		ResultSet resultSet = pgConnection.executeQuery(queryString);
		ArrayList<PoiCategory> result = categoriesFromResultSet(resultSet);

		if (result.size() > 1) {
			throw new SQLException("Expected single result but got " + result.size());
		}

		return result.size() == 1 ? result.get(0) : null;
	}

	private Collection<PoiCategory> getResultCollection(String queryString) throws SQLException {
		ResultSet resultSet = pgConnection.executeQuery(queryString);
		return categoriesFromResultSet(resultSet);
	}

	private ArrayList<PoiCategory> categoriesFromResultSet(ResultSet resultSet)
			throws SQLException {
		ArrayList<PoiCategory> result = new ArrayList<PoiCategory>();

		while (resultSet.next()) {
			result.add(new PostGisPoiCategory(resultSet.getString("title"), fetch(resultSet
					.getString("parent"))));
		}
		return result;
	}

}
