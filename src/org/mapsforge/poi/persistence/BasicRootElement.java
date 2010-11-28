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
import org.garret.perst.Persistent;
import org.garret.perst.Storage;

class BasicRootElement extends Persistent {

	FieldIndex<PerstPoi> poiIntegerIdPKIndex;
	FieldIndex<PerstPoi> poiCategoryFkIndex;
	FieldIndex<PerstCategory> categoryTitlePkIndex;

	public BasicRootElement() {
		// required by perst
	}

	public BasicRootElement(Storage db) {
		super(db);
		poiIntegerIdPKIndex = db.<PerstPoi> createFieldIndex(PerstPoi.class, "id", true);
		poiCategoryFkIndex = db.<PerstPoi> createFieldIndex(PerstPoi.class, "category", false);
		categoryTitlePkIndex = db.<PerstCategory> createFieldIndex(PerstCategory.class,
				"title", true);
	}

}
