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

import java.io.FileInputStream;
import java.util.Vector;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.MercatorProjection;
import org.mapsforge.poi.PointOfInterest;
import org.mapsforge.preprocessing.graph.osm2rg.osmxml.TagHighway;
import org.mapsforge.server.routing.IEdge;
import org.mapsforge.server.routing.IRouter;
import org.mapsforge.server.routing.IVertex;
import org.mapsforge.server.routing.highwayHierarchies.HHRouterServerside;

/**
 * Turn by turn directions contain a way which was found by a routing algorithm. Streets which
 * are of them same name are concatenated, except in case of a U-turn. Street lengths and angles
 * between streets are calculated and saved.
 * 
 * Several methods to get a string representation like GeoJSON, KML or plain text are provided.
 * 
 * @author Eike Send
 */
public class TurnByTurnDescription {
	private static final int MIN_DISTANCE_TO_JUNCTION_FOR_ANGLE_MEASURING = 10;
	/** Navigation mode: Not set */
	public static final int NO_MODE = -1;
	/** Navigation mode: motorway */
	public static final int MOTORWAY_MODE = 0;
	/** Navigation mode: city */
	public static final int CITY_MODE = 1;
	/** Navigation mode: regional */
	public static final int REGIONAL_MODE = 2;
	private static final double VERY_SHORT_STREET_LENGTH = 20;
	Vector<TurnByTurnStreet> streets = new Vector<TurnByTurnStreet>();

	/** landmark generator */
	public static LandmarksFromPerst landmarkService;
	private static boolean debugstatus = false;

	private static void debug(String msg) {
		if (debugstatus)
			System.out.println(msg);
	}

	/**
	 * Constructs a TurnByTurnDirectionsObject from an array of IEdges as the are provided by
	 * the IRouter
	 * 
	 * @param routeEdges
	 *            is the IEdges array to convert to directions
	 */
	public TurnByTurnDescription(IEdge[] routeEdges) {
		generateDirectionsFromPath(routeEdges);
	}

