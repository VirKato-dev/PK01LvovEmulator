package com.lvovpk;

/**
 * Abstract i8080 CPU based on unfinished Lvov Emulator attempt #2B4
 */
abstract class I8080 {
	////////
	// Debugging is here only for speedup considerations :-(
	////////
	int bpx = -1;

	void simuret() {
		rPC = doRead(rSP);
		rPC += doRead(rSP + 1) << 8;
		rSP = (rSP + 2) & 0xFFFF;
	}

	// -----------------------------------------------------------------------------
	/*
	 * Format of Flags register:
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~
	 *     7   6   5   4   3   2   1   0
	 * ----+---+---+---+---+---+---+---+----
	 *     fS  fZ  0   fA  0   fP  1   fC
	 */
	final static int F_S = 1 << 7;
	final static int F_Z = 1 << 6;
	final static int F_5 = 1 << 5;
	final static int F_A = 1 << 4;
	final static int F_3 = 1 << 3;
	final static int F_P = 1 << 2;
	final static int F_1 = 1 << 1;
	final static int F_C = 1 << 0;

	// -----------------------------------------------------------------------------
	final static int HALT_INV = 1;
	final static int HALT_OPC = 2;
	final static int HALT_HLT = 3;

	final static int HALT_BPX = 4;
	final static int HALT_BPR = 5;
	final static int HALT_BPW = 6;
	final static int HALT_BPI = 7;
	final static int HALT_BPO = 8;

	// -----------------------------------------------------------------------------
	private static int[] flags;
	boolean haltIfInvalid, cpuHaltState;
	int rB, rC, rD, rE, rH, rL, rA, rF, rPC, rSP; // Registers: B,C,D,E,H,L,A,F,PC,SP
	int clock, cpuHaltReason;

	// -----------------------------------------------------------------------------
	abstract int doInput(int port);

	abstract void doOutput(int port, int bt);

	abstract int doRead(int addr);

	abstract void doWrite(int addr, int bt);

	// int doFetch(int addr) {return doRead(addr);}

	// -----------------------------------------------------------------------------
	static {
		int i, j;
		flags = new int[256];
		for (i = 0; i < 256; i++) {
			flags[i] = F_1 | F_P;
			for (j = 0; j < 8; j++)
				if ((i & (1 << j)) != 0)
					flags[i] ^= F_P;
			if ((i & 0x80) != 0)
				flags[i] |= F_S;
			if (i == 0)
				flags[i] |= F_Z;
		}
	}

	// -----------------------------------------------------------------------------
	I8080() {
		haltIfInvalid = true;
		cpuHaltState = false;
		cpuHaltReason = HALT_HLT;
		rB = rC = rD = rE = rH = rL = rA = rF = rPC = rSP = 0;
		clock = 0;
	}

	// -----------------------------------------------------------------------------
	private final boolean signalInvOpc() {
		cpuHaltReason = HALT_OPC;
		if (haltIfInvalid)
			cpuHaltState = true;
		return haltIfInvalid;
	}

	// -----------------------------------------------------------------------------
	private final void doWrite2(int aLo, int aHi, int bt) {
		doWrite(aLo + (aHi << 8), bt);
	}

	private final int doRead2(int aLo, int aHi) {
		return doRead(aLo + (aHi << 8));
	}

