package com.iteye.weimingtom.jpegdecoder;

import java.io.*;

public class JPEGDecoder {
	private int height;
	private int nComp;
	private int[] qTab[] = new int[10][];
	private int[] dcTab[] = new int[10][];
	private int[] acTab[] = new int[10][];
	private int nBlock[] = new int[10];
	private int YH;
	private int YV;
	private int Xsize;
	private int Ysize;
	private int marker;
	private int marker_index = 0;
	private int Ri = 0;
	private int DU[][][] = new int[10][4][64];
	private int x = 0;
	private int y = 0;
	private int num = 0;
	private int yp = 0;
	private int IDCT_Source[] = new int[64];

	private static final int MAX_HUFFMAN_SUBTREE = 50;
	public int[][][] HuffTab = new int[4][2][MAX_HUFFMAN_SUBTREE * 256];

	private FrameHeader FH = new FrameHeader();
	private ScanHeader SH = new ScanHeader();
	private QuantizationTable QT = new QuantizationTable();
	private HuffmanTable HT = new HuffmanTable();

	public int progress() {
		if (height == 0) {
			return 0;
		}
		if (yp > height) {
			return 100;
		}
		return yp * 100 / height;
	}

	private int HuffmanValue(int table[], int temp[], int index[],
			InputStream in) throws Exception {
		int code;
		int input;
		int mask = 0xFFFF;
		if (index[0] < 8) {
			temp[0] <<= 8;
			input = JPEGUtils.get8(in);
			if (input == 0xFF) {
				marker = JPEGUtils.get8(in);
				if (marker != 0) {
					marker_index = 9;
				}
			}
			temp[0] |= input;
		} else {
			index[0] -= 8;
		}
		code = table[temp[0] >> index[0]];
		if ((code & JPEGUtils.MSB) != 0) {
			if (marker_index != 0) {
				marker_index = 0;
				return 0xFF00 | marker;
			}
			temp[0] &= (mask >> (16 - index[0]));
			temp[0] <<= 8;
			input = JPEGUtils.get8(in);
			if (input == 0xFF) {
				marker = JPEGUtils.get8(in);
				if (marker != 0) {
					marker_index = 9;
				}
			}
			temp[0] |= input;
			code = table[(code & 0xFF) * 256 + (temp[0] >> index[0])];
			index[0] += 8;
		}
		index[0] += 8 - (code >> 8);
		if (index[0] < 0) {
			JPEGUtils.error("index=" + index[0] + " temp=" + temp[0] + " code="
					+ code + " in HuffmanValue()");
		}
		if (index[0] < marker_index) {
			marker_index = 0;
			return 0xFF00 | marker;
		}
		temp[0] &= (mask >> (16 - index[0]));
		return code & 0xFF;
	}

	private int getn(InputStream in, int n, int temp[], int index[])
			throws Exception {
		int result;
		int one = 1;
		int n_one = -1;
		int mask = 0xFFFF;
		int input;
		if (n == 0) {
			return 0;
		}
		index[0] -= n;
		if (index[0] >= 0) {
			if (index[0] < marker_index) {
				marker_index = 0;
				return (0xFF00 | marker) << 8;
			}
			result = temp[0] >> index[0];
			temp[0] &= (mask >> (16 - index[0]));
		} else {
			temp[0] <<= 8;
			input = JPEGUtils.get8(in);
			if (input == 0xFF) {
				marker = JPEGUtils.get8(in);
				if (marker != 0) {
					marker_index = 9;
				}
			}
			temp[0] |= input;
			index[0] += 8;
			if (index[0] < 0) {
				if (marker_index != 0) {
					marker_index = 0;
					return (0xFF00 | marker) << 8;
				}
				temp[0] <<= 8;
				input = JPEGUtils.get8(in);
				if (input == 0xFF) {
					marker = JPEGUtils.get8(in);
					if (marker != 0) {
						marker_index = 9;
					}
				}
				temp[0] |= input;
				index[0] += 8;
			}
			if (index[0] < 0) {
				JPEGUtils.error("index=" + index[0] + " in getn()");
			}
			if (index[0] < marker_index) {
				marker_index = 0;
				return (0xFF00 | marker) << 8;
			}
			result = temp[0] >> index[0];
			temp[0] &= (mask >> (16 - index[0]));
		}
		if (result < (one << (n - 1))) {
			result += (n_one << n) + 1;
		}
		return result;
	}

