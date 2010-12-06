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
package org.mapsforge.android.maps;

import android.util.Log;

/**
 * Class used for logging text to the console.
 */
final class Logger {
	/**
	 * Log a simple string message with debug level.
	 * 
	 * @param str
	 *            the log message to be printed.
	 */
	static void d(String str) {
		Log.d("osm", Thread.currentThread().getName() + ": " + str);
	}

	/**
	 * Log an exception message and its complete stack trace.
	 * 
	 * @param e
	 *            the exception which should be printed.
	 */
	static void e(Exception e) {
		StringBuilder stringBuilder = new StringBuilder(512);
		stringBuilder.append("Exception in thread \"" + Thread.currentThread().getName()
				+ "\" " + e.toString());
		StackTraceElement[] stack = e.getStackTrace();
		for (int i = 0; i < stack.length; ++i) {
			stringBuilder.append("\n\tat ").append(stack[i].getMethodName()).append("(")
					.append(stack[i].getFileName()).append(":")
					.append(stack[i].getLineNumber()).append(")");
		}
		Log.e("osm", stringBuilder.toString());
	}

	/**
	 * Empty private constructor to prevent object creation.
	 */
	private Logger() {
		// do nothing
	}
}