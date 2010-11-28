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
package org.mapsforge.poi;


/**
 * This class represents a category for {@link PointOfInterest}. Every {@link PoiCategory}
 * should have a unique title so that for two {@link PoiCategory}s a and b a.equals(b) if and
 * only if a.title.equalsIgnoreCase(b.title).
 * 
 * @author weise
 * 
 */
public interface PoiCategory {

	/**
	 * @return title of this category.
	 */
	public String getTitle();

	/**
	 * @return parent category of this category or null if this category has no parent.
	 */
	public PoiCategory getParent();
}
