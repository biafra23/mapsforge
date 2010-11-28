package org.mapsforge.preprocessing.map.osmosis;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.osmosis.core.pipeline.common.TaskManagerFactory;
import org.openstreetmap.osmosis.core.plugin.PluginLoader;

/**
 * The Osmosis PluginLoader for the mapfile-writer osmosis plugin.
 * 
 * @author bross
 * 
 */
public class MapFileWriterPluginLoader implements PluginLoader {

	@Override
	public Map<String, TaskManagerFactory> loadTaskFactories() {
		MapFileWriterFactory mapFileWriterFactory = new MapFileWriterFactory();
		HashMap<String, TaskManagerFactory> map = new HashMap<String, TaskManagerFactory>();
		map.put("mapfile-writer", mapFileWriterFactory);
		map.put("mw", mapFileWriterFactory);
		return map;
	}

}
