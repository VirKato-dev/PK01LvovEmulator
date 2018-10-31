package com.lvovpk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Various .LVT Manipulations
 */
class PKIO {

	final static int CP_866_U = 0;
	final static int CP_1251_U = 1;
	final static int CP_KOI8_U = 2;
	final static int CP_866_N = 3;
	final static int CP_1251_N = 4;
	final static int CP_KOI8_N = 5;

	final static int CP_DEFAULT = CP_1251_U;

	// -----------------------------------------------------------------------------
	final static String NL = System.getProperty("line.separator", "\r\n");

	private static String codes[][] = {
		
		// Unicode //
		{"\u045b\u0402\u0403\u2013\u201e\u2026\u201d\u0453"+ //  866 upcase
		 "\u2022\u20ac\u2030\u0409\u2039\u040a\u040c\u040b"+
		 "\u040f\u045f\u0452\u2018\u2019\u201c\u2020\u201a"+
		 "\u045a\u203a\u2021\u0098\u045c\u2122\u2014\u0459",
		 "\u043e\u00a0\u040e\u0436\u00a4\u0490\u0434\u0408"+ //  866 locase
		 "\u0435\u0401\u00a9\u0404\u00ab\u00ac\u00ad\u00ae"+
		 "\u0407\u043f\u0430\u0431\u0432\u0433\u00a6\u045e"+
		 "\u043c\u043b\u00a7\u0438\u043d\u0439\u0437\u043a"},
		
		{"\u042e\u0410\u0411\u0426\u0414\u0415\u0424\u0413"+ // 1251 upcase
		 "\u0425\u0418\u0419\u041a\u041b\u041c\u041d\u041e"+
		 "\u041f\u042f\u0420\u0421\u0422\u0423\u0416\u0412"+
		 "\u042c\u042b\u0417\u0428\u042d\u0429\u0427\u042a",
		 "\u044e\u0430\u0431\u0446\u0434\u0435\u0444\u0433"+ // 1251 locase
		 "\u0445\u0438\u0439\u043a\u043b\u043c\u043d\u043e"+
		 "\u043f\u044f\u0440\u0441\u0442\u0443\u0436\u0432"+
		 "\u044c\u044b\u0437\u0448\u044d\u0449\u0447\u044a"},
		
		{"\u0430\u0431\u0432\u0433\u0434\u0435\u0436\u0437"+ // koi8 upcase
		 "\u0438\u0439\u043a\u043b\u043c\u043d\u043e\u043f"+
		 "\u0440\u0441\u0442\u0443\u0444\u0445\u0446\u0447"+
		 "\u0448\u0449\u044a\u044b\u044c\u044d\u044e\u044f",
		 "\u0410\u0411\u0412\u0413\u0414\u0415\u0416\u0417"+ // koi8 locase
		 "\u0418\u0419\u041a\u041b\u041c\u041d\u041e\u041f"+
		 "\u0420\u0421\u0422\u0423\u0424\u0425\u0426\u0427"+
		 "\u0428\u0429\u042a\u042b\u042c\u042d\u042e\u042f"},
		
		// Native //
		{"\u009e\u0080\u0081\u0096\u0084\u0085\u0094\u0083"+ //  866 upcase
		 "\u0095\u0088\u0089\u008a\u008b\u008c\u008d\u008e"+
		 "\u008f\u009f\u0090\u0091\u0092\u0093\u0086\u0082"+
		 "\u009c\u009b\u0087\u0098\u009d\u0099\u0097\u009a",
		 "\u00ee\u00a0\u00a1\u00e6\u00a4\u00a5\u00e4\u00a3"+ //  866 locase
		 "\u00e5\u00a8\u00a9\u00aa\u00ab\u00ac\u00ad\u00ae"+
		 "\u00af\u00ef\u00e0\u00e1\u00e2\u00e3\u00a6\u00a2"+
		 "\u00ec\u00eb\u00a7\u00e8\u00ed\u00e9\u00e7\u00ea"},
		
		{"\u00de\u00c0\u00c1\u00d6\u00c4\u00c5\u00d4\u00c3"+ // 1251 upcase
		 "\u00d5\u00c8\u00c9\u00ca\u00cb\u00cc\u00cd\u00ce"+
		 "\u00cf\u00df\u00d0\u00d1\u00d2\u00d3\u00c6\u00c2"+
		 "\u00dc\u00db\u00c7\u00d8\u00dd\u00d9\u00d7\u00da",
		 "\u00fe\u00e0\u00e1\u00f6\u00e4\u00e5\u00f4\u00e3"+ // 1251 locase
		 "\u00f5\u00e8\u00e9\u00ea\u00eb\u00ec\u00ed\u00ee"+
		 "\u00ef\u00ff\u00f0\u00f1\u00f2\u00f3\u00e6\u00e2"+
		 "\u00fc\u00fb\u00e7\u00f8\u00fd\u00f9\u00f7\u00fa"},
		
		{"\u00e0\u00e1\u00e2\u00e3\u00e4\u00e5\u00e6\u00e7"+ // koi8 upcase
		 "\u00e8\u00e9\u00ea\u00eb\u00ec\u00ed\u00ee\u00ef"+
		 "\u00f0\u00f1\u00f2\u00f3\u00f4\u00f5\u00f6\u00f7"+
		 "\u00f8\u00f9\u00fa\u00fb\u00fc\u00fd\u00fe\u00ff",
		 "\u00c0\u00c1\u00c2\u00c3\u00c4\u00c5\u00c6\u00c7"+ // koi8 locase
		 "\u00c8\u00c9\u00ca\u00cb\u00cc\u00cd\u00ce\u00cf"+
		 "\u00d0\u00d1\u00d2\u00d3\u00d4\u00d5\u00d6\u00d7"+
		 "\u00d8\u00d9\u00da\u00db\u00dc\u00dd\u00de\u00df"}
	};

