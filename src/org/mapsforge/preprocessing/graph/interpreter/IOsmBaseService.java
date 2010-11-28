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

import java.sql.SQLException;
import java.util.LinkedList;

import org.mapsforge.preprocessing.graph.model.osmxml.OsmWay_withNodes;
import org.mapsforge.preprocessing.graph.osm2rg.routingGraph.RgEdge;
import org.mapsforge.preprocessing.graph.osm2rg.routingGraph.RgVertex;

public interface IOsmBaseService {

	public LinkedList<OsmWay_withNodes> getAllWaysFromDB() throws SQLException;

	public void insertVertices(LinkedList<RgVertex> vertices) throws SQLException;

	public void insertEdges(LinkedList<RgEdge> edges) throws SQLException;

}
