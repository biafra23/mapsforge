package org.mapsforge.preprocessing.osmosis.poi;

import java.util.Map;
import java.util.Map.Entry;

class BasePoiCategory implements OsmPoiCategory {

	private final Map<String, String> keyValueList;
	private final String uniqueTitle;
	private final String parentUniqueTitle;

	public BasePoiCategory(String uniqueTitle, Map<String, String> keyValueList,
			String parentUniqueTitle) {
		this.keyValueList = keyValueList;
		this.uniqueTitle = uniqueTitle;
		this.parentUniqueTitle = parentUniqueTitle;
	}

	@Override
	public Map<String, String> keyValueList() {
		return keyValueList;
	}

	@Override
	public String uniqueTitle() {
		return uniqueTitle;
	}

	@Override
	public String parentUniqueTitle() {
		return parentUniqueTitle;
	}

	@Override
	public boolean matchesTags(Map<String, String> tagList) {
		for (Entry<String, String> entry : keyValueList.entrySet()) {
			if (!keyValueList.get(entry.getKey()).equalsIgnoreCase(tagList.get(entry.getKey()))) {
				return false;

			}
		}
		return true;
	}

	@Override
	public String toString() {
		return new StringBuilder().append(uniqueTitle)
									.append(" : ")
									.append("parent=")
									.append(parentUniqueTitle)
									.toString();
	}

	@Override
	public boolean emtpyTaglist() {
		return (keyValueList == null || keyValueList.isEmpty());
	}
}