	// -----------------------------------------------------------------------------
	static char koi2dos(char koi, int codepage) {
		return (char) ((koi >= 0x60 && koi <= 0x7F) ? codes[codepage][0].charAt(koi - 0x60) : koi);
	}

	// -----------------------------------------------------------------------------
	static char dos2koi(char dos, int codepage) {
		int ch;

		ch = codes[codepage][0].indexOf(dos);
		if (ch >= 0)
			return (char) (ch + 0x60);

		ch = codes[codepage][1].indexOf(dos);
		if (ch >= 0)
			return (char) (ch + 0x60);

		return Character.toUpperCase(dos);
	}

	// -----------------------------------------------------------------------------
	static String koi2dos(String koi, int codepage) {
		StringBuffer dos = new StringBuffer(koi);
		for (int i = 0; i < dos.length(); i++)
			dos.setCharAt(i, koi2dos(dos.charAt(i), codepage));
		return dos.toString();
	}

	// -----------------------------------------------------------------------------
	static String dos2koi(String dos, int codepage) {
		StringBuffer koi = new StringBuffer(dos);
		for (int i = 0; i < koi.length(); i++)
			koi.setCharAt(i, dos2koi(koi.charAt(i), codepage));
		return koi.toString();
	}

	// -----------------------------------------------------------------------------
	// T a p e s H a n d l i n g
	// -----------------------------------------------------------------------------
	private static String toks[] = {
		// 0x80
		"END", "FOR", "NEXT", "DATA", "INPUT", "DIM", "READ", "LET",
		"GOTO", "RUN", "IF", "RESTORE", "GOSUB", "RETURN", "REM", "STOP",
		// 0x90
		"CLS", "ON", "PLOT", "DRAW", "POKE", "PRINT", "DEF", "CONT", 
		"LIST", "CLEAR", "CLOAD", "CSAVE", "NEW", "LOAD", "MERGE", "SAVE",
		// 0xA0
		"OUT", "WAIT", "SLOAD", "BSAVE", "BLOAD", "BAUD", "LOCATE", "COLOR",
		"LINE", "PSET", "PRESET", "CIRCLE", "PAINT", "GET", "PUT", "BEEP",
		// 0xB0
		"SOUND", "DELETE", "RENUM", "EDIT", "VPOKE", "@@", "@@",
		"@@", "@@", "@@", "@@", "@@", "TAB(", "TO", "SPC(", "FN",
		// 0xC0
		"THEN", "NOT", "STEP", "+", "-", "*", "/", "^",
		"AND", "OR", ">", "=", "<", "@@", "@@", "@@",
		// 0xD0
		"SGN", "INT", "ABS", "USR", "FRE", "INP", "POS", "SQR",
		"RND", "LOG", "EXP", "COS", "SIN", "TAN", "ATN", "PEEK",
		// 0xE0
		"LEN", "STR$", "VAL", "ASC", "CHR$", "LEFT$", "RIGHT$", "MID$",
		"INKEY$", "HEX$", "POINT", "VARPTR", "CSRLIN", "VPEEK", "@@", "@@",
		// 0xF0
		"@@", "@@", "@@", "@@", "@@", "@@", "@@", "@@",
		"@@", "@@", "@@", "@@", "@@", "@@", "@@", "@@"
	};

