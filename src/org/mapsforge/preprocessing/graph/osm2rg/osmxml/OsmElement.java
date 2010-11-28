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
import java.util.HashMap;

/**
 * 
 * Subclass for all available osm xml elements. Tag parsing can be done by additional subclass
 * methods.
 */
public class OsmElement {

	private final long id;
	private final Timestamp timestamp;
	private final String user;
	private final boolean visible;
	private final HashMap<String, String> tags;

	/**
	 * @param id
	 *            the osm id of this element.
	 */
	public OsmElement(long id) {
		this.id = id;
		this.timestamp = null;
		this.user = "";
		this.visible = true;
		this.tags = new HashMap<String, String>();
	}

	/**
	 * Construct an OsmElement by the given osm attributes.
	 * 
	 * @param id
	 *            the osm id.
	 * @param timestamp
	 *            time of last modification.
	 * @param user
	 *            user of last modification.
	 * @param visible
	 *            the visibility.
	 */
	public OsmElement(long id, Timestamp timestamp, String user, boolean visible) {
		this.id = id;
		this.timestamp = timestamp;
		this.user = user;
		this.visible = visible;
		this.tags = new HashMap<String, String>();
	}

	/**
	 * @return returns the osm id of this element.
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return returns the timestamp of last modification.
	 */
	public Timestamp getTimestamp() {
		return timestamp;
	}

	/**
	 * @return returns the user of last modification.
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @return returns the visibility.
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Sets a tag to the given value.
	 * 
	 * @param key
	 *            tag name.
	 * @param value
	 *            tag value.
	 */
	public void setTag(String key, String value) {
		if (key != null && value != null) {
			this.tags.put(key, value);
		}
	}

	/**
	 * Adds a tag to this element.
	 * 
	 * @param key
	 *            tag name.
	 * @param value
	 *            tag value.
	 */
	public void addTag(String key, String value) {
		if (key != null && value != null) {
			this.tags.put(key, value);
		}
	}

	/**
	 * @param key
	 *            tag name.
	 * @return returns the value of the given tag.
	 */
	public String getTag(String key) {
		return tags.get(key);
	}

	/**
	 * @return returns all tag names and associated values.
	 */
	public HashMap<String, String> getTags() {
		// TODO : defensive programming?!
		return tags;
	}

	/**
	 * Parses a String value to and returns a boolean.
	 * 
	 * @param tag
	 *            the tag value.
	 * @param defaultValue
	 *            the default value.
	 * @return returns true if the string represents the boolean value true, else the default
	 *         value is returned.
	 */
	protected static boolean parseBooleanTag(String tag, boolean defaultValue) {
		if (tag == null)
			return defaultValue;
		if (defaultValue) {
			return !(tag.equals("false") || tag.equals("no") || tag.equals("f") || tag
					.equals("0"));
		}
		return (tag.equals("true") || tag.equals("yes") || tag.equals("t") || tag.equals("1"));

	}
}
