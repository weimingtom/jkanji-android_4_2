package com.iteye.weimingtom.jpegdecoder;

public class Bild {

}

/*
import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.io.FileInputStream;

class Bild extends Frame implements Runnable, PixelArray {
    Image im = null;
    Thread load;
    String file;
    JPEGDecoder j = null;

    // Implementation of PixelArray

    int[] pix;
    int width, height;

    public void setSize(int width, int height) {
	this.width = width;
	this.height = height;
	pix = new int[width * height];
    }

    public void setPixel(int x, int y, int argb) {
	pix[x + y * width] = argb;
    }

    // Image viewer

    public static void main(String args[]) {
	new Bild("infinite_stratos_00000001.jpg");
    }

    public Bild(String s) {
	file = s;
	j = new JPEGDecoder();
	load = new Thread(this);
	load.start();
	this.setTitle("Bild:" + s);
	this.resize(300, 200);
	this.show();
	while (im == null) {
	    try {
		Thread.sleep(1000);
	    } catch (Exception e) {
	    }
	    repaint();
	}
    }

    public void run() {
	try {
	    FileInputStream in = new FileInputStream(file);
	    j.decode(in, this);
	    in.close();
	    MemoryImageSource mi = new MemoryImageSource(width, height, pix, 0,
		    width);
	    im = createImage(mi);
	    repaint();
	} catch (Exception e) {
	    System.out.println("Etwas ging schief: " + e);
	}
    }

    public void paint(Graphics g) {
	if (im != null) {
	    g.drawImage(im, 0, 0, this);
	} else {
	    g.drawString("Decodierung", 40, 50);
	    if (j != null)
		g.drawString("Progress:" + j.progress() + "%", 40, 70);
	}
    }
}
*/