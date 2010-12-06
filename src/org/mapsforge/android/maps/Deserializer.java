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

/**
 * This static class converts byte arrays to numbers. Byte order is big-endian.
 */
final class Deserializer {
	/**
	 * Converts five bytes of a byte array to a long number.
	 * 
	 * @param buffer
	 *            the byte array.
	 * @param offset
	 *            the offset in the array.
	 * @return the long value.
	 */
	static long fiveBytesToLong(byte[] buffer, int offset) {
		return (buffer[offset] & 0xffL) << 32 | (buffer[offset + 1] & 0xffL) << 24
				| (buffer[offset + 2] & 0xffL) << 16 | (buffer[offset + 3] & 0xffL) << 8
				| (buffer[offset + 4] & 0xffL);
	}

	/**
	 * Converts three bytes of a byte array to a signed int number.
	 * 
	 * @param buffer
	 *            the byte array.
	 * @param offset
	 *            the offset in the array.
	 * @return the int value.
	 */
	static int threeBytesToSignedInt(byte[] buffer, int offset) {
		// check the sign bit
		if ((buffer[offset] & 0x80) == 0) {
			// positive number
			return buffer[offset] << 16 | (buffer[offset + 1] & 0xff) << 8
					| (buffer[offset + 2] & 0xff);
		}
		// negative number
		return 0xff800000 | (buffer[offset] & 0x7F) << 16 | (buffer[offset + 1] & 0xff) << 8
				| (buffer[offset + 2] & 0xff);
	}

	/**
	 * Converts four bytes of a byte array to an int number.
	 * 
	 * @param buffer
	 *            the byte array.
	 * @param offset
	 *            the offset in the array.
	 * @return the int value.
	 */
	static int toInt(byte[] buffer, int offset) {
		return buffer[offset] << 24 | (buffer[offset + 1] & 0xff) << 16
				| (buffer[offset + 2] & 0xff) << 8 | (buffer[offset + 3] & 0xff);
	}

	/**
	 * Converts eight bytes of a byte array to a long number.
	 * 
	 * @param buffer
	 *            the byte array.
	 * @param offset
	 *            the offset in the array.
	 * @return the long value.
	 */
	static long toLong(byte[] buffer, int offset) {
		return (long) buffer[offset] << 56 | (buffer[offset + 1] & 0xffL) << 48
				| (buffer[offset + 2] & 0xffL) << 40 | (buffer[offset + 3] & 0xffL) << 32
				| (buffer[offset + 4] & 0xffL) << 24 | (buffer[offset + 5] & 0xffL) << 16
				| (buffer[offset + 6] & 0xffL) << 8 | (buffer[offset + 7] & 0xffL);
	}

	/**
	 * Converts two bytes of a byte array to a short number.
	 * 
	 * @param buffer
	 *            the byte array.
	 * @param offset
	 *            the offset in the array.
	 * @return the short value.
	 */
	static short toShort(byte[] buffer, int offset) {
		return (short) (buffer[offset] << 8 | (buffer[offset + 1] & 0xff));
	}

	/**
	 * Empty private constructor to prevent object creation.
	 */
	private Deserializer() {
		// do nothing
	}
}