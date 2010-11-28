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

import java.io.StringWriter;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.poi.PointOfInterest;
import org.mapsforge.preprocessing.graph.osm2rg.osmxml.TagHighway;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility class to convert a {@link TurnByTurnDescription} object into a string representation,
 * plain text, GPX, GeoJSON or KML versions are available
 * 
 * @author Eike
 */
public class TurnByTurnDescriptionToString {
	TurnByTurnDescription model;
	Vector<TurnByTurnStreet> streets;

	/**
	 * @param model
	 *            the TurnByTurnDescription to be converted
	 */
	public TurnByTurnDescriptionToString(TurnByTurnDescription model) {
		this.streets = model.streets;
	}

	static String getTextDescription(TurnByTurnStreet currentStreet,
			TurnByTurnStreet lastStreet, int routingMode) {
		String result = "";
		String lengthText = "";
		lengthText = readableLength(currentStreet.length);
		String turnInstruction = "";
		// System.out.println(currentStreet.name + " " + currentStreet.ref + " "
		// + currentStreet.type + " " + currentStreet.routingmode);
		switch (routingMode) {
			case TurnByTurnDescription.REGIONAL_MODE:
				turnInstruction = angleToText(currentStreet.angleFromStreetLastStreet);
				result += turnInstruction + "onto " + currentStreet.name + " "
						+ currentStreet.ref;
				result += passingThroughText(currentStreet);
				result += "(R)\n\n";
				result += getTownDescription(currentStreet, lastStreet);
				break;
			case TurnByTurnDescription.MOTORWAY_MODE:
				if (currentStreet.type == TagHighway.MOTORWAY_LINK
						|| currentStreet.type == TagHighway.TRUNK_LINK) {
					if (!currentStreet.ref.isEmpty()) {
						// exiting the motorway
						result += "Use exit number " + currentStreet.ref + " ";
						if (!currentStreet.name.isEmpty()) {
							result += "(" + currentStreet.name + ") ";
						}
						result += "for " + lengthText;
					} else {
						// entering the motorway
						result += "Go on the motorway ";
						if (currentStreet.destination != null) {
							result += "going to " + currentStreet.destination;
						}
					}
				} else {
					if (currentStreet.type == TagHighway.MOTORWAY
							|| currentStreet.type == TagHighway.TRUNK) {
						// is motorway or trunk
						result += "Go on " + currentStreet.ref + " ";
						if (!currentStreet.destination.isEmpty()) {
							result += "(" + currentStreet.destination + ") ";
						}
						result += "for " + lengthText;
					} else {
						// is primary road
						result += angleToText(currentStreet.angleFromStreetLastStreet) + "\n";
						result += "Go on " + currentStreet.ref + " for " + lengthText;
					}
				}
				result += " (M)\n\n";
				break;
			case TurnByTurnDescription.CITY_MODE:
			default:
				String landmarkText = getCityLandmarkText(currentStreet);
				turnInstruction = angleToText(currentStreet.angleFromStreetLastStreet);
				if (currentStreet.isRoundabout) {
					result += "On roundabout " + currentStreet.name + ", ";
					result += "take exit " + currentStreet.exitCount + ".\n\n";
				} else {
					if (lastStreet == null || lastStreet.isRoundabout) {
						result += "Go on ";
					} else {
						result += turnInstruction + "onto ";
					}
					result += currentStreet.name + " for " + lengthText
							+ ". (C)\n\n";
					result += landmarkText;
				}
				break;
		}
		return result;
	}

	private static String passingThroughText(TurnByTurnStreet currentStreet) {
		String result = "";
		if (currentStreet.towns.size() > 0) {
			for (String town : currentStreet.towns) {
				if (town != null && currentStreet.town != null
						&& currentStreet.town.getName() != null
						&& !currentStreet.town.getName().equals(town)) {
					result += town + " ";
				}
			}
			if (result.length() > 0) {
				result = "\nPassing through " + result + "\n";
			}
		}
		return result;
	}

	private static String getTownDescription(TurnByTurnStreet currentStreet,
			TurnByTurnStreet lastStreet) {
		String result = "";
		if (currentStreet.town != null
				&& (lastStreet != null && lastStreet.town != currentStreet.town)) {
			result = "In " + currentStreet.town.getName() + "\n";
		}
		return result;
	}

