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
package org.mapsforge.android.routing.blockedHighwayHierarchies;

/**
 *
 */
final class Block implements CacheItem {
	/**
	 * Bit offset to the first vertex of this block.
	 */
	private static final short FIRST_VERTEX_OFFSET = 25 * 8;
	/**
	 * the id of this block
	 */
	private final int blockId;
	/**
	 * The data of this block exactly like it is stored within the file.
	 */
	private final byte[] data;
	/**
	 * the routing graph this block belongs to.
	 */
	private final HHRoutingGraph routingGraph;
	/**
	 * the level of the multileveled graph this block belongs to.
	 */
	private final byte level;
	/**
	 * Number of vertices of typeA. These vertices have neighborhood and also belong to the next
	 * level.
	 */
	private final short numVerticesTypeA;
	/**
	 * Number of vertices of typeB. These vertices have neighborhood but do not belong to the
	 * next level.
	 */
	private final short numVerticesTypeB;
	/**
	 * number of vertices if tyoeC. The vertices have their neighborhood set to infinity and
	 * thus do not belong to the next level.
	 */
	private final short numVerticesTypeC;
	/**
	 * the minimum latitude in micro degrees of all coordinates stored within this block, way
	 * point coordinate and vertex coordinates.
	 */
	private final int minLatitudeE6;
	/**
	 * the minimum longitude in micro degrees of all coordinates stored within this block, way
	 * point coordinate and vertex coordinates.
	 */
	private final int minLongitudeE6;
	/**
	 * Number of bits for encoding a latitude or a longitude.
	 */
	private final byte bitsPerCoordinate;
	/**
	 * number of bits for encoding the neighborhood of the vertices.
	 */
	private final byte bitsPerNeighborhood;

	private final byte bitsPerStreetNameOffset;
	private final int offsetStreetNames;
	private final int offsetReferencedBlocks;
	private final byte bitsPerIndirectBlockRef;

	/**
	 * Number of bits for encoding a vertex of type A.
	 */
	private final int bitsPerVertexTypeA;
	/**
	 * Number of bits for encoding a vertex of type B.
	 */
	private final int bitsPerVertexTypeB;
	/**
	 * Number of bits for encoding a vertex of type C.
	 */
	private final int bitsPerVertexTypeC;

	/**
	 * Constructs a block with the given id from the serialized representation.
	 * 
	 * @param blockId
	 *            the id of this block.
	 * @param data
	 *            the serialized representation of this block.
	 * @param routingGraph
	 *            the routing graph this block belongs to.
	 */
	public Block(int blockId, byte[] data, HHRoutingGraph routingGraph) {
		this.blockId = blockId;
		this.data = data;
		this.routingGraph = routingGraph;

		int bitOffset = 0;
		this.level = Deserializer.readByte(data, bitOffset / 8, bitOffset % 8);
		bitOffset += 8;
		this.numVerticesTypeA = Deserializer.readShort(data, bitOffset / 8, bitOffset % 8);
		bitOffset += 16;
		this.numVerticesTypeB = Deserializer.readShort(data, bitOffset / 8, bitOffset % 8);
		bitOffset += 16;
		this.numVerticesTypeC = Deserializer.readShort(data, bitOffset / 8, bitOffset % 8);
		bitOffset += 16;
		this.minLatitudeE6 = Deserializer.readInt(data, bitOffset / 8, bitOffset % 8);
		bitOffset += 32;
		this.minLongitudeE6 = Deserializer.readInt(data, bitOffset / 8, bitOffset % 8);
		bitOffset += 32;
		this.bitsPerCoordinate = Deserializer.readByte(data, bitOffset / 8,
				bitOffset % 8);
		bitOffset += 8;
		this.bitsPerNeighborhood = Deserializer.readByte(data, bitOffset / 8,
				bitOffset % 8);
		bitOffset += 8;
		this.bitsPerStreetNameOffset = Deserializer.readByte(data, bitOffset / 8,
				bitOffset % 8);
		bitOffset += 8;
		this.bitsPerIndirectBlockRef = Deserializer.readByte(data, bitOffset / 8,
				bitOffset % 8);
		bitOffset += 8;
		this.offsetStreetNames = (int) Deserializer.readUInt(data, 24, bitOffset / 8,
				bitOffset % 8);
		bitOffset += 24;
		this.offsetReferencedBlocks = (int) Deserializer.readUInt(data, 24, bitOffset / 8,
				bitOffset % 8);
		bitOffset += 24;

		this.bitsPerVertexTypeC = ((bitsPerIndirectBlockRef + routingGraph.bitsPerVertexOffset) * level)
				+ 32 + (level == 0 ? (2 * bitsPerCoordinate) : 0);
		this.bitsPerVertexTypeB = bitsPerVertexTypeC + bitsPerNeighborhood;
		this.bitsPerVertexTypeA = bitsPerVertexTypeB + bitsPerIndirectBlockRef
				+ routingGraph.bitsPerVertexOffset;
	}

