package com.iteye.weimingtom.jkanji;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class GaijiDataSource {
	private final static boolean D = false;
	private final static String TAG = "GaijiDataSource";
	
	private SQLiteDatabase database;
	private GaijiSQLiteOpenHelper dbHelper;

	private final String[] allColumns = { 
		GaijiSQLiteOpenHelper.COLUMN_GAIJI_KEY,
		GaijiSQLiteOpenHelper.COLUMN_GAIJI_VALUE, 
	};

	public GaijiDataSource(Context context) {
		dbHelper = new GaijiSQLiteOpenHelper(context);
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

	public void beginInsert() {
		database.beginTransaction();
	}
	
	public void endInsert() {
		database.setTransactionSuccessful();
		database.endTransaction();
	}	
	
	public long createItem(String key, String value) {
		long insertId = -1;
		if (key != null && key.length() > 0 && 
			value != null && value.length() > 0) {
			ContentValues values = new ContentValues();
			values.put(GaijiSQLiteOpenHelper.COLUMN_GAIJI_KEY, key);
			values.put(GaijiSQLiteOpenHelper.COLUMN_GAIJI_VALUE, value);
			insertId = database.insert(GaijiSQLiteOpenHelper.TABLE_GAIJI, 
				null, values);
		}
		return insertId;
	}
	
	public String getItem(String key) {
		String value = null;
		if (key != null) {
			Cursor cursor = database.query(GaijiSQLiteOpenHelper.TABLE_GAIJI,
					allColumns, 
					GaijiSQLiteOpenHelper.COLUMN_GAIJI_KEY + " = ?", new String[]{key}, 
					null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				value = cursor.getString(1);
				cursor.moveToNext();
			}
		}
		return value;
	}
	
	public void deleteAllItem() {
		database.delete(GaijiSQLiteOpenHelper.TABLE_GAIJI, 
			null, null);
	}
}
