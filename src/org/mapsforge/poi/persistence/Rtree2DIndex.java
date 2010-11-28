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
import java.util.List;

import org.garret.perst.Assert;
import org.garret.perst.PersistentCollection;
import org.garret.perst.Storage;

class Rtree2DIndex<T> extends PersistentCollection<T> implements RtreeIndex<T, Rect> {

	public RtreeIndexPage<T, Rect> root;
	private int height;
	private int n;

	@Override
	public ArrayList<T> getList(Rect rect) {
		ArrayList<T> result = new ArrayList<T>();
		if (root != null) {
			root.find(rect, result, height);
		}
		return result;
	}

	@Override
	public Rect getMinimalBoundingShape() {
		return root.getMinimalBoundingShape();
	}

	@Override
	public void packInsert(Iterator<PackEntry<Rect, T>> iterator, Storage storage) {
		if (n != 0) {
			throw new RuntimeException("Only Possible on emtpy trees");
		}

		ArrayList<Rtree2DIndexPage<T>> leafNodes = new ArrayList<Rtree2DIndexPage<T>>();
		ArrayList<T> obs = new ArrayList<T>();
		ArrayList<Rect> rects = new ArrayList<Rect>();
		PackEntry<Rect, T> entry = null;
		while (iterator.hasNext()) {
			entry = iterator.next();
			obs.add(entry.obj);
			rects.add(entry.shape);
			if (obs.size() == Rtree2DIndexPage.capacity) {
				leafNodes.add(new Rtree2DIndexPage<T>(storage, obs.toArray(new Object[obs
						.size()]), rects.toArray(new Rect[rects.size()]), true));
				obs.clear();
				rects.clear();
			}
		}
		if (obs.size() > 0) {
			leafNodes.add(new Rtree2DIndexPage<T>(storage, obs.toArray(new Object[obs.size()]),
					rects.toArray(new Rect[rects.size()]), true));
		}

		ArrayList<Rtree2DIndexPage<T>> lowerLevelNodes = leafNodes;
		height = 1;
		while (lowerLevelNodes.size() > 1) {
			lowerLevelNodes = packInsertHigherLevelNodes(lowerLevelNodes, storage);
			height++;
		}

		root = lowerLevelNodes.get(0);
		Assert.that(root != null);
		modify();
	}

	private ArrayList<Rtree2DIndexPage<T>> packInsertHigherLevelNodes(
			ArrayList<Rtree2DIndexPage<T>> lowerLevelNodes, Storage storage) {
		int offset = 0;
		ArrayList<Rtree2DIndexPage<T>> result = new ArrayList<Rtree2DIndexPage<T>>();
		List<Rtree2DIndexPage<T>> list;
		while (offset < lowerLevelNodes.size()) {
			list = lowerLevelNodes.subList(offset, Math.min(offset + Rtree2DIndexPage.capacity,
					lowerLevelNodes.size()));

			if (!list.isEmpty()) {
				Object[] obs = new Object[list.size()];
				Rect[] rects = new Rect[list.size()];

				for (int i = 0; i < list.size(); i++) {
					obs[i] = list.get(i);
					rects[i] = list.get(i).getMinimalBoundingShape();
				}
				result.add(new Rtree2DIndexPage<T>(storage, obs, rects, false));
			}

			offset += Rtree2DIndexPage.capacity;
		}

		return result;
	}

	@Override
	public void put(T item, Rect rect) {
		Storage db = getStorage();
		if (root == null) {
			root = new Rtree2DIndexPage<T>(db, item, rect, true);
			height = 1;
		} else {
			RtreeIndexPage<T, Rect> newRoot = root.insert(db, rect, item, height);
			if (newRoot != null) {
				root = newRoot;
				height += 1;
			}
		}
		n += 1;
		modify();
	}

	@Override
	public void remove(T item, Rect rect) {
		RtreeIndexPage<T, Rect> newRoot = root.remove(rect, item, height);
		if (newRoot != null) {
			root = newRoot;
			height -= 1;
		}
	}

	@Override
	public void clear() {
		if (root != null) {
			root.purge(height);
			root = null;
		}
		height = 0;
		n = 0;
		modify();
	}

	@Override
	public int size() {
		return n;
	}

	@Override
	public Iterator<T> iterator() {
		System.out.println("size = " + n);
		System.out.println("height = " + height);
		return root.iterator(height);
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <E> E[] toArray(E[] a) {
		throw new UnsupportedOperationException();
	}

}
