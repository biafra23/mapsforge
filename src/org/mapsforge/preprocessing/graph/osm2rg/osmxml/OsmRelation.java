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
 * 
 */
public class OsmRelation extends OsmElement {

	private final LinkedList<Member> wayRefs;

	OsmRelation(long id) {
		super(id);
		this.wayRefs = new LinkedList<Member>();
	}

	OsmRelation(long id, Timestamp timestamp, String user, boolean visible) {
		super(id, timestamp, user, visible);
		this.wayRefs = new LinkedList<Member>();
	}

	void addMember(Member ref) {
		wayRefs.add(ref);
	}

	/**
	 * @return returns all members of this relation.
	 */
	public LinkedList<Member> getMembers() {
		LinkedList<Member> tmp = new LinkedList<Member>();
		tmp.addAll(wayRefs);
		return tmp;
	}

	/**
	 * This class represents a reference from a relation to another osm element like a node or a
	 * way.
	 */
	public static class Member {
		/** Member type way reference. */
		public final static String TYPE_WAY = "way";
		/** Role type outer. */
		public final static String ROLE_OUTER = "outer";
		/** Role type inner. */
		public final static String ROLE_INNER = "innter";
		/** Role type forward. */
		public final static String ROLE_FORWARD = "forward";
		/** Role type backward. */
		public final static String ROLE_BACKWARD = "backward";

		/** type of the referenced element */
		public final String type;
		/** role type */
		public final String role;
		/** id of the referenced element. */
		public final long refId;

		/**
		 * @param type
		 *            type of the referenced element.
		 * @param role
		 *            role type.
		 * @param refId
		 *            id of the referenced element.
		 */
		public Member(String type, String role, long refId) {
			this.type = type;
			this.role = role;
			this.refId = refId;
		}
	}
}
