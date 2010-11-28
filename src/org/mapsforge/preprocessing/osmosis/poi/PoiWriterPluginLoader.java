package org.mapsforge.preprocessing.osmosis.poi;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;

/**
 * Exposes the two command line arguments "xml-poiWriter" and "pg-poiWriter" to osmosis script.
 * 
 * @author weise
 * 
 */
public class PoiWriterPluginLoader implements PluginLoader {

	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		HashMap<String, TaskManagerFactory> map = new HashMap<String, TaskManagerFactory>();
		map.put("xml-poiWriter", new XMLPoiWriterFactory());
		map.put("pg-poiWriter", new PostGISPoiWriterFactory());
		return map;
	}

}
