package com.lvovpk;

/**
 * Lvov BIOS v2.0 Description v3.0
 */
class Bios {

	// -----------------------------------------------------------------------------
	// Image file with BIOS described here is just
	// an ordinary binary .LVT file that will be loaded on startup.
	// btw, BIOS checksum is 1DF200

	// This is the stack pointer after the loading any program

	final static int BasicStack = 0xAFC1;

	// This is the hot-restart entry for the any Basic _command_

	final static int BasicHotEntry = 0x02FD;

	// This is the Basic variable that contains the beginning of
	// the Basic program being loaded (value may grows after ran of
	// plugins, I know some of them)

	final static int BasicProgBegin = 0x0243;

	// This is the end of the loaded Basic program. Basic will use that
	// variable for internal purposes.

	final static int BasicProgEnd = 0x0245;

	// This is the break point on binary loading routine, this allows
	// You to load multipart/overlayed programs

	final static int LoadBinaryBpx = 0xDD94;

	// This is the run address variable for the binary loading routine

	final static int LoadBinaryEntry = 0xBEA9;

	// This is the delta variable for the binary loading routine

	final static int LoadBinaryOfs = 0xBEAB;

	// -----------------------------------------------------------------------------
}
