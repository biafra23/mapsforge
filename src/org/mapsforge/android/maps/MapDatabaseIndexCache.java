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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A cache for database index blocks with a fixed size and LRU policy.
 */
class MapDatabaseIndexCache {
	/**
	 * Bitmask to extract the block address from an index entry.
	 */
	private static final long BITMASK_INDEX_ADDRESS = 0x7FFFFFFFFFFFFFFFl;

	/**
	 * Number of bytes a single index entry consists of.
	 */
	private static final byte BYTES_PER_INDEX_ENTRY = 5;

	/**
	 * Number of index entries that one index block consists of.
	 */
	private static final int INDEX_ENTRIES_PER_CACHE_BLOCK = 128;

	/**
	 * Load factor of the internal HashMap.
	 */
	private static final float LOAD_FACTOR = 0.6f;

	/**
	 * Real size in bytes of one index block.
	 */
	private static final int SIZE_OF_INDEX_BLOCK = INDEX_ENTRIES_PER_CACHE_BLOCK
			* BYTES_PER_INDEX_ENTRY;

	private int addressInIndexBlock;
	private byte[] indexBlock;
	private long indexBlockNumber;
	private IndexCacheEntryKey indexCacheEntryKey;
	private RandomAccessFile inputFile;
	private LinkedHashMap<IndexCacheEntryKey, byte[]> map;

	/**
	 * Constructs an database index cache with a fixes size and LRU policy.
	 * 
	 * @param inputFile
	 *            the map file from which the index should be read and cached.
	 * @param capacity
	 *            the maximum number of entries in the cache.
	 * @throws IllegalArgumentException
	 *             if the capacity is negative.
	 */
	MapDatabaseIndexCache(RandomAccessFile inputFile, int capacity) {
		if (capacity < 0) {
			throw new IllegalArgumentException();
		}
		this.inputFile = inputFile;
		this.map = createMap(capacity);
	}

	private LinkedHashMap<IndexCacheEntryKey, byte[]> createMap(final int initialCapacity) {
		return new LinkedHashMap<IndexCacheEntryKey, byte[]>(
				(int) (initialCapacity / LOAD_FACTOR) + 2, LOAD_FACTOR, true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<IndexCacheEntryKey, byte[]> eldest) {
				return size() > initialCapacity;
			}
		};
	}

	/**
	 * Destroy the cache at the end of its lifetime.
	 */
	void destroy() {
		this.inputFile = null;
		if (this.map != null) {
			this.map.clear();
			this.map = null;
		}
	}

	/**
	 * Returns the real address of a block in the given map file. If the required block address
	 * is not cached, it will be read from the correct map file index and put in the cache.
	 * 
	 * @param mapFileParameters
	 *            the parameters of the map file for which the address is needed.
	 * @param blockNumber
	 *            the number of the block in the map file.
	 * @return the block address or -1 if the block number is invalid.
	 */
	long getAddress(MapFileParameters mapFileParameters, long blockNumber) {
		try {
			// check if the block number is out of bounds
			if (blockNumber >= mapFileParameters.numberOfBlocks) {
				return -1;
			}

			// calculate the index block number
			this.indexBlockNumber = blockNumber / INDEX_ENTRIES_PER_CACHE_BLOCK;

			// create the cache entry key for this request
			this.indexCacheEntryKey = new IndexCacheEntryKey(mapFileParameters,
					this.indexBlockNumber);

			// check for cached index block
			this.indexBlock = this.map.get(this.indexCacheEntryKey);
			if (this.indexBlock == null) {
				// cache miss, create a new index block
				this.indexBlock = new byte[SIZE_OF_INDEX_BLOCK];

				// seek to the correct index block in the file and read it
				this.inputFile.seek(mapFileParameters.indexStartAddress + this.indexBlockNumber
						* SIZE_OF_INDEX_BLOCK);
				if (this.inputFile.read(this.indexBlock, 0, SIZE_OF_INDEX_BLOCK) != SIZE_OF_INDEX_BLOCK) {
					Logger.d("reading the current index block has failed");
					return -1;
				}

				// put the index block in the map
				this.map.put(this.indexCacheEntryKey, this.indexBlock);
			}

			// calculate the address of the index entry inside the index block
			this.addressInIndexBlock = (int) ((blockNumber % INDEX_ENTRIES_PER_CACHE_BLOCK) * BYTES_PER_INDEX_ENTRY);

			// return the real block address
			return Deserializer.fiveBytesToLong(this.indexBlock, this.addressInIndexBlock)
					& BITMASK_INDEX_ADDRESS;
		} catch (IOException e) {
			Logger.e(e);
			return -1;
		}
	}
}