package com.lvovpk;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.ImageProducer;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.MenuElement;

/**
 * User Interface for Lvov Emulator
 */
public class EmulatorUI extends ExtendedEmulator implements Gui, MouseListener, KeyListener, WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 46689693694740808L;
	// these are highly internal fellows...
	static final int cmConfig = 19;
	static final int cmInvokeEditor = 20;
	static final int cmSyncEditorIn = 21;
	static final int cmSyncEditorOut = 22;
	static final int cmInvokeDebugger = 23;
	static final int cmInvokeAbout = 24;
	static final int cmInvokeLog = 25;
	static final int cmChangeTicks = 26;
	static final int cmVolCtl = 1000;
	
	static final int menuShortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

	private JPopupMenu pm = null;
	private EditorWindow tx;
	private DebuggerWindow dbg;
	private LogWindow log;
	private JLabel st = null;
	
	private String configFileName = null;

	public final void do_config_dump(String Name) {
		config_dump(Name);
	} // save default configuration

	// -----------------------------------------------------------------------------
	void config_dump(String tf) {
		try {
			String k[];
			PrintWriter o = new PrintWriter(new FileOutputStream(tf));
			o.println("#main");
			o.println("appv.size.x " + getSize().width);
			o.println("appv.size.y " + getSize().height);
			o.println("");

			o.println("#OSD Settings");
			k = Utils.sort(config.keySet(), true);
			for (int i = 0; i < k.length; i++)
				o.println(Utils.padRight(k[i], 40) + config.get(k[i]).toString());
			o.println("");

			o.println("#LVOV Settings");
			k = Utils.sort(Defaults.cfg.keySet(), true);
			for (int i = 0; i < k.length; i++)
				o.println(Utils.padRight(k[i], 40) + Defaults.cfg.get(k[i]).toString());
			o.println("");

			o.close();
		} catch (IOException ex) {
		}
	}

	void config_load(String tf) {
		writeLog("Loading configurations from file...");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(tf));
			String line = reader.readLine();
			while (line != null) {
				line = line.trim();
				if (line.length() >= 3 && !line.startsWith("#")) {
					String[] values = line.split("#")[0].trim().split(" ");
					if (values.length > 1) {
						Defaults.cfg.put(values[0], values[values.length - 1]);
					}
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException ex) {
			writeLog("Unable to load configurations from file: " + ex.getMessage());
		}
	}

	// -----------------------------------------------------------------------------
	// I m p l e m e n t a t i o n
	// -----------------------------------------------------------------------------
	@Override
	public void run() {
		while (ireq != cmStop)
			super.run();
	}

	// -----------------------------------------------------------------------------
	// C o n f i g u r a t i o n
	// -----------------------------------------------------------------------------
	@Override
	String cfg(String nm) {
		String s = super.cfg(nm);
		if (s.equals(""))
			s = (String) config.get(nm);
		if (s == null)
			return "";
		else
			return s;
	}

	// -----------------------------------------------------------------------------
	private static Map<String, String> config;
	static {
		config = new ConcurrentHashMap<String, String>();

		String Menus[][] = new String[][] {
			{ "ToolbarTop", "No" },
			{ "ToolbarBottom", "No" },
			{ "ToolbarMenu", "Yes" },
			{ "ContextMenu", "No" }
		};
		String Features[][] = new String[][] {
			{ "Mode", "No", "Yes", "Yes", "Yes" },
			{ "Fast", "No", "Yes", "Yes", "Yes" },
			{ "Slow", "No", "Yes", "Yes", "Yes" },
			{ "Ticks", "No", "Yes", "Yes", "Yes" },
			{ "Speaker", "Yes", "No", "Yes", "Yes" },
			{ "Reset", "Yes", "No", "Yes", "Yes" },
			{ "Pause", "Yes", "No", "Yes", "Yes" },
			{ "Resume", "Yes", "No", "Yes", "Yes" },
			{ "Load", "Yes", "No", "Yes", "Yes" },
			{ "Import", "Yes", "No", "Yes", "Yes" },
			{ "Edit", "Yes", "No", "Yes", "Yes" },
			{ "Restore", "Yes", "No", "Yes", "Yes" },
			{ "Debug", "Yes", "No", "Yes", "Yes" },
			{ "DumpF", "No", "Yes", "Yes", "Yes" },
			{ "DumpP", "No", "Yes", "Yes", "Yes" },
			{ "Export", "No", "Yes", "Yes", "Yes" },
			{ "Snap", "No", "Yes", "Yes", "Yes" },
			{ "PRN_O", "No", "Yes", "Yes", "Yes" },
			{ "PRN_C", "No", "Yes", "Yes", "Yes" },
			{ "About", "No", "No", "Yes", "No" },
			{ "Log", "Yes", "No", "Yes", "Yes" },
			{ "Cfg", "No", "Yes", "Yes", "Yes" },
			{ "Quit", "No", "No", "Yes", "Yes" }
		};

		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		config.put("Enable_Statusbar", "Yes");
		config.put("Enable_FlatContextMenu", "Yes");
		for (int i = 0; i < Menus.length; i++) {
			config.put("Enable_" + Menus[i][0], Menus[i][1]);
			for (int j = 0; j < Features.length; j++)
				config.put("Enable_" + Menus[i][0] + "_" + Features[j][0] + "_Feature", Features[j][i + 1]);
		}
	}
	
	// -----------------------------------------------------------------------------
	private void mkMenuItem(JMenu mn, String Name, String Feature, int Command, String Description) {
		if (cfg("Enable_" + Name + "_" + Feature + "_Feature", "yes")) {
			int[] shortcut = Keyboard.getShortcutForCommand(Command);
			if (shortcut != null) {
				mn.add(GuiUtils.createMenuItem(Command, Description, shortcut[0], shortcut[1], this));
			}
			else {
				mn.add(GuiUtils.createMenuItem(Command, Description, this));
			}
		}
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	private void mkToolbarButton(JToolBar tb, String Name, String Feature, int Command, String Description) {
		if (cfg("Enable_" + Name + "_" + Feature + "_Feature", "yes"))
			tb.add(GuiUtils.createButton(Command, Description, this));
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	private void mkMenuMenu(MenuElement mc, JMenu mi, boolean flatten) {
		JMenu mn;
		JComponent jc;
		if (mc instanceof JMenu) {
			mn = (JMenu) mc;
			if (!flatten) {
				if (mi.getItemCount() > 0)
					mn.add(mi);
			} else {
				if (mi.getItemCount() > 0)
					mn.addSeparator();
				int ii = mi.getItemCount();
				for (int i = 0; i < ii; i++)
					mn.add(mi.getItem(0));
			}
		}
		else if (mc instanceof JComponent) {
			jc = (JComponent) mc;
			if (mi.getItemCount() > 0)
				jc.add(mi);
		}
	}

	// -----------------------------------------------------------------------------
	private void mkToolbar(String Name, JToolBar tb) {
		mkToolbarButton(tb, Name, "Mode", cmMode, "Mode");
		mkToolbarButton(tb, Name, "Ticks", cmChangeTicks, "Ticks");
		mkToolbarButton(tb, Name, "Fast", cmFast, "Fast");
		mkToolbarButton(tb, Name, "Slow", cmSlow, "Slow");
		mkToolbarButton(tb, Name, "PRN_O", cmOpenPRN, "oPRN");
		mkToolbarButton(tb, Name, "PRN_C", cmClosePRN, "cPRN");

		mkToolbarButton(tb, Name, "Speaker", cmVolCtl + 0, "Mute");
		mkToolbarButton(tb, Name, "Speaker", cmVolCtl + 100, "Loud");

		mkToolbarButton(tb, Name, "Reset", cmReset, "Reset");
		mkToolbarButton(tb, Name, "Pause", cmPause, "Pause");
		mkToolbarButton(tb, Name, "Resume", cmResume, "Resume");
		mkToolbarButton(tb, Name, "Debug", cmInvokeDebugger, "Debug");

		mkToolbarButton(tb, Name, "Load", cmLoad, "Load");
		mkToolbarButton(tb, Name, "Import", cmImport, "Import");
		mkToolbarButton(tb, Name, "Edit", cmInvokeEditor, "Edit");
		mkToolbarButton(tb, Name, "Restore", cmRestore, "Restore");

		mkToolbarButton(tb, Name, "Export", cmExport, "Export");
		mkToolbarButton(tb, Name, "DumpF", cmDumpF, "Full");
		mkToolbarButton(tb, Name, "DumpP", cmDumpP, "Partial");
		mkToolbarButton(tb, Name, "Snap", cmSnap, "Snap");

		mkToolbarButton(tb, Name, "About", cmInvokeAbout, "About");
		mkToolbarButton(tb, Name, "Log", cmInvokeLog, "Log");
		mkToolbarButton(tb, Name, "Cfg", cmConfig, "Conf");
		mkToolbarButton(tb, Name, "Quit", cmStop, "Quit");
	}

	// -----------------------------------------------------------------------------
	private void mkMenu(String Name, MenuElement mn, boolean flatten) {
		JMenu submn;

		submn = new JMenu("Config");
		mkMenuItem(submn, Name, "Mode", cmMode, "Change rendering mode");
		mkMenuItem(submn, Name, "Ticks", cmChangeTicks, "Change CPU Clock Ticks");
		mkMenuItem(submn, Name, "Fast", cmFast, "Emulate at full speed");
		mkMenuItem(submn, Name, "Slow", cmSlow, "Emulate at real speed");
		mkMenuItem(submn, Name, "PRN_O", cmOpenPRN, "Open printer");
		mkMenuItem(submn, Name, "PRN_C", cmClosePRN, "Close printer");

		JMenu submn2 = new JMenu("Volume");
		for (int i = 0; i <= 100; i += 10)
			mkMenuItem(submn2, Name, "Speaker", cmVolCtl + i, "Set volume " + i + "%");
		mkMenuMenu(submn, submn2, false);

		mkMenuMenu(mn, submn, flatten);

		submn = new JMenu("Control");
		mkMenuItem(submn, Name, "Reset", cmReset, "Perform cold start sequence");
		mkMenuItem(submn, Name, "Pause", cmPause, "Pause execution");
		mkMenuItem(submn, Name, "Resume", cmResume, "Resume execution");
		mkMenuItem(submn, Name, "Debug", cmInvokeDebugger, "Invoke Code Debugger");
		mkMenuMenu(mn, submn, flatten);

		submn = new JMenu("Load");
		mkMenuItem(submn, Name, "Load", cmLoad, "Load program");
		mkMenuItem(submn, Name, "Import", cmImport, "Import Basic program");
		mkMenuItem(submn, Name, "Edit", cmInvokeEditor, "Open editor for Basic");
		mkMenuItem(submn, Name, "Restore", cmRestore, "Restore state from the dump");
		mkMenuMenu(mn, submn, flatten);

		submn = new JMenu("Store");
		mkMenuItem(submn, Name, "Export", cmExport, "Export Basic program");
		mkMenuItem(submn, Name, "DumpF", cmDumpF, "Perform full dump of the emulator state");
		mkMenuItem(submn, Name, "DumpP", cmDumpP, "Perform partial dump of the emulator state");
		mkMenuItem(submn, Name, "Snap", cmSnap, "Take a screenshot");
		mkMenuMenu(mn, submn, flatten);

		submn = new JMenu("General");
		mkMenuItem(submn, Name, "About", cmInvokeAbout, "About");
		mkMenuItem(submn, Name, "Log", cmInvokeLog, "Events log");
		mkMenuItem(submn, Name, "Cfg", cmConfig, "Save default configuration");
		mkMenuItem(submn, Name, "Quit", cmStop, "Quit");
		mkMenuMenu(mn, submn, flatten);
	}

	// -----------------------------------------------------------------------------
	private void configure() {

		addWindowListener(this);
		
		tx = new EditorWindow(this, "Simple Basic Editor", false);
		tx.setPeer(cmSyncEditorIn, cmSyncEditorOut, this);
		dbg = new DebuggerWindow(this, "Simple i8080 Debugger (press F1 for help)", true, new LvovDebugger(lv));
		Keyboard.enableShortcuts = cfg("KeyboardShortcuts", "yes");
		if (Keyboard.enableShortcuts && (fullScreen || !cfg("Enable_ToolbarMenu", "yes"))) {
			lv.addKeyListener(this);
		}
		
		if (!fullScreen) {
			JToolBar tb;
			FlowLayout tbl = new FlowLayout();
			tbl.setHgap(0);
			tbl.setVgap(0);
			tbl.setAlignment(FlowLayout.LEFT);
			
			if (cfg("Enable_ToolbarTop", "yes")) {
				JPanel topPanel = new JPanel();
				topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
				tb = new JToolBar();
				tb.setFloatable(false);
				tb.setLayout(tbl);
				tb.setAlignmentX(Component.LEFT_ALIGNMENT);
				mkToolbar("ToolbarTop", tb);
				topPanel.add(tb);
				add(topPanel, BorderLayout.NORTH);
			}
			
			if (cfg("Enable_Statusbar", "yes") || cfg("Enable_ToolbarBottom", "yes")) {
				JPanel bottomPanel = new JPanel();
				bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
				
				if (cfg("Enable_ToolbarBottom", "yes")) {
					tb = new JToolBar();
					tb.setFloatable(false);
					tb.setLayout(tbl);
					tb.setAlignmentX(Component.LEFT_ALIGNMENT);
					mkToolbar("ToolbarBottom", tb);
					bottomPanel.add(tb);
				}
				
				if (cfg("Enable_Statusbar", "yes")) {
					JPanel stb = new JPanel(tbl);
					stb.add(st = new JLabel("Booting..."));
					stb.setAlignmentX(Component.LEFT_ALIGNMENT);
					bottomPanel.add(stb);
				}
				add(bottomPanel, BorderLayout.SOUTH);
			}
			
			if (cfg("Enable_ToolbarMenu", "yes")) {
				JMenuBar mb = new JMenuBar();
				mkMenu("ToolbarMenu", mb, false);
				setJMenuBar(mb);
			}
			
			setSize(getPreferredSize());
			Toolkit tk = getToolkit();
			setLocation((tk.getScreenSize().width - getSize().width) / 2,
					(tk.getScreenSize().height - getSize().height) / 2);
			setSize(fly.preferredLayoutSize(this));
		}
		
		if (cfg("Enable_ContextMenu", "yes")) {
			pm = new JPopupMenu("Context");
			pm.add(new JMenuItem("Focus on the emulator"));
			boolean flat = cfg("Enable_FlatContextMenu", "yes");
			if (!flat)
				pm.addSeparator();
			mkMenu("ContextMenu", pm, flat);
			//add(pm);
			lv.addMouseListener(this);
		}
		
		validate();
	}

	// -----------------------------------------------------------------------------
	// K e y b o a r d L i s t e n e r
	// -----------------------------------------------------------------------------
	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int command = Keyboard.getCommandForShortcut(e);
		if (command != 0) {
			perform(command);
		}
	}

	// -----------------------------------------------------------------------------
	// M o u s e L i s t e n e r
	// -----------------------------------------------------------------------------
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (pm != null)
			pm.show(lv, e.getX(), e.getY());
	}

	// -----------------------------------------------------------------------------
	// W i n d o w L i s t e n e r
	// -----------------------------------------------------------------------------
	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		destroy();
		System.exit(0);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		stop();
		e.getWindow().dispose();
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	// -----------------------------------------------------------------------------
	// I n t e r n a l L i s t e n e r
	// -----------------------------------------------------------------------------
	@Override
	public void perform(int cmd) {
		String fn;

		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		switch (cmd) {
		case cmFast:
			do_fast();
			break;
		case cmSlow:
			do_slow();
			break;
		case cmMode:
			do_cmode();
			break;
		case cmChangeTicks:
			do_pause();
			showChangeTicksDialog();
			do_resume();
			break;
		case cmReset:
			do_reset();
			break;
		case cmPause:
			do_pause();
			break;
		case cmResume:
			do_resume();
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case cmLoad:
			fn = Utils.useFileAsURL(this, "Choose .LVT program", "lvt");
			if (fn != null)
				do_load(fn);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case cmImport:
			fn = Utils.useFileAsURL(this, "Choose .BAS source", "bas");
			if (fn != null)
				do_import(fn);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case cmExport:
			fn = Utils.mkFile(this, "Choose destination .BAS source", "bas");
			if (fn != null)
				do_export(fn);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case cmInvokeEditor:
			perform(cmSyncEditorIn);
			tx.setVisible(true);
			break;
		case cmSyncEditorOut:
			do_paste(tx.txt.getText());
			break;
		case cmSyncEditorIn:
			tx.txt.setText(do_copy());
			break;
		case cmInvokeDebugger:
			do_pause();
			dbg.showWindow();
			do_resume();
			break;
		case cmInvokeAbout:
			do_pause();
			showAboutDialog();
			do_resume();
			break;
		case cmInvokeLog:
			log.setVisible(true);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case cmDumpF:
			fn = Utils.mkFile(this, "Choose destination .LVD file", "lvd");
			if (fn != null)
				do_full_dump(fn);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case cmDumpP:
			fn = Utils.mkFile(this, "Choose destination .LVD file", "lvd");
			if (fn != null)
				do_partial_dump(fn);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case cmSnap:
			do_pause();
			fn = Utils.mkFile(this, "Choose destination image file", "PNG image#png", "GIF image#gif", "JPEG image#jpg", "Bitmap image#bmp");
			do_resume();
			if (fn != null)
				do_snap(fn);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case cmRestore:
			fn = Utils.useFileAsURL(this, "Choose source .LVD file", "lvd");
			if (fn != null)
				do_restore(fn);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case cmConfig:
			fn = Utils.mkFile(this, "Choose configuration file", "cfg");
			if (fn != null)
				do_config_dump(fn);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case cmOpenPRN:
			fn = Utils.mkFile(this, "Choose device to print to", "print");
			if (fn != null)
				do_open_prn(fn);
			break;
		case cmClosePRN:
			do_close_prn();
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case cmStop:
			ireq = cmStop;
			dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		default:
			if (cmd >= cmVolCtl && cmd <= cmVolCtl + 100)
				do_volume(cmd - cmVolCtl);
			break;
		}
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	private void showChangeTicksDialog() {
		String ticksInput = JOptionPane.showInputDialog(this, "New value for CPU Clock Ticks:", ticks);
		if (ticksInput != null) {
			try {
				int ticksNewValue = Integer.parseInt(ticksInput.trim());
				if (ticksNewValue > 0 && ticksNewValue <= 1000000) {
					ticks = ticksNewValue;
					writeLog("CPU Clock Ticks value set to " + ticks);
				}
				else {
					writeLog("Invalid value for CPU Clock Ticks: \"" + ticksNewValue + "\". Must be between 1 - 1000000");
				}
			}
			catch (Exception e) {
				writeLog("Invalid value for CPU Clock Ticks: \"" + ticksInput + "\". " + e.getClass().getSimpleName());
			}
		}
	}

	private void showAboutDialog() {
		JOptionPane.showMessageDialog(this,
				"PK-01 Lvov (PK-01 Lviv) Computer Emulator (Java Version) 1.2\n"
						+ "(c) 2003 by Hard Wisdom (Vladimir Kalashnikov) \n" + "(c) 2018 by Izhak Serovsky \n\n"
						+ "https://github.com/izhaks/PK01LvovEmulator\n\n"
						+ "The emulator is distributed under the GNU General Public License version 2");
	}

	// -----------------------------------------------------------------------------
	// I n i t i a l i z a t i o n
	// -----------------------------------------------------------------------------
	@Override
	public void init() {
		log = new LogWindow(this, "Events log", false);

		try {
			setIconImage(createImage((ImageProducer) getClass().getResource("pk01lvov.gif").getContent()));
		} catch (IOException e) {
		}

		if (configFileName != null) {
			config_load(configFileName);
		}

		writeLog("Booting Computer...");
		try {
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			super.init();
			if (init_failed != null) {
				writeLog("Boot Error: " + init_failed);
				return;
			}
			configure();
			setTitle("PK-01 Lvov Emulator");
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		} catch (Exception ex) {
			init_failed = ex.toString();
			writeLog("UI Error: " + init_failed);
		}
	}

	// -----------------------------------------------------------------------------
	@Override
	public void destroy() {
		if (cfg("Enable_ContextMenu", "yes") && pm != null)
			remove(pm);
		if (cfg("Enable_ToolbarMenu", "yes"))
			setMenuBar(null);
		removeAll();
		super.destroy();
	}

	@Override
	void showStatus(String status) {
		if (st != null) st.setText(status);
	}

	@Override
	void writeLog(String msg) {
		log.appendToLog(msg);
	}

	// -----------------------------------------------------------------------------
}