	void generateDirectionsFromPath(IEdge[] edges) {
		if (edges == null || edges.length == 0)
			return;
		// These are the edges which are used to make decisions based on local information
		IEdge lastEdge;
		IEdge edgeBeforePoint;
		IEdge edgeAfterPoint;
		IEdge nextEdge;
		// These are both the current decision point, which is at the end of the current edge
		IVertex decisionPointVertex;
		GeoCoordinate decisionPointCoord;
		// These don't change in the process, they are the beginning and end of the route
		GeoCoordinate startPoint = edges[0].getSource().getCoordinate();
		GeoCoordinate endPoint = edges[edges.length - 1].getTarget().getCoordinate();
		PointOfInterest startCity = null;
		PointOfInterest endCity = null;
		if (landmarkService != null) {
			startCity = landmarkService.getCity(startPoint);
			endCity = landmarkService.getCity(endPoint);
			debug("start:" + startCity);
			debug("end:" + endCity);
		}

		// this contains concatenated IEdges and represents the current street / road
		TurnByTurnStreet currentStreet = new TurnByTurnStreet(edges[0]);
		TurnByTurnStreet lastStreet = null;
		// What navigational mode is the current and what was the last one
		int routingMode = NO_MODE;
		int lastRoutingMode = NO_MODE;
		// The whole point of this method boils down to the question if at a given potential
		// decision point a new instruction is to be generated. This boolean represents that.
		boolean startANewStreet;
		for (int i = 0; i < edges.length; i++) {
			// First setup the "environment" variables, ie the edges and points around the
			// potential decision point
			edgeBeforePoint = edges[i];
			decisionPointVertex = edgeBeforePoint.getTarget();
			decisionPointCoord = decisionPointVertex.getCoordinate();
			if (i > 0) {
				lastEdge = edges[i - 1];
			} else {
				lastEdge = null;
			}
			if (i < edges.length - 1) {
				edgeAfterPoint = edges[i + 1];
			} else {
				edgeAfterPoint = null;
			}
			if (i < edges.length - 2) {
				nextEdge = edges[i + 2];
			} else {
				nextEdge = null;
			}
			// Now the variables are set up.
			// First determine which kind of navigational level we're on
			lastRoutingMode = routingMode;

			// if we're on a motorway
			if (isMotorway(edgeBeforePoint)) {
				routingMode = MOTORWAY_MODE;
			}
			// if we're in the start or destination city, we'll do local navigation
			else if (isInStartOrDestinationCity(startCity, endCity, decisionPointCoord)) {
				routingMode = CITY_MODE;
			}
			// if we're not in the start- or end city but on a primary again its motorway
			// routing
			else if (isPrimary(edgeBeforePoint)) {
				routingMode = MOTORWAY_MODE;
			} else {
				// if we're not in the start- or end city and not on a motorway, trunk or
				// primary we must be in regional mode
				routingMode = REGIONAL_MODE;
			}
			// Now that the mode of travel has been determined we need to figure out if a new
			// street is to be started
			startANewStreet = false;
			switch (routingMode) {
				case CITY_MODE:
					startANewStreet = startNewStreetCityMode(lastEdge, edgeBeforePoint,
							edgeAfterPoint, nextEdge, currentStreet);
					break;
				case REGIONAL_MODE:
					startANewStreet = startNewStreetRegionalMode(lastEdge, edgeBeforePoint,
							edgeAfterPoint, nextEdge, currentStreet);
					break;
				case MOTORWAY_MODE:
					startANewStreet = startNewStreetMotorwayMode(lastEdge, edgeBeforePoint,
							edgeAfterPoint, nextEdge, currentStreet);
					break;
			}
			if (lastRoutingMode == NO_MODE) {
				lastRoutingMode = routingMode;
			}
			if (lastRoutingMode != routingMode) {
				startANewStreet = true;
			}

			if (startANewStreet) {
				currentStreet.addLandmark(routingMode, landmarkService);
				currentStreet.turnByTurnText = TurnByTurnDescriptionToString
						.getTextDescription(currentStreet, lastStreet, routingMode);
				streets.add(currentStreet);
				lastStreet = currentStreet;
				if (edgeAfterPoint != null) {
					currentStreet = new TurnByTurnStreet(edgeAfterPoint);
					currentStreet.routingmode = routingMode;
					if (currentStreet.angleFromStreetLastStreet == -360) {
						double delta = getAngleOfEdges(edgeBeforePoint, edgeAfterPoint);
						currentStreet.angleFromStreetLastStreet = delta;
					}
				}
			} else {
				if (currentStreet.isRoundabout
						&& decisionPointVertex.getOutboundEdges().length > 1) {
					currentStreet.exitCount++;
				}
				currentStreet.appendCoordinatesFromEdge(edgeAfterPoint);
			}
		}
	}

	private boolean isInStartOrDestinationCity(PointOfInterest startCity,
			PointOfInterest endCity,
			GeoCoordinate decisionPointCoord) {
		if (landmarkService != null) {
			return ((startCity == landmarkService.getCity(decisionPointCoord)) || (endCity == landmarkService
					.getCity(decisionPointCoord)));
		}
		return false;
	}

	/**
	 * @param lastEdge
	 *            the edge before the edge before the decision point
	 * @param edgeBeforePoint
	 *            the edge before the decision point
	 * @param edgeAfterPoint
	 *            the edge after the decision point
	 * @param nextEdge
	 *            the edge after that
	 * @param currentStreet
	 *            the current street as a whole
	 * @return if a new street should be started
	 */
	private boolean startNewStreetCityMode(IEdge lastEdge, IEdge edgeBeforePoint,
			IEdge edgeAfterPoint, IEdge nextEdge, TurnByTurnStreet currentStreet) {
		// Only one instruction per U-turn is necessary
		// also U-Turns are really the sum of two right angle turns
		if (isUTurn(lastEdge, edgeBeforePoint, edgeAfterPoint)) {
			currentStreet.angleFromStreetLastStreet = 180;
			currentStreet.name = nextEdge.getName();
			debug("Decision: false, 2nd part of U-turn");
			return false;
		}
		if (haveSameName(edgeBeforePoint, edgeAfterPoint)) {
			// If a U-Turn is performed an instruction is needed
			if (isUTurn(edgeBeforePoint, edgeAfterPoint, nextEdge)) {
				debug("Decision: true, U-turn");
				return true;
			}
			if (isRightAngle(getAngleOfEdges(edgeBeforePoint, edgeAfterPoint))) {
				debug("Decision: true, right angle in road");
				return true;
			}
			debug("Decision: false, Same Name");
			return false;
		}
		if (isInTwoLaneJunction(lastEdge, edgeBeforePoint, edgeAfterPoint, nextEdge,
				currentStreet)) {
			debug("Decision: false, two lane junction");
			return false;
		}
		debug("Decision: true, default");
		return true;
	}

