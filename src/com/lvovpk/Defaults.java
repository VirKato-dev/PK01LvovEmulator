package com.lvovpk;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Emulator defaults
 */
class Defaults {

	static Dictionary<String, String> cfg;

	// -----------------------------------------------------------------------------
	static {
		cfg = new Hashtable<String, String>();

		cfg.put("BiosFile", "bios.gz");
		cfg.put("DumpFile", "boot.lvd");
		cfg.put("HALT_ON_INVALID", "Yes");
		cfg.put("Speaker", "80");
		cfg.put("SpeakMode", "2");
		cfg.put("SpeakSlow", "No");
		cfg.put("Printer", "<none>");
		cfg.put("Mode", "4");
		cfg.put("Sync", "Yes");
		cfg.put("CpuTicks", "25000"); // Cause 2.5MHz and 50 Fps

		// -----------------------------------------------------------------------------
		// Keyboard
		// -----------------------------------------------------------------------------

		// Base matrix:

		cfg.put("VK_SHIFT", "70FF");
		cfg.put("VK_Z", "71FF");
		cfg.put("VK_X", "72FF");
		cfg.put("VK_M", "73FF");
		cfg.put("VK_N", "74FF");
		cfg.put("VK_B", "75FF");
		cfg.put("VK_V", "76FF");
		cfg.put("VK_C", "77FF");

		cfg.put("VK_x_C0", "60FF");
		cfg.put("VK_CONTROL", "61FF");
		cfg.put("VK_CAPS_LOCK", "62FF");
		cfg.put("VK_G", "63FF");
		cfg.put("VK_F", "64FF");
		cfg.put("VK_D", "65FF");
		cfg.put("VK_S", "66FF");
		cfg.put("VK_A", "67FF");

		cfg.put("VK_?", "50FF");
		cfg.put("VK_?", "51FF");
		cfg.put("VK_Q", "52FF");
		cfg.put("VK_Y", "53FF");
		cfg.put("VK_T", "54FF");
		cfg.put("VK_R", "55FF");
		cfg.put("VK_E", "56FF");
		cfg.put("VK_W", "57FF");

		cfg.put("VK_F1", "40FF");
		cfg.put("VK_F2", "41FF");
		cfg.put("VK_F3", "42FF");
		cfg.put("VK_5", "43FF");
		cfg.put("VK_4", "44FF");
		cfg.put("VK_3", "45FF");
		cfg.put("VK_2", "46FF");
		cfg.put("VK_1", "47FF");

		cfg.put("VK_SPACE", "30FF");
		cfg.put("VK_x_BC", "31FF");
		cfg.put("VK_x_BE", "32FF");
		cfg.put("VK_x_5D", "33FF");
		cfg.put("VK_x_DD", "34FF");
		cfg.put("VK_ALT", "35FF");
		cfg.put("VK_x_DC", "36FF");
		cfg.put("VK_x_BF", "37FF");

		cfg.put("VK_H", "20FF");
		cfg.put("VK_J", "21FF");
		cfg.put("VK_K", "22FF");
		cfg.put("VK_BACK_SPACE", "23FF");
		cfg.put("VK_DELETE", "24FF");
		cfg.put("VK_x_DE", "25FF");
		cfg.put("VK_x_BA", "26FF");
		cfg.put("VK_L", "27FF");

		cfg.put("VK_U", "10FF");
		cfg.put("VK_I", "11FF");
		cfg.put("VK_O", "12FF");
		cfg.put("VK_ENTER", "13FF");
		cfg.put("VK_NUM_LOCK", "14FF");
		cfg.put("VK_INSERT", "15FF");
		cfg.put("VK_x_DB", "16FF");
		cfg.put("VK_P", "17FF");

		cfg.put("VK_6", "00FF");
		cfg.put("VK_7", "01FF");
		cfg.put("VK_8", "02FF");
		cfg.put("VK_PAGE_UP", "03FF");
		cfg.put("VK_TAB", "04FF");
		cfg.put("VK_x_BB", "05FF");
		cfg.put("VK_0", "06FF");
		cfg.put("VK_9", "07FF");

		// Extended matrix:

		cfg.put("VK_RIGHT", "FF30");
		cfg.put("VK_UP", "FF31");
		cfg.put("VK_LEFT", "FF32");
		cfg.put("VK_DOWN", "FF33");

		cfg.put("VK_HOME", "FF20");
		cfg.put("VK_CLEAR", "FF21");
		cfg.put("VK_SCROLL_LOCK", "FF22");
		cfg.put("VK_F12", "FF23");

		cfg.put("VK_F8", "FF10");
		cfg.put("VK_F9", "FF11");
		cfg.put("VK_F10", "FF12");
		cfg.put("VK_F11", "FF13");

		cfg.put("VK_F7", "FF00");
		cfg.put("VK_F6", "FF01");
		cfg.put("VK_F5", "FF02");
		cfg.put("VK_F4", "FF03");

		// Bonus assignment:

		cfg.put("VK_PAGE_DOWN", "03FF");
		cfg.put("VK_x_BD", "05FF");
		cfg.put("VK_END", "FF20");
		cfg.put("VK_PAUSE", "FF22");
		cfg.put("VK_PRINTSCREEN", "FF22");

		cfg.put("VK_ADD", "60FF");
		cfg.put("VK_MULTIPLY", "15FF");
		cfg.put("VK_SUBTRACT", "05FF");
		cfg.put("VK_DIVIDE", "36FF");
	}

	// -----------------------------------------------------------------------------
}
