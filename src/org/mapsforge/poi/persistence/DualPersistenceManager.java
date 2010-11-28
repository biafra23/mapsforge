package org.mapsforge.poi.persistence;

import java.util.Collection;

import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.poi.PoiCategory;
import org.mapsforge.poi.PointOfInterest;
import org.mapsforge.poi.exchange.IPoiReader;

class DualPersistenceManager implements IPersistenceManager {

	private final PostGisPersistenceManager pgManager;
	private final MultiRtreePersistenceManager perstManager;

	public DualPersistenceManager(PostGisPersistenceManager pgManager,
			MultiRtreePersistenceManager perstManager) {
		this.pgManager = pgManager;
		this.perstManager = perstManager;
	}

	@Override
	public Collection<PoiCategory> allCategories() {
		return perstManager.allCategories();
	}

	@Override
	public void close() {
		pgManager.close();
		perstManager.close();
	}

	@Override
	public Collection<PoiCategory> descendants(String category) {
		return perstManager.descendants(category);
	}

	@Override
	public boolean insertCategory(PoiCategory category) {
		pgManager.insertCategory(category);
		perstManager.insertCategory(category);
		return true;
	}

	@Override
	public void insertPointOfInterest(PointOfInterest poi) {
		perstManager.insertPointOfInterest(poi);
		pgManager.insertPointOfInterest(poi);
	}

	@Override
	public void insertPointsOfInterest(Collection<PointOfInterest> pois) {
		perstManager.insertPointsOfInterest(pois);
		pgManager.insertPointsOfInterest(pois);
	}

	@Override
	public void insertPointsOfInterest(IPoiReader poiReader) {
		perstManager.insertPointsOfInterest(poiReader);
		pgManager.insertPointsOfInterest(poiReader);
	}

	@Override
	public void removeCategory(PoiCategory category) {
		perstManager.removeCategory(category);
		pgManager.removeCategory(category);
	}

	@Override
	public void removePointOfInterest(PointOfInterest poi) {
		perstManager.removePointOfInterest(poi);
		pgManager.removePointOfInterest(poi);
	}

	@Override
	public Collection<PointOfInterest> findInRect(GeoCoordinate p1, GeoCoordinate p2,
			String categoryName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<PointOfInterest> findNearPosition(GeoCoordinate point, int distance,
			String categoryName, int limit) {
		return perstManager.findNearPosition(point, distance, categoryName, limit);
	}

	@Override
	public PointOfInterest getPointById(long poiId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reopen() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clusterStorage() {
		perstManager.clusterStorage();
	}

	@Override
	public void packIndex() {
		throw new UnsupportedOperationException();
	}

}