	/**
	 * @param lastEdge
	 *            the edge before the edge before the decision point
	 * @param edgeBeforePoint
	 *            the edge before the decision point
	 * @param edgeAfterPoint
	 *            the edge after the decision point
	 * @param nextEdge
	 *            the edge after that
	 * @param currentStreet
	 *            the current street as a whole
	 * @return if a new street should be started
	 */
	private boolean startNewStreetRegionalMode(IEdge lastEdge, IEdge edgeBeforePoint,
			IEdge edgeAfterPoint, IEdge nextEdge, TurnByTurnStreet currentStreet) {
		// haveSameRef(edgeBeforePoint, edgeAfterPoint)
		if (landmarkService != null) {
			currentStreet.addtown(landmarkService.getCity(
					edgeBeforePoint.getTarget().getCoordinate()));
		}
		if (isStraight(getAngleOfEdges(edgeBeforePoint, edgeAfterPoint))) {
			// System.out.println("Regional mode: "
			// + landmarkService.getCity(edgeAfterPoint.getTarget().getCoordinate()));

			return false;
		}
		if (landmarkService != null) {
			currentStreet.town = landmarkService.getCity(
					edgeBeforePoint.getTarget().getCoordinate());
		}
		return true;
	}

	/**
	 * @param lastEdge
	 *            the edge before the edge before the decision point
	 * @param edgeBeforePoint
	 *            the edge before the decision point
	 * @param edgeAfterPoint
	 *            the edge after the decision point
	 * @param nextEdge
	 *            the edge after that
	 * @param currentStreet
	 *            the current street as a whole
	 * @return if a new street should be started
	 */
	private boolean startNewStreetMotorwayMode(IEdge lastEdge, IEdge edgeBeforePoint,
			IEdge edgeAfterPoint, IEdge nextEdge, TurnByTurnStreet currentStreet) {
		if (haveSameRef(edgeBeforePoint, edgeAfterPoint)) {
			return false;
		}
		return true;
	}

	private boolean haveSameName(IEdge edge1, IEdge edge2) {
		if (edge2 == null)
			return false;
		if (edge1 == null)
			return false;
		if (edge1.getName() == null && edge2.getName() == null)
			return true;
		if (edge1.getName() == null || edge2.getName() == null)
			return false;
		return edge1.getName().equalsIgnoreCase(edge2.getName());
	}

	private boolean haveSameRef(IEdge edge1, IEdge edge2) {
		if (edge2 == null)
			return false;
		if (edge1 == null)
			return false;
		if (edge1.getRef() == null && edge2.getRef() == null)
			return true;
		if (edge1.getRef() == null || edge2.getRef() == null)
			return false;
		return edge1.getRef().equalsIgnoreCase(edge2.getRef());
	}

