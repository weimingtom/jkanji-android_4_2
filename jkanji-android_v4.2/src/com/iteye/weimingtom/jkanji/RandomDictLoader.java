package com.iteye.weimingtom.jkanji;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;

public class RandomDictLoader {
	public static int TYPE_JPWORDS = 0;
	public static int TYPE_ENWORDS = 1;
	
	public static int getTotal(AssetManager am, int type) throws IOException  {
		String dataFileName, indexFileName;
		if (DataContext.USE_SEPARATE_PACK) {
			if (type == TYPE_ENWORDS) {
				dataFileName = DataContext.DATA2_FILE_NAMES;
				indexFileName = DataContext.INDEX2_FILE_NAMES;				
			} else { 
				dataFileName = DataContext.DATA0_FILE_NAMES;
				indexFileName = DataContext.INDEX0_FILE_NAMES;
			}
		} else {
			dataFileName = DataContext.DATA_FILE_NAMES;
			indexFileName = DataContext.INDEX_FILE_NAMES;
		}
		InputStream dicStr = am.open(dataFileName, AssetManager.ACCESS_STREAMING);
		InputStream idxStr = am.open(indexFileName, AssetManager.ACCESS_STREAMING);
		int result = 0;
		try {
			dicStr.skip(8);
			result |= (dicStr.read() & 0xff) << 24;
			result |= (dicStr.read() & 0xff) << 16;
			result |= (dicStr.read() & 0xff) << 8;
			result |= (dicStr.read() & 0xff) << 0;
		} catch (IOException e) {
			e.printStackTrace();
			result = 0;
		} finally {
			if (dicStr != null) {
				try {
					dicStr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (idxStr != null) {
				try {
					idxStr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	public static Word getWord(AssetManager am, int index, int type) throws IOException {
		String dataFileName, indexFileName;
		if (DataContext.USE_SEPARATE_PACK) {
			if (type == TYPE_ENWORDS) {
				dataFileName = DataContext.DATA2_FILE_NAMES;
				indexFileName = DataContext.INDEX2_FILE_NAMES;				
			} else { 
				dataFileName = DataContext.DATA0_FILE_NAMES;
				indexFileName = DataContext.INDEX0_FILE_NAMES;
			}
		} else {
			dataFileName = DataContext.DATA_FILE_NAMES;
			indexFileName = DataContext.INDEX_FILE_NAMES;
		}
		InputStream dicStr = am.open(dataFileName, AssetManager.ACCESS_STREAMING);
		InputStream idxStr = am.open(indexFileName, AssetManager.ACCESS_STREAMING);
		Word result = null;
		try {
			idxStr.skip(index * 4);
			int pos = 0;
			pos |= (idxStr.read() & 0xff) << 24;
			pos |= (idxStr.read() & 0xff) << 16;
			pos |= (idxStr.read() & 0xff) << 8;
			pos |= (idxStr.read() & 0xff) << 0;
			dicStr.skip(pos + 8);
			String[] records = new String[5]; 
			for (int i = 0; i < 5; i++) {
				int byteNum = 0;
				byteNum |= (dicStr.read() & 0xff) << 24;
				byteNum |= (dicStr.read() & 0xff) << 16;
				byteNum |= (dicStr.read() & 0xff) << 8;
				byteNum |= (dicStr.read() & 0xff) << 0;
				String str = null;
				if (byteNum > 0) {
					byte[] bytes = new byte[byteNum];
					dicStr.read(bytes, 0, byteNum);
					str = new String(bytes, "UTF-8");
				}
				records[i] = str;
			}
			result = new Word(index, records);
		} catch (IOException e) {
			e.printStackTrace();
			result = null;
		} finally {
			if (dicStr != null) {
				try {
					dicStr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (idxStr != null) {
				try {
					idxStr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
