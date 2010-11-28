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

class Rect implements SpatialShape<Rect> {
	static final int memory_size = 4 * 4; // 4 int values

	public static long joinArea(Rect a, Rect b) {
		int left = (a.left < b.left) ? a.left : b.left;
		int right = (a.right > b.right) ? a.right : b.right;
		int top = (a.top < b.top) ? a.top : b.top;
		int bottom = (a.bottom > b.bottom) ? a.bottom : b.bottom;
		return (long) (bottom - top) * (right - left);
	}

	int top;
	int left;
	int bottom;
	int right;

	// not persisted
	transient long linearOrderValue;
	transient Point center;

	public Rect() {
		// required by perst
	}

	public Rect(Rect r) {
		this.top = r.top;
		this.left = r.left;
		this.bottom = r.bottom;
		this.right = r.right;
	}

	public Rect(int top, int left, int bottom, int right) {
		super();
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}

	@Override
	public long area() {
		return (long) (bottom - top) * (right - left);
	}

	@Override
	public boolean contains(Rect shape) {
		return left <= shape.left && top <= shape.top && right >= shape.right
				&& bottom >= shape.bottom;
	}

	@Override
	public boolean intersects(Rect shape) {
		return left <= shape.right && top <= shape.bottom && right >= shape.left
				&& bottom >= shape.top;
	}

	@Override
	public Rect join(Rect shape) {
		linearOrderValue = -1;
		center = null;

		if (left > shape.left) {
			left = shape.left;
		}
		if (right < shape.right) {
			right = shape.right;
		}
		if (top > shape.top) {
			top = shape.top;
		}
		if (bottom < shape.bottom) {
			bottom = shape.bottom;
		}
		return this;
	}

	public Point center() {
		if (center == null) {
			center = new Point((left + right) / 2, (top + bottom) / 2);
		}
		return center;
	}

	@Override
	public long joinArea(Rect shape) {
		linearOrderValue = -1;
		center = null;

		int l = (this.left < shape.left) ? this.left : shape.left;
		int r = (this.right > shape.right) ? this.right : shape.right;
		int t = (this.top < shape.top) ? this.top : shape.top;
		int b = (this.bottom > shape.bottom) ? this.bottom : shape.bottom;
		return (long) (b - t) * (r - l);
	}

	/**
	 * Hash code consists of all rectangle coordinates
	 */
	@Override
	public int hashCode() {
		return top ^ (bottom << 1) ^ (left << 2) ^ (right << 3);
	}

	@Override
	public String toString() {
		return "top=" + top + ", left=" + left + ", bottom=" + bottom + ", right=" + right;
	}

	@Override
	public long linearOderValue() {
		if (linearOrderValue < 0) {
			linearOrderValue = Hilbert.computeValue(center().y, center().x);
		}
		return linearOrderValue;
	}

	public double distance(int x, int y) {
		if (x >= left && x <= right) {
			if (y >= top) {
				if (y <= bottom) {
					return 0;
				}
				return y - bottom;
			}
			return top - y;
		} else if (y >= top && y <= bottom) {
			if (x < left) {
				return left - x;
			}
			return x - right;
		}
		int dx = x < left ? left - x : x - right;
		int dy = y < top ? top - y : y - bottom;
		return Math.sqrt((double) dx * dx + (double) dy * dy);
	}

}
