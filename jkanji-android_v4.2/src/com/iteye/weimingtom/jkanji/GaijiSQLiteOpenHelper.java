package com.iteye.weimingtom.jkanji;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GaijiSQLiteOpenHelper extends SQLiteOpenHelper {
	private final static boolean D = false;
	private final static String TAG = "GaijiSQLiteOpenHelper";
	
	public static final String TABLE_GAIJI = "gaiji";
	public static final String COLUMN_GAIJI_KEY = "gaijikey";
	public static final String COLUMN_GAIJI_KEY_INDEX = "gaijikey_idx";
	public static final String COLUMN_GAIJI_VALUE = "gaijivalue";

	private static final String DATABASE_NAME = "gaiji_data.db";
	private static final int DATABASE_VERSION = 2;

	private static final String DATABASE_CREATE = 
			"create table " + TABLE_GAIJI + 
			"( " + 
			COLUMN_GAIJI_KEY + " text not null, " + 
			COLUMN_GAIJI_VALUE + " text not null " + 
			");";
	private static final String DATABASE_CREATE_INDEX = 
			"create index " + COLUMN_GAIJI_KEY_INDEX + " on " + 
			TABLE_GAIJI + 
			"(" + COLUMN_GAIJI_KEY + 
			");";

	public GaijiSQLiteOpenHelper(final Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
		db.execSQL(DATABASE_CREATE_INDEX);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAIJI);
		onCreate(db);
	}
}
