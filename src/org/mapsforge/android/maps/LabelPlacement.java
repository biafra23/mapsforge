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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import android.graphics.Rect;

/**
 * This class place the labels form POIs, area labels and normal labels. The main target is
 * avoiding collisions of these different labels.
 */
class LabelPlacement {
	/**
	 * This class holds the reference positions for the two and four point greedy algorithms.
	 */
	class ReferencePosition {
		final float height;
		final int nodeNumber;
		SymbolContainer symbol;
		final float width;
		final float x;
		final float y;

		ReferencePosition(float x, float y, int nodeNumber, float width, float height,
				SymbolContainer symbol) {
			this.x = x;
			this.y = y;
			this.nodeNumber = nodeNumber;
			this.width = width;
			this.height = height;
			this.symbol = symbol;
		}
	}

	private static final boolean DEFAULT = false;

	private int labelDistanceToLabel = 2;
	private int labelDistanceToSymbol = 2;
	private int placementOption = 1;
	// You can choose between 2 Position and 4 Position
	// placement Model 0 - 2-Position 1 - 4 Position
	// distance adjustments
	private int startDistanceToSymbols = 4;
	private int symbolDistanceToSymbol = 2;

	DependencyCache dependencyCache;
	PointTextContainer label;
	Rect rect1;
	Rect rect2;
	ReferencePosition reference;
	SymbolContainer smb;

	LabelPlacement() {
		dependencyCache = new DependencyCache();
	}

	/**
	 * centers the labels
	 * 
	 * @param labels
	 *            labels to center
	 */
	private void centerLabels(ArrayList<PointTextContainer> labels) {
		for (int i = 0; i < labels.size(); i++) {
			label = labels.get(i);
			label.x = label.x - label.boundary.width() / 2;
		}
	}

	/**
	 * Centers labels with a safety margin for default rendering.
	 * 
	 * @param labels
	 *            Labels to center
	 */
	private void centerLabels2(ArrayList<PointTextContainer> labels) {
		for (int i = 0; i < labels.size(); i++) {
			label = labels.get(i);
			label.x = label.x - label.boundary.width() / 2;
			if (label.symbol != null) {
				label.y = label.y - label.symbol.symbol.getHeight() / 2 - 3;
			}
		}
	}

	private void preprocessAreaLabels(ArrayList<PointTextContainer> areaLabels) {
		centerLabels(areaLabels);

		removeOutOfTileAreaLabels(areaLabels);

		removeOverlappingAreaLabels(areaLabels);

		if (areaLabels.size() != 0) {
			dependencyCache.removeAreaLabelsInalreadyDrawnareas(areaLabels);
		}
	}

	private void preprocessLabels(ArrayList<PointTextContainer> labels) {
		removeOutOfTileLabels(labels);
	}

	private void preprocessSymbols(ArrayList<SymbolContainer> symbols) {
		removeOutOfTileSymbols(symbols);
		removeOverlappingSymbols(symbols);
		dependencyCache.removeSymbolsFromDrawnAreas(symbols);
	}

