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
package org.mapsforge.preprocessing.routing.blockedHighwayHierarchies;

class Serializer {

	public static final int BITS_PER_BYTE = 8;
	public static final int BITS_PER_SHORT = 16;
	public static final int BITS_PER_INT = 32;
	public static final int BITS_PER_LONG = 64;

	private final static byte[] BITMAKS_BYTE_HIGH_CLEARED = new byte[] { (byte) 0xff, 0x7f,
			0x3f, 0x1f, 0x0f, 0x07, 0x03, 0x01, 0x00 };

	private final static byte[] BITMAKS_BYTE_LOW_CLEARED = new byte[] { (byte) 0xff,
			(byte) 0xfe, (byte) 0xfc, (byte) 0xf8, (byte) 0xf0, (byte) 0xe0, (byte) 0xc0,
			(byte) 0x80, 0x00 };

	private final static byte[] BUFFER = new byte[8];

	private static byte shl(byte val, int nBits) {
		return (byte) ((val & 0xff) << nBits);
	}

	private static byte shr(byte val, int nBits) {
		return (byte) ((val & 0xff) >>> nBits);
	}

	private static void shortToBytes(short val, byte[] buff) {
		buff[0] = (byte) val;
		buff[1] = (byte) (val >>> 8);
	}

	private static void intToBytes(int val, byte[] buff) {
		buff[0] = (byte) val;
		buff[1] = (byte) (val >>> 8);
		buff[2] = (byte) (val >>> 16);
		buff[3] = (byte) (val >>> 24);
	}

	private static void longToBytes(long val, byte[] buff) {
		buff[0] = (byte) val;
		buff[1] = (byte) (val >>> 8);
		buff[2] = (byte) (val >>> 16);
		buff[3] = (byte) (val >>> 24);
		buff[4] = (byte) (val >>> 32);
		buff[5] = (byte) (val >>> 40);
		buff[6] = (byte) (val >>> 48);
		buff[7] = (byte) (val >>> 56);
	}

	private static short bytesToShort(byte[] buffer) {
		return (short) ((buffer[0] & 0xff) | ((buffer[1] & 0xff) << 8));
	}

	private static int bytesToInt(byte[] buffer) {
		return (buffer[0] & 0xff) | ((buffer[1] & 0xff) << 8) | ((buffer[2] & 0xff) << 16)
				| ((buffer[3] & 0xff) << 24);
	}

	private static long bytesToLong(byte[] buffer) {
		return (buffer[0] & 0xffL) | ((buffer[1] & 0xffL) << 8) | ((buffer[2] & 0xffL) << 16)
				| ((buffer[3] & 0xffL) << 24) | ((buffer[4] & 0xffL) << 32)
				| ((buffer[5] & 0xffL) << 40) | ((buffer[6] & 0xffL) << 48)
				| ((buffer[7] & 0xffL) << 56);
	}

	private static void writeBits(byte val, int nBits, byte[] buff, int byteOffset,
			int bitOffset) {
		if (nBits + bitOffset >= BITS_PER_BYTE) {
			buff[byteOffset] &= BITMAKS_BYTE_HIGH_CLEARED[BITS_PER_BYTE - bitOffset];
			buff[byteOffset] |= shl(val, bitOffset);

			val = shr(val, BITS_PER_BYTE - bitOffset);
			nBits -= (BITS_PER_BYTE - bitOffset);
			bitOffset = 0;
			byteOffset++;
		}
		if (nBits > 0) {
			buff[byteOffset] = (byte) ((buff[byteOffset] & BITMAKS_BYTE_HIGH_CLEARED[BITS_PER_BYTE
					- bitOffset]) | (buff[byteOffset] & BITMAKS_BYTE_LOW_CLEARED[bitOffset
					+ nBits]));
			buff[byteOffset] |= shl((byte) (val & BITMAKS_BYTE_HIGH_CLEARED[BITS_PER_BYTE
					- nBits]), bitOffset);
		}
	}

	public static void writeBit(boolean val, byte[] buff, int byteOffset, int bitOffset) {
		if (val) {
			buff[byteOffset] |= shl((byte) 1, bitOffset);
		} else {
			buff[byteOffset] &= ~shl((byte) 1, bitOffset);
		}
	}

	public static void writeByte(byte val, byte[] buff, int byteOffset, int bitOffset) {
		buff[byteOffset] = (byte) ((buff[byteOffset] & BITMAKS_BYTE_HIGH_CLEARED[BITS_PER_BYTE
				- bitOffset]) | shl(val, bitOffset));
		buff[byteOffset + 1] = (byte) ((buff[byteOffset + 1] &
				BITMAKS_BYTE_LOW_CLEARED[bitOffset]) | shr(
				val, BITS_PER_BYTE - bitOffset));
	}

