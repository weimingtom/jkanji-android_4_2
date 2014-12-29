package com.sonyericsson.zoom;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ThumbsSQLiteOpenHelper extends SQLiteOpenHelper {
	private final static boolean D = false;
	private final static String TAG = "ThumbsSQLiteOpenHelper";
	
	public static final String TABLE_THUMBS = "thumbs";
	public static final String COLUMN_THUMBS_IMAGE = "image";
	public static final String COLUMN_THUMBS_NAME = "name";
	
	private static final String DATABASE_NAME = null; //"thumbs.db";
	private static final int DATABASE_VERSION = 2;

	private static final String DATABASE_CREATE = 
			"create table if not exists " + TABLE_THUMBS + 
			"( " +  
			COLUMN_THUMBS_IMAGE + " blob" + "," +
			COLUMN_THUMBS_NAME + " text" +
			");";

	public ThumbsSQLiteOpenHelper(final Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_THUMBS);
		onCreate(db);
	}
}
