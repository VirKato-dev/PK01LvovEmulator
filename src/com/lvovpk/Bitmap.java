package com.lvovpk;

import java.io.IOException;
import java.io.OutputStream;

/**
	Windows Bitmaps Handling Primitives
*/
public class Bitmap {

	// -----------------------------------------------------------------------------
	// % 8 dots per row !
	public static void save4 (OutputStream To, byte[] body, int X, int Y, byte[][] pal, int C) throws IOException {
		final int sizeFileHdr = 14;
		final int sizeInfoHdr = 40;

		final int sizeInfo = C * 4 + sizeInfoHdr + sizeFileHdr;
		final int sizeFile = X * Y / 2 + sizeInfo;

		// File Header
		Utils.dumpBytes(To, "BM"); // Signature
		Utils.dumpDWord(To, sizeFile); // File Size
		Utils.dumpWord(To, 0); // Reserved1
		Utils.dumpWord(To, 0); // Reserved2
		Utils.dumpDWord(To, sizeInfo); // Bits Offset

		// Info Header
		Utils.dumpDWord(To, sizeInfoHdr); // Size of Info Header
		Utils.dumpDWord(To, X); // Width of bitmap
		Utils.dumpDWord(To, Y); // Height of bitmap
		Utils.dumpWord(To, 1); // Number of bit planes per item
		Utils.dumpWord(To, 4); // Number of bits count per item
		Utils.dumpDWord(To, 0); // Type of compression
		Utils.dumpDWord(To, 0); // Size of image in bytes (0 for RGB)
		Utils.dumpDWord(To, 0); // Pixels per meter on X
		Utils.dumpDWord(To, 0); // Pixels per meter on Y
		Utils.dumpDWord(To, C); // Number of used colors
		Utils.dumpDWord(To, C); // ... of them are important

		// Palette
		for (int i = 0; i < C; i++) {
			Utils.dumpByte(To, pal[2][i]); // B
			Utils.dumpByte(To, pal[1][i]); // G
			Utils.dumpByte(To, pal[0][i]); // R
			Utils.dumpByte(To, 0); // Q
		}

		// Image
		int p, pp, y;
		for (pp = X * Y, y = 0; y < Y; y++, pp -= X) {
			for (p = pp - X; p < pp; p += 2) {
				Utils.dumpByte(To, body[p + 1] + (body[p] << 4));
			}
		}
	}

	// -----------------------------------------------------------------------------
}
