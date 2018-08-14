package com.lvovpk;

/**
	Debuggable Target
*/
abstract class Debugger {

	// -----------------------------------------------------------------------------
	public abstract int A();
	public abstract void A(int val);

	public abstract int B();
	public abstract void B(int val);

	public abstract int C();
	public abstract void C(int val);

	public abstract int D();
	public abstract void D(int val);

	public abstract int E();
	public abstract void E(int val);

	public abstract int H();
	public abstract void H(int val);

	public abstract int L();
	public abstract void L(int val);

	public abstract int F();
	public abstract void F(int val);

	public abstract int PC();
	public abstract void PC(int val);

	public abstract int SP();
	public abstract void SP(int val);

	// -----------------------------------------------------------------------------
	public final int BC() {
		return B() * 256 + C();
	}

	public final int DE() {
		return D() * 256 + E();
	}

	public final int HL() {
		return H() * 256 + L();
	}

	public final int PSW() {
		return A() * 256 + F();
	}

	public final void BC(int val) {
		B(val / 256);
		C(val);
	}

	public final void DE(int val) {
		D(val / 256);
		E(val);
	}

	public final void HL(int val) {
		H(val / 256);
		L(val);
	}

	public final void PSW(int val) {
		A(val / 256);
		F(val);
	}

	// -----------------------------------------------------------------------------
	public abstract int CLK();
	public abstract void CLK(int val);

	public abstract int MEM(int addr);
	public abstract void MEM(int addr, int val);

	public abstract int IO(int addr);
	public abstract void IO(int addr, int val);

	public final int MEM2(int addr) {
		return MEM(addr + 1) * 256 + MEM(addr);
	}

	public final void MEM2(int addr, int val) {
		MEM(addr + 1, val / 256);
		MEM(addr, val);
	}

	// -----------------------------------------------------------------------------
	public abstract void STEP1();

	public abstract boolean HALTED();

	public abstract int REASON();

	// -----------------------------------------------------------------------------
}
