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

import org.garret.perst.Storage;

interface RtreeIndexPage<T, S extends SpatialShape<S>> {

	public RtreeIndexPage<T, S> insert(Storage storage, S shape, T item, int level);

	public RtreeIndexPage<T, S> remove(S shape, T item, int level);

	public void find(S shape, ArrayList<T> result, int level);

	public S getMinimalBoundingShape();

	public void purge(int level);

	public Iterator<T> iterator(int level);

}
