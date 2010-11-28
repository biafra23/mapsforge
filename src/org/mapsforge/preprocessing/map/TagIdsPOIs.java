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

import java.util.HashMap;
import java.util.Map;

/**
 * List of all supported OSM tags for POIs. Each tag has a unique ID and is declared as static
 * final byte to speed up the rendering process.
 */
final class TagIdsPOIs {
	static final short AEROWAY$GATE = 0;
	static final short AEROWAY$HELIPAD = 1;
	static final short AMENITY$ATM = 2;
	static final short AMENITY$BANK = 3;
	static final short AMENITY$BICYCLE_RENTAL = 4;
	static final short AMENITY$BUS_STATION = 5;
	static final short AMENITY$CAFE = 6;
	static final short AMENITY$CINEMA = 7;
	static final short AMENITY$FAST_FOOD = 8;
	static final short AMENITY$FOUNTAIN = 9;
	static final short AMENITY$FIRE_STATION = 10;
	static final short AMENITY$FUEL = 11;
	static final short AMENITY$HOSPITAL = 12;
	static final short AMENITY$LIBRARY = 13;
	static final short AMENITY$PARKING = 14;
	static final short AMENITY$PHARMACY = 15;
	static final short AMENITY$PLACE_OF_WORSHIP = 16;
	static final short AMENITY$POLICE = 17;
	static final short AMENITY$POST_BOX = 18;
	static final short AMENITY$POST_OFFICE = 19;
	static final short AMENITY$PUB = 20;
	static final short AMENITY$RECYCLING = 21;
	static final short AMENITY$RESTAURANT = 22;
	static final short AMENITY$SCHOOL = 23;
	static final short AMENITY$SHELTER = 24;
	static final short AMENITY$TELEPHONE = 25;
	static final short AMENITY$THEATRE = 26;
	static final short AMENITY$TOILETS = 27;
	static final short AMENITY$UNIVERSITY = 28;
	static final short BARRIER$BOLLARD = 29;
	static final short BARRIER$CYCLE_BARRIER = 30;
	static final short EMERGENCY$PHONE = 31;
	static final short HIGHWAY$BUS_STOP = 32;
	static final short HIGHWAY$MINI_ROUNDABOUT = 33;
	static final short HIGHWAY$TRAFFIC_SIGNALS = 34;
	static final short HIGHWAY$TURNING_CIRCLE = 35;
	static final short HISTORIC$MEMORIAL = 36;
	static final short HISTORIC$MONUMENT = 37;
	static final short LEISURE$PLAYGROUND = 38;
	static final short LEISURE$SLIPWAY = 39;
	static final short MAN_MADE$LIGHTHOUSE = 40;
	static final short MAN_MADE$SURVEILLANCE = 41;
	static final short MAN_MADE$TOWER = 42;
	static final short MAN_MADE$WINDMILL = 43;
	static final short NATURAL$PEAK = 44;
	static final short NATURAL$SPRING = 45;
	static final short NATURAL$TREE = 46;
	static final short PLACE$CITY = 47;
	static final short PLACE$ISLAND = 48;
	static final short PLACE$SUBURB = 49;
	static final short PLACE$TOWN = 50;
	static final short PLACE$VILLAGE = 51;
	static final short POWER$GENERATOR = 52;
	static final short POWER$TOWER = 53;
	static final short RAILWAY$HALT = 54;
	static final short RAILWAY$LEVEL_CROSSING = 55;
	static final short RAILWAY$STATION = 56;
	static final short RAILWAY$TRAM_STOP = 57;
	static final short SHOP$BAKERY = 58;
	static final short SHOP$HAIRDRESSER = 59;
	static final short SHOP$ORGANIC = 60;
	static final short SHOP$SUPERMARKET = 61;
	static final short STATION$LIGHT_RAIL = 62;
	static final short STATION$SUBWAY = 63;
	static final short TOURISM$ATTRACTION = 64;
	static final short TOURISM$HOSTEL = 65;
	static final short TOURISM$HOTEL = 66;
	static final short TOURISM$INFORMATION = 67;
	static final short TOURISM$MUSEUM = 68;
	static final short TOURISM$VIEWPOINT = 69;