	/**
	 * This method uses an adapted greedy strategy for the fixed four position model, above,
	 * under left and right form the point of interest. It uses no priority search tree, because
	 * it will not function with symbols only with points. Instead it uses two minimum heaps.
	 * They work similar to a sweep line algorithm but have not a O(n log n +k) runtime. To find
	 * the rectangle that has the top edge, I use also a minimum Heap. The rectangles are sorted
	 * by their y coordinates.
	 * 
	 * @param labels
	 *            label positions and text
	 * @param symbols
	 *            symbol positions
	 * @param areaLabels
	 *            area label positions and text
	 * @return list of labels without overlaps with symbols and other labels by the four fixed
	 *         position greedy strategy
	 */
	private ArrayList<PointTextContainer> processFourPointGreedy(
			ArrayList<PointTextContainer> labels, ArrayList<SymbolContainer> symbols,
			ArrayList<PointTextContainer> areaLabels) {
		ArrayList<PointTextContainer> resolutionSet = new ArrayList<PointTextContainer>();

		// Array for the generated reference positions around the points of interests
		ReferencePosition[] refPos = new ReferencePosition[(labels.size()) * 4];

		// lists that sorts the reference points after the minimum top edge y position
		PriorityQueue<ReferencePosition> priorUp = new PriorityQueue<ReferencePosition>(labels
				.size()
				* 4 * 2 + labels.size() / 10 * 2, new Comparator<ReferencePosition>() {
			@Override
			public int compare(ReferencePosition x, ReferencePosition y) {
				if (x.y < y.y) {
					return -1;
				}

				if (x.y > y.y) {
					return 1;
				}

				return 0;
			}
		});
		// lists that sorts the reference points after the minimum bottom edge y position
		PriorityQueue<ReferencePosition> priorDown = new PriorityQueue<ReferencePosition>(
				labels.size() * 4 * 2 + labels.size() / 10 * 2,
				new Comparator<ReferencePosition>() {
					@Override
					public int compare(ReferencePosition x, ReferencePosition y) {
						if (x.y - x.height < y.y - y.height) {
							return -1;
						}

						if (x.y - x.height > y.y - y.height) {
							return 1;
						}
						return 0;
					}
				});

		PointTextContainer tmp;
		int dis = this.startDistanceToSymbols;

		// creates the reference positions
		for (int z = 0; z < labels.size(); z++) {
			if (labels.get(z) != null) {
				if (labels.get(z).symbol != null) {
					tmp = labels.get(z);

					// up
					refPos[z * 4] = new ReferencePosition(tmp.x - tmp.boundary.width() / 2,
							tmp.y - tmp.symbol.symbol.getHeight() / 2 - dis, z, tmp.boundary
									.width(), tmp.boundary.height(), tmp.symbol);
					// down
					refPos[z * 4 + 1] = new ReferencePosition(tmp.x - tmp.boundary.width() / 2,
							tmp.y + tmp.symbol.symbol.getHeight() / 2 + tmp.boundary.height()
									+ dis, z, tmp.boundary.width(), tmp.boundary.height(),
							tmp.symbol);
					// left
					refPos[z * 4 + 2] = new ReferencePosition(tmp.x
							- tmp.symbol.symbol.getWidth() / 2 - tmp.boundary.width() - dis,
							tmp.y + tmp.boundary.height() / 2, z, tmp.boundary.width(),
							tmp.boundary.height(), tmp.symbol);
					// right
					refPos[z * 4 + 3] = new ReferencePosition(tmp.x
							+ tmp.symbol.symbol.getWidth() / 2 + dis, tmp.y
							+ tmp.boundary.height() / 2 - 0.1f, z, tmp.boundary.width(),
							tmp.boundary.height(), tmp.symbol);
				} else {
					refPos[z * 4] = new ReferencePosition(labels.get(z).x
							- ((labels.get(z).boundary.width()) / 2), labels.get(z).y, z,
							labels.get(z).boundary.width(), labels.get(z).boundary.height(),
							null);
					refPos[z * 4 + 1] = null;
					refPos[z * 4 + 2] = null;
					refPos[z * 4 + 3] = null;
				}
			}
		}

		removeNonValidateReferencePosition(refPos, symbols, areaLabels);

		// do while it gives reference positions
		for (int i = 0; i < refPos.length; i++) {
			reference = refPos[i];
			if (reference != null) {
				priorUp.add(reference);
				priorDown.add(reference);
			}
		}

		while (priorUp.size() != 0) {
			reference = priorUp.remove();

			label = labels.get(reference.nodeNumber);

			resolutionSet.add(new PointTextContainer(label.text, reference.x, reference.y,
					label.paintFront, label.paintBack, label.symbol));

			if (priorUp.size() == 0) {
				return resolutionSet;
			}

			priorUp.remove(refPos[reference.nodeNumber * 4 + 0]);
			priorUp.remove(refPos[reference.nodeNumber * 4 + 1]);
			priorUp.remove(refPos[reference.nodeNumber * 4 + 2]);
			priorUp.remove(refPos[reference.nodeNumber * 4 + 3]);

			priorDown.remove((refPos[reference.nodeNumber * 4 + 0]));
			priorDown.remove((refPos[reference.nodeNumber * 4 + 1]));
			priorDown.remove((refPos[reference.nodeNumber * 4 + 2]));
			priorDown.remove((refPos[reference.nodeNumber * 4 + 3]));

			LinkedList<ReferencePosition> linkedRef = new LinkedList<ReferencePosition>();

			while (priorDown.size() != 0) {
				if (priorDown.peek().x < reference.x + reference.width) {
					linkedRef.add(priorDown.remove());
				} else {
					break;
				}
			}
			// brute Force collision test (faster then sweep line for a small amount of
			// objects)
			for (int i = 0; i < linkedRef.size(); i++) {
				if ((linkedRef.get(i).x <= reference.x + reference.width)
						&& (linkedRef.get(i).y >= reference.y - linkedRef.get(i).height)
						&& (linkedRef.get(i).y <= reference.y + linkedRef.get(i).height)) {
					priorUp.remove(linkedRef.get(i));
					linkedRef.remove(i);
					i--;
				}
			}
			priorDown.addAll(linkedRef);
		}

		return resolutionSet;
	}