	private boolean isInTwoLaneJunction(IEdge lastEdge, IEdge edgeBeforePoint,
			IEdge edgeAfterPoint, IEdge nextEdge, TurnByTurnStreet currentStreet) {
		//
		// debug("isInTwoLaneJunction: " + edgeBeforePoint.getName());
		// debug("angle before: " +
		// getAngleOfEdges(lastEdge, edgeBeforePoint));
		// debug("angle after: " +
		// getAngleOfEdges(edgeAfterPoint, nextEdge));
		// debug("isStraight: " +
		// isStraight(getAngleOfEdges(edgeBeforePoint, edgeAfterPoint)));

		// Case I
		// If the edge after the decision point is very short and followed by a right angle,
		// the edgeAfterPoint is part of a two lane junction where the name is the one of
		// the street coming from the other direction
		if (isRightAngle(getAngleOfEdges(edgeAfterPoint, nextEdge))
				&& isStraight(getAngleOfEdges(edgeBeforePoint, edgeAfterPoint))
				&& isVeryShortEdge(edgeAfterPoint)) {
			debug("Two lane junction: before / case 1");
			return true;
		}
		// Case II
		// If there was a right angle turn between the last edge and the edge before the
		// decision point and this edge is very short, the edgeBeforePoint is part of a two lane
		// junction and no instruction is needed, only the name should be that of the actual
		// street
		if (isRightAngle(getAngleOfEdges(lastEdge, edgeBeforePoint))
				&& isStraight(getAngleOfEdges(edgeBeforePoint, edgeAfterPoint))
				&& isVeryShortEdge(edgeBeforePoint) && edgeAfterPoint != null) {
			debug("Two lane junction: after / case 2");
			currentStreet.name = edgeAfterPoint.getName();
			return true;
		}
		return false;
	}

	private boolean isRightAngle(double angle) {
		return (90d - 45d < angle && angle < 90d + 45d)
				|| (270d - 45d < angle && angle < 270d + 45d);
	}

	private boolean isStraight(double angle) {
		return (360d - 45d < angle || angle < 45d);
	}

	private boolean isVeryShortEdge(IEdge edge) {
		GeoCoordinate source = edge.getSource().getCoordinate();
		GeoCoordinate destination = edge.getTarget().getCoordinate();
		return source.sphericalDistance(destination) < VERY_SHORT_STREET_LENGTH;
	}

	static boolean isMotorway(IEdge curEdge) {
		return curEdge.getType() == TagHighway.MOTORWAY ||
				curEdge.getType() == TagHighway.MOTORWAY_LINK ||
				curEdge.getType() == TagHighway.TRUNK ||
				curEdge.getType() == TagHighway.TRUNK_LINK;
	}

	boolean isPrimary(IEdge curEdge) {
		return curEdge.getType() == TagHighway.PRIMARY ||
				curEdge.getType() == TagHighway.PRIMARY_LINK;
	}

	/**
	 * Check 3 edges to see if they form a U-turn.
	 * 
	 * @param edge1
	 *            second last Edge before the current edge
	 * @param edge2
	 *            last Edge before the current edge
	 * @param edge3
	 *            current Edge
	 * @return true if the edges form a u-turn around the 2nd edge
	 */
	private boolean isUTurn(IEdge edge1, IEdge edge2, IEdge edge3) {
		if (edge1 == null || edge2 == null || edge3 == null)
			return false;
		double angleSum = (getAngleOfEdges(edge1, edge2) + getAngleOfEdges(
				edge2, edge3)) % 360;
		if (haveSameName(edge1, edge3)
				&& (170 < angleSum && angleSum < 190)
				&& isVeryShortEdge(edge2)) {
			return true;
		}
		return false;
	}

	/**
	 * Calculate the angle between two IEdge objects / streets
	 * 
	 * @param edge1
	 *            the IEdge of the street before the crossing
	 * @param edge2
	 *            the IEdge of the street after the crossing
	 * @return the angle between the given streets
	 */
	private double getAngleOfEdges(IEdge edge1, IEdge edge2) {
		if (edge1 != null && edge2 != null) {
			// Let's see if i can get the angle between the last street and this
			// This is the crossing
			GeoCoordinate crossingCoordinate = edge2.getAllWaypoints()[0];
			// The following is the last coordinate before the crossing
			GeoCoordinate coordinateBefore = edge1.getAllWaypoints()[edge1
					.getAllWaypoints().length - 2];
			// Take a coordinate further away from the crossing if it's too close
			if (coordinateBefore.sphericalDistance(crossingCoordinate) < MIN_DISTANCE_TO_JUNCTION_FOR_ANGLE_MEASURING
					&& edge1.getAllWaypoints().length > 2) {
				coordinateBefore = edge1.getAllWaypoints()
						[edge1.getAllWaypoints().length - 3];
			}
			// Here comes the first coordinate after the crossing
			GeoCoordinate coordinateAfter = edge2.getAllWaypoints()[1];
			if (coordinateAfter.sphericalDistance(crossingCoordinate) < MIN_DISTANCE_TO_JUNCTION_FOR_ANGLE_MEASURING
					&& edge2.getAllWaypoints().length > 2) {
				coordinateAfter = edge2.getAllWaypoints()[2];
			}
			double delta = getAngleOfCoords(coordinateBefore, crossingCoordinate,
					coordinateAfter);
			return delta;
		}
		return -360;
	}

