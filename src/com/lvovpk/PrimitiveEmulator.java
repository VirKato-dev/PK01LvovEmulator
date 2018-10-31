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
	static final int cmRepaint = -1; // repaint emulator screen
	static final int cmStop = -2; // stop emulator
	int mode;
	PK01 lv;
	int ireq;
	boolean go_fast, go_sound, speak_slow, fullScreen;
	int speak_mode;
	int ticks;
	byte volume_up = 127, volume_down = 127;
	Thread framer;
	LayoutManager fly;
	String init_failed = null;
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
	boolean open_printer(String name) {
		try {
			if (printerDevice != null)
				close_printer();
			printerDevice = Utils.ZIP(name, new FileOutputStream(name));
			lv.init_printer(4096);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	boolean close_printer() {
		try {
			if (printerDevice != null)
				printerDevice.close();
			printerDevice = null;
			lv.init_printer(0);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	void print_this(byte[] buffer) {
		if (printerDevice != null && buffer != null)
			try {
				printerDevice.write(buffer);
			} catch (Exception ex) {
			}
	}

	// -----------------------------------------------------------------------------
	void set_volume(int percent) {
		if (percent < 0)
			volume_up = 127;
		else if (percent > 100)
			volume_up = 0;
		else
			volume_up = (byte) (127 * (100 - percent) / 100);

		if (go_sound) {
			// up to 1 second of sound, see: run()
			lv.init_speaker(volume_up == volume_down ? 0 : 8000);
		}
	}

	// -----------------------------------------------------------------------------
	@Override
	public void run() {
		if (ireq == cmRepaint) {
			lv.render_as(mode);
			lv.invalidate();
			validate();
			lv.repaint();
			ireq = 0;
		}

		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		int frames = 0, fps = 50, wait_cycle, idle = 0;
		long start_frame = System.currentTimeMillis(), // we wanna know FPS
				start_sound = start_frame, // we wanna synchronize...
				start_cycle, stop_cycle;

		lv.pk.clock = 0;

		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		while (ireq == 0 && !lv.pk.cpu_halt_state) {
			start_cycle = System.currentTimeMillis();
			lv.update_image();
			frames++;
			lv.emulate(ticks);
			stop_cycle = System.currentTimeMillis();
			wait_cycle = (int) (stop_cycle - start_cycle);

			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			if ((stop_cycle - start_frame) > 1000) {
				print_this(lv.printed());
				fps = frames;
				if (fps < 1)
					fps = 1;
				showStatus("FPS: " + fps + " // Speed: " + frames * 2 + "%" + " // Idle: " + idle / 10 + "%");
				idle = frames = 0;
				start_frame = stop_cycle;
				stop_cycle = System.currentTimeMillis();
				wait_cycle = (int) (stop_cycle - start_cycle);
			}

			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			if (speak_slow) // synchronize every 1/5 of second
			{
				if ((stop_cycle - start_sound) > 200) {
					if (go_sound)
						Sound.play(speak_mode, 200, volume_down, volume_up, lv.speaked());
					lv.pk.clock = 0;
					start_sound = stop_cycle;
					stop_cycle = System.currentTimeMillis();
					wait_cycle = (int) (stop_cycle - start_cycle);
				}
			} else // each rendering cycle (i.e. 1/50 of second)
			{
				if (go_sound)
					Sound.play(speak_mode, 20, volume_down, volume_up, lv.speaked());

				lv.pk.clock = 0;
				start_sound = stop_cycle;
				stop_cycle = System.currentTimeMillis();
				wait_cycle = (int) (stop_cycle - start_cycle);
			}

			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			if (wait_cycle >= 20)
				wait_cycle = 1;
			else
				wait_cycle = 20 - wait_cycle;
			if (!go_fast) {
				idle += wait_cycle;
				try {
					Thread.sleep(wait_cycle);
				} catch (InterruptedException Ex) {
				}
			}
		}
	}

	// -----------------------------------------------------------------------------
	public void start() {
		if (init_failed != null)
			return;
		lv.requestFocusInWindow();
		if (framer == null)
			framer = new Thread(this);
		if (!framer.isAlive())
			framer.start();
	}

	public void stop() {
		if (init_failed != null)
			return;
		else
			ireq = cmStop;
		for (;;)
			try {
				framer.join();
				break;
			} catch (InterruptedException ex) {
			}
		// try {framer.join();} catch (InterruptedException ex) {} // only one
		// attempt
	}

	// -----------------------------------------------------------------------------
	// N a t i v e B r o w s e r I n i t i a l i z a t i o n
	// -----------------------------------------------------------------------------
	String cfg(String nm) {
		String s = (String) Defaults.cfg.get(nm);
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
				catch(Exception e) {
					fullScreen = false;
				}
			}
			getContentPane().setBackground(Color.BLACK);
			
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			mode = Integer.parseInt(cfg("Mode"));
			ticks = Integer.parseInt(cfg("CpuTicks"));
			
			lv = new PK01(mode);
			go_fast = !cfg("Sync", "yes");
			lv.pk.halt_if_invalid = cfg("HALT_ON_INVALID", "yes");

			speak_mode = Integer.parseInt(cfg("SpeakMode"));
			speak_slow = cfg("SpeakSlow", "yes");
			int volume = Integer.parseInt(cfg("Speaker"));
			if (go_sound = (volume >= 0)) {
				Sound.init();
				set_volume(volume);
			}

			if (!cfg("Printer", "<none>"))
				if (!open_printer(cfg("Printer")))
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
				nBoot = cfg("BiosFile"); // First from file system
				try {
					sBoot = new FileInputStream(nBoot);
					lv.load_bios(Utils.ZIP(nBoot, sBoot));
					sBoot.close();
				} catch (Exception ex1) // If failed then from ".JAR"
				{
					try {
						sBoot = getClass().getResourceAsStream(nBoot);
						lv.load_bios(Utils.ZIP(nBoot, sBoot));
						sBoot.close();
					} catch (Exception ex2) // We ignoring ex2 cause second
					{ // loading attempt is less important than the first one
						throw new Exception("Can't load BIOS: " + ex1);
					}
				}
			}
			if (bBootCold)
				lv.cold_start();

			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			Set<String> e = Keyboard.as_int.keySet();
			for (String n : e) {
				Integer vk = (Integer) Keyboard.as_int.get(n);
				String s = cfg(n);
				if (!s.equals("")) {
					s = new StringTokenizer(s).nextToken();
					lv.set_kb(vk.intValue(), Integer.parseInt(s, 16));
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
			
			ireq = cmRepaint;

			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		} catch (Exception ex) {
			if (lv != null)
				remove(lv);
			add(new JLabel(init_failed = ex.toString()), BorderLayout.CENTER);
		}
	}

	// -----------------------------------------------------------------------------
	public void destroy() {
		Sound.done();
		close_printer();
	}

	abstract void showStatus(String status);
	// -----------------------------------------------------------------------------
}
