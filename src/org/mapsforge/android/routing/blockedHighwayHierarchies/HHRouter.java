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

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.Rect;
import org.mapsforge.preprocessing.routing.highwayHierarchies.util.renderer.RendererV2;
import org.mapsforge.server.routing.IEdge;
import org.mapsforge.server.routing.IRouter;
import org.mapsforge.server.routing.IVertex;

/**
 * This class implements the router interface which is already used server sided. There is some
 * problem on IOException handling. The server side interface does not yet allow to throw
 * IOException thus this implementation returns just null values if an IO Error occurs. A nice
 * solution has to be found. This implementation can not provide id's for edes, thus always -1
 * is returned.
 */
public class HHRouter implements IRouter {

	private static final double MAX_RTREE_SEARCH_RADIUS = 2500;

	final HHRoutingGraph routingGraph;
	final HHAlgorithm hhAlgorithm;

	/**
	 * Construct a Router Object working on the given binary file.
	 * 
	 * @param hhBinaryFile
	 *            the highway hieararchies binary file.
	 * @param cacheSizeBytes
	 *            Number of bytes to use for caching clusters of the graph.
	 * @throws IOException
	 *             on error reading from file.
	 */
	public HHRouter(File hhBinaryFile, int cacheSizeBytes) throws IOException {
		this.routingGraph = new HHRoutingGraph(hhBinaryFile, cacheSizeBytes);
		this.hhAlgorithm = new HHAlgorithm(routingGraph);
	}

	@Override
	public String getAlgorithmName() {
		return "Blocked Highway Hierarchies";
	}

	@Override
	public Rect getBoundingBox() {
		return routingGraph.getBoundingBox();
	}

	@Override
	public IEdge[] getNearestEdges(GeoCoordinate coord) {
		IVertex vertex = getNearestVertex(coord);
		if (vertex == null) {
			return null;
		}
		return vertex.getOutboundEdges();
	}

