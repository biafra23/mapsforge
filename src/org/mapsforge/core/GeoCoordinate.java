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
package org.mapsforge.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This immutable class represents a geographic coordinate with a latitude and longitude value.
 */
public class GeoCoordinate implements Comparable<GeoCoordinate> {

	/**
	 * The multiplication factor to convert from double to int.
	 */
	public static final double FACTOR_DOUBLE_TO_INT = 1000000;

	/**
	 * The largest possible latitude value.
	 */
	public static final double LATITUDE_MAX = 90;

	/**
	 * The smallest possible latitude value.
	 */
	public static final double LATITUDE_MIN = -90;

	/**
	 * The largest possible longitude value.
	 */
	public static final double LONGITUDE_MAX = 180;

	/**
	 * The smallest possible longitude value.
	 */
	public static final double LONGITUDE_MIN = -180;

	/**
	 * The internal latitude value.
	 */
	private final double latitude;

	/**
	 * The internal longitude value.
	 */
	private final double longitude;

	/**
	 * The RegEx pattern to read WKT points
	 */
	private static Pattern wktPointPattern = Pattern
			.compile(".*POINT\\s?\\(([\\d\\.]+)\\s([\\d\\.]+)\\).*");

	/**
	 * Constructs a new GeoCoordinate with the given latitude and longitude values, measured in
	 * degrees.
	 * 
	 * @param latitude
	 *            the latitude value in degrees.
	 * @param longitude
	 *            the longitude value in degrees.
	 * @throws IllegalArgumentException
	 *             if the latitude or longitude value is invalid.
	 */
	public GeoCoordinate(double latitude, double longitude) throws IllegalArgumentException {
		this.latitude = validateLatitude(latitude);
		this.longitude = validateLongitude(longitude);
	}

	/**
	 * Constructs a new GeoCoordinate with the given latitude and longitude values, measured in
	 * microdegrees.
	 * 
	 * @param latitudeE6
	 *            the latitude value in microdegrees.
	 * @param longitudeE6
	 *            the longitude value in microdegrees.
	 * @throws IllegalArgumentException
	 *             if the latitude or longitude value is invalid.
	 */
	public GeoCoordinate(int latitudeE6, int longitudeE6) throws IllegalArgumentException {
		this.latitude = validateLatitude(intToDouble(latitudeE6));
		this.longitude = validateLongitude(intToDouble(longitudeE6));
	}

	/**
	 * Constructs a new GeoCoordinate from a Well-Known-Text (WKT) representation of a point For
	 * example: POINT(13.4125 52.52235)
	 * 
	 * WKT is used in PostGIS and other spatial databases
	 * 
	 * @param wellKnownText
	 *            is the WKT point which describes the new GeoCoordinate, this needs to be in
	 *            degrees using a WGS84 representation. The coordinate order in the POINT is
	 *            defined as POINT(long lat)
	 */
	public GeoCoordinate(String wellKnownText) {
		Matcher m = wktPointPattern.matcher(wellKnownText);
		m.matches();
		this.longitude = validateLongitude(Double.parseDouble(m.group(1)));
		this.latitude = validateLatitude(Double.parseDouble(m.group(2)));
	}

	/**
	 * Constructs a new GeoCoordinate from a comma-separated String containing latitude and
	 * longitude values (also ';', ':' and whitespace work as separator). First latitude and
	 * longitude are interpreted as measured in degrees. If the coordinate is invalid, it is
	 * tried to interpret values as measured in microdegrees.
	 * 
	 * @param latLonString
	 *            the String containing the latitude and longitude values
	 * @return the GeoCoordinate
	 * @throws IllegalArgumentException
	 *             if the latLonString could not be interpreted as a coordinate
	 */
	public static GeoCoordinate fromString(String latLonString) {
		String[] splitted = latLonString.split("[,;:\\s]");
		if (splitted.length != 2)
			throw new IllegalArgumentException("cannot read coordinate, not a valid format");
		double latitude = Double.parseDouble(splitted[0]);
		double longitude = Double.parseDouble(splitted[1]);
		try {
			return new GeoCoordinate(latitude, longitude);
		} catch (IllegalArgumentException e) {
			return new GeoCoordinate(GeoCoordinate.doubleToInt(latitude),
					GeoCoordinate.doubleToInt(longitude));
		}
	}

	/**
	 * Checks the given latitude value and throws an exception if the value is out of range.
	 * 
	 * @param lat
	 *            the latitude value that should be checked.
	 * @return the latitude value.
	 * @throws IllegalArgumentException
	 *             if the latitude value is < LATITUDE_MIN or > LATITUDE_MAX.
	 */
	public static double validateLatitude(double lat) {
		if (lat < LATITUDE_MIN) {
			throw new IllegalArgumentException("invalid latitude value: " + lat);
		} else if (lat > LATITUDE_MAX) {
			throw new IllegalArgumentException("invalid latitude value: " + lat);
		} else {
			return lat;
		}
	}

