package com.lvovpk;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Sound Playing Primitives
 */
public class Sound {

	private static SourceDataLine line = null;

	// -----------------------------------------------------------------------------
	public static void init() throws LineUnavailableException {
		AudioFormat format = new AudioFormat(8000f, 8, 1, false, false);
		line = AudioSystem.getSourceDataLine(format);
		line.open(format);
	}

	public static void done() {
		if (line == null || !line.isOpen())
			return;
		line.stop();
		line.close();
	}

	// -----------------------------------------------------------------------------
	private static void enqueue(byte[] data) {
		// line.stop();
		line.write(data, 0, data.length);
		if (!line.isRunning()) {
			line.start();
		}
	}

	// -----------------------------------------------------------------------------
	public static void play(int mode, int rtime, byte volDn, byte volUp, long[] etime) {
		if (line == null || !line.isOpen())
			return;
		if (etime == null || etime.length - 1 == 0) {
			line.stop();
			return;
		}
		if (mode == 1)
			enqueue(play1(rtime, volDn, volUp, etime));
		else
			enqueue(play2(rtime, volDn, volUp, etime));
	}

	// -----------------------------------------------------------------------------
	private static byte[] play1(int rtime, byte volDn, byte volUp, long[] etime) {
		int pad = rtime * 8000 / 1000; // num of speaks per supplied data
		byte[] data = new byte[pad];

		long et = 0;
		for (int i = 0, j = 0; i < pad; i++) {
			et += 1000000 / 8000; // calculate emulator time
			data[i] = volDn;
			if (etime == null)
				continue;
			if (j >= etime.length - 1)
				continue;
			if (et > etime[j]) {
				data[i] = volUp;
				j++;
			}
		}
		return data;
	}

	// -----------------------------------------------------------------------------
	private static byte[] play2(int rtime, byte volDn, byte volUp, long[] etime) {
		int pad = rtime * 8000 / 1000; // num of speaks per supplied data
		byte[] data = new byte[pad];
		int times, hits;

		if (etime == null)
			times = 0;
		else
			times = etime.length - 1;
		int i = 0, j = 0;
		long prev = 0;
		do {
			if (j >= times)
				while (i < pad)
					data[i++] = volDn;
			else {
				hits = (int) ((etime[j] - prev) * 8 / 1000) + 1;
				for (int k = 1; k <= hits && i < pad; k++, i++)
					data[i] = (byte) (k * (volUp - volDn) / hits + volDn);
				prev = etime[j++];
			}
		} while (i < pad);
		return data;
	}

	// -----------------------------------------------------------------------------
}
