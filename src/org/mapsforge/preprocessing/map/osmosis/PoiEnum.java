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
enum PoiEnum {
	AEROWAY$GATE((byte) 17),
	AEROWAY$HELIPAD((byte) 17),
	AMENITY$ATM((byte) 17),
	AMENITY$BANK((byte) 17),
	AMENITY$BICYCLE_RENTAL((byte) 17),
	AMENITY$BUS_STATION((byte) 16),
	AMENITY$CAFE((byte) 17),
	AMENITY$CINEMA((byte) 17),
	AMENITY$FAST_FOOD((byte) 17),
	AMENITY$FOUNTAIN((byte) 16),
	AMENITY$FIRE_STATION((byte) 17),
	AMENITY$FUEL((byte) 17),
	AMENITY$HOSPITAL((byte) 16),
	AMENITY$LIBRARY((byte) 17),
	AMENITY$PARKING((byte) 17),
	AMENITY$PHARMACY((byte) 17),
	AMENITY$PLACE_OF_WORSHIP((byte) 17),
	AMENITY$POLICE((byte) 17),
	AMENITY$POST_BOX((byte) 17),
	AMENITY$POST_OFFICE((byte) 17),
	AMENITY$PUB((byte) 17),
	AMENITY$RECYCLING((byte) 17),
	AMENITY$RESTAURANT((byte) 17),
	AMENITY$SCHOOL((byte) 17),
	AMENITY$SHELTER((byte) 16),
	AMENITY$TELEPHONE((byte) 17),
	AMENITY$THEATRE((byte) 17),
	AMENITY$TOILETS((byte) 17),
	AMENITY$UNIVERSITY((byte) 17),
	BARRIER$BOLLARD((byte) 16),
	BARRIER$CYCLE_BARRIER((byte) 17),
	EMERGENCY$PHONE((byte) 17),
	HIGHWAY$BUS_STOP((byte) 16),
	HIGHWAY$MINI_ROUNDABOUT((byte) 17),
	HIGHWAY$TRAFFIC_SIGNALS((byte) 17),
	HIGHWAY$TURNING_CIRCLE((byte) 17),
	HISTORIC$MEMORIAL((byte) 17),
	HISTORIC$MONUMENT((byte) 17),
	LEISURE$PLAYGROUND((byte) 17),
	LEISURE$SLIPWAY((byte) 17),
	MAN_MADE$LIGHTHOUSE((byte) 17),
	MAN_MADE$SURVEILLANCE((byte) 17),
	MAN_MADE$TOWER((byte) 17),
	MAN_MADE$WINDMILL((byte) 17),
	NATURAL$PEAK((byte) 15),
	NATURAL$SPRING((byte) 17),
	NATURAL$TREE((byte) 17),
	PLACE$CITY((byte) 8),
	PLACE$ISLAND((byte) 12),
	PLACE$SUBURB((byte) 12),
	PLACE$TOWN((byte) 9),
	PLACE$VILLAGE((byte) 12),
	POWER$GENERATOR((byte) 17),
	POWER$TOWER((byte) 17),
	RAILWAY$HALT((byte) 17),
	RAILWAY$LEVEL_CROSSING((byte) 16),
	RAILWAY$STATION((byte) 15),
	RAILWAY$TRAM_STOP((byte) 17),
	SHOP$BAKERY((byte) 17),
	SHOP$HAIRDRESSER((byte) 17),
	SHOP$ORGANIC((byte) 17),
	SHOP$SUPERMARKET((byte) 17),
	STATION$LIGHT_RAIL((byte) 17),
	STATION$SUBWAY((byte) 17),
	TOURISM$ATTRACTION((byte) 17),
	TOURISM$HOSTEL((byte) 17),
	TOURISM$HOTEL((byte) 17),
	TOURISM$INFORMATION((byte) 17),
	TOURISM$MUSEUM((byte) 17),
	TOURISM$VIEWPOINT((byte) 15);

	private final byte zoomlevel;

	private PoiEnum(byte zoomlevel) {
		this.zoomlevel = zoomlevel;
	}

	public byte zoomlevel() {
		return zoomlevel;
	}

	@Override
	public String toString() {
		return name().replaceFirst("\\$", "=").toLowerCase(Locale.US);
	}

	private static final Map<String, PoiEnum> stringToEnum =
			new HashMap<String, PoiEnum>();

	static {
		for (PoiEnum poi : values()) {
			stringToEnum.put(poi.toString(), poi);
		}
	}

	public static PoiEnum fromString(String symbol) {
		return stringToEnum.get(symbol);
	}

}
