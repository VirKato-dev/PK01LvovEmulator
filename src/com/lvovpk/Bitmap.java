package com.lvovpk;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Windows Bitmaps Handling Primitives
 */
public class Bitmap {

	// -----------------------------------------------------------------------------
	// % 8 dots per row !
	public static void save4(OutputStream to, byte[] body, int x, int y, byte[][] pal, int c) throws IOException {
		final int sizeFileHdr = 14;
		final int sizeInfoHdr = 40;

		final int sizeInfo = c * 4 + sizeInfoHdr + sizeFileHdr;
		final int sizeFile = x * y / 2 + sizeInfo;

		// File Header
		Utils.dumpBytes(to, "BM"); // Signature
		Utils.dumpDWord(to, sizeFile); // File Size
		Utils.dumpWord(to, 0); // Reserved1
		Utils.dumpWord(to, 0); // Reserved2
		Utils.dumpDWord(to, sizeInfo); // Bits Offset

		// Info Header
		Utils.dumpDWord(to, sizeInfoHdr); // Size of Info Header
		Utils.dumpDWord(to, x); // Width of bitmap
		Utils.dumpDWord(to, y); // Height of bitmap
		Utils.dumpWord(to, 1); // Number of bit planes per item
		Utils.dumpWord(to, 4); // Number of bits count per item
		Utils.dumpDWord(to, 0); // Type of compression
		Utils.dumpDWord(to, 0); // Size of image in bytes (0 for RGB)
		Utils.dumpDWord(to, 0); // Pixels per meter on X
		Utils.dumpDWord(to, 0); // Pixels per meter on Y
		Utils.dumpDWord(to, c); // Number of used colors
		Utils.dumpDWord(to, c); // ... of them are important

		// Palette
		for (int i = 0; i < c; i++) {
			Utils.dumpByte(to, pal[2][i]); // B
			Utils.dumpByte(to, pal[1][i]); // G
			Utils.dumpByte(to, pal[0][i]); // R
			Utils.dumpByte(to, 0); // Q
		}

		// Image
		int p, pp, yInd;
		for (pp = x * y, yInd = 0; yInd < y; yInd++, pp -= x) {
			for (p = pp - x; p < pp; p += 2) {
				Utils.dumpByte(to, body[p + 1] + (body[p] << 4));
			}
		}
	}

	// -----------------------------------------------------------------------------
}
