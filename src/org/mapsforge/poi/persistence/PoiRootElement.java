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

import org.garret.perst.FieldIndex;
import org.garret.perst.Storage;

/**
 * @author weise
 * 
 */
class PoiRootElement extends BasicRootElement {

	FieldIndex<DeleteQueue> deleteQueueIndex;
	FieldIndex<NamedSpatialIndex> spatialIndexIndex;

	/**
	 * Default constructor required by perst!
	 */
	public PoiRootElement() {
		// require by perst
	}

	public PoiRootElement(Storage db) {
		super(db);
		deleteQueueIndex = db.<DeleteQueue> createFieldIndex(DeleteQueue.class, "poiId", true);
		spatialIndexIndex = db.<NamedSpatialIndex> createFieldIndex(NamedSpatialIndex.class,
				"name", true);
	}

	public void addSpatialIndex(Storage db, String categoryName) {
		Rtree2DIndex<PerstPoi> index = new Rtree2DIndex<PerstPoi>();
		NamedSpatialIndex namedIndex = new NamedSpatialIndex(categoryName, index);

		spatialIndexIndex.add(namedIndex);
		db.store(namedIndex);
	}

	public void addSpatialIndex(NamedSpatialIndex namedIndex) {
		spatialIndexIndex.add(namedIndex);
		modify();
	}

	public void removeSpatialIndex(String categoryName) {
		NamedSpatialIndex namedIndex = spatialIndexIndex.get(categoryName);
		spatialIndexIndex.remove(namedIndex);
		namedIndex.index.clear();
		namedIndex.index.deallocate();
		namedIndex.deallocate();
	}

	public Rtree2DIndex<PerstPoi> getSpatialIndex(String categoryName) {
		NamedSpatialIndex namedIndex = spatialIndexIndex.get(categoryName);
		return namedIndex == null ? null : namedIndex.index;
	}

}