	@Override
	public int getId() {
		return blockId;
	}

	@Override
	public int getSizeBytes() {
		return 36 + data.length;
	}

	/**
	 * @return Returns the level within the multileveled graph this block belongs to.
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @return Returns the number of vertices stored within this block.
	 */
	public int getNumVertices() {
		return numVerticesTypeA + numVerticesTypeB + numVerticesTypeC;
	}

	/**
	 * Look up the vertex stored at given offset.
	 * 
	 * @param vertexOffset
	 *            the offset of the vertex.
	 * @return the desired vertex.
	 */
	public HHVertex getVertex(int vertexOffset) {
		if (vertexOffset >= getNumVertices()) {
			return null;
		}

		// here, the first vertex of this block is stored.
		int bitOffset = FIRST_VERTEX_OFFSET;

		// recycle a vertex from the pool.
		HHVertex vertex = routingGraph.vertexPool.borrow();
		if (vertexOffset < numVerticesTypeA) {
			// calculate the offset of this vertex
			bitOffset += vertexOffset * bitsPerVertexTypeA;

			// read vertex id of lower levels
			vertex.vertexIds = new int[level + 2];
			for (int i = 0; i < level; i++) {
				int blockIdx = (int) Deserializer.readUInt(data, bitsPerIndirectBlockRef,
						bitOffset / 8, bitOffset % 8);
				bitOffset += bitsPerIndirectBlockRef;
				int _bitOffset = (offsetReferencedBlocks * 8)
						+ (blockIdx * routingGraph.bitsPerBlockId);
				int _blockId = (int) Deserializer.readUInt(data, routingGraph.bitsPerBlockId,
						_bitOffset / 8, _bitOffset % 8);
				int _vertexOffset = (int) Deserializer.readUInt(data,
						routingGraph.bitsPerVertexOffset, bitOffset / 8, bitOffset % 8);
				bitOffset += routingGraph.bitsPerVertexOffset;

				vertex.vertexIds[i] = routingGraph.getVertexId(_blockId, _vertexOffset);
			}

			// get vertex id of current level
			vertex.vertexIds[level] = routingGraph.getVertexId(blockId, vertexOffset);

			// read vertex id of higher level
			int blockIdx = (int) Deserializer.readUInt(data, bitsPerIndirectBlockRef,
					bitOffset / 8, bitOffset % 8);
			bitOffset += bitsPerIndirectBlockRef;
			int _bitOffset = (offsetReferencedBlocks * 8)
					+ (blockIdx * routingGraph.bitsPerBlockId);
			int _blockId = (int) Deserializer.readUInt(data, routingGraph.bitsPerBlockId,
					_bitOffset / 8, _bitOffset % 8);
			int _vertexOffset = (int) Deserializer.readUInt(data,
					routingGraph.bitsPerVertexOffset, bitOffset / 8, bitOffset % 8);
			bitOffset += routingGraph.bitsPerVertexOffset;

			vertex.vertexIds[level + 1] = routingGraph.getVertexId(_blockId, _vertexOffset);

			// read neighborhood
			vertex.neighborhood = (int) Deserializer.readUInt(data, bitsPerNeighborhood,
					bitOffset / 8, bitOffset % 8);
			bitOffset += bitsPerNeighborhood;

			// read Bit-offset of first outbound edge
			vertex.bitOffsetFirstOutboundEdge = Deserializer.readInt(data, bitOffset / 8,
					bitOffset % 8);
			bitOffset += 32;

			// read coordinate
			vertex.latitudeE6 = -1;
			vertex.longitudeE6 = -1;
			if (level == 0) {
				vertex.latitudeE6 = (int) Deserializer.readUInt(data, bitsPerCoordinate,
						bitOffset / 8, bitOffset % 8)
						+ minLatitudeE6;
				bitOffset += bitsPerCoordinate;
				vertex.longitudeE6 = (int) Deserializer.readUInt(data, bitsPerCoordinate,
						bitOffset / 8, bitOffset % 8)
						+ minLongitudeE6;
				bitOffset += bitsPerCoordinate;
			}
		} else if (vertexOffset < numVerticesTypeA + numVerticesTypeB) {
			// read vertex typeB

			// calculate the offset of this vertex
			bitOffset += (numVerticesTypeA * bitsPerVertexTypeA)
					+ ((vertexOffset - numVerticesTypeA) * bitsPerVertexTypeB);

			// read vertex id of lower levels
			vertex.vertexIds = new int[level + 2];
			for (int i = 0; i < level; i++) {
				int blockIdx = (int) Deserializer.readUInt(data, bitsPerIndirectBlockRef,
						bitOffset / 8, bitOffset % 8);
				bitOffset += bitsPerIndirectBlockRef;
				int _bitOffset = (offsetReferencedBlocks * 8)
						+ (blockIdx * routingGraph.bitsPerBlockId);
				int _blockId = (int) Deserializer.readUInt(data, routingGraph.bitsPerBlockId,
						_bitOffset / 8, _bitOffset % 8);
				int _vertexOffset = (int) Deserializer.readUInt(data,
						routingGraph.bitsPerVertexOffset, bitOffset / 8, bitOffset % 8);
				bitOffset += routingGraph.bitsPerVertexOffset;

				vertex.vertexIds[i] = routingGraph.getVertexId(_blockId, _vertexOffset);
			}

			// get vertex id of current level
			vertex.vertexIds[level] = routingGraph.getVertexId(blockId, vertexOffset);

			// read vertex id of higher level
			vertex.vertexIds[level + 1] = -1;

			// read neighborhood
			vertex.neighborhood = (int) Deserializer.readUInt(data, bitsPerNeighborhood,
					bitOffset / 8, bitOffset % 8);
			bitOffset += bitsPerNeighborhood;

			// read Bit-offset of first outbound edge
			vertex.bitOffsetFirstOutboundEdge = Deserializer.readInt(data, bitOffset / 8,
					bitOffset % 8);
			bitOffset += 32;

			// read coordinate
			vertex.latitudeE6 = -1;
			vertex.longitudeE6 = -1;
			if (level == 0) {
				vertex.latitudeE6 = (int) Deserializer.readUInt(data, bitsPerCoordinate,
						bitOffset / 8, bitOffset % 8)
						+ minLatitudeE6;
				bitOffset += bitsPerCoordinate;
				vertex.longitudeE6 = (int) Deserializer.readUInt(data, bitsPerCoordinate,
						bitOffset / 8, bitOffset % 8)
						+ minLongitudeE6;
				bitOffset += bitsPerCoordinate;
			}
		} else {
			// read vertex typeC

			// calculate the offset of this vertex
			bitOffset += (numVerticesTypeA * bitsPerVertexTypeA)
					+ (numVerticesTypeB * bitsPerVertexTypeB)
					+ ((vertexOffset - numVerticesTypeA - numVerticesTypeB) * bitsPerVertexTypeC);

			// read vertex id of lower levels
			vertex.vertexIds = new int[level + 2];
			for (int i = 0; i < level; i++) {
				int blockIdx = (int) Deserializer.readUInt(data, bitsPerIndirectBlockRef,
						bitOffset / 8, bitOffset % 8);
				bitOffset += bitsPerIndirectBlockRef;
				int _bitOffset = (offsetReferencedBlocks * 8)
						+ (blockIdx * routingGraph.bitsPerBlockId);
				int _blockId = (int) Deserializer.readUInt(data, routingGraph.bitsPerBlockId,
						_bitOffset / 8, _bitOffset % 8);
				int _vertexOffset = (int) Deserializer.readUInt(data,
						routingGraph.bitsPerVertexOffset, bitOffset / 8, bitOffset % 8);
				bitOffset += routingGraph.bitsPerVertexOffset;

				vertex.vertexIds[i] = routingGraph.getVertexId(_blockId, _vertexOffset);
			}

			// get vertex id of current level
			vertex.vertexIds[level] = routingGraph.getVertexId(blockId, vertexOffset);

			// read vertex id of higher level
			vertex.vertexIds[level + 1] = -1;

			// read neighborhood
			vertex.neighborhood = Integer.MAX_VALUE;

			// read Bit-offset of first outbound edge
			vertex.bitOffsetFirstOutboundEdge = Deserializer.readInt(data, bitOffset / 8,
					bitOffset % 8);
			bitOffset += 32;

			// read coordinate
			vertex.latitudeE6 = -1;
			vertex.longitudeE6 = -1;
			if (level == 0) {
				vertex.latitudeE6 = (int) Deserializer.readUInt(data, bitsPerCoordinate,
						bitOffset / 8, bitOffset % 8)
						+ minLatitudeE6;
				bitOffset += bitsPerCoordinate;
				vertex.longitudeE6 = (int) Deserializer.readUInt(data, bitsPerCoordinate,
						bitOffset / 8, bitOffset % 8)
						+ minLongitudeE6;
				bitOffset += bitsPerCoordinate;
			}
		}
		return vertex;
	}

