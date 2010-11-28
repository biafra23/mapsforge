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

import java.util.ArrayList;

import org.garret.perst.Link;
import org.garret.perst.SpatialIndex;
import org.garret.perst.Storage;

class PerstRootElement extends BasicRootElement {

	ArrayList<String> names;
	Link<SpatialIndex<PerstPoi>> indexes;

	/**
	 * Default constructor required by perst!
	 */
	public PerstRootElement() {
		// require by perst
	}

	public PerstRootElement(Storage db) {
		super(db);
		names = new ArrayList<String>();
		indexes = db.<SpatialIndex<PerstPoi>> createLink();
	}

	public void addSpatialIndex(Storage db, String categoryName) {
		SpatialIndex<PerstPoi> index = db.<PerstPoi> createSpatialIndex();
		names.add(categoryName);
		indexes.add(index);
		modify();
	}

	public void removeSpatialIndex(String categoryName) {
		int i = names.indexOf(categoryName);
		names.remove(i);
		SpatialIndex<PerstPoi> index = indexes.remove(i);
		index.clear();
		index.deallocate();
		modify();
	}

	public SpatialIndex<PerstPoi> getSpatialIndex(String categoryName) {
		int i = names.indexOf(categoryName);
		return i < 0 ? null : indexes.get(i);
	}
}
