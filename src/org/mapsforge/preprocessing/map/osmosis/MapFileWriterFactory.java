package org.mapsforge.preprocessing.map.osmosis;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.pipeline.v0_6.SinkManager;

class MapFileWriterFactory extends TaskManagerFactory {

	private static final String DEFAULT_PARAM_OUTFILE = "mapsforge.map";
	private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime()
			.availableProcessors();

	private static final String PARAM_OUTFILE = "file";
	private static final String PARAM_BBOX = "bbox";
	private static final String PARAM_ZOOMINTERVAL_CONFIG = "zoom-interval-conf";
	private static final String PARAM_COMMENT = "comment";
	private static final String PARAM_MAP_START_POSITION = "map-start-position";
	private static final String PARAM_DEBUG_INFO = "debug";
	private static final String PARAM_WAYNODE_COMPRESSION = "waynode-compression";
	private static final String PARAM_PIXEL_FILTER = "pixel-filter";
	private static final String PARAM_POLYGON_CLIPPING = "polygon-clipping";
	private static final String PARAM_THREAD_POOL_SIZE = "thread-pool-size";

	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {

		String outfile = getStringArgument(taskConfig, PARAM_OUTFILE, DEFAULT_PARAM_OUTFILE);
		String mapStartPosition = getStringArgument(taskConfig, PARAM_MAP_START_POSITION, null);
		String bbox = getStringArgument(taskConfig, PARAM_BBOX, null);
		String zoomConf = getStringArgument(taskConfig, PARAM_ZOOMINTERVAL_CONFIG, null);
		String comment = getStringArgument(taskConfig, PARAM_COMMENT, null);
		boolean debug = getBooleanArgument(taskConfig, PARAM_DEBUG_INFO, false);
		boolean waynodeCompression = getBooleanArgument(taskConfig, PARAM_WAYNODE_COMPRESSION,
				true);
		boolean pixelFilter = getBooleanArgument(taskConfig, PARAM_PIXEL_FILTER, true);
		boolean polygonClipping = getBooleanArgument(taskConfig, PARAM_POLYGON_CLIPPING, true);
		int threadpoolSize = getIntegerArgument(taskConfig, PARAM_THREAD_POOL_SIZE,
				DEFAULT_THREAD_POOL_SIZE);
		// String zoomIntervalConfiguration = getStringArgument(taskConfig,
		// PARAM_ZOOMINTERVAL_CONFIG, null);
		MapFileWriterTask task = new MapFileWriterTask(outfile, bbox, mapStartPosition,
				comment, zoomConf, debug, waynodeCompression, pixelFilter, polygonClipping,
				threadpoolSize);
		return new SinkManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
	}

}
