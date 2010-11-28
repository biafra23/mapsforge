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

import java.util.Collection;
import java.util.TreeMap;
import java.util.Vector;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.MercatorProjection;
import org.mapsforge.poi.PointOfInterest;
import org.mapsforge.poi.persistence.IPersistenceManager;
import org.mapsforge.poi.persistence.PersistenceManagerFactory;

/**
 * This is kind of a factory for Landmarks in certain rectangle
 * 
 * @author Eike
 */
public class LandmarksFromPerst {

	private static final double MAX_SIDE_DISTANCE_FROM_STREET_CITY_MODE = 20d;
	private static final double MAX_SIDE_DISTANCE_FROM_STREET_REGIONAL_MODE = 800;
	private static final double MAX_SIDE_DISTANCE_FROM_STREET_MOTORWAY_MODE = 0;

	private static final double MAX_REAR_DISTANCE_FROM_STREET_CITY_MODE = 150d;
	private static final double MAX_REAR_DISTANCE_FROM_STREET_REGIONAL_MODE = 5000;
	private static final double MAX_REAR_DISTANCE_FROM_STREET_MOTORWAY_MODE = 0;

	/** */
	public static final double MAX_DISTANCE_AROUND_JUNCTION = 30d;

	private static double[] maxDistanceToTheRearArray = new double[3];
	private static double[] maxDistanceToTheSideArray = new double[3];
	IPersistenceManager persistenceManager;

	static {
		maxDistanceToTheSideArray[TurnByTurnDescription.CITY_MODE] = MAX_SIDE_DISTANCE_FROM_STREET_CITY_MODE;
		maxDistanceToTheSideArray[TurnByTurnDescription.REGIONAL_MODE] = MAX_SIDE_DISTANCE_FROM_STREET_REGIONAL_MODE;
		maxDistanceToTheSideArray[TurnByTurnDescription.MOTORWAY_MODE] = MAX_SIDE_DISTANCE_FROM_STREET_MOTORWAY_MODE;

		maxDistanceToTheRearArray[TurnByTurnDescription.CITY_MODE] = MAX_REAR_DISTANCE_FROM_STREET_CITY_MODE;
		maxDistanceToTheRearArray[TurnByTurnDescription.REGIONAL_MODE] = MAX_REAR_DISTANCE_FROM_STREET_REGIONAL_MODE;
		maxDistanceToTheRearArray[TurnByTurnDescription.MOTORWAY_MODE] = MAX_REAR_DISTANCE_FROM_STREET_MOTORWAY_MODE;
	}

	/**
	 * construct a new landmark generator, so to speak
	 * 
	 * @param databaseFileURI
	 *            the file path of the database
	 */
	public LandmarksFromPerst(String databaseFileURI) {
		this.persistenceManager = PersistenceManagerFactory
				.getPerstMultiRtreePersistenceManager(databaseFileURI);
	}

	@Override
	public void finalize() {
		// free resources
		persistenceManager.close();
	}

	/**
	 * Finds the nearest City/Town/Village
	 * 
	 * @param coord
	 *            where to look
	 * @return nearest City
	 */
	public PointOfInterest getCity(GeoCoordinate coord) {
		TreeMap<Double, PointOfInterest> poisByDistance = new TreeMap<Double, PointOfInterest>();

		double latm = MercatorProjection.latitudeToMetersY(coord.getLatitude());
		double lonm = MercatorProjection.longitudeToMetersX(coord.getLongitude());
		GeoCoordinate searchBoundingBoxNE = new GeoCoordinate(
				MercatorProjection.metersYToLatitude(latm + 15000),
				MercatorProjection.metersXToLongitude(lonm + 15000)
				);
		GeoCoordinate searchBoundingBoxSW = new GeoCoordinate(
				MercatorProjection.metersYToLatitude(latm - 15000),
				MercatorProjection.metersXToLongitude(lonm - 15000)
				);
		Collection<PointOfInterest> poisInBoundingBox =
				persistenceManager.findInRect(searchBoundingBoxNE, searchBoundingBoxSW,
						"Settlement");
		for (PointOfInterest poi : poisInBoundingBox) {
			double dist = coord.sphericalDistance(poi.getGeoCoordinate());
			String type = poi.getCategory().getTitle();
			if ((type.equalsIgnoreCase("village") && dist < 1000) ||
					(type.equalsIgnoreCase("town") && dist < 2000) ||
					(type.equalsIgnoreCase("city") && dist < 10000)) {
				poisByDistance.put(dist, poi);
			}
		}
		if (poisByDistance.isEmpty()) {
			return null;
		}
		return poisByDistance.firstEntry().getValue();
	}

