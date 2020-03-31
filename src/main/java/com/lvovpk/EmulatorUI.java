package com.lvovpk;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.ImageProducer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
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
	static final String EMULATOR_VERSION = "1.4";
	static final int CM_CONFIG = 19;
	static final int CM_INVOKE_EDITOR = 20;
	static final int CM_SYNC_EDITOR_IN = 21;
	static final int CM_SYNC_EDITOR_OUT = 22;
	static final int CM_INVOKE_DEBUGGER = 23;
	static final int CM_INVOKE_ABOUT = 24;
	static final int CM_INVOKE_LOG = 25;
	static final int CM_CHANGE_TICKS = 26;
	static final int CM_TOGGLE_FULLSCREEN = 27;
	static final int CM_VOL_CTL = 1000;

	private JPanel topPanel = null;
	private JPanel bottomPanel = null;
	private JMenuBar menuBar = null;
	private JPopupMenu popupMenu = null;
	private EditorWindow tx;
	private DebuggerWindow dbg;
	private LogWindow log;
	private JLabel st = null;

	private String configFileName = null;
	private String currentDir = null;

	public final void doConfigDump(String name) {
		// save default configuration
		configDump(name);
	}

	// -----------------------------------------------------------------------------
	void configDump(String tf) {
		try {
			String[] k;
			PrintWriter o = new PrintWriter(new FileOutputStream(tf));
			o.println("#main");
			o.println("appv.size.x " + getSize().width);
			o.println("appv.size.y " + getSize().height);
			o.println("");

			o.println("#OSD Settings");
			k = Utils.sort(config.keySet(), true);
			for (int i = 0; i < k.length; i++)
				o.println(Utils.padRight(k[i], 40) + config.get(k[i]));
			o.println("");

			o.println("#LVOV Settings");
			k = Utils.sort(Defaults.cfg.keySet(), true);
			for (int i = 0; i < k.length; i++)
				o.println(Utils.padRight(k[i], 40) + Defaults.cfg.get(k[i]));
			o.println("");

			o.close();
		} catch (IOException ignored) {
		}
	}

	void configLoad(String tf) {
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
		while (ireq != CM_STOP)
			super.run();
	}

	// -----------------------------------------------------------------------------
	// C o n f i g u r a t i o n
	// -----------------------------------------------------------------------------
	@Override
	String cfg(String nm) {
		String s = super.cfg(nm);
		if (s.equals(""))
			s = config.get(nm);
		if (s == null)
			return "";
		else
			return s;
	}

	// -----------------------------------------------------------------------------
	private static Map<String, String> config;
	static {
		config = new ConcurrentHashMap<String, String>();

		String[][] menus = new String[][] {
			{ "ToolbarTop", "No" },
			{ "ToolbarBottom", "No" },
			{ "ToolbarMenu", "Yes" },
			{ "ContextMenu", "No" }
		};
		String[][] features = new String[][] {
			{ "Mode", "No", "Yes", "Yes", "Yes" },
			{ "Fullscreen", "No", "Yes", "Yes", "Yes" },
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
			{ "About", "Yes", "No", "Yes", "Yes" },
			{ "Log", "Yes", "No", "Yes", "Yes" },
			{ "Cfg", "No", "Yes", "Yes", "Yes" },
			{ "Quit", "No", "No", "Yes", "Yes" }
		};

		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		config.put("Enable_Statusbar", "Yes");
		config.put("Enable_FlatContextMenu", "Yes");
		for (int i = 0; i < menus.length; i++) {
			config.put("Enable_" + menus[i][0], menus[i][1]);
			for (int j = 0; j < features.length; j++)
				config.put("Enable_" + menus[i][0] + "_" + features[j][0] + "_Feature", features[j][i + 1]);
		}
	}

	// -----------------------------------------------------------------------------
	private void mkMenuItem(JMenu mn, String name, String feature, int command, String description) {
		if (cfg("Enable_" + name + "_" + feature + "_Feature", "yes")) {
			int[] shortcut = Keyboard.getShortcutForCommand(command);
			if (shortcut != null) {
				mn.add(GuiUtils.createMenuItem(command, description, shortcut[0], shortcut[1], this));
			}
			else {
				mn.add(GuiUtils.createMenuItem(command, description, this));
			}
		}
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	private void mkToolbarButton(JToolBar tb, String name, String feature, int command, String description) {
		if (cfg("Enable_" + name + "_" + feature + "_Feature", "yes"))
			tb.add(GuiUtils.createButton(command, description, this));
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
	private void mkToolbar(String name, JToolBar tb) {
		mkToolbarButton(tb, name, "Mode", CM_MODE, "Mode");
		mkToolbarButton(tb, name, "Fullscreen", CM_TOGGLE_FULLSCREEN, "Fullscreen");
		mkToolbarButton(tb, name, "Ticks", CM_CHANGE_TICKS, "Ticks");
		mkToolbarButton(tb, name, "Fast", CM_FAST, "Fast");
		mkToolbarButton(tb, name, "Slow", CM_SLOW, "Slow");
		mkToolbarButton(tb, name, "PRN_O", CM_OPEN_PRN, "O-Printer");
		mkToolbarButton(tb, name, "PRN_C", CM_CLOSE_PRN, "C-Printer");

		mkToolbarButton(tb, name, "Speaker", CM_VOL_CTL, "Mute");
		mkToolbarButton(tb, name, "Speaker", CM_VOL_CTL + 100, "Loud");

		mkToolbarButton(tb, name, "Reset", CM_RESET, "Reset");
		mkToolbarButton(tb, name, "Pause", CM_PAUSE, "Pause");
		mkToolbarButton(tb, name, "Resume", CM_RESUME, "Resume");
		mkToolbarButton(tb, name, "Debug", CM_INVOKE_DEBUGGER, "Debug");

		mkToolbarButton(tb, name, "Load", CM_LOAD, "Load");
		mkToolbarButton(tb, name, "Import", CM_IMPORT, "Import");
		mkToolbarButton(tb, name, "Edit", CM_INVOKE_EDITOR, "Edit");
		mkToolbarButton(tb, name, "Restore", CM_RESTORE, "Restore");

		mkToolbarButton(tb, name, "Export", CM_EXPORT, "Export");
		mkToolbarButton(tb, name, "DumpF", CM_DUMP_F, "Full");
		mkToolbarButton(tb, name, "DumpP", CM_DUMP_P, "Partial");
		mkToolbarButton(tb, name, "Snap", CM_SNAP, "Snap");

		mkToolbarButton(tb, name, "Log", CM_INVOKE_LOG, "Log");
		mkToolbarButton(tb, name, "About", CM_INVOKE_ABOUT, "About");
		mkToolbarButton(tb, name, "Cfg", CM_CONFIG, "Conf");
		mkToolbarButton(tb, name, "Quit", CM_STOP, "Quit");
	}

	// -----------------------------------------------------------------------------
	private void mkMenu(String name, MenuElement mn, boolean flatten) {
		JMenu submn;

		submn = new JMenu("Config");
		mkMenuItem(submn, name, "Mode", CM_MODE, "Change rendering mode");
		mkMenuItem(submn, name, "Fullscreen", CM_TOGGLE_FULLSCREEN, "Toggle Fullscreen");
		mkMenuItem(submn, name, "Ticks", CM_CHANGE_TICKS, "Change CPU Clock Ticks");
		mkMenuItem(submn, name, "Fast", CM_FAST, "Emulate at full speed");
		mkMenuItem(submn, name, "Slow", CM_SLOW, "Emulate at real speed");
		mkMenuItem(submn, name, "PRN_O", CM_OPEN_PRN, "Open printer");
		mkMenuItem(submn, name, "PRN_C", CM_CLOSE_PRN, "Close printer");

		JMenu submn2 = new JMenu("Volume");
		for (int i = 0; i <= 100; i += 10)
			mkMenuItem(submn2, name, "Speaker", CM_VOL_CTL + i, "Set volume " + i + "%");
		mkMenuMenu(submn, submn2, false);

		mkMenuMenu(mn, submn, flatten);

		submn = new JMenu("Control");
		mkMenuItem(submn, name, "Reset", CM_RESET, "Perform cold start sequence");
		mkMenuItem(submn, name, "Pause", CM_PAUSE, "Pause execution");
		mkMenuItem(submn, name, "Resume", CM_RESUME, "Resume execution");
		mkMenuItem(submn, name, "Debug", CM_INVOKE_DEBUGGER, "Invoke Code Debugger");
		mkMenuMenu(mn, submn, flatten);

		submn = new JMenu("Load");
		mkMenuItem(submn, name, "Load", CM_LOAD, "Load program");
		mkMenuItem(submn, name, "Import", CM_IMPORT, "Import Basic program");
		mkMenuItem(submn, name, "Edit", CM_INVOKE_EDITOR, "Open editor for Basic");
		mkMenuItem(submn, name, "Restore", CM_RESTORE, "Restore state from the dump");
		mkMenuMenu(mn, submn, flatten);

		submn = new JMenu("Store");
		mkMenuItem(submn, name, "Export", CM_EXPORT, "Export Basic program");
		mkMenuItem(submn, name, "DumpF", CM_DUMP_F, "Perform full dump of the emulator state");
		mkMenuItem(submn, name, "DumpP", CM_DUMP_P, "Perform partial dump of the emulator state");
		mkMenuItem(submn, name, "Snap", CM_SNAP, "Take a screenshot");
		mkMenuMenu(mn, submn, flatten);

		submn = new JMenu("General");
		mkMenuItem(submn, name, "About", CM_INVOKE_ABOUT, "About");
		mkMenuItem(submn, name, "Log", CM_INVOKE_LOG, "Events log");
		mkMenuItem(submn, name, "Cfg", CM_CONFIG, "Save default configuration");
		mkMenuItem(submn, name, "Quit", CM_STOP, "Quit");
		mkMenuMenu(mn, submn, flatten);
	}

	// -----------------------------------------------------------------------------
	private void configure() {
		addWindowListener(this);

		tx = new EditorWindow(this, "Simple Basic Editor", false);
		tx.setPeer(CM_SYNC_EDITOR_IN, CM_SYNC_EDITOR_OUT, this);
		dbg = new DebuggerWindow(this, "Simple i8080 Debugger (press F1 for help)", true, new LvovDebugger(lv));
		Keyboard.enableShortcuts = cfg("KeyboardShortcuts", "yes");
		Keyboard.shortcutsModifiers = (cfg("KeyboardShortcutsModifiers").isEmpty() ?
				KeyEvent.CTRL_DOWN_MASK : Integer.parseInt(cfg("KeyboardShortcutsModifiers")));
		if (Keyboard.enableShortcuts && (fullScreen || !cfg("Enable_ToolbarMenu", "yes"))) {
			lv.addKeyListener(this);
		}

		JToolBar tb;
		FlowLayout tbl = new FlowLayout(FlowLayout.LEFT, 0, 0);
		if (cfg("Enable_ToolbarTop", "yes")) {
			topPanel = new JPanel();
			topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
			tb = new JToolBar();
			tb.setFloatable(false);
			tb.setLayout(tbl);
			tb.setAlignmentX(Component.LEFT_ALIGNMENT);
			mkToolbar("ToolbarTop", tb);
			topPanel.add(tb);
			if (!fullScreen)
				add(topPanel, BorderLayout.NORTH);
		}

		if (cfg("Enable_Statusbar", "yes") || cfg("Enable_ToolbarBottom", "yes")) {
			bottomPanel = new JPanel();
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
				JPanel stb = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
				stb.add(st = new JLabel("Booting..."));
				stb.setAlignmentX(Component.LEFT_ALIGNMENT);
				bottomPanel.add(stb);
			}
			if (!fullScreen)
				add(bottomPanel, BorderLayout.SOUTH);
		}

		if (cfg("Enable_ToolbarMenu", "yes")) {
			menuBar = new JMenuBar();
			mkMenu("ToolbarMenu", menuBar, false);
			setJMenuBar(menuBar);
			if (fullScreen)
				menuBar.setVisible(false);
		}

		if (cfg("Enable_ContextMenu", "yes")) {
			popupMenu = new JPopupMenu("Context");
			popupMenu.add(new JMenuItem("Focus on the emulator"));
			boolean flat = cfg("Enable_FlatContextMenu", "yes");
			if (!flat)
				popupMenu.addSeparator();
			mkMenu("ContextMenu", popupMenu, flat);
			//add(popupMenu);
			lv.addMouseListener(this);
		}

		if (!fullScreen) {
			setSize(getPreferredSize());
			Toolkit tk = getToolkit();
			setLocation((tk.getScreenSize().width - getSize().width) / 2,
					(tk.getScreenSize().height - getSize().height) / 2);
			setSize(fly.preferredLayoutSize(this));
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
			e.consume();
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
		if (popupMenu != null)
			popupMenu.show(lv, e.getX(), e.getY());
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
		case CM_FAST:
			doFast();
			break;
		case CM_SLOW:
			doSlow();
			break;
		case CM_MODE:
			doChangeMode();
			break;
		case CM_TOGGLE_FULLSCREEN:
			doPause();
			toggleFullScreen();
			doResume();
			break;
		case CM_CHANGE_TICKS:
			doPause();
			showChangeTicksDialog();
			doResume();
			break;
		case CM_RESET:
			doReset();
			break;
		case CM_PAUSE:
			doPause();
			break;
		case CM_RESUME:
			doResume();
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case CM_LOAD:
			fn = showFileDialog(false, "Choose .LVT program", "lvt");
			if (fn != null)
				doLoad(fn);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case CM_IMPORT:
			fn = showFileDialog(false, "Choose .BAS source", "bas");
			if (fn != null)
				doImport(fn);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case CM_EXPORT:
			fn = showFileDialog(true, "Choose destination .BAS source", "bas");
			if (fn != null)
				doExport(fn);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case CM_INVOKE_EDITOR:
			perform(CM_SYNC_EDITOR_IN);
			tx.setVisible(true);
			break;
		case CM_SYNC_EDITOR_OUT:
			doPaste(tx.txt.getText());
			break;
		case CM_SYNC_EDITOR_IN:
			tx.txt.setText(doCopy());
			break;
		case CM_INVOKE_DEBUGGER:
			doPause();
			dbg.showWindow();
			doResume();
			break;
		case CM_INVOKE_ABOUT:
			doPause();
			showAboutDialog();
			doResume();
			break;
		case CM_INVOKE_LOG:
			log.setVisible(true);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case CM_DUMP_F:
			fn = showFileDialog(true, "Choose destination .LVD or .SAV file", "lvd", "sav");
			if (fn != null)
				doFullDump(fn);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case CM_DUMP_P:
			fn = showFileDialog(true, "Choose destination .LVD file", "lvd");
			if (fn != null)
				doPartialDump(fn);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case CM_SNAP:
			doPause();
			fn = showFileDialog(true, "Choose destination image file",
					"PNG image#png", "GIF image#gif", "JPEG image#jpg", "Bitmap image#bmp");
			doResume();
			if (fn != null)
				doSnap(fn);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case CM_RESTORE:
			fn = showFileDialog(false, "Choose source .LVD or .SAV file", "Dump files (*.lvd; *.sav)#lvd;sav");
			if (fn != null)
				doRestore(fn);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case CM_CONFIG:
			fn = showFileDialog(true, "Choose configuration file", "cfg");
			if (fn != null)
				doConfigDump(fn);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case CM_OPEN_PRN:
			fn = showFileDialog(true, "Choose device to print to", "print");
			if (fn != null)
				doOpenPrn(fn);
			break;
		case CM_CLOSE_PRN:
			doClosePrn();
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case CM_STOP:
			ireq = CM_STOP;
			dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		default:
			if (cmd >= CM_VOL_CTL && cmd <= CM_VOL_CTL + 100)
				doVolume(cmd - CM_VOL_CTL);
			break;
		}
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	private void toggleFullScreen() {
		fullScreen = !fullScreen;
		if (menuBar != null)
			menuBar.setVisible(!fullScreen);
		setVisible(false);
		removeNotify();
		setUndecorated(fullScreen);
		if (fullScreen) {
			if (topPanel != null)
				remove(topPanel);
			if (bottomPanel != null)
				remove(bottomPanel);
			remove(lv);
			setExtendedState(JFrame.MAXIMIZED_BOTH);
			fly = new GridBagLayout();
			setLayout(fly);
			add(lv, new GridBagConstraints());

			Toolkit tk = Toolkit.getDefaultToolkit();
			setSize((int) tk.getScreenSize().getWidth(), (int) tk.getScreenSize().getHeight());
			setLocation(0, 0);
		}
		else {
			setExtendedState(JFrame.NORMAL);
			remove(lv);
			fly = new BorderLayout(0, 0);
			setLayout(fly);
			add(lv, BorderLayout.CENTER);
			if (topPanel != null)
				add(topPanel, BorderLayout.NORTH);
			if (bottomPanel != null)
				add(bottomPanel, BorderLayout.SOUTH);

			Toolkit tk = getToolkit();
			setSize(fly.preferredLayoutSize(this));
			pack();
			setLocation((tk.getScreenSize().width - getSize().width) / 2,
					(tk.getScreenSize().height - getSize().height) / 2);
		}
		addNotify();
		setVisible(true);

		if (Keyboard.enableShortcuts && menuBar != null) {
			if (fullScreen)
				lv.addKeyListener(this);
			else
				lv.removeKeyListener(this);
		}
		lv.requestFocusInWindow();
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
				"PK-01 Lvov (PK-01 Lviv) Computer Emulator (Java Version) " + EMULATOR_VERSION + "\n"
						+ "(c) 2003 by Hard Wisdom (Vladimir Kalashnikov)\n"
						+ "(c) 2020 by Izhak Serovsky\n"
						+ "https://github.com/izhaks/PK01LvovEmulator\n\n"
						+ "Run with the following command-line options:\n"
						+ "    -d <conf_file> to dump the default configurations\n"
						+ "    -r <conf_file> to replace the default configurations\n"
						+ "    -p <basic_file> to produce basic .lvt from textual stdin\n"
						+ "            (-p866 -p1251 -pkoi8 allows you to specify the codepage)\n"
						+ "    -v <basic_file> to list/view basic .lvt onto textual stdout\n"
						+ "            (-v866 -v1251 -vkoi8 allows you to specify the codepage)\n"
						+ "    -h to print the available command-line options\n\n"
						+ "The emulator is distributed under the GNU General Public License version 2");
	}

	private String showFileDialog(boolean saveFile, String title, String... masks) {
		File selectedFile = (saveFile ?
				Utils.saveFileDialog(this, currentDir, title, masks) :
					Utils.openFileDialog(this, currentDir, title, masks));
		if (selectedFile != null) {
			currentDir = selectedFile.getParent();
			return selectedFile.getAbsolutePath();
		}
		return null;
	}

	// -----------------------------------------------------------------------------
	// I n i t i a l i z a t i o n
	// -----------------------------------------------------------------------------
	@Override
	public void init() {
		log = new LogWindow(this, "Events log", false);

		try {
			setIconImage(createImage((ImageProducer) getClass().getResource("/pk01lvov.gif").getContent()));
		} catch (IOException ignored) {
		}

		if (configFileName != null) {
			configLoad(configFileName);
		}

		writeLog("Booting Computer...");
		try {
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			super.init();
			if (initFailed != null) {
				writeLog("Boot Error: " + initFailed);
				return;
			}
			configure();
			setTitle("PK-01 Lvov Emulator");
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		} catch (Exception ex) {
			initFailed = ex.toString();
			writeLog("UI Error: " + initFailed);
		}
	}

	// -----------------------------------------------------------------------------
	@Override
	public void destroy() {
		if (topPanel != null)
			remove(topPanel);
		if (bottomPanel != null)
			remove(bottomPanel);
		if (popupMenu != null)
			remove(popupMenu);
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
