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
package org.mapsforge.directions;

import org.mapsforge.core.GeoCoordinate;

/**
 * A lot like a landmark / poi but it has radius as it's a large area
 * 
 * @author Eike
 */
public class TurnByTurnCity {
	GeoCoordinate location;
	String name;
	int radius;

	/**
	 * @param location
	 *            where it's at
	 * @param name
	 *            what it's called
	 * @param radius
	 *            how far it extends from the center
	 */
	public TurnByTurnCity(GeoCoordinate location, String name, int radius) {
		super();
		this.location = location;
		this.name = name;
		this.radius = radius;
	}

	/**
	 * @param decisionPointCoord
	 *            is the coordinate to be looked at
	 * @return true if the given coordinate is within the radius of the city
	 */
	public boolean contains(GeoCoordinate decisionPointCoord) {
		// TODO Auto-generated method stub
		return false;
	}
}
