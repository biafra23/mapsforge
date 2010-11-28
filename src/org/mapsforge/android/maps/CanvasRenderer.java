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

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * A map renderer which uses a Canvas for drawing.
 */
class CanvasRenderer extends DatabaseMapGenerator {
	private static final Paint PAINT_TILE_FRAME = new Paint(Paint.ANTI_ALIAS_FLAG);
	private static final String THREAD_NAME = "CanvasRenderer";
	private int arrayListIndex;
	private Canvas canvas;
	private CircleContainer circleContainer;
	private WayContainer complexWayContainer;
	private float[][] coordinates;
	private byte currentLayer;
	private byte currentLevel;
	private Path path;
	private WayTextContainer pathTextContainer;
	private PointTextContainer pointTextContainer;
	private ShapePaintContainer shapePaintContainer;
	private ArrayList<ArrayList<ShapePaintContainer>> shapePaintContainers;
	private SymbolContainer symbolContainer;
	private float[] textCoordinates;
	private float[] tileFrame;
	private ArrayList<ShapePaintContainer> wayList;

	@Override
	void drawMapSymbols(ArrayList<SymbolContainer> drawSymbols) {
		for (this.arrayListIndex = drawSymbols.size() - 1; this.arrayListIndex >= 0; --this.arrayListIndex) {
			this.symbolContainer = drawSymbols.get(this.arrayListIndex);
			this.canvas.drawBitmap(this.symbolContainer.symbol, this.symbolContainer.x,
					this.symbolContainer.y, null);
		}
	}

	@Override
	void drawNodes(ArrayList<PointTextContainer> drawNodes) {
		for (this.arrayListIndex = drawNodes.size() - 1; this.arrayListIndex >= 0; --this.arrayListIndex) {
			this.pointTextContainer = drawNodes.get(this.arrayListIndex);
			if (this.pointTextContainer.paintBack != null) {
				this.canvas.drawText(this.pointTextContainer.text, this.pointTextContainer.x,
						this.pointTextContainer.y, this.pointTextContainer.paintBack);
			}
			this.canvas.drawText(this.pointTextContainer.text, this.pointTextContainer.x,
					this.pointTextContainer.y, this.pointTextContainer.paintFront);
		}
	}

	@Override
	void drawTileFrame() {
		this.canvas.drawLines(this.tileFrame, PAINT_TILE_FRAME);
	}

	@Override
	void drawWayNames(ArrayList<WayTextContainer> drawWayNames) {
		for (this.arrayListIndex = drawWayNames.size() - 1; this.arrayListIndex >= 0; --this.arrayListIndex) {
			this.pathTextContainer = drawWayNames.get(this.arrayListIndex);
			this.path.rewind();
			this.textCoordinates = this.pathTextContainer.coordinates;
			this.path.moveTo(this.textCoordinates[0], this.textCoordinates[1]);
			for (int i = 2; i < this.textCoordinates.length; i += 2) {
				this.path.lineTo(this.textCoordinates[i], this.textCoordinates[i + 1]);
			}
			this.canvas.drawTextOnPath(this.pathTextContainer.text, this.path, 0, 3,
					this.pathTextContainer.paint);
		}
	}

	@Override
	void drawWays(ArrayList<ArrayList<ArrayList<ShapePaintContainer>>> drawWays, byte layers,
			byte levelsPerLayer) {
		for (this.currentLayer = 0; this.currentLayer < layers; ++this.currentLayer) {
			this.shapePaintContainers = drawWays.get(this.currentLayer);
			for (this.currentLevel = 0; this.currentLevel < levelsPerLayer; ++this.currentLevel) {
				this.wayList = this.shapePaintContainers.get(this.currentLevel);
				for (this.arrayListIndex = this.wayList.size() - 1; this.arrayListIndex >= 0; --this.arrayListIndex) {
					this.shapePaintContainer = this.wayList.get(this.arrayListIndex);
					this.path.rewind();
					switch (this.shapePaintContainer.shapeContainer.getShapeType()) {
						case CIRCLE:
							this.circleContainer = (CircleContainer) this.shapePaintContainer.shapeContainer;
							this.path.addCircle(this.circleContainer.x, this.circleContainer.y,
									this.circleContainer.radius, Path.Direction.CCW);
							break;
						case WAY:
							this.complexWayContainer = (WayContainer) this.shapePaintContainer.shapeContainer;
							this.coordinates = this.complexWayContainer.coordinates;
							for (int j = 0; j < this.coordinates.length; ++j) {
								// make sure that the coordinates sequence is not empty
								if (this.coordinates[j].length > 2) {
									this.path.moveTo(this.coordinates[j][0],
											this.coordinates[j][1]);
									for (int i = 2; i < this.coordinates[j].length; i += 2) {
										this.path.lineTo(this.coordinates[j][i],
												this.coordinates[j][i + 1]);
									}
								}
							}
							break;
					}
					this.canvas.drawPath(this.path, this.shapePaintContainer.paint);
				}
			}
		}
	}

	@Override
	void finishMapGeneration() {
		// do nothing
	}

	@Override
	String getThreadName() {
		return THREAD_NAME;
	}

	@Override
	void onAttachedToWindow() {
		// do nothing
	}

	@Override
	void onDetachedFromWindow() {
		// do nothing
	}

	@Override
	void setupMapGenerator(Bitmap bitmap) {
		this.canvas = new Canvas(bitmap);
		this.tileFrame = new float[] { 0, 0, 0, Tile.TILE_SIZE, 0, Tile.TILE_SIZE,
				Tile.TILE_SIZE, Tile.TILE_SIZE, Tile.TILE_SIZE, Tile.TILE_SIZE, Tile.TILE_SIZE,
				0 };
		this.path = new Path();
		this.path.setFillType(Path.FillType.EVEN_ODD);
	}
}