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
package org.mapsforge.preprocessing.graph.routingGraphInterface;

/**
 * 
 */
public interface IRgEdge {

	/**
	 * @return vertex id of source vertex.
	 */
	public int getSourceId();

	/**
	 * @return vertex id of target vertex.
	 */
	public int getTargetId();

	/**
	 * @return directed or undirected.
	 */
	public boolean isUndirected();

	/**
	 * @return returns waypoint longitudes in degree.
	 */
	public double[] getLongitudes();

	/**
	 * @return returns waypoint latitudes in degree.
	 */
	public double[] getLatitudes();

}
