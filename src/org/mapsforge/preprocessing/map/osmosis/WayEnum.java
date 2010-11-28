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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 
 * @author bross
 * 
 */
enum WayEnum {
	ADMIN_LEVEL$2(WayType.UNCLASSIFIED, (byte) 6),
	ADMIN_LEVEL$4(WayType.UNCLASSIFIED, (byte) 12),
	ADMIN_LEVEL$6(WayType.UNCLASSIFIED, (byte) 15),
	ADMIN_LEVEL$8(WayType.UNCLASSIFIED, (byte) 16),
	ADMIN_LEVEL$9(WayType.UNCLASSIFIED, (byte) 16),
	ADMIN_LEVEL$10(WayType.UNCLASSIFIED, (byte) 16),
	AERIALWAY$CABLE_CAR(WayType.UNCLASSIFIED, (byte) 16),
	AERIALWAY$CHAIR_LIFT(WayType.UNCLASSIFIED, (byte) 16),
	AEROWAY$AERODROME(WayType.UNCLASSIFIED, (byte) 13),
	AEROWAY$APRON(WayType.UNCLASSIFIED, (byte) 13),
	AEROWAY$HELIPAD(WayType.UNCLASSIFIED, (byte) 17),
	AEROWAY$RUNWAY(WayType.UNCLASSIFIED, (byte) 10),
	AEROWAY$TAXIWAY(WayType.UNCLASSIFIED, (byte) 10),
	AEROWAY$TERMINAL(WayType.UNCLASSIFIED, (byte) 16),
	AMENITY$COLLEGE(WayType.AMENITY, (byte) 15),
	AMENITY$EMBASSY(WayType.AMENITY, (byte) 15),
	AMENITY$FOUNTAIN(WayType.AMENITY, (byte) 15),
	AMENITY$GRAVE_YARD(WayType.AMENITY, (byte) 15),
	AMENITY$HOSPITAL(WayType.AMENITY, (byte) 15),
	AMENITY$PARKING(WayType.AMENITY, (byte) 15),
	AMENITY$SCHOOL(WayType.AMENITY, (byte) 15),
	AMENITY$UNIVERSITY(WayType.AMENITY, (byte) 15),
	AREA$YES(WayType.UNCLASSIFIED, (byte) 127),
	BARRIER$FENCE(WayType.UNCLASSIFIED, (byte) 16),
	BARRIER$WALL(WayType.UNCLASSIFIED, (byte) 17),
	BOUNDARY$ADMINISTRATIVE(WayType.UNCLASSIFIED, (byte) 127),
	BOUNDARY$NATIONAL_PARK(WayType.UNCLASSIFIED, (byte) 12),
	BRIDGE$YES(WayType.UNCLASSIFIED, (byte) 127),
	BUILDING$APARTMENTS(WayType.BUILDING, (byte) 16),
	BUILDING$EMBASSY(WayType.BUILDING, (byte) 16),
	BUILDING$GOVERNMENT(WayType.BUILDING, (byte) 16),
	BUILDING$GYM(WayType.BUILDING, (byte) 16),
	BUILDING$ROOF(WayType.BUILDING, (byte) 16),
	BUILDING$SPORTS(WayType.BUILDING, (byte) 16),
	BUILDING$TRAIN_STATION(WayType.BUILDING, (byte) 16),
	BUILDING$UNIVERSITY(WayType.BUILDING, (byte) 16),
	BUILDING$YES(WayType.BUILDING, (byte) 16),
	HIGHWAY$BRIDLEWAY(WayType.HIGHWAY, (byte) 13),
	HIGHWAY$BUS_GUIDEWAY(WayType.HIGHWAY, (byte) 14),
	HIGHWAY$CONSTRUCTION(WayType.HIGHWAY, (byte) 15),
	HIGHWAY$CYCLEWAY(WayType.HIGHWAY, (byte) 13),
	HIGHWAY$FOOTWAY(WayType.HIGHWAY, (byte) 15),
	HIGHWAY$LIVING_STREET(WayType.HIGHWAY, (byte) 14),
	HIGHWAY$MOTORWAY(WayType.HIGHWAY, (byte) 8),
	HIGHWAY$MOTORWAY_LINK(WayType.HIGHWAY, (byte) 8),
	HIGHWAY$PATH(WayType.HIGHWAY, (byte) 14),
	HIGHWAY$PEDESTRIAN(WayType.HIGHWAY, (byte) 14),
	HIGHWAY$PRIMARY(WayType.HIGHWAY, (byte) 8),
	HIGHWAY$PRIMARY_LINK(WayType.HIGHWAY, (byte) 8),
	HIGHWAY$RACEWAY(WayType.HIGHWAY, (byte) 13),
	HIGHWAY$RESIDENTIAL(WayType.HIGHWAY, (byte) 14),
	HIGHWAY$ROAD(WayType.HIGHWAY, (byte) 12),
	HIGHWAY$SECONDARY(WayType.HIGHWAY, (byte) 9),
	HIGHWAY$SERVICE(WayType.HIGHWAY, (byte) 14),
	HIGHWAY$SERVICES(WayType.HIGHWAY, (byte) 14),
	HIGHWAY$STEPS(WayType.HIGHWAY, (byte) 16),
	HIGHWAY$TERTIARY(WayType.HIGHWAY, (byte) 10),
	HIGHWAY$TRACK(WayType.HIGHWAY, (byte) 12),
	HIGHWAY$TRUNK(WayType.HIGHWAY, (byte) 8),
	HIGHWAY$TRUNK_LINK(WayType.HIGHWAY, (byte) 8),
	HIGHWAY$UNCLASSIFIED(WayType.HIGHWAY, (byte) 13),
	HISTORIC$RUINS(WayType.UNCLASSIFIED, (byte) 17),
	LANDUSE$ALLOTMENTS(WayType.LANDUSE, (byte) 12),
	LANDUSE$BASIN(WayType.LANDUSE, (byte) 14),
	LANDUSE$BROWNFIELD(WayType.LANDUSE, (byte) 12),
	LANDUSE$CEMETERY(WayType.LANDUSE, (byte) 12),
	LANDUSE$COMMERCIAL(WayType.LANDUSE, (byte) 12),
	LANDUSE$CONSTRUCTION(WayType.LANDUSE, (byte) 14),
	LANDUSE$FARM(WayType.LANDUSE, (byte) 12),
	LANDUSE$FARMLAND(WayType.LANDUSE, (byte) 12),
	LANDUSE$FOREST(WayType.LANDUSE, (byte) 12),
	LANDUSE$GRASS(WayType.LANDUSE, (byte) 12),
	LANDUSE$GREENFIELD(WayType.LANDUSE, (byte) 12),
	LANDUSE$INDUSTRIAL(WayType.LANDUSE, (byte) 12),
	LANDUSE$MILITARY(WayType.LANDUSE, (byte) 12),
	LANDUSE$QUARRY(WayType.LANDUSE, (byte) 12),
	LANDUSE$RAILWAY(WayType.LANDUSE, (byte) 12),
	LANDUSE$RECREATION_GROUND(WayType.LANDUSE, (byte) 12),
	LANDUSE$RESERVOIR(WayType.LANDUSE, (byte) 12),
	LANDUSE$RESIDENTIAL(WayType.LANDUSE, (byte) 12),
	LANDUSE$RETAIL(WayType.LANDUSE, (byte) 12),
	LANDUSE$VILLAGE_GREEN(WayType.LANDUSE, (byte) 12),
	LANDUSE$VINEYARD(WayType.LANDUSE, (byte) 12),
	LANDUSE$WOOD(WayType.LANDUSE, (byte) 12),
	LEISURE$COMMON(WayType.LEISURE, (byte) 12),
	LEISURE$GARDEN(WayType.LEISURE, (byte) 12),
	LEISURE$GOLF_COURSE(WayType.LEISURE, (byte) 12),
	LEISURE$PARK(WayType.LEISURE, (byte) 12),
	LEISURE$PITCH(WayType.LEISURE, (byte) 15),
	LEISURE$PLAYGROUND(WayType.LEISURE, (byte) 16),
	LEISURE$SPORTS_CENTRE(WayType.LEISURE, (byte) 12),
	LEISURE$STADIUM(WayType.LEISURE, (byte) 12),
	LEISURE$TRACK(WayType.LEISURE, (byte) 15),
	LEISURE$WATER_PARK(WayType.LEISURE, (byte) 15),
	MAN_MADE$PIER(WayType.UNCLASSIFIED, (byte) 15),
	MILITARY$AIRFIELD(WayType.UNCLASSIFIED, (byte) 12),
	MILITARY$BARRACKS(WayType.UNCLASSIFIED, (byte) 12),
	MILITARY$NAVAL_BASE(WayType.UNCLASSIFIED, (byte) 12),
	NATURAL$BEACH(WayType.NATURAL, (byte) 14),
	NATURAL$COASTLINE(WayType.NATURAL, (byte) 0),
	NATURAL$GLACIER(WayType.NATURAL, (byte) 12),
	NATURAL$HEATH(WayType.NATURAL, (byte) 12),
	NATURAL$LAND(WayType.NATURAL, (byte) 12),
	NATURAL$SCRUB(WayType.NATURAL, (byte) 12),
	NATURAL$WATER(WayType.NATURAL, (byte) 12),
	NATURAL$WOOD(WayType.NATURAL, (byte) 12),
	PLACE$LOCALITY(WayType.UNCLASSIFIED, (byte) 17),
	RAILWAY$LIGHT_RAIL(WayType.RAILWAY, (byte) 12),
	RAILWAY$RAIL(WayType.RAILWAY, (byte) 10),
	RAILWAY$STATION(WayType.RAILWAY, (byte) 13),
	RAILWAY$SUBWAY(WayType.RAILWAY, (byte) 13),
	RAILWAY$TRAM(WayType.RAILWAY, (byte) 13),
	ROUTE$FERRY(WayType.UNCLASSIFIED, (byte) 12),
	SPORT$GOLF(WayType.UNCLASSIFIED, (byte) 15),
	SPORT$SHOOTING(WayType.UNCLASSIFIED, (byte) 15),
	SPORT$SOCCER(WayType.UNCLASSIFIED, (byte) 15),
	SPORT$TENNIS(WayType.UNCLASSIFIED, (byte) 15),
	TOURISM$ATTRACTION(WayType.UNCLASSIFIED, (byte) 15),
	TOURISM$HOSTEL(WayType.UNCLASSIFIED, (byte) 15),
	TOURISM$ZOO(WayType.UNCLASSIFIED, (byte) 12),
	TUNNEL$NO(WayType.UNCLASSIFIED, (byte) 127),
	TUNNEL$YES(WayType.UNCLASSIFIED, (byte) 127),
	WATERWAY$CANAL(WayType.WATERWAY, (byte) 9),
	WATERWAY$DAM(WayType.WATERWAY, (byte) 12),
	WATERWAY$DRAIN(WayType.WATERWAY, (byte) 12),
	WATERWAY$RIVER(WayType.WATERWAY, (byte) 8),
	WATERWAY$RIVERBANK(WayType.WATERWAY, (byte) 8),
	WATERWAY$STREAM(WayType.WATERWAY, (byte) 6);

	private final byte zoomlevel;
	private final WayType wayType;
	private static final byte INVALID_ZOOMLEVEL = Byte.MAX_VALUE;

	private WayEnum(WayType wayType, byte zoomlevel) {
		this.zoomlevel = zoomlevel;
		this.wayType = wayType;
	}

	public byte zoomlevel() {
		return zoomlevel;
	}

	public WayType waytype() {
		return wayType;
	}

	public boolean associatedWithValidZoomlevel() {
		return zoomlevel != INVALID_ZOOMLEVEL;
	}

	@Override
	public String toString() {
		return name().replaceFirst("\\$", "=").toLowerCase(Locale.US);
	}

	private static final Map<String, WayEnum> stringToEnum =
			new HashMap<String, WayEnum>();

	static {
		for (WayEnum way : values()) {
			stringToEnum.put(way.toString(), way);
		}
	}

	public static WayEnum fromString(String symbol) {
		return stringToEnum.get(symbol);
	}

	public enum WayType {
		HIGHWAY, RAILWAY, BUILDING, LANDUSE, LEISURE, AMENITY, NATURAL, WATERWAY, UNCLASSIFIED;
	}

}
