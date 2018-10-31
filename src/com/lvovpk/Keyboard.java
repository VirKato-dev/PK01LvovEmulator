package com.lvovpk;

import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keyboard Abstraction layer
 */
class Keyboard {

	private static final int[][] SHORTCUTS = {
		{ KeyEvent.VK_F8,     EmulatorUI.MENU_SHORTCUT_KEY_MASK, EmulatorUI.CM_MODE },
		{ KeyEvent.VK_L,      EmulatorUI.MENU_SHORTCUT_KEY_MASK, EmulatorUI.CM_LOAD },
		{ KeyEvent.VK_F6,     EmulatorUI.MENU_SHORTCUT_KEY_MASK, EmulatorUI.CM_RESET },
		{ KeyEvent.VK_P,      EmulatorUI.MENU_SHORTCUT_KEY_MASK, EmulatorUI.CM_PAUSE },
		{ KeyEvent.VK_C,      EmulatorUI.MENU_SHORTCUT_KEY_MASK, EmulatorUI.CM_RESUME },
		{ KeyEvent.VK_I,      EmulatorUI.MENU_SHORTCUT_KEY_MASK, EmulatorUI.CM_IMPORT },
		{ KeyEvent.VK_B,      EmulatorUI.MENU_SHORTCUT_KEY_MASK, EmulatorUI.CM_INVOKE_EDITOR },
		{ KeyEvent.VK_R,      EmulatorUI.MENU_SHORTCUT_KEY_MASK, EmulatorUI.CM_RESTORE },
		{ KeyEvent.VK_E,      EmulatorUI.MENU_SHORTCUT_KEY_MASK, EmulatorUI.CM_EXPORT },
		{ KeyEvent.VK_D,      EmulatorUI.MENU_SHORTCUT_KEY_MASK, EmulatorUI.CM_DUMP_F },
		{ KeyEvent.VK_O,      EmulatorUI.MENU_SHORTCUT_KEY_MASK, EmulatorUI.CM_INVOKE_LOG },
		{ KeyEvent.VK_F5,     EmulatorUI.MENU_SHORTCUT_KEY_MASK, EmulatorUI.CM_SNAP },
		{ KeyEvent.VK_T,      EmulatorUI.MENU_SHORTCUT_KEY_MASK, EmulatorUI.CM_CHANGE_TICKS },
		{ KeyEvent.VK_ESCAPE, 0,                                 EmulatorUI.CM_INVOKE_DEBUGGER }
	};
	
	public static boolean enableShortcuts = false;
	
	public static Map<String, Integer> asInt;
	

