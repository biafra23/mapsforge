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

import org.garret.perst.Persistent;
import org.mapsforge.poi.PoiCategory;

class PerstCategory extends Persistent implements PoiCategory {

	String title;
	PerstCategory parent;

	public PerstCategory() {
		// require by perst
	}

	public PerstCategory(String title, PerstCategory parent) {
		this.parent = parent;
		this.title = title;
	}

	@Override
	public String toString() {
		return title + (parent == null ? "" : " < " + parent.getTitle());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PoiCategory))
			return false;
		PoiCategory other = (PoiCategory) obj;
		if (title == null) {
			if (other.getTitle() != null)
				return false;
		} else if (!title.equalsIgnoreCase(other.getTitle()))
			return false;
		return true;
	}

	@Override
	public PoiCategory getParent() {
		return parent;
	}

	@Override
	public String getTitle() {
		return title;
	}

}