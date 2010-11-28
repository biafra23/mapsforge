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
package org.mapsforge.preprocessing.map;

import java.util.LinkedList;
import java.util.SortedSet;

class MapElementWay implements Comparable<MapElementWay> {
	long id;
	byte innerWays;
	byte layer = 5;
	SortedSet<Long> multipolygonInnerMemberIds;
	SortedSet<Long> multipolygonOuterMemberIds;
	String name = "";
	int newId;
	LinkedList<Long> nodesSequence;
	byte tagBitmap;
	LinkedList<String> tags;
	byte zoomLevel;

	int wayType = 1;
	int convexness;
	short tileBitmask;
	String ref = "";

	MapElementWay(long id) {
		this.id = id;
		this.tags = new LinkedList<String>();
		this.nodesSequence = new LinkedList<Long>();
		this.zoomLevel = Byte.MAX_VALUE;
		this.multipolygonInnerMemberIds = null;
		this.multipolygonOuterMemberIds = null;
		this.tagBitmap = 0;
		this.tileBitmask = 0;
	}

	@Override
	public int compareTo(MapElementWay way) {
		if (this.zoomLevel > way.zoomLevel) {
			return 1;
		} else if (this.zoomLevel < way.zoomLevel) {
			return -1;
		} else if (this.id > way.id) {
			return 1;
		} else if (this.id < way.id) {
			return -1;
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MapElementWay) {
			MapElementWay way = (MapElementWay) obj;
			return this.id == way.id;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (int) this.id;
	}
}