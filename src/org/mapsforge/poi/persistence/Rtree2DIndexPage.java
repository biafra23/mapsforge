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

import org.garret.perst.Storage;
import org.garret.perst.impl.Page;

class Rtree2DIndexPage<T> extends AbstractHilbertRtreePage<T, Rect> {

	static final int capacity = (Page.pageSize - 8 - 4 * 4 - 8) / (4 * 4 + 4);

	Rect[] rects;

	Rtree2DIndexPage(Storage storage, Object[] objs, Rect[] rects, boolean leaf) {
		super(storage, objs, rects, leaf);
	}

	Rtree2DIndexPage(Storage storage, Object obj, Rect rect, boolean leaf) {
		super(storage, obj, rect, leaf);
	}

	Rtree2DIndexPage(Storage storage, ArrayList<AbstractHilbertRtreePage<T, Rect>> children) {
		super(storage, children);
	}

	Rtree2DIndexPage(Storage storage, boolean leaf) {
		super(storage, leaf);
	}

	Rtree2DIndexPage() {
		super();
	}

	@Override
	protected void initialize(Storage storage) {
		branch = storage.createLink(capacity());
		branch.setSize(capacity());
		rects = new Rect[capacity()];
		for (int i = 0; i < capacity(); i++) {
			rects[i] = new Rect();
		}
		n = 0;
	}

	@Override
	protected void instantiateShape(int index) {
		rects[index] = new Rect();
	}

	@Override
	public Rect getMinimalBoundingShape() {
		Rect rect = new Rect(rects[0]);
		for (int i = 1; i < n; i++) {
			rect.join(rects[i]);
		}
		return rect;
	}

	@Override
	AbstractHilbertRtreePage<T, Rect> newRoot(Storage storage,
			ArrayList<AbstractHilbertRtreePage<T, Rect>> children) {
		return new Rtree2DIndexPage<T>(storage, children);
	}

	@Override
	AbstractHilbertRtreePage<T, Rect> newNode(Storage storage,
			AbstractHilbertRtreePage<T, Rect> parentNode, boolean leaf) {
		Rtree2DIndexPage<T> node = new Rtree2DIndexPage<T>(storage, leaf);
		node.parent = parentNode;
		return node;
	}

	@Override
	Rect getShape(int index) {
		return rects[index];
	}

	@Override
	Rect[] getShapes() {
		return rects;
	}

	@Override
	void setShape(int index, Rect rect) {
		rects[index] = rect;
	}

	@Override
	int capacity() {
		return capacity;
	}
}
