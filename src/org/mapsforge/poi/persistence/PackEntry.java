package org.mapsforge.poi.persistence;

/**
 * Used when packing indexes implementing {@link RtreeIndex}.
 * 
 * @author weise
 * 
 * @param <S>
 *            shape class implementing {@link SpatialShape}.
 * @param <T>
 *            indexed item.
 */
class PackEntry<S extends SpatialShape<S>, T> {
	public S shape;
	public T obj;

	public PackEntry(S shape, T obj) {
		super();
		this.shape = shape;
		this.obj = obj;
	}
}
