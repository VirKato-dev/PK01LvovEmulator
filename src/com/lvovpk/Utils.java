package com.lvovpk;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Supplementary Tools
 */
public class Utils {

	// -----------------------------------------------------------------------------
	// S o r t i n g
	// -----------------------------------------------------------------------------
	public static String[] sort(Set<String> e, boolean ascend) {
		Vector<String> vct = new Vector<String>();
		int i;

		for (String key : e) { // sort in ascending order
			for (i = 0; i < vct.size(); i++)
				if (key.compareTo(vct.elementAt(i).toString()) < 0)
					break;
			vct.insertElementAt(key, i);
		}

		String Result[] = new String[vct.size()]; // then prepare data for client

		if (ascend)
			for (i = 0; i < vct.size(); i++) // ok, leave intact
				Result[i] = vct.elementAt(i).toString();
		else
			for (i = 0; i < vct.size(); i++) // hmm, needs to reverse order of data
				Result[vct.size() - i - 1] = vct.elementAt(i).toString();

		return Result;
	}

	// -----------------------------------------------------------------------------
	// F o r m a t t i n g
	// -----------------------------------------------------------------------------
	public static String padRight(String s, int w) {
		return padRight(s, w, " ");
	}

	public static String padRight(String s, int w, String pad) {
		while (s.length() < w)
			s = s + pad;
		return s.substring(0, w);
	}

	// -----------------------------------------------------------------------------
	public static String padLeft(String s, int w) {
		return padLeft(s, w, " ");
	}

	public static String padLeft(String s, int w, String pad) {
		while (s.length() < w)
			s = pad + s;
		return s.substring(s.length() - w, s.length());
	}

	// -----------------------------------------------------------------------------
	public static String HEX(int dat) {
		return "0x" + Integer.toHexString(dat).toUpperCase();
	}

	// -----------------------------------------------------------------------------
	// P a r s i n g
	// -----------------------------------------------------------------------------
	public static int spanAllow(String str, int start, String range) {
		for (int i = start;; i++) {
			if (i >= str.length())
				return i;
			if (range.indexOf(str.charAt(i)) < 0)
				return i;
		}
	}

	// -----------------------------------------------------------------------------
	public static int spanDeny(String str, int start, String range) {
		for (int i = start;; i++) {
			if (i >= str.length())
				return i;
			if (range.indexOf(str.charAt(i)) >= 0)
				return i;
		}
	}

	// -----------------------------------------------------------------------------
	// I / O S t u f f
	// -----------------------------------------------------------------------------
	// Dump
	// -----------------------------------------------------------------------------
	public static void dumpByte(OutputStream to, int dat) throws IOException {
		to.write(dat);
	}

	public static void dumpWord(OutputStream to, int dat) throws IOException {
		dumpByte(to, dat);
		dumpByte(to, dat >> 8);
	}

	public static void dumpDWord(OutputStream to, int dat) throws IOException {
		dumpWord(to, dat);
		dumpWord(to, dat >> 16);
	}

	// -----------------------------------------------------------------------------
	public static void dumpBytes(OutputStream to, String dat) throws IOException {
		dumpBytes(to, dat, 0, dat.length());
	}

	public static void dumpBytes(OutputStream to, String dat, int off, int len) throws IOException {
		for (int i = 0; i < len; dumpByte(to, dat.charAt(off + i)), i++)
			;
	}

	// -----------------------------------------------------------------------------
	public static void dumpBytes(OutputStream to, int dat[]) throws IOException {
		dumpBytes(to, dat, 0, dat.length);
	}

	public static void dumpBytes(OutputStream to, int dat[], int off, int len) throws IOException {
		for (int i = 0; i < len; dumpByte(to, dat[off + i]), i++)
			;
	}

	// -----------------------------------------------------------------------------
	public static void dumpBytes(OutputStream to, short dat[]) throws IOException {
		dumpBytes(to, dat, 0, dat.length);
	}

	public static void dumpBytes(OutputStream to, short dat[], int off, int len) throws IOException {
		for (int i = 0; i < len; dumpByte(to, dat[off + i]), i++)
			;
	}

	// -----------------------------------------------------------------------------
	public static void dumpBytes(OutputStream to, byte dat[]) throws IOException {
		dumpBytes(to, dat, 0, dat.length);
	}

	public static void dumpBytes(OutputStream to, byte dat[], int off, int len) throws IOException {
		for (int i = 0; i < len; dumpByte(to, dat[off + i]), i++)
			;
	}

