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
package org.mapsforge.poi.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.garret.perst.FieldIndex;
import org.garret.perst.Rectangle;
import org.garret.perst.Storage;
import org.mapsforge.poi.PoiCategory;
import org.mapsforge.poi.PointOfInterest;

class PerstMRtreePersistenceManager extends
		AbstractPerstPersistenceManager<PerstRootElement> {

	/**
	 * @param storageFileName
	 *            filename of the perst storage file that should be used.
	 */
	public PerstMRtreePersistenceManager(String storageFileName) {
		super(storageFileName);
	}

	@Override
	public boolean insertCategory(PoiCategory category) {
		if (!categoryManager.contains(category.getTitle())) {
			if (super.insertCategory(category)) {
				root.addSpatialIndex(db, category.getTitle());
				return true;
			}
		}
		return false;
	}

	@Override
	public void insertPointOfInterest(PointOfInterest poi) {
		if (poi == null)
			throw new NullPointerException();

		PerstCategory category = categoryManager.get(poi.getCategory().getTitle());

		if (category == null) {
			throw new IllegalArgumentException("POI of unknown category, insert category first");
		}

		PerstPoi perstPoi = new PerstPoi(poi, category);
		root.poiIntegerIdPKIndex.put(perstPoi);
		root.poiCategoryFkIndex.put(perstPoi);

		Collection<PoiCategory> categories = categoryManager.ancestors(perstPoi.category.title);

		for (PoiCategory cat : categories) {
			root.getSpatialIndex(cat.getTitle()).put(
					new Rectangle(perstPoi.latitude, perstPoi.longitude, perstPoi.latitude,
							perstPoi.longitude),
					perstPoi);
		}
		db.store(perstPoi);
	}

	@Override
	protected Collection<PointOfInterest> find(Rect rect, String category, int limit) {
		if (!categoryManager.contains(category)) {
			return new ArrayList<PointOfInterest>(0);
		}

		ArrayList<PointOfInterest> result = new ArrayList<PointOfInterest>();

		Rectangle rectangle = new Rectangle(rect.top, rect.left, rect.bottom, rect.right);

		ArrayList<PerstPoi> pois = root.getSpatialIndex(category).getList(rectangle);

		// TODO: order pois by distance asc;

		int max = limit <= 0 ? Integer.MAX_VALUE : limit;

		if (max >= pois.size()) {
			result.addAll(pois);
		} else {
			result.addAll(pois.subList(0, max));
		}

		return result;
	}

	@Override
	public void removeCategory(PoiCategory category) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePointOfInterest(PointOfInterest poi) {
		removePointOfInterest(root.poiIntegerIdPKIndex.get(poi.getId()));
	}

	private void removePointOfInterest(PerstPoi perstPoi) {
		if (perstPoi == null)
			return;

		root.poiIntegerIdPKIndex.remove(perstPoi);

		Collection<PoiCategory> categories = categoryManager.ancestors(perstPoi.category.title);
		for (PoiCategory category : categories) {
			root.getSpatialIndex(category.getTitle()).remove(
					new Rectangle(perstPoi.latitude, perstPoi.longitude, perstPoi.latitude,
							perstPoi.longitude),
					perstPoi);
		}

		root.poiCategoryFkIndex.remove(perstPoi);
		db.deallocate(perstPoi);
	}

	@Override
	public Collection<PoiCategory> allCategories() {
		return categoryManager.allCategories();
	}

	@Override
	public void clusterStorage() {
		if (categoryManager.get("Root") == null) {
			throw new UnsupportedOperationException(
					"This only works for PersistenceManager that have a root Category 'Root'");
		}

		PerstMRtreePersistenceManager destinationManager = new PerstMRtreePersistenceManager(
				fileName + ".clustered");

		// create temporary index for cluster value
		FieldIndex<ClusterEntry> clusterIndex = createClusterIndex(root.poiIntegerIdPKIndex
				.iterator());

		Collection<PoiCategory> categories = categoryManager.allCategories();

		for (PoiCategory category : categories) {
			destinationManager.insertCategory(category);
		}

		Iterator<ClusterEntry> clusterIterator = clusterIndex.iterator();
		while (clusterIterator.hasNext()) {
			destinationManager.insertPointOfInterest(clusterIterator.next().poi);
		}

		System.out.println(destinationManager.root.getSpatialIndex("Root").size() + " =? "
				+ root.getSpatialIndex("Root").size());

		destinationManager.close();

		clusterIndex.clear();
		clusterIndex.deallocate();
	}

	@Override
	public void packIndex() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected PerstRootElement initRootElement(Storage database) {
		return new PerstRootElement(database);
	}

}
