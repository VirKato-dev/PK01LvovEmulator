package com.lvovpk;

import java.awt.event.KeyEvent;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * LVOV Software Abstraction Toolkit
 */
class PK01 extends PK00 {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8094497537939504694L;

	// Bios def
	// --------------------------------------------------------------------
	PK01() {
		super(mode_first);
	}

	PK01(int mode) {
		super(mode);
	}

	// -----------------------------------------------------------------------------
	void init_printer(int size) {
		pk.set_print_space(size);
	}

	void init_speaker(int size) {
		pk.set_speak_space(size);
	}

	// -----------------------------------------------------------------------------
	byte[] printed() {
		if (pk.printed <= 0)
			return null;
		byte[] data = new byte[pk.printed];

		System.arraycopy(pk.printer, 0, data, 0, pk.printed);

		pk.printed = 0;
		return data;
	}

	// -----------------------------------------------------------------------------
	long[] speaked() {
		if (pk.speaked < 0)
			return null;
		long[] data = new long[pk.speaked + 1];

		if (pk.speaked > 0)
			System.arraycopy(pk.speaker, 0, data, 0, pk.speaked);

		data[pk.speaked] = pk.clock;
		pk.speaked = 0;
		return data;
	}

	// -----------------------------------------------------------------------------
	// G U E S T M a n i p u l a t i o n s
	// -----------------------------------------------------------------------------
	void cold_start() {
		pk.reset();
		pk.r_PC = 0xC000;
		pk.cpu_halt_state = false;
	}

