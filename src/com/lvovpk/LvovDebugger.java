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
		return lv.pk.rA;
	}

	@Override
	public int B() {
		return lv.pk.rB;
	}

	@Override
	public int C() {
		return lv.pk.rC;
	}

	@Override
	public int D() {
		return lv.pk.rD;
	}

	@Override
	public int E() {
		return lv.pk.rE;
	}

	@Override
	public int H() {
		return lv.pk.rH;
	}

	@Override
	public int L() {
		return lv.pk.rL;
	}

	@Override
	public int F() {
		return lv.pk.rF;
	}

	@Override
	public int PC() {
		return lv.pk.rPC;
	}

	@Override
	public int SP() {
		return lv.pk.rSP;
	}

	@Override
	public void A(int val) {
		lv.pk.rA = val & 0xFF;
	}

	@Override
	public void B(int val) {
		lv.pk.rB = val & 0xFF;
	}

	@Override
	public void C(int val) {
		lv.pk.rC = val & 0xFF;
	}

	@Override
	public void D(int val) {
		lv.pk.rD = val & 0xFF;
	}

	@Override
	public void E(int val) {
		lv.pk.rE = val & 0xFF;
	}

	@Override
	public void H(int val) {
		lv.pk.rH = val & 0xFF;
	}

	@Override
	public void L(int val) {
		lv.pk.rL = val & 0xFF;
	}

	@Override
	public void F(int val) {
		lv.pk.rF = val & 0xFF;
	}

	@Override
	public void PC(int val) {
		lv.pk.rPC = val & 0xFFFF;
	}

	@Override
	public void SP(int val) {
		lv.pk.rSP = val & 0xFFFF;
	}

	// -----------------------------------------------------------------------------
	@Override
	public void STEP1() {
		lv.pk.eval(1);
	}

	// -----------------------------------------------------------------------------
	@Override
	public int MEM(int addr) {
		return lv.pk.doRead(addr);
	}

	@Override
	public void MEM(int addr, int val) {
		lv.pk.doWrite(addr, val);
	}

	@Override
	public int IO(int addr) {
		return lv.pk.doInput(addr);
	}

	@Override
	public void IO(int addr, int val) {
		lv.pk.doOutput(addr, val);
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
		return lv.pk.cpuHaltState;
	}

	@Override
	public int REASON() {
		return lv.pk.cpuHaltReason;
	}

	// -----------------------------------------------------------------------------
}
