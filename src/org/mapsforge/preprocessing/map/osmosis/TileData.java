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
package org.mapsforge.preprocessing.map.osmosis;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mapsforge.core.GeoCoordinate;

class TileData {

	private Set<TDNode> pois;
	private Set<TDWay> ways;

	TileData() {
		this.pois = new HashSet<TDNode>();
		this.ways = new HashSet<TDWay>();
	}

	void addPOI(TDNode poi) {
		pois.add(poi);
	}

	void addWay(TDWay way) {
		ways.add(way);
	}

	void removeWay(TDWay way) {
		ways.remove(way);
	}

	Map<Byte, List<TDNode>> poisByZoomlevel(byte minValidZoomlevel,
			byte maxValidZoomlevel) {
		HashMap<Byte, List<TDNode>> poisByZoomlevel = new HashMap<Byte, List<TDNode>>();
		for (TDNode poi : pois) {
			byte zoomlevel = poi.getMinimumZoomLevel();
			if (zoomlevel > maxValidZoomlevel)
				zoomlevel = maxValidZoomlevel;
			if (zoomlevel < minValidZoomlevel)
				zoomlevel = minValidZoomlevel;
			List<TDNode> group = poisByZoomlevel.get(zoomlevel);
			if (group == null)
				group = new ArrayList<TDNode>();
			group.add(poi);
			poisByZoomlevel.put(zoomlevel, group);
		}

		return poisByZoomlevel;
	}

	Map<Byte, List<TDWay>> waysByZoomlevel(byte minValidZoomlevel, byte maxValidZoomlevel) {
		HashMap<Byte, List<TDWay>> waysByZoomlevel = new HashMap<Byte, List<TDWay>>();
		for (TDWay way : ways) {
			byte zoomlevel = way.getMinimumZoomLevel();
			if (zoomlevel > maxValidZoomlevel)
				zoomlevel = maxValidZoomlevel;
			if (zoomlevel < minValidZoomlevel)
				zoomlevel = minValidZoomlevel;
			List<TDWay> group = waysByZoomlevel.get(zoomlevel);
			if (group == null)
				group = new ArrayList<TDWay>();
			group.add(way);
			waysByZoomlevel.put(zoomlevel, group);
		}

		return waysByZoomlevel;
	}

	static class TDNode {

		private final long id;
		private final int latitude;
		private final int longitude;

		private final short elevation;
		private final String houseNumber;
		private final byte layer;
		private final String name;
		private EnumSet<PoiEnum> tags;

		TDNode(long id, int latitude, int longitude, short elevation, byte layer,
				String houseNumber, String name) {
			this.id = id;
			this.latitude = latitude;
			this.longitude = longitude;
			this.elevation = elevation;
			this.houseNumber = houseNumber;
			this.layer = layer;
			this.name = name;
		}

		byte getMinimumZoomLevel() {
			byte min = Byte.MAX_VALUE;
			if (tags == null)
				return min;
			for (PoiEnum poiEnum : tags) {
				if (poiEnum.zoomlevel() < min)
					min = poiEnum.zoomlevel();
			}

			return min;
		}

		EnumSet<PoiEnum> getTags() {
			return tags;
		}

		void setTags(EnumSet<PoiEnum> tags) {
			this.tags = tags;
		}

		long getId() {
			return id;
		}

		int getLatitude() {
			return latitude;
		}

		int getLongitude() {
			return longitude;
		}

		short getElevation() {
			return elevation;
		}

		String getHouseNumber() {
			return houseNumber;
		}

		byte getLayer() {
			return layer;
		}

		String getName() {
			return name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (id ^ (id >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TDNode other = (TDNode) obj;
			if (id != other.id)
				return false;
			return true;
		}

	}

	static class TDWay {
		private final long id;
		private final byte layer;
		private final String name;
		private final String ref;
		private EnumSet<WayEnum> tags;
		private short waytype;
		private final TDNode[] wayNodes;

		// private boolean innerWay = false;

		// boolean isInnerWay() {
		// return innerWay;
		// }
		//
		// void setInnerWay(boolean innerWay) {
		// this.innerWay = innerWay;
		// }

		TDWay(long id, byte layer, String name, String ref, TDNode[] wayNodes) {
			this.id = id;
			this.layer = layer;
			this.name = name;
			this.ref = ref;
			this.wayNodes = wayNodes;
		}

		TDWay(long id, byte layer, String name, String ref, EnumSet<WayEnum> tags,
				short waytype, TDNode[] wayNodes) {
			this.id = id;
			this.layer = layer;
			this.name = name;
			this.ref = ref;
			this.tags = tags;
			this.waytype = waytype;
			this.wayNodes = wayNodes;
		}

		List<GeoCoordinate> wayNodesAsCoordinateList() {
			List<GeoCoordinate> waynodeCoordinates = new ArrayList<GeoCoordinate>();
			for (TDNode waynode : wayNodes) {
				waynodeCoordinates.add(new GeoCoordinate(waynode.getLatitude(),
						waynode.getLongitude()));
			}

			return waynodeCoordinates;
		}

		byte getMinimumZoomLevel() {
			byte min = Byte.MAX_VALUE;
			if (tags == null)
				return min;
			for (WayEnum wayEnum : tags) {
				if (wayEnum.zoomlevel() < min)
					min = wayEnum.zoomlevel();
			}

			return min;
		}

		long getId() {
			return id;
		}

		byte getLayer() {
			return layer;
		}

		String getName() {
			return name;
		}

		String getRef() {
			return ref;
		}

		EnumSet<WayEnum> getTags() {
			return tags;
		}

		short getWaytype() {
			return waytype;
		}

		void setWaytype(short waytype) {
			this.waytype = waytype;
		}

		void setTags(EnumSet<WayEnum> tags) {
			this.tags = tags;
		}

		void addTag(WayEnum tag) {
			if (tags == null)
				tags = EnumSet.of(tag);
			else
				tags.add(tag);

		}

		TDNode[] getWayNodes() {
			return wayNodes;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (id ^ (id >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TDWay other = (TDWay) obj;
			if (id != other.id)
				return false;
			return true;
		}

	}
}
