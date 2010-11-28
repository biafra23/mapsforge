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
package org.mapsforge.preprocessing.graph.model.gui;

import java.util.HashSet;
import java.util.Iterator;

import org.mapsforge.preprocessing.model.EHighwayLevel;

public class Transport {

	private String name;
	private int maxSpeed;
	private HashSet<EHighwayLevel> useableWays;

	public Transport(String name, int speed) {
		this.name = name;
		this.maxSpeed = speed;
		this.useableWays = new HashSet<EHighwayLevel>();
	}

	public Transport(String name, int speed, HashSet<EHighwayLevel> ways) {
		this.name = name;
		this.maxSpeed = speed;
		this.useableWays = ways;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the maxSpeed
	 */
	public int getMaxSpeed() {
		return maxSpeed;
	}

	public String getUseableWaysSerialized() {
		String result = "";
		if (useableWays == null)
			return result;
		Iterator<EHighwayLevel> it = useableWays.iterator();
		while (it.hasNext()) {
			result += (it.next().toString() + ";");
		}
		return result;
	}

	@Override
	public String toString() {
		return name;

	}

	public HashSet<EHighwayLevel> getUseableWays() {
		return useableWays;
	}

	public boolean addHighwayLevelToUsableWays(EHighwayLevel hwyLvl) {
		if (!useableWays.contains(hwyLvl)) {
			useableWays.add(hwyLvl);
			return true;
		}

		return false;
	}

}
