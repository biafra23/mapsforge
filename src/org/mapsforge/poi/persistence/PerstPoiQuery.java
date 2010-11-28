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

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.poi.PointOfInterest;

/**
 * Implements {@link IPoiQuery} interface for the embedded Perst database. Provides methods for
 * finding points of interest in a rectangle specified by two {@link GeoCoordinate} objects or
 * near a position specified by a single {@link GeoCoordinate}.
 * 
 * @author weise
 * 
 */
public class PerstPoiQuery implements IPoiQuery {

	private final MultiRtreePersistenceManager persistenceManager;

	/**
	 * @param storageFileName
	 *            name of the perst storage file.
	 */
	public PerstPoiQuery(String storageFileName) {
		this.persistenceManager = new MultiRtreePersistenceManager(storageFileName);
	}

	@Override
	public Collection<PointOfInterest> findNearPosition(GeoCoordinate point, int distance,
			String categoryName, int limit) {
		return persistenceManager.findNearPosition(point, distance, categoryName, limit);
	}

	@Override
	public Collection<PointOfInterest> findInRect(GeoCoordinate p1, GeoCoordinate p2,
			String categoryName) {
		return persistenceManager.findInRect(p1, p2, categoryName);
	}

	@Override
	public void close() {
		persistenceManager.close();
	}

}
