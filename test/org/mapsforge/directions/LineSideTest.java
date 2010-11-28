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

import org.junit.Test;
import org.mapsforge.core.GeoCoordinate;

/**
 * Testing the methods which check if a {@link GeoCoordinate} is in an arbitrary rectangle
 * 
 * @author Eike
 */
public class LineSideTest {

	/**
	 * The easy test
	 */
	@Test
	public void testUpsideRect() {
		// These Points form a square
		GeoCoordinate[] coords = new GeoCoordinate[4];
		coords[0] = new GeoCoordinate(52.5290001, 13.40); // links oben
		coords[1] = new GeoCoordinate(52.529, 13.410001); // rechts oben
		coords[2] = new GeoCoordinate(52.5250001, 13.41); // rechts unten
		coords[3] = new GeoCoordinate(52.525, 13.400001); // links unten

		// should be in the square
		GeoCoordinate t1 = new GeoCoordinate(52.527, 13.405);

		// should not be in the square
		GeoCoordinate f1 = new GeoCoordinate(52.52793, 13.41739);
		GeoCoordinate f2 = new GeoCoordinate(52.52278, 13.40649);
		GeoCoordinate f3 = new GeoCoordinate(52.52566, 13.38602);
		GeoCoordinate f4 = new GeoCoordinate(52.53529, 13.40743);
		assertTrue(LandmarksFromPerst.isInsideRectangle(coords, t1));
		assertFalse(LandmarksFromPerst.isInsideRectangle(coords, f1));
		assertFalse(LandmarksFromPerst.isInsideRectangle(coords, f2));
		assertFalse(LandmarksFromPerst.isInsideRectangle(coords, f3));
		assertFalse(LandmarksFromPerst.isInsideRectangle(coords, f4));
	}

	/**
	 * includes the test for division by zero
	 */
	@Test
	public void testUpsideRectZero() {
		// These Points form a square
		GeoCoordinate[] coords = new GeoCoordinate[4];
		coords[0] = new GeoCoordinate(52.529, 13.40); // links oben
		coords[1] = new GeoCoordinate(52.529, 13.41); // rechts oben
		coords[2] = new GeoCoordinate(52.525, 13.41); // rechts unten
		coords[3] = new GeoCoordinate(52.525, 13.40); // links unten

		// should be in the square
		GeoCoordinate t1 = new GeoCoordinate(52.527, 13.405);
		// should not be in the square
		GeoCoordinate f1 = new GeoCoordinate(52.52793, 13.41739);
		GeoCoordinate f2 = new GeoCoordinate(52.52278, 13.40649);
		GeoCoordinate f3 = new GeoCoordinate(52.52566, 13.38602);
		GeoCoordinate f4 = new GeoCoordinate(52.53529, 13.40743);
		assertTrue(LandmarksFromPerst.isInsideRectangle(coords, t1));
		assertFalse(LandmarksFromPerst.isInsideRectangle(coords, f1));
		assertFalse(LandmarksFromPerst.isInsideRectangle(coords, f2));
		assertFalse(LandmarksFromPerst.isInsideRectangle(coords, f3));
		assertFalse(LandmarksFromPerst.isInsideRectangle(coords, f4));
	}

	/**
	 * test in a tilted rectangle
	 */
	@Test
	public void testTiltedRect() {
		// These Points form roughly tilted a rectangle
		GeoCoordinate[] coords = new GeoCoordinate[4];
		coords[0] = new GeoCoordinate(52.52004, 13.40484); // links
		coords[1] = new GeoCoordinate(52.52381, 13.41194); // oben
		coords[2] = new GeoCoordinate(52.52187, 13.41658); // rechts
		coords[3] = new GeoCoordinate(52.51744, 13.40847); // unten

		// should be in the square
		GeoCoordinate t1 = new GeoCoordinate(52.52108, 13.41012);
		GeoCoordinate t2 = new GeoCoordinate(52.51851, 13.40877);
		GeoCoordinate t3 = new GeoCoordinate(52.51974, 13.40681);
		GeoCoordinate t4 = new GeoCoordinate(52.52291, 13.41190);

		// should not be in the square (way outside)
		GeoCoordinate f1 = new GeoCoordinate(52.51333, 13.40887);
		GeoCoordinate f2 = new GeoCoordinate(52.51972, 13.39581);
		GeoCoordinate f3 = new GeoCoordinate(52.52773, 13.41057);
		GeoCoordinate f4 = new GeoCoordinate(52.52176, 13.42563);

		// should not be in the square (inside bounding box)
		GeoCoordinate f5 = new GeoCoordinate(52.51881, 13.41452);
		GeoCoordinate f6 = new GeoCoordinate(52.51811, 13.40591);
		GeoCoordinate f7 = new GeoCoordinate(52.52230, 13.40752);
		GeoCoordinate f8 = new GeoCoordinate(52.52317, 13.41512);

		assertTrue(LandmarksFromPerst.isInsideRectangle(coords, t1));
		assertTrue(LandmarksFromPerst.isInsideRectangle(coords, t2));
		assertTrue(LandmarksFromPerst.isInsideRectangle(coords, t3));
		assertTrue(LandmarksFromPerst.isInsideRectangle(coords, t4));
		assertFalse(LandmarksFromPerst.isInsideRectangle(coords, f1));
		assertFalse(LandmarksFromPerst.isInsideRectangle(coords, f2));
		assertFalse(LandmarksFromPerst.isInsideRectangle(coords, f3));
		assertFalse(LandmarksFromPerst.isInsideRectangle(coords, f4));
		assertFalse(LandmarksFromPerst.isInsideRectangle(coords, f5));
		assertFalse(LandmarksFromPerst.isInsideRectangle(coords, f6));
		assertFalse(LandmarksFromPerst.isInsideRectangle(coords, f7));
		assertFalse(LandmarksFromPerst.isInsideRectangle(coords, f8));
	}

}
