package com.lvovpk;

/**
 * LVOV Hardware Abstraction
 */
class Lvov extends I8080 {
	byte[] printer;
	int printed;
	long[] speaker;
	int speaked;

	boolean[] dirty;

	short[] memory = new short[0x10000], video = new short[0x4000], ports = new short[0x100];

	// -----------------------------------------------------------------------------
	byte[] kbdBase = new byte[8], kbdExt = new byte[4];

	// -----------------------------------------------------------------------------
	@Override
	int doInput(int port) {
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
				return (C2 | 0x40); // strobe ? (C2 | 0x40) : (C2 & 0xBF);
			}
			return C2;

		case 0xD1:
			int nD0 = ~ports[0xD0];
			for (r = i = 0; i < 8; i++)
				if ((nD0 & (1 << i)) != 0)
					r |= kbdBase[i];
			return ~r & 0xFF;

		case 0xD2:
			int nD2 = ~ports[0xD2];
			for (r = i = 0; i < 4; i++)
				if ((nD2 & (1 << i)) != 0)
					r |= kbdExt[i];
			return ~((r << 4) | (nD2 & 0x0F)) & 0xFF;
		}
	}

	// -----------------------------------------------------------------------------
	@Override
	void doOutput(int port, int bt) {
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
	int doRead(int addr) {
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
	void doWrite(int addr, int bt) {
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
	static byte computeColorIndex(int port, int color) {
		byte result = BLACK;
		if ((port & 0x40) != 0)
			result ^= BLUE;
		if ((port & 0x20) != 0)
			result ^= GREEN;
		if ((port & 0x10) != 0)
			result ^= RED;
		switch (color) {
		default:
			break;
		case 0:
			if ((port & 0x08) == 0)
				result ^= RED;
			if ((port & 0x04) == 0)
				result ^= BLUE;
			break;
		case 2:
			result ^= GREEN;
			break;
		case 3:
			result ^= RED;
			if ((port & 0x02) == 0)
				result ^= GREEN;
			break;
		case 1:
			result ^= BLUE;
			if ((port & 0x01) == 0)
				result ^= RED;
			break;
		}
		return result;
	}

	// -----------------------------------------------------------------------------
	void reset() {
		for (int i = 0; i < ports.length; i++)
			ports[i] = 0xFF;
		dirty = null;
	}

	// -----------------------------------------------------------------------------
	void setPrintSpace(int size) {
		if (size <= 0)
			printer = null;
		else if (printer == null || size < printer.length)
			printer = new byte[size];
		printed = 0;
	}

	// -----------------------------------------------------------------------------
	void setSpeakSpace(int size) {
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

		for (i = 0; i < kbdBase.length; i++)
			kbdBase[i] = 0;
		for (i = 0; i < kbdExt.length; i++)
			kbdExt[i] = 0;
	}

	// -----------------------------------------------------------------------------
}