	/**
	 * Checks the given longitude value and throws an exception if the value is out of range.
	 * 
	 * @param lon
	 *            the longitude value that should be checked.
	 * @return the longitude value.
	 * @throws IllegalArgumentException
	 *             if the longitude value is < LONGITUDE_MIN or > LONGITUDE_MAX.
	 */
	public static double validateLongitude(double lon) {
		if (lon < LONGITUDE_MIN) {
			throw new IllegalArgumentException("invalid longitude value: " + lon);
		} else if (lon > LONGITUDE_MAX) {
			throw new IllegalArgumentException("invalid longitude value: " + lon);
		} else {
			return lon;
		}
	}

	/**
	 * Returns the latitude value of this coordinate.
	 * 
	 * @return the latitude value of this coordinate.
	 */
	public double getLatitude() {
		return this.latitude;
	}

	/**
	 * Returns the longitude value of this coordinate.
	 * 
	 * @return the longitude value of this coordinate.
	 */
	public double getLongitude() {
		return this.longitude;
	}

	/**
	 * Returns the latitude value in microdegrees of this coordinate.
	 * 
	 * @return the latitude value in microdegrees of this coordinate.
	 */
	public int getLatitudeE6() {
		return doubleToInt(this.latitude);
	}

	/**
	 * Returns the longitude value in microdegrees of this coordinate.
	 * 
	 * @return the longitude value in microdegrees of this coordinate.
	 */
	public int getLongitudeE6() {
		return doubleToInt(this.longitude);
	}

	/**
	 * Calculate the spherical distance from this GeoCoordinate to another
	 * 
	 * Use vincentyDistance for more accuracy but less performance
	 * 
	 * @param other
	 *            The GeoCoordinate to calculate the distance to
	 * @return the distance in meters as a double
	 */
	public double sphericalDistance(GeoCoordinate other) {
		return sphericalDistance(this, other);
	}

	/**
	 * Calculate the spherical distance between two GeoCoordinates in meters using the Haversine
	 * formula
	 * 
	 * This calculation is done using the assumption, that the earth is a sphere, it is not
	 * though. If you need a higher precision and can afford a longer execution time you might
	 * want to use vincentyDistance
	 * 
	 * @param gc1
	 *            first GeoCoordinate
	 * @param gc2
	 *            second GeoCoordinate
	 * @return distance in meters as a double
	 * @throws IllegalArgumentException
	 *             if one of the arguments is null
	 */
	public static double sphericalDistance(GeoCoordinate gc1, GeoCoordinate gc2)
			throws IllegalArgumentException {
		if (gc1 == null || gc2 == null)
			throw new IllegalArgumentException(
					"The GeoCoordinates for distance calculations may not be null.");
		return sphericalDistance(gc1.getLongitude(), gc1.getLatitude(), gc2.getLongitude(), gc2
				.getLatitude());
	}

	/**
	 * Calculate the spherical distance between two GeoCoordinates in meters using the Haversine
	 * formula.
	 * 
	 * This calculation is done using the assumption, that the earth is a sphere, it is not
	 * though. If you need a higher precision and can afford a longer execution time you might
	 * want to use vincentyDistance
	 * 
	 * @param lon1
	 *            longitude of first coordinate
	 * @param lat1
	 *            latitude of first coordinate
	 * @param lon2
	 *            longitude of second coordinate
	 * @param lat2
	 *            latitude of second coordinate
	 * 
	 * @return distance in meters as a double
	 * @throws IllegalArgumentException
	 *             if one of the arguments is null
	 */
	public static double sphericalDistance(double lon1, double lat1, double lon2, double lat2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return c * WGS84.EQUATORIALRADIUS;
	}

	/**
	 * Distance based on the assumption that the earth is a sphere.
	 * 
	 * @param lon1
	 *            longitude of 1st coordinate.
	 * @param lat1
	 *            latitude of 1st coordinate.
	 * @param lon2
	 *            longitude of 2nd coordinate.
	 * @param lat2
	 *            latitude of 2nd coordinate.
	 * @return distance in meters.
	 */
	public static double sphericalDistance(int lon1, int lat1, int lon2, int lat2) {
		return sphericalDistance(intToDouble(lon1), intToDouble(lat1), intToDouble(lon2),
				intToDouble(lat2));
	}

