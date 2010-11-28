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

import java.sql.Timestamp;
import java.util.LinkedList;

/**
 * This class implements a ways object with regard to be used by the xml parser and its clients.
 * If a way is read, clients can register at the parser to receive a call back with an instance
 * of this class as parameter. This object should not be used for holding data within main
 * memory, it is just a temporary object to enable parsing on an object level, abstracting from
 * the xml structure.
 */
public class OsmWay extends OsmElement {

	private static int DEFAULT_TAG_VALUE_ONEWAY = 0;
	private static boolean DEFAULT_TAG_VALUE_URBAN = false;
	private static String DEFAULT_TAG_VALUE_NAME = "";
	private static String DEFAULT_TAG_VALUE_HIGHWAY_LEVEL = null;

	private LinkedList<Long> nodeRefs;

	/**
	 * Construct a Way Object by given Osm XML attributes.
	 * 
	 * @param id
	 *            the id of the way.
	 * @param timestamp
	 *            the timestamp of last modification.
	 * @param user
	 *            user of last modification.
	 * @param visible
	 *            visible or not.
	 */
	OsmWay(long id, Timestamp timestamp, String user, boolean visible) {
		super(id, timestamp, user, visible);
		this.nodeRefs = new LinkedList<Long>();
	}

	/**
	 * for use by the parser.
	 * 
	 * @param id
	 *            add the given node id to the list of node ids.
	 */
	void addNodeRef(Long id) {
		if (id != null) {
			nodeRefs.add(id);
		}
	}

	/**
	 * @return returns a list of node ids of all referenced nodes.
	 */
	public LinkedList<Long> getNodeRefs() {
		LinkedList<Long> tmp = new LinkedList<Long>();
		tmp.addAll(nodeRefs);
		return tmp;
	}

	/**
	 * Determines if the way is a one way. If it is, the result can be either -1 or 1. If it is
	 * not a one way the result is 0.
	 * 
	 * @return returns -1 if it is one way in reverse direction. returns 1 if it is one way in
	 *         original direction. return 0 if it is not a one way.
	 */
	public int isOneway() {
		String hwyLevel = getHighwayLevel();
		if (hwyLevel != null
				&& (hwyLevel.equals(TagHighway.MOTORWAY)
						|| hwyLevel.equals(TagHighway.MOTORWAY_LINK)
						|| hwyLevel.equals(TagHighway.TRUNK) || hwyLevel
						.equals(TagHighway.TRUNK_LINK))) {
			return 1;
		}
		String tag = getTag("oneway");
		if (tag == null) {
			return DEFAULT_TAG_VALUE_ONEWAY;
		} else if (tag.equals("true") || tag.equals("yes") || tag.equals("t")
				|| tag.equals("1")) {
			return 1;
		} else if (tag.equals("false") || tag.equals("no") || tag.equals("f")
				|| tag.equals("0")) {
			return 0;
		} else if (tag.equals("-1")) {
			return -1;
		} else {
			return DEFAULT_TAG_VALUE_ONEWAY;
		}
	}

	/**
	 * Gives the highway tag.
	 * 
	 * @return returns the value of the highway tag. If it is not set, the default value
	 *         DEFAULT_TAG_VALUE_HIGHWAY_LEVEL is be returned.
	 */
	public String getHighwayLevel() {
		String v = getTag("highway");
		if (v != null) {
			return v;
		}
		return DEFAULT_TAG_VALUE_HIGHWAY_LEVEL;
	}

	/**
	 * Not implemented yet.
	 * 
	 * @return returns true if the way lies within boundaries of a city StVO.
	 */
	public boolean isUrban() {
		return DEFAULT_TAG_VALUE_URBAN;
	}

	/**
	 * @return true if this way is a roundabout
	 */
	public boolean isRoundabout() {
		String v = getTag("junction");
		if (v == null) {
			return false;
		}
		if (v.equals("roundabout")) {
			return true;
		}
		return false;
	}

	/**
	 * @return returns the name of the street assigned by the name tag.
	 */
	public String getName() {
		String v = getTag("name");
		if (v == null) {
			return DEFAULT_TAG_VALUE_NAME;
		}
		return v;
	}

	/**
	 * @return ref tag, like "A 2" for a german autobahn
	 */
	public String getRef() {
		String v = getTag("ref");
		if (v == null) {
			return DEFAULT_TAG_VALUE_NAME;
		}
		return v;
	}

	/**
	 * @return destination tag, stating which way it is going
	 */
	public String getDestination() {
		String v = getTag("destination");
		if (v == null) {
			return DEFAULT_TAG_VALUE_NAME;
		}
		return v;
	}

}
