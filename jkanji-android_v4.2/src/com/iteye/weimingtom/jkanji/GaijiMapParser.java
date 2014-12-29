package com.iteye.weimingtom.jkanji;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;

public class GaijiMapParser {
	public static int parse(String filename, GaijiDataSource dataSrc) {
		int result = 0;
		FileInputStream istr = null;
		InputStreamReader reader = null;
		BufferedReader rbuf = null;
		try {
			istr = new FileInputStream(filename);
			reader = new InputStreamReader(istr, "shift-jis");
			rbuf = new BufferedReader(reader);
			String line;
			dataSrc.beginInsert();
			while (null != (line = rbuf.readLine())) {
				if (!line.startsWith("#")) {
					line = line.replace("\u0009", " ");
					String[] strs = line.split(" ");
//					Log.e("GaijiMapParser", "line == " + line + ",strs == " + strs.length);
					if (strs != null && strs.length >= 2) {
						String str1 = strs[0];
						String str2 = strs[1];
						String key = null;
						String value = null;
//						Log.e("GaijiMapParser", "str1 == " + str1 + "," + "str2 == " + str2);
						if (str1 != null && str1.length() >= 5) {
							key = str1.substring(0, 5);
						}
						if (str2 != null && str2.length() >= 5) {
							value = str2.substring(0, 5);
						}
						if (key != null && value != null &&
							(value.startsWith("u"))) {
//							Log.e("GaijiMapParser", "key == " + key + "," + "value == " + value);
							dataSrc.createItem(key, value);
							result++;
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			dataSrc.endInsert();
//			Log.e("GaijiMapParser", "endInsert()");
			if (rbuf != null) {
				try {
					rbuf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (istr != null) {
				try {
					istr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
