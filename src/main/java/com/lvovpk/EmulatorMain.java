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

	private static final long serialVersionUID = 4339279222189621874L;
	private String[] args;

	// -----------------------------------------------------------------------------
	@Override
	public void init() {
		super.init();
		if (initFailed == null && args != null && args.length >= 1 && !args[0].equals("-r"))
			restore(args[0]);
		else if (initFailed == null && args != null && args.length >= 3) // args[0] equals "-r"
			restore(args[2]);
	}

	// -----------------------------------------------------------------------------
	private static void parse(String output, int codepage) throws IOException {
		StringBuilder sb = new StringBuilder();
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
	public static void main(String[] args) {
		if (System.getProperty("swing.defaultlaf") == null) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ignored) {
			}
		}
		try {
			EmulatorMain em = new EmulatorMain();
			if (args.length == 0 || args[0].equals("-r") || !args[0].startsWith("-")) {

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
				System.out.println(
						"PK-01 Lvov (PK-01 Lviv) Computer Emulator (Java Version) " + EMULATOR_VERSION + PKIO.NL
								+ "Use the following command-line options: " + PKIO.NL
								+ "    -d <conf_file> to dump the default configurations" + PKIO.NL
								+ "    -r <conf_file> to replace the default configurations" + PKIO.NL
								+ "    -p <basic_file> to produce basic .lvt from textual stdin" + PKIO.NL
								+ "        (-p866 -p1251 -pkoi8 allows you to specify the codepage)" + PKIO.NL
								+ "    -v <basic_file> to list/view basic .lvt onto textual stdout" + PKIO.NL
								+ "        (-v866 -v1251 -vkoi8 allows you to specify the codepage)" + PKIO.NL
								+ "    -h to show this information" + PKIO.NL);
				if (!args[0].equals("-h")) {
					throw new Exception("Unrecognized command line option");
				}
			}

			// -----------------------------------------------------------------------------
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	// -----------------------------------------------------------------------------
}