	/**
	 * get a landmark near the end of the street / edge
	 * 
	 * @param points
	 *            the street which is to be used
	 * @param routingMode
	 *            depending on what mode the sideway and distance to the rear as viewed from the
	 *            decision point differs. This can be any of the navigation modes defined
	 *            {@link TurnByTurnDescription}
	 * @return a landmark near the end or null
	 */
	public PointOfInterest getPOINearStreet(Vector<GeoCoordinate> points, int routingMode) {

		double maxDistanceToTheSide = maxDistanceToTheSideArray[routingMode];
		double maxDistanceToTheRear = maxDistanceToTheRearArray[routingMode];

		int i = points.size(); // This is >= 2
		GeoCoordinate source;
		GeoCoordinate target;
		double lengthOfStreetSoFar = 0;
		double currentLength;
		GeoCoordinate[] pointsAroundStreet = new GeoCoordinate[4];

		// This is the collection of the POIs:
		TreeMap<Double, PointOfInterest> poisInRectangle = new TreeMap<Double, PointOfInterest>();

		do {
			target = points.elementAt(i - 1);
			source = points.elementAt(i - 2);
			currentLength = source.sphericalDistance(target);
			lengthOfStreetSoFar += currentLength;

			MathVector streetVector = getVectorFromCoords(source, target, currentLength);
			MathVector streetNormalVector = streetVector.getNormalVector();

			// Here a bounding box around the street is calculated, which follows
			// the direction of the street, ie. it is not parallel to the latitude/longitude
			// lines
			pointsAroundStreet = getPointsAroundStreet(maxDistanceToTheSide, source, target,
					streetVector, streetNormalVector);

			// Calculate the outer bounding box of the tilted box
			GeoCoordinate[] bbox = getBoundingBox(pointsAroundStreet);

			// Get all landmarks within the outer bounding box of the coordinates
			Collection<PointOfInterest> poisInBoundingBox =
					persistenceManager.findInRect(bbox[0], bbox[1], "Landmark");

			// Keep only the landmarks which are within the inner bounding box (tilted
			// rectangle)
			// and put them in a map sorted by distance from vertex
			for (PointOfInterest poi : poisInBoundingBox) {
				if (isInsideRectangle(pointsAroundStreet, poi.getGeoCoordinate())) {
					poisInRectangle.put(target.sphericalDistance(poi.getGeoCoordinate()), poi);
				}
			}
			i--;
		} while (i >= 2 && lengthOfStreetSoFar < maxDistanceToTheRear
				&& poisInRectangle.isEmpty());

		if (poisInRectangle.isEmpty()) {
			return null;
		}
		return poisInRectangle.firstEntry().getValue();
	}

	private GeoCoordinate[] getPointsAroundStreet(double maxDistanceToTheSide,
			GeoCoordinate source,
			GeoCoordinate target, MathVector streetVector, MathVector streetNormalVector) {
		GeoCoordinate[] result = new GeoCoordinate[4];
		// These are the 2 front Coordinates
		result[0] = getOrthogonalGeoCoordinate(target, streetNormalVector,
				-maxDistanceToTheSide);
		result[1] = getOrthogonalGeoCoordinate(target, streetNormalVector,
				maxDistanceToTheSide);
		// These are the 2 rear Coordinates
		result[2] = getOrthogonalGeoCoordinate(source, streetNormalVector,
				maxDistanceToTheSide);
		result[3] = getOrthogonalGeoCoordinate(source, streetNormalVector,
				-maxDistanceToTheSide);
		// additionally, go a short distance ahead onto the junction
		result[0] = getOrthogonalGeoCoordinate(result[0],
				streetVector,
				MAX_DISTANCE_AROUND_JUNCTION);
		result[1] = getOrthogonalGeoCoordinate(result[1],
				streetVector,
				MAX_DISTANCE_AROUND_JUNCTION);

		return result;
	}

