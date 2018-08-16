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
 * Events Log Window
 */
public class LogWindow extends JDialog implements ActionListener, WindowListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5844867748591714511L;
	private JTextArea logText;
	private JButton clearButton;
	private JButton closeButton;

	public LogWindow(JFrame owner, String title, boolean modal) {
		super(owner, title, modal);
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

		logText = new JTextArea("");
		logText.setEditable(false);
		logText.setFont(new Font("Monospaced", Font.BOLD, 14));

		JScrollPane sp = new JScrollPane(logText);
		sp.setBounds(0, 0, 450, 400);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		add(sp, BorderLayout.CENTER);
		add(tbb, BorderLayout.SOUTH);

		tb.add(clearButton = new JButton("Clear log"));
		tb.add(closeButton = new JButton("Close"));
		tbb.add(tb, BorderLayout.CENTER);

		clearButton.addActionListener(this);
		closeButton.addActionListener(this);

		addWindowListener(this);
		setSize(480, 400);
		setLocation(100, 100);
		validate();
	}

	public void appendToLog(String msg) {
		logText.append(msg + "\n");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(clearButton))
			logText.setText("");
		else if (e.getSource().equals(closeButton))
			setVisible(false);
	}

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

}