	static double getAngleOfCoords(GeoCoordinate lastCoordinate,
			GeoCoordinate crossingCoordinate, GeoCoordinate firstCoordinate) {
		double delta;
		// calculate angles of the incoming street
		double deltaY = MercatorProjection.latitudeToMetersY(crossingCoordinate
				.getLatitude())
				- MercatorProjection.latitudeToMetersY(lastCoordinate.getLatitude());
		double deltaX = MercatorProjection.longitudeToMetersX(crossingCoordinate
				.getLongitude())
				- MercatorProjection.longitudeToMetersX(lastCoordinate.getLongitude());
		double alpha = java.lang.Math.toDegrees(java.lang.Math.atan(deltaX / deltaY));
		if (deltaY < 0)
			alpha += 180; // this compensates for the atan result being between -90 and +90
		// deg
		// calculate angles of the outgoing street
		deltaY = MercatorProjection.latitudeToMetersY(firstCoordinate.getLatitude())
				- MercatorProjection.latitudeToMetersY(crossingCoordinate.getLatitude());
		deltaX = MercatorProjection.longitudeToMetersX(firstCoordinate.getLongitude())
				- MercatorProjection.longitudeToMetersX(crossingCoordinate.getLongitude());
		double beta = java.lang.Math.toDegrees(java.lang.Math.atan(deltaX / deltaY));
		if (deltaY < 0)
			beta += 180; // this compensates for the atan result being between -90 and +90
		// deg
		// the angle difference is angle of the turn,
		delta = alpha - beta;
		// For some reason the angle is conterclockwise, so it's turned around
		delta = 360 - delta;
		// make sure there are no values above 360 or below 0
		delta = java.lang.Math.round((delta + 360) % 360);
		return delta;
	}

	/**
	 * @param args
	 *            unused
	 */
	public static void main(String[] args) {
		try {
			long time = System.currentTimeMillis();
			FileInputStream iStream = new FileInputStream("C:/uni/niedersachsen_car.hh");
			IRouter router = HHRouterServerside.deserialize(iStream);
			iStream.close();
			time = System.currentTimeMillis() - time;
			debug("Loaded Router in " + time + " ms");
			time = System.currentTimeMillis();
			// String filename = "c:/uni/berlin_landmarks.dbs.clustered";
			String filename = "c:/uni/niedersachsen.perst";

			landmarkService = new LandmarksFromPerst(filename);
			time = System.currentTimeMillis() - time;
			debug("Loaded LandmarkBuilder in " + time + " ms");
			// int source = router.getNearestVertex(
			// new GeoCoordinate(52.491300151248, 13.29112072938)).getId();
			// int target = router.getNearestVertex(
			// new GeoCoordinate(52.474508242192, 13.385945407712)).getId();
			// int target = router.getNearestVertex(
			// new GeoCoordinate(52.513011299314, 13.288249038565)).getId();
			// Long haul using all modes of travel:
			int source = router.getNearestVertex(
					new GeoCoordinate(53.032582591161, 8.6618994748688)).getId();
			int target = router.getNearestVertex(
					new GeoCoordinate(53.134937444613, 8.2331393661737)).getId();
			IEdge[] shortestPath = router.getShortestPath(source, target);

			time = System.currentTimeMillis() - time;
			TurnByTurnDescription directions = new TurnByTurnDescription(shortestPath);
			time = System.currentTimeMillis() - time;
			debug("Route directions built in " + time + " ms");
			System.out.println(new TurnByTurnDescriptionToString(directions));
			landmarkService.persistenceManager.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}