	@Override
	public IVertex getNearestVertex(GeoCoordinate coord) {
		try {
			HHVertex vertex = routingGraph.getNearestVertex(coord, MAX_RTREE_SEARCH_RADIUS);
			if (vertex == null) {
				return null;
			}
			return new VertexImpl(vertex);
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public IEdge[] getShortestPath(int sourceId, int targetId) {
		try {
			LinkedList<HHEdge> shortestPath = new LinkedList<HHEdge>();
			hhAlgorithm.getShortestPath(sourceId, targetId, shortestPath, false);
			EdgeImpl[] edges = new EdgeImpl[shortestPath.size()];
			int i = 0;
			for (HHEdge e : shortestPath) {
				edges[i++] = new EdgeImpl(e);
			}
			return edges;
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public IEdge[] getShortestPathDebug(int sourceId, int targetId,
			Collection<IEdge> searchspaceBuff) {
		return getShortestPath(sourceId, targetId);
	}

	@Override
	public IVertex getVertex(int id) {
		HHVertex vertex;
		try {
			vertex = routingGraph.getVertex(id);
			return new VertexImpl(vertex);
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public Iterator<? extends IVertex> getVerticesWithinBox(Rect bbox) {
		try {
			final LinkedList<HHVertex> vertices = routingGraph.getVerticesWithinBBox(bbox);
			return new Iterator<IVertex>() {

				@Override
				public boolean hasNext() {
					return vertices.size() > 0;
				}

				@Override
				public IVertex next() {
					return new VertexImpl(vertices.removeFirst());
				}

				@Override
				public void remove() {
					// not implement, static data!
				}
			};
		} catch (IOException e) {
			return null;
		}
	}

	private class VertexImpl implements IVertex {

		private final HHVertex vertex;

		VertexImpl(HHVertex vertex) {
			this.vertex = vertex;
		}

		@Override
		public GeoCoordinate getCoordinate() {
			return new GeoCoordinate(vertex.latitudeE6, vertex.longitudeE6);
		}

		@Override
		public int getId() {
			return vertex.vertexIds[0];
		}

		@Override
		public IEdge[] getOutboundEdges() {
			try {
				HHEdge[] edges = routingGraph.getOutboundEdges(vertex);
				// do not return backward only edges
				int n = 0;
				for (int i = 0; i < edges.length; i++) {
					if (edges[i].isForward) {
						n++;
					}
				}
				EdgeImpl[] result = new EdgeImpl[n];
				int j = 0;
				for (int i = 0; i < edges.length; i++) {
					if (edges[i].isForward) {
						result[j++] = new EdgeImpl(edges[i]);
					} else {
						routingGraph.releaseEdge(edges[i]);
					}
				}
				return result;
			} catch (IOException e) {
				return null;
			}
		}
	}

	private class EdgeImpl implements IEdge {

		private final HHEdge edge;

		EdgeImpl(HHEdge edge) {
			this.edge = edge;
		}

		@Override
		public GeoCoordinate[] getAllWaypoints() {
			try {

				GeoCoordinate[] waypoints = new GeoCoordinate[edge.waypoints.length / 2 + 2];
				HHVertex source = routingGraph.getVertex(edge.sourceId);
				HHVertex target =
						routingGraph.getVertex(edge.targetId);
				waypoints[0] = new
						GeoCoordinate(source.latitudeE6, source.longitudeE6);
				for (int i = 1; i < waypoints.length - 1; i++) {
					waypoints[i] = new
							GeoCoordinate(edge.waypoints[(i - 1) * 2],
							edge.waypoints[((i - 1) * 2) +
							1]);
				}
				waypoints[waypoints.length - 1] = new GeoCoordinate(target.latitudeE6,
						target.longitudeE6);
				routingGraph.releaseVertex(source);
				routingGraph.releaseVertex(target);
				return waypoints;
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
			return null;
		}

		@Override
		public int getId() {
			return -1;
		}

		@Override
		public String getName() {
			if (edge.name == null) {
				return null;
			}
			return new String(edge.name);
		}

		@Override
		public String getRef() {
			if (edge.ref == null) {
				return null;
			}
			return new String(edge.ref);
		}

		@Override
		public IVertex getSource() {
			try {
				HHVertex source = routingGraph.getVertex(edge.sourceId);
				return new VertexImpl(source);
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		public IVertex getTarget() {
			try {
				HHVertex source = routingGraph.getVertex(edge.targetId);
				return new VertexImpl(source);
			} catch (IOException e) {
				return null;
			}
		}

		@Override
		public GeoCoordinate[] getWaypoints() {
			GeoCoordinate[] waypoints = new GeoCoordinate[edge.waypoints.length / 2];
			for (int i = 0; i < waypoints.length; i++) {
				waypoints[i] = new GeoCoordinate(edge.waypoints[i << 1],
						edge.waypoints[i << 1 + 1]);
			}
			return waypoints;
		}

		@Override
		public int getWeight() {
			return edge.weight;
		}

		@Override
		public String getType() {
			if (edge.osmStreetType == -1) {
				return null;
			}
			return routingGraph.streetTypes[edge.osmStreetType];
		}

		@Override
		public boolean isRoundabout() {
			return edge.isRoundAbout;
		}

		@Override
		public String getDestination() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	/**
	 * A Sample.
	 * 
	 * @param args
	 *            not used.
	 * @throws IOException
	 *             if there is something wrong reading the file.
	 */
	public static void main(String[] args) throws IOException {
		HHRouter router = new HHRouter(new File(
				"router/berlin.blockedHH"), 1000 * 1024);
		// IVertex source = router.getNearestVertex(new GeoCoordinate(50.509769, 10.4567655));
		// IVertex target = router.getNearestVertex(new GeoCoordinate(52.4556941, 13.2918805));
		IVertex source = router.getNearestVertex(new GeoCoordinate(52.509769, 13.456766));
		IVertex target = router.getNearestVertex(new GeoCoordinate(52.454685, 13.29188));
		long startTime = System.currentTimeMillis();
		IEdge[] shortestPath = router.getShortestPath(source.getId(), target.getId());
		long time = System.currentTimeMillis() - startTime;
		for (IEdge e : shortestPath) {
			System.out.println(e.getName() + " " + e.getRef() + " " + e.getType());
		}
		RendererV2 renderer = new RendererV2(1024, 768, router, Color.BLACK,
				Color.WHITE);

		renderer.addRoute(shortestPath, Color.GREEN);
		System.out.println(time + "ms");
	}
}
