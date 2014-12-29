package com.sonyericsson.zoom;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class JkanjiGallerySQLiteOpenHelper extends SQLiteOpenHelper {
	private final static boolean D = false;
	private final static String TAG = "JkanjiGallerySQLiteOpenHelper";
	
	public static final String TABLE_HISTORY = "history";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_CONTENT = "content";

	private static final String DATABASE_NAME = "gallery_data.db";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE = 
			"create table " + TABLE_HISTORY + 
			"( " + 
			COLUMN_ID + " integer primary key autoincrement, " + 
			COLUMN_CONTENT + " text not null " + 
			");";

	public JkanjiGallerySQLiteOpenHelper(final Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
		onCreate(db);
	}
}