	static final Map<String, Short> getMap() {
		Map<String, Short> map = new HashMap<String, Short>();
		map.put("aeroway=gate", Short.valueOf((short) 0));
		map.put("aeroway=helipad", Short.valueOf((short) 1));
		map.put("amenity=atm", Short.valueOf((short) 2));
		map.put("amenity=bank", Short.valueOf((short) 3));
		map.put("amenity=bicycle_rental", Short.valueOf((short) 4));
		map.put("amenity=bus_station", Short.valueOf((short) 5));
		map.put("amenity=cafe", Short.valueOf((short) 6));
		map.put("amenity=cinema", Short.valueOf((short) 7));
		map.put("amenity=fast_food", Short.valueOf((short) 8));
		map.put("amenity=fire_station", Short.valueOf((short) 10));
		map.put("amenity=fountain", Short.valueOf((short) 9));
		map.put("amenity=fuel", Short.valueOf((short) 11));
		map.put("amenity=hospital", Short.valueOf((short) 12));
		map.put("amenity=library", Short.valueOf((short) 13));
		map.put("amenity=parking", Short.valueOf((short) 14));
		map.put("amenity=pharmacy", Short.valueOf((short) 15));
		map.put("amenity=place_of_worship", Short.valueOf((short) 16));
		map.put("amenity=police", Short.valueOf((short) 17));
		map.put("amenity=post_box", Short.valueOf((short) 18));
		map.put("amenity=post_office", Short.valueOf((short) 19));
		map.put("amenity=pub", Short.valueOf((short) 20));
		map.put("amenity=recycling", Short.valueOf((short) 21));
		map.put("amenity=restaurant", Short.valueOf((short) 22));
		map.put("amenity=school", Short.valueOf((short) 23));
		map.put("amenity=shelter", Short.valueOf((short) 24));
		map.put("amenity=telephone", Short.valueOf((short) 25));
		map.put("amenity=theatre", Short.valueOf((short) 26));
		map.put("amenity=toilets", Short.valueOf((short) 27));
		map.put("amenity=university", Short.valueOf((short) 28));
		map.put("barrier=bollard", Short.valueOf((short) 29));
		map.put("barrier=cycle_barrier", Short.valueOf((short) 30));
		map.put("emergency=phone", Short.valueOf((short) 31));
		map.put("highway=bus_stop", Short.valueOf((short) 32));
		map.put("highway=mini_roundabout", Short.valueOf((short) 33));
		map.put("highway=traffic_signals", Short.valueOf((short) 34));
		map.put("highway=turning_circle", Short.valueOf((short) 35));
		map.put("historic=memorial", Short.valueOf((short) 36));
		map.put("historic=monument", Short.valueOf((short) 37));
		map.put("leisure=playground", Short.valueOf((short) 38));
		map.put("leisure=slipway", Short.valueOf((short) 39));
		map.put("man_made=lighthouse", Short.valueOf((short) 40));
		map.put("man_made=surveillance", Short.valueOf((short) 41));
		map.put("man_made=tower", Short.valueOf((short) 42));
		map.put("man_made=windmill", Short.valueOf((short) 43));
		map.put("natural=peak", Short.valueOf((short) 44));
		map.put("natural=spring", Short.valueOf((short) 45));
		map.put("natural=tree", Short.valueOf((short) 46));
		map.put("place=city", Short.valueOf((short) 47));
		map.put("place=island", Short.valueOf((short) 48));
		map.put("place=suburb", Short.valueOf((short) 49));
		map.put("place=town", Short.valueOf((short) 50));
		map.put("place=village", Short.valueOf((short) 51));
		map.put("power=generator", Short.valueOf((short) 52));
		map.put("power=tower", Short.valueOf((short) 53));
		map.put("railway=halt", Short.valueOf((short) 54));
		map.put("railway=level_crossing", Short.valueOf((short) 55));
		map.put("railway=station", Short.valueOf((short) 56));
		map.put("railway=tram_stop", Short.valueOf((short) 57));
		map.put("shop=bakery", Short.valueOf((short) 58));
		map.put("shop=hairdresser", Short.valueOf((short) 59));
		map.put("shop=organic", Short.valueOf((short) 60));
		map.put("shop=supermarket", Short.valueOf((short) 61));
		map.put("station=light_rail", Short.valueOf((short) 62));
		map.put("station=subway", Short.valueOf((short) 63));
		map.put("tourism=attraction", Short.valueOf((short) 64));
		map.put("tourism=hostel", Short.valueOf((short) 65));
		map.put("tourism=hotel", Short.valueOf((short) 66));
		map.put("tourism=information", Short.valueOf((short) 67));
		map.put("tourism=museum", Short.valueOf((short) 68));
		map.put("tourism=viewpoint", Short.valueOf((short) 69));
		return map;
	}
}