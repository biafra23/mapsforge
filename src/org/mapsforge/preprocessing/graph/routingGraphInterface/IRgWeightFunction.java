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
 * @param <E>
 *            weights should be scaled so that summing up weights should not exceed respective
 *            data-type max value.
 */
public interface IRgWeightFunction<E extends IRgEdge> {

	/**
	 * 
	 * @param edge
	 *            the edge to be weighted.
	 * @return weight as double precision.
	 */
	public double getWeightDouble(E edge);

	/**
	 * @param edge
	 *            the edge to be weighted.
	 * @return weight as int.
	 */
	public int getWeightInt(E edge);
}