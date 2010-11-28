package org.mapsforge.poi;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import org.garret.perst.Decimal;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.poi.persistence.IPersistenceManager;
import org.mapsforge.poi.persistence.PersistenceManagerFactory;

/**
 * Used to benchmark poi queries. Only really useful with patched perst, otherwise it is
 * impossible to count page loads.
 * 
 * @author weise
 * 
 */
public class Benchmark {

	class Series {
		String category;
		int radius;
		int nodes;
		int pages;
		int nonNodePages;
		int found;
		int time;

		String stringifyAverage(int value) {
			return new Decimal(new Double(value) / points.length, 10, 2).toString().replace(
					'.', ',');
		}

		String[] getAverageValues() {
			return new String[] { category, "" + radius, stringifyAverage(nodes),
					stringifyAverage(pages), stringifyAverage(nonNodePages),
					stringifyAverage(found), stringifyAverage(time) };
		}
	}

	static final GeoCoordinate[] points = new GeoCoordinate[] {
			new GeoCoordinate(52.456009, 13.205566), new GeoCoordinate(53.265213, 11.359863),
			new GeoCoordinate(51.481383, 7.272949), new GeoCoordinate(48.79239, 9.272461),
			new GeoCoordinate(52.469397, 13.513184), new GeoCoordinate(48.107431, 11.645508),
			new GeoCoordinate(50.916887, 6.943359), new GeoCoordinate(53.494582, 9.909668),
			new GeoCoordinate(53.585984, 10.041504), new GeoCoordinate(53.225768, 10.415039),
			new GeoCoordinate(53.067627, 8.690186) };
	static final int[] radii = new int[] { 1000, 5000, 10000, 25000, 50000, 100000 };
	static final String[] categories = new String[] { "School", "Gas Station", "Restaurant",
			"Food & Drink", "Root" };

	/**
	 * @param args
	 *            none used
	 */
	public static void main(String[] args) {
		String file = "c:/users/weise/Desktop/berlin.dbs";
		IPersistenceManager persistenceManager = PersistenceManagerFactory
				.getPerstMultiRtreePersistenceManager(file);
		Benchmark benchmark = new Benchmark(persistenceManager, file + ".csv");
		try {
			benchmark.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private IPersistenceManager persistenceManager;
	private PerstQueryTracer tracer;
	private BufferedWriter writer;

	/**
	 * @param persistenceManager
	 *            {@link IPersistenceManager} to be tested.
	 * @param outputFile
	 *            path to output file. Will be written to in csv format.
	 */
	public Benchmark(IPersistenceManager persistenceManager, String outputFile) {
		this.persistenceManager = persistenceManager;
		this.tracer = PerstQueryTracer.getInstance();
		try {
			FileWriter fileWriter = new FileWriter(outputFile);
			writer = new BufferedWriter(fileWriter);
		} catch (IOException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * Runs the benchmark
	 * 
	 * @throws IOException
	 *             if unable to write to specified output file.
	 */
	public void run() throws IOException {
		writer.write("category; radius; nodes; pages; nonNodePages; found; time\r\n");

		for (String category : categories) {
			for (int radius : radii) {
				Series series = new Series();
				series.category = category;
				series.radius = radius;
				for (GeoCoordinate point : points) {
					runSingleQuery(point, radius, category, series);
				}
				writeLine(series.getAverageValues());
			}
		}
		writer.close();
	}

	private void runSingleQuery(GeoCoordinate point, int radius, String category, Series series) {
		persistenceManager.reopen();

		tracer.start();
		Collection<PointOfInterest> pois = persistenceManager.findNearPosition(point, radius,
				category, -1);
		tracer.stop();

		series.found += pois.size();
		series.nodes += tracer.nodesTouched();
		series.pages += tracer.pagesLoaded();
		series.nonNodePages += tracer.noneNodePagesLoaded();
		series.time += tracer.queryTime();

	}

	private void writeLine(String[] values) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (String value : values) {
			sb.append(value).append("; ");
		}
		String line = sb.toString();
		writer.write(line.substring(0, line.length() - 2) + "\r\n");
	}

}