	private void output(PixelArray out) {
		int temp_x;
		int temp_8y;
		int temp;
		int k = 0;
		int[] DU10;
		int[] DU20;
		DU10 = DU[1][0];
		DU20 = DU[2][0];
		num++;
		for (int i = 0; i < YV; i++) {
			for (int j = 0; j < YH; j++) {
				temp_8y = i * 32;
				temp_x = temp = j * 4;
				for (int l = 0; l < 64; l++) {
					if (x < Xsize && y < Ysize) {
						out.setPixel(x, y, JPEGUtils.YUV_to_BGR(
								DU[0][k][l] + 128, DU10[temp_8y + temp_x],
								DU20[temp_8y + temp_x]));
					}
					x++;
					if ((x % YH) == 0)
						temp_x++;
					if ((x % 8) == 0) {
						y++;
						x -= 8;
						temp_x = temp;
						if ((y % YV) == 0)
							temp_8y += 8;
					}
				}
				k++;
				x += 8;
				y -= 8;
			}
			x -= YH * 8;
			y += 8;
		}
		x += YH * 8;
		y -= YV * 8;
		if (x >= Xsize) {
			y += YV * 8;
			x = 0;
		}
		yp = y;
	}

	private int decode_MCU(InputStream in, int PrevDC[], int temp[],
			int index[]) throws Exception {
		int value;
		int[] actab;
		int[] dctab;
		int[] qtab;
		int Cs;
		for (Cs = 0; Cs < nComp; Cs++) {
			qtab = qTab[Cs];
			actab = acTab[Cs];
			dctab = dcTab[Cs];
			for (int i = 0; i < nBlock[Cs]; i++) {
				for (int k = 0; k < IDCT_Source.length; k++) {
					IDCT_Source[k] = 0;
				}
				value = HuffmanValue(dctab, temp, index, in);
				if (value >= 0xFF00)
					return value;
				PrevDC[Cs] = IDCT_Source[0] = PrevDC[Cs]
						+ getn(in, value, temp, index);
				IDCT_Source[0] *= qtab[0];
				for (int j = 1; j < 64; j++) {
					value = HuffmanValue(actab, temp, index, in);
					if (value >= 0xFF00) {
						return value;
					}
					j += (value >> 4);
					if ((value & 0x0F) == 0) {
						if ((value >> 4) == 0) {
							break;
						}
					} else {
						IDCT_Source[JPEGUtils.IDCT_P[j]] = getn(in,
								value & 0x0F, temp, index) * qtab[j];
					}
				}
				ScaleIDCT(DU[Cs][i]);
			}
		}
		return 0;
	}

