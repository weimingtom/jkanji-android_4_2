package com.iteye.weimingtom.snowbook.pojo;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ListItemModel {
	private CharSequence title;
	private CharSequence detail;
	private String filename;

	public ListItemModel(Context context, CharSequence title, CharSequence detail, String filename) {
		this.title = title;
		this.detail = detail;
		this.filename = filename;
	}

	public CharSequence getTitle() {
		return title;
	}

	public void setTitle(CharSequence title) {
		this.title = title;
	}

	public CharSequence getDetail() {
		return detail;
	}

	public void setDetail(CharSequence detail) {
		this.detail = detail;
	}

	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
}
