package com.iteye.weimingtom.jpegdecoder;

import java.io.IOException;
import java.io.InputStream;

public class JPEGUtils {
	public static final int MSB = 0x80000000;

	public final static int IDCT_P[] = { 0, 5, 40, 16, 45, 2, 7, 42, 21, 56, 8,
			61, 18, 47, 1, 4, 41, 23, 58, 13, 32, 24, 37, 10, 63, 17, 44, 3, 6,
			43, 20, 57, 15, 34, 29, 48, 53, 26, 39, 9, 60, 19, 46, 22, 59, 12,
			33, 31, 50, 55, 25, 36, 11, 62, 14, 35, 28, 49, 52, 27, 38, 30, 51,
			54 };

	public final static int table[] = { 0, 1, 5, 6, 14, 15, 27, 28, 2, 4, 7,
			13, 16, 26, 29, 42, 3, 8, 12, 17, 25, 30, 41, 43, 9, 11, 18, 24,
			31, 40, 44, 53, 10, 19, 23, 32, 39, 45, 52, 54, 20, 22, 33, 38, 46,
			51, 55, 60, 21, 34, 37, 47, 50, 56, 59, 61, 35, 36, 48, 49, 57, 58,
			62, 63 };

	public static void error(String message) throws Exception {
		throw new Exception(message);
	}

	public final static int get16(InputStream in) throws Exception {
		int temp;
		try {
			temp = in.read();
			temp <<= 8;
			return temp | in.read();
		} catch (IOException e) {
			error("get16() read error: " + e.toString());
			return -1;
		}
	}

	public final static int get8(InputStream in) throws Exception {
		try {
			return in.read();
		} catch (IOException e) {
			error("get8() read error: " + e.toString());
			return -1;
		}
	}

	public static int readNumber(InputStream in) throws Exception {
		int Ld;
		Ld = JPEGUtils.get16(in);
		if (Ld != 4) {
			JPEGUtils.error("ERROR: Define number format error [Ld!=4]");
		}
		return JPEGUtils.get16(in);
	}

	public static String readComment(InputStream in) throws Exception {
		int Lc;
		int count = 0;
		int i;
		StringBuffer sb = new StringBuffer();
		Lc = JPEGUtils.get16(in);
		count += 2;
		for (i = 0; count < Lc; i++) {
			sb.append((char) JPEGUtils.get8(in));
			count++;
		}
		return sb.toString();
	}

	public static int readApp(InputStream in) throws Exception {
		int Lp;
		int count = 0;
		Lp = JPEGUtils.get16(in);
		count += 2;
		while (count < Lp) {
			JPEGUtils.get8(in);
			count++;
		}
		return Lp;
	}

	public static int YUV_to_BGR(int Y, int u, int v) {
		if (Y < 0) {
			Y = 0;
		}
		int tempB;
		int tempG;
		int tempR;
		tempB = Y + ((116130 * u) >> 16);
		if (tempB < 0) {
			tempB = 0;
		} else if (tempB > 255) {
			tempB = 255;
		}
		tempG = Y - ((22554 * u + 46802 * v) >> 16);
		if (tempG < 0) {
			tempG = 0;
		} else if (tempG > 255) {
			tempG = 255;
		}
		tempR = Y + ((91881 * v) >> 16);
		if (tempR < 0) {
			tempR = 0;
		} else if (tempR > 255) {
			tempR = 255;
		}
		return 0xff000000 | ((tempR << 16) + (tempG << 8) + tempB);
	}
}