	private byte[] getZeroTerminatedString(int byteOffset) {
		int i = byteOffset;
		while (data[i] != (byte) 0x00) {
			i++;
		}
		byte[] b = new byte[i - byteOffset];
		for (int j = 0; j < b.length; j++) {
			b[j] = data[byteOffset + j];
		}
		return b;
	}

	/**
	 * Looks up the outgoing adjacency list of the given vertex.
	 * 
	 * @param vertex
	 *            the source vertex.
	 * @return all outgoing edges of the given vertex.
	 */
	public HHEdge[] getOutboundEdges(HHVertex vertex) {
		int bitOffset = vertex.bitOffsetFirstOutboundEdge;

		// read number of edges in adjacency list
		int numEdges = (int) Deserializer.readUInt(data, 4, bitOffset / 8, bitOffset % 8);
		bitOffset += 4;

		if (numEdges == 15) {
			numEdges = (int) Deserializer.readUInt(data, 24, bitOffset / 8, bitOffset % 8);
			bitOffset += 24;
		}

		HHEdge[] edges = new HHEdge[numEdges];
		for (int i = 0; i < edges.length; i++) {
			// recycle edge from pool
			HHEdge edge = routingGraph.edgePool.borrow();

			// set source id
			edge.sourceId = vertex.vertexIds[vertex.vertexIds.length - 2];

			// set weight
			edge.weight = (int) Deserializer.readUInt(data, routingGraph.bitsPerEdgeWeight,
					bitOffset / 8, bitOffset % 8);
			bitOffset += routingGraph.bitsPerEdgeWeight;

			boolean isInternal = Deserializer.readBit(data, bitOffset / 8, bitOffset % 8);
			bitOffset += 1;

			// set target id
			int _blockId;
			if (!isInternal) {
				int blockIdx = (int) Deserializer.readUInt(data, bitsPerIndirectBlockRef,
						bitOffset / 8, bitOffset % 8);
				bitOffset += bitsPerIndirectBlockRef;
				int _bitOffset = (offsetReferencedBlocks * 8)
						+ (blockIdx * routingGraph.bitsPerBlockId);
				_blockId = (int) Deserializer.readUInt(data, routingGraph.bitsPerBlockId,
						_bitOffset / 8, _bitOffset % 8);
			} else {
				_blockId = blockId;
			}
			int _vertexOffset = (int) Deserializer.readUInt(data,
					routingGraph.bitsPerVertexOffset, bitOffset / 8, bitOffset % 8);
			bitOffset += routingGraph.bitsPerVertexOffset;
			edge.targetId = routingGraph.getVertexId(_blockId, _vertexOffset);

			// set forward
			edge.isForward = Deserializer.readBit(data, bitOffset / 8, bitOffset % 8);
			bitOffset += 1;

			// set backward
			edge.isBackward = Deserializer.readBit(data, bitOffset / 8, bitOffset % 8);
			bitOffset += 1;

			// set core
			edge.isCore = Deserializer.readBit(data, bitOffset / 8, bitOffset % 8);
			bitOffset += 1;

			// clear satellite data
			edge.osmStreetType = -1;
			edge.isRoundAbout = false;
			edge.name = null;
			edge.ref = null;
			edge.waypoints = null;

			// set satellite data (only for level-0 forward edges)
			if (level == 0 && edge.isForward) {
				// set motor-way link
				edge.osmStreetType = (byte) Deserializer.readUInt(data,
						routingGraph.bitsPerStreetType,
						bitOffset / 8,
						bitOffset % 8);
				bitOffset += routingGraph.bitsPerStreetType;

				// set roundabout
				edge.isRoundAbout = Deserializer
						.readBit(data, bitOffset / 8, bitOffset % 8);
				bitOffset += 1;

				// hasName ?
				boolean hasName = Deserializer
						.readBit(data, bitOffset / 8, bitOffset % 8);
				bitOffset += 1;

				// hasRef ?
				boolean hasRef = Deserializer
						.readBit(data, bitOffset / 8, bitOffset % 8);
				bitOffset += 1;

				// set name
				if (hasName) {
					int byteOffset = (int) Deserializer.readUInt(data,
							bitsPerStreetNameOffset,
							bitOffset / 8,
							bitOffset % 8);
					bitOffset += bitsPerStreetNameOffset;
					edge.name = getZeroTerminatedString(offsetStreetNames + byteOffset);
				}

				// set ref
				if (hasRef) {
					int byteOffset = (int) Deserializer.readUInt(data,
							bitsPerStreetNameOffset,
							bitOffset / 8,
							bitOffset % 8);
					bitOffset += bitsPerStreetNameOffset;
					edge.ref = getZeroTerminatedString(offsetStreetNames + byteOffset);
				}

				// set waypoints
				int numWaypoints = (int) Deserializer.readUInt(data, 4, bitOffset / 8,
						bitOffset % 8);
				bitOffset += 4;
				if (numWaypoints == 15) {
					numWaypoints = (int) Deserializer.readUInt(data, 16, bitOffset / 8,
							bitOffset % 8);
					bitOffset += 16;
				}
				edge.waypoints = new int[numWaypoints * 2];
				for (int j = 0; j < numWaypoints; j++) {
					edge.waypoints[j * 2] = minLatitudeE6
							+ (int) Deserializer.readUInt(data, bitsPerCoordinate,
							bitOffset / 8,
							bitOffset % 8);
					bitOffset += bitsPerCoordinate;
					edge.waypoints[(j * 2) + 1] = minLongitudeE6
							+ (int) Deserializer.readUInt(data, bitsPerCoordinate,
							bitOffset / 8,
							bitOffset % 8);
					bitOffset += bitsPerCoordinate;
				}
			}

			// set minLevel
			edge.minLevel = 0;
			if (level > 0) {
				edge.minLevel = Deserializer.readByte(data, bitOffset / 8, bitOffset % 8);
				bitOffset += 8;
			}

			// set hop indices if this edge is a shortcut
			edge.hopIndices = null;
			if (edge.minLevel > 0
					&& routingGraph.hasShortcutHopIndices != HHRoutingGraph.HOP_INDICES_NONE) {
				int numHopIndices = (int) Deserializer.readUInt(data, 5, bitOffset / 8,
						bitOffset % 8);
				bitOffset += 5;

				edge.hopIndices = new int[numHopIndices];
				for (int j = 0; j < numHopIndices; j++) {
					edge.hopIndices[j] = (int) Deserializer.readUInt(data, 4, bitOffset / 8,
							bitOffset % 8);
					bitOffset += 4;
					if (edge.hopIndices[j] == 15) {
						edge.hopIndices[j] = (int) Deserializer.readUInt(data, 24,
								bitOffset / 8,
								bitOffset % 8);
						bitOffset += 24;
					}
				}
			}
			edges[i] = edge;
		}
		return edges;
	}
}
