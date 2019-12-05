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
		asInt.put("VK_ENTER",                     Integer.valueOf('\n'));
		asInt.put("VK_BACK_SPACE",                Integer.valueOf('\b'));
		asInt.put("VK_TAB",                       Integer.valueOf('\t'));
		asInt.put("VK_CANCEL",                    Integer.valueOf(0x03));
		asInt.put("VK_CLEAR",                     Integer.valueOf(0x0C));
		asInt.put("VK_SHIFT",                     Integer.valueOf(0x10));
		asInt.put("VK_CONTROL",                   Integer.valueOf(0x11));
		asInt.put("VK_ALT",                       Integer.valueOf(0x12));
		asInt.put("VK_PAUSE",                     Integer.valueOf(0x13));
		asInt.put("VK_CAPS_LOCK",                 Integer.valueOf(0x14));
		asInt.put("VK_ESCAPE",                    Integer.valueOf(0x1B));
		asInt.put("VK_SPACE",                     Integer.valueOf(0x20));
		asInt.put("VK_PAGE_UP",                   Integer.valueOf(0x21));
		asInt.put("VK_PAGE_DOWN",                 Integer.valueOf(0x22));
		asInt.put("VK_END",                       Integer.valueOf(0x23));
		asInt.put("VK_HOME",                      Integer.valueOf(0x24));
		asInt.put("VK_LEFT",                      Integer.valueOf(0x25));
		asInt.put("VK_UP",                        Integer.valueOf(0x26));
		asInt.put("VK_RIGHT",                     Integer.valueOf(0x27));
		asInt.put("VK_DOWN",                      Integer.valueOf(0x28));
		asInt.put("VK_COMMA",                     Integer.valueOf(0x2C));
		asInt.put("VK_MINUS",                     Integer.valueOf(0x2D));
		asInt.put("VK_PERIOD",                    Integer.valueOf(0x2E));
		asInt.put("VK_SLASH",                     Integer.valueOf(0x2F));
		asInt.put("VK_0",                         Integer.valueOf(0x30));
		asInt.put("VK_1",                         Integer.valueOf(0x31));
		asInt.put("VK_2",                         Integer.valueOf(0x32));
		asInt.put("VK_3",                         Integer.valueOf(0x33));
		asInt.put("VK_4",                         Integer.valueOf(0x34));
		asInt.put("VK_5",                         Integer.valueOf(0x35));
		asInt.put("VK_6",                         Integer.valueOf(0x36));
		asInt.put("VK_7",                         Integer.valueOf(0x37));
		asInt.put("VK_8",                         Integer.valueOf(0x38));
		asInt.put("VK_9",                         Integer.valueOf(0x39));
		asInt.put("VK_SEMICOLON",                 Integer.valueOf(0x3B));
		asInt.put("VK_EQUALS",                    Integer.valueOf(0x3D));
		asInt.put("VK_A",                         Integer.valueOf(0x41));
		asInt.put("VK_B",                         Integer.valueOf(0x42));
		asInt.put("VK_C",                         Integer.valueOf(0x43));
		asInt.put("VK_D",                         Integer.valueOf(0x44));
		asInt.put("VK_E",                         Integer.valueOf(0x45));
		asInt.put("VK_F",                         Integer.valueOf(0x46));
		asInt.put("VK_G",                         Integer.valueOf(0x47));
		asInt.put("VK_H",                         Integer.valueOf(0x48));
		asInt.put("VK_I",                         Integer.valueOf(0x49));
		asInt.put("VK_J",                         Integer.valueOf(0x4A));
		asInt.put("VK_K",                         Integer.valueOf(0x4B));
		asInt.put("VK_L",                         Integer.valueOf(0x4C));
		asInt.put("VK_M",                         Integer.valueOf(0x4D));
		asInt.put("VK_N",                         Integer.valueOf(0x4E));
		asInt.put("VK_O",                         Integer.valueOf(0x4F));
		asInt.put("VK_P",                         Integer.valueOf(0x50));
		asInt.put("VK_Q",                         Integer.valueOf(0x51));
		asInt.put("VK_R",                         Integer.valueOf(0x52));
		asInt.put("VK_S",                         Integer.valueOf(0x53));
		asInt.put("VK_T",                         Integer.valueOf(0x54));
		asInt.put("VK_U",                         Integer.valueOf(0x55));
		asInt.put("VK_V",                         Integer.valueOf(0x56));
		asInt.put("VK_W",                         Integer.valueOf(0x57));
		asInt.put("VK_X",                         Integer.valueOf(0x58));
		asInt.put("VK_Y",                         Integer.valueOf(0x59));
		asInt.put("VK_Z",                         Integer.valueOf(0x5A));
		asInt.put("VK_OPEN_BRACKET",              Integer.valueOf(0x5B));
		asInt.put("VK_BACK_SLASH",                Integer.valueOf(0x5C));
		asInt.put("VK_CLOSE_BRACKET",             Integer.valueOf(0x5D));
		asInt.put("VK_NUMPAD0",                   Integer.valueOf(0x60));
		asInt.put("VK_NUMPAD1",                   Integer.valueOf(0x61));
		asInt.put("VK_NUMPAD2",                   Integer.valueOf(0x62));
		asInt.put("VK_NUMPAD3",                   Integer.valueOf(0x63));
		asInt.put("VK_NUMPAD4",                   Integer.valueOf(0x64));
		asInt.put("VK_NUMPAD5",                   Integer.valueOf(0x65));
		asInt.put("VK_NUMPAD6",                   Integer.valueOf(0x66));
		asInt.put("VK_NUMPAD7",                   Integer.valueOf(0x67));
		asInt.put("VK_NUMPAD8",                   Integer.valueOf(0x68));
		asInt.put("VK_NUMPAD9",                   Integer.valueOf(0x69));
		asInt.put("VK_MULTIPLY",                  Integer.valueOf(0x6A));
		asInt.put("VK_ADD",                       Integer.valueOf(0x6B));
		asInt.put("VK_SEPARATER",                 Integer.valueOf(0x6C));
		asInt.put("VK_SUBTRACT",                  Integer.valueOf(0x6D));
		asInt.put("VK_DECIMAL",                   Integer.valueOf(0x6E));
		asInt.put("VK_DIVIDE",                    Integer.valueOf(0x6F));
		asInt.put("VK_DELETE",                    Integer.valueOf(0x7F));
		asInt.put("VK_NUM_LOCK",                  Integer.valueOf(0x90));
		asInt.put("VK_SCROLL_LOCK",               Integer.valueOf(0x91));
		asInt.put("VK_F1",                        Integer.valueOf(0x70));
		asInt.put("VK_F2",                        Integer.valueOf(0x71));
		asInt.put("VK_F3",                        Integer.valueOf(0x72));
		asInt.put("VK_F4",                        Integer.valueOf(0x73));
		asInt.put("VK_F5",                        Integer.valueOf(0x74));
		asInt.put("VK_F6",                        Integer.valueOf(0x75));
		asInt.put("VK_F7",                        Integer.valueOf(0x76));
		asInt.put("VK_F8",                        Integer.valueOf(0x77));
		asInt.put("VK_F9",                        Integer.valueOf(0x78));
		asInt.put("VK_F10",                       Integer.valueOf(0x79));
		asInt.put("VK_F11",                       Integer.valueOf(0x7A));
		asInt.put("VK_F12",                       Integer.valueOf(0x7B));
		asInt.put("VK_F13",                       Integer.valueOf(0xF000));
		asInt.put("VK_F14",                       Integer.valueOf(0xF001));
		asInt.put("VK_F15",                       Integer.valueOf(0xF002));
		asInt.put("VK_F16",                       Integer.valueOf(0xF003));
		asInt.put("VK_F17",                       Integer.valueOf(0xF004));
		asInt.put("VK_F18",                       Integer.valueOf(0xF005));
		asInt.put("VK_F19",                       Integer.valueOf(0xF006));
		asInt.put("VK_F20",                       Integer.valueOf(0xF007));
		asInt.put("VK_F21",                       Integer.valueOf(0xF008));
		asInt.put("VK_F22",                       Integer.valueOf(0xF009));
		asInt.put("VK_F23",                       Integer.valueOf(0xF00A));
		asInt.put("VK_F24",                       Integer.valueOf(0xF00B));
		asInt.put("VK_PRINTSCREEN",               Integer.valueOf(0x9A));
		asInt.put("VK_INSERT",                    Integer.valueOf(0x9B));
		asInt.put("VK_HELP",                      Integer.valueOf(0x9C));
		asInt.put("VK_META",                      Integer.valueOf(0x9D));
		asInt.put("VK_BACK_QUOTE",                Integer.valueOf(0xC0));
		asInt.put("VK_QUOTE",                     Integer.valueOf(0xDE));
		asInt.put("VK_KP_UP",                     Integer.valueOf(0xE0));
		asInt.put("VK_KP_DOWN",                   Integer.valueOf(0xE1));
		asInt.put("VK_KP_LEFT",                   Integer.valueOf(0xE2));
		asInt.put("VK_KP_RIGHT",                  Integer.valueOf(0xE3));
		asInt.put("VK_DEAD_GRAVE",                Integer.valueOf(0x80));
		asInt.put("VK_DEAD_ACUTE",                Integer.valueOf(0x81));
		asInt.put("VK_DEAD_CIRCUMFLEX",           Integer.valueOf(0x82));
		asInt.put("VK_DEAD_TILDE",                Integer.valueOf(0x83));
		asInt.put("VK_DEAD_MACRON",               Integer.valueOf(0x84));
		asInt.put("VK_DEAD_BREVE",                Integer.valueOf(0x85));
		asInt.put("VK_DEAD_ABOVEDOT",             Integer.valueOf(0x86));
		asInt.put("VK_DEAD_DIAERESIS",            Integer.valueOf(0x87));
		asInt.put("VK_DEAD_ABOVERING",            Integer.valueOf(0x88));
		asInt.put("VK_DEAD_DOUBLEACUTE",          Integer.valueOf(0x89));
		asInt.put("VK_DEAD_CARON",                Integer.valueOf(0x8A));
		asInt.put("VK_DEAD_CEDILLA",              Integer.valueOf(0x8B));
		asInt.put("VK_DEAD_OGONEK",               Integer.valueOf(0x8C));
		asInt.put("VK_DEAD_IOTA",                 Integer.valueOf(0x8D));
		asInt.put("VK_DEAD_VOICED_SOUND",         Integer.valueOf(0x8E));
		asInt.put("VK_DEAD_SEMIVOICED_SOUND",     Integer.valueOf(0x8F));
		asInt.put("VK_AMPERSAND",                 Integer.valueOf(0x96));
		asInt.put("VK_ASTERISK",                  Integer.valueOf(0x97));
		asInt.put("VK_QUOTEDBL",                  Integer.valueOf(0x98));
		asInt.put("VK_LESS",                      Integer.valueOf(0x99));
		asInt.put("VK_GREATER",                   Integer.valueOf(0xA0));
		asInt.put("VK_BRACELEFT",                 Integer.valueOf(0xA1));
		asInt.put("VK_BRACERIGHT",                Integer.valueOf(0xA2));
		asInt.put("VK_AT",                        Integer.valueOf(0x0200));
		asInt.put("VK_COLON",                     Integer.valueOf(0x0201));
		asInt.put("VK_CIRCUMFLEX",                Integer.valueOf(0x0202));
		asInt.put("VK_DOLLAR",                    Integer.valueOf(0x0203));
		asInt.put("VK_EURO_SIGN",                 Integer.valueOf(0x0204));
		asInt.put("VK_EXCLAMATION_MARK",          Integer.valueOf(0x0205));
		asInt.put("VK_INVERTED_EXCLAMATION_MARK", Integer.valueOf(0x0206));
		asInt.put("VK_LEFT_PARENTHESIS",          Integer.valueOf(0x0207));
		asInt.put("VK_NUMBER_SIGN",               Integer.valueOf(0x0208));
		asInt.put("VK_PLUS",                      Integer.valueOf(0x0209));
		asInt.put("VK_RIGHT_PARENTHESIS",         Integer.valueOf(0x020A));
		asInt.put("VK_UNDERSCORE",                Integer.valueOf(0x020B));
		asInt.put("VK_FINAL",                     Integer.valueOf(0x0018));
		asInt.put("VK_CONVERT",                   Integer.valueOf(0x001C));
		asInt.put("VK_NONCONVERT",                Integer.valueOf(0x001D));
		asInt.put("VK_ACCEPT",                    Integer.valueOf(0x001E));
		asInt.put("VK_MODECHANGE",                Integer.valueOf(0x001F));
		asInt.put("VK_KANA",                      Integer.valueOf(0x0015));
		asInt.put("VK_KANJI",                     Integer.valueOf(0x0019));
		asInt.put("VK_ALPHANUMERIC",              Integer.valueOf(0x00F0));
		asInt.put("VK_KATAKANA",                  Integer.valueOf(0x00F1));
		asInt.put("VK_HIRAGANA",                  Integer.valueOf(0x00F2));
		asInt.put("VK_FULL_WIDTH",                Integer.valueOf(0x00F3));
		asInt.put("VK_HALF_WIDTH",                Integer.valueOf(0x00F4));
		asInt.put("VK_ROMAN_CHARACTERS",          Integer.valueOf(0x00F5));
		asInt.put("VK_ALL_CANDIDATES",            Integer.valueOf(0x0100));
		asInt.put("VK_PREVIOUS_CANDIDATE",        Integer.valueOf(0x0101));
		asInt.put("VK_CODE_INPUT",                Integer.valueOf(0x0102));
		asInt.put("VK_JAPANESE_KATAKANA",         Integer.valueOf(0x0103));
		asInt.put("VK_JAPANESE_HIRAGANA",         Integer.valueOf(0x0104));
		asInt.put("VK_JAPANESE_ROMAN",            Integer.valueOf(0x0105));
		asInt.put("VK_CUT",                       Integer.valueOf(0xFFD1));
		asInt.put("VK_COPY",                      Integer.valueOf(0xFFCD));
		asInt.put("VK_PASTE",                     Integer.valueOf(0xFFCF));
		asInt.put("VK_UNDO",                      Integer.valueOf(0xFFCB));
		asInt.put("VK_AGAIN",                     Integer.valueOf(0xFFC9));
		asInt.put("VK_FIND",                      Integer.valueOf(0xFFD0));
		asInt.put("VK_PROPS",                     Integer.valueOf(0xFFCA));
		asInt.put("VK_STOP",                      Integer.valueOf(0xFFC8));
		asInt.put("VK_COMPOSE",                   Integer.valueOf(0xFF20));
		asInt.put("VK_ALT_GRAPH",                 Integer.valueOf(0xFF7E));
		asInt.put("VK_UNDEFINED",                 Integer.valueOf(0x0));

		for (int i = 0; i < 0xFF; i++) {
			asInt.put("VK_x_" + Integer.toHexString(i).toUpperCase(), Integer.valueOf(i));
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
