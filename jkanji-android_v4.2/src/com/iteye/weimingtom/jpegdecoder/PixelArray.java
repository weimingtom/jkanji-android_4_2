package com.iteye.weimingtom.jpegdecoder;

public interface PixelArray {
    public void setSize(int width, int height);
    public void setPixel(int x, int y, int argb);
}
