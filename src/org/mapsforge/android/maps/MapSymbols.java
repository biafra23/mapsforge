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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * This class holds all symbols that can be rendered on the map. All bitmaps are created when
 * the MapSymbols constructor is called and are recycled when the recycle() method is called.
 */
class MapSymbols {
	final Bitmap atm;
	final Bitmap bakery;
	final Bitmap bank;
	final Bitmap bicycle_rental;
	final Bitmap bus;
	final Bitmap bus_sta;
	final Bitmap cafe;
	final Bitmap church;
	final Bitmap cinema;
	final Bitmap fastfood;
	final Bitmap firebrigade;
	final Bitmap fountain;
	final Bitmap helipad;
	final Bitmap hospital;
	final Bitmap hostel;
	final Bitmap hotel;
	final Bitmap information;
	final Bitmap library;
	final Bitmap parking;
	final Bitmap peak;
	final Bitmap petrolStation;
	final Bitmap pharmacy;
	final Bitmap playground;
	final Bitmap postbox;
	final Bitmap postoffice;
	final Bitmap pub;
	final Bitmap railway_crossing;
	final Bitmap recycling;
	final Bitmap restaurant;
	final Bitmap school;
	final Bitmap shelter;
	final Bitmap supermarket;
	final Bitmap telephone;
	final Bitmap theatre;
	final Bitmap toilets;
	final Bitmap traffic_signal;
	final Bitmap university;
	final Bitmap viewpoint;
	final Bitmap windmill;

	MapSymbols() {
		this.atm = BitmapFactory
				.decodeStream(getClass().getResourceAsStream("symbols/atm.png"));
		this.bakery = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/bakery.png"));
		this.bank = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/bank.png"));
		this.bicycle_rental = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/bicycle_rental.png"));
		this.bus = BitmapFactory
				.decodeStream(getClass().getResourceAsStream("symbols/bus.png"));
		this.bus_sta = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/bus_sta.png"));
		this.cafe = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/cafe.png"));
		this.church = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/church.png"));
		this.cinema = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/cinema.png"));
		this.fastfood = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/fastfood.png"));
		this.firebrigade = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/firebrigade.png"));
		this.fountain = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/fountain.png"));
		this.helipad = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/helipad.png"));
		this.hospital = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/hospital.png"));
		this.hostel = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/hostel.png"));
		this.hotel = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/hotel.png"));
		this.information = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/information.png"));
		this.library = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/library.png"));
		this.parking = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/parking.png"));
		this.peak = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/peak.png"));
		this.petrolStation = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/petrolStation.png"));
		this.pharmacy = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/pharmacy.png"));
		this.playground = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/playground.png"));
		this.postbox = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/postbox.png"));
		this.postoffice = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/postoffice.png"));
		this.pub = BitmapFactory
				.decodeStream(getClass().getResourceAsStream("symbols/pub.png"));
		this.railway_crossing = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/railway-crossing.png"));
		this.recycling = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/recycling.png"));
		this.restaurant = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/restaurant.png"));
		this.school = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/school.png"));
		this.shelter = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/shelter.png"));
		this.supermarket = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/supermarket.png"));
		this.telephone = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/telephone.png"));
		this.theatre = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/theatre.png"));
		this.toilets = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/toilets.png"));
		this.traffic_signal = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/traffic_signal.png"));
		this.university = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/university.png"));
		this.viewpoint = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/viewpoint.png"));
		this.windmill = BitmapFactory.decodeStream(getClass().getResourceAsStream(
				"symbols/windmill.png"));
	}

	void recycle() {
		this.atm.recycle();
		this.bakery.recycle();
		this.bank.recycle();
		this.bicycle_rental.recycle();
		this.bus.recycle();
		this.bus_sta.recycle();
		this.cafe.recycle();
		this.church.recycle();
		this.cinema.recycle();
		this.fastfood.recycle();
		this.firebrigade.recycle();
		this.fountain.recycle();
		this.helipad.recycle();
		this.hospital.recycle();
		this.hostel.recycle();
		this.hotel.recycle();
		this.information.recycle();
		this.library.recycle();
		this.parking.recycle();
		this.peak.recycle();
		this.petrolStation.recycle();
		this.pharmacy.recycle();
		this.playground.recycle();
		this.postbox.recycle();
		this.postoffice.recycle();
		this.pub.recycle();
		this.railway_crossing.recycle();
		this.recycling.recycle();
		this.restaurant.recycle();
		this.school.recycle();
		this.shelter.recycle();
		this.supermarket.recycle();
		this.telephone.recycle();
		this.theatre.recycle();
		this.toilets.recycle();
		this.traffic_signal.recycle();
		this.university.recycle();
		this.viewpoint.recycle();
		this.windmill.recycle();
	}
}