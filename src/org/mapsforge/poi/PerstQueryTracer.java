package org.mapsforge.poi;
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


/**
 * Implements singleton pattern. Use {@link #getInstance()} method to retrieve instance. Used in
 * patched perst version to count page loads and nodes touched as well as time elapsed during a
 * POI query.
 * 
 * @author weise
 * 
 */
/**
 * @author weise
 * 
 */
public class PerstQueryTracer {

	private static PerstQueryTracer instance;

	/**
	 * @return instance of {@link PerstQueryTracer}
	 */
	public static synchronized PerstQueryTracer getInstance() {
		if (instance == null) {
			instance = new PerstQueryTracer();
		}
		return instance;
	}

	private int pages = 0;
	private int nodes = 0;
	private long start = 0;
	private long stop = 0;

	/**
	 * Increments count for number of pages loaded.
	 */
	public void incrementPages() {
		pages++;
	}

	/**
	 * Increments count for number of nodes touched.
	 */
	public void incrementNodes() {
		nodes++;
	}

	/**
	 * Resets all count values to 0 and start timer.
	 */
	public void start() {
		pages = 0;
		nodes = 0;
		stop = 0;
		start = System.currentTimeMillis();
	}

	/**
	 * Stops timer.
	 */
	public void stop() {
		stop = System.currentTimeMillis();
	}

	/**
	 * @return number of nodes touched between calls of {@link #start()} and {@link #stop()}.
	 */
	public int nodesTouched() {
		return nodes;
	}

	/**
	 * @return number of pages loaded between calls of {@link #start()} and {@link #stop()}.
	 */
	public int pagesLoaded() {
		return pages;
	}

	/**
	 * @return number of non node pages loaded between calls of {@link #start()} and
	 *         {@link #stop()}.
	 */
	public int noneNodePagesLoaded() {
		return pages - nodes;
	}

	/**
	 * @return time in ms elapsed between calls of {@link #start()} and {@link #stop()}.
	 */
	public long queryTime() {
		return stop - start;
	}
}
