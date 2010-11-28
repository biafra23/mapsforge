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
package org.mapsforge.preprocessing.graph.osm2rg.routingGraph;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;

import org.mapsforge.preprocessing.graph.routingGraphInterface.IRgWeightFunction;

/**
 * No good heuristcs until now, should be extended along with edge data.
 */
public class RgWeightFunctionTime implements IRgWeightFunction<RgEdge> {

	private final HashMap<String, Double> highwayLevelAverageSpeed;
	private double defaultSpeed;

	/**
	 * @param highwayLevelAverageSpeed
	 *            map highway level to average speed in km/h
	 * @param defaultSpeed
	 *            default average speed in km/h
	 */
	public RgWeightFunctionTime(HashMap<String, Double> highwayLevelAverageSpeed,
			double defaultSpeed) {
		this.highwayLevelAverageSpeed = highwayLevelAverageSpeed;
		this.defaultSpeed = defaultSpeed;
	}

	/**
	 * Read the average speed form a highwayLevel2AverageSpeed File.
	 * 
	 * @param file
	 *            the source file.
	 * @throws IOException
	 *             on error reading file.
	 */
	public RgWeightFunctionTime(File file) throws IOException {
		LineNumberReader reader = new LineNumberReader(new FileReader(file));
		String line = reader.readLine();
		this.highwayLevelAverageSpeed = new HashMap<String, Double>();
		while (line != null) {

			line = line.trim();
			if (!line.startsWith("#") && line.length() > 0) {
				String[] tmp = line.split("=");
				if (tmp.length == 2) {
					String key = tmp[0];
					try {
						double value = Double.parseDouble(tmp[1]);
						if (key.equals("DEFAULT_VALUE")) {
							this.defaultSpeed = value;
						} else {
							highwayLevelAverageSpeed.put(key, value);
						}
					} catch (NumberFormatException e) {
						e.printStackTrace();
						throw new IllegalArgumentException("wrong file format!");
					}
				} else {
					throw new IllegalArgumentException("wrong file format!");
				}
			}
			line = reader.readLine();

		}
	}

	@Override
	public double getWeightDouble(RgEdge edge) {
		double m_per_s;
		if (highwayLevelAverageSpeed.containsKey(edge.getHighwayLevel())) {
			m_per_s = highwayLevelAverageSpeed.get(edge.getHighwayLevel()) / 3.6d;
		} else {
			m_per_s = defaultSpeed / 3.6d;
		}
		return (edge.getLengthMeters() / m_per_s) * 10;
	}

	@Override
	public int getWeightInt(RgEdge edge) {
		return (int) Math.rint(getWeightDouble(edge));
	}

}
