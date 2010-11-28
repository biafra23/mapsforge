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
import org.garret.perst.Persistent;
import org.garret.perst.Storage;

class Rtree3DIndex<T> extends Persistent implements RtreeIndex<T, RtreeBox> {

	private int height;
	private int n;
	private RtreeIndexPage<T, RtreeBox> root;

	@Override
	public ArrayList<T> getList(RtreeBox b) {
		ArrayList<T> result = new ArrayList<T>();
		if (root != null) {
			root.find(b, result, height);
		}
		return result;
	}

	@Override
	public void packInsert(Iterator<PackEntry<RtreeBox, T>> iterator, Storage storage) {
		if (n != 0) {
			throw new UnsupportedOperationException("Only possible on emtpy trees");
		}

		ArrayList<RtreeIndexPage<T, RtreeBox>> lowerLevelNodes = packLeafNodes(iterator,
				storage);
		height = 1;
		while (lowerLevelNodes.size() > 1) {
			lowerLevelNodes = packInsertHigherLevelNodes(lowerLevelNodes, storage);
			height++;
		}

		root = lowerLevelNodes.get(0);
		Assert.that(root != null);
		modify();
	}

	private ArrayList<RtreeIndexPage<T, RtreeBox>> packLeafNodes(
			Iterator<PackEntry<RtreeBox, T>> iterator, Storage storage) {
		ArrayList<RtreeIndexPage<T, RtreeBox>> leafNodes = new ArrayList<RtreeIndexPage<T, RtreeBox>>();
		ArrayList<T> objs = new ArrayList<T>();
		ArrayList<RtreeBox> boxes = new ArrayList<RtreeBox>();
		PackEntry<RtreeBox, T> entry = null;
		while (iterator.hasNext()) {
			n++;
			entry = iterator.next();
			objs.add(entry.obj);
			boxes.add(entry.shape);
			if (objs.size() == Rtree3DIndexPage.capacity) {
				leafNodes.add(new Rtree3DIndexPage<T>(storage, objs.toArray(new Object[objs
						.size()]), boxes.toArray(new RtreeBox[boxes.size()]), true));
				objs.clear();
				boxes.clear();
			}
		}
		if (objs.size() > 0) {
			leafNodes.add(new Rtree3DIndexPage<T>(storage, objs
					.toArray(new Object[objs.size()]), boxes
					.toArray(new RtreeBox[boxes.size()]), true));
		}
		return leafNodes;
	}

	private ArrayList<RtreeIndexPage<T, RtreeBox>> packInsertHigherLevelNodes(
			ArrayList<RtreeIndexPage<T, RtreeBox>> lowerLevelNodes, Storage storage) {
		int offset = 0;
		ArrayList<RtreeIndexPage<T, RtreeBox>> result = new ArrayList<RtreeIndexPage<T, RtreeBox>>();
		List<RtreeIndexPage<T, RtreeBox>> list;
		while (offset < lowerLevelNodes.size()) {
			list = lowerLevelNodes.subList(offset, Math.min(offset + Rtree3DIndexPage.capacity,
					lowerLevelNodes.size()));

			if (!list.isEmpty()) {
				Object[] obs = new Object[list.size()];
				RtreeBox[] boxes = new RtreeBox[list.size()];

				for (int i = 0; i < list.size(); i++) {
					obs[i] = list.get(i);
					boxes[i] = list.get(i).getMinimalBoundingShape();
				}
				result.add(new Rtree3DIndexPage<T>(storage, obs, boxes, false));
			}

			offset += Rtree3DIndexPage.capacity;
		}

		return result;
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return n;
	}

	@Override
	public RtreeBox getMinimalBoundingShape() {
		return root.getMinimalBoundingShape();
	}

	@Override
	public void put(T item, RtreeBox shape) {
		Storage db = getStorage();
		if (root == null) {
			root = new Rtree3DIndexPage<T>(db, item, shape, true);
			height = 1;
		} else {
			RtreeIndexPage<T, RtreeBox> newRoot = root.insert(db, shape, item, height);
			if (newRoot != null) {
				root = newRoot;
				height += 1;
			}
		}
		n += 1;
		modify();
	}

	@Override
	public void remove(T item, RtreeBox shape) {
		RtreeIndexPage<T, RtreeBox> newRoot = root.remove(shape, item, height);
		if (newRoot != null) {
			root = newRoot;
			height--;
		}
	}

	@Override
	public Iterator<T> iterator() {
		return root.iterator(height);
	}

}