	private void ScaleIDCT(int matrix[]) {
		int p[][] = new int[8][8];
		int t0, t1, t2, t3, i;
		int src0, src1, src2, src3, src4, src5, src6, src7;
		int det0, det1, det2, det3, det4, det5, det6, det7;
		int mindex = 0;
		for (i = 0; i < 8; i++) {
			src0 = IDCT_Source[0 * 8 + i];
			src1 = IDCT_Source[1 * 8 + i];
			src2 = IDCT_Source[2 * 8 + i] - IDCT_Source[3 * 8 + i];
			src3 = IDCT_Source[3 * 8 + i] + IDCT_Source[2 * 8 + i];
			src4 = IDCT_Source[4 * 8 + i] - IDCT_Source[7 * 8 + i];
			src6 = IDCT_Source[5 * 8 + i] - IDCT_Source[6 * 8 + i];
			t0 = IDCT_Source[5 * 8 + i] + IDCT_Source[6 * 8 + i];
			t1 = IDCT_Source[4 * 8 + i] + IDCT_Source[7 * 8 + i];
			src5 = t0 - t1;
			src7 = t0 + t1;
			det4 = -src4 * 480 - src6 * 192;
			det5 = src5 * 384;
			det6 = src6 * 480 - src4 * 192;
			det7 = src7 * 256;
			t0 = src0 * 256;
			t1 = src1 * 256;
			t2 = src2 * 384;
			t3 = src3 * 256;
			det3 = t3;
			det0 = t0 + t1;
			det1 = t0 - t1;
			det2 = t2 - t3;
			src0 = det0 + det3;
			src1 = det1 + det2;
			src2 = det1 - det2;
			src3 = det0 - det3;
			src4 = det6 - det4 - det5 - det7;
			src5 = det5 - det6 + det7;
			src6 = det6 - det7;
			src7 = det7;
			p[0][i] = (src0 + src7 + (1 << 12)) >> 13;
			p[1][i] = (src1 + src6 + (1 << 12)) >> 13;
			p[2][i] = (src2 + src5 + (1 << 12)) >> 13;
			p[3][i] = (src3 + src4 + (1 << 12)) >> 13;
			p[4][i] = (src3 - src4 + (1 << 12)) >> 13;
			p[5][i] = (src2 - src5 + (1 << 12)) >> 13;
			p[6][i] = (src1 - src6 + (1 << 12)) >> 13;
			p[7][i] = (src0 - src7 + (1 << 12)) >> 13;
		}
		for (i = 0; i < 8; i++) {
			src0 = p[i][0];
			src1 = p[i][1];
			src2 = p[i][2] - p[i][3];
			src3 = p[i][3] + p[i][2];
			src4 = p[i][4] - p[i][7];
			src6 = p[i][5] - p[i][6];
			t0 = p[i][5] + p[i][6];
			t1 = p[i][4] + p[i][7];
			src5 = t0 - t1;
			src7 = t0 + t1;
			det4 = -src4 * 480 - src6 * 192;
			det5 = src5 * 384;
			det6 = src6 * 480 - src4 * 192;
			det7 = src7 * 256;
			t0 = src0 * 256;
			t1 = src1 * 256;
			t2 = src2 * 384;
			t3 = src3 * 256;
			det3 = t3;
			det0 = t0 + t1;
			det1 = t0 - t1;
			det2 = t2 - t3;
			src0 = det0 + det3;
			src1 = det1 + det2;
			src2 = det1 - det2;
			src3 = det0 - det3;
			src4 = det6 - det4 - det5 - det7;
			src5 = det5 - det6 + det7;
			src6 = det6 - det7;
			src7 = det7;
			matrix[mindex++] = (src0 + src7 + (1 << 12)) >> 13;
			matrix[mindex++] = (src1 + src6 + (1 << 12)) >> 13;
			matrix[mindex++] = (src2 + src5 + (1 << 12)) >> 13;
			matrix[mindex++] = (src3 + src4 + (1 << 12)) >> 13;
			matrix[mindex++] = (src3 - src4 + (1 << 12)) >> 13;
			matrix[mindex++] = (src2 - src5 + (1 << 12)) >> 13;
			matrix[mindex++] = (src1 - src6 + (1 << 12)) >> 13;
			matrix[mindex++] = (src0 - src7 + (1 << 12)) >> 13;
		}
	}

