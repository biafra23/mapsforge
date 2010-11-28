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
package org.mapsforge.preprocessing.util;

import java.util.HashMap;

import org.mapsforge.preprocessing.model.EHighwayLevel;

public class HighwayLevelExtractor {

	private static HashMap<String, EHighwayLevel> mapping;

	static {
		mapping = new HashMap<String, EHighwayLevel>();

		mapping.put("motorway", EHighwayLevel.motorway);
		mapping.put("motorway_link", EHighwayLevel.motorway_link);
		mapping.put("motorway-link", EHighwayLevel.motorway_link);
		mapping.put("trunk", EHighwayLevel.trunk);
		mapping.put("trunk_link", EHighwayLevel.trunk_link);
		mapping.put("trunk-link", EHighwayLevel.trunk_link);
		mapping.put("primary", EHighwayLevel.primary);
		mapping.put("primary_link", EHighwayLevel.primary_link);
		mapping.put("secondary", EHighwayLevel.secondary);
		mapping.put("tertiary", EHighwayLevel.tertiary);
		mapping.put("residential", EHighwayLevel.residential);
		mapping.put("living-street", EHighwayLevel.living_street);
		mapping.put("living_street", EHighwayLevel.living_street);
		mapping.put("track", EHighwayLevel.track);
		mapping.put("path", EHighwayLevel.path);
		mapping.put("pedestrian", EHighwayLevel.pedestrian);
		mapping.put("foot", EHighwayLevel.foot);
		mapping.put("footway", EHighwayLevel.footway);
		mapping.put("service", EHighwayLevel.service);
		mapping.put("steps", EHighwayLevel.steps);
		mapping.put("cycleway", EHighwayLevel.cycleway);
		mapping.put("bridleway", EHighwayLevel.bridleway);
		mapping.put("construction", EHighwayLevel.construction);
		mapping.put("raceway", EHighwayLevel.raceway);
		mapping.put("road", EHighwayLevel.road);

	}

	public static EHighwayLevel getLevel(String level) {
		EHighwayLevel ret = mapping.get(level);
		if (ret == null)
			ret = EHighwayLevel.unmapped;

		return ret;
	}

}
