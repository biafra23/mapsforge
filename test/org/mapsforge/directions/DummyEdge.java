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
import org.mapsforge.server.routing.IEdge;
import org.mapsforge.server.routing.IVertex;

/**
 * This Class is for testing purposes only.
 * 
 * @author Eike
 */
public class DummyEdge implements IEdge {
	GeoCoordinate[] wayPoints;
	int id;
	String name = "";
	String ref = "";
	String type = "";
	boolean roundabout = false;

	public DummyEdge(GeoCoordinate[] wayPoints, String name) {
		this.wayPoints = wayPoints;
		this.name = name;
	}

	public DummyEdge(GeoCoordinate[] wayPoints, String name, String ref, String type,
			boolean roundabout) {
		this.wayPoints = wayPoints;
		this.name = name;
		this.ref = ref;
		this.type = type;
		this.roundabout = roundabout;
	}

	@Override
	public GeoCoordinate[] getAllWaypoints() {
		return wayPoints;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IVertex getSource() {
		DummyVertex src = new DummyVertex(wayPoints[0]);
		return src;
	}

	@Override
	public IVertex getTarget() {
		DummyVertex target = new DummyVertex(wayPoints[wayPoints.length - 1]);
		return target;
	}

	@Override
	public GeoCoordinate[] getWaypoints() {
		return wayPoints;
	}

	@Override
	public int getWeight() {
		return 0;
	}

	@Override
	public String getRef() {
		return ref;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public boolean isRoundabout() {
		return roundabout;
	}

	@Override
	public String getDestination() {
		// TODO Auto-generated method stub
		return null;
	}

}
