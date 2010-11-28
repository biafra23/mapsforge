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

import java.util.TreeMap;

class WhiteList {
	static TreeMap<String, Byte> getNodeTagWhitelist() {
		TreeMap<String, Byte> tagWhitelist = new TreeMap<String, Byte>();

		tagWhitelist.put("aeroway=helipad", Byte.valueOf((byte) 17));

		tagWhitelist.put("amenity=atm", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=bank", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=bicycle_rental", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=bus_station", Byte.valueOf((byte) 16));
		tagWhitelist.put("amenity=cafe", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=cinema", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=fast_food", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=fire_station", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=fountain", Byte.valueOf((byte) 16));
		tagWhitelist.put("amenity=fuel", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=hospital", Byte.valueOf((byte) 16));
		tagWhitelist.put("amenity=library", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=parking", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=pharmacy", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=place_of_worship", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=post_box", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=post_office", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=pub", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=recycling", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=restaurant", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=school", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=shelter", Byte.valueOf((byte) 16));
		tagWhitelist.put("amenity=telephone", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=theatre", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=toilets", Byte.valueOf((byte) 17));
		tagWhitelist.put("amenity=university", Byte.valueOf((byte) 17));

		tagWhitelist.put("barrier=bollard", Byte.valueOf((byte) 16));

		tagWhitelist.put("highway=bus_stop", Byte.valueOf((byte) 16));
		tagWhitelist.put("highway=traffic_signals", Byte.valueOf((byte) 17));

		tagWhitelist.put("historic=memorial", Byte.valueOf((byte) 17));
		tagWhitelist.put("historic=monument", Byte.valueOf((byte) 17));

		tagWhitelist.put("leisure=playground", Byte.valueOf((byte) 17));

		tagWhitelist.put("man_made=windmill", Byte.valueOf((byte) 17));

		tagWhitelist.put("natural=peak", Byte.valueOf((byte) 15));

		tagWhitelist.put("place=city", Byte.valueOf((byte) 8));
		tagWhitelist.put("place=island", Byte.valueOf((byte) 12));
		tagWhitelist.put("place=suburb", Byte.valueOf((byte) 14));
		tagWhitelist.put("place=town", Byte.valueOf((byte) 9));
		tagWhitelist.put("place=village", Byte.valueOf((byte) 14));

		tagWhitelist.put("railway=halt", Byte.valueOf((byte) 17));
		tagWhitelist.put("railway=level_crossing", Byte.valueOf((byte) 16));
		tagWhitelist.put("railway=station", Byte.valueOf((byte) 15));
		tagWhitelist.put("railway=tram_stop", Byte.valueOf((byte) 17));

		tagWhitelist.put("shop=bakery", Byte.valueOf((byte) 17));
		tagWhitelist.put("shop=organic", Byte.valueOf((byte) 17));
		tagWhitelist.put("shop=supermarket", Byte.valueOf((byte) 17));

		tagWhitelist.put("station=light_rail", Byte.valueOf(Byte.MAX_VALUE));
		tagWhitelist.put("station=subway", Byte.valueOf(Byte.MAX_VALUE));

		tagWhitelist.put("tourism=attraction", Byte.valueOf((byte) 17));
		tagWhitelist.put("tourism=hostel", Byte.valueOf((byte) 17));
		tagWhitelist.put("tourism=hotel", Byte.valueOf((byte) 17));
		tagWhitelist.put("tourism=information", Byte.valueOf((byte) 17));
		tagWhitelist.put("tourism=museum", Byte.valueOf((byte) 17));
		tagWhitelist.put("tourism=viewpoint", Byte.valueOf((byte) 15));

		return tagWhitelist;
	}

	static TreeMap<String, Byte> getWayTagWhitelist() {
		TreeMap<String, Byte> tagWhitelist = new TreeMap<String, Byte>();

		tagWhitelist.put("admin_level=2", Byte.valueOf((byte) 6));
		tagWhitelist.put("admin_level=4", Byte.valueOf((byte) 12)); // like Osmarender
		tagWhitelist.put("admin_level=6", Byte.valueOf((byte) 15));
		tagWhitelist.put("admin_level=8", Byte.valueOf((byte) 16));
		tagWhitelist.put("admin_level=9", Byte.valueOf((byte) 16));
		tagWhitelist.put("admin_level=10", Byte.valueOf((byte) 16));

		tagWhitelist.put("aeroway=aerodrome", Byte.valueOf((byte) 13));
		tagWhitelist.put("aeroway=apron", Byte.valueOf((byte) 13));
		tagWhitelist.put("aeroway=helipad", Byte.valueOf((byte) 17));
		tagWhitelist.put("aeroway=runway", Byte.valueOf((byte) 10));
		tagWhitelist.put("aeroway=taxiway", Byte.valueOf((byte) 10));
		tagWhitelist.put("aeroway=terminal", Byte.valueOf((byte) 16));

		tagWhitelist.put("amenity=college", Byte.valueOf((byte) 15));
		tagWhitelist.put("amenity=fountain", Byte.valueOf((byte) 15));
		tagWhitelist.put("amenity=grave_yard", Byte.valueOf((byte) 15));
		tagWhitelist.put("amenity=hospital", Byte.valueOf((byte) 15));
		tagWhitelist.put("amenity=parking", Byte.valueOf((byte) 15));
		tagWhitelist.put("amenity=school", Byte.valueOf((byte) 15));
		tagWhitelist.put("amenity=university", Byte.valueOf((byte) 15));

		tagWhitelist.put("area=yes", Byte.valueOf(Byte.MAX_VALUE));

		tagWhitelist.put("barrier=fence", Byte.valueOf((byte) 16));
		tagWhitelist.put("barrier=wall", Byte.valueOf((byte) 17));

		tagWhitelist.put("boundary=administrative", Byte.valueOf(Byte.MAX_VALUE));
		tagWhitelist.put("boundary=national_park", Byte.valueOf((byte) 12));

		tagWhitelist.put("bridge=yes", Byte.valueOf(Byte.MAX_VALUE));

		tagWhitelist.put("building=apartments", Byte.valueOf((byte) 16));
		tagWhitelist.put("building=embassy", Byte.valueOf((byte) 16));
		tagWhitelist.put("building=government", Byte.valueOf((byte) 16));
		tagWhitelist.put("building=gym", Byte.valueOf((byte) 16));
		tagWhitelist.put("building=roof", Byte.valueOf((byte) 16));
		tagWhitelist.put("building=sports", Byte.valueOf((byte) 16));
		tagWhitelist.put("building=train_station", Byte.valueOf((byte) 16));
		tagWhitelist.put("building=university", Byte.valueOf((byte) 16));
		tagWhitelist.put("building=yes", Byte.valueOf((byte) 16));

		tagWhitelist.put("highway=bridleway", Byte.valueOf((byte) 13));
		tagWhitelist.put("highway=construction", Byte.valueOf((byte) 15));
		tagWhitelist.put("highway=cycleway", Byte.valueOf((byte) 13));
		tagWhitelist.put("highway=footway", Byte.valueOf((byte) 15));
		tagWhitelist.put("highway=living_street", Byte.valueOf((byte) 14));
		tagWhitelist.put("highway=motorway", Byte.valueOf((byte) 8));
		tagWhitelist.put("highway=motorway_link", Byte.valueOf((byte) 8));
		tagWhitelist.put("highway=path", Byte.valueOf((byte) 14));
		tagWhitelist.put("highway=pedestrian", Byte.valueOf((byte) 14));
		tagWhitelist.put("highway=primary", Byte.valueOf((byte) 8));
		tagWhitelist.put("highway=primary_link", Byte.valueOf((byte) 8));
		tagWhitelist.put("highway=residential", Byte.valueOf((byte) 14));
		tagWhitelist.put("highway=road", Byte.valueOf((byte) 12));
		tagWhitelist.put("highway=secondary", Byte.valueOf((byte) 9));
		tagWhitelist.put("highway=service", Byte.valueOf((byte) 14));
		tagWhitelist.put("highway=steps", Byte.valueOf((byte) 16));
		tagWhitelist.put("highway=tertiary", Byte.valueOf((byte) 10));
		tagWhitelist.put("highway=track", Byte.valueOf((byte) 12));
		tagWhitelist.put("highway=trunk", Byte.valueOf((byte) 8));
		tagWhitelist.put("highway=trunk_link", Byte.valueOf((byte) 8));
		tagWhitelist.put("highway=unclassified", Byte.valueOf((byte) 13));

		tagWhitelist.put("historic=ruins", Byte.valueOf((byte) 17));

		tagWhitelist.put("landuse=allotments", Byte.valueOf((byte) 12));
		tagWhitelist.put("landuse=basin", Byte.valueOf((byte) 14));
		tagWhitelist.put("landuse=brownfield", Byte.valueOf((byte) 12));
		tagWhitelist.put("landuse=cemetery", Byte.valueOf((byte) 12));
		tagWhitelist.put("landuse=commercial", Byte.valueOf((byte) 12));
		tagWhitelist.put("landuse=construction", Byte.valueOf((byte) 14));
		tagWhitelist.put("landuse=farm", Byte.valueOf((byte) 12));
		tagWhitelist.put("landuse=farmland", Byte.valueOf((byte) 12));
		tagWhitelist.put("landuse=forest", Byte.valueOf((byte) 12));
		tagWhitelist.put("landuse=grass", Byte.valueOf((byte) 12));
		tagWhitelist.put("landuse=greenfield", Byte.valueOf((byte) 12));
		tagWhitelist.put("landuse=industrial", Byte.valueOf((byte) 12));
		tagWhitelist.put("landuse=military", Byte.valueOf((byte) 12));
		tagWhitelist.put("landuse=recreation_ground", Byte.valueOf((byte) 12));
		tagWhitelist.put("landuse=reservoir", Byte.valueOf((byte) 12));
		tagWhitelist.put("landuse=residential", Byte.valueOf((byte) 12));
		tagWhitelist.put("landuse=retail", Byte.valueOf((byte) 12));
		tagWhitelist.put("landuse=village_green", Byte.valueOf((byte) 12));
		tagWhitelist.put("landuse=wood", Byte.valueOf((byte) 12));

		tagWhitelist.put("leisure=common", Byte.valueOf((byte) 12));
		tagWhitelist.put("leisure=garden", Byte.valueOf((byte) 12));
		tagWhitelist.put("leisure=golf_course", Byte.valueOf((byte) 12));
		tagWhitelist.put("leisure=park", Byte.valueOf((byte) 12));
		tagWhitelist.put("leisure=pitch", Byte.valueOf((byte) 15));
		tagWhitelist.put("leisure=playground", Byte.valueOf((byte) 16));
		tagWhitelist.put("leisure=sports_centre", Byte.valueOf((byte) 12));
		tagWhitelist.put("leisure=stadium", Byte.valueOf((byte) 12));
		tagWhitelist.put("leisure=track", Byte.valueOf((byte) 15));
		tagWhitelist.put("leisure=water_park", Byte.valueOf((byte) 15));

		tagWhitelist.put("man_made=pier", Byte.valueOf((byte) 15));

		tagWhitelist.put("military=airfield", Byte.valueOf((byte) 12));
		tagWhitelist.put("military=barracks", Byte.valueOf((byte) 12));
		tagWhitelist.put("military=naval_base", Byte.valueOf((byte) 12));

		tagWhitelist.put("natural=beach", Byte.valueOf((byte) 14));
		tagWhitelist.put("natural=coastline", Byte.valueOf((byte) 0));
		tagWhitelist.put("natural=heath", Byte.valueOf((byte) 12));
		tagWhitelist.put("natural=land", Byte.valueOf((byte) 12));
		tagWhitelist.put("natural=scrub", Byte.valueOf((byte) 12));
		tagWhitelist.put("natural=water", Byte.valueOf((byte) 12));
		tagWhitelist.put("natural=wood", Byte.valueOf((byte) 12));

		tagWhitelist.put("place=locality", Byte.valueOf((byte) 17));

		tagWhitelist.put("railway=light_rail", Byte.valueOf((byte) 12));
		tagWhitelist.put("railway=rail", Byte.valueOf((byte) 10));
		tagWhitelist.put("railway=station", Byte.valueOf((byte) 13));
		tagWhitelist.put("railway=subway", Byte.valueOf((byte) 13));
		tagWhitelist.put("railway=tram", Byte.valueOf((byte) 13));

		tagWhitelist.put("route=ferry", Byte.valueOf((byte) 12));

		tagWhitelist.put("sport=shooting", Byte.valueOf((byte) 15));
		tagWhitelist.put("sport=tennis", Byte.valueOf((byte) 15));

		tagWhitelist.put("tourism=attraction", Byte.valueOf((byte) 15));
		tagWhitelist.put("tourism=zoo", Byte.valueOf((byte) 12));

		tagWhitelist.put("tunnel=no", Byte.valueOf(Byte.MAX_VALUE));
		tagWhitelist.put("tunnel=yes", Byte.valueOf(Byte.MAX_VALUE));

		tagWhitelist.put("waterway=canal", Byte.valueOf((byte) 12));
		tagWhitelist.put("waterway=drain", Byte.valueOf((byte) 12));
		tagWhitelist.put("waterway=river", Byte.valueOf((byte) 12));
		tagWhitelist.put("waterway=riverbank", Byte.valueOf((byte) 12));
		tagWhitelist.put("waterway=stream", Byte.valueOf((byte) 12));

		return tagWhitelist;
	}
}