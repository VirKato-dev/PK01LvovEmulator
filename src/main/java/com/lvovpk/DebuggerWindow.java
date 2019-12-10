package com.lvovpk;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Simple Debugger
 */
class DebuggerWindow extends JDialog implements ActionListener, WindowListener, KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7645934054444666050L;
	private Debugger peer;
	private JTextArea tty;
	private JButton ok;
	private JTextField cmdLine;

	// -----------------------------------------------------------------------------
	DebuggerWindow(JFrame wnd, String title, boolean modal, Debugger peer) {
		super(wnd, title, modal);
		this.peer = peer;
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

		tty = new JTextArea("");
		tty.setEditable(false);
		tty.setFont(new Font("Monospaced", Font.BOLD, 14));

		JScrollPane sp = new JScrollPane(tty);
		sp.setBounds(0, 0, 450, 600);
		add(sp, BorderLayout.CENTER);

		tb.add(cmdLine = new JTextField(40));

		add(tbb, BorderLayout.SOUTH);
		tbb.add(tb, BorderLayout.WEST);
		tbb.add(ok = new JButton("Continue"), BorderLayout.EAST);

		ok.addActionListener(this);
		tty.addKeyListener(this);
		cmdLine.addKeyListener(this);
		addWindowListener(this);
		setSize(550, 680);
		setLocation(100, 100);
		validate();
	}

	// -----------------------------------------------------------------------------
	public void showWindow() {
		render();
		setVisible(true);
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
	// D e b u g g e r I m p l e m e n t a t i o n
	// -----------------------------------------------------------------------------
	static String[] hex0 = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

	static String hex1(int val) {
		return hex0[val & 15];
	}

	static String hex2(int val) {
		return hex1(val / 16) + hex1(val);
	}

	static String hex4(int val) {
		return hex2(val / 256) + hex2(val);
	}

	static int hex(String val) {
		int result = 0;
		for (int i = 0; i < val.length(); i++) {
			String d = val.substring(i, i + 1).toUpperCase();
			for (int j = 0; j < hex0.length; j++)
				if (hex0[j].equals(d)) {
					result = result * 16 + j;
					break;
				}
		}
		return result;
	}

	// -----------------------------------------------------------------------------
	boolean f_Reg = true, f_Mem = true, f_Port = true, f_Code = true;

	int ptr_Mem = 0, sz_Mem = 4, new_Mem = ptr_Mem, ptr_Port = 0, sz_Port = 2, new_Port = ptr_Port, ptr_Code = 0,
			sz_Code = 16, new_Code = ptr_Code;

	// -----------------------------------------------------------------------------
	static String[][] flags = new String[][] {
			{ "P", "M" }, { "NZ", "Z" },  { "0", "1" }, { "ac", "AC" },
			{ "0", "1" }, { "PO", "PE" }, { "0", "1" }, { "NC", "C" }
		};

	// -----------------------------------------------------------------------------
	static String[] opcodes = new String[] {
			"NOP", "LXI\tB,#", "STAX\tB", "INX\tB", "INR\tB", "DCR\tB", "MVI\tB,*",	"RLC",
			"!NOP", "DAD\tB",  "LDAX\tB", "DCX\tB", "INR\tC", "DCR\tC", "MVI\tC,*", "RRC",

			"!NOP", "LXI\tD,#", "STAX\tD", "INX\tD", "INR\tD", "DCR\tD", "MVI\tD,*", "RAL",
			"!NOP", "DAD\tD",   "LDAX\tD", "DCX\tD", "INR\tE", "DCR\tE", "MVI\tE,*", "RAR",

			"!NOP", "LXI\tH,#", "SHLD\t#", "INX\tH", "INR\tH", "DCR\tH", "MVI\tH,*", "DAA",
			"!NOP", "DAD\tH",  "LHLD\t#",  "DCX\tH", "INR\tL", "DCR\tL", "MVI\tL,*", "CMA",

			"!NOP", "LXI\tSP,#", "STA\t#", "INX\tSP", "INR\tM", "DCR\tM", "MVI\tM,*", "STC",
			"!NOP", "DAD\tSP",   "LDA\t#", "DCX\tSP", "INR\tA", "DCR\tA", "MVI\tA,*", "CMC",

			"MOV\tB,B", "MOV\tB,C", "MOV\tB,D", "MOV\tB,E", "MOV\tB,H", "MOV\tB,L", "MOV\tB,M", "MOV\tB,A",
			"MOV\tC,B",	"MOV\tC,C", "MOV\tC,D", "MOV\tC,E", "MOV\tC,H", "MOV\tC,L", "MOV\tC,M", "MOV\tC,A",
			"MOV\tD,B", "MOV\tD,C",	"MOV\tD,D", "MOV\tD,E", "MOV\tD,H", "MOV\tD,L", "MOV\tD,M", "MOV\tD,A",
			"MOV\tE,B", "MOV\tE,C", "MOV\tE,D",	"MOV\tE,E", "MOV\tE,H", "MOV\tE,L", "MOV\tE,M", "MOV\tE,A",
			"MOV\tH,B", "MOV\tH,C", "MOV\tH,D", "MOV\tH,E",	"MOV\tH,H", "MOV\tH,L", "MOV\tH,M", "MOV\tH,A",
			"MOV\tL,B", "MOV\tL,C", "MOV\tL,D", "MOV\tL,E", "MOV\tL,H",	"MOV\tL,L", "MOV\tL,M", "MOV\tL,A",
			"MOV\tM,B", "MOV\tM,C", "MOV\tM,D", "MOV\tM,E", "MOV\tM,H", "MOV\tM,L",	"HLT",      "MOV\tM,A",
			"MOV\tA,B", "MOV\tA,C", "MOV\tA,D", "MOV\tA,E", "MOV\tA,H", "MOV\tA,L", "MOV\tA,M",	"MOV\tA,A",

			"ADD\tB", "ADD\tC", "ADD\tD", "ADD\tE", "ADD\tH", "ADD\tL", "ADD\tM", "ADD\tA",
			"ADC\tB", "ADC\tC", "ADC\tD", "ADC\tE", "ADC\tH", "ADC\tL", "ADC\tM", "ADC\tA",
			"SUB\tB", "SUB\tC",	"SUB\tD", "SUB\tE",	"SUB\tH", "SUB\tL", "SUB\tM", "SUB\tA",
			"SBB\tB", "SBB\tC", "SBB\tD", "SBB\tE", "SBB\tH", "SBB\tL", "SBB\tM", "SBB\tA",
			"ANA\tB", "ANA\tC", "ANA\tD", "ANA\tE",	"ANA\tH", "ANA\tL", "ANA\tM", "ANA\tA",
			"XRA\tB", "XRA\tC", "XRA\tD", "XRA\tE", "XRA\tH", "XRA\tL", "XRA\tM", "XRA\tA",
			"ORA\tB", "ORA\tC", "ORA\tD", "ORA\tE", "ORA\tH", "ORA\tL",	"ORA\tM", "ORA\tA",
			"CMP\tB", "CMP\tC", "CMP\tD", "CMP\tE",	"CMP\tH", "CMP\tL", "CMP\tM", "CMP\tA",

			"RNZ", "POP\tB",   "JNZ\t#", "JMP\t#",  "CNZ\t#", "PUSH\tB",   "ADI\t*", "RST\t0",
			"RZ",  "RET",      "JZ\t#",  "!JMP\t#", "CZ\t#",  "CALL\t#",   "ACI\t*", "RST\t1",
			"RNC", "POP\tD",   "JNC\t#", "OUT\t*",  "CNC\t#", "PUSH\tD",   "SUI\t*", "RST\t2",
			"RC",  "!RET",     "JC\t#",  "IN\t*",   "CC\t#",  "!CALL\t#",  "SBI\t*", "RST\t3",
			"RPO", "POP\tH",   "JPO\t#", "XTHL",    "CPO\t#", "PUSH\tH",   "ANI\t*", "RST\t4",
			"RPE", "PCHL",     "JPE\t#", "XCHG",    "CPE\t#", "!CALL\t#",  "XRI\t*", "RST\t5",
			"RP",  "POP\tPSW", "JP\t#",  "DI",      "CP\t#",  "PUSH\tPSW", "ORI\t*", "RST\t6",
			"RM",  "SPHL",     "JM\t#",  "EI",      "CM\t#",  "!CALL\t#",  "CPI\t*", "RST\t7"
		};

	// -----------------------------------------------------------------------------
	void render() {
		int ln, ptr, pc = peer.PC(), sp = peer.SP();
		tty.setText("Clock: " + peer.CLK() + "   " + (peer.HALTED() ? "HALTED: " + peer.REASON() : ""));
		StringBuffer ttyCurrent = new StringBuffer(4096);

		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		if (f_Reg) {
			ttyCurrent.append("\n---[:Registers:]---");
			ttyCurrent.append("\nB=" + hex2(peer.B()));
			ttyCurrent.append("  C=" + hex2(peer.C()));
			ttyCurrent.append("  D=" + hex2(peer.D()));
			ttyCurrent.append("  E=" + hex2(peer.E()));
			ttyCurrent.append("  H=" + hex2(peer.H()));
			ttyCurrent.append("  L=" + hex2(peer.L()));
			ttyCurrent.append("\nA=" + hex2(peer.A()));
			ttyCurrent.append(" PC=" + hex4(peer.PC()));
			ttyCurrent.append(" SP=" + hex4(peer.SP()));
			ttyCurrent.append("  F =");
			for (int f = peer.F(), i = 128, j = 0; i > 0; i >>= 1, j++)
				ttyCurrent.append(" " + flags[j][(f & i) == 0 ? 0 : 1]);
		}
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		if (f_Mem) {
			ttyCurrent.append("\n---[:Memory:]---");
			for (ln = 0, ptr = ptr_Mem; ln < sz_Mem; ln++) {
				ttyCurrent.append("\n" + hex4(ptr) + ":");
				for (int b = 0; b < 16; b++, ptr++)
					ttyCurrent.append(((b == 4 || b == 8 || b == 12) ? " - " : " ") + hex2(peer.MEM(ptr)));
			}
			new_Mem = ptr;
		}
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		if (f_Port) {
			ttyCurrent.append("\n---[:Ports:]---");
			for (ln = 0, ptr = ptr_Port; ln < sz_Port; ln++) {
				ttyCurrent.append("\n" + hex4(ptr) + ":");
				for (int b = 0; b < 16; b++, ptr++)
					ttyCurrent.append(((b == 4 || b == 8 || b == 12) ? " - " : " ") + hex2(peer.IO(ptr)));
			}
			new_Port = ptr;
		}
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		if (f_Code) {
			ttyCurrent.append("\n---[:Code: & :Stack:]---");
			int stack = sp + sz_Code * 2 - 2;
			for (ln = 0, ptr = ptr_Code; ln < sz_Code; ln++, ptr++, stack -= 2) {
				ttyCurrent.append("\n" + hex4(ptr) + ": " + (ptr == pc ? ">>>" : "   ") + " ");
				String cmd = opcodes[peer.MEM(ptr)];
				int pos = ttyCurrent.length();
				for (int i = 0; i < cmd.length(); i++) {
					char ch = cmd.charAt(i);
					switch (ch) {
					default:
						ttyCurrent.append(ch);
						break;
					case '\t':
						while (ttyCurrent.length() - pos < 8)
							ttyCurrent.append(' ');
						break;
					case '#':
						ttyCurrent.append(hex4(peer.MEM2(ptr + 1)));
						ptr += 2;
						break;
					case '*':
						ttyCurrent.append(hex2(peer.MEM(ptr + 1)));
						ptr += 1;
						break;
					}
				}
				while (ttyCurrent.length() - pos < 16)
					ttyCurrent.append(' ');
				ttyCurrent.append("" + hex4(stack) + ": " + hex4(peer.MEM2(stack)));
			}
			new_Code = ptr;
		}
		tty.append(ttyCurrent.toString());
	}

	// -----------------------------------------------------------------------------
	void help() {
		tty.setText("Key Bindings & Commands:" + "\n" + "\nF1 - this help"
				+ "\nESC - refresh window/exit from help" + "\n" + "\nUP - scroll code window up one byte"
				+ "\nDOWN - scroll code window down one byte" + "\nEND - scroll code window down one page" + "\n"
				+ "\nF8 - do one emulation step" + "\n" + "\ndADDR - dump from ADDR" + "\ntADDR - show ports from ADDR"
				+ "\nuADDR - unassemble from ADDR" + "\ngADDR - go until ADDR" + "\nrREG=VAL - store VAL into REGister"
				+ "\n. - unassemble from PC");
	}

	// -----------------------------------------------------------------------------
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(ok))
			setVisible(false);
	}

	// -----------------------------------------------------------------------------
	@Override
	public void keyPressed(KeyEvent e) {
		boolean changed = true;
		switch (e.getKeyCode()) {
		default:
			changed = false;
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case KeyEvent.VK_F1:
			changed = false;
			help();
			break;
		case KeyEvent.VK_ESCAPE:
			break;
		case KeyEvent.VK_ENTER:
			if (!process(cmdLine.getText()))
				cmdLine.setText("");
			else {
				int pc = peer.PC();
				if (pc < ptr_Code || pc >= new_Code)
					ptr_Code = pc;
			}
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case KeyEvent.VK_UP:
			ptr_Code = (ptr_Code - 1) & 0xFFFF;
			break;
		case KeyEvent.VK_DOWN:
			ptr_Code = (ptr_Code + 1) & 0xFFFF;
			break;
		case KeyEvent.VK_END:
			ptr_Code = new_Code;
			break;
		case KeyEvent.VK_F8:
			peer.STEP1();
			int pc = peer.PC();
			if (pc < ptr_Code || pc >= new_Code)
				ptr_Code = pc;
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		}
		if (changed)
			render();
	}

	// -----------------------------------------------------------------------------
	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	// -----------------------------------------------------------------------------
	boolean process(String cmd) {
		char cm = cmd.length() > 0 ? cmd.charAt(0) : 0;
		String op = cmd.length() > 1 ? cmd.substring(1).toUpperCase() : "";
		switch (cm) {
		case '.':
			ptr_Code = peer.PC();
			break;
		case 'd':
			ptr_Mem = hex(op);
			break;
		case 'u':
			ptr_Code = hex(op);
			break;
		case 't':
			ptr_Port = hex(op);
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case 'g':
			for (int stop = hex(op), num = 0; stop != peer.PC();) {
				peer.STEP1();
				if (++num > 100000)
					return true;
			}
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		case 'r':
			if (op.startsWith("PSW"))
				peer.PSW(hex(op.substring(3)));
			else if (op.startsWith("PC"))
				peer.PC(hex(op.substring(2)));
			else if (op.startsWith("SP"))
				peer.SP(hex(op.substring(2)));
			else if (op.startsWith("BC"))
				peer.BC(hex(op.substring(2)));
			else if (op.startsWith("DE"))
				peer.DE(hex(op.substring(2)));
			else if (op.startsWith("HL"))
				peer.HL(hex(op.substring(2)));
			else if (op.startsWith("B"))
				peer.B(hex(op.substring(1)));
			else if (op.startsWith("C"))
				peer.C(hex(op.substring(1)));
			else if (op.startsWith("D"))
				peer.D(hex(op.substring(1)));
			else if (op.startsWith("E"))
				peer.E(hex(op.substring(1)));
			else if (op.startsWith("H"))
				peer.H(hex(op.substring(1)));
			else if (op.startsWith("L"))
				peer.L(hex(op.substring(1)));
			else if (op.startsWith("A"))
				peer.A(hex(op.substring(1)));
			else if (op.startsWith("F"))
				peer.F(hex(op.substring(1)));
			break;
		default:
			break;
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
		}
		return false;
	}

	// -----------------------------------------------------------------------------
}
