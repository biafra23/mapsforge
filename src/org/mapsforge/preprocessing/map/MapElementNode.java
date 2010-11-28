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

class MapElementNode implements Comparable<MapElementNode> {
	final long id;
	final int latitude;
	final int longitude;
	String name = "";
	LinkedList<String> tags;
	byte zoomLevel;

	int layer = 5;
	int elevation = 0;
	String housenumber = "";

	MapElementNode(long id, int latitude, int longitude) {
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.tags = new LinkedList<String>();
		this.zoomLevel = Byte.MAX_VALUE;
	}

	@Override
	public int compareTo(MapElementNode node) {
		if (this.zoomLevel > node.zoomLevel) {
			return 1;
		} else if (this.zoomLevel < node.zoomLevel) {
			return -1;
		} else if (this.id > node.id) {
			return 1;
		} else if (this.id < node.id) {
			return -1;
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapElementNode other = (MapElementNode) obj;
		if (id != other.id)
			return false;
		if (latitude != other.latitude)
			return false;
		if (longitude != other.longitude)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (tags == null) {
			if (other.tags != null)
				return false;
		} else if (!tags.equals(other.tags))
			return false;
		if (zoomLevel != other.zoomLevel)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + latitude;
		result = prime * result + longitude;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + zoomLevel;
		return result;
	}
}