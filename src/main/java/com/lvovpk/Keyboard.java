package com.lvovpk;

import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keyboard Abstraction layer
 */
class Keyboard {

	private static final int MODIFIERS_MASK =
			KeyEvent.CTRL_DOWN_MASK |
			KeyEvent.SHIFT_DOWN_MASK |
			KeyEvent.ALT_DOWN_MASK |
			KeyEvent.ALT_GRAPH_DOWN_MASK |
			KeyEvent.META_DOWN_MASK;

	private static final int[][] SHORTCUTS = {
			{ KeyEvent.VK_M,      1, EmulatorUI.CM_MODE },
			{ KeyEvent.VK_F,      1, EmulatorUI.CM_TOGGLE_FULLSCREEN },
			{ KeyEvent.VK_L,      1, EmulatorUI.CM_LOAD },
			{ KeyEvent.VK_R,      1, EmulatorUI.CM_RESET },
			{ KeyEvent.VK_P,      1, EmulatorUI.CM_PAUSE },
			{ KeyEvent.VK_C,      1, EmulatorUI.CM_RESUME },
			{ KeyEvent.VK_I,      1, EmulatorUI.CM_IMPORT },
			{ KeyEvent.VK_B,      1, EmulatorUI.CM_INVOKE_EDITOR },
			{ KeyEvent.VK_U,      1, EmulatorUI.CM_RESTORE },
			{ KeyEvent.VK_E,      1, EmulatorUI.CM_EXPORT },
			{ KeyEvent.VK_D,      1, EmulatorUI.CM_DUMP_F },
			{ KeyEvent.VK_A,      1, EmulatorUI.CM_INVOKE_ABOUT },
			{ KeyEvent.VK_O,      1, EmulatorUI.CM_INVOKE_LOG },
			{ KeyEvent.VK_S,      1, EmulatorUI.CM_SNAP },
			{ KeyEvent.VK_T,      1, EmulatorUI.CM_CHANGE_TICKS },
			{ KeyEvent.VK_ESCAPE, 0, EmulatorUI.CM_INVOKE_DEBUGGER }
	};

	public static boolean enableShortcuts = false;
	public static int shortcutsModifiers = 0;

	public static Map<String, Integer> asInt;

