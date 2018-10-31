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
	String ireq2, Overlay;
	int ireq3;

	// -----------------------------------------------------------------------------
	// U t i l i t y
	// -----------------------------------------------------------------------------
	abstract void writeLog(String msg);

	// -----------------------------------------------------------------------------
	// I n t e r f a c e
	// -----------------------------------------------------------------------------
	static final int cmMode = 1;

	public final void do_cmode() {
		ireq = cmMode;
	} // change rendering mode

	static final int cmReset = 2;

	public final void do_reset() {
		ireq = cmReset;
	} // perform cold_start

	static final int cmDumpF = 3;

	public final void do_full_dump(String Name) {
		ireq2 = Name;
		ireq = cmDumpF;
	} // do full dump of computer state

	static final int cmDumpP = 4;

	public final void do_partial_dump(String Name) {
		ireq2 = Name;
		ireq = cmDumpP;
	} // do partial dump of computer state

	static final int cmCopy = 5;

	public final String do_copy() // !!! SYNC !!!
	{
		return copy();
	} // return basic program as text

	static final int cmExport = 6;

	public final void do_export(String Name) {
		ireq2 = Name;
		ireq = cmExport;
	} // export basic program as text

	static final int cmRestore = 7;

	public final void do_restore(String Name) {
		ireq2 = Name;
		ireq = cmRestore;
	} // restore computer state from dump

	static final int cmLoad = 8;

	public final void do_load(String Name) {
		ireq2 = Overlay = Name;
		ireq = cmLoad;
	} // load program and launch it

	static final int cmImport = 9;

	public final void do_import(String Name) {
		ireq2 = Overlay = Name;
		ireq = cmImport;
	} // import program and launch it

	static final int cmPaste = 10;

	public final void do_paste(String Body) {
		Overlay = "";
		ireq2 = Body;
		ireq = cmPaste;
	} // import program from argument

	static final int cmFast = 11;

	public final void do_fast() {
		ireq = cmFast;
	} // emulate at full speed

	static final int cmSlow = 12;

	public final void do_slow() {
		ireq = cmSlow;
	} // emulate synchronizing with timer

	static final int cmPause = 13;

	public final void do_pause() {
		ireq = cmPause;
	} // pause execution

	static final int cmResume = 14;

	public final void do_resume() {
		ireq = cmResume;
	} // resume execution

	static final int cmSnap = 15;

	public final void do_snap(String Name) {
		ireq2 = Name;
		ireq = cmSnap;
	} // make screen snapshot

	static final int cmOpenPRN = 16;

	public final void do_open_prn(String Name) {
		ireq2 = Name;
		ireq = cmOpenPRN;
	} // open printer device

	static final int cmClosePRN = 17;

	public final void do_close_prn() {
		ireq = cmClosePRN;
	} // close printer device

	static final int cmVolume = 18;

	public final void do_volume(int Loud) {
		ireq3 = Loud;
		ireq = cmVolume;
	} // set sound volume

	// -----------------------------------------------------------------------------
	// I m p l e m e n t a t i o n
	// -----------------------------------------------------------------------------
	boolean dump(String name, boolean full) {
		writeLog("Dumping " + (full ? "full" : "partial") + " state...");
		try {
			OutputStream Dump = Utils.ZIP(name, new FileOutputStream(name));
			lv.dump(Dump, full);
			Dump.close();
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
			InputStream Dump = new FileInputStream(name);
			lv.restore(Utils.ZIP(name, Dump));
			Dump.close();
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
			lv.load_prog(Utils.ZIP(name, prog), as);
			prog.close();
			return true;
		} catch (Exception ex) {
			writeLog("Unable to load program: " + ex);
			return false;
		}
	}

	// -----------------------------------------------------------------------------
	private String new_overlay(String name) {
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
	boolean load_as_text(String name) {
		writeLog("Importing Program...");
		try {
			InputStreamReader src = new InputStreamReader(Utils.ZIP(name, new FileInputStream(name)));
			StringBuffer sb = new StringBuffer();
			for (int ch; (ch = src.read()) >= 0; sb.append((char) ch))
				;
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
			InputStream is = new ByteArrayInputStream(PKIO.text2basic(body, PKIO.cp_default));
			lv.load_prog(is, true);
			is.close();
			return true;
		} catch (Exception ex) {
			writeLog("Unable to paste program: " + ex);
			return false;
		}
	}

	// -----------------------------------------------------------------------------
	boolean save_as_text(String name) {
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

	String copy(String Name) {
		writeLog("Copying Program...");
		try {
			ByteArrayOutputStream tmp = new ByteArrayOutputStream();
			lv.save_prog(tmp, Name);

			ByteArrayInputStream is = new ByteArrayInputStream(tmp.toByteArray());
			tmp.close();

			String result = PKIO.basic2text(is, PKIO.cp_default).toString();

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
	boolean open_printer(String name) {
		writeLog("Opening printer...");
		if (super.open_printer(name)) {
			return true;
		} else {
			writeLog("Unable to open printer!");
			return false;
		}
	}

	// -----------------------------------------------------------------------------
	@Override
	boolean close_printer() {
		writeLog("Closing printer...");
		if (super.close_printer()) {
			return true;
		} else {
			writeLog("Unable to close printer!");
			return false;
		}
	}

	// -----------------------------------------------------------------------------
	@Override
	void print_this(byte[] buffer) {
		if (printerDevice != null && buffer != null) {
			writeLog("Printing...");
			super.print_this(buffer);
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
				if (lv.pk.cpu_halt_state && ireq == 0) // what's the matter of halt ?
				{
					if (lv.pk.cpu_halt_reason == Lvov.halt_bpx) // bpx on load ?
					{
						if (!load(Overlay = new_overlay(Overlay), false))
							do_pause();
						else {
							lv.pk.simuret();
							lv.pk.cpu_halt_state = false;
						}
					} else // unknown, say to user that we're halted :-Q
					{
						writeLog("CPU halted at " + Utils.HEX(lv.pk.r_PC) + " // Reason: " + lv.pk.cpu_halt_reason);
						do_pause();
					}
					break;
				}
				return;
			case cmMode:
				if (mode < PK00.mode_last)
					mode++;
				else
					mode = PK00.mode_first;
				ireq = cmRepaint;
				break;
			case cmReset:
				writeLog("System reset!");
				lv.cold_start();
				ireq = 0;
				break;
			case cmLoad:
				load(ireq2);
				ireq = 0;
				break;
			case cmImport:
				load_as_text(ireq2);
				ireq = 0;
				break;
			case cmExport:
				save_as_text(ireq2);
				ireq = 0;
				break;
			case cmPaste:
				paste(ireq2);
				ireq = 0;
				break;
			case cmDumpF:
				dump(ireq2, true);
				ireq = 0;
				break;
			case cmDumpP:
				dump(ireq2, false);
				ireq = 0;
				break;
			case cmRestore:
				restore(ireq2);
				ireq = 0;
				break;
			case cmSnap:
				snap(ireq2);
				ireq = 0;
				break;
			case cmFast:
				go_fast = true;
				ireq = 0;
				break;
			case cmSlow:
				go_fast = false;
				ireq = 0;
				break;
			case cmOpenPRN:
				open_printer(ireq2);
				ireq = 0;
				break;
			case cmClosePRN:
				close_printer();
				ireq = 0;
				break;
			case cmVolume:
				set_volume(ireq3);
				ireq = 0;
				break;
			case cmResume:
				ireq = 0;
				break;
			case cmPause:
				while (ireq == cmPause)
					try {
						Thread.sleep(1000);
					} catch (Exception Ex) {
					}
				break;
			}
		}
	}

	// -----------------------------------------------------------------------------
	@Override
	public void init() {
		super.init();
		if (init_failed == null)
			lv.pk.bpx = Bios.LoadBinaryBpx;
	}

	// -----------------------------------------------------------------------------
}