	// -----------------------------------------------------------------------------
	private void set_var(int var, int val) {
		pk.memory[var] = (short) (val & 0xFF);
		pk.memory[var + 1] = (short) (val / 256 & 0xFF);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// - -
	private int get_var(int var) {
		return pk.memory[var] + pk.memory[var + 1] * 256;
	}

	// -----------------------------------------------------------------------------
	void warm_start() {
		pk.ports[0xC2] = 0xFF;
		set_var(pk.r_SP = Bios.BasicStack, Bios.BasicHotEntry);
		pk.r_PC = Bios.BasicHotEntry;
		pk.cpu_halt_state = false;
	}

	// -----------------------------------------------------------------------------
	private void load_prog_basic(InputStream Prog) throws IOException {
		warm_start();
		int pos = get_var(Bios.BasicProgBegin);
		try {
			for (;; pos++)
				pk.do_write(pos, Utils.restoreByte(Prog));
		} catch (EOFException ex) {
		}
		set_var(Bios.BasicProgEnd, pos);
	}

	// -----------------------------------------------------------------------------
	private void save_prog_basic(OutputStream Prog) throws IOException {
		int pos = get_var(Bios.BasicProgBegin), cnt;
		for (cnt = 0; pos <= 0xFFFF && cnt < 3; pos++) {
			int bt = pk.do_read(pos);
			if (bt == 0)
				cnt++;
			else
				cnt = 0;
			Utils.dumpByte(Prog, bt);
		}
	}

	// -----------------------------------------------------------------------------
	private void load_prog_binary(InputStream Prog, boolean manual) throws IOException {
		int beg = Utils.restoreWord(Prog);
		int end = Utils.restoreWord(Prog);
		int run = Utils.restoreWord(Prog);
		int ofs = get_var(Bios.LoadBinaryOfs);

		if (manual) {
			warm_start();
			pk.r_PC = run;
		} else {
			beg += ofs;
			end += ofs;
			set_var(Bios.LoadBinaryEntry, run);
		}
		for (int pos = beg; pos <= end; pos++)
			pk.do_write(pos, Utils.restoreByte(Prog));
	}

	// -----------------------------------------------------------------------------
	private void save_prog_binary(OutputStream Prog, int from, int upto, int epo) throws IOException {
		Utils.dumpWord(Prog, from);
		Utils.dumpWord(Prog, upto);
		Utils.dumpWord(Prog, epo);

		for (int pos = from; pos <= upto; pos++)
			Utils.dumpByte(Prog, pk.do_read(pos));
	}

	// -----------------------------------------------------------------------------
	void load_prog(InputStream Prog, boolean manual) throws IOException {
		int type = PKIO.recognize(Prog);
		switch (type) {
		case 0xD3:
			load_prog_basic(Prog);
			break;
		case 0xD0:
			load_prog_binary(Prog, manual);
			break;
		default:
			throw new IOException("Unknown .LVT type " + Utils.HEX(type));
		}
	}

	// -----------------------------------------------------------------------------
	void save_prog(OutputStream Prog, String Name) throws IOException {
		PKIO.prepare(Prog, Name, 0xD3, PKIO.cp_default);
		save_prog_basic(Prog);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	void save_prog(OutputStream Prog, String Name, int from, int upto) throws IOException {
		save_prog(Prog, Name, from, upto, from);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	void save_prog(OutputStream Prog, String Name, int from, int upto, int epo) throws IOException {
		PKIO.prepare(Prog, Name, 0xD0, PKIO.cp_default);
		save_prog_binary(Prog, from, upto, epo);
	}

	// -----------------------------------------------------------------------------
	void load_bios(InputStream Bios) throws IOException {
		int type = PKIO.recognize(Bios);
		if (type != 0xD0)
			throw new IOException("Not a binary .LVT !");
		Utils.restoreWord(Bios); // beg
		Utils.restoreWord(Bios); // end
		Utils.restoreWord(Bios); // run
		Utils.restoreBytes(Bios, pk.memory, 0xC000, 0x4000);
	}

	// -----------------------------------------------------------------------------
	void dump(OutputStream To, boolean full) throws Exception {
		String Sign = full ? "LVOV/DUMP/3.0/F\u0000" : "LVOV/DUMP/3.0/P\u0000";
		Utils.dumpBytes(To, Sign);

		Utils.dumpBytes(To, new int[16]); // row 1 - flags (full dump)
		Utils.dumpBytes(To,
				new int[] { // row 2 - registers [16]
						((pk.r_PC >> 8) & 0xFF), (pk.r_PC & 0xFF), ((pk.r_SP >> 8) & 0xFF), (pk.r_SP & 0xFF), pk.r_A,
						pk.r_F, pk.r_B, pk.r_C, pk.r_D, pk.r_E, pk.r_H, pk.r_L, 0, 0, 0, 0 });
		Utils.dumpBytes(To,
				new int[] { // row 3 - ports [16]
						pk.ports[0xC0], pk.ports[0xC1], pk.ports[0xC2], pk.ports[0xC3], pk.ports[0xD0], pk.ports[0xD1],
						pk.ports[0xD2], pk.ports[0xD3], 0, 0, 0, 0, 0, 0, 0, 0 });
		Utils.dumpBytes(To, pk.video);
		if (full)
			Utils.dumpBytes(To, pk.memory);
		else
			Utils.dumpBytes(To, pk.memory, 0, 0xC000);
	}

	// -----------------------------------------------------------------------------
	boolean restore(InputStream From) throws Exception {
		int Sign[] = new int[16];

		Utils.restoreBytes(From, Sign); // row 0 - signature
		for (int i = 0; i < 14; i++)
			if ("LVOV/DUMP/3.0/".charAt(i) != Sign[i])
				throw new Exception("Wrong .LVD sign at " + i + " !");

		boolean full = false;
		if (Sign[14] == 'F')
			full = true;
		else if (Sign[14] != 'P')
			throw new Exception("Wrong .LVD sub-sign: " + Utils.HEX(Sign[14]));

		short Line[] = new short[16];
		Utils.restoreBytes(From, Line);
		if (full) // row 1 - flags (full dump)
		{
		}

		Utils.restoreBytes(From, Line); // row 2 - registers
		pk.r_PC = Line[0] * 256 + Line[1];
		pk.r_SP = Line[2] * 256 + Line[3];
		pk.r_A = Line[4];
		pk.r_F = Line[5];
		pk.r_B = Line[6];
		pk.r_C = Line[7];
		pk.r_D = Line[8];
		pk.r_E = Line[9];
		pk.r_H = Line[10];
		pk.r_L = Line[11];

		Utils.restoreBytes(From, Line); // row 3 - ports
		for (int i = 0; i < 4; i++)
			pk.ports[0xC0 + i] = Line[i];
		for (int i = 0; i < 4; i++)
			pk.ports[0xD0 + i] = Line[i + 4];

		Utils.restoreBytes(From, pk.video);
		if (full)
			Utils.restoreBytes(From, pk.memory);
		else
			Utils.restoreBytes(From, pk.memory, 0, 0xC000);

		pk.dirty = null;
		return !full;
	}

	// -----------------------------------------------------------------------------
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (Keyboard.getCommandForShortcut(e) == 0) {
			super.keyPressed(e);
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		if (Keyboard.getCommandForShortcut(e) == 0) {
			super.keyReleased(e);
		}
	}
}
