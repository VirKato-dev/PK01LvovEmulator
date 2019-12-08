package com.lvovpk;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

/**
 * Extended Lvov Emulator
 */
public abstract class ExtendedEmulator extends PrimitiveEmulator {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4020586722385270698L;
	static final int CM_MODE = 1;
	static final int CM_RESET = 2;
	static final int CM_DUMP_F = 3;
	static final int CM_DUMP_P = 4;
	static final int CM_COPY = 5;
	static final int CM_EXPORT = 6;
	static final int CM_RESTORE = 7;
	static final int CM_LOAD = 8;
	static final int CM_IMPORT = 9;
	static final int CM_PASTE = 10;
	static final int CM_FAST = 11;
	static final int CM_SLOW = 12;
	static final int CM_PAUSE = 13;
	static final int CM_RESUME = 14;
	static final int CM_SNAP = 15;
	static final int CM_OPEN_PRN = 16;
	static final int CM_CLOSE_PRN = 17;
	static final int CM_VOLUME = 18;

	String ireq2, overlay;
	int ireq3;

	// -----------------------------------------------------------------------------
	// I n t e r f a c e
	// -----------------------------------------------------------------------------
	public final void doChangeMode() {
		ireq = CM_MODE;
	} // change rendering mode

	public final void doReset() {
		ireq = CM_RESET;
	} // perform cold_start

	public final void doFullDump(String name) {
		ireq2 = name;
		ireq = CM_DUMP_F;
	} // do full dump of computer state

	public final void doPartialDump(String name) {
		ireq2 = name;
		ireq = CM_DUMP_P;
	} // do partial dump of computer state

	public final String doCopy() // !!! SYNC !!!
	{
		return copy();
	} // return basic program as text

	public final void doExport(String name) {
		ireq2 = name;
		ireq = CM_EXPORT;
	} // export basic program as text

	public final void doRestore(String name) {
		ireq2 = name;
		ireq = CM_RESTORE;
	} // restore computer state from dump

	public final void doLoad(String name) {
		ireq2 = overlay = name;
		ireq = CM_LOAD;
	} // load program and launch it

	public final void doImport(String name) {
		ireq2 = overlay = name;
		ireq = CM_IMPORT;
	} // import program and launch it

	public final void doPaste(String body) {
		overlay = "";
		ireq2 = body;
		ireq = CM_PASTE;
	} // import program from argument

	public final void doFast() {
		ireq = CM_FAST;
	} // emulate at full speed

	public final void doSlow() {
		ireq = CM_SLOW;
	} // emulate synchronizing with timer

	public final void doPause() {
		ireq = CM_PAUSE;
	} // pause execution

	public final void doResume() {
		ireq = CM_RESUME;
	} // resume execution

	public final void doSnap(String name) {
		ireq2 = name;
		ireq = CM_SNAP;
	} // make screen snapshot

	public final void doOpenPrn(String name) {
		ireq2 = name;
		ireq = CM_OPEN_PRN;
	} // open printer device

	public final void doClosePrn() {
		ireq = CM_CLOSE_PRN;
	} // close printer device

	public final void doVolume(int loud) {
		ireq3 = loud;
		ireq = CM_VOLUME;
	} // set sound volume

	// -----------------------------------------------------------------------------
	// I m p l e m e n t a t i o n
	// -----------------------------------------------------------------------------
	boolean dump(String name, boolean full) {
		writeLog("Dumping " + (full ? "full" : "partial") + " state...");
		try {
			OutputStream dump = Utils.ZIP(name, new FileOutputStream(name));
			lv.dump(dump, full);
			dump.close();
			return true;
		} catch (Exception ex) {
			writeLog("Unable to dump state: " + ex);
			return false;
		}
	}

	// -----------------------------------------------------------------------------
	boolean restore(String name) {
		writeLog("Restoring state...");
		try {
			InputStream dump = new FileInputStream(name);
			lv.restore(Utils.ZIP(name, dump));
			dump.close();
			return true;
		} catch (Exception ex) {
			writeLog("Unable to restore state: " + ex);
			return false;
		}
	}