	/**
	 * This method uses an adapted greedy strategy for the fixed two position model, above and
	 * under. It uses no priority search tree, because it will not function with symbols only
	 * with points. Instead it uses two minimum heaps. They work similar to a sweep line
	 * algorithm but have not a O(n log n +k) runtime. To find the rectangle that has the
	 * leftest edge, I use also a minimum Heap. The rectangles are sorted by their x
	 * coordinates.
	 * 
	 * @param labels
	 *            label positions and text
	 * @param symbols
	 *            symbol positions
	 * @param areaLabels
	 *            area label positions and text
	 * @return list of labels without overlaps with symbols and other labels by the two fixed
	 *         position greedy strategy
	 */
	private ArrayList<PointTextContainer> processTwoPointGreedy(
			ArrayList<PointTextContainer> labels, ArrayList<SymbolContainer> symbols,
			ArrayList<PointTextContainer> areaLabels) {
		ArrayList<PointTextContainer> resolutionSet = new ArrayList<PointTextContainer>();
		// Array for the generated reference positions around the points of interests
		ReferencePosition[] refPos = new ReferencePosition[(labels.size() * 2)];

		// lists that sorts the reference points after the minimum right edge x position
		PriorityQueue<ReferencePosition> priorRight = new PriorityQueue<ReferencePosition>(
				labels.size() * 2 + labels.size() / 10 * 2,
				new Comparator<ReferencePosition>() {
					@Override
					public int compare(ReferencePosition x, ReferencePosition y) {
						if (x.x + x.width < y.x + y.width) {
							return -1;
						}

						if (x.x + x.width > y.x + y.width) {
							return 1;
						}

						return 0;
					}
				});
		// lists that sorts the reference points after the minimum left edge x position
		PriorityQueue<ReferencePosition> priorLeft = new PriorityQueue<ReferencePosition>(
				labels.size() * 2 + labels.size() / 10 * 2,
				new Comparator<ReferencePosition>() {
					@Override
					public int compare(ReferencePosition x, ReferencePosition y) {
						if (x.x < y.x) {
							return -1;
						}

						if (x.x > y.x) {
							return 1;
						}

						return 0;
					}
				});

		// creates the reference positions
		for (int z = 0; z < labels.size(); z++) {
			label = labels.get(z);

			if (label.symbol != null) {
				refPos[z * 2] = new ReferencePosition(label.x - (label.boundary.width() / 2)
						- 0.1f,
						label.y - label.boundary.height() - this.startDistanceToSymbols, z,
						label.boundary.width(), label.boundary.height(), label.symbol);
				refPos[z * 2 + 1] = new ReferencePosition(label.x
						- (label.boundary.width() / 2), label.y
						+ label.symbol.symbol.getHeight() + this.startDistanceToSymbols, z,
						label.boundary.width(), label.boundary.height(), label.symbol);
			} else {
				refPos[z * 2] = new ReferencePosition(label.x - (label.boundary.width() / 2)
						- 0.1f, label.y, z, label.boundary.width(), label.boundary.height(),
						null);
				refPos[z * 2 + 1] = null;
			}
		}

		// removes reference positions that overlaps with other symbols or dependency objects
		removeNonValidateReferencePosition(refPos, symbols, areaLabels);

		for (int i = 0; i < refPos.length; i++) {
			reference = refPos[i];
			if (reference != null) {
				priorLeft.add(reference);
				priorRight.add(reference);
			}
		}

		while (priorRight.size() != 0) {
			reference = priorRight.remove();

			label = labels.get(reference.nodeNumber);

			resolutionSet.add(new PointTextContainer(label.text, reference.x, reference.y,
					label.paintFront, label.paintBack, reference.symbol));

			// Removes the other position that is a possible position for the label of one point
			// of interest

			priorRight.remove(refPos[reference.nodeNumber * 2 + 1]);

			if (priorRight.size() == 0) {
				return resolutionSet;
			}

			priorLeft.remove(reference);
			priorLeft.remove((refPos[reference.nodeNumber * 2 + 1]));

			// find overlapping labels and deletes the reference points and delete them
			LinkedList<ReferencePosition> linkedRef = new LinkedList<ReferencePosition>();

			while (priorLeft.size() != 0) {
				if (priorLeft.peek().x < reference.x + reference.width) {
					linkedRef.add(priorLeft.remove());
				} else {
					break;
				}
			}

			// brute Force collision test (faster then sweep line for a small amount of
			// objects)
			for (int i = 0; i < linkedRef.size(); i++) {
				if ((linkedRef.get(i).x <= reference.x + reference.width)
						&& (linkedRef.get(i).y >= reference.y - linkedRef.get(i).height)
						&& (linkedRef.get(i).y <= reference.y + linkedRef.get(i).height)) {
					priorRight.remove(linkedRef.get(i));
					linkedRef.remove(i);
					i--;
				}
			}
			priorLeft.addAll(linkedRef);
		}

		return resolutionSet;
	}