	public static void writeShort(short val, byte[] buff, int byteOffset, int bitOffset) {
		shortToBytes(val, BUFFER);
		writeByte(BUFFER[0], buff, byteOffset, bitOffset);
		writeByte(BUFFER[1], buff, byteOffset + 1, bitOffset);
	}

	public static void writeInt(int val, byte[] buff, int byteOffset, int bitOffset) {
		intToBytes(val, BUFFER);
		writeByte(BUFFER[0], buff, byteOffset++, bitOffset);
		writeByte(BUFFER[1], buff, byteOffset++, bitOffset);
		writeByte(BUFFER[2], buff, byteOffset++, bitOffset);
		writeByte(BUFFER[3], buff, byteOffset++, bitOffset);
	}

	public static void writeLong(long val, byte[] buff, int byteOffset, int bitOffset) {
		longToBytes(val, BUFFER);
		writeByte(BUFFER[0], buff, byteOffset++, bitOffset);
		writeByte(BUFFER[1], buff, byteOffset++, bitOffset);
		writeByte(BUFFER[2], buff, byteOffset++, bitOffset);
		writeByte(BUFFER[3], buff, byteOffset++, bitOffset);
		writeByte(BUFFER[4], buff, byteOffset++, bitOffset);
		writeByte(BUFFER[5], buff, byteOffset++, bitOffset);
		writeByte(BUFFER[6], buff, byteOffset++, bitOffset);
		writeByte(BUFFER[7], buff, byteOffset++, bitOffset);
	}

	public static void writeUInt(long val, int nBits, byte[] buff, int byteOffset, int bitOffset) {
		assert (nBits < BITS_PER_LONG);
		longToBytes(val, BUFFER);
		int j = 0;

		while (nBits >= BITS_PER_BYTE) {
			writeByte(BUFFER[j++], buff, byteOffset++, bitOffset);
			nBits -= BITS_PER_BYTE;

		}
		if (nBits > 0) {
			writeBits(BUFFER[j], nBits, buff, byteOffset, bitOffset);
		}
	}

	private static byte readBits(byte[] buff, int nBits, int byteOffset, int bitOffset) {
		if (bitOffset + nBits > BITS_PER_BYTE) {
			return (byte) (readByte(buff, byteOffset, bitOffset) & BITMAKS_BYTE_HIGH_CLEARED[BITS_PER_BYTE
					- nBits]);
		}
		return (byte) (shr(buff[byteOffset], bitOffset) & BITMAKS_BYTE_HIGH_CLEARED[BITS_PER_BYTE
				- nBits]);
	}

	public static boolean readBit(byte[] buff, int byteOffset, int bitOffset) {
		return 0 != (buff[byteOffset] & shl((byte) 1, bitOffset));
	}

	public static byte readByte(byte[] buff, int byteOffset, int bitOffset) {
		if (bitOffset == 0) {
			return buff[byteOffset];
		}
		return (byte) (shr(buff[byteOffset], bitOffset) | shl(buff[byteOffset + 1],
				BITS_PER_BYTE - bitOffset));

	}

	public static short readShort(byte[] buff, int byteOffset, int bitOffset) {
		BUFFER[0] = readByte(buff, byteOffset++, bitOffset);
		BUFFER[1] = readByte(buff, byteOffset, bitOffset);
		return bytesToShort(BUFFER);
	}

	public static int readInt(byte[] buff, int byteOffset, int bitOffset) {
		BUFFER[0] = readByte(buff, byteOffset++, bitOffset);
		BUFFER[1] = readByte(buff, byteOffset++, bitOffset);
		BUFFER[2] = readByte(buff, byteOffset++, bitOffset);
		BUFFER[3] = readByte(buff, byteOffset, bitOffset);
		return bytesToInt(BUFFER);
	}

	public static long readLong(byte[] buff, int byteOffset, int bitOffset) {
		BUFFER[0] = readByte(buff, byteOffset++, bitOffset);
		BUFFER[1] = readByte(buff, byteOffset++, bitOffset);
		BUFFER[2] = readByte(buff, byteOffset++, bitOffset);
		BUFFER[3] = readByte(buff, byteOffset++, bitOffset);
		BUFFER[4] = readByte(buff, byteOffset++, bitOffset);
		BUFFER[5] = readByte(buff, byteOffset++, bitOffset);
		BUFFER[6] = readByte(buff, byteOffset++, bitOffset);
		BUFFER[7] = readByte(buff, byteOffset, bitOffset);
		return bytesToLong(BUFFER);
	}

	public static long readUInt(byte[] buff, int nBits, int byteOffset, int bitOffset) {
		int j = 0;
		while (nBits >= BITS_PER_BYTE) {
			BUFFER[j++] = readByte(buff, byteOffset++, bitOffset);
			nBits -= 8;
		}
		if (nBits > 0) {
			BUFFER[j++] = readBits(buff, nBits, byteOffset, bitOffset);
		}
		while (j < 8) {
			BUFFER[j++] = 0;
		}
		return bytesToLong(BUFFER);
	}
}
