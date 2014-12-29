package com.iteye.weimingtom.jkanji;

import android.os.Parcel;
import android.os.Parcelable;

public class Word implements Parcelable {
	private final static String[] NUM_STRS = {
		"(0)", "(1)", "(2)", "(3)", "(4)",
		"(5)", "(6)", "(7)", "(8)", "(9)",
	};
	
	public int id;
	public String[] record;
	public String catalog;
	public String reading;
	public String kanji;
	public String mean;	
	public String etc;
	
	public Word(int id, String[] record) {
		this.id = id;
		this.record = record;
		parse(record);
	}
	
	private void parse(String[] record) {
		if (record != null) {
			this.catalog = record[0];
			this.reading = record[1];
			this.kanji = record[2];
			this.mean = record[3];
			this.etc = record[4];
		}
	}
	
	@Override
	public String toString() {
		return "id:" + id + " " + //
			//"record:" + record + " " + //
			"catalog:" + catalog + " " + //
			"reading:" + reading + " " + //
			"kanji:" + kanji + " " + //
			"mean:" + mean + " " + //
			"etc:" + etc; //
	}
	
	public String toShareString() {
		String kanji2 = kanji;
		if (kanji == null) {
			kanji2 = reading;
		}
		return (kanji2 != null ? kanji2 : "") + 
			(reading != null ? "【" + reading + "】" : "") + this.getAccent() + "\n" + 
			((catalog != null) ? "（" + catalog + "）\n" : "") +
			mean + "\n";
	}
	
	@Override
	public int describeContents() {         
		return 0;     
	}     
	
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(catalog);
		out.writeString(reading);
		out.writeString(kanji);
		out.writeString(mean);
		out.writeString(etc);
	}

	public static final Parcelable.Creator<Word> CREATOR = 
		new Parcelable.Creator<Word>() {         
			public Word createFromParcel(Parcel in) {             
				return new Word(in);         
			}         
				
			public Word[] newArray(int size) {             
				return new Word[size];         
			}     
		};
		
	private Word(Parcel in) {  
		id = in.readInt();
		catalog = in.readString();
		reading = in.readString();
		kanji = in.readString();
		mean = in.readString();
		etc = in.readString();
	}
	
	public String getTTSString() {
		String ret = "";
		if (kanji != null && kanji.length() > 0 && kanji.charAt(0) > 255) {
			ret = kanji;
		} else if (reading != null && reading.length() > 0) {
			ret = reading;
		}
		ret = ret.replace("～", " ");
		ret = ret.trim();
		return ret;
	}
	
	public String getAccent() {
		return getAccentStr(this.etc);
	}
	
	private static String getAccentStr(String etc) {
		if (etc == null || etc.length() == 0) {
			return "";
		}
		int len = etc.length();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++){
			String ch = etc.substring(i, i + 1);
			try {
				int num = Integer.parseInt(ch);
				if (num >= 0 && num < NUM_STRS.length) {
					sb.append(NUM_STRS[num]);
				}
			} catch (Throwable e) {
				
			}
		}
		return sb.toString();
	}
}
