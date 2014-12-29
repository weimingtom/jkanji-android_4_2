package com.iteye.weimingtom.jkanji;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.Time;
import android.util.Log;

public class JkanjiShelfHistoryDataSource {
	private final static boolean D = false;
	private final static String TAG = "JkanjiShelfHistoryDataSource";
	
	private SQLiteDatabase database;
	private JkanjiShelfSQLiteOpenHelper dbHelper;

	private final String[] allColumns = { 
		JkanjiShelfSQLiteOpenHelper.COLUMN_ID,
		JkanjiShelfSQLiteOpenHelper.COLUMN_CONTENT, 
	};

	public JkanjiShelfHistoryDataSource(Context context) {
		dbHelper = new JkanjiShelfSQLiteOpenHelper(context);
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

	public long createItem(JkanjiShelfHistoryItem item) {
		Time currTime = new Time(Time.getCurrentTimezone());
		currTime.setToNow();
		item.setPlainTime(currTime);
		ContentValues values = new ContentValues();
		long insertId;
		values.put(JkanjiShelfSQLiteOpenHelper.COLUMN_CONTENT, item.getContent());
		if (item.getId() < 0) {
			insertId = database.insert(JkanjiShelfSQLiteOpenHelper.TABLE_HISTORY, 
				null, values);
			if (D) {
				Log.d(TAG, "insert(" + item.getId() + "," + insertId + ") " + item);
			}
			return insertId;
		} else {
			insertId = item.getId();
			database.update(JkanjiShelfSQLiteOpenHelper.TABLE_HISTORY,
				values, 
				JkanjiShelfSQLiteOpenHelper.COLUMN_ID + " = " + insertId, 
				null);
			if (D) {
				Log.d(TAG, "update(" + item.getId() + "," + insertId + ") " + item);
			}
			return insertId;
		}
	}

	private JkanjiShelfHistoryItem cursorToItem(Cursor cursor) {
		JkanjiShelfHistoryItem content = new JkanjiShelfHistoryItem();
		content.setId(cursor.getLong(0));
		content.setContent(cursor.getString(1));
		return content;
	}

	public void deleteItem(JkanjiShelfHistoryItem content) {
		long id = content.getId();
		database.delete(JkanjiShelfSQLiteOpenHelper.TABLE_HISTORY, 
			JkanjiShelfSQLiteOpenHelper.COLUMN_ID + " = " + id, null);
	}

	public ArrayList<JkanjiShelfHistoryItem> getAllItems() {
		ArrayList<JkanjiShelfHistoryItem> contents = new ArrayList<JkanjiShelfHistoryItem>();
		Cursor cursor = database.query(JkanjiShelfSQLiteOpenHelper.TABLE_HISTORY,
				allColumns, 
				null, null, 
				null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			JkanjiShelfHistoryItem content = cursorToItem(cursor);
			contents.add(content);
			cursor.moveToNext();
		}
		cursor.close();
		return contents;
	}
	
	public JkanjiShelfHistoryItem getItemById(long id) {
		JkanjiShelfHistoryItem item = null;
		Cursor cursor = database.query(JkanjiShelfSQLiteOpenHelper.TABLE_HISTORY,
				allColumns, 
				JkanjiShelfSQLiteOpenHelper.COLUMN_ID + " = " + id, null, 
				null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			item = cursorToItem(cursor);
			cursor.moveToNext();
		}
		return item;
	}
}
