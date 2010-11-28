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
package org.mapsforge.preprocessing.graph.osm2rg.osmxml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;

import org.mapsforge.preprocessing.graph.osm2rg.osmxml.OsmRelation.Member;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Runs through osm xml and calls all registered handlers on occurance of the respective osm
 * element.
 */
public class OsmXmlParser {

	private final XMLReader xmlReader;
	final LinkedList<IOsmWayListener> wayListeners;
	final LinkedList<IOsmNodeListener> nodeListeners;
	final LinkedList<IOsmRelationListener> relationListeners;

	/**
	 * Constructs a new Osm xml Parser.
	 * 
	 * @throws SAXException
	 *             on error parsing document.
	 */
	public OsmXmlParser() throws SAXException {
		this.xmlReader = XMLReaderFactory.createXMLReader();
		this.wayListeners = new LinkedList<IOsmWayListener>();
		this.nodeListeners = new LinkedList<IOsmNodeListener>();
		this.xmlReader.setContentHandler(new OsmXmlHandler());
		this.relationListeners = new LinkedList<IOsmRelationListener>();

	}

	/**
	 * Registers a way listener at the parser. All listeners are notified by callbacks on
	 * occurence of the respective element of type way.
	 * 
	 * @param wayListener
	 *            the listener to register.
	 */
	public void addWayListener(IOsmWayListener wayListener) {
		if (wayListener != null && !this.wayListeners.contains(wayListener)) {
			this.wayListeners.add(wayListener);
		}
	}

	/**
	 * Registers a node listener at the parser. All listeners are notified by callbacks on
	 * occurence of the respective element of type node.
	 * 
	 * @param nodeListener
	 *            the listener to register.
	 */
	public void addNodeListener(IOsmNodeListener nodeListener) {
		if (nodeListener != null && !this.nodeListeners.contains(nodeListener)) {
			this.nodeListeners.add(nodeListener);
		}
	}

	/**
	 * Registers a relation listener at the parser. All listeners are notified by callbacks on
	 * occurence of the respective element of type relation.
	 * 
	 * @param relationListener
	 *            the listener to register.
	 */
	public void addRelationListener(IOsmRelationListener relationListener) {
		if (relationListener != null && !this.nodeListeners.contains(relationListener)) {
			this.relationListeners.add(relationListener);
		}
	}

	/**
	 * removes all listeners.
	 */
	public void resetListners() {
		this.wayListeners.clear();
		this.nodeListeners.clear();
		this.relationListeners.clear();
	}

	/**
	 * Starts parsing the osm file. Triggers call backs to all registered Listeners.
	 * 
	 * @param osmXmlFile
	 *            the source file.
	 * @throws IOException
	 *             on error reading file.
	 * @throws SAXException
	 *             on xml related error.
	 */
	public void parse(File osmXmlFile) throws IOException, SAXException {
		FileInputStream istream = new FileInputStream(osmXmlFile);
		InputSource isource = new InputSource(istream);
		xmlReader.parse(isource);
		istream.close();
	}

	class OsmXmlHandler extends DefaultHandler {

		private OsmNode currentNode;
		private OsmWay currentWay;
		private OsmRelation currentRelation;

		@Override
		public void startDocument() {
			this.currentNode = null;
			this.currentWay = null;
			this.currentRelation = null;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) {
			/* nodes */
			if (qName.equals("node")) {
				Long id = Long.parseLong(attributes.getValue("id"));
				double lon = Double.parseDouble(attributes.getValue("lon"));
				double lat = Double.parseDouble(attributes.getValue("lat"));
				String user = attributes.getValue("user");
				Timestamp timestamp = parseTimestampTag(attributes.getValue("timestamp"));
				boolean visible = parseBooleanTag(attributes.getValue("visible"), true);
				this.currentNode = new OsmNode(id, timestamp, user, visible, lon, lat);
			}
			/* node tags */
			else if (currentNode != null && qName.equals("tag")) {
				currentNode.setTag(attributes.getValue("k"), attributes.getValue("v"));
			}
			/* ways */
			else if (qName.equals("way")) {
				Long id = Long.parseLong(attributes.getValue("id"));
				String user = attributes.getValue("user");
				Timestamp timestamp = parseTimestampTag(attributes.getValue("timestamp"));
				boolean visible = parseBooleanTag(attributes.getValue("visible"), true);
				this.currentWay = new OsmWay(id, timestamp, user, visible);
			}
			/* way tags */
			else if (currentWay != null && qName.equals("tag")) {
				currentWay.setTag(attributes.getValue("k"), attributes.getValue("v"));
			}
			/* way node refs */
			else if (currentWay != null && qName.equals("nd")) {
				try {
					long id = Long.parseLong(attributes.getValue("ref"));
					currentWay.addNodeRef(id);
				} catch (NumberFormatException e) {
					// skip this reference TODO : check if this should be handled another way.
				}
				/* relation */
			} else if (qName.equals("relation")) {
				Long id = Long.parseLong(attributes.getValue("id"));
				String user = attributes.getValue("user");
				Timestamp timestamp = parseTimestampTag(attributes.getValue("timestamp"));
				boolean visible = parseBooleanTag(attributes.getValue("visible"), true);
				this.currentRelation = new OsmRelation(id, timestamp, user, visible);
			}
			/* relation refs */
			else if (currentRelation != null && qName.equals("member")) {
				String type = attributes.getValue("type");
				Long refId = Long.parseLong(attributes.getValue("ref"));
				String role = attributes.getValue("role");
				/* way refs */
				currentRelation.addMember(new Member(type, role, refId));

			}
			/* relation tags */
			else if (currentRelation != null && qName.equals("tag")) {
				currentRelation.setTag(attributes.getValue("k"), attributes.getValue("v"));
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			if (qName.equals("node")) {
				Iterator<IOsmNodeListener> iter = nodeListeners.iterator();
				while (iter.hasNext()) {
					iter.next().handleNode(currentNode);
				}
				currentNode = null;
			} else if (qName.equals("way")) {
				Iterator<IOsmWayListener> iter = wayListeners.iterator();
				while (iter.hasNext()) {
					iter.next().handleWay(currentWay);
				}
				currentWay = null;
			} else if (qName.equals("relation")) {
				Iterator<IOsmRelationListener> iter = relationListeners.iterator();
				while (iter.hasNext()) {
					iter.next().handleRelation(currentRelation);
				}
				currentRelation = null;
			}
		}

		private boolean parseBooleanTag(String tag, boolean defaultValue) {
			if (tag == null)
				return defaultValue;
			if (defaultValue) {
				return !(tag.equals("false") || tag.equals("no") || tag.equals("f") || tag
						.equals("0"));
			}
			return (tag.equals("true") || tag.equals("yes") || tag.equals("t") || tag
					.equals("1"));

		}

		private Timestamp parseTimestampTag(String tag) {
			if (tag == null || tag.length() < 20)
				return null;
			String date = tag.substring(0, 10);
			String time = tag.substring(11, 19);
			try {
				Timestamp ts = Timestamp.valueOf(date + " " + time);
				return ts;
			} catch (Exception e) {
				return null;
			}
		}
	}
}
