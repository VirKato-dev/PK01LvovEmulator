package com.lvovpk;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JMenuItem;

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
	public static JMenuItem createMenuItem(int cmd, String face, char sc, Gui recv) {
		JMenuItem Item = new JMenuItem(face/* , new MenuShortcut(sc) */);
		Item.addActionListener(new GuiUtils(recv, cmd));
		return Item;
	}

	// -----------------------------------------------------------------------------
	public static JMenuItem createMenuItem(int cmd, String face, Gui recv) {
		JMenuItem item = new JMenuItem(face);
		item.addActionListener(new GuiUtils(recv, cmd));
		return item;
	}

	// -----------------------------------------------------------------------------
}
