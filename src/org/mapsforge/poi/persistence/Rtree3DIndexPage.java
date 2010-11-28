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

class Rtree3DIndexPage<T> extends AbstractHilbertRtreePage<T, RtreeBox> {

	static final int capacity = (Page.pageSize - 8 - 4 * 4 - 8) / (6 * 4 + 4);

	RtreeBox[] boxes;

	Rtree3DIndexPage(Storage storage, Object[] objs, RtreeBox[] boxes, boolean leaf) {
		super(storage, objs, boxes, leaf);
	}

	Rtree3DIndexPage(Storage storage, Object obj, RtreeBox box, boolean leaf) {
		super(storage, obj, box, leaf);
	}

	Rtree3DIndexPage(Storage storage, ArrayList<AbstractHilbertRtreePage<T, RtreeBox>> children) {
		super(storage, children);
	}

	Rtree3DIndexPage(Storage storage, boolean leaf) {
		super(storage, leaf);
	}

	Rtree3DIndexPage() {
		super();
	}

	@Override
	int capacity() {
		return capacity;
	}

	@Override
	RtreeBox getShape(int index) {
		return boxes[index];
	}

	@Override
	RtreeBox[] getShapes() {
		return boxes;
	}

	@Override
	void initialize(Storage storage) {
		branch = storage.createLink(capacity());
		branch.setSize(capacity());
		boxes = new RtreeBox[capacity()];
		for (int i = 0; i < capacity(); i++) {
			boxes[i] = new RtreeBox();
		}
		n = 0;
	}

	@Override
	protected void instantiateShape(int index) {
		boxes[index] = new RtreeBox();
	}

	@Override
	AbstractHilbertRtreePage<T, RtreeBox> newRoot(Storage storage,
			ArrayList<AbstractHilbertRtreePage<T, RtreeBox>> children) {
		return new Rtree3DIndexPage<T>(storage, children);
	}

	@Override
	AbstractHilbertRtreePage<T, RtreeBox> newNode(Storage storage,
			AbstractHilbertRtreePage<T, RtreeBox> parentNode, boolean leaf) {
		Rtree3DIndexPage<T> node = new Rtree3DIndexPage<T>(storage, leaf);
		node.parent = parentNode;
		return node;
	}

	@Override
	void setShape(int index, RtreeBox shape) {
		boxes[index] = shape;
	}

	@Override
	public RtreeBox getMinimalBoundingShape() {
		RtreeBox box = new RtreeBox(boxes[0]);
		for (int i = 1; i < n; i++) {
			box.join(boxes[i]);
		}
		return box;
	}

}
