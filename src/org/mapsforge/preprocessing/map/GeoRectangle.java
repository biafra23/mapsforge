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

class GeoRectangle {
	int latitude1;
	int latitude2;
	int longitude1;
	int longitude2;

	GeoRectangle(int latitude1, int longitude1, int latitude2, int longitude2) {
		this.latitude1 = latitude1;
		this.longitude1 = longitude1;
		this.latitude2 = latitude2;
		this.longitude2 = longitude2;
	}

	@Override
	public String toString() {
		return this.latitude1 + ", " + this.longitude1 + " / " + this.latitude2 + ", "
				+ this.longitude2;
	}
}