	// -----------------------------------------------------------------------------
	boolean load(String name) {
		return load(name, true);
	}

	private boolean load(String name, boolean as) {
		writeLog("Loading Program...");
		try {
			InputStream prog = new FileInputStream(name);
			lv.loadProg(Utils.ZIP(name, prog), as);
			prog.close();
			return true;
		} catch (Exception ex) {
			writeLog("Unable to load program: " + ex);
			return false;
		}
	}

	// -----------------------------------------------------------------------------
	private String newOverlay(String name) {
		int at = name.lastIndexOf(".lv"); // finding extension
		if (at == -1)
			return name + ".next"; // no one, exiting...

		String prefix = name.substring(0, at);
		String suffix = name.substring(at + 4);
		char num = name.charAt(at + 3);

		// handle continuous suffix overflow
		if (num == 'o')
			return prefix + ".lv" + "oo" + suffix;

		int dig = Character.digit(num, 10);
		if (dig < 0)
			num = '0';
		else
			num = Character.forDigit(dig + 1, 10); // handle next suffix
		if (num == '\u0000')
			num = 'o'; // handle first suffix overflow

		return prefix + ".lv" + num + suffix;
	}

	// -----------------------------------------------------------------------------
	boolean loadAsText(String name) {
		writeLog("Importing Program...");
		try {
			InputStreamReader src = new InputStreamReader(Utils.ZIP(name, new FileInputStream(name)));
			StringBuffer sb = new StringBuffer();
			for (int ch; (ch = src.read()) >= 0; sb.append((char) ch));
			src.close();
			return paste(sb.toString());
		} catch (Exception ex) {
			writeLog("Unable to import program: " + ex);
			return false;
		}
	}

	// -----------------------------------------------------------------------------
	boolean paste(String body) {
		writeLog("Pasting Program...");
		try {
			InputStream is = new ByteArrayInputStream(PKIO.text2basic(body, PKIO.CP_DEFAULT));
			lv.loadProg(is, true);
			is.close();
			return true;
		} catch (Exception ex) {
			writeLog("Unable to paste program: " + ex);
			return false;
		}
	}

	// -----------------------------------------------------------------------------
	boolean saveAsText(String name) {
		writeLog("Exporting Program...");
		try {
			PrintWriter dst = new PrintWriter(Utils.ZIP(name, new FileOutputStream(name)));
			dst.print(copy(name));
			dst.close();
			return true;
		} catch (Exception ex) {
			writeLog("Unable to export program: " + ex);
			return false;
		}
	}

	// -----------------------------------------------------------------------------
	// can be invoked asynchronously, doesn't modify guest
	String copy() {
		return copy("AUTO");
	}

	String copy(String name) {
		writeLog("Copying Program...");
		try {
			ByteArrayOutputStream tmp = new ByteArrayOutputStream();
			lv.saveProg(tmp, name);

			ByteArrayInputStream is = new ByteArrayInputStream(tmp.toByteArray());
			tmp.close();

			String result = PKIO.basic2text(is, PKIO.CP_DEFAULT);

			is.close();
			return result;
		} catch (Exception ex) {
			writeLog("Unable to copy program: " + ex);
			return "";
		}
	}

