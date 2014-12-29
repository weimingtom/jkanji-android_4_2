package com.iteye.weimingtom.jpegdecoder;

import java.io.InputStream;

public class QuantizationTable {
	private int Lq;
	private int[] Pq = new int[4];
	private int[] Tq = new int[4];
	public int[][] Q = new int[4][64];

	public QuantizationTable() {
		Tq[0] = 0;
		Tq[1] = 0;
		Tq[2] = 0;
		Tq[3] = 0;
	}

	public int get(InputStream in) throws Exception {
		int i;
		int count = 0;
		int temp;
		int t;
		Lq = JPEGUtils.get16(in);
		count += 2;
		while (count < Lq) {
			temp = JPEGUtils.get8(in);
			count++;
			t = temp & 0x0F;
			if (t > 3) {
				JPEGUtils.error("ERROR: Quantization table ID > 3");
			}
			Pq[t] = temp >> 4;
			if (Pq[t] == 0) {
				Pq[t] = 8;
			} else if (Pq[t] == 1) {
				Pq[t] = 16;
			} else {
				JPEGUtils.error("ERROR: Quantization table precision error");
			}
			Tq[t] = 1;
			if (Pq[t] == 8) {
				for (i = 0; i < 64; i++) {
					if (count > Lq) {
						JPEGUtils
								.error("ERROR: Quantization table format error");
					}
					Q[t][i] = JPEGUtils.get8(in);
					count++;
				}
				EnhanceQuantizationTable(Q[t]);
			} else {
				for (i = 0; i < 64; i++) {
					if (count > Lq) {
						JPEGUtils
								.error("ERROR: Quantization table format error");
					}
					Q[t][i] = JPEGUtils.get16(in);
					count += 2;
				}
				EnhanceQuantizationTable(Q[t]);
			}
		}
		if (count != Lq) {
			JPEGUtils.error("ERROR: Quantization table error [count!=Lq]");
		}
		return 1;
	}

	private void EnhanceQuantizationTable(int qtab[]) {
		int i;
		for (i = 0; i < 8; i++) {
			qtab[JPEGUtils.table[0 * 8 + i]] *= 90;
			qtab[JPEGUtils.table[4 * 8 + i]] *= 90;
			qtab[JPEGUtils.table[2 * 8 + i]] *= 118;
			qtab[JPEGUtils.table[6 * 8 + i]] *= 49;
			qtab[JPEGUtils.table[5 * 8 + i]] *= 71;
			qtab[JPEGUtils.table[1 * 8 + i]] *= 126;
			qtab[JPEGUtils.table[7 * 8 + i]] *= 25;
			qtab[JPEGUtils.table[3 * 8 + i]] *= 106;
		}
		for (i = 0; i < 8; i++) {
			qtab[JPEGUtils.table[0 + 8 * i]] *= 90;
			qtab[JPEGUtils.table[4 + 8 * i]] *= 90;
			qtab[JPEGUtils.table[2 + 8 * i]] *= 118;
			qtab[JPEGUtils.table[6 + 8 * i]] *= 49;
			qtab[JPEGUtils.table[5 + 8 * i]] *= 71;
			qtab[JPEGUtils.table[1 + 8 * i]] *= 126;
			qtab[JPEGUtils.table[7 + 8 * i]] *= 25;
			qtab[JPEGUtils.table[3 + 8 * i]] *= 106;
		}
		for (i = 0; i < 64; i++) {
			qtab[i] >>= 6;
		}
	}
}
