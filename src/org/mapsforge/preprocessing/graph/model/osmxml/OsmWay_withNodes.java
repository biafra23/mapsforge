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
package org.mapsforge.preprocessing.graph.model.osmxml;

import java.sql.Timestamp;
import java.util.LinkedList;

import org.mapsforge.preprocessing.graph.osm2rg.osmxml.OsmElement;
import org.mapsforge.preprocessing.graph.osm2rg.osmxml.OsmNode;
import org.mapsforge.preprocessing.model.EHighwayLevel;

public class OsmWay_withNodes extends OsmElement {

	private static boolean DEFAULT_TAG_VALUE_ONEWAY = false;
	private static boolean DEFAULT_TAG_VALUE_URBAN = false;
	private static String DEFAULT_TAG_VALUE_NAME = "";
	private static EHighwayLevel DEFAULT_TAG_VALUE_HIGHWAY_LEVEL = null;
	private static int DEFAULT_TAG_VALUE_MAX_SPEED = 50;

	private LinkedList<OsmNode> nodes;

	public OsmWay_withNodes(long id) {
		super(id);
		this.setNodes(new LinkedList<OsmNode>());
	}

	public OsmWay_withNodes(long id, Timestamp timestamp, String user, boolean visible) {
		super(id, timestamp, user, visible);
		this.nodes = new LinkedList<OsmNode>();
	}

	public OsmWay_withNodes(long id, LinkedList<OsmNode> nodes) {
		super(id);
		this.setNodes(nodes);
	}

	public void addNode(OsmNode node) {
		if (node != null && !nodes.contains(node)) {
			nodes.add(node);
		}
	}

	public void setNodes(LinkedList<OsmNode> nodes) {
		this.nodes = nodes;
	}

	public LinkedList<OsmNode> getNodes() {
		return nodes;
	}

	public int getMaxSpeed() {
		// TODO alle m�glichen maxspeeds hinzuf�gen
		return DEFAULT_TAG_VALUE_MAX_SPEED;
	}

	public boolean isOneway() {
		EHighwayLevel hwyLevel = getHighwayLevel();
		if (hwyLevel == EHighwayLevel.motorway || hwyLevel == EHighwayLevel.motorway_link
				|| hwyLevel == EHighwayLevel.trunk || hwyLevel == EHighwayLevel.trunk_link) {
			return true;
		}
		String tag = getTag("oneway");
		if (tag == null) {
			return DEFAULT_TAG_VALUE_ONEWAY;
		} else if (tag.equals("true") || tag.equals("yes") || tag.equals("t")
				|| tag.equals("1")) {
			return true;
		} else if (tag.equals("false") || tag.equals("no") || tag.equals("f")
				|| tag.equals("0")) {
			return false;
		} else if (tag.equals("-1")) {
			return false;
		} else {
			return DEFAULT_TAG_VALUE_ONEWAY;
		}
	}

	public EHighwayLevel getHighwayLevel() {
		String v = getTag("highway");
		if (v != null) {
			try {
				return EHighwayLevel.valueOf(v);
			} catch (IllegalArgumentException e) {

			}
		}
		return DEFAULT_TAG_VALUE_HIGHWAY_LEVEL;
	}

	public boolean isUrban() {
		return DEFAULT_TAG_VALUE_URBAN;
	}

	public String getName() {
		String v = getTag("name");
		if (v == null) {
			return DEFAULT_TAG_VALUE_NAME;
		}
		return v;
	}

}
