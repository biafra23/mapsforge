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

class TagIDsNodes {
	Short aeroway$helipad;
	Short amenity$atm;
	Short amenity$bank;
	Short amenity$bicycle_rental;
	Short amenity$bus_station;
	Short amenity$cafe;
	Short amenity$cinema;
	Short amenity$fast_food;
	Short amenity$fire_station;
	Short amenity$fountain;
	Short amenity$fuel;
	Short amenity$hospital;
	Short amenity$library;
	Short amenity$parking;
	Short amenity$pharmacy;
	Short amenity$place_of_worship;
	Short amenity$post_box;
	Short amenity$post_office;
	Short amenity$pub;
	Short amenity$recycling;
	Short amenity$restaurant;
	Short amenity$school;
	Short amenity$shelter;
	Short amenity$telephone;
	Short amenity$theatre;
	Short amenity$toilets;
	Short amenity$university;
	Short barrier$bollard;
	Short highway$bus_stop;
	Short highway$traffic_signals;
	Short historic$memorial;
	Short historic$monument;
	Short leisure$playground;
	Short man_made$windmill;
	Short natural$peak;
	Short place$city;
	Short place$island;
	Short place$suburb;
	Short place$town;
	Short place$village;
	Short railway$halt;
	Short railway$level_crossing;
	Short railway$station;
	Short railway$tram_stop;
	Short shop$bakery;
	Short shop$organic;
	Short shop$supermarket;
	Short station$light_rail;
	Short station$subway;
	Short tourism$attraction;
	Short tourism$hostel;
	Short tourism$hotel;
	Short tourism$information;
	Short tourism$museum;
	Short tourism$viewpoint;

	void update(HashMap<String, Short> nodeTags) {
		this.aeroway$helipad = nodeTags.get("aeroway=helipad");

		this.amenity$atm = nodeTags.get("amenity=atm");
		this.amenity$bank = nodeTags.get("amenity=bank");
		this.amenity$bicycle_rental = nodeTags.get("amenity=bicycle_rental");
		this.amenity$bus_station = nodeTags.get("amenity=bus_station");
		this.amenity$cafe = nodeTags.get("amenity=cafe");
		this.amenity$cinema = nodeTags.get("amenity=cinema");
		this.amenity$fast_food = nodeTags.get("amenity=fast_food");
		this.amenity$fire_station = nodeTags.get("amenity=fire_station");
		this.amenity$fountain = nodeTags.get("amenity=fountain");
		this.amenity$fuel = nodeTags.get("amenity=fuel");
		this.amenity$hospital = nodeTags.get("amenity=hospital");
		this.amenity$library = nodeTags.get("amenity=library");
		this.amenity$parking = nodeTags.get("amenity=parking");
		this.amenity$pharmacy = nodeTags.get("amenity=pharmacy");
		this.amenity$place_of_worship = nodeTags.get("amenity=place_of_worship");
		this.amenity$post_box = nodeTags.get("amenity=post_box");
		this.amenity$post_office = nodeTags.get("amenity=post_office");
		this.amenity$pub = nodeTags.get("amenity=pub");
		this.amenity$recycling = nodeTags.get("amenity=recycling");
		this.amenity$restaurant = nodeTags.get("amenity=restaurant");
		this.amenity$school = nodeTags.get("amenity=school");
		this.amenity$shelter = nodeTags.get("amenity=shelter");
		this.amenity$telephone = nodeTags.get("amenity=telephone");
		this.amenity$theatre = nodeTags.get("amenity=theatre");
		this.amenity$toilets = nodeTags.get("amenity=toilets");
		this.amenity$university = nodeTags.get("amenity=university");

		this.barrier$bollard = nodeTags.get("barrier=bollard");

		this.highway$bus_stop = nodeTags.get("highway=bus_stop");
		this.highway$traffic_signals = nodeTags.get("highway=traffic_signals");

		this.historic$memorial = nodeTags.get("historic=memorial");
		this.historic$monument = nodeTags.get("historic=monument");

		this.leisure$playground = nodeTags.get("leisure=playground");

		this.man_made$windmill = nodeTags.get("man_made=windmill");

		this.natural$peak = nodeTags.get("natural=peak");

		this.place$city = nodeTags.get("place=city");
		this.place$island = nodeTags.get("place=island");
		this.place$suburb = nodeTags.get("place=suburb");
		this.place$town = nodeTags.get("place=town");
		this.place$village = nodeTags.get("place=village");

		this.railway$halt = nodeTags.get("railway=halt");
		this.railway$level_crossing = nodeTags.get("railway=level_crossing");
		this.railway$station = nodeTags.get("railway=station");
		this.railway$tram_stop = nodeTags.get("railway=tram_stop");

		this.shop$bakery = nodeTags.get("shop=bakery");
		this.shop$organic = nodeTags.get("shop=organic");
		this.shop$supermarket = nodeTags.get("shop=supermarket");

		this.station$light_rail = nodeTags.get("station=light_rail");
		this.station$subway = nodeTags.get("station=subway");

		this.tourism$attraction = nodeTags.get("tourism=attraction");
		this.tourism$hostel = nodeTags.get("tourism=hostel");
		this.tourism$hotel = nodeTags.get("tourism=hotel");
		this.tourism$information = nodeTags.get("tourism=information");
		this.tourism$museum = nodeTags.get("tourism=museum");
		this.tourism$viewpoint = nodeTags.get("tourism=viewpoint");
	}
}