	public void decode(InputStream in, PixelArray out) throws Exception {
		int current, m, i, scan_num = 0, RST_num;
		int PRED[] = new int[10];
		if (in == null) {
			return;
		}
		x = 0;
		y = 0;
		yp = 0;
		num = 0;
		current = JPEGUtils.get16(in);
		if (current != 0xFFD8) {
			JPEGUtils.error("Not a JPEG file");
			return;
		}
		current = JPEGUtils.get16(in);
		while (current >> 4 != 0x0FFC) {
			switch (current) {
			case 0xFFC4:
				HT.get(in, this.HuffTab);
				break;

			case 0xFFCC:
				JPEGUtils
						.error("Program doesn't support arithmetic coding. (format error)");
				return;

			case 0xFFDB:
				QT.get(in);
				break;

			case 0xFFDD:
				Ri = JPEGUtils.readNumber(in);
				break;

			case 0xFFE0:
			case 0xFFE1:
			case 0xFFE2:
			case 0xFFE3:
			case 0xFFE4:
			case 0xFFE5:
			case 0xFFE6:
			case 0xFFE7:
			case 0xFFE8:
			case 0xFFE9:
			case 0xFFEA:
			case 0xFFEB:
			case 0xFFEC:
			case 0xFFED:
			case 0xFFEE:
			case 0xFFEF:
				JPEGUtils.readApp(in);
				break;

			case 0xFFFE:
				JPEGUtils.readComment(in);
				break;

			default:
				if (current >> 8 != 0xFF) {
					JPEGUtils.error("ERROR: format error! (decode)");
				}
			}
			current = JPEGUtils.get16(in);
		}
		if (current < 0xFFC0 || current > 0xFFC7) {
			JPEGUtils.error("ERROR: could not handle arithmetic code!");
		}
		height = FH.get(in, current);
		current = JPEGUtils.get16(in);
		out.setSize(FH.X, FH.Y);
		do {
			while (current != 0x0FFDA) {
				switch (current) {
				case 0xFFC4:
					HT.get(in, this.HuffTab);
					break;

				case 0xFFCC:
					JPEGUtils
							.error("Program doesn't support arithmetic coding. (format error)");

				case 0xFFDB:
					QT.get(in);
					break;

				case 0xFFDD:
					Ri = JPEGUtils.readNumber(in);
					break;

				case 0xFFE0:
				case 0xFFE1:
				case 0xFFE2:
				case 0xFFE3:
				case 0xFFE4:
				case 0xFFE5:
				case 0xFFE6:
				case 0xFFE7:
				case 0xFFE8:
				case 0xFFE9:
				case 0xFFEA:
				case 0xFFEB:
				case 0xFFEC:
				case 0xFFED:
				case 0xFFEE:
				case 0xFFEF:
					JPEGUtils.readApp(in);
					break;

				case 0xFFFE:
					JPEGUtils.readComment(in);
					break;

				default:
					if (current >> 8 != 0xFF) {
						JPEGUtils.error("ERROR: format error! (Parser.decode)");
					}
				}
				current = JPEGUtils.get16(in);
			}
			SH.get(in);
			nComp = (int) SH.Ns;
			for (i = 0; i < nComp; i++) {
				int CompN = SH.Comp[i].Cs;
				qTab[i] = QT.Q[FH.Comp[CompN].Tq];
				nBlock[i] = FH.Comp[CompN].V * FH.Comp[CompN].H;
				dcTab[i] = HuffTab[SH.Comp[i].Td][0];
				acTab[i] = HuffTab[SH.Comp[i].Ta][1];
			}
			YH = FH.Comp[1].H;
			YV = FH.Comp[1].V;
			Xsize = FH.X;
			Ysize = FH.Y;
			scan_num++;
			m = 0;
			for (RST_num = 0;; RST_num++) {
				int MCU_num;
				int temp[] = new int[1];
				int index[] = new int[1];
				temp[0] = 0;
				index[0] = 0;
				for (i = 0; i < 10; i++) {
					PRED[i] = 0;
				}
				if (Ri == 0) {
					current = decode_MCU(in, PRED, temp, index);
					while (current == 0) {
						m++;
						output(out);
						current = decode_MCU(in, PRED, temp, index);
					}
					break;
				}
				for (MCU_num = 0; MCU_num < Ri; MCU_num++) {
					current = decode_MCU(in, PRED, temp, index);
					output(out);
					if (current != 0) {
						break;
					}
				}
				if (current == 0) {
					if (marker_index != 0) {
						current = (0xFF00 | marker);
						marker_index = 0;
					} else {
						current = JPEGUtils.get16(in);
					}
				}
				if (current >= 0xFFD0 && current <= 0xFFD7) {

				} else {
					break;
				}
			}
			if (current == 0xFFDC && scan_num == 1) {
				JPEGUtils.readNumber(in);
				current = JPEGUtils.get16(in);
			}
		} while (current != 0xFFD9);
	}
}