	// -----------------------------------------------------------------------------
	boolean snap(String name) {
		writeLog("Taking a screenshot...");
		try {
			String fortmatName = Utils.getFileExtension(name).toLowerCase();
			if (fortmatName.equals("bmp")) {
				OutputStream snap = Utils.ZIP(name, new FileOutputStream(name));
				lv.snapshot(snap);
				snap.close();
			}
			else {
				BufferedImage image = new BufferedImage(lv.getWidth(), lv.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D g2d = image.createGraphics();
				lv.paint(g2d);
				if (fortmatName.equals("png")
					|| fortmatName.equals("gif")
					|| fortmatName.equals("jpg")) {
						ImageIO.write(image, fortmatName, new File(name));
				}
				else {
					ImageIO.write(image, "png", new File(name + ".png"));
				}
				g2d.dispose();
			}
			return true;
		} catch (Exception ex) {
			writeLog("Unable to take a screenshot: " + ex);
			return false;
		}
	}

	// -----------------------------------------------------------------------------
	@Override
	boolean openPrinter(String name) {
		writeLog("Opening printer...");
		if (super.openPrinter(name)) {
			return true;
		} else {
			writeLog("Unable to open printer!");
			return false;
		}
	}

	// -----------------------------------------------------------------------------
	@Override
	boolean closePrinter() {
		writeLog("Closing printer...");
		if (super.closePrinter()) {
			return true;
		} else {
			writeLog("Unable to close printer!");
			return false;
		}
	}

	// -----------------------------------------------------------------------------
	@Override
	void printThis(byte[] buffer) {
		if (printerDevice != null && buffer != null) {
			writeLog("Printing...");
			super.printThis(buffer);
		}
	}

	// -----------------------------------------------------------------------------
	// D i s p a t c h e r
	// -----------------------------------------------------------------------------
	@Override
	public void run() {
		for (;;) {
			super.run();
			switch (ireq) {
			default:
				if (lv.pk.cpuHaltState && ireq == 0) // what's the matter of halt ?
				{
					if (lv.pk.cpuHaltReason == Lvov.HALT_BPX) // bpx on load ?
					{
						if (!load(overlay = newOverlay(overlay), false))
							doPause();
						else {
							lv.pk.simuret();
							lv.pk.cpuHaltState = false;
						}
					} else // unknown, say to user that we're halted :-Q
					{
						writeLog("CPU halted at " + Utils.HEX(lv.pk.rPC) + " // Reason: " + lv.pk.cpuHaltReason);
						doPause();
					}
					break;
				}
				return;
			case CM_MODE:
				if (mode < PK00.MODE_LAST)
					mode++;
				else
					mode = PK00.MODE_FIRST;
				ireq = CM_REPAINT;
				break;
			case CM_RESET:
				writeLog("System reset!");
				lv.coldStart();
				ireq = 0;
				break;
			case CM_LOAD:
				load(ireq2);
				ireq = 0;
				break;
			case CM_IMPORT:
				loadAsText(ireq2);
				ireq = 0;
				break;
			case CM_EXPORT:
				saveAsText(ireq2);
				ireq = 0;
				break;
			case CM_PASTE:
				paste(ireq2);
				ireq = 0;
				break;
			case CM_DUMP_F:
				dump(ireq2, true);
				ireq = 0;
				break;
			case CM_DUMP_P:
				dump(ireq2, false);
				ireq = 0;
				break;
			case CM_RESTORE:
				restore(ireq2);
				ireq = 0;
				break;
			case CM_SNAP:
				snap(ireq2);
				ireq = 0;
				break;
			case CM_FAST:
				goFast = true;
				ireq = 0;
				break;
			case CM_SLOW:
				goFast = false;
				ireq = 0;
				break;
			case CM_OPEN_PRN:
				openPrinter(ireq2);
				ireq = 0;
				break;
			case CM_CLOSE_PRN:
				closePrinter();
				ireq = 0;
				break;
			case CM_VOLUME:
				setVolume(ireq3);
				ireq = 0;
				break;
			case CM_RESUME:
				ireq = 0;
				break;
			case CM_PAUSE:
				while (ireq == CM_PAUSE)
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ignored) {
					}
				break;
			}
		}
	}

	// -----------------------------------------------------------------------------
	@Override
	public void init() {
		super.init();
		if (initFailed == null)
			lv.pk.bpx = Bios.LOAD_BINARY_BPX;
	}

	// -----------------------------------------------------------------------------
}
