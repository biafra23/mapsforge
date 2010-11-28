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

import java.util.Arrays;
import java.util.Vector;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.poi.PointOfInterest;
import org.mapsforge.server.routing.IEdge;

/**
 * Represents one or many routing graph edges which belong to the same street
 * 
 * @author Eike
 */
class TurnByTurnStreet {
	double length = 0;
	double angleFromStreetLastStreet = -360;
	boolean isRoundabout = false;
	Vector<GeoCoordinate> points = new Vector<GeoCoordinate>();
	String name = "";
	String ref = "";
	String type = "";
	String destination = "";
	PointOfInterest nearestLandmark;
	int exitCount = 1;
	int routingmode;
	String turnByTurnText;
	PointOfInterest town;
	Vector<String> towns = new Vector<String>();

	/**
	 * Constructor for using a single IEdge
	 * 
	 * @param edge
	 *            turn this IEdge into a new TurnByTurnStreet
	 */
	TurnByTurnStreet(IEdge edge) {
		name = edge.getName();
		ref = edge.getRef();
		type = edge.getType();
		destination = edge.getDestination();
		isRoundabout = edge.isRoundabout();
		routingmode = TurnByTurnDescription.isMotorway(edge) ? 0 : 1;
		appendCoordinatesFromEdge(edge);
	}

	/**
	 * Append the GeoCoordinates of the given edge to the internal data structure
	 * 
	 * @param edge
	 *            The edge to take the GeoCoordinates from
	 */
	void appendCoordinatesFromEdge(IEdge edge) {
		if (edge != null) {
			GeoCoordinate[] newWaypoints = edge.getAllWaypoints();
			if (points.size() > 0 && newWaypoints[0].equals(points.lastElement())) {
				points.removeElementAt(points.size() - 1);
			}
			points.addAll(Arrays.asList(edge.getAllWaypoints()));
			for (int i = 0; i < newWaypoints.length - 1; i++) {
				length += newWaypoints[i].sphericalDistance(newWaypoints[i + 1]);
			}
		}
	}

	public void addLandmark(int routingMode, LandmarksFromPerst landmarkService) {
		if (landmarkService != null)
			nearestLandmark = landmarkService.getPOINearStreet(this.points, routingMode);
	}

	public void addtown(PointOfInterest city) {
		if (city != null && !towns.contains(city.getName())) {
			towns.add(city.getName());
		}
	}
}
