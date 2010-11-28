/*
 * Copyright 2010 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.android.routing.blockedHighwayHierarchies;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.Random;

public class ExternalMemorySpeedTester {

	static void createTestFile(File file, long size) throws IOException {
		byte[] content = new byte[16384];
		for (int i = 0; i < content.length; i++) {
			content[i] = (byte) (i % 256);
		}
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file),
				65536);
		while (size > 0) {
			out.write(content, 0, (int) Math.min(content.length, size));
			size -= content.length;
			System.out.println(size);
		}
		out.flush();
		out.close();
	}

	public static void measureReadPerformance(File inputFile, PrintStream out,
			int[] blockSizes,
			int numReads, int[] alignments)
			throws IOException {
		RandomAccessFile raf = new RandomAccessFile(inputFile, "r");
		Random rnd = new Random();
		DecimalFormat df1 = new DecimalFormat("#.###");
		DecimalFormat df2 = new DecimalFormat("#");
		int fileSize = (int) raf.length();

		out.print("#blocksize");
		for (int alignment : alignments) {
			out.print("\tMB/s[" + alignment + "]");
			out.print("\tBytes/s[" + alignment + "]");
		}
		out.println("");
		for (int blockSize : blockSizes) {
			long[] sumNanos = new long[alignments.length];
			for (int i = 0; i < numReads; i++) {
				for (int j = 0; j < alignments.length; j++) {
					long startAddr = (Math.abs(rnd.nextLong()) % (fileSize - blockSize) / alignments[j])
							* alignments[j];
					byte[] b = new byte[blockSize];

					long startNanos = System.nanoTime();
					raf.seek(startAddr);
					raf.readFully(b);
					long endNanos = System.nanoTime();

					sumNanos[j] += endNanos - startNanos;
				}
			}
			out.print(blockSize);
			for (int j = 0; j < alignments.length; j++) {
				int avgNanos = (int) (sumNanos[j] / numReads);
				double avgMBs = ((((double) blockSize) / ((double) avgNanos)) * Math.pow(10, 9))
						/ (1024 * 1024);
				double avgBytes = ((((double) blockSize) / ((double) avgNanos)) * Math.pow(10,
						9));
				out.print("\t" + df1.format(avgMBs));
				out.print("\t" + df2.format(avgBytes));
			}
			out.println();
		}
		out.flush();
		raf.close();
	}

	public static void main(String[] args) throws IOException {
		// File inputFile = new File(
		// "F:/video/concerts/jimi hendrix - live at woodstock (disc 1-2).iso");
		File inputFile = new File("flash2.dat");
		// createFile(inputFile, 1024L * 1024L * 1024L * 3L);
		int numReads = 100;
		int[] blockSizes = new int[] { 1024, 1024 * 2, 1024 * 4, 1024 * 8, 1024 * 16,
				1024 * 32, 1024 * 64, 1024 * 128, 1024 * 256, 1024 * 512, 1024 * 1024,
				1024 * 2048, 1024 * 4096, 1024 * 8192 };
		int[] alignments = new int[] { 1, 512, 1024, 2048, 4096 };
		measureReadPerformance(inputFile, System.out, blockSizes, numReads, alignments);
	}
}
