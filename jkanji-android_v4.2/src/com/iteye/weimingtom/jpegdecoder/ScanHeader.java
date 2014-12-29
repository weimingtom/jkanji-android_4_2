package com.iteye.weimingtom.jpegdecoder;

import java.io.InputStream;

public class ScanHeader {
	private int Ls;
	public int Ns;
	private int Ss;
	private int Se;
	private int Ah;
	private int Al;
	public ScanComponent[] Comp;

	public int get(InputStream in) throws Exception {
		int i;
		int temp;
		int count = 0;
		Ls = JPEGUtils.get16(in);
		count += 2;
		Ns = JPEGUtils.get8(in);
		count++;
		Comp = new ScanComponent[Ns];
		for (i = 0; i < Ns; i++) {
			Comp[i] = new ScanComponent();
			if (count > Ls) {
				JPEGUtils.error("ERROR: scan header format error");
			}
			Comp[i].Cs = JPEGUtils.get8(in);
			count++;
			temp = JPEGUtils.get8(in);
			count++;
			Comp[i].Td = temp >> 4;
			Comp[i].Ta = temp & 0x0F;
		}
		Ss = JPEGUtils.get8(in);
		count++;
		Se = JPEGUtils.get8(in);
		count++;
		temp = JPEGUtils.get8(in);
		count++;
		Ah = temp >> 4;
		Al = temp & 0x0F;
		if (count != Ls) {
			JPEGUtils.error("ERROR: scan header format error [count!=Ns]");
		}
		return 1;
	}
}