	// -----------------------------------------------------------------------------
	static int recognize(InputStream prog) throws IOException {
		int sign[] = new int[16];
		Utils.restoreBytes(prog, sign);

		for (int i = 0; i < 9; i++) // it's prefix
			if ("LVOV/2.0/".charAt(i) != sign[i])
				throw new IOException("Wrong .LVT signature at " + i + " !");

		return sign[9];
	}

	// -----------------------------------------------------------------------------
	static void prepare(OutputStream prog, String name, int type, int cp) throws IOException {
		int sign[] = new int[16]; // preparing signature

		for (int i = 0; i < 9; i++) // it's prefix
			sign[i] = "LVOV/2.0/".charAt(i);

		sign[9] = type; // type of file

		for (int i = 0; i < name.length() && i < 6; i++) // and file name
			sign[10 + i] = (byte) dos2koi(name.charAt(i), cp);

		for (int i = name.length(); i < 6; i++) // padded with spaces
			sign[10 + i] = ' ';

		Utils.dumpBytes(prog, sign); // well, signature has been completed
	}

	// -----------------------------------------------------------------------------
	static String basic2text(InputStream prog, int cp) throws IOException {
		if (recognize(prog) != 0xD3)
			throw new IOException("Not a Basic program");

		int ch, line;
		StringBuffer text = new StringBuffer();

		while (Utils.restoreWord(prog) != 0) {
			line = Utils.restoreWord(prog);
			text.append(line).append(' ');
			while ((ch = Utils.restoreByte(prog)) != 0) {
				if ((ch & 0x80) != 0)
					text.append(toks[ch - 0x80]);
				else
					text.append(koi2dos((char) ch, cp));
			}
			text.append(NL);
		}
		return text.toString();
	}

	// -----------------------------------------------------------------------------
	static byte[] text2basic(String text, int cp) throws IOException {
		return text2basic("AUTO", text, cp);
	}

	// -----------------------------------------------------------------------------
	static byte[] text2basic(String name, String text, int cp) throws IOException {
		ByteArrayOutputStream prog = new ByteArrayOutputStream();
		ByteArrayOutputStream line = new ByteArrayOutputStream();

		prepare(prog, name, 0xD3, cp);

		text = dos2koi(text, cp); // source must be in koi8
		int pos = 0, total = text.length(), addr = 0x1723, numb, lastNumb = -1;

		for (;;) // main consuming cycle
		{
			int npos1 = Utils.spanAllow(text, pos, "\t\r\n "); // skip blank
			int npos2 = Utils.spanAllow(text, npos1, "0123456789"); // parse num
			int nstr1 = Utils.spanAllow(text, npos2, "\t "); // skip blank
			int nstr2 = Utils.spanDeny(text, nstr1, "\r\n"); // parse rest

			if (npos1 >= total)
				break; // all data were read
			if (npos1 == npos2 || nstr1 == npos2)
				throw new IOException("Not a line no.: " + text.substring(npos1, nstr2));
			numb = Integer.parseInt(text.substring(npos1, npos2));
			if (numb > 65535 || numb <= lastNumb)
				throw new IOException(numb > 65535 ? "Line no. " + numb + " is too big"
						: "Line no. " + numb + " is too small," + " must be .GT. " + lastNumb);
			lastNumb = numb;

			String skip = null;
			int tok = toks.length;
			Utils.dumpWord(line, numb); // storing line number
			for (pos = nstr1; pos < nstr2;) // beginning line processing...
			{
				int ch = text.charAt(pos);
				if (ch == '"') // skip until next '"' or EOL
				{
					nstr1 = Utils.spanDeny(text, pos + 1, "\"\r\n");
					if (nstr1 < nstr2)
						nstr1++; // skip trailing '"'
					Utils.dumpBytes(line, text, pos, nstr1 - pos);
					pos = nstr1;
					continue;
				}

				if (skip == null)
					for (tok = 0; tok < toks.length; tok++) // token?
						if (text.startsWith(toks[tok], pos))
							break;

				if (tok < toks.length) // yes, store token value
				{
					Utils.dumpByte(line, tok + 0x80);
					pos += toks[tok].length();

					if (toks[tok].equals("REM"))
						skip = "\r\n";
					else if (toks[tok].equals("DATA"))
						skip = ":\r\n";
					tok = toks.length;
				} else {
					if (skip != null && skip.indexOf(ch) >= 0)
						skip = null;
					Utils.dumpByte(line, ch);
					pos++;
				}
			}
			Utils.dumpByte(line, 0);
			addr += line.size() + 2; // close line
			Utils.dumpWord(prog, addr); // storing line header
			line.writeTo(prog);
			line.reset();
		}
		Utils.dumpWord(prog, 0); // close file

		return prog.toByteArray();
	}

	// -----------------------------------------------------------------------------
}
