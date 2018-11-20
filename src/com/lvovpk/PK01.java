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
		super(MODE_FIRST);
	}

	PK01(int mode) {
		super(mode);
	}

	// -----------------------------------------------------------------------------
	void initPrinter(int size) {
		pk.setPrintSpace(size);
	}

	void initSpeaker(int size) {
		pk.setSpeakSpace(size);
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
	void coldStart() {
		pk.reset();
		pk.rPC = 0xC000;
		pk.cpuHaltState = false;
	}

	// -----------------------------------------------------------------------------
	private void setVar(int var, int val) {
		pk.memory[var] = (short) (val & 0xFF);
		pk.memory[var + 1] = (short) (val / 256 & 0xFF);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	private int getVar(int var) {
		return pk.memory[var] + pk.memory[var + 1] * 256;
	}

	// -----------------------------------------------------------------------------
	void warmStart() {
		pk.ports[0xC2] = 0xFF;
		setVar(pk.rSP = Bios.BASIC_STACK, Bios.BASIC_HOT_ENTRY);
		pk.rPC = Bios.BASIC_HOT_ENTRY;
		pk.cpuHaltState = false;
	}

	// -----------------------------------------------------------------------------
	private void loadProgBasic(InputStream prog) throws IOException {
		warmStart();
		int pos = getVar(Bios.BASIC_PROG_BEGIN);
		try {
			for (;; pos++)
				pk.doWrite(pos, Utils.restoreByte(prog));
		} catch (EOFException ex) {
		}
		setVar(Bios.BASIC_PROG_END, pos);
	}

	// -----------------------------------------------------------------------------
	private void saveProgBasic(OutputStream prog) throws IOException {
		int pos = getVar(Bios.BASIC_PROG_BEGIN), cnt;
		for (cnt = 0; pos <= 0xFFFF && cnt < 3; pos++) {
			int bt = pk.doRead(pos);
			if (bt == 0)
				cnt++;
			else
				cnt = 0;
			Utils.dumpByte(prog, bt);
		}
	}

	// -----------------------------------------------------------------------------
	private void loadProgBinary(InputStream prog, boolean manual) throws IOException {
		int beg = Utils.restoreWord(prog);
		int end = Utils.restoreWord(prog);
		int run = Utils.restoreWord(prog);
		int ofs = getVar(Bios.LOAD_BINARY_OFS);

		if (manual) {
			warmStart();
			pk.rPC = run;
		} else {
			beg += ofs;
			end += ofs;
			setVar(Bios.LOAD_BINARY_ENTRY, run);
		}
		for (int pos = beg; pos <= end; pos++)
			pk.doWrite(pos, Utils.restoreByte(prog));
	}

	// -----------------------------------------------------------------------------
	private void saveProgBinary(OutputStream prog, int from, int upto, int epo) throws IOException {
		Utils.dumpWord(prog, from);
		Utils.dumpWord(prog, upto);
		Utils.dumpWord(prog, epo);

		for (int pos = from; pos <= upto; pos++)
			Utils.dumpByte(prog, pk.doRead(pos));
	}

	// -----------------------------------------------------------------------------
	void loadProg(InputStream prog, boolean manual) throws IOException {
		int type = PKIO.recognize(prog);
		switch (type) {
		case 0xD3:
			loadProgBasic(prog);
			break;
		case 0xD0:
			loadProgBinary(prog, manual);
			break;
		default:
			throw new IOException("Unknown .LVT type " + Utils.HEX(type));
		}
	}

	// -----------------------------------------------------------------------------
	void saveProg(OutputStream prog, String name) throws IOException {
		PKIO.prepare(prog, name, 0xD3, PKIO.CP_DEFAULT);
		saveProgBasic(prog);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	void saveProg(OutputStream prog, String name, int from, int upto) throws IOException {
		saveProg(prog, name, from, upto, from);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	void saveProg(OutputStream prog, String name, int from, int upto, int epo) throws IOException {
		PKIO.prepare(prog, name, 0xD0, PKIO.CP_DEFAULT);
		saveProgBinary(prog, from, upto, epo);
	}

	// -----------------------------------------------------------------------------
	void loadBios(InputStream bios) throws IOException {
		int type = PKIO.recognize(bios);
		if (type != 0xD0)
			throw new IOException("Not a binary .LVT !");
		Utils.restoreWord(bios); // beg
		Utils.restoreWord(bios); // end
		Utils.restoreWord(bios); // run
		Utils.restoreBytes(bios, pk.memory, 0xC000, 0x4000);
	}

	// -----------------------------------------------------------------------------
	void dump(OutputStream to, boolean full) throws Exception {
		String sign = full ? "LVOV/DUMP/3.0/F\u0000" : "LVOV/DUMP/3.0/P\u0000";
		Utils.dumpBytes(to, sign);

		Utils.dumpBytes(to, new int[16]); // row 1 - flags (full dump)
		Utils.dumpBytes(to,
				new int[] { // row 2 - registers [16]
						((pk.rPC >> 8) & 0xFF), (pk.rPC & 0xFF), ((pk.rSP >> 8) & 0xFF), (pk.rSP & 0xFF), pk.rA,
						pk.rF, pk.rB, pk.rC, pk.rD, pk.rE, pk.rH, pk.rL, 0, 0, 0, 0 });
		Utils.dumpBytes(to,
				new int[] { // row 3 - ports [16]
						pk.ports[0xC0], pk.ports[0xC1], pk.ports[0xC2], pk.ports[0xC3], pk.ports[0xD0], pk.ports[0xD1],
						pk.ports[0xD2], pk.ports[0xD3], 0, 0, 0, 0, 0, 0, 0, 0 });
		Utils.dumpBytes(to, pk.video);
		if (full)
			Utils.dumpBytes(to, pk.memory);
		else
			Utils.dumpBytes(to, pk.memory, 0, 0xC000);
	}

	// -----------------------------------------------------------------------------
	boolean restore(InputStream from) throws Exception {
		int sign[] = new int[16];

		Utils.restoreBytes(from, sign); // row 0 - signature
		for (int i = 0; i < 14; i++)
			if ("LVOV/DUMP/3.0/".charAt(i) != sign[i])
				throw new Exception("Wrong .LVD sign at " + i + " !");

		boolean full = false;
		if (sign[14] == 'F')
			full = true;
		else if (sign[14] != 'P')
			throw new Exception("Wrong .LVD sub-sign: " + Utils.HEX(sign[14]));

		short line[] = new short[16];
		Utils.restoreBytes(from, line);
		if (full) // row 1 - flags (full dump)
		{
		}

		Utils.restoreBytes(from, line); // row 2 - registers
		pk.rPC = line[0] * 256 + line[1];
		pk.rSP = line[2] * 256 + line[3];
		pk.rA = line[4];
		pk.rF = line[5];
		pk.rB = line[6];
		pk.rC = line[7];
		pk.rD = line[8];
		pk.rE = line[9];
		pk.rH = line[10];
		pk.rL = line[11];

		Utils.restoreBytes(from, line); // row 3 - ports
		for (int i = 0; i < 4; i++)
			pk.ports[0xC0 + i] = line[i];
		for (int i = 0; i < 4; i++)
			pk.ports[0xD0 + i] = line[i + 4];

		Utils.restoreBytes(from, pk.video);
		if (full)
			Utils.restoreBytes(from, pk.memory);
		else
			Utils.restoreBytes(from, pk.memory, 0, 0xC000);

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
