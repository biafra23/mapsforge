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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mapsforge.core.GeoCoordinate;

/**
 * Test TurnByTurnDescription functionality
 * 
 * @author Eike
 */
public class TurnByTurnDescriptionTest {
	TurnByTurnDescription twoStreets;
	TurnByTurnDescription uTurnCase0;
	TurnByTurnDescription uTurnCase1;
	TurnByTurnDescription shortStreet2LaneCase0;
	TurnByTurnDescription shortStreet2LaneCase1;

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {

		twoStreets = new TurnByTurnDescription(new DummyEdge[] {
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(53.08997, 8.78746),
						new GeoCoordinate(53.088898, 8.785359),
						new GeoCoordinate(53.088842, 8.785249) }, "Hansestraße"),
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(53.088842, 8.785249),
						new GeoCoordinate(53.088775, 8.785105),
						new GeoCoordinate(53.088752, 8.785057),
						new GeoCoordinate(53.088692, 8.784977),
						new GeoCoordinate(53.088614, 8.784839),
						new GeoCoordinate(53.087088, 8.782624) }, "Hansator") });
		uTurnCase0 = new TurnByTurnDescription(new DummyEdge[] {
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(0, 0),
						new GeoCoordinate(0, 5E-3) }, "some street"),
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(0, 5E-3),
						new GeoCoordinate(0, 8E-3) }, "some other street"),
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(0, 8E-3),
						new GeoCoordinate(5E-5, 8E-3) }, "some other street"),
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(5E-5, 8E-3),
						new GeoCoordinate(5E-5, 5E-3) }, "some other street"),
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(5E-5, 5E-3),
						new GeoCoordinate(5E-5, 0) }, "another street") });
		uTurnCase1 = new TurnByTurnDescription(new DummyEdge[] {
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(0, 0),
						new GeoCoordinate(0, 5E-3) }, "some street"),
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(0, 5E-3),
						new GeoCoordinate(0, 8E-3) }, "some other street"),
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(0, 8E-3),
						new GeoCoordinate(5E-5, 8E-3) }, "short in between street"),
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(5E-5, 8E-3),
						new GeoCoordinate(5E-5, 5E-3) }, "some other street"),
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(5E-5, 5E-3),
						new GeoCoordinate(5E-5, 0) }, "another street") });
		shortStreet2LaneCase0 = new TurnByTurnDescription(new DummyEdge[] {
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(0, 0),
						new GeoCoordinate(0, 5E-3) }, "some street"),
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(0, 5E-3),
						new GeoCoordinate(0, 8E-3) }, "some other street"),
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(0, 8E-3),
						new GeoCoordinate(5E-5, 8E-3) }, "short in between street"),
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(5E-5, 8E-3),
						new GeoCoordinate(8E-3, 8E-3) }, "another street") });
		shortStreet2LaneCase1 = new TurnByTurnDescription(new DummyEdge[] {
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(0, 0),
						new GeoCoordinate(0, 5E-3) }, "some street"),
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(0, 5E-3),
						new GeoCoordinate(0, 8E-3) }, "some other street"),
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(0, 8E-3),
						new GeoCoordinate(0, 8.01E-3) }, "short in between street"),
				new DummyEdge(new GeoCoordinate[] { new GeoCoordinate(0, 8.01E-3),
						new GeoCoordinate(0, 8E-3) }, "another street") });
	}

	/**
	 * 
	 */
	@Test
	public void testTurnByTurnDescription() {
		assertTrue(twoStreets.streets.size() == 2);
		assertTrue(uTurnCase0.streets.size() == 4);
		assertTrue(uTurnCase0.streets.get(2).angleFromStreetLastStreet == 180d);
		assertFalse(uTurnCase1.toString().contains("between"));
		assertFalse(shortStreet2LaneCase0.toString().contains("between"));
	}
}
