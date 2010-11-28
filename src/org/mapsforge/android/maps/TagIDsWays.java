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

import java.util.HashMap;

class TagIDsWays {
	Short admin_level$10;
	Short admin_level$2;
	Short admin_level$4;
	Short admin_level$6;
	Short admin_level$8;
	Short admin_level$9;
	Short aeroway$aerodrome;
	Short aeroway$apron;
	Short aeroway$runway;
	Short aeroway$taxiway;
	Short aeroway$terminal;
	Short amenity$college;
	Short amenity$fountain;
	Short amenity$grave_yard;
	Short amenity$hospital;
	Short amenity$parking;
	Short amenity$school;
	Short amenity$university;
	Short area$yes;
	Short barrier$fence;
	Short barrier$wall;
	Short boundary$administrative;
	Short boundary$national_park;
	Short bridge$yes;
	Short building$apartments;
	Short building$embassy;
	Short building$government;
	Short building$gym;
	Short building$roof;
	Short building$sports;
	Short building$train_station;
	Short building$university;
	Short building$yes;
	Short highway$bridleway;
	Short highway$construction;
	Short highway$cycleway;
	Short highway$footway;
	Short highway$living_street;
	Short highway$motorway;
	Short highway$motorway_link;
	Short highway$path;
	Short highway$pedestrian;
	Short highway$primary;
	Short highway$primary_link;
	Short highway$residential;
	Short highway$road;
	Short highway$secondary;
	Short highway$service;
	Short highway$steps;
	Short highway$tertiary;
	Short highway$track;
	Short highway$trunk;
	Short highway$trunk_link;
	Short highway$unclassified;
	Short historic$ruins;
	Short landuse$allotments;
	Short landuse$basin;
	Short landuse$brownfield;
	Short landuse$cemetery;
	Short landuse$commercial;
	Short landuse$construction;
	Short landuse$farm;
	Short landuse$farmland;
	Short landuse$forest;
	Short landuse$grass;
	Short landuse$greenfield;
	Short landuse$industrial;
	Short landuse$military;
	Short landuse$recreation_ground;
	Short landuse$reservoir;
	Short landuse$residential;
	Short landuse$retail;
	Short landuse$village_green;
	Short landuse$wood;
	Short leisure$common;
	Short leisure$garden;
	Short leisure$golf_course;
	Short leisure$park;
	Short leisure$pitch;
	Short leisure$playground;
	Short leisure$sports_centre;
	Short leisure$stadium;
	Short leisure$track;
	Short leisure$water_park;
	Short man_made$pier;
	Short military$airfield;
	Short military$barracks;
	Short military$naval_base;
	Short natural$beach;
	Short natural$coastline;
	Short natural$heath;
	Short natural$land;
	Short natural$scrub;
	Short natural$water;
	Short natural$wood;
	Short place$locality;
	Short railway$light_rail;
	Short railway$rail;
	Short railway$station;
	Short railway$subway;
	Short railway$tram;
	Short route$ferry;
	Short sport$shooting;
	Short sport$tennis;
	Short tourism$attraction;
	Short tourism$zoo;
	Short tunnel$no;
	Short tunnel$yes;
	Short waterway$canal;
	Short waterway$drain;
	Short waterway$river;
	Short waterway$riverbank;
	Short waterway$stream;

