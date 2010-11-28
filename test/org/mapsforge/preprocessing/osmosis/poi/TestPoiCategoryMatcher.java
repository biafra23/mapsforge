package org.mapsforge.preprocessing.osmosis.poi;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

/**
 * Used to test {@link PoiCategoryMatcher} class.
 * 
 * @author weise
 * 
 */
public class TestPoiCategoryMatcher {

	ArrayList<OsmPoiCategory> categories = new ArrayList<OsmPoiCategory>();

	/**
	 * setup categories list for use in unit tests.
	 */
	@Before
	public void setupCategories() {
		categories.add(new BasePoiCategory("Food",
				new TagListBuilder().tagList(),
				null));

		categories.add(new BasePoiCategory("Restaurant",
				new TagListBuilder().addTag("amenity", "restaurant").tagList(),
				"Food"));

		categories.add(new BasePoiCategory("Italian Restaurant",
				new TagListBuilder().addTag("amenity", "restaurant")
									.addTag("cuisine", "italian").tagList(),
				"Restaurant"));

		categories.add(new BasePoiCategory("Steak House",
				new TagListBuilder().addTag("amenity", "restaurant")
									.addTag("cuisine", "steak_house").tagList(),
				"Restaurant"));

		categories.add(new BasePoiCategory("Fast Food",
				new TagListBuilder().addTag("amenity", "fast_food").tagList(),
				"Food"));

		categories.add(new BasePoiCategory("Bar",
				new TagListBuilder().addTag("amenity", "bar").tagList(),
				null));

		categories.add(new BasePoiCategory("SportsBar",
				new TagListBuilder().addTag("amenity", "bar")
									.addTag("type", "sport").tagList(),
				"Bar"));
		categories.add(new BasePoiCategory("Soccerbar",
				new TagListBuilder().addTag("amenity", "bar")
									.addTag("type", "sport")
									.addTag("sport", "soccer").tagList(),
				"SportsBar"));
		categories.add(new BasePoiCategory("Football-Bar",
				new TagListBuilder().addTag("amenity", "bar")
									.addTag("type", "sport")
									.addTag("sport", "Football").tagList(),
				"SportsBar"));

	}

	/**
	 * Check if {@link PoiCategoryMatcher} matches tags to the right category.
	 */
	@Test
	public void testFindMatch() {
		PoiCategoryMatcher matcher = new PoiCategoryMatcher(categories);

		assertEquals(null, matcher.findMatch(new HashMap<String, String>()));
		assertEquals("Restaurant", matcher.findMatch(
									new TagListBuilder().addTag("amenity", "restaurant")
											.tagList()).getTitle());
		assertEquals("Steak House", matcher.findMatch(
				new TagListBuilder().addTag("amenity", "restaurant")
									.addTag("cuisine", "steak_house")
									.tagList()).getTitle());
		assertEquals("Soccerbar", matcher.findMatch(
				new TagListBuilder().addTag("amenity", "bar")
									.addTag("type", "sport")
									.addTag("sport", "soccer").tagList()).getTitle());
		assertEquals("Soccerbar", matcher.findMatch(
				new TagListBuilder().addTag("sport", "soccer")
									.addTag("amenity", "bar")
									.addTag("type", "sport").tagList()).getTitle());
	}

}
