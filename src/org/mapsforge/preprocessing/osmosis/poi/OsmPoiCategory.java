package org.mapsforge.preprocessing.osmosis.poi;

import java.util.Map;

interface OsmPoiCategory {

	public Map<String, String> keyValueList();

	public String uniqueTitle();

	public String parentUniqueTitle();

	boolean matchesTags(Map<String, String> tagList);

	boolean emtpyTaglist();

}