	// -----------------------------------------------------------------------------
	static {
		asInt = new ConcurrentHashMap<String, Integer>();
		asInt.put("VK_ENTER",                     (int) '\n');
		asInt.put("VK_BACK_SPACE",                (int) '\b');
		asInt.put("VK_TAB",                       (int) '\t');
		asInt.put("VK_CANCEL",                    0x03);
		asInt.put("VK_CLEAR",                     0x0C);
		asInt.put("VK_SHIFT",                     0x10);
		asInt.put("VK_CONTROL",                   0x11);
		asInt.put("VK_ALT",                       0x12);
		asInt.put("VK_PAUSE",                     0x13);
		asInt.put("VK_CAPS_LOCK",                 0x14);
		asInt.put("VK_ESCAPE",                    0x1B);
		asInt.put("VK_SPACE",                     0x20);
		asInt.put("VK_PAGE_UP",                   0x21);
		asInt.put("VK_PAGE_DOWN",                 0x22);
		asInt.put("VK_END",                       0x23);
		asInt.put("VK_HOME",                      0x24);
		asInt.put("VK_LEFT",                      0x25);
		asInt.put("VK_UP",                        0x26);
		asInt.put("VK_RIGHT",                     0x27);
		asInt.put("VK_DOWN",                      0x28);
		asInt.put("VK_COMMA",                     0x2C);
		asInt.put("VK_MINUS",                     0x2D);
		asInt.put("VK_PERIOD",                    0x2E);
		asInt.put("VK_SLASH",                     0x2F);
		asInt.put("VK_0",                         0x30);
		asInt.put("VK_1",                         0x31);
		asInt.put("VK_2",                         0x32);
		asInt.put("VK_3",                         0x33);
		asInt.put("VK_4",                         0x34);
		asInt.put("VK_5",                         0x35);
		asInt.put("VK_6",                         0x36);
		asInt.put("VK_7",                         0x37);
		asInt.put("VK_8",                         0x38);
		asInt.put("VK_9",                         0x39);
		asInt.put("VK_SEMICOLON",                 0x3B);
		asInt.put("VK_EQUALS",                    0x3D);
		asInt.put("VK_A",                         0x41);
		asInt.put("VK_B",                         0x42);
		asInt.put("VK_C",                         0x43);
		asInt.put("VK_D",                         0x44);
		asInt.put("VK_E",                         0x45);
		asInt.put("VK_F",                         0x46);
		asInt.put("VK_G",                         0x47);
		asInt.put("VK_H",                         0x48);
		asInt.put("VK_I",                         0x49);
		asInt.put("VK_J",                         0x4A);
		asInt.put("VK_K",                         0x4B);
		asInt.put("VK_L",                         0x4C);
		asInt.put("VK_M",                         0x4D);
		asInt.put("VK_N",                         0x4E);
		asInt.put("VK_O",                         0x4F);
		asInt.put("VK_P",                         0x50);
		asInt.put("VK_Q",                         0x51);
		asInt.put("VK_R",                         0x52);
		asInt.put("VK_S",                         0x53);
		asInt.put("VK_T",                         0x54);
		asInt.put("VK_U",                         0x55);
		asInt.put("VK_V",                         0x56);
		asInt.put("VK_W",                         0x57);
		asInt.put("VK_X",                         0x58);
		asInt.put("VK_Y",                         0x59);
		asInt.put("VK_Z",                         0x5A);
		asInt.put("VK_OPEN_BRACKET",              0x5B);
		asInt.put("VK_BACK_SLASH",                0x5C);
		asInt.put("VK_CLOSE_BRACKET",             0x5D);
		asInt.put("VK_NUMPAD0",                   0x60);
		asInt.put("VK_NUMPAD1",                   0x61);
		asInt.put("VK_NUMPAD2",                   0x62);
		asInt.put("VK_NUMPAD3",                   0x63);
		asInt.put("VK_NUMPAD4",                   0x64);
		asInt.put("VK_NUMPAD5",                   0x65);
		asInt.put("VK_NUMPAD6",                   0x66);
		asInt.put("VK_NUMPAD7",                   0x67);
		asInt.put("VK_NUMPAD8",                   0x68);
		asInt.put("VK_NUMPAD9",                   0x69);
		asInt.put("VK_MULTIPLY",                  0x6A);
		asInt.put("VK_ADD",                       0x6B);
		asInt.put("VK_SEPARATER",                 0x6C);
		asInt.put("VK_SUBTRACT",                  0x6D);
		asInt.put("VK_DECIMAL",                   0x6E);
		asInt.put("VK_DIVIDE",                    0x6F);
		asInt.put("VK_DELETE",                    0x7F);
		asInt.put("VK_NUM_LOCK",                  0x90);
		asInt.put("VK_SCROLL_LOCK",               0x91);
		asInt.put("VK_F1",                        0x70);
		asInt.put("VK_F2",                        0x71);
		asInt.put("VK_F3",                        0x72);
		asInt.put("VK_F4",                        0x73);
		asInt.put("VK_F5",                        0x74);
		asInt.put("VK_F6",                        0x75);
		asInt.put("VK_F7",                        0x76);
		asInt.put("VK_F8",                        0x77);
		asInt.put("VK_F9",                        0x78);
		asInt.put("VK_F10",                       0x79);
		asInt.put("VK_F11",                       0x7A);
		asInt.put("VK_F12",                       0x7B);
		asInt.put("VK_F13",                       0xF000);
		asInt.put("VK_F14",                       0xF001);
		asInt.put("VK_F15",                       0xF002);
		asInt.put("VK_F16",                       0xF003);
		asInt.put("VK_F17",                       0xF004);
		asInt.put("VK_F18",                       0xF005);
		asInt.put("VK_F19",                       0xF006);
		asInt.put("VK_F20",                       0xF007);
		asInt.put("VK_F21",                       0xF008);
		asInt.put("VK_F22",                       0xF009);
		asInt.put("VK_F23",                       0xF00A);
		asInt.put("VK_F24",                       0xF00B);
		asInt.put("VK_PRINTSCREEN",               0x9A);
		asInt.put("VK_INSERT",                    0x9B);
		asInt.put("VK_HELP",                      0x9C);
		asInt.put("VK_META",                      0x9D);
		asInt.put("VK_BACK_QUOTE",                0xC0);
		asInt.put("VK_QUOTE",                     0xDE);
		asInt.put("VK_KP_UP",                     0xE0);
		asInt.put("VK_KP_DOWN",                   0xE1);
		asInt.put("VK_KP_LEFT",                   0xE2);
		asInt.put("VK_KP_RIGHT",                  0xE3);
		asInt.put("VK_DEAD_GRAVE",                0x80);
		asInt.put("VK_DEAD_ACUTE",                0x81);
		asInt.put("VK_DEAD_CIRCUMFLEX",           0x82);
		asInt.put("VK_DEAD_TILDE",                0x83);
		asInt.put("VK_DEAD_MACRON",               0x84);
		asInt.put("VK_DEAD_BREVE",                0x85);
		asInt.put("VK_DEAD_ABOVEDOT",             0x86);
		asInt.put("VK_DEAD_DIAERESIS",            0x87);
		asInt.put("VK_DEAD_ABOVERING",            0x88);
		asInt.put("VK_DEAD_DOUBLEACUTE",          0x89);
		asInt.put("VK_DEAD_CARON",                0x8A);
		asInt.put("VK_DEAD_CEDILLA",              0x8B);
		asInt.put("VK_DEAD_OGONEK",               0x8C);
		asInt.put("VK_DEAD_IOTA",                 0x8D);
		asInt.put("VK_DEAD_VOICED_SOUND",         0x8E);
		asInt.put("VK_DEAD_SEMIVOICED_SOUND",     0x8F);
		asInt.put("VK_AMPERSAND",                 0x96);
		asInt.put("VK_ASTERISK",                  0x97);
		asInt.put("VK_QUOTEDBL",                  0x98);
		asInt.put("VK_LESS",                      0x99);
		asInt.put("VK_GREATER",                   0xA0);
		asInt.put("VK_BRACELEFT",                 0xA1);
		asInt.put("VK_BRACERIGHT",                0xA2);
		asInt.put("VK_AT",                        0x0200);
		asInt.put("VK_COLON",                     0x0201);
		asInt.put("VK_CIRCUMFLEX",                0x0202);
		asInt.put("VK_DOLLAR",                    0x0203);
		asInt.put("VK_EURO_SIGN",                 0x0204);
		asInt.put("VK_EXCLAMATION_MARK",          0x0205);
		asInt.put("VK_INVERTED_EXCLAMATION_MARK", 0x0206);
		asInt.put("VK_LEFT_PARENTHESIS",          0x0207);
		asInt.put("VK_NUMBER_SIGN",               0x0208);
		asInt.put("VK_PLUS",                      0x0209);
		asInt.put("VK_RIGHT_PARENTHESIS",         0x020A);
		asInt.put("VK_UNDERSCORE",                0x020B);
		asInt.put("VK_FINAL",                     0x0018);
		asInt.put("VK_CONVERT",                   0x001C);
		asInt.put("VK_NONCONVERT",                0x001D);
		asInt.put("VK_ACCEPT",                    0x001E);
		asInt.put("VK_MODECHANGE",                0x001F);
		asInt.put("VK_KANA",                      0x0015);
		asInt.put("VK_KANJI",                     0x0019);
		asInt.put("VK_ALPHANUMERIC",              0x00F0);
		asInt.put("VK_KATAKANA",                  0x00F1);
		asInt.put("VK_HIRAGANA",                  0x00F2);
		asInt.put("VK_FULL_WIDTH",                0x00F3);
		asInt.put("VK_HALF_WIDTH",                0x00F4);
		asInt.put("VK_ROMAN_CHARACTERS",          0x00F5);
		asInt.put("VK_ALL_CANDIDATES",            0x0100);
		asInt.put("VK_PREVIOUS_CANDIDATE",        0x0101);
		asInt.put("VK_CODE_INPUT",                0x0102);
		asInt.put("VK_JAPANESE_KATAKANA",         0x0103);
		asInt.put("VK_JAPANESE_HIRAGANA",         0x0104);
		asInt.put("VK_JAPANESE_ROMAN",            0x0105);
		asInt.put("VK_CUT",                       0xFFD1);
		asInt.put("VK_COPY",                      0xFFCD);
		asInt.put("VK_PASTE",                     0xFFCF);
		asInt.put("VK_UNDO",                      0xFFCB);
		asInt.put("VK_AGAIN",                     0xFFC9);
		asInt.put("VK_FIND",                      0xFFD0);
		asInt.put("VK_PROPS",                     0xFFCA);
		asInt.put("VK_STOP",                      0xFFC8);
		asInt.put("VK_COMPOSE",                   0xFF20);
		asInt.put("VK_ALT_GRAPH",                 0xFF7E);
		asInt.put("VK_UNDEFINED",                 0x0);

		for (int i = 0; i < 0xFF; i++) {
			asInt.put("VK_x_" + Integer.toHexString(i).toUpperCase(), i);
		}
	}

	// -----------------------------------------------------------------------------

	public static int getCommandForShortcut(KeyEvent e) {
		if (enableShortcuts) {
			for (int i = 0; i < SHORTCUTS.length; i++) {
				if (SHORTCUTS[i][0] == e.getKeyCode() && (SHORTCUTS[i][1] * shortcutsModifiers) == (e.getModifiersEx() & MODIFIERS_MASK)) {
					return SHORTCUTS[i][2];
				}
			}
		}
		return 0;
	}

	public static int[] getShortcutForCommand(int command) {
		if (enableShortcuts) {
			for (int i = 0; i < SHORTCUTS.length; i++) {
				if (SHORTCUTS[i][2] == command) {
					return new int[] { SHORTCUTS[i][0], SHORTCUTS[i][1] * shortcutsModifiers };
				}
			}
		}
		return null;
	}
}