	private static String getCityLandmarkText(TurnByTurnStreet currentStreet) {
		String result = "";
		if (currentStreet != null && currentStreet.nearestLandmark != null) {
			PointOfInterest landmark = currentStreet.nearestLandmark;
			double landmarkBearing = TurnByTurnDescription.getAngleOfCoords(
					currentStreet.points.firstElement(),
					currentStreet.points.lastElement(),
					landmark.getGeoCoordinate());
			double distanceToJunction = currentStreet.points.lastElement()
					.sphericalDistance(landmark.getGeoCoordinate());
			String landmarkName = landmark.getCategory().getTitle();
			if (landmark.getName() != null)
				landmarkName += " " + landmark.getName();
			if (landmarkBearing < 90 || landmarkBearing > 270
					|| distanceToJunction < LandmarksFromPerst.MAX_DISTANCE_AROUND_JUNCTION) {
				result += "At the corner of " + landmarkName;
			} else {
				result += readableLength(distanceToJunction) + " after " + landmarkName;
				if (landmarkBearing < 180) {
					result += " (right side)";
				} else {
					result += " (left side)";
				}
			}
			result += "\n";
		}
		return result;
	}

	private static String angleToText(double angle) {
		int delta = (int) java.lang.Math.round(angle / 45);
		String turnInstruction = "";
		switch (delta) {
			case 0:
			case 8:
				turnInstruction += "Go straight ";
				break;
			case 1:
				turnInstruction += "Make a slight right turn ";
				break;
			case 2:
				turnInstruction += "Make a right turn ";
				break;
			case 3:
				turnInstruction += "Make a sharp right turn ";
				break;
			case 4:
				turnInstruction += "Make U-Turn ";
				break;
			case 5:
				turnInstruction += "Make a sharp left turn ";
				break;
			case 6:
				turnInstruction += "Make a left turn ";
				break;
			case 7:
				turnInstruction += "Make slight left turn ";
				break;
			default:
				turnInstruction += "";
		}
		return turnInstruction;
	}

	private static String readableLength(double length) {
		double result = java.lang.Math.round(length / 10) * 10;
		String lengthText;
		if (result > 1000) {
			result = java.lang.Math.round(result / 100) / 10;
			lengthText = result + " km";
		} else {
			lengthText = (int) result + " m";
		}
		return lengthText;
	}

	@Override
	public String toString() {
		String result = "";
		for (int i = 0; i < streets.size(); i++) {
			TurnByTurnStreet s = streets.elementAt(i);
			result += s.turnByTurnText;
		}
		result += "You have reached your destination.\n";
		return result;
	}

	/**
	 * Generates a GeoJSON String which represents the route
	 * 
	 * @return a string containing a GeoJSON representation of the route
	 * @throws JSONException
	 *             if the construction of the JSON fails
	 */
	public String toJSONString() throws JSONException {
		JSONObject json = new JSONObject();
		JSONArray jsonfeatures = new JSONArray();
		json.put("type", "FeatureCollection");
		json.put("features", jsonfeatures);
		for (TurnByTurnStreet street : streets) {
			JSONObject jsonstreet = new JSONObject();
			jsonstreet.put("type", "Feature");
			JSONArray streetCoordinatesAsJson = new JSONArray();
			for (int j = 0; j < street.points.size(); j++) {
				GeoCoordinate sc = street.points.elementAt(j);
				streetCoordinatesAsJson.put(new JSONArray()
						.put(sc.getLongitude())
						.put(sc.getLatitude()));
			}
			jsonstreet.put("geometry", new JSONObject()
					.put("type", "LineString")
					.put("coordinates", streetCoordinatesAsJson));
			jsonstreet.put("properties", new JSONObject()
					.put("Name", street.name)
					.put("Ref", street.ref)
					.put("Length", street.length)
					.put("Angle", street.angleFromStreetLastStreet)
					.put("Directions", street.turnByTurnText)
					.put("Type", street.type));
			jsonfeatures.put(jsonstreet);
		}
		return json.toString(2);
	}

