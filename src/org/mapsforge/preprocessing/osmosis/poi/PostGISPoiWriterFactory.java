package org.mapsforge.preprocessing.osmosis.poi;

import org.openstreetmap.osmosis.core.pipeline.common.TaskConfiguration;
import org.openstreetmap.osmosis.core.pipeline.common.TaskManager;
import org.openstreetmap.osmosis.core.pipeline.v0_6.DatasetSinkManager;

class PostGISPoiWriterFactory extends PoiWriterFactory {

	@Override
	protected TaskManager createTaskManagerImpl(TaskConfiguration taskConfig) {
		PoiWriter task;

		handleArguments(taskConfig);
		task = new PoiWriter(persistenceManager, categories);

		return new DatasetSinkManager(taskConfig.getId(), task, taskConfig.getPipeArgs());
	}

}