	//-----------------------------------------------------------------------------
	static {
	    asInt = new ConcurrentHashMap<String, Integer>();
	    asInt.put("VK_ENTER",                     new Integer(    '\n'));
	    asInt.put("VK_BACK_SPACE",                new Integer(    '\b'));
	    asInt.put("VK_TAB",                       new Integer(    '\t'));
	    asInt.put("VK_CANCEL",                    new Integer(    0x03));
	    asInt.put("VK_CLEAR",                     new Integer(    0x0C));
	    asInt.put("VK_SHIFT",                     new Integer(    0x10));
	    asInt.put("VK_CONTROL",                   new Integer(    0x11));
	    asInt.put("VK_ALT",                       new Integer(    0x12));
	    asInt.put("VK_PAUSE",                     new Integer(    0x13));
	    asInt.put("VK_CAPS_LOCK",                 new Integer(    0x14));
	    asInt.put("VK_ESCAPE",                    new Integer(    0x1B));
	    asInt.put("VK_SPACE",                     new Integer(    0x20));
	    asInt.put("VK_PAGE_UP",                   new Integer(    0x21));
	    asInt.put("VK_PAGE_DOWN",                 new Integer(    0x22));
	    asInt.put("VK_END",                       new Integer(    0x23));
	    asInt.put("VK_HOME",                      new Integer(    0x24));
	    asInt.put("VK_LEFT",                      new Integer(    0x25));
	    asInt.put("VK_UP",                        new Integer(    0x26));
	    asInt.put("VK_RIGHT",                     new Integer(    0x27));
	    asInt.put("VK_DOWN",                      new Integer(    0x28));
	    asInt.put("VK_COMMA",                     new Integer(    0x2C));
	    asInt.put("VK_MINUS",                     new Integer(    0x2D));
	    asInt.put("VK_PERIOD",                    new Integer(    0x2E));
	    asInt.put("VK_SLASH",                     new Integer(    0x2F));
	    asInt.put("VK_0",                         new Integer(    0x30));
	    asInt.put("VK_1",                         new Integer(    0x31));
	    asInt.put("VK_2",                         new Integer(    0x32));
	    asInt.put("VK_3",                         new Integer(    0x33));
	    asInt.put("VK_4",                         new Integer(    0x34));
	    asInt.put("VK_5",                         new Integer(    0x35));
	    asInt.put("VK_6",                         new Integer(    0x36));
	    asInt.put("VK_7",                         new Integer(    0x37));
	    asInt.put("VK_8",                         new Integer(    0x38));
	    asInt.put("VK_9",                         new Integer(    0x39));
	    asInt.put("VK_SEMICOLON",                 new Integer(    0x3B));
	    asInt.put("VK_EQUALS",                    new Integer(    0x3D));
	    asInt.put("VK_A",                         new Integer(    0x41));
	    asInt.put("VK_B",                         new Integer(    0x42));
	    asInt.put("VK_C",                         new Integer(    0x43));
	    asInt.put("VK_D",                         new Integer(    0x44));
	    asInt.put("VK_E",                         new Integer(    0x45));
	    asInt.put("VK_F",                         new Integer(    0x46));
	    asInt.put("VK_G",                         new Integer(    0x47));
	    asInt.put("VK_H",                         new Integer(    0x48));
	    asInt.put("VK_I",                         new Integer(    0x49));
	    asInt.put("VK_J",                         new Integer(    0x4A));
	    asInt.put("VK_K",                         new Integer(    0x4B));
	    asInt.put("VK_L",                         new Integer(    0x4C));
	    asInt.put("VK_M",                         new Integer(    0x4D));
	    asInt.put("VK_N",                         new Integer(    0x4E));
	    asInt.put("VK_O",                         new Integer(    0x4F));
	    asInt.put("VK_P",                         new Integer(    0x50));
	    asInt.put("VK_Q",                         new Integer(    0x51));
	    asInt.put("VK_R",                         new Integer(    0x52));
	    asInt.put("VK_S",                         new Integer(    0x53));
	    asInt.put("VK_T",                         new Integer(    0x54));
	    asInt.put("VK_U",                         new Integer(    0x55));
	    asInt.put("VK_V",                         new Integer(    0x56));
	    asInt.put("VK_W",                         new Integer(    0x57));
	    asInt.put("VK_X",                         new Integer(    0x58));
	    asInt.put("VK_Y",                         new Integer(    0x59));
	    asInt.put("VK_Z",                         new Integer(    0x5A));
	    asInt.put("VK_OPEN_BRACKET",              new Integer(    0x5B));
	    asInt.put("VK_BACK_SLASH",                new Integer(    0x5C));
	    asInt.put("VK_CLOSE_BRACKET",             new Integer(    0x5D));
	    asInt.put("VK_NUMPAD0",                   new Integer(    0x60));
	    asInt.put("VK_NUMPAD1",                   new Integer(    0x61));
	    asInt.put("VK_NUMPAD2",                   new Integer(    0x62));
	    asInt.put("VK_NUMPAD3",                   new Integer(    0x63));
	    asInt.put("VK_NUMPAD4",                   new Integer(    0x64));
	    asInt.put("VK_NUMPAD5",                   new Integer(    0x65));
	    asInt.put("VK_NUMPAD6",                   new Integer(    0x66));
	    asInt.put("VK_NUMPAD7",                   new Integer(    0x67));
	    asInt.put("VK_NUMPAD8",                   new Integer(    0x68));
	    asInt.put("VK_NUMPAD9",                   new Integer(    0x69));
	    asInt.put("VK_MULTIPLY",                  new Integer(    0x6A));
	    asInt.put("VK_ADD",                       new Integer(    0x6B));
	    asInt.put("VK_SEPARATER",                 new Integer(    0x6C));
	    asInt.put("VK_SUBTRACT",                  new Integer(    0x6D));
	    asInt.put("VK_DECIMAL",                   new Integer(    0x6E));
	    asInt.put("VK_DIVIDE",                    new Integer(    0x6F));
	    asInt.put("VK_DELETE",                    new Integer(    0x7F));
	    asInt.put("VK_NUM_LOCK",                  new Integer(    0x90));
	    asInt.put("VK_SCROLL_LOCK",               new Integer(    0x91));
	    asInt.put("VK_F1",                        new Integer(    0x70));
	    asInt.put("VK_F2",                        new Integer(    0x71));
	    asInt.put("VK_F3",                        new Integer(    0x72));
	    asInt.put("VK_F4",                        new Integer(    0x73));
	    asInt.put("VK_F5",                        new Integer(    0x74));
	    asInt.put("VK_F6",                        new Integer(    0x75));
	    asInt.put("VK_F7",                        new Integer(    0x76));
	    asInt.put("VK_F8",                        new Integer(    0x77));
	    asInt.put("VK_F9",                        new Integer(    0x78));
	    asInt.put("VK_F10",                       new Integer(    0x79));
	    asInt.put("VK_F11",                       new Integer(    0x7A));
	    asInt.put("VK_F12",                       new Integer(    0x7B));
	    asInt.put("VK_F13",                       new Integer(  0xF000));
	    asInt.put("VK_F14",                       new Integer(  0xF001));
	    asInt.put("VK_F15",                       new Integer(  0xF002));
	    asInt.put("VK_F16",                       new Integer(  0xF003));
	    asInt.put("VK_F17",                       new Integer(  0xF004));
	    asInt.put("VK_F18",                       new Integer(  0xF005));
	    asInt.put("VK_F19",                       new Integer(  0xF006));
	    asInt.put("VK_F20",                       new Integer(  0xF007));
	    asInt.put("VK_F21",                       new Integer(  0xF008));
	    asInt.put("VK_F22",                       new Integer(  0xF009));
	    asInt.put("VK_F23",                       new Integer(  0xF00A));
	    asInt.put("VK_F24",                       new Integer(  0xF00B));
	    asInt.put("VK_PRINTSCREEN",               new Integer(    0x9A));
	    asInt.put("VK_INSERT",                    new Integer(    0x9B));
	    asInt.put("VK_HELP",                      new Integer(    0x9C));
	    asInt.put("VK_META",                      new Integer(    0x9D));
	    asInt.put("VK_BACK_QUOTE",                new Integer(    0xC0));
	    asInt.put("VK_QUOTE",                     new Integer(    0xDE));
	    asInt.put("VK_KP_UP",                     new Integer(    0xE0));
	    asInt.put("VK_KP_DOWN",                   new Integer(    0xE1));
	    asInt.put("VK_KP_LEFT",                   new Integer(    0xE2));
	    asInt.put("VK_KP_RIGHT",                  new Integer(    0xE3));
	    asInt.put("VK_DEAD_GRAVE",                new Integer(    0x80));
	    asInt.put("VK_DEAD_ACUTE",                new Integer(    0x81));
	    asInt.put("VK_DEAD_CIRCUMFLEX",           new Integer(    0x82));
	    asInt.put("VK_DEAD_TILDE",                new Integer(    0x83));
	    asInt.put("VK_DEAD_MACRON",               new Integer(    0x84));
	    asInt.put("VK_DEAD_BREVE",                new Integer(    0x85));
	    asInt.put("VK_DEAD_ABOVEDOT",             new Integer(    0x86));
	    asInt.put("VK_DEAD_DIAERESIS",            new Integer(    0x87));
	    asInt.put("VK_DEAD_ABOVERING",            new Integer(    0x88));
	    asInt.put("VK_DEAD_DOUBLEACUTE",          new Integer(    0x89));
	    asInt.put("VK_DEAD_CARON",                new Integer(    0x8A));
	    asInt.put("VK_DEAD_CEDILLA",              new Integer(    0x8B));
	    asInt.put("VK_DEAD_OGONEK",               new Integer(    0x8C));
	    asInt.put("VK_DEAD_IOTA",                 new Integer(    0x8D));
	    asInt.put("VK_DEAD_VOICED_SOUND",         new Integer(    0x8E));
	    asInt.put("VK_DEAD_SEMIVOICED_SOUND",     new Integer(    0x8F));
	    asInt.put("VK_AMPERSAND",                 new Integer(    0x96));
	    asInt.put("VK_ASTERISK",                  new Integer(    0x97));
	    asInt.put("VK_QUOTEDBL",                  new Integer(    0x98));
	    asInt.put("VK_LESS",                      new Integer(    0x99));
	    asInt.put("VK_GREATER",                   new Integer(    0xA0));
	    asInt.put("VK_BRACELEFT",                 new Integer(    0xA1));
	    asInt.put("VK_BRACERIGHT",                new Integer(    0xA2));
	    asInt.put("VK_AT",                        new Integer(  0x0200));
	    asInt.put("VK_COLON",                     new Integer(  0x0201));
	    asInt.put("VK_CIRCUMFLEX",                new Integer(  0x0202));
	    asInt.put("VK_DOLLAR",                    new Integer(  0x0203));
	    asInt.put("VK_EURO_SIGN",                 new Integer(  0x0204));
	    asInt.put("VK_EXCLAMATION_MARK",          new Integer(  0x0205));
	    asInt.put("VK_INVERTED_EXCLAMATION_MARK", new Integer(  0x0206));
	    asInt.put("VK_LEFT_PARENTHESIS",          new Integer(  0x0207));
	    asInt.put("VK_NUMBER_SIGN",               new Integer(  0x0208));
	    asInt.put("VK_PLUS",                      new Integer(  0x0209));
	    asInt.put("VK_RIGHT_PARENTHESIS",         new Integer(  0x020A));
	    asInt.put("VK_UNDERSCORE",                new Integer(  0x020B));
	    asInt.put("VK_FINAL",                     new Integer(  0x0018));
	    asInt.put("VK_CONVERT",                   new Integer(  0x001C));
	    asInt.put("VK_NONCONVERT",                new Integer(  0x001D));
	    asInt.put("VK_ACCEPT",                    new Integer(  0x001E));
	    asInt.put("VK_MODECHANGE",                new Integer(  0x001F));
	    asInt.put("VK_KANA",                      new Integer(  0x0015));
	    asInt.put("VK_KANJI",                     new Integer(  0x0019));
	    asInt.put("VK_ALPHANUMERIC",              new Integer(  0x00F0));
	    asInt.put("VK_KATAKANA",                  new Integer(  0x00F1));
	    asInt.put("VK_HIRAGANA",                  new Integer(  0x00F2));
	    asInt.put("VK_FULL_WIDTH",                new Integer(  0x00F3));
	    asInt.put("VK_HALF_WIDTH",                new Integer(  0x00F4));
	    asInt.put("VK_ROMAN_CHARACTERS",          new Integer(  0x00F5));
	    asInt.put("VK_ALL_CANDIDATES",            new Integer(  0x0100));
	    asInt.put("VK_PREVIOUS_CANDIDATE",        new Integer(  0x0101));
	    asInt.put("VK_CODE_INPUT",                new Integer(  0x0102));
	    asInt.put("VK_JAPANESE_KATAKANA",         new Integer(  0x0103));
	    asInt.put("VK_JAPANESE_HIRAGANA",         new Integer(  0x0104));
	    asInt.put("VK_JAPANESE_ROMAN",            new Integer(  0x0105));
	    asInt.put("VK_CUT",                       new Integer(  0xFFD1));
	    asInt.put("VK_COPY",                      new Integer(  0xFFCD));
	    asInt.put("VK_PASTE",                     new Integer(  0xFFCF));
	    asInt.put("VK_UNDO",                      new Integer(  0xFFCB));
	    asInt.put("VK_AGAIN",                     new Integer(  0xFFC9));
	    asInt.put("VK_FIND",                      new Integer(  0xFFD0));
	    asInt.put("VK_PROPS",                     new Integer(  0xFFCA));
	    asInt.put("VK_STOP",                      new Integer(  0xFFC8));
	    asInt.put("VK_COMPOSE",                   new Integer(  0xFF20));
	    asInt.put("VK_ALT_GRAPH",                 new Integer(  0xFF7E));
	    asInt.put("VK_UNDEFINED",                 new Integer(     0x0));
	    
	    for (int i=0; i<0xFF; i++) asInt.put(
	        "VK_x_"+Integer.toHexString(i).toUpperCase(),
	        new Integer(i)
	    );
	}

	//-----------------------------------------------------------------------------
	
	public static int getCommandForShortcut(KeyEvent e) {
		if (enableShortcuts) {
			for (int i = 0; i < SHORTCUTS.length; i++) {
				if (SHORTCUTS[i][0] == e.getKeyCode() && SHORTCUTS[i][1] == e.getModifiers()) {
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
					return new int[] { SHORTCUTS[i][0], SHORTCUTS[i][1] };
				}
			}
		}
		return null;
	}
}
