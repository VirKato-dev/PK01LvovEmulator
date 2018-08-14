package com.lvovpk;

/**
	LVOV Hardware Abstraction
*/
class Lvov extends I8080 {
	byte[] printer;
	int printed;
	long[] speaker;
	int speaked;

	boolean[] dirty;

	short[] memory = new short[0x10000], video = new short[0x4000], ports = new short[0x100];

	// -----------------------------------------------------------------------------
	byte[] kbd_base = new byte[8], kbd_ext = new byte[4];

	// -----------------------------------------------------------------------------
	@Override
	int do_input(int port) {
		int i, r;
		port = 0xC0 + (port & 0x13);
		switch (port) {
		default: // default behaviour for unknown ports
			// return ports[port];
			return 0xFF;

		case 0xC2:
			int C2 = ports[0xC2];
			boolean strobe = (C2 & 4) != 0;
			if (printer != null && strobe && printed < printer.length) {
				printer[printed++] = (byte) (ports[0xC0] ^ 0xFF);
				return strobe ? (C2 | 0x40) : (C2 & 0xBF);
			}
			return C2;

		case 0xD1:
			int nD0 = ~ports[0xD0];
			for (r = i = 0; i < 8; i++)
				if ((nD0 & (1 << i)) != 0)
					r |= kbd_base[i];
			return ~r & 0xFF;

		case 0xD2:
			int nD2 = ~ports[0xD2];
			for (r = i = 0; i < 4; i++)
				if ((nD2 & (1 << i)) != 0)
					r |= kbd_ext[i];
			return ~((r << 4) | (nD2 & 0x0F)) & 0xFF;
		}
	}

	// -----------------------------------------------------------------------------
	@Override
	void do_output(int port, int bt) {
		port = 0xC0 + (port & 0x13);
		switch (port) {
		default:
			break;

		case 0xC1:
			dirty = null;
			break;

		case 0xC2:
			if (speaker != null && speaked < speaker.length && ((bt ^ ports[0xC2]) & 1) != 0)
				speaker[speaked++] = clock;
			break;
		}
		ports[port] = (short) bt;
	}

	// -----------------------------------------------------------------------------
	@Override
	int do_read(int addr) {
		addr &= 0xFFFF;
		if ((ports[0xC2] & 2) != 0)
			return memory[addr];
		else if (addr > 0x7FFF)
			return memory[addr];
		else if (addr < 0x4000)
			return 0;
		else
			return video[addr - 0x4000];
	}

	// -----------------------------------------------------------------------------
	@Override
	void do_write(int addr, int bt) {
		addr &= 0xFFFF;
		if (addr >= 0xC000)
			return;
		else if ((ports[0xC2] & 2) != 0)
			memory[addr] = (short) bt;
		else if (addr < 0x4000)
			return;
		else if (addr < 0x8000) {
			if (dirty != null)
				dirty[(addr - 0x4000) >> 6] = true;
			video[addr - 0x4000] = (short) bt;
		} else
			memory[addr] = (short) bt;
	}

	// -----------------------------------------------------------------------------
	// P a l e t t e G a m e s
	// -----------------------------------------------------------------------------
	private final static byte BLACK = 0;
	private final static byte BLUE = 1;
	private final static byte GREEN = 2;
	private final static byte RED = 4;

	// Calculates color value from palette port
	static byte compute_color_index(int port, int color) {
		byte Result = BLACK;
		if ((port & 0x40) != 0)
			Result ^= BLUE;
		if ((port & 0x20) != 0)
			Result ^= GREEN;
		if ((port & 0x10) != 0)
			Result ^= RED;
		switch (color) {
		default:
			break;
		case 0:
			if ((port & 0x08) == 0)
				Result ^= RED;
			if ((port & 0x04) == 0)
				Result ^= BLUE;
			break;
		case 2:
			Result ^= GREEN;
			break;
		case 3:
			Result ^= RED;
			if ((port & 0x02) == 0)
				Result ^= GREEN;
			break;
		case 1:
			Result ^= BLUE;
			if ((port & 0x01) == 0)
				Result ^= RED;
			break;
		}
		return Result;
	}

	// -----------------------------------------------------------------------------
	void reset() {
		for (int i = 0; i < ports.length; i++)
			ports[i] = 0xFF;
		dirty = null;
	}

	// -----------------------------------------------------------------------------
	void set_print_space(int size) {
		if (size <= 0)
			printer = null;
		else if (printer == null || size < printer.length)
			printer = new byte[size];
		printed = 0;
	}

	// -----------------------------------------------------------------------------
	void set_speak_space(int size) {
		if (size <= 0)
			speaker = null;
		else if (speaker == null || size < speaker.length)
			speaker = new long[size];
		speaked = 0;
	}

	// -----------------------------------------------------------------------------
	Lvov() {
		super();
		int i;
		for (i = 0; i < memory.length; i++)
			memory[i] = 0;
		for (i = 0; i < video.length; i++)
			video[i] = 0;

		for (i = 0; i < kbd_base.length; i++)
			kbd_base[i] = 0;
		for (i = 0; i < kbd_ext.length; i++)
			kbd_ext[i] = 0;
	}

	// -----------------------------------------------------------------------------
}
