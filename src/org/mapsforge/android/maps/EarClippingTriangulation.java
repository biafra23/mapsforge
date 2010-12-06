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

import java.util.ArrayList;

/**
 * A class for polygon triangulation using the ear clipping algorithm
 * 
 * @author jonas.hoffmann
 */
class EarClippingTriangulation {
	/* current number of polygon vertices */
	private int num;

	/* contain the triangle points after triangulation */
	private ArrayList<ImmutablePoint> trianglePoints;

	/* x coordinates of the input polygon */
	private float[] xCoords;

	/* y coordinates of the input polygon */
	private float[] yCoords;

	/**
	 * initialize and then triangulate
	 * 
	 * @param polyCoords
	 *            the coordinates of the input polygon, x and y alternating
	 * 
	 */
	EarClippingTriangulation(float[] polyCoords) {
		boolean clockwise = CoastlineWay.isClockWise(polyCoords);
		this.num = polyCoords.length / 2;
		int i;

		/* closed polygon? skip duplicate coordinate */
		if ((polyCoords[0] == polyCoords[polyCoords.length - 2])
				&& (polyCoords[1] == polyCoords[polyCoords.length - 1])) {
			this.num--;
		}

		this.xCoords = new float[this.num];
		this.yCoords = new float[this.num];
		this.trianglePoints = new ArrayList<ImmutablePoint>((this.num - 2) * 3); // any polygon
		// triangulation has
		// n-2 triangles

		for (i = 0; i < this.num; i++) {
			if (clockwise) {
				// split into x and y coords
				this.xCoords[i] = polyCoords[i * 2];
				this.yCoords[i] = polyCoords[i * 2 + 1];
			} else {
				// anti-clockwise order
				// split into x and y coords in reverse order
				this.xCoords[this.num - 1 - i] = polyCoords[i * 2];
				this.yCoords[this.num - 1 - i] = polyCoords[i * 2 + 1];
			}
		}
		doTriangulation();
	}

	/**
	 * clip an ear at position p
	 * 
	 * @param p
	 *            number of the polygon vertex at which the ear is clipped
	 */
	private void clipEarAtPosition(int p) {
		// Logger.d("clipping ear at position: " + p + " number of polygon vertices: " + num);

		/*
		 * add the new triangle to the list
		 */
		if ((p > 0) && (p < this.num - 1)) {
			this.trianglePoints
					.add(new ImmutablePoint(this.xCoords[p - 1], this.yCoords[p - 1]));
			this.trianglePoints.add(new ImmutablePoint(this.xCoords[p], this.yCoords[p]));
			this.trianglePoints
					.add(new ImmutablePoint(this.xCoords[p + 1], this.yCoords[p + 1]));
		} else if (0 == p) {
			this.trianglePoints.add(new ImmutablePoint(this.xCoords[this.num - 1],
					this.yCoords[this.num - 1]));
			this.trianglePoints.add(new ImmutablePoint(this.xCoords[0], this.yCoords[0]));
			this.trianglePoints.add(new ImmutablePoint(this.xCoords[1], this.yCoords[1]));
		} else if (this.num - 1 == p) {
			this.trianglePoints.add(new ImmutablePoint(this.xCoords[this.num - 2],
					this.yCoords[this.num - 2]));
			this.trianglePoints.add(new ImmutablePoint(this.xCoords[this.num - 1],
					this.yCoords[this.num - 1]));
			this.trianglePoints.add(new ImmutablePoint(this.xCoords[0], this.yCoords[0]));
		}

		/* remove point from x and y coordinate arrays */
		for (int i = p; i < this.num - 1; i++) {
			this.xCoords[i] = this.xCoords[i + 1];
			this.yCoords[i] = this.yCoords[i + 1];
		}
		/* adjust number of points left in the polygon */

		this.num--;
	}

	/**
	 * triangulate by finding ears to clip and clipping them as long as there are more than 3
	 * vertices left in the polygon.
	 */
	private void doTriangulation() {
		int pos;

		while (this.num > 3) {
			// as long as there are more than 2 points (at least 1 triangle)

			pos = 0;
			// find position to clip
			// TODO: for negative coordinates this does not always find an ear (convex test
			// wrong)
			for (int i = 0; i < this.num; i++) {
				// find position to clip an ear
				if (earAtPoint(i)) {
					pos = i;
					break;
				}
			}
			clipEarAtPosition(pos);
		}
		// if 3 points are left, clip this last triangle anywhere
		if (this.num == 3) {
			clipEarAtPosition(0);
		}
	}

	/**
	 * test for an ear at vertex p
	 * 
	 * @param p
	 *            number of the polygon vertex to test
	 * @return true if there is an ear at vertex p
	 */
	private boolean earAtPoint(int p) {
		// Logger.d("check for ear at point " + p);
		if (p == 0) {
			return isEar(this.xCoords[this.num - 1], this.yCoords[this.num - 1],
					this.xCoords[0], this.yCoords[0], this.xCoords[1], this.yCoords[1]);
		} else if (p == this.num - 1) {
			return isEar(this.xCoords[this.num - 2], this.yCoords[this.num - 2],
					this.xCoords[this.num - 1], this.yCoords[this.num - 1], this.xCoords[0],
					this.yCoords[0]);
		}

		return isEar(this.xCoords[p - 1], this.yCoords[p - 1], this.xCoords[p],
				this.yCoords[p], this.xCoords[p + 1], this.yCoords[p + 1]);
	}