	/**
	 * Creates a KML (Keyhole markup language) version of the directions.
	 * 
	 * @return a KML string
	 * @throws ParserConfigurationException
	 *             if the DOM can't be built
	 * @throws TransformerConfigurationException
	 *             if turning the DOM into a string fails
	 * @throws TransformerException
	 *             if turning the DOM into a string fails
	 * @throws TransformerFactoryConfigurationError
	 *             if turning the DOM into a string fails
	 */
	public String toKML() throws ParserConfigurationException,
			TransformerConfigurationException, TransformerException,
			TransformerFactoryConfigurationError {
		// This creates a new DOM
		Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		// And let's get this started
		dom.setXmlVersion("1.0");
		dom.setXmlStandalone(true);
		Element kml = dom.createElement("kml");
		dom.appendChild(kml);
		kml.setAttribute("xmlns", "http://www.opengis.net/kml/2.2");
		Element document = dom.createElement("Document");
		kml.appendChild(document);
		Element name = dom.createElement("name");
		name.setTextContent("MapsForge directions from " + streets.firstElement().name + " to "
				+ streets.lastElement().name);
		document.appendChild(name);
		Element style = dom.createElement("Style");
		style.setAttribute("id", "MapsForgeStyle");
		document.appendChild(style);
		Element lineStyle = dom.createElement("LineStyle");
		style.appendChild(lineStyle);
		Element color = dom.createElement("color");
		color.setTextContent("ff0000ff");
		lineStyle.appendChild(color);
		Element width = dom.createElement("width");
		width.setTextContent("3");
		lineStyle.appendChild(width);
		for (TurnByTurnStreet street : streets) {
			Element placemark = dom.createElement("Placemark");
			document.appendChild(placemark);
			Element placemarkName = dom.createElement("name");
			placemarkName.setTextContent(street.name);
			placemark.appendChild(placemarkName);
			Element lineString = dom.createElement("LineString");
			placemark.appendChild(lineString);
			Element coordinates = dom.createElement("coordinates");
			lineString.appendChild(coordinates);
			String coordinatesContent = "";
			for (GeoCoordinate c : street.points) {
				coordinatesContent += c.getLongitude() + "," + c.getLatitude() + " ";
			}
			coordinatesContent = coordinatesContent.substring(0,
					coordinatesContent.length() - 1); // remove last space
			coordinates.setTextContent(coordinatesContent);
			Element extendedData = dom.createElement("ExtendedData");
			placemark.appendChild(extendedData);
			Element length = dom.createElement("Length");
			extendedData.appendChild(length);
			length.setTextContent(Double.toString(street.length));
			Element angle = dom.createElement("AngleToPreviousStreet");
			extendedData.appendChild(angle);
			angle.setTextContent(Double.toString(street.angleFromStreetLastStreet));
			Element styleUrl = dom.createElement("styleUrl");
			placemark.appendChild(styleUrl);
			styleUrl.setTextContent("#MapsForgeStyle");

		}
		// This is for turning the DOM object into a proper StringWriter
		StringWriter stringWriter = new StringWriter();
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(dom),
				new StreamResult(stringWriter));
		return stringWriter.getBuffer().toString();
	}

	/**
	 * Creates a GPX (GPS Exchange Format) version of the directions.
	 * 
	 * @return a KML string
	 * @throws ParserConfigurationException
	 *             if the DOM can't be built
	 * @throws TransformerConfigurationException
	 *             if turning the DOM into a string fails
	 * @throws TransformerException
	 *             if turning the DOM into a string fails
	 * @throws TransformerFactoryConfigurationError
	 *             if turning the DOM into a string fails
	 */
	public String toGPX() throws ParserConfigurationException,
			TransformerConfigurationException, TransformerException,
			TransformerFactoryConfigurationError {
		// This creates a new DOM
		Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		// And let's get this started
		dom.setXmlVersion("1.0");
		dom.setXmlStandalone(true);
		Element gpx = dom.createElement("gpx");
		dom.appendChild(gpx);
		gpx.setAttribute("version", "1.1");
		gpx.setAttribute("xmlns", "http://www.topografix.com/GPX/1/1");
		gpx.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		gpx.setAttribute("xmlns:mf", "http://tom.mapsforge.de");
		gpx.setAttribute("xsi:schemaLocation",
				"http://www.topografix.com/GPX/1/1 http://www.topografix.com/gpx/1/1/gpx.xsd");
		gpx.setAttribute("creator", "tom.mapsforge.de");
		Element metadata = dom.createElement("metadata");
		gpx.appendChild(metadata);
		Element name = dom.createElement("name");
		name.setTextContent("MapsForge directions from " + streets.firstElement().name + " to "
				+ streets.lastElement().name);
		metadata.appendChild(name);
		for (TurnByTurnStreet street : streets) {
			Element trk = dom.createElement("trk");
			gpx.appendChild(trk);
			Element trkName = dom.createElement("name");
			trkName.setTextContent(street.name);
			trk.appendChild(trkName);
			Element trkseg = dom.createElement("trkseg");
			trk.appendChild(trkseg);
			for (GeoCoordinate c : street.points) {
				Element trkpt = dom.createElement("trkpt");
				trkseg.appendChild(trkpt);
				trkpt.setAttribute("lat", Double.toString(c.getLatitude()));
				trkpt.setAttribute("lon", Double.toString(c.getLongitude()));
			}
			Element extensions = dom.createElement("extensions");
			trkseg.appendChild(extensions);
			Element length = dom.createElement("mf:Length");
			extensions.appendChild(length);
			length.setTextContent(Double.toString(street.length));
			Element angle = dom.createElement("mf:AngleToPreviousStreet");
			extensions.appendChild(angle);
			angle.setTextContent(Double.toString(street.angleFromStreetLastStreet));
		}
		// This is for turning the DOM object into a proper StringWriter
		StringWriter stringWriter = new StringWriter();
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(dom),
				new StreamResult(stringWriter));
		return stringWriter.getBuffer().toString();
	}
}