	// -----------------------------------------------------------------------------
	// Restore
	// -----------------------------------------------------------------------------
	public static int restoreByte(InputStream from) throws IOException {
		int dat = from.read();
		if (dat == -1)
			throw new EOFException("EOF");
		return dat;
	}

	public static int restoreWord(InputStream from) throws IOException {
		int dat = restoreByte(from);
		dat += restoreByte(from) << 8;
		return dat;
	}

	public static int restoreDWord(InputStream from) throws IOException {
		int dat = restoreWord(from);
		dat += restoreWord(from) << 16;
		return dat;
	}

	// -----------------------------------------------------------------------------
	public static void restoreBytes(InputStream from, int dat[]) throws IOException {
		restoreBytes(from, dat, 0, dat.length);
	}

	public static void restoreBytes(InputStream from, int dat[], int off, int len) throws IOException {
		for (int i = 0; i < len; dat[off + i] = restoreByte(from), i++)
			;
	}

	// -----------------------------------------------------------------------------
	public static void restoreBytes(InputStream from, short dat[]) throws IOException {
		restoreBytes(from, dat, 0, dat.length);
	}

	public static void restoreBytes(InputStream from, short dat[], int off, int len) throws IOException {
		for (int i = 0; i < len; dat[off + i] = (short) restoreByte(from), i++)
			;
	}

	// -----------------------------------------------------------------------------
	public static void restoreBytes(InputStream from, byte dat[]) throws IOException {
		restoreBytes(from, dat, 0, dat.length);
	}

	public static void restoreBytes(InputStream from, byte dat[], int off, int len) throws IOException {
		for (int i = 0; i < len; dat[off + i] = (byte) restoreByte(from), i++)
			;
	}

	// -----------------------------------------------------------------------------
	public static InputStream ZIP(String Name, InputStream Stream) throws IOException {
		Stream = new BufferedInputStream(Stream, 1024);
		if (Name.endsWith(".gz")) // if file is packed - unpack it
			Stream = new GZIPInputStream(Stream);
		return Stream;
	}

	public static OutputStream ZIP(String Name, OutputStream Stream) throws IOException {
		Stream = new BufferedOutputStream(Stream, 1024);
		if (Name.endsWith(".gz")) // if file is packable - pack it
			Stream = new GZIPOutputStream(Stream);
		return Stream;
	}

	// -----------------------------------------------------------------------------
	public static String useFileAsURL(JFrame wnd, String title, String... masks) {
		JFileChooser dlg = new JFileChooser();
		dlg.setDialogTitle(title);
		dlg.setAcceptAllFileFilterUsed(false);
		for (String mask : masks) {
			String[] maskArr = mask.split("#");
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					(maskArr.length > 1 ? maskArr[0] : "*." + maskArr[0]), maskArr[maskArr.length - 1].split(";"));
			dlg.addChoosableFileFilter(filter);
		}

		int returnValue = dlg.showOpenDialog(wnd);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			return dlg.getSelectedFile().getPath();
		}
		return null;
	}

	public static String mkFile(JFrame wnd, String title, String... masks) {
		JFileChooser dlg = new JFileChooser();
		dlg.setDialogTitle(title);
		dlg.setAcceptAllFileFilterUsed(false);
		for (String mask : masks) {
			String[] maskArr = mask.split("#");
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					(maskArr.length > 1 ? maskArr[0] : "*." + maskArr[0]), maskArr[maskArr.length - 1].split(";"));
			dlg.addChoosableFileFilter(filter);
		}

		int returnValue = dlg.showSaveDialog(wnd);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String filePath = dlg.getSelectedFile().getPath();
			String[] extensions = ((FileNameExtensionFilter)dlg.getFileFilter()).getExtensions();
			for (String extension : extensions) {
				if (getFileExtension(filePath).equalsIgnoreCase(extension)) {
					return filePath;
				}
			}
			return dlg.getSelectedFile().getPath() + "." + extensions[0];
		}
		return null;
	}
	
	public static String getFileExtension(String fileName) {
		int lastDotIndex = fileName.lastIndexOf(".");
		if (lastDotIndex == -1 || lastDotIndex == (fileName.length() - 1)) {
			return "";
		}
		return fileName.substring(lastDotIndex + 1);
	}

	// -----------------------------------------------------------------------------
}
