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
package org.mapsforge.preprocessing.routing.highwayHierarchies.util.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.JXMapKit.DefaultProviders;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.painter.Painter;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.server.routing.IEdge;
import org.mapsforge.server.routing.IRouter;
import org.mapsforge.server.routing.IVertex;

public class RouteViewer {

	private static final long serialVersionUID = 1L;
	private final JFrame frame;
	final JXMapKit mapKit;
	final RouteOverlay overlay;
	GeoPosition routeSource;
	GeoPosition routeDestination;

	private LinkedList<IEdge> edges = new LinkedList<IEdge>();

	public RouteViewer(IRouter router) {
		this.frame = new JFrame("RouteViewer");
		this.frame.setSize(800, 600);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.mapKit = new JXMapKit();

		mapKit.setDefaultProvider(DefaultProviders.OpenStreetMaps);
		mapKit.setDataProviderCreditShown(true);

		this.overlay = new RouteOverlay(router, Color.RED.darker(),
				Color.BLUE.darker().darker());
		mapKit.getMainMap().setOverlayPainter(overlay);
		mapKit.getMainMap().addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				mapKit.setAddressLocation((mapKit.getMainMap().convertPointToGeoPosition(evt
						.getPoint())));
			}
		});

		PopMenu menu = new PopMenu();
		mapKit.getMainMap().addMouseListener(menu.getMouseAdapter());

		this.frame.add(mapKit);
		frame.setVisible(true);
	}

	public void drawEdges(LinkedList<IEdge> edges) {
		this.edges.addAll(edges);
	}

	private class PopMenu extends JPopupMenu {

		private static final long serialVersionUID = 1L;

		private MouseAdapter mouseListener;
		Color cSelected = Color.green.darker().darker();
		Point position;

		public PopMenu() {
			super();
			mouseListener = new MouseAdapter() {

				@Override
				public void mouseReleased(MouseEvent e) {
					if (e.isPopupTrigger()) {
						show(e.getComponent(), e.getX(), e.getY());
						position = e.getPoint();
					}
				}
			};
			add(getRouterMenu());
		}

		public MouseAdapter getMouseAdapter() {
			return mouseListener;
		}

		private JMenu getRouterMenu() {
			JMenu menu = new JMenu("routing");
			JMenuItem miSrc = new JMenuItem("choose as source") {
				private static final long serialVersionUID = 1L;

				@Override
				public Color getForeground() {
					if (routeSource == null) {
						return super.getForeground();
					}
					return cSelected;
				}
			};
			miSrc.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					routeSource = mapKit.getMainMap().convertPointToGeoPosition(position);
					if (routeDestination != null && routeSource != null) {
						overlay.setRoute(new GeoCoordinate(routeSource.getLatitude(),
								routeSource.getLongitude()),
								new GeoCoordinate(routeDestination.getLatitude(),
								routeDestination.getLongitude()));
						routeSource = null;
						routeDestination = null;
					}

				}
			});
			menu.add(miSrc);
			JMenuItem miDst = new JMenuItem("choose as destination") {
				private static final long serialVersionUID = 1L;

				@Override
				public Color getForeground() {
					if (routeDestination == null) {
						return super.getForeground();
					}
					return cSelected;
				}
			};
			miDst.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					routeDestination = mapKit.getMainMap().convertPointToGeoPosition(position);
					if (routeDestination != null && routeSource != null) {
						overlay.setRoute(new GeoCoordinate(routeSource.getLatitude(),
								routeSource.getLongitude()),
								new GeoCoordinate(routeDestination.getLatitude(),
								routeDestination.getLongitude()));
						routeSource = null;
						routeDestination = null;
					}

				}
			});
			menu.add(miDst);

			return menu;
		}
	}

	private class RouteOverlay implements Painter<JXMapViewer> {

		private final IRouter router;
		private final Color cRoute, cSearchSpace;
		private IEdge[] route;
		private LinkedList<IEdge> searchSpace;

		public RouteOverlay(IRouter router, Color cRoute, Color cSearchSpace) {
			this.router = router;
			this.searchSpace = new LinkedList<IEdge>();
			this.route = null;
			this.cRoute = cRoute;
			this.cSearchSpace = cSearchSpace;
		}

		public void setRoute(GeoCoordinate src, GeoCoordinate tgt) {
			synchronized (this) {
				IVertex s = router.getNearestVertex(src);
				IVertex t = router.getNearestVertex(tgt);
				searchSpace.clear();
				this.route = router.getShortestPathDebug(s.getId(), t.getId(), searchSpace);
				mapKit.repaint();
			}
		}

		@Override
		public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
			g = (Graphics2D) g.create();
			Rectangle rect = mapKit.getMainMap().getViewportBounds();
			g.translate(-rect.x, -rect.y);
			g.setColor(cSearchSpace);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g.setStroke(new BasicStroke(2));
			synchronized (this) {
				for (IEdge e : edges) {
					GeoCoordinate[] coords = e.getAllWaypoints();
					for (int j = 1; j < coords.length; j++) {
						double[] cs = new double[] { coords[j - 1].getLatitude(),
								coords[j - 1].getLongitude() };
						double[] ct = new double[] { coords[j].getLatitude(),
								coords[j].getLongitude() };
						Point2D s = mapKit.getMainMap().getTileFactory()
								.geoToPixel(new GeoPosition(cs), mapKit.getMainMap().getZoom());
						Point2D t = mapKit.getMainMap().getTileFactory()
								.geoToPixel(new GeoPosition(ct), mapKit.getMainMap().getZoom());
						g.drawLine((int) s.getX(), (int) s.getY(), (int) t.getX(),
								(int) t.getY());
					}
				}

				for (IEdge e : searchSpace) {
					GeoCoordinate[] coords = new GeoCoordinate[e.getWaypoints().length + 2];
					coords[0] = e.getSource().getCoordinate();
					coords[coords.length - 1] = e.getTarget().getCoordinate();
					int i = 1;
					for (GeoCoordinate c : e.getWaypoints()) {
						coords[i++] = c;
					}
					for (int j = 1; j < coords.length; j++) {
						double[] cs = new double[] { coords[j - 1].getLatitude(),
								coords[j - 1].getLongitude() };
						double[] ct = new double[] { coords[j].getLatitude(),
								coords[j].getLongitude() };
						Point2D s = mapKit.getMainMap().getTileFactory()
								.geoToPixel(new GeoPosition(cs), mapKit.getMainMap().getZoom());
						Point2D t = mapKit.getMainMap().getTileFactory()
								.geoToPixel(new GeoPosition(ct), mapKit.getMainMap().getZoom());
						g.drawLine((int) s.getX(), (int) s.getY(), (int) t.getX(),
								(int) t.getY());
					}
				}

				g.setStroke(new BasicStroke(4));
				g.setColor(cRoute);
				if (route != null) {
					for (IEdge e : route) {
						GeoCoordinate[] coords = new GeoCoordinate[e.getWaypoints().length + 2];
						coords[0] = e.getSource().getCoordinate();
						coords[coords.length - 1] = e.getTarget().getCoordinate();
						int i = 1;
						for (GeoCoordinate c : e.getWaypoints()) {
							coords[i++] = c;
						}
						for (int j = 1; j < coords.length; j++) {
							double[] cs = new double[] { coords[j - 1].getLatitude(),
									coords[j - 1].getLongitude() };
							double[] ct = new double[] { coords[j].getLatitude(),
									coords[j].getLongitude() };
							Point2D s = mapKit
									.getMainMap()
									.getTileFactory()
									.geoToPixel(new GeoPosition(cs),
									mapKit.getMainMap().getZoom());
							Point2D t = mapKit
									.getMainMap()
									.getTileFactory()
									.geoToPixel(new GeoPosition(ct),
									mapKit.getMainMap().getZoom());
							g.drawLine((int) s.getX(), (int) s.getY(), (int) t.getX(),
									(int) t.getY());
						}
					}
				}
			}
			g.dispose();
		}
	}

}
