/*
 * Copyright 2012 Alexander Reichert

This file is part of ShareToClipboard.
ShareToClipboard is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
ShareToClipboard is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with Foobar. If not, see http://www.gnu.org/licenses/.
 */
package com.elgubbo.sharetoclipboard.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.Time;
import android.util.Log;

import com.elgubbo.sharetoclipboard.ShareContent;

/**
 * The Class ShareDataSource handles the neccesary CRUD operations on the DB
 */
public class ShareDataSource {
	private final static boolean D = false;
	private final static String TAG = "ShareDataSource";
	
	/** The database. */
	private SQLiteDatabase database;

	/** The db helper. */
	private ShareSQLAdapter dbHelper;

	/** The all columns. */
	private final String[] allColumns = { ShareSQLAdapter.COLUMN_ID,
			ShareSQLAdapter.COLUMN_CONTENT, ShareSQLAdapter.COLUMN_DATATYPE,
			ShareSQLAdapter.COLUMN_DATE, ShareSQLAdapter.COLUMN_DESCRIPTION };

	/**
	 * Instantiates a new share data source.
	 * 
	 * @param context
	 *            the context
	 */
	public ShareDataSource(Context context) {
		dbHelper = new ShareSQLAdapter(context);
	}

	/**
	 * Close.
	 */
	public void close() {
		if (database != null) {
			database.close();
			database = null;
		}
		if (dbHelper != null) {
			dbHelper.close();
			dbHelper = null;
		}
	}

	/**
	 * Creates the content.
	 * 
	 * @param content
	 *            the content
	 * @param desctription
	 *            the desctription
	 * @param dataType
	 *            the data type
	 * @return the share content
	 */
	public boolean createContent(String content,
			String desctription, String dataType, long id) {
		Time currentTime = new Time(Time.getCurrentTimezone());
		currentTime.setToNow();
		ContentValues values = new ContentValues();
		values.put(ShareSQLAdapter.COLUMN_CONTENT, content);
		values.put(ShareSQLAdapter.COLUMN_DATATYPE, dataType);
		values.put(ShareSQLAdapter.COLUMN_DATE, currentTime.format2445());
		values.put(ShareSQLAdapter.COLUMN_DESCRIPTION, desctription);
		if (id < 0) {
			long result = database.insert(ShareSQLAdapter.TABLE_SHARED,
					null, values);
			return result >= 0;
		} else {
			int num = database.update(ShareSQLAdapter.TABLE_SHARED, values, 
				ShareSQLAdapter.COLUMN_ID + " = " + id, null);
			return num > 0;
		}
	}

	/**
	 * Cursor to content.
	 * 
	 * @param cursor
	 *            the cursor
	 * @return the share content
	 */
	private ShareContent cursorToContent(Cursor cursor) {
		ShareContent content = new ShareContent();
		content.setId(cursor.getLong(0));
		content.setContent(cursor.getString(1));
		if (D) {
			Log.d(TAG, "Content is set to: " + content.getContent());
		}
		content.setDataType(cursor.getString(2));
		if (D) {
			Log.d(TAG, "Type is set to: " + content.getDataType());
		}
		
		Time time = new Time();
		try {
			time.parse(cursor.getString(3));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		content.setTime(time);
		content.setDescription(cursor.getString(4));
		if (D) {
			Log.d(TAG, "Description is set to: " + content.getDescription());
		}
		return content;
	}

	/**
	 * Delete content.
	 * 
	 * @param content
	 *            the content
	 */
	public void deleteContent(ShareContent content) {
		long id = content.getId();
		System.out.println("Comment deleted with id: " + id);
		database.delete(ShareSQLAdapter.TABLE_SHARED, ShareSQLAdapter.COLUMN_ID
				+ " = " + id, null);
	}

	/**
	 * Gets the all contents.
	 * 
	 * @return the all contents
	 */
	public ArrayList<ShareContent> getAllContents() {
		ArrayList<ShareContent> contents = new ArrayList<ShareContent>();

		Cursor cursor = database.query(ShareSQLAdapter.TABLE_SHARED,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ShareContent content = cursorToContent(cursor);
			contents.add(content);
			cursor.moveToNext();
		}
		cursor.close();
		return contents;
	}

	/**
	 * Open.
	 * 
	 * @throws SQLException
	 *             the sQL exception
	 */
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

}
