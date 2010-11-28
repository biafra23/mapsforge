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

import java.util.Vector;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.server.routing.IEdge;
import org.mapsforge.server.routing.IVertex;

/**
 * Dummy vertex class
 * 
 * @author Eike
 */
public class DummyVertex implements IVertex {

	GeoCoordinate geo;
	Vector<IEdge> outbound;

	DummyVertex(GeoCoordinate geo) {
		this.geo = geo;
	}

	@Override
	public GeoCoordinate getCoordinate() {
		return geo;
	}

	@Override
	public int getId() {
		return 0;
	}

	public void addEdge(IEdge newEdge) {
		outbound.add(newEdge);
	}

	@Override
	public IEdge[] getOutboundEdges() {
		return (IEdge[]) outbound.toArray();
	}

}
