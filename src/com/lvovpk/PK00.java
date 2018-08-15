package com.lvovpk;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * LVOV Software Abstraction
 */
abstract class PK00 extends Canvas implements KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5674032851714416043L;
	final static int mode_first = 1;
	final static int mode_3x2_solid = 1;
	final static int mode_3x2_interlaced = 2;
	final static int mode_1x1_solid = 3;
	final static int mode_last = 3;

	Lvov pk;

	// -----------------------------------------------------------------------------
	// C o m p o n e n t I m p l e m e n t a t i o n
	// -----------------------------------------------------------------------------
	PK00() {
		this(mode_1x1_solid);
	}

	PK00(int mode) {
		super();
		pk = new Lvov();
		render_as(mode);
		addKeyListener(this);
	}

	// -----------------------------------------------------------------------------
	@Override
	public Dimension getPreferredSize() {
		if (render_3x2)
			return new Dimension(3 * 256, 2 * 256);
		else
			return new Dimension(256, 256);
	}

	// -----------------------------------------------------------------------------
	@Override
	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void paint(Graphics g) {
		if (v_img == null)
			super.paint(g);
		else
			g.drawImage(v_img, 0, 0, this);
	}

	// -----------------------------------------------------------------------------
	// K e y b o a r d M a n i p u l a t i o n s
	// a n d S u p p l e m e n t a r y S t u f f
	// -----------------------------------------------------------------------------
	private Hashtable<Integer, Integer> keyMask = new Hashtable<Integer, Integer>();

	// -----------------------------------------------------------------------------
	public void clr_kb(int vk) {
		keyMask.remove(new Integer(vk));
	}

	public void set_kb(int vk, int mask) {
		keyMask.put(new Integer(vk), new Integer(mask));
	}

	// -----------------------------------------------------------------------------
	@Override
	public void keyPressed(KeyEvent e) {
		Integer mask = (Integer) keyMask.get(new Integer(e.getKeyCode()));
		if (mask != null)
			kbd(mask.intValue(), true);
	}

	// -----------------------------------------------------------------------------
	@Override
	public void keyReleased(KeyEvent e) {
		Integer mask = (Integer) keyMask.get(new Integer(e.getKeyCode()));
		if (mask != null)
			kbd(mask.intValue(), false);
	}

	// -----------------------------------------------------------------------------
	@Override
	public void keyTyped(KeyEvent e) {
	}

	// -----------------------------------------------------------------------------
	// K e y b o a r d H a n d l i n g
	// -----------------------------------------------------------------------------
	private void kbd(int mask, boolean press) {
		int bcol = (mask >> 12) & 0x0F;
		int brow = (mask >> 8) & 0x0F;
		int ecol = (mask >> 4) & 0x0F;
		int erow = (mask >> 0) & 0x0F;

		if (brow < 8 && bcol < 8)
			if (press)
				pk.kbd_base[bcol] |= (1 << brow);
			else
				pk.kbd_base[bcol] &= ~(1 << brow);

		if (erow < 8 && ecol < 4)
			if (press)
				pk.kbd_ext[ecol] |= (1 << erow);
			else
				pk.kbd_ext[ecol] &= ~(1 << erow);
	}

	// -----------------------------------------------------------------------------
	// P a l e t t e M a n i p u l a t i o n s
	// a n d S u p p l e m e n t a r y S t u f f
	// -----------------------------------------------------------------------------
	// BLACK=0, BLUE=1, GREEN=2, CYAN=3, RED=4, MAGENTA=5, YELLOW=6, WHITE=7
	private static byte
		b[] = { (byte)0x00, (byte)0xC0, (byte)0x00, (byte)0xC0,
				(byte)0x00, (byte)0xC0, (byte)0x00, (byte)0xFF },
		g[] = { (byte)0x00, (byte)0x00, (byte)0xC0, (byte)0xC0,
				(byte)0x00, (byte)0x00, (byte)0xC0, (byte)0xFF },
		r[] = { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
				(byte)0xC0, (byte)0xC0, (byte)0xC0, (byte)0xFF };

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	private boolean render_3x2, render_interlaced;
	private Image v_img;
	private MemoryImageSource v_src;
	private byte[] v_mem = new byte[256 * 3 * 256 * 2]; // 768x512 or 256x256
	private byte[][] v_pal = new byte[256][4];

	// -----------------------------------------------------------------------------
	private void make_v_pal() {
		int i, j, pal = pk.ports[0xC1];
		for (i = 0; i < 256; i++)
			for (j = 0; j < 4; j++)
				v_pal[i][3 - j] = Lvov.compute_color_index(pal, (i >> j & 1) | (i >> j + 3 & 2));
	}

	// -----------------------------------------------------------------------------
	private void update_v_line(int line) {
		if (render_3x2) {
			update_v_line_3x2(line, true);
			update_v_line_3x2(line, false);
		} else
			update_v_line_1x1(line);
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	private void update_v_line_1x1(int line) {
		int i, j, p;
		byte[] c;
		for (i = line, j = line + 64, p = i << 2; i < j; i++) {
			c = v_pal[pk.video[i]];
			v_mem[p++] = c[0];
			v_mem[p++] = c[1];
			v_mem[p++] = c[2];
			v_mem[p++] = c[3];
		}
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	private void update_v_line_3x2(int line, boolean first) {
		int i = line, j = line + 64, p = i * 4 * 3 * 2 + (first ? 0 : 3 * 256);
		byte c[];

		if (render_interlaced && first) {
			for (i = p, j = p + 64 * 4 * 3; i < j; i++)
				v_mem[p++] = 0;
		} else {
			for (; i < j; i++) {
				c = v_pal[pk.video[i]];
				v_mem[p++] = c[0];
				v_mem[p++] = c[0];
				v_mem[p++] = c[0];

				v_mem[p++] = c[1];
				v_mem[p++] = c[1];
				v_mem[p++] = c[1];

				v_mem[p++] = c[2];
				v_mem[p++] = c[2];
				v_mem[p++] = c[2];

				v_mem[p++] = c[3];
				v_mem[p++] = c[3];
				v_mem[p++] = c[3];
			}
		}
	}

	// -----------------------------------------------------------------------------
	// V i d e o H a n d l i n g
	// -----------------------------------------------------------------------------
	void update_image() {
		int i, j, p;
		if (pk.dirty == null) {
			pk.dirty = new boolean[256];
			make_v_pal();
			for (p = i = 0; i < 256; i++, p += 64)
				update_v_line(p);
			v_src.newPixels();
		} else {
			for (p = i = 0; i < 256; i++, p += 64) {
				for (j = i; j < 256 && pk.dirty[j]; j++, p += 64) {
					update_v_line(p);
					pk.dirty[j] = false;
				}
				if (j > i) {
					if (render_3x2)
						v_src.newPixels(0, 2 * i, 3 * 256, 2 * (j - i));
					else
						v_src.newPixels(0, i, 256, j - i);
					i = j;
				}
			}
		}
	}

	// -----------------------------------------------------------------------------
	void render_as(int mode) {
		pk.dirty = null;
		if (mode == mode_3x2_solid || mode == mode_3x2_interlaced) {
			render_3x2 = true;
			if (mode == mode_3x2_interlaced)
				render_interlaced = true;
			v_src = new MemoryImageSource(256 * 3, 256 * 2, new IndexColorModel(8, 8, r, g, b), v_mem, 0, 256 * 3);
		} else {
			render_3x2 = render_interlaced = false;
			v_src = new MemoryImageSource(256, 256, new IndexColorModel(8, 8, r, g, b), v_mem, 0, 256);
		}
		v_src.setAnimated(true);
		v_src.setFullBufferUpdates(false);
		v_img = createImage(v_src);
	}

	// -----------------------------------------------------------------------------
	// G U E S T M a n i p u l a t i o n s
	// -----------------------------------------------------------------------------
	void emulate(int ticks) {
		pk.eval(ticks);
	}

	// -----------------------------------------------------------------------------
	void snapshot(OutputStream To) throws Exception {
		if (render_3x2)
			Bitmap.save4(To, v_mem, 3 * 256, 2 * 256, new byte[][] { r, g, b }, 8);
		else
			Bitmap.save4(To, v_mem, 256, 256, new byte[][] { r, g, b }, 8);
	}

	// -----------------------------------------------------------------------------
}
