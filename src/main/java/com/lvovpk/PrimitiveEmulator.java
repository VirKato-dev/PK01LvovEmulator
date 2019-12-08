package com.lvovpk;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Primitive Lvov Emulator
 */
public abstract class PrimitiveEmulator extends JFrame implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1660208074679142261L;
	static final int CM_REPAINT = -1; // repaint emulator screen
	static final int CM_STOP = -2; // stop emulator
	int mode;
	PK01 lv;
	int ireq;
	boolean goFast, goSound, speakSlow, fullScreen;
	int speakMode;
	int ticks;
	byte volumeUp = 127, volumeDown = 127;
	Thread framer;
	LayoutManager fly;
	String initFailed = null;
	OutputStream printerDevice;

	// -----------------------------------------------------------------------------
	@Override
	public void validate() {
		if (!fullScreen) {
			LayoutManager fly = getLayout();
			if (fly != null)
				setSize(fly.preferredLayoutSize(this));
		}
		super.validate();
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	boolean openPrinter(String name) {
		try {
			if (printerDevice != null)
				closePrinter();
			printerDevice = Utils.ZIP(name, new FileOutputStream(name));
			lv.initPrinter(4096);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	boolean closePrinter() {
		try {
			if (printerDevice != null)
				printerDevice.close();
			printerDevice = null;
			lv.initPrinter(0);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	void printThis(byte[] buffer) {
		if (printerDevice != null && buffer != null)
			try {
				printerDevice.write(buffer);
			} catch (Exception ignored) {
			}
	}

	// -----------------------------------------------------------------------------
	void setVolume(int percent) {
		if (percent < 0)
			volumeUp = 127;
		else if (percent > 100)
			volumeUp = 0;
		else
			volumeUp = (byte) (127 * (100 - percent) / 100);

		if (goSound) {
			// up to 1 second of sound, see: run()
			lv.initSpeaker(volumeUp == volumeDown ? 0 : 8000);
		}
	}

	// -----------------------------------------------------------------------------
	@Override
	public void run() {
		if (ireq == CM_REPAINT) {
			lv.renderAs(mode);
			lv.invalidate();
			validate();
			lv.repaint();
			ireq = 0;
		}

		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		int frames = 0, fps = 50, waitCycle, idle = 0;
		long startFrame = System.currentTimeMillis(), // we wanna know FPS
				startSound = startFrame, // we wanna synchronize...
				startCycle, stopCycle;

		lv.pk.clock = 0;

		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		while (ireq == 0 && !lv.pk.cpuHaltState) {
			startCycle = System.currentTimeMillis();
			lv.updateImage();
			frames++;
			lv.emulate(ticks);
			stopCycle = System.currentTimeMillis();
			waitCycle = (int) (stopCycle - startCycle);

			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			if ((stopCycle - startFrame) > 1000) {
				printThis(lv.printed());
				fps = frames;
				if (fps < 1)
					fps = 1;
				showStatus("FPS: " + fps + " // Speed: " + frames * 2 + "%" + " // Idle: " + idle / 10 + "%");
				idle = frames = 0;
				startFrame = stopCycle;
				stopCycle = System.currentTimeMillis();
				waitCycle = (int) (stopCycle - startCycle);
			}

			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			if (speakSlow) // synchronize every 1/5 of second
			{
				if ((stopCycle - startSound) > 200) {
					if (goSound)
						Sound.play(speakMode, 200, volumeDown, volumeUp, lv.spoken());
					lv.pk.clock = 0;
					startSound = stopCycle;
					stopCycle = System.currentTimeMillis();
					waitCycle = (int) (stopCycle - startCycle);
				}
			} else // each rendering cycle (i.e. 1/50 of second)
			{
				if (goSound)
					Sound.play(speakMode, 20, volumeDown, volumeUp, lv.spoken());

				lv.pk.clock = 0;
				startSound = stopCycle;
				stopCycle = System.currentTimeMillis();
				waitCycle = (int) (stopCycle - startCycle);
			}

			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			if (waitCycle >= 20)
				waitCycle = 1;
			else
				waitCycle = 20 - waitCycle;
			if (!goFast) {
				idle += waitCycle;
				try {
					Thread.sleep(waitCycle);
				} catch (InterruptedException ignored) {
				}
			}
		}
	}

	// -----------------------------------------------------------------------------
	public void start() {
		if (initFailed != null)
			return;
		lv.requestFocusInWindow();
		if (framer == null)
			framer = new Thread(this);
		if (!framer.isAlive())
			framer.start();
	}

	public void stop() {
		if (initFailed != null)
			return;
		else
			ireq = CM_STOP;
		for (;;)
			try {
				framer.join();
				break;
			} catch (InterruptedException ignored) {
			}
		// try {framer.join();} catch (InterruptedException ex) {} // only one attempt
	}

	// -----------------------------------------------------------------------------
	String cfg(String nm) {
		String s = Defaults.cfg.get(nm);
		if (s == null)
			return "";
		else
			return s;
	}

	boolean cfg(String nm, String val) {
		return cfg(nm).equalsIgnoreCase(val);
	}

	// -----------------------------------------------------------------------------
	public void init() {
		try {

			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			fullScreen = cfg("FullScreen", "yes");
			if (fullScreen) {
				try {
					setExtendedState(JFrame.MAXIMIZED_BOTH); 
					setUndecorated(true);
				}
				catch (Exception ex) {
					fullScreen = false;
				}
			}
			getContentPane().setBackground(Color.BLACK);
			
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			mode = Integer.parseInt(cfg("Mode"));
			ticks = Integer.parseInt(cfg("CpuTicks"));
			
			lv = new PK01(mode);
			goFast = !cfg("Sync", "yes");
			lv.pk.haltIfInvalid = cfg("HaltOnInvalid", "yes");

			speakMode = Integer.parseInt(cfg("SpeakMode"));
			speakSlow = cfg("SpeakSlow", "yes");
			int volume = Integer.parseInt(cfg("Speaker"));
			if (goSound = (volume >= 0)) {
				try {
					Sound.init();
				}
				catch (Exception ex) {
					writeLog("Sound initialization failed: " + ex.getMessage());
					ex.printStackTrace();
				}
				setVolume(volume);
			}

			if (!cfg("Printer", "<none>"))
				if (!openPrinter(cfg("Printer")))
					throw new Exception("Unable to open printer !");

			boolean bBoot = false, bBootCold = false;
			String nBoot;
			InputStream sBoot;

			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			try // First of all - try dump file
			{
				nBoot = cfg("DumpFile");
				sBoot = new FileInputStream(nBoot);
				bBoot = lv.restore(Utils.ZIP(nBoot, sBoot));
				sBoot.close();
			} catch (Exception ex) {
				bBoot = bBootCold = true;
			}

			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			if (bBoot) // Depending on type of dump may need to load BIOS
			{
				nBoot = cfg("BiosFile"); // First from the file system
				try {
					sBoot = new FileInputStream(nBoot);
					lv.loadBios(Utils.ZIP(nBoot, sBoot));
					sBoot.close();
				} catch (Exception ex1) // If failed then from ".JAR"
				{
					try {
						sBoot = getClass().getResourceAsStream(nBoot);
						lv.loadBios(Utils.ZIP(nBoot, sBoot));
						sBoot.close();
					} catch (Exception ex2) {
						throw new Exception("Can't load BIOS: " + ex1);
					}
				}
			}
			if (bBootCold)
				lv.coldStart();

			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			Set<String> e = Keyboard.asInt.keySet();
			for (String n : e) {
				Integer vk = Keyboard.asInt.get(n);
				String s = cfg(n);
				if (!s.equals("")) {
					s = new StringTokenizer(s).nextToken();
					lv.setKb(vk, Integer.parseInt(s, 16));
				}
			}

			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			if (fullScreen) {
				fly = new GridBagLayout();
				setLayout(fly);
				add(lv, new GridBagConstraints());
			}
			else {
				fly = new BorderLayout(0, 0);
				setLayout(fly);
				add(lv, BorderLayout.CENTER);
				setSize(fly.preferredLayoutSize(this));
			}
			
			ireq = CM_REPAINT;

			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		} catch (Exception ex) {
			if (lv != null)
				remove(lv);
			add(new JLabel(initFailed = ex.toString()), BorderLayout.CENTER);
		}
	}

	// -----------------------------------------------------------------------------
	public void destroy() {
		Sound.done();
		closePrinter();
	}

	abstract void showStatus(String status);
	abstract void writeLog(String msg);
	// -----------------------------------------------------------------------------
}
