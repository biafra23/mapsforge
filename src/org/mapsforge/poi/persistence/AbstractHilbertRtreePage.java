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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.garret.perst.Assert;
import org.garret.perst.Link;
import org.garret.perst.Persistent;
import org.garret.perst.Storage;
import org.mapsforge.poi.PerstQueryTracer;

abstract class AbstractHilbertRtreePage<T, S extends SpatialShape<S>> extends Persistent
		implements RtreeIndexPage<T, S> {

	static final int cooperatingSiblings = 1; // 1 -> 2-to-3 split // 2 -> 3-to-4 split

	class HilbertIterator implements Iterator<T> {
		private Stack<Iterator<Entry<S>>> iteratorStack;
		private int height = 0;

		public HilbertIterator(AbstractHilbertRtreePage<T, S> root, int height) {
			super();
			this.iteratorStack = new Stack<Iterator<Entry<S>>>();
			this.height = height;
			iteratorStack.push(root.getEntryList().iterator());
			downToLeaf();
		}

		@Override
		public boolean hasNext() {
			while (!iteratorStack.empty() && !iteratorStack.peek().hasNext()) {
				iteratorStack.pop();
			}
			if (iteratorStack.empty()) {
				return false;
			}
			return iteratorStack.peek().hasNext();
		}

		@Override
		public T next() {
			while (!iteratorStack.empty() && !iteratorStack.peek().hasNext()) {
				iteratorStack.pop();
			}
			if (iteratorStack.empty()) {
				throw new NoSuchElementException();
			}

			downToLeaf();

			return (T) iteratorStack.peek().next().item;
		}

		private void downToLeaf() {
			while (currentLevel() != 0) {
				AbstractHilbertRtreePage<T, S> page = (AbstractHilbertRtreePage<T, S>) iteratorStack
						.peek().next().item;
				iteratorStack.push(page.getEntryList().iterator());
			}
		}

		private int currentLevel() {
			return height - iteratorStack.size();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private static class Entry<S extends SpatialShape<S>> implements Comparable<Entry<S>> {
		final Object item;
		final S shape;
		final long value;

		Entry(Object item, S shape, long hilbert) {
			super();
			this.item = item;
			this.shape = shape;
			value = hilbert;
		}

		@Override
		public int compareTo(Entry<S> other) {
			long result = value - other.value;
			if (result < 0)
				return -1;
			if (result > 0)
				return 1;
			return 0;
		}
	}

	private static <E, R extends SpatialShape<R>> boolean isOverflowing(
			ArrayList<AbstractHilbertRtreePage<E, R>> pages, int capacity) {
		for (int i = 0; i < pages.size(); i++) {
			if (pages.get(i).n < capacity) {
				return false;
			}
		}
		return true;
	}

	private static <E, R extends SpatialShape<R>> boolean isUnderflowing(
			ArrayList<AbstractHilbertRtreePage<E, R>> pages, int minFill) {
		int numberOfEntrys = 0;
		for (AbstractHilbertRtreePage<E, R> page : pages) {
			numberOfEntrys += page.n;
		}

		return (numberOfEntrys < (minFill * pages.size()));
	}

	private static <E, R extends SpatialShape<R>> void distributeElements(
			ArrayList<AbstractHilbertRtreePage<E, R>> pages,
			ArrayList<Entry<R>> additionalItems,
			int capacity) {

		ArrayList<Entry<R>> children = collectChildren(pages);

		if (additionalItems != null && !additionalItems.isEmpty()) {
			children.addAll(additionalItems);
			Collections.sort(children);
		}

		Assert.that(children.size() <= (pages.size() * capacity));

		int rest = children.size() % pages.size();
		int offset = 0;
		int step = children.size() / pages.size();

		for (int i = 0; i < rest; i++) {
			pages.get(i).replaceChildren(children.subList(offset, offset + step + 1));
			offset += (step + 1);
		}
		for (int i = rest; i < pages.size(); i++) {
			pages.get(i).replaceChildren(children.subList(offset, offset + step));
			offset += (step);
		}
	}

	private static <E, R extends SpatialShape<R>> ArrayList<Entry<R>> collectChildren(
			ArrayList<AbstractHilbertRtreePage<E, R>> pages) {
		ArrayList<Entry<R>> result = new ArrayList<Entry<R>>();
		for (AbstractHilbertRtreePage<E, R> page : pages) {
			result.addAll(page.getEntryListRaw());
		}
		return result;
	}

	int n = 0;
	long largestHilbertValue;
	Link<Object> branch;
	AbstractHilbertRtreePage<T, S> parent;
	boolean isLeaf = true;

	AbstractHilbertRtreePage() {
		// required by perst
	}

	AbstractHilbertRtreePage(Storage storage, Object[] objs, S[] shapes, boolean leaf) {
		Assert.that(objs.length <= capacity());
		Assert.that(objs.length == shapes.length);
		initialize(storage);

		for (int i = 0; i < objs.length; i++) {
			Assert.that(objs[i] != null);
			Assert.that(shapes[i] != null);
			setBranch(i, shapes[i], objs[i]);
		}

		n = objs.length;
		largestHilbertValue = shapes[n - 1].linearOderValue();
		isLeaf = leaf;
	}

	AbstractHilbertRtreePage(Storage storage, Object obj, S shape, boolean isLeaf) {
		initialize(storage);
		setBranch(0, shape, obj);
		n = 1;
		largestHilbertValue = shape.linearOderValue();
		this.isLeaf = isLeaf;
	}

	AbstractHilbertRtreePage(Storage storage, boolean leaf) {
		initialize(storage);
		isLeaf = leaf;
	}

	AbstractHilbertRtreePage(Storage storage,
			ArrayList<AbstractHilbertRtreePage<T, S>> children) {
		initialize(storage);
		isLeaf = false;
		for (AbstractHilbertRtreePage<T, S> page : children) {
			page.parent = this;
			addBranch(page.getMinimalBoundingShape(), page);
		}
	}

	abstract void initialize(Storage storage);

	abstract AbstractHilbertRtreePage<T, S> newRoot(Storage storage,
			ArrayList<AbstractHilbertRtreePage<T, S>> children);

	abstract AbstractHilbertRtreePage<T, S> newNode(Storage storage,
			AbstractHilbertRtreePage<T, S> parentNode, boolean leaf);

	abstract void setShape(int index, S shape);

	abstract S getShape(int index);

	abstract S[] getShapes();

	abstract int capacity();

	private int minFill() {
		return capacity() / 2;
	}

	@Override
	public void find(S shape, ArrayList<T> result, int level) {
		PerstQueryTracer.getInstance().incrementNodes();

		if (level - 1 != 0) { /* this is an internal node in the tree */
			for (int i = 0; i < n; i++) {
				if (shape.intersects(getShape(i))) {
					this.<AbstractHilbertRtreePage<T, S>> getBranch(i).find(shape, result,
							level - 1);
				}
			}
		} else { /* this is a leaf node */
			for (int i = 0; i < n; i++) {
				if (shape.intersects(getShape(i))) {
					result.add(this.<T> getBranch(i));
				}
			}
		}
	}

	@Override
	public RtreeIndexPage<T, S> insert(Storage storage, S shape, T item, int level) {
		modify();
		AbstractHilbertRtreePage<T, S> leaf = chooseLeaf(level, shape.linearOderValue());
		return leaf.put(storage, item, shape);
	}

	private AbstractHilbertRtreePage<T, S> chooseLeaf(int level, long hilbert) {
		if (level - 1 != 0) { /* this is an internal node in the tree */
			int pos = findPositionInBranch(hilbert);
			return this.<AbstractHilbertRtreePage<T, S>> getBranch(pos).chooseLeaf(level - 1,
					hilbert);
		}
		return this;
	}

	@Override
	public void purge(int level) {
		if (level - 1 != 0) { /* this is an internal node in the tree */
			for (int i = 0; i < n; i++) {
				this.<AbstractHilbertRtreePage<T, S>> getBranch(i).purge(level - 1);
			}
		}
		deallocate();
	}

	@Override
	public RtreeIndexPage<T, S> remove(S shape, T item, int level) {
		AbstractHilbertRtreePage<T, S> leaf = findContainingLeaf(shape, level);
		if (leaf != null) {
			return leaf.remove(item);

		}
		return null;
	}

	private RtreeIndexPage<T, S> remove(Object item) {
		removeBranch(branch.indexOfObject(item));
		AbstractHilbertRtreePage<T, S> newRoot = null;
		if (n < minFill()) {
			newRoot = distributeOnRemove();
		}
		return newRoot;
	}

	abstract protected void instantiateShape(int index);

	private <E> E getBranch(final int index) {
		Object result = branch.get(index);
		return (E) result;
	}

	private AbstractHilbertRtreePage<T, S> put(Storage storage, Object item, S shape) {
		if (n < capacity()) {
			addBranch(shape, item);
			if (parent != null) {
				updateParent();
			}
			return null;
		}
		long hilbert = isLeaf ? shape.linearOderValue()
				: ((AbstractHilbertRtreePage<T, S>) item).largestHilbertValue;
		return distributeOnPut(storage, new Entry<S>(item, shape, hilbert));
	}

	private AbstractHilbertRtreePage<T, S> distributeOnPut(Storage storage, Entry<S> newEntry) {
		ArrayList<Entry<S>> list = new ArrayList<Entry<S>>(1);
		list.add(newEntry);

		if (parent == null) {
			// in this case the tree definitely has to grow
			// distribute children evenly among this node and a new sibling
			// and add them both as children to new root node.
			ArrayList<AbstractHilbertRtreePage<T, S>> siblings = new ArrayList<AbstractHilbertRtreePage<T, S>>(
					2);
			siblings.add(this);
			siblings.add(newNode(storage, null, isLeaf));
			distributeElements(siblings, list, capacity());
			return newRoot(storage, siblings);
		}

		AbstractHilbertRtreePage<T, S> newNode = null;
		ArrayList<AbstractHilbertRtreePage<T, S>> siblings = getCooperatingSiblings();
		ArrayList<AbstractHilbertRtreePage<T, S>> pages = new ArrayList<AbstractHilbertRtreePage<T, S>>(
				siblings);
		// first check if a new sibling is needed to hold all elements
		if (isOverflowing(siblings, this.capacity())) {
			// create new sibling and add to list of pages to distribute children
			newNode = newNode(storage, parent, isLeaf);
			pages.add(newNode);
		}
		// distribute elements among siblings
		distributeElements(pages, list, capacity());
		// propagate changes up the tree
		return updateParent(storage, siblings, newNode);
	}

	ArrayList<Entry<S>> getEntryList() {
		ArrayList<Entry<S>> result = new ArrayList<Entry<S>>(n);
		if (isLeaf) {
			for (int i = 0; i < n; i++) {
				result.add(new Entry<S>(branch.get(i), getShape(i), getShape(i)
						.linearOderValue()));
			}
		} else {
			AbstractHilbertRtreePage<T, S> page = null;
			for (int i = 0; i < n; i++) {
				page = this.<AbstractHilbertRtreePage<T, S>> getBranch(i);
				result.add(new Entry<S>(page, getShape(i), page.largestHilbertValue));
			}
		}

		return result;
	}

	ArrayList<Entry<S>> getEntryListRaw() {
		ArrayList<Entry<S>> result = new ArrayList<Entry<S>>(n);
		if (isLeaf) {
			for (int i = 0; i < n; i++) {
				result.add(new Entry<S>(branch.getRaw(i), getShape(i), getShape(i)
						.linearOderValue()));
			}
		} else {
			AbstractHilbertRtreePage<T, S> page = null;
			for (int i = 0; i < n; i++) {
				page = this.<AbstractHilbertRtreePage<T, S>> getBranch(i);
				result.add(new Entry<S>(page, getShape(i), page.largestHilbertValue));
			}
		}

		return result;
	}

	/**
	 * @param storage
	 *            the storage this page is stored in.
	 * @param pages
	 *            changed on this level including this.
	 * @param newNode
	 *            the newly created page if split occurred.
	 * @return the new root if one has been created, else returns null.
	 */
	private AbstractHilbertRtreePage<T, S> updateParent(Storage storage,
			ArrayList<AbstractHilbertRtreePage<T, S>> pages,
			AbstractHilbertRtreePage<T, S> newNode) {
		if (parent == null)
			throw new NullPointerException();

		for (AbstractHilbertRtreePage<T, S> page : pages) {
			page.parent.removeBranch(page.parent.branch.indexOfObject(page));
			page.parent.addBranch(page.getMinimalBoundingShape(), page);
		}
		if (newNode != null) {
			return parent.put(storage, newNode, newNode.getMinimalBoundingShape());
		}
		parent.updateParent();
		return null;
	}

	private void updateParent() {
		if (parent != null) {
			parent.removeBranch(parent.branch.indexOfObject(this));
			parent.addBranch(getMinimalBoundingShape(), this);
		}
	}

	private void replaceChildren(List<Entry<S>> children) {
		Assert.that(children.size() <= capacity());
		Entry<S> entry = null;
		branch.clear();
		branch.setSize(capacity());
		for (int i = 0; i < children.size(); i++) {
			entry = children.get(i);
			setBranch(i, entry.shape, entry.item);
		}

		for (int i = children.size(); i < n; i++) {
			instantiateShape(i);
		}
		n = children.size();
		largestHilbertValue = getShape(n - 1).linearOderValue();

		modify();
	}

	private AbstractHilbertRtreePage<T, S> distributeOnRemove() {
		if (parent != null) { /* inner node */
			ArrayList<AbstractHilbertRtreePage<T, S>> pages = getCooperatingSiblings();
			if (isUnderflowing(pages, this.minFill())) {
				pages.remove(this);
				distributeElements(pages, this.getEntryList(), capacity());
				parent.remove(this);
				this.deallocate();
			} else {
				distributeElements(pages, null, capacity());
			}
			updateParent(getStorage(), pages, null);
		} else { /* root node */
			if (n == 1) {
				Object newRoot = getBranch(0);
				if (newRoot instanceof AbstractHilbertRtreePage<?, ?>) {
					this.deallocate();
					return (AbstractHilbertRtreePage<T, S>) newRoot;
				}
			}
		}
		return null;
	}

	private ArrayList<AbstractHilbertRtreePage<T, S>> getCooperatingSiblings() {
		if (parent == null) {
			return new ArrayList<AbstractHilbertRtreePage<T, S>>();
		}

		int index = parent.branch.indexOfObject(this);
		int offset = 0;
		int max = 0;
		int siblings = Math.min(cooperatingSiblings + 1, parent.n);

		if (index - siblings / 2 < 0) {
			offset = 0;
			max = siblings;
		} else if (index - siblings / 2 + siblings > parent.n) {
			max = parent.n;
			offset = parent.n - siblings;
		} else {
			offset = index - siblings / 2;
			max = offset + siblings;
		}

		Assert.that("offset=" + offset + " index=" + index + " max=" + max + " n=" + parent.n,
				0 <= offset && offset <= index && index <= max && max <= parent.n);

		ArrayList<AbstractHilbertRtreePage<T, S>> pages = new ArrayList<AbstractHilbertRtreePage<T, S>>(
				siblings);

		for (int i = offset; i < max; i++) {
			pages.add(parent.<AbstractHilbertRtreePage<T, S>> getBranch(i));
		}

		return pages;
	}

	private void addBranch(S shape, Object obj) {
		int pos = 0;
		long hilbert = isLeaf ? shape.linearOderValue()
				: ((AbstractHilbertRtreePage<T, S>) obj).largestHilbertValue;
		// find position in child list
		pos = findPositionInBranch(hilbert);
		// insert into child list
		branch.add(pos, obj);
		branch.setSize(capacity());
		S[] shapes = getShapes();
		System.arraycopy(shapes, pos, shapes, pos + 1, n - pos);
		shapes[pos] = shape;
		// set parent if page
		if (obj instanceof AbstractHilbertRtreePage<?, ?>) {
			this.<AbstractHilbertRtreePage<T, S>> getBranch(pos).parent = this;
		}
		if (pos == n - 1) {
			largestHilbertValue = shapes[pos].linearOderValue();
		}
		n += 1;
	}

	/**
	 * @param hilbert
	 *            hilbert value
	 * @return position for hilbert value in branch
	 */
	private int findPositionInBranch(long hilbert) {
		if (n == 0)
			return 0;

		int i = n / 2;
		int upperBoundary = n - 1;
		int lowerBoundary = 0;

		if (isLeaf) {
			S[] shapes = getShapes();

			while (true) {
				if (shapes[i].linearOderValue() < hilbert) {
					if (i == upperBoundary) {
						return upperBoundary + 1;
					}
					lowerBoundary = i;
					i += Math.max((upperBoundary - i) / 2, 1);
				} else {
					if (i == 0 || shapes[i - 1].linearOderValue() < hilbert) {
						return i;
					}
					upperBoundary = i;
					i -= Math.max((i - lowerBoundary) / 2, 1);
				}
			}
		}
		while (true) {
			if (this.<AbstractHilbertRtreePage<T, S>> getBranch(i).largestHilbertValue < hilbert) {
				if (i == upperBoundary) {
					return upperBoundary;
				}
				lowerBoundary = i;
				i += Math.max((upperBoundary - i) / 2, 1);
			} else {
				if (i == 0
						|| this.<AbstractHilbertRtreePage<T, S>> getBranch(i - 1).largestHilbertValue < hilbert) {
					return i;
				}
				upperBoundary = i;
				i -= Math.max((i - lowerBoundary) / 2, 1);
			}
		}
	}

	private void removeBranch(int i) {
		if (i < 0)
			return;
		n -= 1;
		S[] shapes = getShapes();
		System.arraycopy(shapes, i + 1, shapes, i, n - i);
		branch.remove(i);
		branch.setSize(capacity());
		if (i == n && n != 0) {
			largestHilbertValue = shapes[n - 1].linearOderValue();
		}
		modify();
	}

	private void setBranch(int i, S shape, Object obj) {
		setShape(i, shape);
		branch.setObject(i, obj);
		if (obj instanceof AbstractHilbertRtreePage<?, ?>) {
			this.<AbstractHilbertRtreePage<T, S>> getBranch(i).parent = this;
		}
	}

	private AbstractHilbertRtreePage<T, S> findContainingLeaf(S shape, int level) {
		if (level - 1 != 0) { /* this is an internal node in the tree */
			for (int i = 0; i < n; i++) {
				if (shape.intersects(getShape(i))) {
					return this.<AbstractHilbertRtreePage<T, S>> getBranch(i)
							.findContainingLeaf(shape, level - 1);
				}
			}
			return null;
		}
		return this;
	}

	@Override
	public Iterator<T> iterator(int level) {
		return new HilbertIterator(this, level);
	}

}
