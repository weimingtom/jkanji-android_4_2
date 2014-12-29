package com.iteye.weimingtom.jpegdecoder;

import java.io.InputStream;

public class FrameHeader {
    private int SOF;
    private int Lf;
    private int P;
    public int Y;
    public int X;
    private int Nf;
    public ComponentSpec[] Comp;

    public int get(InputStream in, int sof) throws Exception {
		int i;
		int temp;
		int count = 0;
		int c;
		int _height; //TODO:没有考虑异常安全
		SOF = sof;
		Lf = JPEGUtils.get16(in);
		count += 2;
		P = JPEGUtils.get8(in);
		count++;
		Y = JPEGUtils.get16(in);
		count += 2;
		_height = Y;
		X = JPEGUtils.get16(in);
		count += 2;
		Nf = JPEGUtils.get8(in);
		count++;
		Comp = new ComponentSpec[Nf + 1];
		for (i = 0; i <= Nf; i++) {
		    Comp[i] = new ComponentSpec();
		}
		for (i = 1; i <= Nf; i++) {
		    if (count > Lf) {
		    	JPEGUtils.error("ERROR: frame format error");
		    }
		    c = JPEGUtils.get8(in);
		    count++;
		    if (c >= Lf) {
		    	JPEGUtils.error("ERROR: fram format error [c>=Lf]");
		    }
		    Comp[c].C = c;
		    temp = JPEGUtils.get8(in);
		    count++;
		    Comp[c].H = temp >> 4;
		    Comp[c].V = temp & 0x0F;
		    Comp[c].Tq = JPEGUtils.get8(in);
		    count++;
		}
		if (count != Lf) {
		    JPEGUtils.error("ERROR: frame format error [Lf!=count]");
		}
		return _height;
	}
}
