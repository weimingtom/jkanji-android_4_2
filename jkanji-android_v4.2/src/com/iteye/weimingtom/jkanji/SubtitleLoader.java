package com.iteye.weimingtom.jkanji;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import android.util.Log;

public class SubtitleLoader {
	private static final boolean D = false;
	private static final String TAG = "SubtitleLoader";
	
	private final static class SubItem {
		public String start;
		public String end;
		public String subtitle;
		public int startTime;
		public int endTime;
		
		@Override
		public String toString() {
    		return "[" + startTime + " -> " + endTime + "]" + 
    				" => " + subtitle;
		}
	}
	
	private String filename;
	private List<SubItem> subtitles = new ArrayList<SubItem>();
	
	public SubtitleLoader() {
		
	}
	
	public String getSubtitle(int msec) {
		StringBuffer sb = new StringBuffer();
		for (SubItem item : subtitles) {
			if (item.startTime <= msec && msec <= item.endTime) {
				sb.append(item.subtitle);
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	private static int timeStrToMS(String str) {
		if (str == null) {
			return 0;
		}
		String[] strs = str.split(":");
		if (strs == null || strs.length != 3) {
			return 0;
		}
		int hour = Integer.parseInt(strs[0]);
		int min = Integer.parseInt(strs[1]);
		float sec = Float.parseFloat(strs[2]);
		return (int)(sec * 1000) + min * 1000 * 60 + hour * 1000 * 60 * 60;
	}
	
	public void appendItem(String start, String end, String subtitle) {
		SubItem item = new SubItem();
		item.start = start;
		item.startTime = timeStrToMS(start);
		item.end = end;
		item.endTime = timeStrToMS(end);
		item.subtitle = subtitle;
		if (D) {
			Log.d(TAG, item.toString());
		}
		subtitles.add(item);
	}
	
	public void load(String filename) {
		this.filename = filename;
		InputStream inputStream = null;
		BufferedReader reader = null;
		try {
			String line;
			inputStream = new FileInputStream(filename);
	        reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			while (true) {
		        if ((line = reader.readLine()) != null) {
		        	if (line.startsWith("Dialogue:")) {
		        		String[] strings = TextUtils.split(line, ",");
		        		String start = "";
		        		String end = "";
		        		String subtitle = "";
		        		if (strings.length >= 4) {
		        			start = strings[1];
		        			end = strings[2];
		        			subtitle = strings[strings.length - 1];
		        		}
		        		appendItem(start, end, subtitle);
		        	}
				} else {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
