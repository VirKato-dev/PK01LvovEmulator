package com.lvovpk;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.swing.UIManager;

/**
 * Entry Point to Lvov Emulator
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
		if (initFailed == null && args != null && args.length >= 1 && !args[0].equals("-r"))
			restore(args[0]);
		else if (initFailed == null && args != null && args.length >= 3 && args[0].equals("-r"))
			restore(args[2]);
	}

	// -----------------------------------------------------------------------------
	private static void parse(String output, int codepage) throws IOException {
		StringBuffer sb = new StringBuffer();
		InputStreamReader in = new InputStreamReader(System.in);
		for (int ch; (ch = in.read()) >= 0; sb.append((char) ch));
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
	// I n i t i a l i z a t i o n
	// -----------------------------------------------------------------------------
	public static void main(String[] args) throws Exception {
		if (System.getProperty("swing.defaultlaf") == null) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {
			}
		}
		try {
			EmulatorMain em = new EmulatorMain();
			if (args.length <= 0 || args[0].equals("-r") || !args[0].startsWith("-")) {

				if (args.length > 1 && args[0].equals("-r")) {
					em.setConfigFileName(args[1]);
				}

				em.args = args;

				// ok, bootup (maybe with one dumpfile)
				em.init();
				em.setVisible(true);
				em.start();
				return;
			}

			// -----------------------------------------------------------------------------
			if (args[0].equals("-d")) {
				em.configDump(args[1]);
				System.out.println(args[1] + " - Config Dumped OK");
			} else if (args[0].startsWith("-p")) {
				if (args[0].endsWith("866"))
					parse(args[1], PKIO.CP_866_U);
				else if (args[0].endsWith("1251"))
					parse(args[1], PKIO.CP_1251_U);
				else if (args[0].endsWith("koi8"))
					parse(args[1], PKIO.CP_KOI8_U);
				else
					parse(args[1], PKIO.CP_DEFAULT);
				System.out.println(args[1] + " - Stdin Parsed OK");
			} else if (args[0].startsWith("-v")) {
				if (args[0].endsWith("866"))
					view(args[1], PKIO.CP_866_U);
				else if (args[0].endsWith("1251"))
					view(args[1], PKIO.CP_1251_U);
				else if (args[0].endsWith("koi8"))
					view(args[1], PKIO.CP_KOI8_U);
				else
					view(args[1], PKIO.CP_DEFAULT);
			}

			// -----------------------------------------------------------------------------
			else {
				System.out.println("USAGE: PK01LvovEmulator.jar -d <conf_file> to dump default configurations" + PKIO.NL
						+ "                  -r <conf_file> to replace default configurations" + PKIO.NL
						+ "                  -p <basic_file> to produce basic .lvt from textual stdin" + PKIO.NL
						+ "                     -p866 -p1251 -pkoi8 allows you to specify codepage," + PKIO.NL
						+ "                     -p just uses default and it isn't good on wintel" + PKIO.NL
						+ "                  -v <basic_file> to list/view basic .lvt onto textual stdout" + PKIO.NL
						+ "                     -v866 -v1251 -vkoi8 allows you to specify codepage" + PKIO.NL
						+ "                  -h show this information" + PKIO.NL);
				if (!args[0].equals("-h")) {
					throw new Exception("Unrecognized Command Line");
				}
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