	private void removeEmptySymbolReferences(ArrayList<PointTextContainer> nodes,
			ArrayList<SymbolContainer> symbols) {
		for (int i = 0; i < nodes.size(); i++) {
			label = nodes.get(i);
			if (!symbols.contains(label.symbol)) {
				label.symbol = null;
			}
		}
	}

	/**
	 * The greedy algorithms need possible label positions, to choose the best among them. This
	 * method removes the reference points, that are not validate. Not validate means, that the
	 * Reference overlap with another symbol or label or is outside of the tile.
	 * 
	 * @param refPos
	 *            list of the potential positions
	 * @param symbols
	 *            actual list of the symbols
	 * @param areaLabels
	 *            actual list of the area labels
	 */
	private void removeNonValidateReferencePosition(ReferencePosition[] refPos,
			ArrayList<SymbolContainer> symbols, ArrayList<PointTextContainer> areaLabels) {
		int dis = labelDistanceToSymbol;

		for (int i = 0; i < symbols.size(); i++) {
			smb = symbols.get(i);
			rect1 = new android.graphics.Rect((int) smb.x - dis, (int) smb.y - dis, (int) smb.x
					+ smb.symbol.getWidth() + dis, (int) smb.y + smb.symbol.getHeight() + dis);

			for (int y = 0; y < refPos.length; y++) {
				if (refPos[y] != null) {

					rect2 = new android.graphics.Rect((int) refPos[y].x,
							(int) (refPos[y].y - refPos[y].height),
							(int) (refPos[y].x + refPos[y].width), (int) (refPos[y].y));

					if (android.graphics.Rect.intersects(rect2, rect1)) {
						refPos[y] = null;
					}
				}

			}
		}

		dis = this.labelDistanceToLabel;

		for (PointTextContainer areaLabel : areaLabels) {

			rect1 = new android.graphics.Rect((int) areaLabel.x - dis, (int) areaLabel.y
					- areaLabel.boundary.height() - dis, (int) areaLabel.x
					+ areaLabel.boundary.width() + dis, (int) areaLabel.y + dis);

			for (int y = 0; y < refPos.length; y++) {
				if (refPos[y] != null) {

					rect2 = new android.graphics.Rect((int) refPos[y].x,
							(int) (refPos[y].y - refPos[y].height),
							(int) (refPos[y].x + refPos[y].width), (int) (refPos[y].y));

					if (android.graphics.Rect.intersects(rect2, rect1)) {
						refPos[y] = null;
					}
				}
			}
		}

		dependencyCache.removeReferencePointsFromDependencyCache(refPos);
	}