	/**
	 * Calculate the spherical distance from this GeoCoordinate to another
	 * 
	 * Use "distance" for faster computation with less accuracy
	 * 
	 * @param other
	 *            The GeoCoordinate to calculate the distance to
	 * @return the distance in meters as a double
	 */
	public double vincentyDistance(GeoCoordinate other) {
		return vincentyDistance(this, other);
	}

	/**
	 * Calculates geodetic distance between two GeoCoordinates using Vincenty inverse formula
	 * for ellipsoids. This is very accurate but consumes more resources and time than the
	 * sphericalDistance method
	 * 
	 * Adaptation of Chriss Veness' JavaScript Code on
	 * http://www.movable-type.co.uk/scripts/latlong-vincenty.html
	 * 
	 * Paper: Vincenty inverse formula - T Vincenty, "Direct and Inverse Solutions of Geodesics
	 * on the Ellipsoid with application of nested equations", Survey Review, vol XXII no 176,
	 * 1975 (http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf)
	 * 
	 * @param gc1
	 *            first GeoCoordinate
	 * @param gc2
	 *            second GeoCoordinate
	 * 
	 * @return distance in meters between points as a double
	 */
	public static double vincentyDistance(GeoCoordinate gc1, GeoCoordinate gc2) {
		double f = 1 / WGS84.INVERSEFLATTENING;
		double L = Math.toRadians(gc2.getLongitude() - gc1.getLongitude());
		double U1 = Math.atan((1 - f) * Math.tan(Math.toRadians(gc1.getLatitude())));
		double U2 = Math.atan((1 - f) * Math.tan(Math.toRadians(gc2.getLatitude())));
		double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
		double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

		double lambda = L, lambdaP, iterLimit = 100;

		double cosSqAlpha = 0, sinSigma = 0, cosSigma = 0, cos2SigmaM = 0, sigma = 0, sinLambda = 0, sinAlpha = 0, cosLambda = 0;
		do {
			sinLambda = Math.sin(lambda);
			cosLambda = Math.cos(lambda);
			sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
					+ (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda)
					* (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
			if (sinSigma == 0)
				return 0; // co-incident points
			cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
			sigma = Math.atan2(sinSigma, cosSigma);
			sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
			cosSqAlpha = 1 - sinAlpha * sinAlpha;
			if (cosSqAlpha != 0) {
				cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
			} else {
				cos2SigmaM = 0;
			}
			double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
			lambdaP = lambda;
			lambda = L
					+ (1 - C)
					* f
					* sinAlpha
					* (sigma + C * sinSigma
							* (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
		} while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);

		if (iterLimit == 0)
			return 0; // formula failed to converge

		double uSq = cosSqAlpha
				* (Math.pow(WGS84.EQUATORIALRADIUS, 2) - Math.pow(WGS84.POLARRADIUS, 2))
				/ Math.pow(WGS84.POLARRADIUS, 2);
		double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
		double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
		double deltaSigma = B
				* sinSigma
				* (cos2SigmaM + B
						/ 4
						* (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM
								* (-3 + 4 * sinSigma * sinSigma)
								* (-3 + 4 * cos2SigmaM * cos2SigmaM)));
		double s = WGS84.POLARRADIUS * A * (sigma - deltaSigma);

		return s;
	}

	/**
	 * Converts a coordinate from degrees to microdegrees.
	 * 
	 * @param coordinate
	 *            the coordinate in degrees.
	 * @return the coordinate in microdegrees.
	 */
	public static int doubleToInt(double coordinate) {
		return (int) (coordinate * FACTOR_DOUBLE_TO_INT);
	}

	/**
	 * Converts a coordinate from microdegrees to degrees.
	 * 
	 * @param coordinate
	 *            the coordinate in microdegrees.
	 * @return the coordinate in degrees.
	 */
	public static double intToDouble(int coordinate) {
		return coordinate / FACTOR_DOUBLE_TO_INT;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof GeoCoordinate)) {
			return false;
		} else {
			GeoCoordinate other = (GeoCoordinate) obj;
			if (this.latitude != other.latitude) {
				return false;
			} else if (this.longitude != other.longitude) {
				return false;
			}
			return true;
		}
	}

	/**
	 * This method is necessary for inserting GeoCoordinates into tree data structures.
	 */
	@Override
	public int compareTo(GeoCoordinate geoCoordinate) {
		if (this.latitude > geoCoordinate.latitude || this.longitude > geoCoordinate.longitude) {
			return 1;
		} else if (this.latitude < geoCoordinate.latitude
				|| this.longitude < geoCoordinate.longitude) {
			return -1;
		}
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this.latitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.longitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "latitude: " + this.latitude + ", longitude: " + this.longitude;
	}
}