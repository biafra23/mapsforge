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
import java.util.Iterator;

import org.garret.perst.IPersistent;
import org.garret.perst.Storage;

interface RtreeIndex<T, S extends SpatialShape<S>> extends IPersistent {

	public ArrayList<T> getList(S b);

	public S getMinimalBoundingShape();

	public void packInsert(Iterator<PackEntry<S, T>> iterator, Storage storage);

	public void put(T item, S shape);

	public void remove(T item, S shape);

	public void clear();

	public int size();

	public Iterator<T> iterator();

}