	/**
	 * This method removes the area labels, that are not visible in the actual tile.
	 * 
	 * @param areaLabels
	 *            area Labels from the actual tile
	 */
	private void removeOutOfTileAreaLabels(ArrayList<PointTextContainer> areaLabels) {
		for (int i = 0; i < areaLabels.size(); i++) {
			label = areaLabels.get(i);

			if (label.x > Tile.TILE_SIZE) {
				areaLabels.remove(i);

				i--;
			} else if (label.y - label.boundary.height() > Tile.TILE_SIZE) {
				areaLabels.remove(i);

				i--;
			} else if (label.x + label.boundary.width() < 0.0f) {
				areaLabels.remove(i);

				i--;
			} else if (label.y + label.boundary.height() < 0.0f) {
				areaLabels.remove(i);

				i--;
			}
		}
	}

	/**
	 * This method removes the labels, that are not visible in the actual tile.
	 * 
	 * @param labels
	 *            Labels from the actual tile
	 */
	private void removeOutOfTileLabels(ArrayList<PointTextContainer> labels) {
		for (int i = 0; i < labels.size();) {
			label = labels.get(i);

			if (label.x - label.boundary.width() / 2 > Tile.TILE_SIZE) {
				labels.remove(i);
				label = null;

			} else if (label.y - label.boundary.height() > Tile.TILE_SIZE) {
				labels.remove(i);
				label = null;

			} else if ((label.x - label.boundary.width() / 2 + label.boundary.width()) < 0.0f) {
				labels.remove(i);
				label = null;

			} else if (label.y < 0.0f) {
				labels.remove(i);
				label = null;

			} else {
				i++;
			}
		}
	}

	/**
	 * This method removes the Symbols, that are not visible in the actual tile.
	 * 
	 * @param symbols
	 *            Symbols from the actual tile
	 */
	private void removeOutOfTileSymbols(ArrayList<SymbolContainer> symbols) {
		for (int i = 0; i < symbols.size();) {
			smb = symbols.get(i);

			if (smb.x > Tile.TILE_SIZE) {
				symbols.remove(i);

			} else if (smb.y > Tile.TILE_SIZE) {
				symbols.remove(i);

			} else if (smb.x + smb.symbol.getWidth() < 0.0f) {
				symbols.remove(i);

			} else if (smb.y + smb.symbol.getHeight() < 0.0f) {
				symbols.remove(i);

			} else {
				i++;
			}
		}
	}

	/**
	 * This method removes all the area labels, that overlap each other. So that the output is
	 * collision free
	 * 
	 * @param areaLabels
	 *            area labels from the actual tile
	 */
	private void removeOverlappingAreaLabels(ArrayList<PointTextContainer> areaLabels) {
		int dis = this.labelDistanceToLabel;

		for (int x = 0; x < areaLabels.size(); x++) {
			label = areaLabels.get(x);
			rect1 = new android.graphics.Rect((int) label.x - dis, (int) label.y - dis,
					(int) (label.x + label.boundary.width()) + dis, (int) (label.y
							+ label.boundary.height() + dis));

			for (int y = x + 1; y < areaLabels.size(); y++) {
				if (y != x) {
					label = areaLabels.get(y);
					rect2 = new android.graphics.Rect((int) label.x, (int) label.y,
							(int) (label.x + label.boundary.width()),
							(int) (label.y + label.boundary.height()));

					if (android.graphics.Rect.intersects(rect1, rect2)) {
						areaLabels.remove(y);

						y--;
					}
				}
			}
		}
	}

	/**
	 * Removes the the symbols that overlap with area labels.
	 * 
	 * @param symbols
	 *            list of symbols
	 * @param pTC
	 *            list of labels
	 */
	private void removeOverlappingSymbolsWithAreaLabels(ArrayList<SymbolContainer> symbols,
			ArrayList<PointTextContainer> pTC) {
		int dis = labelDistanceToSymbol;

		for (int x = 0; x < pTC.size(); x++) {
			label = pTC.get(x);

			rect1 = new android.graphics.Rect((int) label.x - dis,
					(int) (label.y - label.boundary.height()) - dis, (int) (label.x
							+ label.boundary.width() + dis), (int) (label.y + dis));

			for (int y = 0; y < symbols.size(); y++) {
				smb = symbols.get(y);

				rect2 = (new android.graphics.Rect((int) smb.x, (int) smb.y,
						(int) (smb.x + smb.symbol.getWidth()), (int) (smb.y + smb.symbol
								.getHeight())));

				if (android.graphics.Rect.intersects(rect1, rect2)) {
					symbols.remove(y);
					y--;
				}
			}
		}
	}

