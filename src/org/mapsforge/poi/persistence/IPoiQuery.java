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

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.poi.PoiCategory;
import org.mapsforge.poi.PointOfInterest;

/**
 * Abstracts from an underlying storage DB for points of interest by providing methods for
 * searching points of interest near a {@link GeoCoordinate} or in a rectangle specified by two
 * {@link GeoCoordinate} objects.<br/>
 * <br/>
 * Remember to call the {@link #close()} methods after you are done querying in order to release
 * resources that might have been claimed by this {@link IPoiQuery}.
 * 
 * @author weise
 * 
 */
public interface IPoiQuery {

	/**
	 * Fetch {@link PointOfInterest} from underlying storage near a given position.
	 * 
	 * @param point
	 *            {@link GeoPoint} center of the search.
	 * @param distance
	 *            in meters
	 * @param categoryName
	 *            unique title of {@link PoiCategory} the returned {@link PointOfInterest}
	 *            should belong to.
	 * @param limit
	 *            max number of {@link PointOfInterest} to be returned.
	 * @return {@link Collection} of {@link PointOfInterest} of the given {@link PoiCategory}
	 *         near the given position.
	 */
	public Collection<PointOfInterest> findNearPosition(GeoCoordinate point, int distance,
			String categoryName, int limit);

	/**
	 * Find all {@link PointOfInterest} of the given {@link PoiCategory} in a rectangle
	 * specified by the two given {@link GeoPoint}s.
	 * 
	 * @param p1
	 *            {@link GeoPoint} specifying one corner of the rectangle.
	 * @param p2
	 *            {@link GeoPoint} specifying one corner of the rectangle.
	 * @param categoryName
	 *            unique title of {@link PoiCategory} the returned {@link PointOfInterest}
	 *            should belong to.
	 * @return {@link Collection} of {@link PointOfInterest} of the given {@link PoiCategory}
	 *         contained in the rectangle specified by the two given {@link GeoPoint}s.
	 */
	public Collection<PointOfInterest> findInRect(GeoCoordinate p1, GeoCoordinate p2,
			String categoryName);

	/**
	 * Use this to free claimed resources. After that you might no longer be able to query for
	 * points of interest with this instance of {@link IPoiQuery}. This should always be a
	 * called a soon as you are done querying.
	 */
	public void close();

}