	private MathVector getVectorFromCoords(GeoCoordinate source, GeoCoordinate target,
			double length) {
		return new MathVector(
				MercatorProjection.longitudeToMetersX(target.getLongitude()) -
						MercatorProjection.longitudeToMetersX(source.getLongitude()),
				MercatorProjection.latitudeToMetersY(target.getLatitude()) -
						MercatorProjection.latitudeToMetersY(source.getLatitude()),
						length);
	}

	private static GeoCoordinate getOrthogonalGeoCoordinate(GeoCoordinate target,
			MathVector normalVector, double d) {
		return new GeoCoordinate(
				MercatorProjection.metersYToLatitude(
						MercatorProjection.latitudeToMetersY(target.getLatitude())
								+ d * normalVector.y),
				MercatorProjection.metersXToLongitude(
						MercatorProjection.longitudeToMetersX(target.getLongitude())
								+ d * normalVector.x));
	}

	/**
	 * This function checks if {@link GeoCoordinate} t is on one side or the other of a line
	 * formed by the first two parameters.
	 * 
	 * It is used at the core of the function which checks if a {@link GeoCoordinate} is inside
	 * a rectangle.
	 * 
	 * @param p1
	 *            first point of the line
	 * @param p2
	 *            second point of the line
	 * @param t
	 *            the point be checked
	 * @return -1 for one side or 1 for the other
	 */
	private static short whichSide(GeoCoordinate p1, GeoCoordinate p2, GeoCoordinate t) {
		if (p2.getLongitude() == p1.getLongitude()) {
			if (t.getLongitude() > p1.getLongitude())
				return -1;
			return 1;
		}
		if (t.getLatitude() - p1.getLatitude() -
				(p2.getLatitude() - p1.getLatitude())
				/ (p2.getLongitude() - p1.getLongitude())
				* (t.getLongitude() - p1.getLongitude()) < 0) {
			return -1;
		}
		return 1;
	}

	private static boolean isOnTheSameSide(GeoCoordinate p1, GeoCoordinate p2,
			GeoCoordinate t1, GeoCoordinate t2) {
		return whichSide(p1, p2, t1) == whichSide(p1, p2, t2);
	}

	/**
	 * Determine if a {@link GeoCoordinate} is inside a rectangle (actually any convex foursided
	 * geometry works)
	 * 
	 * @param coords
	 *            rectangle
	 * @param t
	 *            the coordinate to be tested
	 * @return true if the last parameter is inside the rectangle, false if not
	 */
	public static boolean isInsideRectangle(GeoCoordinate[] coords, GeoCoordinate t) {
		return isOnTheSameSide(coords[0], coords[1], coords[2], t) &&
				isOnTheSameSide(coords[1], coords[2], coords[3], t) &&
				isOnTheSameSide(coords[2], coords[3], coords[0], t) &&
				isOnTheSameSide(coords[3], coords[0], coords[1], t);
	}

	private static GeoCoordinate[] getBoundingBox(GeoCoordinate[] coords) {
		double north = coords[0].getLatitude();
		double east = coords[0].getLongitude();
		double south = coords[0].getLatitude();
		double west = coords[0].getLongitude();
		for (GeoCoordinate c : coords) {
			if (c.getLatitude() < south)
				south = c.getLatitude();
			if (c.getLongitude() < west)
				west = c.getLongitude();
			if (c.getLatitude() > north)
				north = c.getLatitude();
			if (c.getLongitude() > east)
				east = c.getLongitude();
		}
		GeoCoordinate sw = new GeoCoordinate(south, west);
		GeoCoordinate ne = new GeoCoordinate(north, east);
		return new GeoCoordinate[] { sw, ne };
	}
}

class MathVector {
	double x;
	double y;
	double l;

	/*
	 * public MathVector(GeoCoordinate c1, GeoCoordinate c2) {
	 * 
	 * }
	 */

	public MathVector(double x, double y, double l) {
		this.x = x / l;
		this.y = y / l;
		this.l = l;
	}

	public MathVector(double x, double y) {
		this.x = x;
		this.y = y;
		this.l = 1;
	}

	public MathVector getNormalVector() {
		return new MathVector(y, -x);
	}
}
