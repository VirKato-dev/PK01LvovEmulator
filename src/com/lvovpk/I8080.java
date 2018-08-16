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
		r_PC = do_read(r_SP);
		r_PC += do_read(r_SP + 1) << 8;
		r_SP = (r_SP + 2) & 0xFFFF;
	}

	// -----------------------------------------------------------------------------
	/*
	 * Format of Flags register:
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~
	 *     7   6   5   4   3   2   1   0
	 * ----+---+---+---+---+---+---+---+----
	 *     fS  fZ  0   fA  0   fP  1   fC
	 */
	final static int f_S = 1 << 7;
	final static int f_Z = 1 << 6;
	final static int f_5 = 1 << 5;
	final static int f_A = 1 << 4;
	final static int f_3 = 1 << 3;
	final static int f_P = 1 << 2;
	final static int f_1 = 1 << 1;
	final static int f_C = 1 << 0;

	// -----------------------------------------------------------------------------
	final static int halt_inv = 1;
	final static int halt_opc = 2;
	final static int halt_hlt = 3;

	final static int halt_bpx = 4;
	final static int halt_bpr = 5;
	final static int halt_bpw = 6;
	final static int halt_bpi = 7;
	final static int halt_bpo = 8;

	// -----------------------------------------------------------------------------
	private static int[] flags;
	boolean halt_if_invalid, cpu_halt_state;
	int r_B, r_C, r_D, r_E, r_H, r_L, r_A, r_F, r_PC, r_SP, clock, cpu_halt_reason;

	// -----------------------------------------------------------------------------
	abstract int do_input(int port);

	abstract void do_output(int port, int bt);

	abstract int do_read(int addr);

	abstract void do_write(int addr, int bt);

	// int do_fetch(int addr) {return do_read(addr);}

	// -----------------------------------------------------------------------------
	static {
		int i, j;
		flags = new int[256];
		for (i = 0; i < 256; i++) {
			flags[i] = f_1 | f_P;
			for (j = 0; j < 8; j++)
				if ((i & (1 << j)) != 0)
					flags[i] ^= f_P;
			if ((i & 0x80) != 0)
				flags[i] |= f_S;
			if (i == 0)
				flags[i] |= f_Z;
		}
	}

	// -----------------------------------------------------------------------------
	I8080() {
		halt_if_invalid = true;
		cpu_halt_state = false;
		cpu_halt_reason = halt_hlt;
		r_B = r_C = r_D = r_E = r_H = r_L = r_A = r_F = r_PC = r_SP = 0;
		clock = 0;
	}

	// -----------------------------------------------------------------------------
	private final boolean signal_inv_opc() {
		cpu_halt_reason = halt_opc;
		if (halt_if_invalid)
			cpu_halt_state = true;
		return halt_if_invalid;
	}

	// -----------------------------------------------------------------------------
	private final void do_write2(int a_lo, int a_hi, int bt) {
		do_write(a_lo + (a_hi << 8), bt);
	}

	private final int do_read2(int a_lo, int a_hi) {
		return do_read(a_lo + (a_hi << 8));
	}

	// -----------------------------------------------------------------------------
	void eval(int ticks) {
		int opcode, tmp1, tmp2, tmp3;
		int upto = clock + ticks;

		for (;;) {
			////////
			// too slow :-( I loose 20 fps on iP120 !
			// opcode = do_fetch(r_PC); // if do_fetch() causes halt it 'll be handled
			////////
			if (r_PC == bpx) {
				cpu_halt_state = true;
				cpu_halt_reason = halt_bpx;
				break;
			}
			opcode = do_read(r_PC); // if do_read() causes halt it 'll be
									// handled
			if (cpu_halt_state || clock > upto)
				break;

			switch (opcode) {
			default:
				cpu_halt_state = true;
				cpu_halt_reason = halt_inv;
				break;
			case 0x00: // 00 NOP
			case 0xF3: // F3 DI
			case 0xFB: // FB EI
				clock += 4;
				r_PC += 1;
				break;
			case 0x01: // 01 LXI B num
				r_B = do_read(r_PC + 2);
				r_C = do_read(r_PC + 1);
				clock += 10;
				r_PC += 3;
				break;
			case 0x02: // 02 STAX B
				do_write2(r_C, r_B, r_A);
				clock += 7;
				r_PC += 1;
				break;
			case 0x03: // 03 INX B
				if (++r_C > 0xFF) {
					r_C = 0;
					if (++r_B > 0xFF)
						r_B = 0;
				}
				;
				clock += 5;
				r_PC += 1;
				break;
			case 0x04: // 04 INR B
				r_F = flags[tmp2 = (r_B + 1) & 0xFF] | (r_F & f_C);
				if (((tmp2 ^ r_B) & 0x10) != 0)
					r_F |= f_A;
				r_B = tmp2;
				clock += 5;
				r_PC += 1;
				break;
			case 0x05: // 05 DCR B
				r_F = flags[tmp2 = (r_B - 1) & 0xFF] | (r_F & f_C);
				if (((tmp2 ^ r_B) & 0x10) != 0)
					r_F |= f_A;
				r_B = tmp2;
				clock += 5;
				r_PC += 1;
				break;
			case 0x06: // 06 MVI B num
				r_B = do_read(r_PC + 1);
				clock += 7;
				r_PC += 2;
				break;
			case 0x07: // 07 RLC
				r_A <<= 1;
				if (r_A > 0xFF) {
					r_A |= 1;
					r_F |= f_C;
					r_A &= 0xFF;
				} else
					r_F &= ~f_C;
				clock += 4;
				r_PC += 1;
				break;
			case 0x08: // 08 _nop_
			case 0x10: // 10 _nop_
			case 0x18: // 18 _nop_
			case 0x20: // 20 _nop_
			case 0x28: // 28 _nop_
			case 0x30: // 30 _nop_
			case 0x38: // 38 _nop_
				if (signal_inv_opc())
					break;
				clock += 4;
				r_PC += 1;
				break;
			case 0x09: // 09 DAD B
				r_L += r_C;
				r_H += r_B;
				if (r_L > 0xFF) {
					r_H++;
					r_L &= 0xFF;
				}
				;
				if (r_H > 0xFF) {
					r_H &= 0xFF;
					r_F |= f_C;
				} else
					r_F &= ~f_C;
				clock += 10;
				r_PC += 1;
				break;
			case 0x0A: // 0A LDAX B
				r_A = do_read2(r_C, r_B);
				clock += 7;
				r_PC += 1;
				break;
			case 0x0B: // 0B DCX B
				if (--r_C < 0) {
					r_C = 0xFF;
					if (--r_B < 0)
						r_B = 0xFF;
				}
				;
				clock += 5;
				r_PC += 1;
				break;
			case 0x0C: // 0C INR C
				r_F = flags[tmp2 = (r_C + 1) & 0xFF] | (r_F & f_C);
				if (((tmp2 ^ r_C) & 0x10) != 0)
					r_F |= f_A;
				r_C = tmp2;
				clock += 5;
				r_PC += 1;
				break;
			case 0x0D: // 0D DCR C
				r_F = flags[tmp2 = (r_C - 1) & 0xFF] | (r_F & f_C);
				if (((tmp2 ^ r_C) & 0x10) != 0)
					r_F |= f_A;
				r_C = tmp2;
				clock += 5;
				r_PC += 1;
				break;
			case 0x0E: // 0E MVI C num
				r_C = do_read(r_PC + 1);
				clock += 7;
				r_PC += 2;
				break;
			case 0x0F: // 0F RRC
				tmp1 = r_A & 1;
				r_A >>= 1;
				if (tmp1 != 0) {
					r_A |= 0x80;
					r_F |= f_C;
				} else
					r_F &= ~f_C;
				clock += 4;
				r_PC += 1;
				break;
			case 0x11: // 11 LXI D num
				r_D = do_read(r_PC + 2);
				r_E = do_read(r_PC + 1);
				clock += 10;
				r_PC += 3;
				break;
			case 0x12: // 12 STAX D
				do_write2(r_E, r_D, r_A);
				clock += 7;
				r_PC += 1;
				break;
			case 0x13: // 13 INX D
				if (++r_E > 0xFF) {
					r_E = 0;
					if (++r_D > 0xFF)
						r_D = 0;
				}
				;
				clock += 5;
				r_PC += 1;
				break;
			case 0x14: // 14 INR D
				r_F = flags[tmp2 = (r_D + 1) & 0xFF] | (r_F & f_C);
				if (((tmp2 ^ r_D) & 0x10) != 0)
					r_F |= f_A;
				r_D = tmp2;
				clock += 5;
				r_PC += 1;
				break;
			case 0x15: // 15 DCR D
				r_F = flags[tmp2 = (r_D - 1) & 0xFF] | (r_F & f_C);
				if (((tmp2 ^ r_D) & 0x10) != 0)
					r_F |= f_A;
				r_D = tmp2;
				clock += 5;
				r_PC += 1;
				break;
			case 0x16: // 16 MVI D num
				r_D = do_read(r_PC + 1);
				clock += 7;
				r_PC += 2;
				break;
			case 0x17: // 17 RAL
				r_A <<= 1;
				if ((r_F & f_C) != 0)
					r_A |= 1;
				if (r_A > 0xFF) {
					r_F |= f_C;
					r_A &= 0xFF;
				} else
					r_F &= ~f_C;
				clock += 4;
				r_PC += 1;
				break;
			case 0x19: // 19 DAD D
				r_L += r_E;
				r_H += r_D;
				if (r_L > 0xFF) {
					r_H++;
					r_L &= 0xFF;
				}
				;
				if (r_H > 0xFF) {
					r_H &= 0xFF;
					r_F |= f_C;
				} else
					r_F &= ~f_C;
				clock += 10;
				r_PC += 1;
				break;
			case 0x1A: // 1A LDAX D
				r_A = do_read2(r_E, r_D);
				clock += 7;
				r_PC += 1;
				break;
			case 0x1B: // 1B DCX D
				if (--r_E < 0) {
					r_E = 0xFF;
					if (--r_D < 0)
						r_D = 0xFF;
				}
				;
				clock += 5;
				r_PC += 1;
				break;
			case 0x1C: // 1C INR E
				r_F = flags[tmp2 = (r_E + 1) & 0xFF] | (r_F & f_C);
				if (((tmp2 ^ r_E) & 0x10) != 0)
					r_F |= f_A;
				r_E = tmp2;
				clock += 5;
				r_PC += 1;
				break;
			case 0x1D: // 1D DCR E
				r_F = flags[tmp2 = (r_E - 1) & 0xFF] | (r_F & f_C);
				if (((tmp2 ^ r_E) & 0x10) != 0)
					r_F |= f_A;
				r_E = tmp2;
				clock += 5;
				r_PC += 1;
				break;
			case 0x1E: // 1E MVI E num
				r_E = do_read(r_PC + 1);
				clock += 7;
				r_PC += 2;
				break;
			case 0x1F: // 1F RAR
				tmp1 = r_A & 1;
				r_A >>= 1;
				if ((r_F & f_C) != 0)
					r_A |= 0x80;
				if (tmp1 != 0)
					r_F |= f_C;
				else
					r_F &= ~f_C;
				clock += 4;
				r_PC += 1;
				break;
			case 0x21: // 21 LXI H num
				r_H = do_read(r_PC + 2);
				r_L = do_read(r_PC + 1);
				clock += 10;
				r_PC += 3;
				break;
			case 0x22: // 22 SHLD addr
				tmp1 = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
				do_write(tmp1, r_L);
				do_write(tmp1 + 1, r_H);
				clock += 16;
				r_PC += 3;
				break;
			case 0x23: // 23 INX H
				if (++r_L > 0xFF) {
					r_L = 0;
					if (++r_H > 0xFF)
						r_H = 0;
				}
				;
				clock += 5;
				r_PC += 1;
				break;
			case 0x24: // 24 INR H
				r_F = flags[tmp2 = (r_H + 1) & 0xFF] | (r_F & f_C);
				if (((tmp2 ^ r_H) & 0x10) != 0)
					r_F |= f_A;
				r_H = tmp2;
				clock += 5;
				r_PC += 1;
				break;
			case 0x25: // 25 DCR H
				r_F = flags[tmp2 = (r_H - 1) & 0xFF] | (r_F & f_C);
				if (((tmp2 ^ r_H) & 0x10) != 0)
					r_F |= f_A;
				r_H = tmp2;
				clock += 5;
				r_PC += 1;
				break;
			case 0x26: // 26 MVI H num
				r_H = do_read(r_PC + 1);
				clock += 7;
				r_PC += 2;
				break;
			case 0x27: // 27 DAA
				tmp1 = 0;
				if (((r_F & f_C) != 0) || (r_A > 0x99))
					tmp1 |= 0x60;
				if (((r_F & f_A) != 0) || ((r_A & 0x0F) > 0x09))
					tmp1 |= 0x06;
				tmp2 = r_A + tmp1;
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ tmp1) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 4;
				r_PC += 1;
				break;
			case 0x29: // 29 DAD H
				r_L += r_L;
				r_H += r_H;
				if (r_L > 0xFF) {
					r_H++;
					r_L &= 0xFF;
				}
				;
				if (r_H > 0xFF) {
					r_H &= 0xFF;
					r_F |= f_C;
				} else
					r_F &= ~f_C;
				clock += 10;
				r_PC += 1;
				break;
			case 0x2A: // 2A LHLD addr
				tmp1 = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
				r_L = do_read(tmp1);
				r_H = do_read(tmp1 + 1);
				clock += 16;
				r_PC += 3;
				break;
			case 0x2B: // 2B DCX H
				if (--r_L < 0) {
					r_L = 0xFF;
					if (--r_H < 0)
						r_H = 0xFF;
				}
				;
				clock += 5;
				r_PC += 1;
				break;
			case 0x2C: // 2C INR L
				r_F = flags[tmp2 = (r_L + 1) & 0xFF] | (r_F & f_C);
				if (((tmp2 ^ r_L) & 0x10) != 0)
					r_F |= f_A;
				r_L = tmp2;
				clock += 5;
				r_PC += 1;
				break;
			case 0x2D: // 2D DCR L
				r_F = flags[tmp2 = (r_L - 1) & 0xFF] | (r_F & f_C);
				if (((tmp2 ^ r_L) & 0x10) != 0)
					r_F |= f_A;
				r_L = tmp2;
				clock += 5;
				r_PC += 1;
				break;
			case 0x2E: // 2E MVI L num
				r_L = do_read(r_PC + 1);
				clock += 7;
				r_PC += 2;
				break;
			case 0x2F: // 2F CMA
				r_A ^= 0xFF;
				clock += 4;
				r_PC += 1;
				break;
			case 0x31: // 31 LXI SP num
				r_SP = (do_read(r_PC + 2) << 8) + do_read(r_PC + 1);
				clock += 10;
				r_PC += 3;
				break;
			case 0x32: // 32 STA addr
				do_write2(do_read(r_PC + 1), do_read(r_PC + 2), r_A);
				clock += 13;
				r_PC += 3;
				break;
			case 0x33: // 33 INX SP
				if (r_SP == 0xFFFF)
					r_SP = 0;
				else
					r_SP++;
				clock += 5;
				r_PC += 1;
				break;
			case 0x34: // 34 INR M
				tmp1 = do_read2(r_L, r_H);
				r_F = flags[tmp2 = (tmp1 + 1) & 0xFF] | (r_F & f_C);
				if (((tmp2 ^ tmp1) & 0x10) != 0)
					r_F |= f_A;
				do_write2(r_L, r_H, tmp2);
				clock += 10;
				r_PC += 1;
				break;
			case 0x35: // 35 DCR M
				tmp1 = do_read2(r_L, r_H);
				r_F = flags[tmp2 = (tmp1 - 1) & 0xFF] | (r_F & f_C);
				if (((tmp2 ^ tmp1) & 0x10) != 0)
					r_F |= f_A;
				do_write2(r_L, r_H, tmp2);
				clock += 10;
				r_PC += 1;
				break;
			case 0x36: // 36 MVI M num
				do_write2(r_L, r_H, do_read(r_PC + 1));
				clock += 10;
				r_PC += 2;
				break;
			case 0x37: // 37 STC
				r_F |= f_C;
				clock += 4;
				r_PC += 1;
				break;
			case 0x39: // 39 DAD SP
				r_L += r_SP & 0xFF;
				r_H += (r_SP >> 8) & 0xFF;
				if (r_L > 0xFF) {
					r_H++;
					r_L &= 0xFF;
				}
				;
				if (r_H > 0xFF) {
					r_H &= 0xFF;
					r_F |= f_C;
				} else
					r_F &= ~f_C;
				clock += 10;
				r_PC += 1;
				break;
			case 0x3A: // 3A LDA addr
				r_A = do_read2(do_read(r_PC + 1), do_read(r_PC + 2));
				clock += 13;
				r_PC += 3;
				break;
			case 0x3B: // 3B DCX SP
				if (r_SP != 0)
					r_SP--;
				else
					r_SP = 0xFFFF;
				clock += 5;
				r_PC += 1;
				break;
			case 0x3C: // 3C INR A
				r_F = flags[tmp2 = (r_A + 1) & 0xFF] | (r_F & f_C);
				if (((tmp2 ^ r_A) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 5;
				r_PC += 1;
				break;
			case 0x3D: // 3D DCR A
				r_F = flags[tmp2 = (r_A - 1) & 0xFF] | (r_F & f_C);
				if (((tmp2 ^ r_A) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 5;
				r_PC += 1;
				break;
			case 0x3E: // 3E MVI A num
				r_A = do_read(r_PC + 1);
				clock += 7;
				r_PC += 2;
				break;
			case 0x3F: // 3F CMC
				r_F ^= f_C;
				clock += 4;
				r_PC += 1;
				break;
			case 0x40: // 40 MOV B B
			case 0x49: // 49 MOV C C
			case 0x52: // 52 MOV D D
			case 0x5B: // 5B MOV E E
			case 0x64: // 64 MOV H H
			case 0x6D: // 6D MOV L L
			case 0x7F: // 7F MOV A A
				clock += 5;
				r_PC += 1;
				break;
			case 0x41: // 41 MOV B C
				r_B = r_C;
				clock += 5;
				r_PC += 1;
				break;
			case 0x42: // 42 MOV B D
				r_B = r_D;
				clock += 5;
				r_PC += 1;
				break;
			case 0x43: // 43 MOV B E
				r_B = r_E;
				clock += 5;
				r_PC += 1;
				break;
			case 0x44: // 44 MOV B H
				r_B = r_H;
				clock += 5;
				r_PC += 1;
				break;
			case 0x45: // 45 MOV B L
				r_B = r_L;
				clock += 5;
				r_PC += 1;
				break;
			case 0x46: // 46 MOV B M
				r_B = do_read2(r_L, r_H);
				clock += 7;
				r_PC += 1;
				break;
			case 0x47: // 47 MOV B A
				r_B = r_A;
				clock += 5;
				r_PC += 1;
				break;
			case 0x48: // 48 MOV C B
				r_C = r_B;
				clock += 5;
				r_PC += 1;
				break;
			case 0x4A: // 4A MOV C D
				r_C = r_D;
				clock += 5;
				r_PC += 1;
				break;
			case 0x4B: // 4B MOV C E
				r_C = r_E;
				clock += 5;
				r_PC += 1;
				break;
			case 0x4C: // 4C MOV C H
				r_C = r_H;
				clock += 5;
				r_PC += 1;
				break;
			case 0x4D: // 4D MOV C L
				r_C = r_L;
				clock += 5;
				r_PC += 1;
				break;
			case 0x4E: // 4E MOV C M
				r_C = do_read2(r_L, r_H);
				clock += 7;
				r_PC += 1;
				break;
			case 0x4F: // 4F MOV C A
				r_C = r_A;
				clock += 5;
				r_PC += 1;
				break;
			case 0x50: // 50 MOV D B
				r_D = r_B;
				clock += 5;
				r_PC += 1;
				break;
			case 0x51: // 51 MOV D C
				r_D = r_C;
				clock += 5;
				r_PC += 1;
				break;
			case 0x53: // 53 MOV D E
				r_D = r_E;
				clock += 5;
				r_PC += 1;
				break;
			case 0x54: // 54 MOV D H
				r_D = r_H;
				clock += 5;
				r_PC += 1;
				break;
			case 0x55: // 55 MOV D L
				r_D = r_L;
				clock += 5;
				r_PC += 1;
				break;
			case 0x56: // 56 MOV D M
				r_D = do_read2(r_L, r_H);
				clock += 7;
				r_PC += 1;
				break;
			case 0x57: // 57 MOV D A
				r_D = r_A;
				clock += 5;
				r_PC += 1;
				break;
			case 0x58: // 58 MOV E B
				r_E = r_B;
				clock += 5;
				r_PC += 1;
				break;
			case 0x59: // 59 MOV E C
				r_E = r_C;
				clock += 5;
				r_PC += 1;
				break;
			case 0x5A: // 5A MOV E D
				r_E = r_D;
				clock += 5;
				r_PC += 1;
				break;
			case 0x5C: // 5C MOV E H
				r_E = r_H;
				clock += 5;
				r_PC += 1;
				break;
			case 0x5D: // 5D MOV E L
				r_E = r_L;
				clock += 5;
				r_PC += 1;
				break;
			case 0x5E: // 5E MOV E M
				r_E = do_read2(r_L, r_H);
				clock += 7;
				r_PC += 1;
				break;
			case 0x5F: // 5F MOV E A
				r_E = r_A;
				clock += 5;
				r_PC += 1;
				break;
			case 0x60: // 60 MOV H B
				r_H = r_B;
				clock += 5;
				r_PC += 1;
				break;
			case 0x61: // 61 MOV H C
				r_H = r_C;
				clock += 5;
				r_PC += 1;
				break;
			case 0x62: // 62 MOV H D
				r_H = r_D;
				clock += 5;
				r_PC += 1;
				break;
			case 0x63: // 63 MOV H E
				r_H = r_E;
				clock += 5;
				r_PC += 1;
				break;
			case 0x65: // 65 MOV H L
				r_H = r_L;
				clock += 5;
				r_PC += 1;
				break;
			case 0x66: // 66 MOV H M
				r_H = do_read2(r_L, r_H);
				clock += 7;
				r_PC += 1;
				break;
			case 0x67: // 67 MOV H A
				r_H = r_A;
				clock += 5;
				r_PC += 1;
				break;
			case 0x68: // 68 MOV L B
				r_L = r_B;
				clock += 5;
				r_PC += 1;
				break;
			case 0x69: // 69 MOV L C
				r_L = r_C;
				clock += 5;
				r_PC += 1;
				break;
			case 0x6A: // 6A MOV L D
				r_L = r_D;
				clock += 5;
				r_PC += 1;
				break;
			case 0x6B: // 6B MOV L E
				r_L = r_E;
				clock += 5;
				r_PC += 1;
				break;
			case 0x6C: // 6C MOV L H
				r_L = r_H;
				clock += 5;
				r_PC += 1;
				break;
			case 0x6E: // 6E MOV L M
				r_L = do_read2(r_L, r_H);
				clock += 7;
				r_PC += 1;
				break;
			case 0x6F: // 6F MOV L A
				r_L = r_A;
				clock += 5;
				r_PC += 1;
				break;
			case 0x70: // 70 MOV M B
				do_write2(r_L, r_H, r_B);
				clock += 7;
				r_PC += 1;
				break;
			case 0x71: // 71 MOV M C
				do_write2(r_L, r_H, r_C);
				clock += 7;
				r_PC += 1;
				break;
			case 0x72: // 72 MOV M D
				do_write2(r_L, r_H, r_D);
				clock += 7;
				r_PC += 1;
				break;
			case 0x73: // 73 MOV M E
				do_write2(r_L, r_H, r_E);
				clock += 7;
				r_PC += 1;
				break;
			case 0x74: // 74 MOV M H
				do_write2(r_L, r_H, r_H);
				clock += 7;
				r_PC += 1;
				break;
			case 0x75: // 75 MOV M L
				do_write2(r_L, r_H, r_L);
				clock += 7;
				r_PC += 1;
				break;
			case 0x76: // 76 HLT
				cpu_halt_state = true;
				cpu_halt_reason = halt_hlt;
				clock += 4;
				r_PC += 1;
				break;
			case 0x77: // 77 MOV M A
				do_write2(r_L, r_H, r_A);
				clock += 7;
				r_PC += 1;
				break;
			case 0x78: // 78 MOV A B
				r_A = r_B;
				clock += 5;
				r_PC += 1;
				break;
			case 0x79: // 79 MOV A C
				r_A = r_C;
				clock += 5;
				r_PC += 1;
				break;
			case 0x7A: // 7A MOV A D
				r_A = r_D;
				clock += 5;
				r_PC += 1;
				break;
			case 0x7B: // 7B MOV A E
				r_A = r_E;
				clock += 5;
				r_PC += 1;
				break;
			case 0x7C: // 7C MOV A H
				r_A = r_H;
				clock += 5;
				r_PC += 1;
				break;
			case 0x7D: // 7D MOV A L
				r_A = r_L;
				clock += 5;
				r_PC += 1;
				break;
			case 0x7E: // 7E MOV A M
				r_A = do_read2(r_L, r_H);
				clock += 7;
				r_PC += 1;
				break;
			case 0x80: // 80 ADD B
				tmp2 = r_A + r_B;
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ r_B) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 4;
				r_PC += 1;
				break;
			case 0x81: // 81 ADD C
				tmp2 = r_A + r_C;
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ r_C) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 4;
				r_PC += 1;
				break;
			case 0x82: // 82 ADD D
				tmp2 = r_A + r_D;
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ r_D) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 4;
				r_PC += 1;
				break;
			case 0x83: // 83 ADD E
				tmp2 = r_A + r_E;
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ r_E) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 4;
				r_PC += 1;
				break;
			case 0x84: // 84 ADD H
				tmp2 = r_A + r_H;
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ r_H) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 4;
				r_PC += 1;
				break;
			case 0x85: // 85 ADD L
				tmp2 = r_A + r_L;
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ r_L) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 4;
				r_PC += 1;
				break;
			case 0x86: // 86 ADD M
				tmp1 = do_read2(r_L, r_H);
				tmp2 = r_A + tmp1;
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ tmp1) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 7;
				r_PC += 1;
				break;
			case 0x87: // 87 ADD A
				tmp2 = r_A + r_A;
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ r_A) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 4;
				r_PC += 1;
				break;
			case 0x88: // 88 ADC B
				tmp2 = r_A + r_B + ((r_F & f_C) != 0 ? 1 : 0);
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ r_B) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 4;
				r_PC += 1;
				break;
			case 0x89: // 89 ADC C
				tmp2 = r_A + r_C + ((r_F & f_C) != 0 ? 1 : 0);
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ r_C) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 4;
				r_PC += 1;
				break;
			case 0x8A: // 8A ADC D
				tmp2 = r_A + r_D + ((r_F & f_C) != 0 ? 1 : 0);
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ r_D) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 4;
				r_PC += 1;
				break;
			case 0x8B: // 8B ADC E
				tmp2 = r_A + r_E + ((r_F & f_C) != 0 ? 1 : 0);
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ r_E) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 4;
				r_PC += 1;
				break;
			case 0x8C: // 8C ADC H
				tmp2 = r_A + r_H + ((r_F & f_C) != 0 ? 1 : 0);
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ r_H) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 4;
				r_PC += 1;
				break;
			case 0x8D: // 8D ADC L
				tmp2 = r_A + r_L + ((r_F & f_C) != 0 ? 1 : 0);
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ r_L) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 4;
				r_PC += 1;
				break;
			case 0x8E: // 8E ADC M
				tmp1 = do_read2(r_L, r_H);
				tmp2 = r_A + tmp1 + ((r_F & f_C) != 0 ? 1 : 0);
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ tmp1) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 7;
				r_PC += 1;
				break;
			case 0x8F: // 8F ADC A
				tmp2 = r_A + r_A + ((r_F & f_C) != 0 ? 1 : 0);
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ r_A) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 4;
				r_PC += 1;
				break;
			case 0x90: // 90 SUB B
				r_F = flags[tmp2 = (r_A - r_B) & 0xFF];
				if (r_A < r_B)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_B) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 4;
				r_PC += 1;
				break;
			case 0x91: // 91 SUB C
				r_F = flags[tmp2 = (r_A - r_C) & 0xFF];
				if (r_A < r_C)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_C) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 4;
				r_PC += 1;
				break;
			case 0x92: // 92 SUB D
				r_F = flags[tmp2 = (r_A - r_D) & 0xFF];
				if (r_A < r_D)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_D) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 4;
				r_PC += 1;
				break;
			case 0x93: // 93 SUB E
				r_F = flags[tmp2 = (r_A - r_E) & 0xFF];
				if (r_A < r_E)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_E) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 4;
				r_PC += 1;
				break;
			case 0x94: // 94 SUB H
				r_F = flags[tmp2 = (r_A - r_H) & 0xFF];
				if (r_A < r_H)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_H) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 4;
				r_PC += 1;
				break;
			case 0x95: // 95 SUB L
				r_F = flags[tmp2 = (r_A - r_L) & 0xFF];
				if (r_A < r_L)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_L) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 4;
				r_PC += 1;
				break;
			case 0x96: // 96 SUB M
				tmp1 = do_read2(r_L, r_H);
				r_F = flags[tmp2 = (r_A - tmp1) & 0xFF];
				if (r_A < tmp1)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ tmp1) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 7;
				r_PC += 1;
				break;
			case 0x97: // 97 SUB A
				r_F = flags[0];
				r_A = 0;
				clock += 4;
				r_PC += 1;
				break;
			case 0x98: // 98 SBB B
				tmp3 = (r_F & f_C) != 0 ? 1 : 0;
				r_F = flags[tmp2 = (r_A - r_B - tmp3) & 0xFF];
				if (r_A < r_B + tmp3)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_B) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 4;
				r_PC += 1;
				break;
			case 0x99: // 99 SBB C
				tmp3 = (r_F & f_C) != 0 ? 1 : 0;
				r_F = flags[tmp2 = (r_A - r_C - tmp3) & 0xFF];
				if (r_A < r_C + tmp3)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_C) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 4;
				r_PC += 1;
				break;
			case 0x9A: // 9A SBB D
				tmp3 = (r_F & f_C) != 0 ? 1 : 0;
				r_F = flags[tmp2 = (r_A - r_D - tmp3) & 0xFF];
				if (r_A < r_D + tmp3)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_D) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 4;
				r_PC += 1;
				break;
			case 0x9B: // 9B SBB E
				tmp3 = (r_F & f_C) != 0 ? 1 : 0;
				r_F = flags[tmp2 = (r_A - r_E - tmp3) & 0xFF];
				if (r_A < r_E + tmp3)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_E) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 4;
				r_PC += 1;
				break;
			case 0x9C: // 9C SBB H
				tmp3 = (r_F & f_C) != 0 ? 1 : 0;
				r_F = flags[tmp2 = (r_A - r_H - tmp3) & 0xFF];
				if (r_A < r_H + tmp3)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_H) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 4;
				r_PC += 1;
				break;
			case 0x9D: // 9D SBB L
				tmp3 = (r_F & f_C) != 0 ? 1 : 0;
				r_F = flags[tmp2 = (r_A - r_L - tmp3) & 0xFF];
				if (r_A < r_L + tmp3)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_L) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 4;
				r_PC += 1;
				break;
			case 0x9E: // 9E SBB M
				tmp1 = do_read2(r_L, r_H);
				tmp3 = (r_F & f_C) != 0 ? 1 : 0;
				r_F = flags[tmp2 = (r_A - tmp1 - tmp3) & 0xFF];
				if (r_A < tmp1 + tmp3)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ tmp1) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 7;
				r_PC += 1;
				break;
			case 0x9F: // 9F SBB A
				r_F = flags[tmp2 = (r_F & f_C) != 0 ? 0xFF : 0];
				if (tmp2 != 0)
					r_F |= f_A | f_C;
				r_A = tmp2;
				clock += 4;
				r_PC += 1;
				break;
			case 0xA0: // A0 ANA B
				r_F = flags[r_A &= r_B] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xA1: // A1 ANA C
				r_F = flags[r_A &= r_C] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xA2: // A2 ANA D
				r_F = flags[r_A &= r_D] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xA3: // A3 ANA E
				r_F = flags[r_A &= r_E] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xA4: // A4 ANA H
				r_F = flags[r_A &= r_H] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xA5: // A5 ANA L
				r_F = flags[r_A &= r_L] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xA6: // A6 ANA M
				tmp1 = do_read2(r_L, r_H);
				r_F = flags[r_A &= tmp1] | (r_F & f_A);
				clock += 7;
				r_PC += 1;
				break;
			case 0xA7: // A7 ANA A
				r_F = flags[r_A &= r_A] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xA8: // A8 XRA B
				r_F = flags[r_A ^= r_B] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xA9: // A9 XRA C
				r_F = flags[r_A ^= r_C] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xAA: // AA XRA D
				r_F = flags[r_A ^= r_D] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xAB: // AB XRA E
				r_F = flags[r_A ^= r_E] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xAC: // AC XRA H
				r_F = flags[r_A ^= r_H] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xAD: // AD XRA L
				r_F = flags[r_A ^= r_L] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xAE: // AE XRA M
				tmp1 = do_read2(r_L, r_H);
				r_F = flags[r_A ^= tmp1] | (r_F & f_A);
				clock += 7;
				r_PC += 1;
				break;
			case 0xAF: // AF XRA A
				r_F = flags[r_A = 0] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xB0: // B0 ORA B
				r_F = flags[r_A |= r_B] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xB1: // B1 ORA C
				r_F = flags[r_A |= r_C] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xB2: // B2 ORA D
				r_F = flags[r_A |= r_D] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xB3: // B3 ORA E
				r_F = flags[r_A |= r_E] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xB4: // B4 ORA H
				r_F = flags[r_A |= r_H] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xB5: // B5 ORA L
				r_F = flags[r_A |= r_L] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xB6: // B6 ORA M
				tmp1 = do_read2(r_L, r_H);
				r_F = flags[r_A |= tmp1] | (r_F & f_A);
				clock += 7;
				r_PC += 1;
				break;
			case 0xB7: // B7 ORA A
				r_F = flags[r_A |= r_A] | (r_F & f_A);
				clock += 4;
				r_PC += 1;
				break;
			case 0xB8: // B8 CMP B
				r_F = flags[tmp2 = (r_A - r_B) & 0xFF];
				if (r_A < r_B)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_B) & 0x10) != 0)
					r_F |= f_A;
				clock += 4;
				r_PC += 1;
				break;
			case 0xB9: // B9 CMP C
				r_F = flags[tmp2 = (r_A - r_C) & 0xFF];
				if (r_A < r_C)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_C) & 0x10) != 0)
					r_F |= f_A;
				clock += 4;
				r_PC += 1;
				break;
			case 0xBA: // BA CMP D
				r_F = flags[tmp2 = (r_A - r_D) & 0xFF];
				if (r_A < r_D)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_D) & 0x10) != 0)
					r_F |= f_A;
				clock += 4;
				r_PC += 1;
				break;
			case 0xBB: // BB CMP E
				r_F = flags[tmp2 = (r_A - r_E) & 0xFF];
				if (r_A < r_E)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_E) & 0x10) != 0)
					r_F |= f_A;
				clock += 4;
				r_PC += 1;
				break;
			case 0xBC: // BC CMP H
				r_F = flags[tmp2 = (r_A - r_H) & 0xFF];
				if (r_A < r_H)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_H) & 0x10) != 0)
					r_F |= f_A;
				clock += 4;
				r_PC += 1;
				break;
			case 0xBD: // BD CMP L
				r_F = flags[tmp2 = (r_A - r_L) & 0xFF];
				if (r_A < r_L)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ r_L) & 0x10) != 0)
					r_F |= f_A;
				clock += 4;
				r_PC += 1;
				break;
			case 0xBE: // BE CMP M
				tmp1 = do_read2(r_L, r_H);
				r_F = flags[tmp2 = (r_A - tmp1) & 0xFF];
				if (r_A < tmp1)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ tmp1) & 0x10) != 0)
					r_F |= f_A;
				clock += 7;
				r_PC += 1;
				break;
			case 0xBF: // BF CMP A
				r_F = flags[0];
				clock += 4;
				r_PC += 1;
				break;
			case 0xC0: // C0 RNZ
				if ((r_F & f_Z) == 0) {
					;
					r_PC = do_read(r_SP);
					r_PC += do_read(r_SP + 1) << 8;
					r_SP = (r_SP + 2) & 0xFFFF;
					clock += 11;
					break;
				} else {
					clock += 5;
				}
				;
				r_PC += 1;
				break;
			case 0xC1: // C1 POP B
				r_C = do_read(r_SP);
				r_B = do_read(r_SP + 1);
				r_SP = (r_SP + 2) & 0xFFFF;
				clock += 10;
				r_PC += 1;
				break;
			case 0xC2: // C2 JNZ addr
				if ((r_F & f_Z) == 0) {
					;
					r_PC = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
					clock += 10;
					break;
				} else {
					clock += 10;
				}
				;
				r_PC += 3;
				break;
			case 0xC3: // C3 JMP addr
				r_PC = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
				clock += 10;
				break;
			case 0xC4: // C4 CNZ addr
				if ((r_F & f_Z) == 0) {
					;
					tmp1 = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
					r_SP = (r_SP - 2) & 0xFFFF;
					r_PC += 3;
					do_write(r_SP, r_PC & 0xFF);
					do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
					r_PC = tmp1;
					clock += 17;
					break;
				} else {
					clock += 11;
				}
				;
				r_PC += 3;
				break;
			case 0xC5: // C5 PUSH B
				r_SP = (r_SP - 2) & 0xFFFF;
				do_write(r_SP, r_C);
				do_write(r_SP + 1, r_B);
				clock += 11;
				r_PC += 1;
				break;
			case 0xC6: // C6 ADI num
				tmp1 = do_read(r_PC + 1);
				tmp2 = r_A + tmp1;
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ tmp1) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 7;
				r_PC += 2;
				break;
			case 0xC7: // C7 RST 0
				r_SP = (r_SP - 2) & 0xFFFF;
				r_PC++;
				do_write(r_SP, r_PC & 0xFF);
				do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
				r_PC = 0 * 8;
				clock += 11;
				break;
			case 0xC8: // C8 RZ
				if ((r_F & f_Z) != 0) {
					;
					r_PC = do_read(r_SP);
					r_PC += do_read(r_SP + 1) << 8;
					r_SP = (r_SP + 2) & 0xFFFF;
					clock += 11;
					break;
				} else {
					clock += 5;
				}
				;
				r_PC += 1;
				break;
			case 0xC9: // C9 RET
				r_PC = do_read(r_SP);
				r_PC += do_read(r_SP + 1) << 8;
				r_SP = (r_SP + 2) & 0xFFFF;
				clock += 11;
				break;
			case 0xCA: // CA JZ addr
				if ((r_F & f_Z) != 0) {
					;
					r_PC = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
					clock += 10;
					break;
				} else {
					clock += 10;
				}
				;
				r_PC += 3;
				break;
			case 0xCB: // CB _jmp_ addr
				if (signal_inv_opc())
					break;
				r_PC = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
				clock += 10;
				break;
			case 0xCC: // CC CZ addr
				if ((r_F & f_Z) != 0) {
					;
					tmp1 = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
					r_SP = (r_SP - 2) & 0xFFFF;
					r_PC += 3;
					do_write(r_SP, r_PC & 0xFF);
					do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
					r_PC = tmp1;
					clock += 17;
					break;
				} else {
					clock += 11;
				}
				;
				r_PC += 3;
				break;
			case 0xCD: // CD CALL addr
				tmp1 = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
				r_SP = (r_SP - 2) & 0xFFFF;
				r_PC += 3;
				do_write(r_SP, r_PC & 0xFF);
				do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
				r_PC = tmp1;
				clock += 17;
				break;
			case 0xCE: // CE ACI num
				tmp1 = do_read(r_PC + 1);
				tmp2 = r_A + tmp1 + ((r_F & f_C) != 0 ? 1 : 0);
				r_F = flags[tmp2 & 0xFF];
				if (((tmp2 ^ r_A ^ tmp1) & 0x10) != 0)
					r_F |= f_A;
				if (tmp2 > 0xFF)
					r_F |= f_C;
				r_A = tmp2 & 0xFF;
				clock += 7;
				r_PC += 2;
				break;
			case 0xCF: // CF RST 1
				r_SP = (r_SP - 2) & 0xFFFF;
				r_PC++;
				do_write(r_SP, r_PC & 0xFF);
				do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
				r_PC = 1 * 8;
				clock += 11;
				break;
			case 0xD0: // D0 RNC
				if ((r_F & f_C) == 0) {
					;
					r_PC = do_read(r_SP);
					r_PC += do_read(r_SP + 1) << 8;
					r_SP = (r_SP + 2) & 0xFFFF;
					clock += 11;
					break;
				} else {
					clock += 5;
				}
				;
				r_PC += 1;
				break;
			case 0xD1: // D1 POP D
				r_E = do_read(r_SP);
				r_D = do_read(r_SP + 1);
				r_SP = (r_SP + 2) & 0xFFFF;
				clock += 10;
				r_PC += 1;
				break;
			case 0xD2: // D2 JNC addr
				if ((r_F & f_C) == 0) {
					;
					r_PC = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
					clock += 10;
					break;
				} else {
					clock += 10;
				}
				;
				r_PC += 3;
				break;
			case 0xD3: // D3 OUT port
				do_output(do_read(r_PC + 1), r_A);
				clock += 10;
				r_PC += 2;
				break;
			case 0xD4: // D4 CNC addr
				if ((r_F & f_C) == 0) {
					;
					tmp1 = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
					r_SP = (r_SP - 2) & 0xFFFF;
					r_PC += 3;
					do_write(r_SP, r_PC & 0xFF);
					do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
					r_PC = tmp1;
					clock += 17;
					break;
				} else {
					clock += 11;
				}
				;
				r_PC += 3;
				break;
			case 0xD5: // D5 PUSH D
				r_SP = (r_SP - 2) & 0xFFFF;
				do_write(r_SP, r_E);
				do_write(r_SP + 1, r_D);
				clock += 11;
				r_PC += 1;
				break;
			case 0xD6: // D6 SUI num
				tmp1 = do_read(r_PC + 1);
				r_F = flags[tmp2 = (r_A - tmp1) & 0xFF];
				if (r_A < tmp1)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ tmp1) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 7;
				r_PC += 2;
				break;
			case 0xD7: // D7 RST 2
				r_SP = (r_SP - 2) & 0xFFFF;
				r_PC++;
				do_write(r_SP, r_PC & 0xFF);
				do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
				r_PC = 2 * 8;
				clock += 11;
				break;
			case 0xD8: // D8 RC
				if ((r_F & f_C) != 0) {
					;
					r_PC = do_read(r_SP);
					r_PC += do_read(r_SP + 1) << 8;
					r_SP = (r_SP + 2) & 0xFFFF;
					clock += 11;
					break;
				} else {
					clock += 5;
				}
				;
				r_PC += 1;
				break;
			case 0xD9: // D9 _ret_
				if (signal_inv_opc())
					break;
				r_PC = do_read(r_SP);
				r_PC += do_read(r_SP + 1) << 8;
				r_SP = (r_SP + 2) & 0xFFFF;
				clock += 11;
				break;
			case 0xDA: // DA JC addr
				if ((r_F & f_C) != 0) {
					;
					r_PC = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
					clock += 10;
					break;
				} else {
					clock += 10;
				}
				;
				r_PC += 3;
				break;
			case 0xDB: // DB IN port
				r_A = do_input(do_read(r_PC + 1));
				clock += 10;
				r_PC += 2;
				break;
			case 0xDC: // DC CC addr
				if ((r_F & f_C) != 0) {
					;
					tmp1 = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
					r_SP = (r_SP - 2) & 0xFFFF;
					r_PC += 3;
					do_write(r_SP, r_PC & 0xFF);
					do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
					r_PC = tmp1;
					clock += 17;
					break;
				} else {
					clock += 11;
				}
				;
				r_PC += 3;
				break;
			case 0xDD: // DD _call_ addr
			case 0xED: // ED _call_ addr
			case 0xFD: // FD _call_ addr
				if (signal_inv_opc())
					break;
				tmp1 = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
				r_SP = (r_SP - 2) & 0xFFFF;
				r_PC += 3;
				do_write(r_SP, r_PC & 0xFF);
				do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
				r_PC = tmp1;
				clock += 17;
				break;
			case 0xDE: // DE SBI num
				tmp1 = do_read(r_PC + 1);
				tmp3 = (r_F & f_C) != 0 ? 1 : 0;
				r_F = flags[tmp2 = (r_A - tmp1 - tmp3) & 0xFF];
				if (r_A < tmp1 + tmp3)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ tmp1) & 0x10) != 0)
					r_F |= f_A;
				r_A = tmp2;
				clock += 7;
				r_PC += 2;
				break;
			case 0xDF: // DF RST 3
				r_SP = (r_SP - 2) & 0xFFFF;
				r_PC++;
				do_write(r_SP, r_PC & 0xFF);
				do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
				r_PC = 3 * 8;
				clock += 11;
				break;
			case 0xE0: // E0 RPO
				if ((r_F & f_P) == 0) {
					;
					r_PC = do_read(r_SP);
					r_PC += do_read(r_SP + 1) << 8;
					r_SP = (r_SP + 2) & 0xFFFF;
					clock += 11;
					break;
				} else {
					clock += 5;
				}
				;
				r_PC += 1;
				break;
			case 0xE1: // E1 POP H
				r_L = do_read(r_SP);
				r_H = do_read(r_SP + 1);
				r_SP = (r_SP + 2) & 0xFFFF;
				clock += 10;
				r_PC += 1;
				break;
			case 0xE2: // E2 JPO addr
				if ((r_F & f_P) == 0) {
					;
					r_PC = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
					clock += 10;
					break;
				} else {
					clock += 10;
				}
				;
				r_PC += 3;
				break;
			case 0xE3: // E3 XTHL
				tmp1 = do_read(r_SP);
				do_write(r_SP, r_L);
				r_L = tmp1;
				tmp1 = do_read(r_SP + 1);
				do_write(r_SP + 1, r_H);
				r_H = tmp1;
				clock += 18;
				r_PC += 1;
				break;
			case 0xE4: // E4 CPO addr
				if ((r_F & f_P) == 0) {
					;
					tmp1 = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
					r_SP = (r_SP - 2) & 0xFFFF;
					r_PC += 3;
					do_write(r_SP, r_PC & 0xFF);
					do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
					r_PC = tmp1;
					clock += 17;
					break;
				} else {
					clock += 11;
				}
				;
				r_PC += 3;
				break;
			case 0xE5: // E5 PUSH H
				r_SP = (r_SP - 2) & 0xFFFF;
				do_write(r_SP, r_L);
				do_write(r_SP + 1, r_H);
				clock += 11;
				r_PC += 1;
				break;
			case 0xE6: // E6 ANI num
				r_F = flags[r_A &= do_read(r_PC + 1)] | (r_F & f_A);
				clock += 7;
				r_PC += 2;
				break;
			case 0xE7: // E7 RST 4
				r_SP = (r_SP - 2) & 0xFFFF;
				r_PC++;
				do_write(r_SP, r_PC & 0xFF);
				do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
				r_PC = 4 * 8;
				clock += 11;
				break;
			case 0xE8: // E8 RPE
				if ((r_F & f_P) != 0) {
					;
					r_PC = do_read(r_SP);
					r_PC += do_read(r_SP + 1) << 8;
					r_SP = (r_SP + 2) & 0xFFFF;
					clock += 11;
					break;
				} else {
					clock += 5;
				}
				;
				r_PC += 1;
				break;
			case 0xE9: // E9 PCHL
				r_PC = (r_H << 8) + r_L;
				clock += 5;
				break;
			case 0xEA: // EA JPE addr
				if ((r_F & f_P) != 0) {
					;
					r_PC = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
					clock += 10;
					break;
				} else {
					clock += 10;
				}
				;
				r_PC += 3;
				break;
			case 0xEB: // EB XCHG
				tmp1 = r_D;
				r_D = r_H;
				r_H = tmp1;
				tmp1 = r_E;
				r_E = r_L;
				r_L = tmp1;
				clock += 4;
				r_PC += 1;
				break;
			case 0xEC: // EC CPE addr
				if ((r_F & f_P) != 0) {
					;
					tmp1 = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
					r_SP = (r_SP - 2) & 0xFFFF;
					r_PC += 3;
					do_write(r_SP, r_PC & 0xFF);
					do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
					r_PC = tmp1;
					clock += 17;
					break;
				} else {
					clock += 11;
				}
				;
				r_PC += 3;
				break;
			case 0xEE: // EE XRI num
				r_F = flags[r_A ^= do_read(r_PC + 1)] | (r_F & f_A);
				clock += 7;
				r_PC += 2;
				break;
			case 0xEF: // EF RST 5
				r_SP = (r_SP - 2) & 0xFFFF;
				r_PC++;
				do_write(r_SP, r_PC & 0xFF);
				do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
				r_PC = 5 * 8;
				clock += 11;
				break;
			case 0xF0: // F0 RP
				if ((r_F & f_S) == 0) {
					;
					r_PC = do_read(r_SP);
					r_PC += do_read(r_SP + 1) << 8;
					r_SP = (r_SP + 2) & 0xFFFF;
					clock += 11;
					break;
				} else {
					clock += 5;
				}
				;
				r_PC += 1;
				break;
			case 0xF1: // F1 POP PSW
				r_F = do_read(r_SP);
				r_A = do_read(r_SP + 1);
				r_SP = (r_SP + 2) & 0xFFFF;
				clock += 10;
				r_PC += 1;
				break;
			case 0xF2: // F2 JP addr
				if ((r_F & f_S) == 0) {
					;
					r_PC = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
					clock += 10;
					break;
				} else {
					clock += 10;
				}
				;
				r_PC += 3;
				break;
			case 0xF4: // F4 CP addr
				if ((r_F & f_S) == 0) {
					;
					tmp1 = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
					r_SP = (r_SP - 2) & 0xFFFF;
					r_PC += 3;
					do_write(r_SP, r_PC & 0xFF);
					do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
					r_PC = tmp1;
					clock += 17;
					break;
				} else {
					clock += 11;
				}
				;
				r_PC += 3;
				break;
			case 0xF5: // F5 PUSH PSW
				r_SP = (r_SP - 2) & 0xFFFF;
				do_write(r_SP, r_F);
				do_write(r_SP + 1, r_A);
				clock += 11;
				r_PC += 1;
				break;
			case 0xF6: // F6 ORI num
				r_F = flags[r_A |= do_read(r_PC + 1)] | (r_F & f_A);
				clock += 7;
				r_PC += 2;
				break;
			case 0xF7: // F7 RST 6
				r_SP = (r_SP - 2) & 0xFFFF;
				r_PC++;
				do_write(r_SP, r_PC & 0xFF);
				do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
				r_PC = 6 * 8;
				clock += 11;
				break;
			case 0xF8: // F8 RM
				if ((r_F & f_S) != 0) {
					;
					r_PC = do_read(r_SP);
					r_PC += do_read(r_SP + 1) << 8;
					r_SP = (r_SP + 2) & 0xFFFF;
					clock += 11;
					break;
				} else {
					clock += 5;
				}
				;
				r_PC += 1;
				break;
			case 0xF9: // F9 SPHL
				r_SP = (r_H << 8) + r_L;
				clock += 5;
				r_PC += 1;
				break;
			case 0xFA: // FA JM addr
				if ((r_F & f_S) != 0) {
					;
					r_PC = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
					clock += 10;
					break;
				} else {
					clock += 10;
				}
				;
				r_PC += 3;
				break;
			case 0xFC: // FC CM addr
				if ((r_F & f_S) != 0) {
					;
					tmp1 = do_read(r_PC + 1) + (do_read(r_PC + 2) << 8);
					r_SP = (r_SP - 2) & 0xFFFF;
					r_PC += 3;
					do_write(r_SP, r_PC & 0xFF);
					do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
					r_PC = tmp1;
					clock += 17;
					break;
				} else {
					clock += 11;
				}
				;
				r_PC += 3;
				break;
			case 0xFE: // FE CPI num
				tmp1 = do_read(r_PC + 1);
				r_F = flags[tmp2 = (r_A - tmp1) & 0xFF];
				if (r_A < tmp1)
					r_F |= f_C;
				if (((tmp2 ^ r_A ^ tmp1) & 0x10) != 0)
					r_F |= f_A;
				clock += 7;
				r_PC += 2;
				break;
			case 0xFF: // FF RST 7
				r_SP = (r_SP - 2) & 0xFFFF;
				r_PC++;
				do_write(r_SP, r_PC & 0xFF);
				do_write(r_SP + 1, (r_PC >> 8) & 0xFF);
				r_PC = 7 * 8;
				clock += 11;
				break;
			}
			r_PC &= 0xFFFF;
		}
	}

	// -----------------------------------------------------------------------------
}
