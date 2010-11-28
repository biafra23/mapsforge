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


class RtreeBox implements SpatialShape<RtreeBox> {

	int top;
	int bottom;
	int left;
	int right;
	int front;
	int back;

	RtreeBox() {
		super();
	}

	RtreeBox(RtreeBox b) {
		this.top = b.top;
		this.bottom = b.bottom;
		this.left = b.left;
		this.right = b.right;
		this.front = b.front;
		this.back = b.back;
	}

	RtreeBox(int top, int bottom, int left, int right, int front, int back) {
		super();
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
		this.front = front;
		this.back = back;
	}

	RtreeBox(int x, int y, int z) {
		super();
		top = bottom = x;
		left = right = y;
		back = front = z;
	}

	@Override
	public boolean intersects(RtreeBox b) {
		return left <= b.right && top <= b.bottom && right >= b.left && bottom >= b.top
				&& back <= b.front && front >= b.back;
	}

	@Override
	public RtreeBox join(RtreeBox b) {
		this.top = Math.min(this.top, b.top);
		this.bottom = Math.max(this.bottom, b.bottom);
		this.left = Math.min(this.left, b.left);
		this.right = Math.max(this.right, b.right);
		this.front = Math.max(this.front, b.front);
		this.back = Math.min(this.back, b.back);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("top=").append(top).append(" ");
		sb.append("bottom=").append(bottom).append(" ");
		sb.append("left=").append(left).append(" ");
		sb.append("right=").append(right).append(" ");
		sb.append("back=").append(back).append(" ");
		sb.append("front=").append(front).append(" ");
		return sb.toString();
	}

	@Override
	public long area() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(RtreeBox shape) {
		return left <= shape.left && top <= shape.top && right >= shape.right
				&& bottom >= shape.bottom && shape.back >= back && shape.front <= front;
	}

	@Override
	public long joinArea(RtreeBox shape) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long linearOderValue() {
		int x = (left + right) / 2;
		int y = (top + bottom) / 2;
		int z = (back + front) / 2;

		return Hilbert.computeValue3D(x, y, z);
	}

}
