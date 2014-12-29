package com.iteye.weimingtom.jpegdecoder;

import java.io.InputStream;

public class HuffmanTable {
	private int Lh;
	private int[][] Tc = new int[4][2];
	private int[] Th = new int[4];
	private int[][][] L = new int[4][2][16];
	private int[][][][] V = new int[4][2][16][200];

	public HuffmanTable() {
		Tc[0][0] = 0;
		Tc[1][0] = 0;
		Tc[2][0] = 0;
		Tc[3][0] = 0;
		Tc[0][1] = 0;
		Tc[1][1] = 0;
		Tc[2][1] = 0;
		Tc[3][1] = 0;
		Th[0] = 0;
		Th[1] = 0;
		Th[2] = 0;
		Th[3] = 0;
	}

	public int get(InputStream in, int[][][] HuffTab) throws Exception {
		int i, j, temp, count = 0, t, c;
		Lh = JPEGUtils.get16(in);
		count += 2;
		while (count < Lh) {
			temp = JPEGUtils.get8(in);
			count++;
			t = temp & 0x0F;
			if (t > 3) {
				JPEGUtils.error("ERROR: Huffman table ID > 3");
			}
			c = temp >> 4;
			if (c > 2) {
				JPEGUtils.error("ERROR: Huffman table [Table class > 2 ]");
			}
			Th[t] = 1;
			Tc[t][c] = 1;
			for (i = 0; i < 16; i++) {
				L[t][c][i] = JPEGUtils.get8(in);
				count++;
			}
			for (i = 0; i < 16; i++) {
				for (j = 0; j < L[t][c][i]; j++) {
					if (count > Lh) {
						JPEGUtils
								.error("ERROR: Huffman table format error [count>Lh]");
					}
					V[t][c][i][j] = JPEGUtils.get8(in);
					count++;
				}
			}
		}
		if (count != Lh) {
			JPEGUtils.error("ERROR: Huffman table format error [count!=Lf]");
		}
		for (i = 0; i < 4; i++) {
			for (j = 0; j < 2; j++) {
				if (Tc[i][j] != 0) {
					Build_HuffTab(HuffTab[i][j], L[i][j], V[i][j]);
				}
			}
		}
		return 1;
	}

	private void Build_HuffTab(int tab[], int L[], int V[][]) throws Exception {
		int current_table;
		int i;
		int j;
		int n;
		int temp;
		int k;
		temp = 256;
		k = 0;
		for (i = 0; i < 8; i++) {
			for (j = 0; j < L[i]; j++) {
				for (n = 0; n < (temp >> (i + 1)); n++) {
					tab[k] = V[i][j] | ((i + 1) << 8);
					k++;
				}
			}
		}
		for (i = 1; k < 256; i++, k++) {
			tab[k] = i | JPEGUtils.MSB;
		}
		if (i > 50) {
			JPEGUtils.error("ERROR: Huffman table out of memory!");
		}
		current_table = 1;
		k = 0;
		for (i = 8; i < 16; i++) {
			for (j = 0; j < L[i]; j++) {
				for (n = 0; n < (temp >> (i - 7)); n++) {
					tab[current_table * 256 + k] = V[i][j] | ((i + 1) << 8);
					k++;
				}
				if (k >= 256) {
					if (k > 256) {
						JPEGUtils.error("ERROR: Huffman table error(1)!");
					}
					k = 0;
					current_table++;
				}
			}
		}
	}
}