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

/**
 * Can be used to create instances of {@link PoiCategory}.
 * 
 * @author weise
 * 
 */
public class CategoryBuilder {

	private final String title;
	private final PoiCategory parent;

	/**
	 * Use this constructor if category has no parent.
	 * 
	 * @param title
	 *            of the category.
	 */
	public CategoryBuilder(String title) {
		super();
		this.title = title;
		this.parent = null;
	}

	/**
	 * @param title
	 *            of the category.
	 * @param parent
	 *            PoiCategory.
	 */
	public CategoryBuilder(String title, PoiCategory parent) {
		super();
		this.title = title;
		this.parent = parent;
	}

	/**
	 * @return a newly created {@link PoiCategory} object.
	 */
	public PoiCategory build() {
		return new PostGisPoiCategory(title, parent);
	}

}