	// -----------------------------------------------------------------------------
	void eval(int ticks) {
		int opcode, tmp1, tmp2, tmp3;
		int upto = clock + ticks;

		for (;;) {
			////////
			// too slow :-( I loose 20 fps on iP120 !
			// opcode = doFetch(rPC); // if doFetch() causes halt it 'll be handled
			////////
			if (rPC == bpx) {
				cpuHaltState = true;
				cpuHaltReason = HALT_BPX;
				break;
			}
			opcode = doRead(rPC); // if doRead() causes halt it 'll be handled
			if (cpuHaltState || clock > upto)
				break;

			switch (opcode) {
			default:
				cpuHaltState = true;
				cpuHaltReason = HALT_INV;
				break;
			case 0x00: // 00 NOP
			case 0xF3: // F3 DI
			case 0xFB: // FB EI
				clock += 4;
				rPC += 1;
				break;
			case 0x01: // 01 LXI B num
				rB = doRead(rPC + 2);
				rC = doRead(rPC + 1);
				clock += 10;
				rPC += 3;
				break;
			case 0x02: // 02 STAX B
				doWrite2(rC, rB, rA);
				clock += 7;
				rPC += 1;
				break;
			case 0x03: // 03 INX B
				if (++rC > 0xFF) {
					rC = 0;
					if (++rB > 0xFF)
						rB = 0;
				}
				clock += 5;
				rPC += 1;
				break;
			case 0x04: // 04 INR B
				rF = flags[tmp2 = (rB + 1) & 0xFF] | (rF & F_C);
				if (((tmp2 ^ rB) & 0x10) != 0)
					rF |= F_A;
				rB = tmp2;
				clock += 5;
				rPC += 1;
				break;
			case 0x05: // 05 DCR B
				rF = flags[tmp2 = (rB - 1) & 0xFF] | (rF & F_C);
				if (((tmp2 ^ rB) & 0x10) != 0)
					rF |= F_A;
				rB = tmp2;
				clock += 5;
				rPC += 1;
				break;
			case 0x06: // 06 MVI B num
				rB = doRead(rPC + 1);
				clock += 7;
				rPC += 2;
				break;
			case 0x07: // 07 RLC
				rA <<= 1;
				if (rA > 0xFF) {
					rA |= 1;
					rF |= F_C;
					rA &= 0xFF;
				} else
					rF &= ~F_C;
				clock += 4;
				rPC += 1;
				break;
			case 0x08: // 08 _nop_
			case 0x10: // 10 _nop_
			case 0x18: // 18 _nop_
			case 0x20: // 20 _nop_
			case 0x28: // 28 _nop_
			case 0x30: // 30 _nop_
			case 0x38: // 38 _nop_
				if (signalInvOpc())
					break;
				clock += 4;
				rPC += 1;
				break;
			case 0x09: // 09 DAD B
				rL += rC;
				rH += rB;
				if (rL > 0xFF) {
					rH++;
					rL &= 0xFF;
				}
				if (rH > 0xFF) {
					rH &= 0xFF;
					rF |= F_C;
				} else
					rF &= ~F_C;
				clock += 10;
				rPC += 1;
				break;
			case 0x0A: // 0A LDAX B
				rA = doRead2(rC, rB);
				clock += 7;
				rPC += 1;
				break;
			case 0x0B: // 0B DCX B
				if (--rC < 0) {
					rC = 0xFF;
					if (--rB < 0)
						rB = 0xFF;
				}
				clock += 5;
				rPC += 1;
				break;
			case 0x0C: // 0C INR C
				rF = flags[tmp2 = (rC + 1) & 0xFF] | (rF & F_C);
				if (((tmp2 ^ rC) & 0x10) != 0)
					rF |= F_A;
				rC = tmp2;
				clock += 5;
				rPC += 1;
				break;
			case 0x0D: // 0D DCR C
				rF = flags[tmp2 = (rC - 1) & 0xFF] | (rF & F_C);
				if (((tmp2 ^ rC) & 0x10) != 0)
					rF |= F_A;
				rC = tmp2;
				clock += 5;
				rPC += 1;
				break;
			case 0x0E: // 0E MVI C num
				rC = doRead(rPC + 1);
				clock += 7;
				rPC += 2;
				break;
			case 0x0F: // 0F RRC
				tmp1 = rA & 1;
				rA >>= 1;
				if (tmp1 != 0) {
					rA |= 0x80;
					rF |= F_C;
				} else
					rF &= ~F_C;
				clock += 4;
				rPC += 1;
				break;
			case 0x11: // 11 LXI D num
				rD = doRead(rPC + 2);
				rE = doRead(rPC + 1);
				clock += 10;
				rPC += 3;
				break;
			case 0x12: // 12 STAX D
				doWrite2(rE, rD, rA);
				clock += 7;
				rPC += 1;
				break;
			case 0x13: // 13 INX D
				if (++rE > 0xFF) {
					rE = 0;
					if (++rD > 0xFF)
						rD = 0;
				}
				clock += 5;
				rPC += 1;
				break;
			case 0x14: // 14 INR D
				rF = flags[tmp2 = (rD + 1) & 0xFF] | (rF & F_C);
				if (((tmp2 ^ rD) & 0x10) != 0)
					rF |= F_A;
				rD = tmp2;
				clock += 5;
				rPC += 1;
				break;
			case 0x15: // 15 DCR D
				rF = flags[tmp2 = (rD - 1) & 0xFF] | (rF & F_C);
				if (((tmp2 ^ rD) & 0x10) != 0)
					rF |= F_A;
				rD = tmp2;
				clock += 5;
				rPC += 1;
				break;
			case 0x16: // 16 MVI D num
				rD = doRead(rPC + 1);
				clock += 7;
				rPC += 2;
				break;
			case 0x17: // 17 RAL
				rA <<= 1;
				if ((rF & F_C) != 0)
					rA |= 1;
				if (rA > 0xFF) {
					rF |= F_C;
					rA &= 0xFF;
				} else
					rF &= ~F_C;
				clock += 4;
				rPC += 1;
				break;
			case 0x19: // 19 DAD D
				rL += rE;
				rH += rD;
				if (rL > 0xFF) {
					rH++;
					rL &= 0xFF;
				}
				if (rH > 0xFF) {
					rH &= 0xFF;
					rF |= F_C;
				} else
					rF &= ~F_C;
				clock += 10;
				rPC += 1;
				break;
			case 0x1A: // 1A LDAX D
				rA = doRead2(rE, rD);
				clock += 7;
				rPC += 1;
				break;
			case 0x1B: // 1B DCX D
				if (--rE < 0) {
					rE = 0xFF;
					if (--rD < 0)
						rD = 0xFF;
				}
				clock += 5;
				rPC += 1;
				break;
			case 0x1C: // 1C INR E
				rF = flags[tmp2 = (rE + 1) & 0xFF] | (rF & F_C);
				if (((tmp2 ^ rE) & 0x10) != 0)
					rF |= F_A;
				rE = tmp2;
				clock += 5;
				rPC += 1;
				break;
			case 0x1D: // 1D DCR E
				rF = flags[tmp2 = (rE - 1) & 0xFF] | (rF & F_C);
				if (((tmp2 ^ rE) & 0x10) != 0)
					rF |= F_A;
				rE = tmp2;
				clock += 5;
				rPC += 1;
				break;
			case 0x1E: // 1E MVI E num
				rE = doRead(rPC + 1);
				clock += 7;
				rPC += 2;
				break;
			case 0x1F: // 1F RAR
				tmp1 = rA & 1;
				rA >>= 1;
				if ((rF & F_C) != 0)
					rA |= 0x80;
				if (tmp1 != 0)
					rF |= F_C;
				else
					rF &= ~F_C;
				clock += 4;
				rPC += 1;
				break;
			case 0x21: // 21 LXI H num
				rH = doRead(rPC + 2);
				rL = doRead(rPC + 1);
				clock += 10;
				rPC += 3;
				break;
			case 0x22: // 22 SHLD addr
				tmp1 = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
				doWrite(tmp1, rL);
				doWrite(tmp1 + 1, rH);
				clock += 16;
				rPC += 3;
				break;
			case 0x23: // 23 INX H
				if (++rL > 0xFF) {
					rL = 0;
					if (++rH > 0xFF)
						rH = 0;
				}
				clock += 5;
				rPC += 1;
				break;
			case 0x24: // 24 INR H
				rF = flags[tmp2 = (rH + 1) & 0xFF] | (rF & F_C);
				if (((tmp2 ^ rH) & 0x10) != 0)
					rF |= F_A;
				rH = tmp2;
				clock += 5;
				rPC += 1;
				break;
			case 0x25: // 25 DCR H
				rF = flags[tmp2 = (rH - 1) & 0xFF] | (rF & F_C);
				if (((tmp2 ^ rH) & 0x10) != 0)
					rF |= F_A;
				rH = tmp2;
				clock += 5;
				rPC += 1;
				break;
			case 0x26: // 26 MVI H num
				rH = doRead(rPC + 1);
				clock += 7;
				rPC += 2;
				break;
			case 0x27: // 27 DAA
				tmp1 = 0;
				if (((rF & F_C) != 0) || (rA > 0x99))
					tmp1 |= 0x60;
				if (((rF & F_A) != 0) || ((rA & 0x0F) > 0x09))
					tmp1 |= 0x06;
				tmp2 = rA + tmp1;
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ tmp1) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 4;
				rPC += 1;
				break;
			case 0x29: // 29 DAD H
				rL += rL;
				rH += rH;
				if (rL > 0xFF) {
					rH++;
					rL &= 0xFF;
				}
				if (rH > 0xFF) {
					rH &= 0xFF;
					rF |= F_C;
				} else
					rF &= ~F_C;
				clock += 10;
				rPC += 1;
				break;
			case 0x2A: // 2A LHLD addr
				tmp1 = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
				rL = doRead(tmp1);
				rH = doRead(tmp1 + 1);
				clock += 16;
				rPC += 3;
				break;
			case 0x2B: // 2B DCX H
				if (--rL < 0) {
					rL = 0xFF;
					if (--rH < 0)
						rH = 0xFF;
				}
				clock += 5;
				rPC += 1;
				break;
			case 0x2C: // 2C INR L
				rF = flags[tmp2 = (rL + 1) & 0xFF] | (rF & F_C);
				if (((tmp2 ^ rL) & 0x10) != 0)
					rF |= F_A;
				rL = tmp2;
				clock += 5;
				rPC += 1;
				break;
			case 0x2D: // 2D DCR L
				rF = flags[tmp2 = (rL - 1) & 0xFF] | (rF & F_C);
				if (((tmp2 ^ rL) & 0x10) != 0)
					rF |= F_A;
				rL = tmp2;
				clock += 5;
				rPC += 1;
				break;
			case 0x2E: // 2E MVI L num
				rL = doRead(rPC + 1);
				clock += 7;
				rPC += 2;
				break;
			case 0x2F: // 2F CMA
				rA ^= 0xFF;
				clock += 4;
				rPC += 1;
				break;
			case 0x31: // 31 LXI SP num
				rSP = (doRead(rPC + 2) << 8) + doRead(rPC + 1);
				clock += 10;
				rPC += 3;
				break;
			case 0x32: // 32 STA addr
				doWrite2(doRead(rPC + 1), doRead(rPC + 2), rA);
				clock += 13;
				rPC += 3;
				break;
			case 0x33: // 33 INX SP
				if (rSP == 0xFFFF)
					rSP = 0;
				else
					rSP++;
				clock += 5;
				rPC += 1;
				break;
			case 0x34: // 34 INR M
				tmp1 = doRead2(rL, rH);
				rF = flags[tmp2 = (tmp1 + 1) & 0xFF] | (rF & F_C);
				if (((tmp2 ^ tmp1) & 0x10) != 0)
					rF |= F_A;
				doWrite2(rL, rH, tmp2);
				clock += 10;
				rPC += 1;
				break;
			case 0x35: // 35 DCR M
				tmp1 = doRead2(rL, rH);
				rF = flags[tmp2 = (tmp1 - 1) & 0xFF] | (rF & F_C);
				if (((tmp2 ^ tmp1) & 0x10) != 0)
					rF |= F_A;
				doWrite2(rL, rH, tmp2);
				clock += 10;
				rPC += 1;
				break;
			case 0x36: // 36 MVI M num
				doWrite2(rL, rH, doRead(rPC + 1));
				clock += 10;
				rPC += 2;
				break;
			case 0x37: // 37 STC
				rF |= F_C;
				clock += 4;
				rPC += 1;
				break;
			case 0x39: // 39 DAD SP
				rL += rSP & 0xFF;
				rH += (rSP >> 8) & 0xFF;
				if (rL > 0xFF) {
					rH++;
					rL &= 0xFF;
				}
				if (rH > 0xFF) {
					rH &= 0xFF;
					rF |= F_C;
				} else
					rF &= ~F_C;
				clock += 10;
				rPC += 1;
				break;
			case 0x3A: // 3A LDA addr
				rA = doRead2(doRead(rPC + 1), doRead(rPC + 2));
				clock += 13;
				rPC += 3;
				break;
			case 0x3B: // 3B DCX SP
				if (rSP != 0)
					rSP--;
				else
					rSP = 0xFFFF;
				clock += 5;
				rPC += 1;
				break;
			case 0x3C: // 3C INR A
				rF = flags[tmp2 = (rA + 1) & 0xFF] | (rF & F_C);
				if (((tmp2 ^ rA) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 5;
				rPC += 1;
				break;
			case 0x3D: // 3D DCR A
				rF = flags[tmp2 = (rA - 1) & 0xFF] | (rF & F_C);
				if (((tmp2 ^ rA) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 5;
				rPC += 1;
				break;
			case 0x3E: // 3E MVI A num
				rA = doRead(rPC + 1);
				clock += 7;
				rPC += 2;
				break;
			case 0x3F: // 3F CMC
				rF ^= F_C;
				clock += 4;
				rPC += 1;
				break;
			case 0x40: // 40 MOV B B
			case 0x49: // 49 MOV C C
			case 0x52: // 52 MOV D D
			case 0x5B: // 5B MOV E E
			case 0x64: // 64 MOV H H
			case 0x6D: // 6D MOV L L
			case 0x7F: // 7F MOV A A
				clock += 5;
				rPC += 1;
				break;
			case 0x41: // 41 MOV B C
				rB = rC;
				clock += 5;
				rPC += 1;
				break;
			case 0x42: // 42 MOV B D
				rB = rD;
				clock += 5;
				rPC += 1;
				break;
			case 0x43: // 43 MOV B E
				rB = rE;
				clock += 5;
				rPC += 1;
				break;
			case 0x44: // 44 MOV B H
				rB = rH;
				clock += 5;
				rPC += 1;
				break;
			case 0x45: // 45 MOV B L
				rB = rL;
				clock += 5;
				rPC += 1;
				break;
			case 0x46: // 46 MOV B M
				rB = doRead2(rL, rH);
				clock += 7;
				rPC += 1;
				break;
			case 0x47: // 47 MOV B A
				rB = rA;
				clock += 5;
				rPC += 1;
				break;
			case 0x48: // 48 MOV C B
				rC = rB;
				clock += 5;
				rPC += 1;
				break;
			case 0x4A: // 4A MOV C D
				rC = rD;
				clock += 5;
				rPC += 1;
				break;
			case 0x4B: // 4B MOV C E
				rC = rE;
				clock += 5;
				rPC += 1;
				break;
			case 0x4C: // 4C MOV C H
				rC = rH;
				clock += 5;
				rPC += 1;
				break;
			case 0x4D: // 4D MOV C L
				rC = rL;
				clock += 5;
				rPC += 1;
				break;
			case 0x4E: // 4E MOV C M
				rC = doRead2(rL, rH);
				clock += 7;
				rPC += 1;
				break;
			case 0x4F: // 4F MOV C A
				rC = rA;
				clock += 5;
				rPC += 1;
				break;
			case 0x50: // 50 MOV D B
				rD = rB;
				clock += 5;
				rPC += 1;
				break;
			case 0x51: // 51 MOV D C
				rD = rC;
				clock += 5;
				rPC += 1;
				break;
			case 0x53: // 53 MOV D E
				rD = rE;
				clock += 5;
				rPC += 1;
				break;
			case 0x54: // 54 MOV D H
				rD = rH;
				clock += 5;
				rPC += 1;
				break;
			case 0x55: // 55 MOV D L
				rD = rL;
				clock += 5;
				rPC += 1;
				break;
			case 0x56: // 56 MOV D M
				rD = doRead2(rL, rH);
				clock += 7;
				rPC += 1;
				break;
			case 0x57: // 57 MOV D A
				rD = rA;
				clock += 5;
				rPC += 1;
				break;
			case 0x58: // 58 MOV E B
				rE = rB;
				clock += 5;
				rPC += 1;
				break;
			case 0x59: // 59 MOV E C
				rE = rC;
				clock += 5;
				rPC += 1;
				break;
			case 0x5A: // 5A MOV E D
				rE = rD;
				clock += 5;
				rPC += 1;
				break;
			case 0x5C: // 5C MOV E H
				rE = rH;
				clock += 5;
				rPC += 1;
				break;
			case 0x5D: // 5D MOV E L
				rE = rL;
				clock += 5;
				rPC += 1;
				break;
			case 0x5E: // 5E MOV E M
				rE = doRead2(rL, rH);
				clock += 7;
				rPC += 1;
				break;
			case 0x5F: // 5F MOV E A
				rE = rA;
				clock += 5;
				rPC += 1;
				break;
			case 0x60: // 60 MOV H B
				rH = rB;
				clock += 5;
				rPC += 1;
				break;
			case 0x61: // 61 MOV H C
				rH = rC;
				clock += 5;
				rPC += 1;
				break;
			case 0x62: // 62 MOV H D
				rH = rD;
				clock += 5;
				rPC += 1;
				break;
			case 0x63: // 63 MOV H E
				rH = rE;
				clock += 5;
				rPC += 1;
				break;
			case 0x65: // 65 MOV H L
				rH = rL;
				clock += 5;
				rPC += 1;
				break;
			case 0x66: // 66 MOV H M
				rH = doRead2(rL, rH);
				clock += 7;
				rPC += 1;
				break;
			case 0x67: // 67 MOV H A
				rH = rA;
				clock += 5;
				rPC += 1;
				break;
			case 0x68: // 68 MOV L B
				rL = rB;
				clock += 5;
				rPC += 1;
				break;
			case 0x69: // 69 MOV L C
				rL = rC;
				clock += 5;
				rPC += 1;
				break;
			case 0x6A: // 6A MOV L D
				rL = rD;
				clock += 5;
				rPC += 1;
				break;
			case 0x6B: // 6B MOV L E
				rL = rE;
				clock += 5;
				rPC += 1;
				break;
			case 0x6C: // 6C MOV L H
				rL = rH;
				clock += 5;
				rPC += 1;
				break;
			case 0x6E: // 6E MOV L M
				rL = doRead2(rL, rH);
				clock += 7;
				rPC += 1;
				break;
			case 0x6F: // 6F MOV L A
				rL = rA;
				clock += 5;
				rPC += 1;
				break;
			case 0x70: // 70 MOV M B
				doWrite2(rL, rH, rB);
				clock += 7;
				rPC += 1;
				break;
			case 0x71: // 71 MOV M C
				doWrite2(rL, rH, rC);
				clock += 7;
				rPC += 1;
				break;
			case 0x72: // 72 MOV M D
				doWrite2(rL, rH, rD);
				clock += 7;
				rPC += 1;
				break;
			case 0x73: // 73 MOV M E
				doWrite2(rL, rH, rE);
				clock += 7;
				rPC += 1;
				break;
			case 0x74: // 74 MOV M H
				doWrite2(rL, rH, rH);
				clock += 7;
				rPC += 1;
				break;
			case 0x75: // 75 MOV M L
				doWrite2(rL, rH, rL);
				clock += 7;
				rPC += 1;
				break;
			case 0x76: // 76 HLT
				cpuHaltState = true;
				cpuHaltReason = HALT_HLT;
				clock += 4;
				rPC += 1;
				break;
			case 0x77: // 77 MOV M A
				doWrite2(rL, rH, rA);
				clock += 7;
				rPC += 1;
				break;
			case 0x78: // 78 MOV A B
				rA = rB;
				clock += 5;
				rPC += 1;
				break;
			case 0x79: // 79 MOV A C
				rA = rC;
				clock += 5;
				rPC += 1;
				break;
			case 0x7A: // 7A MOV A D
				rA = rD;
				clock += 5;
				rPC += 1;
				break;
			case 0x7B: // 7B MOV A E
				rA = rE;
				clock += 5;
				rPC += 1;
				break;
			case 0x7C: // 7C MOV A H
				rA = rH;
				clock += 5;
				rPC += 1;
				break;
			case 0x7D: // 7D MOV A L
				rA = rL;
				clock += 5;
				rPC += 1;
				break;
			case 0x7E: // 7E MOV A M
				rA = doRead2(rL, rH);
				clock += 7;
				rPC += 1;
				break;
			case 0x80: // 80 ADD B
				tmp2 = rA + rB;
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ rB) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 4;
				rPC += 1;
				break;
			case 0x81: // 81 ADD C
				tmp2 = rA + rC;
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ rC) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 4;
				rPC += 1;
				break;
			case 0x82: // 82 ADD D
				tmp2 = rA + rD;
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ rD) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 4;
				rPC += 1;
				break;
			case 0x83: // 83 ADD E
				tmp2 = rA + rE;
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ rE) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 4;
				rPC += 1;
				break;
			case 0x84: // 84 ADD H
				tmp2 = rA + rH;
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ rH) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 4;
				rPC += 1;
				break;
			case 0x85: // 85 ADD L
				tmp2 = rA + rL;
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ rL) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 4;
				rPC += 1;
				break;
			case 0x86: // 86 ADD M
				tmp1 = doRead2(rL, rH);
				tmp2 = rA + tmp1;
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ tmp1) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 7;
				rPC += 1;
				break;
			case 0x87: // 87 ADD A
				tmp2 = rA + rA;
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ rA) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 4;
				rPC += 1;
				break;
			case 0x88: // 88 ADC B
				tmp2 = rA + rB + ((rF & F_C) != 0 ? 1 : 0);
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ rB) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 4;
				rPC += 1;
				break;
			case 0x89: // 89 ADC C
				tmp2 = rA + rC + ((rF & F_C) != 0 ? 1 : 0);
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ rC) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 4;
				rPC += 1;
				break;
			case 0x8A: // 8A ADC D
				tmp2 = rA + rD + ((rF & F_C) != 0 ? 1 : 0);
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ rD) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 4;
				rPC += 1;
				break;
			case 0x8B: // 8B ADC E
				tmp2 = rA + rE + ((rF & F_C) != 0 ? 1 : 0);
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ rE) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 4;
				rPC += 1;
				break;
			case 0x8C: // 8C ADC H
				tmp2 = rA + rH + ((rF & F_C) != 0 ? 1 : 0);
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ rH) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 4;
				rPC += 1;
				break;
			case 0x8D: // 8D ADC L
				tmp2 = rA + rL + ((rF & F_C) != 0 ? 1 : 0);
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ rL) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 4;
				rPC += 1;
				break;
			case 0x8E: // 8E ADC M
				tmp1 = doRead2(rL, rH);
				tmp2 = rA + tmp1 + ((rF & F_C) != 0 ? 1 : 0);
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ tmp1) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 7;
				rPC += 1;
				break;
			case 0x8F: // 8F ADC A
				tmp2 = rA + rA + ((rF & F_C) != 0 ? 1 : 0);
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ rA) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 4;
				rPC += 1;
				break;
			case 0x90: // 90 SUB B
				rF = flags[tmp2 = (rA - rB) & 0xFF];
				if (rA < rB)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rB) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 4;
				rPC += 1;
				break;
			case 0x91: // 91 SUB C
				rF = flags[tmp2 = (rA - rC) & 0xFF];
				if (rA < rC)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rC) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 4;
				rPC += 1;
				break;
			case 0x92: // 92 SUB D
				rF = flags[tmp2 = (rA - rD) & 0xFF];
				if (rA < rD)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rD) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 4;
				rPC += 1;
				break;
			case 0x93: // 93 SUB E
				rF = flags[tmp2 = (rA - rE) & 0xFF];
				if (rA < rE)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rE) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 4;
				rPC += 1;
				break;
			case 0x94: // 94 SUB H
				rF = flags[tmp2 = (rA - rH) & 0xFF];
				if (rA < rH)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rH) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 4;
				rPC += 1;
				break;
			case 0x95: // 95 SUB L
				rF = flags[tmp2 = (rA - rL) & 0xFF];
				if (rA < rL)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rL) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 4;
				rPC += 1;
				break;
			case 0x96: // 96 SUB M
				tmp1 = doRead2(rL, rH);
				rF = flags[tmp2 = (rA - tmp1) & 0xFF];
				if (rA < tmp1)
					rF |= F_C;
				if (((tmp2 ^ rA ^ tmp1) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 7;
				rPC += 1;
				break;
			case 0x97: // 97 SUB A
				rF = flags[0];
				rA = 0;
				clock += 4;
				rPC += 1;
				break;
			case 0x98: // 98 SBB B
				tmp3 = (rF & F_C) != 0 ? 1 : 0;
				rF = flags[tmp2 = (rA - rB - tmp3) & 0xFF];
				if (rA < rB + tmp3)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rB) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 4;
				rPC += 1;
				break;
			case 0x99: // 99 SBB C
				tmp3 = (rF & F_C) != 0 ? 1 : 0;
				rF = flags[tmp2 = (rA - rC - tmp3) & 0xFF];
				if (rA < rC + tmp3)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rC) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 4;
				rPC += 1;
				break;
			case 0x9A: // 9A SBB D
				tmp3 = (rF & F_C) != 0 ? 1 : 0;
				rF = flags[tmp2 = (rA - rD - tmp3) & 0xFF];
				if (rA < rD + tmp3)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rD) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 4;
				rPC += 1;
				break;
			case 0x9B: // 9B SBB E
				tmp3 = (rF & F_C) != 0 ? 1 : 0;
				rF = flags[tmp2 = (rA - rE - tmp3) & 0xFF];
				if (rA < rE + tmp3)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rE) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 4;
				rPC += 1;
				break;
			case 0x9C: // 9C SBB H
				tmp3 = (rF & F_C) != 0 ? 1 : 0;
				rF = flags[tmp2 = (rA - rH - tmp3) & 0xFF];
				if (rA < rH + tmp3)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rH) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 4;
				rPC += 1;
				break;
			case 0x9D: // 9D SBB L
				tmp3 = (rF & F_C) != 0 ? 1 : 0;
				rF = flags[tmp2 = (rA - rL - tmp3) & 0xFF];
				if (rA < rL + tmp3)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rL) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 4;
				rPC += 1;
				break;
			case 0x9E: // 9E SBB M
				tmp1 = doRead2(rL, rH);
				tmp3 = (rF & F_C) != 0 ? 1 : 0;
				rF = flags[tmp2 = (rA - tmp1 - tmp3) & 0xFF];
				if (rA < tmp1 + tmp3)
					rF |= F_C;
				if (((tmp2 ^ rA ^ tmp1) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 7;
				rPC += 1;
				break;
			case 0x9F: // 9F SBB A
				rF = flags[tmp2 = (rF & F_C) != 0 ? 0xFF : 0];
				if (tmp2 != 0)
					rF |= F_A | F_C;
				rA = tmp2;
				clock += 4;
				rPC += 1;
				break;
			case 0xA0: // A0 ANA B
				rF = flags[rA &= rB] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xA1: // A1 ANA C
				rF = flags[rA &= rC] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xA2: // A2 ANA D
				rF = flags[rA &= rD] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xA3: // A3 ANA E
				rF = flags[rA &= rE] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xA4: // A4 ANA H
				rF = flags[rA &= rH] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xA5: // A5 ANA L
				rF = flags[rA &= rL] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xA6: // A6 ANA M
				tmp1 = doRead2(rL, rH);
				rF = flags[rA &= tmp1] | (rF & F_A);
				clock += 7;
				rPC += 1;
				break;
			case 0xA7: // A7 ANA A
				rF = flags[rA /* &= rA */] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xA8: // A8 XRA B
				rF = flags[rA ^= rB] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xA9: // A9 XRA C
				rF = flags[rA ^= rC] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xAA: // AA XRA D
				rF = flags[rA ^= rD] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xAB: // AB XRA E
				rF = flags[rA ^= rE] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xAC: // AC XRA H
				rF = flags[rA ^= rH] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xAD: // AD XRA L
				rF = flags[rA ^= rL] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xAE: // AE XRA M
				tmp1 = doRead2(rL, rH);
				rF = flags[rA ^= tmp1] | (rF & F_A);
				clock += 7;
				rPC += 1;
				break;
			case 0xAF: // AF XRA A
				rF = flags[rA = 0] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xB0: // B0 ORA B
				rF = flags[rA |= rB] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xB1: // B1 ORA C
				rF = flags[rA |= rC] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xB2: // B2 ORA D
				rF = flags[rA |= rD] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xB3: // B3 ORA E
				rF = flags[rA |= rE] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xB4: // B4 ORA H
				rF = flags[rA |= rH] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xB5: // B5 ORA L
				rF = flags[rA |= rL] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xB6: // B6 ORA M
				tmp1 = doRead2(rL, rH);
				rF = flags[rA |= tmp1] | (rF & F_A);
				clock += 7;
				rPC += 1;
				break;
			case 0xB7: // B7 ORA A
				rF = flags[rA /* |= rA */] | (rF & F_A);
				clock += 4;
				rPC += 1;
				break;
			case 0xB8: // B8 CMP B
				rF = flags[tmp2 = (rA - rB) & 0xFF];
				if (rA < rB)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rB) & 0x10) != 0)
					rF |= F_A;
				clock += 4;
				rPC += 1;
				break;
			case 0xB9: // B9 CMP C
				rF = flags[tmp2 = (rA - rC) & 0xFF];
				if (rA < rC)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rC) & 0x10) != 0)
					rF |= F_A;
				clock += 4;
				rPC += 1;
				break;
			case 0xBA: // BA CMP D
				rF = flags[tmp2 = (rA - rD) & 0xFF];
				if (rA < rD)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rD) & 0x10) != 0)
					rF |= F_A;
				clock += 4;
				rPC += 1;
				break;
			case 0xBB: // BB CMP E
				rF = flags[tmp2 = (rA - rE) & 0xFF];
				if (rA < rE)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rE) & 0x10) != 0)
					rF |= F_A;
				clock += 4;
				rPC += 1;
				break;
			case 0xBC: // BC CMP H
				rF = flags[tmp2 = (rA - rH) & 0xFF];
				if (rA < rH)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rH) & 0x10) != 0)
					rF |= F_A;
				clock += 4;
				rPC += 1;
				break;
			case 0xBD: // BD CMP L
				rF = flags[tmp2 = (rA - rL) & 0xFF];
				if (rA < rL)
					rF |= F_C;
				if (((tmp2 ^ rA ^ rL) & 0x10) != 0)
					rF |= F_A;
				clock += 4;
				rPC += 1;
				break;
			case 0xBE: // BE CMP M
				tmp1 = doRead2(rL, rH);
				rF = flags[tmp2 = (rA - tmp1) & 0xFF];
				if (rA < tmp1)
					rF |= F_C;
				if (((tmp2 ^ rA ^ tmp1) & 0x10) != 0)
					rF |= F_A;
				clock += 7;
				rPC += 1;
				break;
			case 0xBF: // BF CMP A
				rF = flags[0];
				clock += 4;
				rPC += 1;
				break;
			case 0xC0: // C0 RNZ
				if ((rF & F_Z) == 0) {
					rPC = doRead(rSP);
					rPC += doRead(rSP + 1) << 8;
					rSP = (rSP + 2) & 0xFFFF;
					clock += 11;
					break;
				} else {
					clock += 5;
				}
				rPC += 1;
				break;
			case 0xC1: // C1 POP B
				rC = doRead(rSP);
				rB = doRead(rSP + 1);
				rSP = (rSP + 2) & 0xFFFF;
				clock += 10;
				rPC += 1;
				break;
			case 0xC2: // C2 JNZ addr
				if ((rF & F_Z) == 0) {
					rPC = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
					clock += 10;
					break;
				} else {
					clock += 10;
				}
				rPC += 3;
				break;
			case 0xC3: // C3 JMP addr
				rPC = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
				clock += 10;
				break;
			case 0xC4: // C4 CNZ addr
				if ((rF & F_Z) == 0) {
					tmp1 = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
					rSP = (rSP - 2) & 0xFFFF;
					rPC += 3;
					doWrite(rSP, rPC & 0xFF);
					doWrite(rSP + 1, (rPC >> 8) & 0xFF);
					rPC = tmp1;
					clock += 17;
					break;
				} else {
					clock += 11;
				}
				rPC += 3;
				break;
			case 0xC5: // C5 PUSH B
				rSP = (rSP - 2) & 0xFFFF;
				doWrite(rSP, rC);
				doWrite(rSP + 1, rB);
				clock += 11;
				rPC += 1;
				break;
			case 0xC6: // C6 ADI num
				tmp1 = doRead(rPC + 1);
				tmp2 = rA + tmp1;
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ tmp1) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 7;
				rPC += 2;
				break;
			case 0xC7: // C7 RST 0
				rSP = (rSP - 2) & 0xFFFF;
				rPC++;
				doWrite(rSP, rPC & 0xFF);
				doWrite(rSP + 1, (rPC >> 8) & 0xFF);
				rPC = 0 * 8;
				clock += 11;
				break;
			case 0xC8: // C8 RZ
				if ((rF & F_Z) != 0) {
					rPC = doRead(rSP);
					rPC += doRead(rSP + 1) << 8;
					rSP = (rSP + 2) & 0xFFFF;
					clock += 11;
					break;
				} else {
					clock += 5;
				}
				rPC += 1;
				break;
			case 0xC9: // C9 RET
				rPC = doRead(rSP);
				rPC += doRead(rSP + 1) << 8;
				rSP = (rSP + 2) & 0xFFFF;
				clock += 11;
				break;
			case 0xCA: // CA JZ addr
				if ((rF & F_Z) != 0) {
					rPC = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
					clock += 10;
					break;
				} else {
					clock += 10;
				}
				rPC += 3;
				break;
			case 0xCB: // CB _jmp_ addr
				if (signalInvOpc())
					break;
				rPC = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
				clock += 10;
				break;
			case 0xCC: // CC CZ addr
				if ((rF & F_Z) != 0) {
					tmp1 = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
					rSP = (rSP - 2) & 0xFFFF;
					rPC += 3;
					doWrite(rSP, rPC & 0xFF);
					doWrite(rSP + 1, (rPC >> 8) & 0xFF);
					rPC = tmp1;
					clock += 17;
					break;
				} else {
					clock += 11;
				}
				rPC += 3;
				break;
			case 0xCD: // CD CALL addr
				tmp1 = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
				rSP = (rSP - 2) & 0xFFFF;
				rPC += 3;
				doWrite(rSP, rPC & 0xFF);
				doWrite(rSP + 1, (rPC >> 8) & 0xFF);
				rPC = tmp1;
				clock += 17;
				break;
			case 0xCE: // CE ACI num
				tmp1 = doRead(rPC + 1);
				tmp2 = rA + tmp1 + ((rF & F_C) != 0 ? 1 : 0);
				rF = flags[tmp2 & 0xFF];
				if (((tmp2 ^ rA ^ tmp1) & 0x10) != 0)
					rF |= F_A;
				if (tmp2 > 0xFF)
					rF |= F_C;
				rA = tmp2 & 0xFF;
				clock += 7;
				rPC += 2;
				break;
			case 0xCF: // CF RST 1
				rSP = (rSP - 2) & 0xFFFF;
				rPC++;
				doWrite(rSP, rPC & 0xFF);
				doWrite(rSP + 1, (rPC >> 8) & 0xFF);
				rPC = 1 * 8;
				clock += 11;
				break;
			case 0xD0: // D0 RNC
				if ((rF & F_C) == 0) {
					rPC = doRead(rSP);
					rPC += doRead(rSP + 1) << 8;
					rSP = (rSP + 2) & 0xFFFF;
					clock += 11;
					break;
				} else {
					clock += 5;
				}
				rPC += 1;
				break;
			case 0xD1: // D1 POP D
				rE = doRead(rSP);
				rD = doRead(rSP + 1);
				rSP = (rSP + 2) & 0xFFFF;
				clock += 10;
				rPC += 1;
				break;
			case 0xD2: // D2 JNC addr
				if ((rF & F_C) == 0) {
					rPC = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
					clock += 10;
					break;
				} else {
					clock += 10;
				}
				rPC += 3;
				break;
			case 0xD3: // D3 OUT port
				doOutput(doRead(rPC + 1), rA);
				clock += 10;
				rPC += 2;
				break;
			case 0xD4: // D4 CNC addr
				if ((rF & F_C) == 0) {
					tmp1 = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
					rSP = (rSP - 2) & 0xFFFF;
					rPC += 3;
					doWrite(rSP, rPC & 0xFF);
					doWrite(rSP + 1, (rPC >> 8) & 0xFF);
					rPC = tmp1;
					clock += 17;
					break;
				} else {
					clock += 11;
				}
				rPC += 3;
				break;
			case 0xD5: // D5 PUSH D
				rSP = (rSP - 2) & 0xFFFF;
				doWrite(rSP, rE);
				doWrite(rSP + 1, rD);
				clock += 11;
				rPC += 1;
				break;
			case 0xD6: // D6 SUI num
				tmp1 = doRead(rPC + 1);
				rF = flags[tmp2 = (rA - tmp1) & 0xFF];
				if (rA < tmp1)
					rF |= F_C;
				if (((tmp2 ^ rA ^ tmp1) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 7;
				rPC += 2;
				break;
			case 0xD7: // D7 RST 2
				rSP = (rSP - 2) & 0xFFFF;
				rPC++;
				doWrite(rSP, rPC & 0xFF);
				doWrite(rSP + 1, (rPC >> 8) & 0xFF);
				rPC = 2 * 8;
				clock += 11;
				break;
			case 0xD8: // D8 RC
				if ((rF & F_C) != 0) {
					rPC = doRead(rSP);
					rPC += doRead(rSP + 1) << 8;
					rSP = (rSP + 2) & 0xFFFF;
					clock += 11;
					break;
				} else {
					clock += 5;
				}
				rPC += 1;
				break;
			case 0xD9: // D9 _ret_
				if (signalInvOpc())
					break;
				rPC = doRead(rSP);
				rPC += doRead(rSP + 1) << 8;
				rSP = (rSP + 2) & 0xFFFF;
				clock += 11;
				break;
			case 0xDA: // DA JC addr
				if ((rF & F_C) != 0) {
					rPC = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
					clock += 10;
					break;
				} else {
					clock += 10;
				}
				rPC += 3;
				break;
			case 0xDB: // DB IN port
				rA = doInput(doRead(rPC + 1));
				clock += 10;
				rPC += 2;
				break;
			case 0xDC: // DC CC addr
				if ((rF & F_C) != 0) {
					tmp1 = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
					rSP = (rSP - 2) & 0xFFFF;
					rPC += 3;
					doWrite(rSP, rPC & 0xFF);
					doWrite(rSP + 1, (rPC >> 8) & 0xFF);
					rPC = tmp1;
					clock += 17;
					break;
				} else {
					clock += 11;
				}
				rPC += 3;
				break;
			case 0xDD: // DD _call_ addr
			case 0xED: // ED _call_ addr
			case 0xFD: // FD _call_ addr
				if (signalInvOpc())
					break;
				tmp1 = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
				rSP = (rSP - 2) & 0xFFFF;
				rPC += 3;
				doWrite(rSP, rPC & 0xFF);
				doWrite(rSP + 1, (rPC >> 8) & 0xFF);
				rPC = tmp1;
				clock += 17;
				break;
			case 0xDE: // DE SBI num
				tmp1 = doRead(rPC + 1);
				tmp3 = (rF & F_C) != 0 ? 1 : 0;
				rF = flags[tmp2 = (rA - tmp1 - tmp3) & 0xFF];
				if (rA < tmp1 + tmp3)
					rF |= F_C;
				if (((tmp2 ^ rA ^ tmp1) & 0x10) != 0)
					rF |= F_A;
				rA = tmp2;
				clock += 7;
				rPC += 2;
				break;
			case 0xDF: // DF RST 3
				rSP = (rSP - 2) & 0xFFFF;
				rPC++;
				doWrite(rSP, rPC & 0xFF);
				doWrite(rSP + 1, (rPC >> 8) & 0xFF);
				rPC = 3 * 8;
				clock += 11;
				break;
			case 0xE0: // E0 RPO
				if ((rF & F_P) == 0) {
					rPC = doRead(rSP);
					rPC += doRead(rSP + 1) << 8;
					rSP = (rSP + 2) & 0xFFFF;
					clock += 11;
					break;
				} else {
					clock += 5;
				}
				rPC += 1;
				break;
			case 0xE1: // E1 POP H
				rL = doRead(rSP);
				rH = doRead(rSP + 1);
				rSP = (rSP + 2) & 0xFFFF;
				clock += 10;
				rPC += 1;
				break;
			case 0xE2: // E2 JPO addr
				if ((rF & F_P) == 0) {
					rPC = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
					clock += 10;
					break;
				} else {
					clock += 10;
				}
				rPC += 3;
				break;
			case 0xE3: // E3 XTHL
				tmp1 = doRead(rSP);
				doWrite(rSP, rL);
				rL = tmp1;
				tmp1 = doRead(rSP + 1);
				doWrite(rSP + 1, rH);
				rH = tmp1;
				clock += 18;
				rPC += 1;
				break;
			case 0xE4: // E4 CPO addr
				if ((rF & F_P) == 0) {
					tmp1 = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
					rSP = (rSP - 2) & 0xFFFF;
					rPC += 3;
					doWrite(rSP, rPC & 0xFF);
					doWrite(rSP + 1, (rPC >> 8) & 0xFF);
					rPC = tmp1;
					clock += 17;
					break;
				} else {
					clock += 11;
				}
				rPC += 3;
				break;
			case 0xE5: // E5 PUSH H
				rSP = (rSP - 2) & 0xFFFF;
				doWrite(rSP, rL);
				doWrite(rSP + 1, rH);
				clock += 11;
				rPC += 1;
				break;
			case 0xE6: // E6 ANI num
				rF = flags[rA &= doRead(rPC + 1)] | (rF & F_A);
				clock += 7;
				rPC += 2;
				break;
			case 0xE7: // E7 RST 4
				rSP = (rSP - 2) & 0xFFFF;
				rPC++;
				doWrite(rSP, rPC & 0xFF);
				doWrite(rSP + 1, (rPC >> 8) & 0xFF);
				rPC = 4 * 8;
				clock += 11;
				break;
			case 0xE8: // E8 RPE
				if ((rF & F_P) != 0) {
					rPC = doRead(rSP);
					rPC += doRead(rSP + 1) << 8;
					rSP = (rSP + 2) & 0xFFFF;
					clock += 11;
					break;
				} else {
					clock += 5;
				}
				rPC += 1;
				break;
			case 0xE9: // E9 PCHL
				rPC = (rH << 8) + rL;
				clock += 5;
				break;
			case 0xEA: // EA JPE addr
				if ((rF & F_P) != 0) {
					rPC = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
					clock += 10;
					break;
				} else {
					clock += 10;
				}
				rPC += 3;
				break;
			case 0xEB: // EB XCHG
				tmp1 = rD;
				rD = rH;
				rH = tmp1;
				tmp1 = rE;
				rE = rL;
				rL = tmp1;
				clock += 4;
				rPC += 1;
				break;
			case 0xEC: // EC CPE addr
				if ((rF & F_P) != 0) {
					tmp1 = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
					rSP = (rSP - 2) & 0xFFFF;
					rPC += 3;
					doWrite(rSP, rPC & 0xFF);
					doWrite(rSP + 1, (rPC >> 8) & 0xFF);
					rPC = tmp1;
					clock += 17;
					break;
				} else {
					clock += 11;
				}
				rPC += 3;
				break;
			case 0xEE: // EE XRI num
				rF = flags[rA ^= doRead(rPC + 1)] | (rF & F_A);
				clock += 7;
				rPC += 2;
				break;
			case 0xEF: // EF RST 5
				rSP = (rSP - 2) & 0xFFFF;
				rPC++;
				doWrite(rSP, rPC & 0xFF);
				doWrite(rSP + 1, (rPC >> 8) & 0xFF);
				rPC = 5 * 8;
				clock += 11;
				break;
			case 0xF0: // F0 RP
				if ((rF & F_S) == 0) {
					rPC = doRead(rSP);
					rPC += doRead(rSP + 1) << 8;
					rSP = (rSP + 2) & 0xFFFF;
					clock += 11;
					break;
				} else {
					clock += 5;
				}
				rPC += 1;
				break;
			case 0xF1: // F1 POP PSW
				rF = doRead(rSP);
				rA = doRead(rSP + 1);
				rSP = (rSP + 2) & 0xFFFF;
				clock += 10;
				rPC += 1;
				break;
			case 0xF2: // F2 JP addr
				if ((rF & F_S) == 0) {
					rPC = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
					clock += 10;
					break;
				} else {
					clock += 10;
				}
				rPC += 3;
				break;
			case 0xF4: // F4 CP addr
				if ((rF & F_S) == 0) {
					tmp1 = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
					rSP = (rSP - 2) & 0xFFFF;
					rPC += 3;
					doWrite(rSP, rPC & 0xFF);
					doWrite(rSP + 1, (rPC >> 8) & 0xFF);
					rPC = tmp1;
					clock += 17;
					break;
				} else {
					clock += 11;
				}
				rPC += 3;
				break;
			case 0xF5: // F5 PUSH PSW
				rSP = (rSP - 2) & 0xFFFF;
				doWrite(rSP, rF);
				doWrite(rSP + 1, rA);
				clock += 11;
				rPC += 1;
				break;
			case 0xF6: // F6 ORI num
				rF = flags[rA |= doRead(rPC + 1)] | (rF & F_A);
				clock += 7;
				rPC += 2;
				break;
			case 0xF7: // F7 RST 6
				rSP = (rSP - 2) & 0xFFFF;
				rPC++;
				doWrite(rSP, rPC & 0xFF);
				doWrite(rSP + 1, (rPC >> 8) & 0xFF);
				rPC = 6 * 8;
				clock += 11;
				break;
			case 0xF8: // F8 RM
				if ((rF & F_S) != 0) {
					rPC = doRead(rSP);
					rPC += doRead(rSP + 1) << 8;
					rSP = (rSP + 2) & 0xFFFF;
					clock += 11;
					break;
				} else {
					clock += 5;
				}
				rPC += 1;
				break;
			case 0xF9: // F9 SPHL
				rSP = (rH << 8) + rL;
				clock += 5;
				rPC += 1;
				break;
			case 0xFA: // FA JM addr
				if ((rF & F_S) != 0) {
					rPC = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
					clock += 10;
					break;
				} else {
					clock += 10;
				}
				rPC += 3;
				break;
			case 0xFC: // FC CM addr
				if ((rF & F_S) != 0) {
					tmp1 = doRead(rPC + 1) + (doRead(rPC + 2) << 8);
					rSP = (rSP - 2) & 0xFFFF;
					rPC += 3;
					doWrite(rSP, rPC & 0xFF);
					doWrite(rSP + 1, (rPC >> 8) & 0xFF);
					rPC = tmp1;
					clock += 17;
					break;
				} else {
					clock += 11;
				}
				rPC += 3;
				break;
			case 0xFE: // FE CPI num
				tmp1 = doRead(rPC + 1);
				rF = flags[tmp2 = (rA - tmp1) & 0xFF];
				if (rA < tmp1)
					rF |= F_C;
				if (((tmp2 ^ rA ^ tmp1) & 0x10) != 0)
					rF |= F_A;
				clock += 7;
				rPC += 2;
				break;
			case 0xFF: // FF RST 7
				rSP = (rSP - 2) & 0xFFFF;
				rPC++;
				doWrite(rSP, rPC & 0xFF);
				doWrite(rSP + 1, (rPC >> 8) & 0xFF);
				rPC = 7 * 8;
				clock += 11;
				break;
			}
			rPC &= 0xFFFF;
		}
	}

	// -----------------------------------------------------------------------------
}
