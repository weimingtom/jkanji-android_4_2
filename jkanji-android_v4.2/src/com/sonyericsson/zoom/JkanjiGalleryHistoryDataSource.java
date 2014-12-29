package com.sonyericsson.zoom;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.Time;
import android.util.Log;

public class JkanjiGalleryHistoryDataSource {
	private final static boolean D = false;
	private final static String TAG = "JkanjiGalleryHistoryDataSource";
	
	private SQLiteDatabase database;
	private JkanjiGallerySQLiteOpenHelper dbHelper;

	private final String[] allColumns = { 
		JkanjiGallerySQLiteOpenHelper.COLUMN_ID,
		JkanjiGallerySQLiteOpenHelper.COLUMN_CONTENT, 
	};

	public JkanjiGalleryHistoryDataSource(Context context) {
		dbHelper = new JkanjiGallerySQLiteOpenHelper(context);
	}
	
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}
	
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

	public long createItem(JkanjiGalleryHistoryItem item) {
		Time currTime = new Time(Time.getCurrentTimezone());
		currTime.setToNow();
		item.setPlainTime(currTime);
		ContentValues values = new ContentValues();
		long insertId;
		values.put(JkanjiGallerySQLiteOpenHelper.COLUMN_CONTENT, item.getContent());
		if (item.getId() < 0) {
			insertId = database.insert(JkanjiGallerySQLiteOpenHelper.TABLE_HISTORY, 
				null, values);
			if (D) {
				Log.d(TAG, "insert(" + item.getId() + "," + insertId + ") " + item);
			}
			return insertId;
		} else {
			insertId = item.getId();
			database.update(JkanjiGallerySQLiteOpenHelper.TABLE_HISTORY,
				values, 
				JkanjiGallerySQLiteOpenHelper.COLUMN_ID + " = " + insertId, 
				null);
			if (D) {
				Log.d(TAG, "update(" + item.getId() + "," + insertId + ") " + item);
			}
			return insertId;
		}
	}

	private JkanjiGalleryHistoryItem cursorToItem(Cursor cursor) {
		JkanjiGalleryHistoryItem content = new JkanjiGalleryHistoryItem();
		content.setId(cursor.getLong(0));
		content.setContent(cursor.getString(1));
		return content;
	}

	public void deleteItem(JkanjiGalleryHistoryItem content) {
		long id = content.getId();
		database.delete(JkanjiGallerySQLiteOpenHelper.TABLE_HISTORY, 
			JkanjiGallerySQLiteOpenHelper.COLUMN_ID + " = " + id, null);
	}

	public ArrayList<JkanjiGalleryHistoryItem> getAllItems() {
		ArrayList<JkanjiGalleryHistoryItem> contents = new ArrayList<JkanjiGalleryHistoryItem>();
		Cursor cursor = database.query(JkanjiGallerySQLiteOpenHelper.TABLE_HISTORY,
				allColumns, 
				null, null, 
				null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			JkanjiGalleryHistoryItem content = cursorToItem(cursor);
			contents.add(content);
			cursor.moveToNext();
		}
		cursor.close();
		return contents;
	}
	
	public JkanjiGalleryHistoryItem getItemById(long id) {
		JkanjiGalleryHistoryItem item = null;
		Cursor cursor = database.query(JkanjiGallerySQLiteOpenHelper.TABLE_HISTORY,
				allColumns, 
				JkanjiGallerySQLiteOpenHelper.COLUMN_ID + " = " + id, null, 
				null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			item = cursorToItem(cursor);
			cursor.moveToNext();
		}
		return item;
	}
}
