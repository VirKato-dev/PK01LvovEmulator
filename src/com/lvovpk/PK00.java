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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LVOV Software Abstraction
 */
abstract class PK00 extends Canvas implements KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5674032851714416043L;
	final static int MODE_FIRST = 1;
	final static int MODE_LAST = 11;

	private int scaleX = 1;
	private int scaleY = 1;
	private int[][] scaleFactors = { {1, 1}, {2, 2}, {3, 2}, {5, 3}, {6, 4}, {7, 4} };
	
	Lvov pk;

	// -----------------------------------------------------------------------------
	// C o m p o n e n t I m p l e m e n t a t i o n
	// -----------------------------------------------------------------------------
	PK00() {
		this(MODE_FIRST);
	}

	PK00(int mode) {
		super();
		pk = new Lvov();
		renderAs(mode);
		addKeyListener(this);
	}

	// -----------------------------------------------------------------------------
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(scaleX * 256, scaleY * 256);
	}

	// -----------------------------------------------------------------------------
	@Override
	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void paint(Graphics g) {
		if (vImg == null)
			super.paint(g);
		else
			g.drawImage(vImg, 0, 0, this);
	}

	// -----------------------------------------------------------------------------
	// K e y b o a r d M a n i p u l a t i o n s
	// a n d S u p p l e m e n t a r y S t u f f
	// -----------------------------------------------------------------------------
	private Map<Integer, Integer> keyMask = new ConcurrentHashMap<Integer, Integer>();

	// -----------------------------------------------------------------------------
	public void clrKb(int vk) {
		keyMask.remove(new Integer(vk));
	}

	public void setKb(int vk, int mask) {
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
				pk.kbdBase[bcol] |= (1 << brow);
			else
				pk.kbdBase[bcol] &= ~(1 << brow);

		if (erow < 8 && ecol < 4)
			if (press)
				pk.kbdExt[ecol] |= (1 << erow);
			else
				pk.kbdExt[ecol] &= ~(1 << erow);
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
	private boolean renderInterlaced;
	private Image vImg;
	private MemoryImageSource vSrc;
	private byte[] vMem = new byte[256 * 7 * 256 * 4]; // up to 1792x1024
	private byte[][] vPal = new byte[256][4];

	// -----------------------------------------------------------------------------
	private void makeVPal() {
		int i, j, pal = pk.ports[0xC1];
		for (i = 0; i < 256; i++)
			for (j = 0; j < 4; j++)
				vPal[i][3 - j] = Lvov.computeColorIndex(pal, (i >> j & 1) | (i >> j + 3 & 2));
	}

	// -----------------------------------------------------------------------------
	private void updateVLine(int line) {
		for (int i = 0; i < scaleY; i++) {
			updateVLine(line, i);
		}
	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	private void updateVLine(int line, int row) {
		int i = line, j = line + 64, p = i * 4 * scaleX * scaleY + (row * scaleX * 256);
		byte[] c;
		
		if (renderInterlaced && scaleY > 1 && row >= (scaleY >> 1)) {
			for (i = p, j = p + 64 * 4 * scaleX; i < j; i++)
				vMem[p++] = 0;
		} else {
			for (; i < j; i++) {
				c = vPal[pk.video[i]];
				for (int v = 0; v < 4; v++) {
					for (int s = 0; s < scaleX; s++) {
						vMem[p++] = c[v];
					}
				}
			}
		}
	}

	// -----------------------------------------------------------------------------
	// V i d e o H a n d l i n g
	// -----------------------------------------------------------------------------
	void updateImage() {
		int i, j, p;
		if (pk.dirty == null) {
			pk.dirty = new boolean[256];
			makeVPal();
			for (p = i = 0; i < 256; i++, p += 64)
				updateVLine(p);
			vSrc.newPixels();
		} else {
			for (p = i = 0; i < 256; i++, p += 64) {
				for (j = i; j < 256 && pk.dirty[j]; j++, p += 64) {
					updateVLine(p);
					pk.dirty[j] = false;
				}
				if (j > i) {
					vSrc.newPixels(0, scaleY * i, scaleX * 256, scaleY * (j - i));
					i = j;
				}
			}
		}
	}

	// -----------------------------------------------------------------------------
	void renderAs(int mode) {
		pk.dirty = null;
		renderInterlaced = (mode > 1 && mode % 2 != 0);
		scaleX = scaleFactors[mode / 2][0];
		scaleY = scaleFactors[mode / 2][1];
		vSrc = new MemoryImageSource(256 * scaleX, 256 * scaleY, new IndexColorModel(8, 8, r, g, b), vMem, 0, 256 * scaleX);
		vSrc.setAnimated(true);
		vSrc.setFullBufferUpdates(false);
		vImg = createImage(vSrc);
	}

	// -----------------------------------------------------------------------------
	// G U E S T M a n i p u l a t i o n s
	// -----------------------------------------------------------------------------
	void emulate(int ticks) {
		pk.eval(ticks);
	}

	// -----------------------------------------------------------------------------
	void snapshot(OutputStream to) throws Exception {
		Bitmap.save4(to, vMem, scaleX * 256, scaleY * 256, new byte[][] { r, g, b }, 8);
	}

	// -----------------------------------------------------------------------------
}
