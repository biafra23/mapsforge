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
package org.mapsforge.android.maps;

/**
 * An immutable container class which is the key for the index cache.
 */
class IndexCacheEntryKey {
	private final int hashCode;
	private final long indexBlockNumber;
	private final MapFileParameters mapFileParameters;
	private IndexCacheEntryKey other;

	/**
	 * Creates an immutable key to be stored in a map.
	 * 
	 * @param mapFileParameters
	 *            the parameters of the map file.
	 * @param indexBlockNumber
	 *            the number of the index block.
	 */
	IndexCacheEntryKey(MapFileParameters mapFileParameters, long indexBlockNumber) {
		this.mapFileParameters = mapFileParameters;
		this.indexBlockNumber = indexBlockNumber;
		this.hashCode = calculateHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof IndexCacheEntryKey)) {
			return false;
		} else {
			this.other = (IndexCacheEntryKey) obj;
			if (this.mapFileParameters == null && this.other.mapFileParameters != null) {
				return false;
			} else if (this.mapFileParameters != null
					&& !this.mapFileParameters.equals(this.other.mapFileParameters)) {
				return false;
			} else if (this.indexBlockNumber != this.other.indexBlockNumber) {
				return false;
			}
			return true;
		}
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	/**
	 * Calculates the hash value of this object.
	 * 
	 * @return the hash value of this object.
	 */
	private int calculateHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.mapFileParameters == null) ? 0 : this.mapFileParameters.hashCode());
		result = prime * result
				+ (int) (this.indexBlockNumber ^ (this.indexBlockNumber >>> 32));
		return result;
	}
}