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
package org.mapsforge.preprocessing.graph.interpreter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.mapsforge.preprocessing.graph.model.gui.DatabaseProperties;
import org.mapsforge.preprocessing.graph.model.gui.Profile;
import org.mapsforge.preprocessing.graph.model.gui.Transport;
import org.mapsforge.preprocessing.model.EHighwayLevel;
import org.mapsforge.preprocessing.util.HighwayLevelExtractor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class deserialized the profile object of the configuration file. This would happened by
 * using a SAX parser implementation. The profile object and his attributes can get bye getter
 * methods.
 * 
 * @author kunis
 * 
 */
public class SimpleRoutingConfigurationParser extends DefaultHandler {

	private Profile profile;
	private Transport transport;
	private DatabaseProperties dbprops;
	private String osmUrl;

	private String currentObject, characters;
	String transportName, profileName, key, value, username, password, dbname, host;
	int maxSpeed, port;
	HashSet<EHighwayLevel> highways = new HashSet<EHighwayLevel>();

	/**
	 * Constructor to create a parser to get the routing configurations of the configuration
	 * file.
	 * 
	 * @param is
	 *            input stream for parsing
	 */
	public SimpleRoutingConfigurationParser(InputStream is) {

		createParser(is);
	}

	private void createParser(InputStream is) {
		DefaultHandler saxParser = this;

		// get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {

			// get a new instance of parser
			SAXParser sp = spf.newSAXParser();

			// parse the file and also register this class for call backs
			sp.parse(is, saxParser);

		} catch (SAXException se) {
			se.printStackTrace();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		currentObject = null;
		transportName = profileName = key = value = username = password = dbname = host = "";
		maxSpeed = port = -1;
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {

		if (qName.toLowerCase().equals("profile") || qName.toLowerCase().equals("transport")) {
			currentObject = qName.toLowerCase();
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		EHighwayLevel currenHwyLvl = null;

		if (qName.toLowerCase().equals("profile")) {
			if (profileName == "" || transport == null || dbprops == null) {
				System.err.println("This profil has some illegal values.");
				System.exit(-1);
			} else {
				profile = new Profile(profileName, null, transport, null, dbprops);
			}
			currentObject = null;
		} else if (qName.toLowerCase().equals("transport")) {
			if (transportName == "" || maxSpeed <= 0) {
				System.err.println("There are missing some arguments. Can't create transport.");
				System.exit(-1);
			} else {
				transport = new Transport(transportName, maxSpeed, highways);
			}
			currentObject = "profile";

		} else if (qName.toLowerCase().equals("dbprops")) {
			if (host == "" || dbname == "" || username == "" || password == "" || port < 0) {
				System.err
						.println("Can't create a database connection, because the values are invalid.");
				System.exit(-1);
			} else {
				dbprops = new DatabaseProperties(host, port, dbname, username, password);
			}

		} else if (qName.toLowerCase().equals("name")) {
			if (currentObject.equals("profile")) {
				profileName = characters;
			} else if (currentObject.equals("transport")) {
				transportName = characters;
			}
		} else if (qName.toLowerCase().equals("highway")) {

			currenHwyLvl = HighwayLevelExtractor.getLevel(characters);
			if (currenHwyLvl != null)
				highways.add(currenHwyLvl);
		} else if (qName.toLowerCase().equals("maxspeed")) {
			maxSpeed = Integer.parseInt(characters);
		} else if (qName.toLowerCase().equals("key")) {
			key = characters;
		} else if (qName.toLowerCase().equals("value")) {
			value = characters;
		} else if (qName.toLowerCase().equals("host")) {
			host = characters;
		} else if (qName.toLowerCase().equals("username")) {
			username = characters;
		} else if (qName.toLowerCase().equals("password")) {
			password = characters;
		} else if (qName.toLowerCase().equals("dbname")) {
			dbname = characters;
		} else if (qName.toLowerCase().equals("port")) {
			port = Integer.parseInt(characters);
		}

	}

	@Override
	public void characters(char ch[], int start, int length) {
		characters = "";

		for (int i = start; i < start + length; i++) {
			switch (ch[i]) {
				case '\\':

					break;
				case '"':

					break;
				case '\n':

					break;
				case '\r':

					break;
				case '\t':

					break;
				default:
					characters += ch[i];
					break;
			}
		}
	}

	/**
	 * Returns the Transport that would be parsed.
	 * 
	 * @return the Transport object.
	 */
	public Transport getTransport() {
		return transport;
	}

	/**
	 * Returns the URL to the OSM file.
	 * 
	 * @return the URL to the OSM file
	 */
	public String getOsmUrl() {
		return osmUrl;
	}

	/**
	 * Returns the Profile that would be parsed.
	 * 
	 * @return the Profile object.
	 */
	public Profile getProfile() {
		return profile;
	}

	/**
	 * Returns the database properties that would be parsed.
	 * 
	 * @return the database properties.
	 */
	public DatabaseProperties getDbprops() {
		return dbprops;
	}

	/**
	 * Sets the Transport object.
	 * 
	 * @param transport
	 *            the transport object, that should be set.
	 */
	public void setTransport(Transport transport) {
		this.transport = transport;
	}

	/**
	 * Sets the osm URL.
	 * 
	 * @param osmUrl
	 *            the URL, that should be set.
	 */
	public void setOsmUrl(String osmUrl) {
		this.osmUrl = osmUrl;
	}

	/**
	 * Sets the Profile.
	 * 
	 * @param profile
	 *            the URL, that should be set.
	 */
	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	/**
	 * Sets the database properties.
	 * 
	 * @param dbprops
	 *            the database properties, that should be set.
	 */
	public void setDbprops(DatabaseProperties dbprops) {
		this.dbprops = dbprops;
	}

	// public static void main(String[] args) {

	/*
	 * File file = new File("U:\\berlin.osm\\testprofil.profil"); if (!file.exists() ||
	 * !file.isFile() || !file.canRead()) {
	 * System.out.println("Can not read file. Maybe istn't one."); System.exit(-1); }
	 * SimpleRoutingConfigurationParser parser = new SimpleRoutingConfigurationParser(file);
	 * System.out.println("profil name: " + parser.profil.getName());
	 * System.out.println("transport name: " + parser.profil.getTransport().getName());
	 * System.out.println("transport ways: " +
	 * parser.profil.getTransport().getUseableWaysSerialized());
	 */
	// }

}
