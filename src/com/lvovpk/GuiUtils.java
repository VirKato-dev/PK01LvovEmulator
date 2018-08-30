package com.lvovpk;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * Tool GUI Package
 */
public class GuiUtils implements ActionListener {
	private int command;
	private Gui peer;

	private GuiUtils(Gui recv, int cmd) {
		peer = recv;
		command = cmd;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (peer != null)
			peer.perform(command);
	}

	// -----------------------------------------------------------------------------
	public static JButton createButton(int cmd, String face, Gui recv) {
		JButton item = new JButton(face);
		item.addActionListener(new GuiUtils(recv, cmd));
		return item;
	}

	// -----------------------------------------------------------------------------
	public static JMenuItem createMenuItem(int cmd, String face, int keyCode, int modifiers, Gui recv) {
		JMenuItem item = new JMenuItem(face);
		item.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers));
		item.addActionListener(new GuiUtils(recv, cmd));
		return item;
	}

	// -----------------------------------------------------------------------------
	public static JMenuItem createMenuItem(int cmd, String face, Gui recv) {
		JMenuItem item = new JMenuItem(face);
		item.addActionListener(new GuiUtils(recv, cmd));
		return item;
	}

	// -----------------------------------------------------------------------------
}
