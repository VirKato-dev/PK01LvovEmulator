package com.lvovpk;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

/**
 * Simple Text Editor
 */
class EditorWindow extends JDialog implements ActionListener, WindowListener {

	private static final long serialVersionUID = 2839796731433686240L;
	public JTextArea txt;
	private final JButton ok, cancel, checkIn, checkOut;
	private int cmdIn, cmdOut;
	private Gui peer;

	// -----------------------------------------------------------------------------
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(cancel))
			setVisible(false);
		else {
			if (peer != null) {
				if (e.getSource().equals(checkIn))
					peer.perform(cmdIn);

				else if (e.getSource().equals(checkOut))
					peer.perform(cmdOut);

				else if (e.getSource().equals(ok)) {
					peer.perform(cmdOut);
					setVisible(false);
				}
			}
		}
	}

	// -----------------------------------------------------------------------------
	EditorWindow(JFrame wnd, String title, boolean modal) {
		super(wnd, title, modal);
		{
			BorderLayout lyo = new BorderLayout();
			lyo.setHgap(0);
			lyo.setVgap(0);
			setLayout(lyo);
		}
		JPanel tb = new JPanel();
		{
			FlowLayout lyo = new FlowLayout();
			lyo.setHgap(0);
			lyo.setVgap(0);
			tb.setLayout(lyo);
		}
		JPanel tbb = new JPanel();
		{
			BorderLayout lyo = new BorderLayout();
			lyo.setHgap(0);
			lyo.setVgap(0);
			tbb.setLayout(lyo);
		}

		txt = new JTextArea("");
		txt.setFont(new Font("Monospaced", Font.BOLD, 14));

		JScrollPane sp = new JScrollPane(txt);
		sp.setBounds(0, 0, 450, 500);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(sp, BorderLayout.CENTER);
		add(tbb, BorderLayout.SOUTH);

		tb.add(ok = new JButton("Ok"));
		tb.add(cancel = new JButton("Cancel"));
		tbb.add(checkIn = new JButton("Checkin"), BorderLayout.WEST);
		tbb.add(checkOut = new JButton("Checkout"), BorderLayout.CENTER);
		tbb.add(tb, BorderLayout.EAST);

		checkOut.addActionListener(this);
		checkIn.addActionListener(this);
		ok.addActionListener(this);
		cancel.addActionListener(this);

		addWindowListener(this);
		setSize(500, 550);
		setLocation(100, 100);
		validate();
	}

	// -----------------------------------------------------------------------------
	void setPeer(int cmdIn, int cmdOut, Gui peer) {
		this.cmdIn = cmdIn;
		this.cmdOut = cmdOut;
		this.peer = peer;
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
	}

	@Override
	public void windowClosing(WindowEvent e) {
		setVisible(false);
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	// -----------------------------------------------------------------------------
}