	/**
	 * test if the triangle (x1,y1) (x1,y2) (x3,y3) is convex
	 * 
	 * @param x1
	 *            x coordinate of first triangle vertex
	 * @param y1
	 *            y coordinate of first triangle vertex
	 * @param x2
	 *            x coordinate of second triangle vertex
	 * @param y2
	 *            y coordinate of second triangle vertex
	 * @param x3
	 *            x coordinate of third triangle vertex
	 * @param y3
	 *            y coordinate of third triangle vertex
	 * 
	 * @return true if triangle (x1,y1) (x1,y2) (x3,y3) is convex
	 */
	private boolean isConvex(float x1, float y1, float x2, float y2, float x3, float y3) {
		/*
		 * triangle area = 0.5 * (x1 * (y3 - y2) + x2 * (y1 - y3) + x3 * (y2 - y1)) is negative
		 * for convex and positive for concave triangle
		 */

		if ((x1 * (y3 - y2) + x2 * (y1 - y3) + x3 * (y2 - y1)) < 0) {
			return true;
		}
		return false;
	}

	/**
	 * test, if the vertex at position p is convex
	 * 
	 * @param p
	 *            number of vertex to test
	 * @return true if polygon is convex at vertex p
	 */
	private boolean isConvexPoint(int p) {
		if (p == 0) {
			return isConvex(this.xCoords[this.num - 1], this.yCoords[this.num - 1],
					this.xCoords[0], this.yCoords[0], this.xCoords[1], this.yCoords[1]);
		} else if (p == this.num - 1) {
			return isConvex(this.xCoords[this.num - 2], this.yCoords[this.num - 2],
					this.xCoords[this.num - 1], this.yCoords[this.num - 1], this.xCoords[0],
					this.yCoords[0]);
		}
		return isConvex(this.xCoords[p - 1], this.yCoords[p - 1], this.xCoords[p],
				this.yCoords[p], this.xCoords[p + 1], this.yCoords[p + 1]);
	}

	/**
	 * test if the triangle (x1,y1) (x1,y2) (x3,y3) is an ear
	 * 
	 * @param x1
	 *            x coordinate of first triangle vertex
	 * @param y1
	 *            y coordinate of first triangle vertex
	 * @param x2
	 *            x coordinate of second triangle vertex
	 * @param y2
	 *            y coordinate of second triangle vertex
	 * @param x3
	 *            x coordinate of third triangle vertex
	 * @param y3
	 *            y coordinate of third triangle vertex
	 * 
	 * @return true if the triangle (x1,y1) (x1,y2) (x3,y3) is an ear, false otherwise
	 */
	private boolean isEar(float x1, float y1, float x2, float y2, float x3, float y3) {
		// make sure the triangle is convex
		if (!isConvex(x1, y1, x2, y2, x3, y3)) {
			// Logger.d("not convex at " + x1 + "," + y1 + " " + x2 + "," + y2 + " " + x3 + ","
			// + y3);
			return false;
		}

		// if it contains no point, it's an ear
		return !pointInsideTriangle(x1, y1, x2, y2, x3, y3);
	}

	/**
	 * 
	 * test if any points of the polygon lie inside the triangle (x1,y1) (x1,y2) (x3,y3)
	 * 
	 * @param x1
	 *            x coordinate of first triangle vertex
	 * @param y1
	 *            y coordinate of first triangle vertex
	 * @param x2
	 *            x coordinate of second triangle vertex
	 * @param y2
	 *            y coordinate of second triangle vertex
	 * @param x3
	 *            x coordinate of third triangle vertex
	 * @param y3
	 *            y coordinate of third triangle vertex
	 * 
	 * @return true if any point of the polygon lies inside the triangle (x1,y1) (x1,y2) (x3,y3)
	 */
	private boolean pointInsideTriangle(float x1, float y1, float x2, float y2, float x3,
			float y3) {
		for (int i = 0; i < this.num; i++) {
			if ((!isConvexPoint(i)) /* point is concave */
					&& (((this.xCoords[i] != x1) && (this.yCoords[i] != y1))
							|| ((this.xCoords[i] != x2) && (this.yCoords[i] != y2)) || ((this.xCoords[i] != x3) && (this.yCoords[i] != y3)))) {

				boolean convex1 = isConvex(x1, y1, x2, y2, this.xCoords[i], this.yCoords[i]);
				boolean convex2 = isConvex(x2, y2, x3, y3, this.xCoords[i], this.yCoords[i]);
				boolean convex3 = isConvex(x3, y3, x1, y1, this.xCoords[i], this.yCoords[i]);

				if ((!convex1 && !convex2 && !convex3) || (convex1 && convex2 && convex3)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @return triangle points as ArrayList of Points
	 */
	ArrayList<ImmutablePoint> getTriangles() {
		return this.trianglePoints;
	}

	/**
	 * convert ArrayList of triangle points to float array
	 * 
	 * @return coordinates as float array
	 */
	float[] getTrianglesAsFloatArray() {
		int s = this.trianglePoints.size();
		float[] coords = new float[s * 2];

		for (int i = 0; i < s; i++) {
			ImmutablePoint p = this.trianglePoints.get(i);
			coords[i * 2] = p.x;
			coords[i * 2 + 1] = p.y;
		}
		return coords;
	}
}