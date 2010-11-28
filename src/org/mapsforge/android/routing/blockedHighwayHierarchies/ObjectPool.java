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
package org.mapsforge.android.routing.blockedHighwayHierarchies;

import java.util.ArrayList;

/**
 * This class implements an object pool, poolable objects can be borrowed and released back to
 * the pool. The pool only holds references to released objects, thus if the programmer forgets
 * to release an object, it will be wasted by the garbage collection later on. The Poolable
 * interface assures, that releasing an object twice does not imply errors.
 * 
 * @param <T>
 *            The type of objects to be pooled.
 */
final class ObjectPool<T extends ObjectPool.Poolable> {

	/**
	 * This class constructs new objects of the given type. This functionality is used when the
	 * pool is empty and is asked to borrow an object.
	 * 
	 * @param <T>
	 *            Type of objects the factory produces.
	 */
	public static interface PoolableFactory<T> {

		/**
		 * Produces objects.
		 * 
		 * @return A new Instance of the repective class.
		 */
		public T makeObject();

	}

	/**
	 * This interface allows easy verification if an object is pooled or borrowed. Both methods
	 * should only be called by the respective object pool.
	 */
	public static interface Poolable {

		/**
		 * @return Returns true if the object is released to the pool, that is to say it is not
		 *         in use.
		 */
		public boolean isReleased();

		/**
		 * @param b
		 *            sets the released status to the given value.
		 */
		public void setReleased(boolean b);
	}

	private final ArrayList<T> objects;
	private final PoolableFactory<T> factory;
	private int numBorrowed;

	/**
	 * Creates a new object pool with the specified initial size.
	 * 
	 * @param factory
	 *            the factory which is used to produce objects.
	 * @param initialSize
	 *            the initial number of poolable objects to be created.
	 */
	public ObjectPool(PoolableFactory<T> factory, int initialSize) {
		this.objects = new ArrayList<T>(initialSize);
		this.factory = factory;
		this.numBorrowed = 0;
		for (int i = 0; i < initialSize; i++) {
			objects.add(factory.makeObject());
		}
	}

	/**
	 * @return Returns a poolable object from the pool.
	 */
	public T borrow() {
		T obj;
		if (objects.size() == 0) {
			obj = factory.makeObject();
		} else {
			obj = objects.remove(objects.size() - 1);
		}
		numBorrowed++;
		obj.setReleased(false);
		return obj;
	}

	/**
	 * Releases an object to the pool. It must be taken care that the reference to this object
	 * is no more used in the code peaces after the call to release.
	 * 
	 * @param obj
	 *            the poolable object to be released back to the pool.
	 */
	public void release(T obj) {
		if (obj != null) {
			if (!obj.isReleased()) {
				objects.add(obj);
				numBorrowed--;
				obj.setReleased(true);
			}
		}
	}

	/**
	 * Removes all poolable objects from the pool. Normally they should than be targets of the
	 * garbage collection.
	 */
	public void clear() {
		this.objects.clear();
		this.numBorrowed = 0;
	}

	/**
	 * Gives the number of borrowed objects.
	 * 
	 * @return number of poolable objects borrowed.
	 */
	public int numBorrowed() {
		return numBorrowed;
	}

	/**
	 * Gives the number of poolable objects currently in the pool.
	 * 
	 * @return Returns the number of poolable objects currently in the pool.
	 */
	public int numReleased() {
		return objects.size();
	}

	@Override
	public String toString() {
		return "borrowed = " + numBorrowed + ", released = " + numReleased();
	}
}
