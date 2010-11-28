package org.mapsforge.preprocessing.osmosis.poi;

import java.util.HashMap;
import java.util.Map;

class TagListBuilder {

	private final HashMap<String, String> tagList = new HashMap<String, String>();

	public TagListBuilder addTag(String key, String value) {
		tagList.put(key, value);
		return this;
	}

	public Map<String, String> tagList() {
		return tagList;
	}

}
