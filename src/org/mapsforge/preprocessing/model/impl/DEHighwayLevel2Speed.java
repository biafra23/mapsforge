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
package org.mapsforge.preprocessing.model.impl;

import org.mapsforge.preprocessing.model.EHighwayLevel;
import org.mapsforge.preprocessing.model.IHighwayLevel2Speed;

public class DEHighwayLevel2Speed implements IHighwayLevel2Speed {

	@Override
	public int speed(EHighwayLevel highwayLevel) {
		switch (highwayLevel) {
			case motorway:
				return 100;
			case motorway_link:
				return 70;
			case trunk:
				return 90;
			case trunk_link:
				return 60;
			case primary:
				return 50;
			case primary_link:
				return 40;
			case secondary:
				return 42;
			case secondary_link:
				return 42;
			case tertiary:
				return 50;
			case residential:
				return 30;
			case road:
				return 40;
			case living_street:
				return 6;
			case cycleway:
				return 25;

			case raceway:
				return 300;

			default:
				return 6;
		}
	}
}