	void update(HashMap<String, Short> nodeTags) {
		this.admin_level$10 = nodeTags.get("admin_level=10");
		this.admin_level$2 = nodeTags.get("admin_level=2");
		this.admin_level$4 = nodeTags.get("admin_level=4");
		this.admin_level$6 = nodeTags.get("admin_level=6");
		this.admin_level$8 = nodeTags.get("admin_level=8");
		this.admin_level$9 = nodeTags.get("admin_level=9");

		this.aeroway$aerodrome = nodeTags.get("aeroway=aerodrome");
		this.aeroway$apron = nodeTags.get("aeroway=apron");
		this.aeroway$runway = nodeTags.get("aeroway=runway");
		this.aeroway$taxiway = nodeTags.get("aeroway=taxiway");
		this.aeroway$terminal = nodeTags.get("aeroway=terminal");

		this.amenity$college = nodeTags.get("amenity=college");
		this.amenity$fountain = nodeTags.get("amenity=fountain");
		this.amenity$grave_yard = nodeTags.get("amenity=grave_yard");
		this.amenity$hospital = nodeTags.get("amenity=hospital");
		this.amenity$parking = nodeTags.get("amenity=parking");
		this.amenity$school = nodeTags.get("amenity=school");
		this.amenity$university = nodeTags.get("amenity=university");

		this.area$yes = nodeTags.get("area=yes");

		this.barrier$fence = nodeTags.get("barrier=fence");
		this.barrier$wall = nodeTags.get("barrier=wall");

		this.boundary$administrative = nodeTags.get("boundary=administrative");
		this.boundary$national_park = nodeTags.get("boundary=national_park");

		this.bridge$yes = nodeTags.get("bridge=yes");

		this.building$apartments = nodeTags.get("building=apartments");
		this.building$embassy = nodeTags.get("building=embassy");
		this.building$government = nodeTags.get("building=government");
		this.building$gym = nodeTags.get("building=gym");
		this.building$roof = nodeTags.get("building=roof");
		this.building$sports = nodeTags.get("building=sports");
		this.building$train_station = nodeTags.get("building=train_station");
		this.building$university = nodeTags.get("building=university");
		this.building$yes = nodeTags.get("building=yes");

		this.highway$bridleway = nodeTags.get("highway=bridleway");
		this.highway$construction = nodeTags.get("highway=construction");
		this.highway$cycleway = nodeTags.get("highway=cycleway");
		this.highway$footway = nodeTags.get("highway=footway");
		this.highway$living_street = nodeTags.get("highway=living_street");
		this.highway$motorway = nodeTags.get("highway=motorway");
		this.highway$motorway_link = nodeTags.get("highway=motorway_link");
		this.highway$path = nodeTags.get("highway=path");
		this.highway$pedestrian = nodeTags.get("highway=pedestrian");
		this.highway$primary = nodeTags.get("highway=primary");
		this.highway$primary_link = nodeTags.get("highway=primary_link");
		this.highway$residential = nodeTags.get("highway=residential");
		this.highway$road = nodeTags.get("highway=road");
		this.highway$secondary = nodeTags.get("highway=secondary");
		this.highway$service = nodeTags.get("highway=service");
		this.highway$steps = nodeTags.get("highway=steps");
		this.highway$tertiary = nodeTags.get("highway=tertiary");
		this.highway$track = nodeTags.get("highway=track");
		this.highway$trunk = nodeTags.get("highway=trunk");
		this.highway$trunk_link = nodeTags.get("highway=trunk_link");
		this.highway$unclassified = nodeTags.get("highway=unclassified");

		this.historic$ruins = nodeTags.get("historic=ruins");

		this.landuse$allotments = nodeTags.get("landuse=allotments");
		this.landuse$basin = nodeTags.get("landuse=basin");
		this.landuse$brownfield = nodeTags.get("landuse=brownfield");
		this.landuse$cemetery = nodeTags.get("landuse=cemetery");
		this.landuse$commercial = nodeTags.get("landuse=commercial");
		this.landuse$construction = nodeTags.get("landuse=construction");
		this.landuse$farm = nodeTags.get("landuse=farm");
		this.landuse$farmland = nodeTags.get("landuse=farmland");
		this.landuse$forest = nodeTags.get("landuse=forest");
		this.landuse$grass = nodeTags.get("landuse=grass");
		this.landuse$greenfield = nodeTags.get("landuse=greenfield");
		this.landuse$industrial = nodeTags.get("landuse=industrial");
		this.landuse$military = nodeTags.get("landuse=military");
		this.landuse$recreation_ground = nodeTags.get("landuse=recreation_ground");
		this.landuse$reservoir = nodeTags.get("landuse=reservoir");
		this.landuse$residential = nodeTags.get("landuse=residential");
		this.landuse$retail = nodeTags.get("landuse=retail");
		this.landuse$village_green = nodeTags.get("landuse=village_green");
		this.landuse$wood = nodeTags.get("landuse=wood");

		this.leisure$common = nodeTags.get("leisure=common");
		this.leisure$garden = nodeTags.get("leisure=garden");
		this.leisure$golf_course = nodeTags.get("leisure=golf_course");
		this.leisure$park = nodeTags.get("leisure=park");
		this.leisure$pitch = nodeTags.get("leisure=pitch");
		this.leisure$playground = nodeTags.get("leisure=playground");
		this.leisure$sports_centre = nodeTags.get("leisure=sports_centre");
		this.leisure$stadium = nodeTags.get("leisure=stadium");
		this.leisure$track = nodeTags.get("leisure=track");
		this.leisure$water_park = nodeTags.get("leisure=water_park");

		this.man_made$pier = nodeTags.get("man_made=pier");

		this.military$airfield = nodeTags.get("military=airfield");
		this.military$barracks = nodeTags.get("military=barracks");
		this.military$naval_base = nodeTags.get("military=naval_base");

		this.natural$beach = nodeTags.get("natural=beach");
		this.natural$coastline = nodeTags.get("natural=coastline");
		this.natural$heath = nodeTags.get("natural=heath");
		this.natural$land = nodeTags.get("natural=land");
		this.natural$scrub = nodeTags.get("natural=scrub");
		this.natural$water = nodeTags.get("natural=water");
		this.natural$wood = nodeTags.get("natural=wood");

		this.place$locality = nodeTags.get("place=locality");

		this.railway$light_rail = nodeTags.get("railway=light_rail");
		this.railway$rail = nodeTags.get("railway=rail");
		this.railway$station = nodeTags.get("railway=station");
		this.railway$subway = nodeTags.get("railway=subway");
		this.railway$tram = nodeTags.get("railway=tram");

		this.route$ferry = nodeTags.get("route=ferry");

		this.sport$shooting = nodeTags.get("sport=shooting");
		this.sport$tennis = nodeTags.get("sport=tennis");

		this.tourism$attraction = nodeTags.get("tourism=attraction");
		this.tourism$zoo = nodeTags.get("tourism=zoo");

		this.tunnel$no = nodeTags.get("tunnel=no");
		this.tunnel$yes = nodeTags.get("tunnel=yes");

		this.waterway$canal = nodeTags.get("waterway=canal");
		this.waterway$drain = nodeTags.get("waterway=drain");
		this.waterway$river = nodeTags.get("waterway=river");
		this.waterway$riverbank = nodeTags.get("waterway=riverbank");
		this.waterway$stream = nodeTags.get("waterway=stream");
	}
}