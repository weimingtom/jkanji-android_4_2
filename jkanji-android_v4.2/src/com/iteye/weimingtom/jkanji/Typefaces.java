package com.iteye.weimingtom.jkanji;

import java.util.Hashtable;

import android.content.Context;
import android.graphics.Typeface;

/**
 * @see http://code.google.com/p/android/issues/detail?id=9904
 * @author Administrator
 *
 */
public class Typefaces {
	private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

	private static Typeface get(Context c, String assetPath) {
		synchronized (cache) {
			if (!cache.containsKey(assetPath)) {
				try {
					Typeface t = Typeface.createFromAsset(c.getAssets(),
							assetPath);
					cache.put(assetPath, t);
				} catch (Exception e) {
					System.err.println("Could not get typeface '" + assetPath
							+ "' because " + e.getMessage());
					e.printStackTrace();
					return null;
				}
			}
			return cache.get(assetPath);
		}
	}
	
	public static Typeface getFile(String filePath) {
		synchronized (cache) {
			if (filePath == null) {
				return null;
			}
			if (filePath != null && filePath.length() == 0) {
				return null;
			}
			if (!cache.containsKey(filePath)) {
				try {
					Typeface t = Typeface.createFromFile(filePath);
					cache.put(filePath, t);
				} catch (Exception e) {
					System.err.println("Could not get typeface '" + filePath
							+ "' because " + e.getMessage());
					e.printStackTrace();
					return null;
				}
			}
			return cache.get(filePath);
		}
	}
}
