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
package org.mapsforge.preprocessing.map.osmosis;

/**
 * Represents the configuration of zoom intervals. A zoom interval is defined by a base zoom
 * level, a minimum zoom level and a maximum zoom level.
 * 
 * @author bross
 * 
 */
class ZoomIntervalConfiguration {

	private byte[] baseZoom;
	private byte[] minZoom;
	private byte[] maxZoom;

	private byte minMinZoom;
	private byte maxMaxZoom;

	private ZoomIntervalConfiguration(byte[][] intervals) {
		baseZoom = new byte[intervals.length];
		minZoom = new byte[intervals.length];
		maxZoom = new byte[intervals.length];

		int i = 0;
		for (byte[] interval : intervals) {
			i++;
			if (interval.length != 3)
				throw new IllegalArgumentException(
						"invalid interval configuration, found only " + interval.length
								+ "parameters for interval " + i);
			if (interval[0] <= interval[1] || interval[0] >= interval[2])
				throw new IllegalArgumentException("invalid configuration for interval " + i +
						", make sure that minZoom < baseZoom < maxZoom");
			if (i > 1) {
				if (interval[0] < baseZoom[i - 2])
					throw new IllegalArgumentException(
							"interval configurations must follow an increasing order");
				if (interval[1] != ((maxZoom[i - 2]) + 1))
					throw new IllegalArgumentException("minZoom of interval " + i
							+ " not adjacent to maxZoom of interval " + (i - 1));
			}
			baseZoom[i - 1] = interval[0];
			minZoom[i - 1] = interval[1];
			maxZoom[i - 1] = interval[2];
		}
		minMinZoom = minZoom[0];
		maxMaxZoom = maxZoom[maxZoom.length - 1];
	}

	static ZoomIntervalConfiguration getStandardConfiguration() {
		return new ZoomIntervalConfiguration(new byte[][] {
				new byte[] { 8, 6, 11 },
				new byte[] { 14, 12, 17 }
		});
	}

	static ZoomIntervalConfiguration newInstance(byte[]... intervals) {
		return new ZoomIntervalConfiguration(intervals);
	}

	static ZoomIntervalConfiguration fromString(String confString) {
		String[] splitted = confString.split(",");
		if (splitted.length % 3 != 0) {
			throw new IllegalArgumentException(
					"invalid zoom interval configuration, amount of comma-separated values must be a multiple of 3");
		}
		byte[][] intervals = new byte[splitted.length / 3][3];
		for (int i = 0; i < intervals.length; i++) {
			intervals[i][0] = Byte.parseByte(splitted[i * 3]);
			intervals[i][1] = Byte.parseByte(splitted[i * 3 + 1]);
			intervals[i][2] = Byte.parseByte(splitted[i * 3 + 2]);
		}

		return ZoomIntervalConfiguration.newInstance(intervals);
	}

	int getNumberOfZoomIntervals() {
		return baseZoom.length;
	}

	byte[] getBaseZoomValues() {
		return baseZoom;
	}

	byte getBaseZoom(int i) {
		return baseZoom[i];
	}

	byte getMinZoom(int i) {
		return minZoom[i];
	}

	byte getMaxZoom(int i) {
		return maxZoom[i];
	}

	byte getMinMinZoom() {
		return minMinZoom;
	}

	byte getMaxMaxZoom() {
		return maxMaxZoom;
	}

}
