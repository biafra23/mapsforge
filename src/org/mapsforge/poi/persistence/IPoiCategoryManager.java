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

import java.util.Collection;

import org.mapsforge.poi.PoiCategory;

/**
 * Implementations of this class provide methods for retrieving {@link PoiCategory} objects by
 * their unique title. There are methods for retrieving a single object, an object and all its
 * descendants and all {@link PoiCategory} currently managed by this {@link IPoiCategoryManager}
 * .
 * 
 * @author weise
 * 
 */
interface IPoiCategoryManager {

	/**
	 * @param categoryName
	 *            the unique title of {@link PoiCategory} that shall be returned.
	 * @return PoiCategory cat with cat.title = categoryName unless there is no such
	 *         {@link PoiCategory} currently managed by this {@link IPoiCategoryManager}.
	 */
	public PoiCategory get(String categoryName);

	/**
	 * @param categoryName
	 *            the unique title of the {@link PoiCategory} for which all its descendants
	 *            shall be returned.
	 * @return A collection of {@link PoiCategory} containing {@link PoiCategory} cat with
	 *         cat.title = categoryName and sub categories derived from cat.
	 */
	public Collection<PoiCategory> descendants(String categoryName);

	/**
	 * @return A collection of {@link PoiCategory} containing all categories currently managed
	 *         by this {@link IPoiCategoryManager}.
	 */
	public Collection<PoiCategory> allCategories();

	/**
	 * @param categoryName
	 *            the unique title of the {@link PoiCategory} for which shall be checked whether
	 *            it is managed by this IPCM or not.
	 * @return true if there is a {@link PoiCategory} cat managed by this
	 *         {@link IPoiCategoryManager} with cat.title = categoryName.
	 */
	public boolean contains(String categoryName);

}