	int getlabelDistanceToLabel() {
		return this.labelDistanceToLabel;
	}

	int getLabelDistanceToSymbol() {
		return labelDistanceToSymbol;
	}

	int getPlacementOption() {
		return placementOption;
	}

	int getstartDistanceToSymbols() {
		return this.startDistanceToSymbols;
	}

	int getsymbolDistanceToSymbol() {
		return this.symbolDistanceToSymbol;
	}

	/**
	 * The inputs are all the label and symbol objects of the current tile. The output is
	 * overlap free label and symbol placement with the greedy strategy. The placement model is
	 * either the two fixed point or the four fixed point model.
	 * 
	 * @param labels
	 *            labels from the current tile.
	 * @param symbols
	 *            symbols of the current tile.
	 * @param areaLabels
	 *            area labels from the current tile.
	 * @param cT
	 *            current tile with the x,y- coordinates and the zoom level.
	 * @return the processed list of labels.
	 */
	ArrayList<PointTextContainer> placeLabels(ArrayList<PointTextContainer> labels,
			ArrayList<SymbolContainer> symbols, ArrayList<PointTextContainer> areaLabels,
			Tile cT) {
		ArrayList<PointTextContainer> returnLabels = labels;
		if (!DEFAULT) {
			dependencyCache.generateTileAndDependencyOnTile(cT);

			preprocessAreaLabels(areaLabels);

			preprocessLabels(returnLabels);

			preprocessSymbols(symbols);

			removeEmptySymbolReferences(returnLabels, symbols);

			removeOverlappingSymbolsWithAreaLabels(symbols, areaLabels);

			dependencyCache.removeOverlappingObjectsWithDependencyOnTile(returnLabels,
					areaLabels, symbols);

			if (returnLabels.size() != 0) {
				switch (this.placementOption) {
					case 0:
						returnLabels = processTwoPointGreedy(returnLabels, symbols, areaLabels);
						break;
					case 1:
						returnLabels = processFourPointGreedy(returnLabels, symbols, areaLabels);
						break;
					default:
						break;
				}
			}

			dependencyCache.fillDependencyOnTile(returnLabels, symbols, areaLabels);
		} else {
			centerLabels(areaLabels);
			centerLabels2(returnLabels);
		}
		return returnLabels;
	}

	/**
	 * This method removes all the Symbols, that overlap each other. So that the output is
	 * collision free.
	 * 
	 * @param symbols
	 *            symbols from the actual tile
	 */
	void removeOverlappingSymbols(ArrayList<SymbolContainer> symbols) {
		int dis = this.symbolDistanceToSymbol;

		for (int x = 0; x < symbols.size(); x++) {
			smb = symbols.get(x);
			rect1 = new android.graphics.Rect((int) smb.x - dis, (int) smb.y - dis, (int) smb.x
					+ smb.symbol.getWidth() + dis, (int) smb.y + smb.symbol.getHeight() + dis);

			for (int y = x + 1; y < symbols.size(); y++) {
				if (y != x) {
					smb = symbols.get(y);
					rect2 = (new android.graphics.Rect((int) smb.x, (int) smb.y, (int) smb.x
							+ smb.symbol.getWidth(), (int) smb.y + smb.symbol.getHeight()));

					if (android.graphics.Rect.intersects(rect2, rect1)) {
						symbols.remove(y);
						y--;
					}
				}
			}
		}
	}

	void setlabelDistanceToLabel(int labelDistanceToLabel) {
		this.labelDistanceToLabel = labelDistanceToLabel;
	}

	void setLabelDistanceToSymbol(int labelDistanceToSymbol) {
		this.labelDistanceToSymbol = labelDistanceToSymbol;
	}

	void setstartDistanceToSymbols(int startDistanceToSymbols) {
		this.startDistanceToSymbols = startDistanceToSymbols;
	}

	void setsymbolDistanceToSymbol(int symbolDistanceToSymbol) {
		this.symbolDistanceToSymbol = symbolDistanceToSymbol;
	}
}