package com.lvovpk;

/**
 * Lvov vs. Debugger interface
 */
class LvovDebugger extends Debugger {
	PK01 lv;

	LvovDebugger(PK01 comp) {
		lv = comp;
	}

	// -----------------------------------------------------------------------------
	@Override
	public int A() {
		return lv.pk.r_A;
	}

	@Override
	public int B() {
		return lv.pk.r_B;
	}

	@Override
	public int C() {
		return lv.pk.r_C;
	}

	@Override
	public int D() {
		return lv.pk.r_D;
	}

	@Override
	public int E() {
		return lv.pk.r_E;
	}

	@Override
	public int H() {
		return lv.pk.r_H;
	}

	@Override
	public int L() {
		return lv.pk.r_L;
	}

	@Override
	public int F() {
		return lv.pk.r_F;
	}

	@Override
	public int PC() {
		return lv.pk.r_PC;
	}

	@Override
	public int SP() {
		return lv.pk.r_SP;
	}

	@Override
	public void A(int val) {
		lv.pk.r_A = val & 0xFF;
	}

	@Override
	public void B(int val) {
		lv.pk.r_B = val & 0xFF;
	}

	@Override
	public void C(int val) {
		lv.pk.r_C = val & 0xFF;
	}

	@Override
	public void D(int val) {
		lv.pk.r_D = val & 0xFF;
	}

	@Override
	public void E(int val) {
		lv.pk.r_E = val & 0xFF;
	}

	@Override
	public void H(int val) {
		lv.pk.r_H = val & 0xFF;
	}

	@Override
	public void L(int val) {
		lv.pk.r_L = val & 0xFF;
	}

	@Override
	public void F(int val) {
		lv.pk.r_F = val & 0xFF;
	}

	@Override
	public void PC(int val) {
		lv.pk.r_PC = val & 0xFFFF;
	}

	@Override
	public void SP(int val) {
		lv.pk.r_SP = val & 0xFFFF;
	}

	// -----------------------------------------------------------------------------
	@Override
	public void STEP1() {
		lv.pk.eval(1);
	}

	// -----------------------------------------------------------------------------
	@Override
	public int MEM(int addr) {
		return lv.pk.do_read(addr);
	}

	@Override
	public void MEM(int addr, int val) {
		lv.pk.do_write(addr, val);
	}

	@Override
	public int IO(int addr) {
		return lv.pk.do_input(addr);
	}

	@Override
	public void IO(int addr, int val) {
		lv.pk.do_output(addr, val);
	}

	@Override
	public int CLK() {
		return lv.pk.clock;
	}

	@Override
	public void CLK(int val) {
		lv.pk.clock = val;
	}

	// -----------------------------------------------------------------------------
	@Override
	public boolean HALTED() {
		return lv.pk.cpu_halt_state;
	}

	@Override
	public int REASON() {
		return lv.pk.cpu_halt_reason;
	}

	// -----------------------------------------------------------------------------
}
