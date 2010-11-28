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
package org.mapsforge.android.maps;

/**
 * An immutable container for the two endpoints of a way. The purpose of this class is to
 * compare segments by looking at their first and last point.
 */
class EndPoints {
	private final ImmutablePoint end;
	private final int hashCode;
	private EndPoints other;
	private final ImmutablePoint start;

	/**
	 * Creates a new EndPoints instance with the given points.
	 * 
	 * @param start
	 *            the start point.
	 * @param end
	 *            the end point.
	 */
	EndPoints(ImmutablePoint start, ImmutablePoint end) {
		this.start = start;
		this.end = end;
		this.hashCode = calculateHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof EndPoints)) {
			return false;
		} else {
			this.other = (EndPoints) obj;
			if (!this.start.equals(this.other.start)) {
				return false;
			} else if (!this.end.equals(this.other.end)) {
				return false;
			}
			return true;
		}
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	/**
	 * Calculates the hash value of this object.
	 * 
	 * @return the hash value of this object.
	 */
	private int calculateHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.start == null) ? 0 : this.start.hashCode());
		result = prime * result + ((this.end == null) ? 0 : this.end.hashCode());
		return result;
	}
}