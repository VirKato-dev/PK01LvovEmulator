package com.lvovpk;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
	Entry Point to Lvov Emulator
*/
public class EmulatorMain extends EmulatorUI {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4339279222189621874L;
	private String args[];

	// -----------------------------------------------------------------------------
	@Override
	public void init() {
		super.init();
		if (init_failed == null && args != null && args.length >= 1)
			restore(args[0]);
	}

	// -----------------------------------------------------------------------------
	private static void parse(String output, int codepage) throws IOException {
		StringBuffer sb = new StringBuffer();
		InputStreamReader in = new InputStreamReader(System.in);
		for (int ch; (ch = in.read()) >= 0; sb.append((char) ch))
			;
		in.close();

		OutputStream out = Utils.ZIP(output, new FileOutputStream(output));
		out.write(PKIO.text2basic(output, sb.toString(), codepage));
		out.close();
	}

	// -----------------------------------------------------------------------------
	private static void view(String input, int codepage) throws IOException {
		InputStream in = Utils.ZIP(input, new FileInputStream(input));
		System.out.print(PKIO.basic2text(in, codepage));
		in.close();
	}

	// -----------------------------------------------------------------------------
	// S t a n d a l o n e I n i t i a l i z a t i o n
	// -----------------------------------------------------------------------------
	public static void main(String[] args) throws Exception {
		try {
			EmulatorMain em = new EmulatorMain();
			if (args.length <= 0 || !args[0].startsWith("-")) {
				// ok, bootup (maybe with one dumpfile)
				
				em.args = args;
				em.init();
				em.setVisible(true);
				em.start();
				return;
			}

			// -----------------------------------------------------------------------------
			if (args[0].equals("-d")) {
				em.config_dump(args[1]);
				System.out.println(args[1] + " - Config Dumped OK");
			} else if (args[0].startsWith("-p")) {
				if (args[0].endsWith("866"))
					parse(args[1], PKIO.cp_866_u);
				else if (args[0].endsWith("1251"))
					parse(args[1], PKIO.cp_1251_u);
				else if (args[0].endsWith("koi8"))
					parse(args[1], PKIO.cp_koi8_u);
				else
					parse(args[1], PKIO.cp_default);
				System.out.println(args[1] + " - Stdin Parsed OK");
			} else if (args[0].startsWith("-v")) {
				if (args[0].endsWith("866"))
					view(args[1], PKIO.cp_866_u);
				else if (args[0].endsWith("1251"))
					view(args[1], PKIO.cp_1251_u);
				else if (args[0].endsWith("koi8"))
					view(args[1], PKIO.cp_koi8_u);
				else
					view(args[1], PKIO.cp_default);
			}

			// -----------------------------------------------------------------------------
			else {
				System.out.println("USAGE: emul.class -d <conf_file> to dump defaults\n"
						+ "                  -p <basic_file> to produce basic .lvt from textual stdin\n"
						+ "                     -p866 -p1251 -pkoi8 allows you to specify codepage,\n"
						+ "                     -p just uses default and it isn't good on wintel\n"
						+ "                  -v <basic_file> to list/view basic .lvt onto textual stdout\n"
						+ "                     -v866 -v1251 -vkoi8 allows you to specify codepage\n");
				throw new Exception("Unrecognized Command Line");
			}

			// -----------------------------------------------------------------------------
		} catch (Exception ex) {
			System.out.println("Unhappy exception happened :-(");
			ex.printStackTrace();
			System.err.println("---");
			throw ex; // System.exit(0);
		}
	}

	// -----------------------------------------------------------------------------
}
