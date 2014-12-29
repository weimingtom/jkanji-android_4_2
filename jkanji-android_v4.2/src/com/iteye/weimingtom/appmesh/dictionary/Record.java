package com.iteye.weimingtom.appmesh.dictionary;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class Record {
	private static final int COL_NUM_MAX = 8;
	public static final int BYTES_MAX = 2048;
	
	private String romaji = ""; //A
	private String romaji2 = ""; //B
	private String sound = ""; //C
	private String kanji = ""; //D
	private String kanji2 = ""; //E
	private String accent = ""; //F
	private String pos = ""; //G
	private String mean = ""; //H
	
	public void parse(String line) {
		String[] results = line.split(",");
		if (results != null) {
			if (results.length >= COL_NUM_MAX) {
				romaji = results[0];
				romaji2 = results[1];
				sound = results[2];
				kanji = results[3];
				kanji2 = results[4];
				accent = results[5];
				pos = results[6];
				mean = results[7];
				return;
			}
		}
		throw new RuntimeException("error col num");
	}
	
	public ByteBuffer toByteBuffer() {
		ByteBuffer bytes = ByteBuffer.allocate(BYTES_MAX);
		bytes.position(0);
		bytes.order(ByteOrder.LITTLE_ENDIAN);
		bytes.clear();
		
		try {
			byte[] bytesStr = romaji.getBytes("UTF-8");
			bytes.putInt(bytesStr.length);
			bytes.put(bytesStr);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			byte[] bytesStr = romaji2.getBytes("UTF-8");
			bytes.putInt(bytesStr.length);
			bytes.put(bytesStr);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			byte[] bytesStr = sound.getBytes("UTF-8");
			bytes.putInt(bytesStr.length);
			bytes.put(bytesStr);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			byte[] bytesStr = kanji.getBytes("UTF-8");
			bytes.putInt(bytesStr.length);
			bytes.put(bytesStr);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			byte[] bytesStr = kanji2.getBytes("UTF-8");
			bytes.putInt(bytesStr.length);
			bytes.put(bytesStr);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			byte[] bytesStr = accent.getBytes("UTF-8");
			bytes.putInt(bytesStr.length);
			bytes.put(bytesStr);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			byte[] bytesStr = pos.getBytes("UTF-8");
			bytes.putInt(bytesStr.length);
			bytes.put(bytesStr);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			byte[] bytesStr = mean.getBytes("UTF-8");
			bytes.putInt(bytesStr.length);
			bytes.put(bytesStr);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return bytes;
	}
	
	public void fromByteBuffer(ByteBuffer bytes) {
		bytes.order(ByteOrder.LITTLE_ENDIAN);
		int len;
		len = bytes.getInt();
		if (len > 0) {
			byte[] bytesStr = new byte[len];
			bytes.get(bytesStr);
			try {
				romaji = new String(bytesStr, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		len = bytes.getInt();
		if (len > 0) {
			byte[] bytesStr = new byte[len];
			bytes.get(bytesStr);
			try {
				romaji2 = new String(bytesStr, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		len = bytes.getInt();
		if (len > 0) {
			byte[] bytesStr = new byte[len];
			bytes.get(bytesStr);
			try {
				sound = new String(bytesStr, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		len = bytes.getInt();
		if (len > 0) {
			byte[] bytesStr = new byte[len];
			bytes.get(bytesStr);
			try {
				kanji = new String(bytesStr, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		len = bytes.getInt();
		if (len > 0) {
			byte[] bytesStr = new byte[len];
			bytes.get(bytesStr);
			try {
				kanji2 = new String(bytesStr, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		len = bytes.getInt();
		if (len > 0) {
			byte[] bytesStr = new byte[len];
			bytes.get(bytesStr);
			try {
				accent = new String(bytesStr, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		len = bytes.getInt();
		if (len > 0) {
			byte[] bytesStr = new byte[len];
			bytes.get(bytesStr);
			try {
				pos = new String(bytesStr, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		len = bytes.getInt();
		if (len > 0) {
			byte[] bytesStr = new byte[len];
			bytes.get(bytesStr);
			try {
				mean = new String(bytesStr, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	public String getRomaji() {
		return romaji;
	}

	public void setRomaji(String romaji) {
		this.romaji = romaji;
	}

	public String getRomaji2() {
		return romaji2;
	}

	public void setRomaji2(String romaji2) {
		this.romaji2 = romaji2;
	}

	public String getSound() {
		return sound;
	}

	public void setSound(String sound) {
		this.sound = sound;
	}

	public String getKanji() {
		return kanji;
	}

	public void setKanji(String kanji) {
		this.kanji = kanji;
	}

	public String getKanji2() {
		return kanji2;
	}

	public void setKanji2(String kanji2) {
		this.kanji2 = kanji2;
	}

	public String getAccent() {
		return accent;
	}

	public void setAccent(String accent) {
		this.accent = accent;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getMean() {
		return mean;
	}

	public void setMean(String mean) {
		this.mean = mean;
	}

	public static int getColNumMax() {
		return COL_NUM_MAX;
	}
	
	
}
