package com.iteye.weimingtom.jkanji;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PrefUtil {
	private final static boolean D = false;
	private final static String TAG = "PrefUtil";
	
	private static final boolean USE_JSON = true;
	private static final boolean USE_DATABASE = false;
	
	private static final String JSON_FILE_NAME = "prefutil_json.txt";
	private static final String DICT_OUTPUT_FILE_NAME = "prefutil_dict_output.txt";
	private static final String JSON_SEP_STR = "|";
	//FIXME:
	private static final int MODE = Context.MODE_PRIVATE;// | Context.MODE_MULTI_PROCESS;
	
	public static synchronized String getString(Context context, String prefName, String key, String defValue) {
	    if (USE_JSON) {
	    	String result = defValue;
			if (key != null && prefName != null && 
	    		JKanjiActivity.SHARE_PREF_NAME.equals(prefName) &&
	    		JKanjiActivity.SHARE_PREF_OUTPUT_TEXT.equals(key)) {
				result = getFileString(context, DICT_OUTPUT_FILE_NAME);
	    	} else {
				try {
					JSONObject jsonObject = new JSONObject(getFileString(context, JSON_FILE_NAME));
					result = jsonObject.optString(prefName + JSON_SEP_STR + key, defValue);
				} catch (JSONException e) {
					e.printStackTrace();
				}
	    	}
			return result;
	    } else if (USE_DATABASE) {
			String retsult = null;
			PrefDataSource dataSrc = new PrefDataSource(context);
			dataSrc.open();
			SettingItem item = dataSrc.getItemByKey(prefName, key);
			if (item != null && item.id >= 0L) {
				retsult = item.value;
			} else {
				retsult = defValue;
			}
			dataSrc.close();
			return retsult;
		} else {
			SharedPreferences sp = context.getSharedPreferences(prefName, MODE);
			return sp.getString(key, defValue);
		}
	}
	
	public static synchronized void putString(Context context, String prefName, String key, String value) {
		if (USE_JSON) {
			if (key != null && prefName != null && 
	    		JKanjiActivity.SHARE_PREF_NAME.equals(prefName) &&
	    		JKanjiActivity.SHARE_PREF_OUTPUT_TEXT.equals(key)) {
				putFileString(context, DICT_OUTPUT_FILE_NAME, value);
	    	} else {
				JSONObject jsonObject = null; 
				try {
					jsonObject = new JSONObject(getFileString(context, JSON_FILE_NAME));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (jsonObject == null) {
					jsonObject = new JSONObject();
				}
				try {
					jsonObject.put(prefName + JSON_SEP_STR + key, value);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				putFileString(context, JSON_FILE_NAME, jsonObject.toString());
	    	}
		} else if (USE_DATABASE) {
			PrefDataSource dataSrc = new PrefDataSource(context);
			dataSrc.open();
			SettingItem item = dataSrc.getItemByKey(prefName, key);
			item.prefName = prefName;
			item.key = key;
			item.value = value;
			dataSrc.createItem(item);
			dataSrc.close();
		} else {
			Editor e = context.getSharedPreferences(prefName, MODE).edit();
			e.putString(key, value);
	    	//try {
	    	//	e.apply();
	    	//} catch (Throwable ex) {
	    		e.commit();
	    	//}
	    }
	}
	
	public static synchronized boolean getBoolean(Context context, String prefName, String key, boolean defValue) {
	    if (USE_JSON) {
			boolean result = defValue;
			try {
				JSONObject jsonObject = new JSONObject(getFileString(context, JSON_FILE_NAME));
				result = jsonObject.optBoolean(prefName + JSON_SEP_STR + key, defValue);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
	    } else if (USE_DATABASE) {
			boolean retsult = false;
			PrefDataSource dataSrc = new PrefDataSource(context);
			dataSrc.open();
			SettingItem item = dataSrc.getItemByKey(prefName, key);
			if (item != null && item.id >= 0L) {
				try {
					retsult = Boolean.parseBoolean(item.value);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			} else {
				retsult = defValue;
			}
			dataSrc.close();
			return retsult;
		} else {
			SharedPreferences sp = context.getSharedPreferences(prefName, MODE);
			return sp.getBoolean(key, defValue);
		}
	}
	
	public static synchronized void putBoolean(Context context, String prefName, String key, boolean value) {
		if (USE_JSON) {
			JSONObject jsonObject = null; 
			try {
				jsonObject = new JSONObject(getFileString(context, JSON_FILE_NAME));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (jsonObject == null) {
				jsonObject = new JSONObject();
			}
			try {
				jsonObject.put(prefName + JSON_SEP_STR + key, value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			putFileString(context, JSON_FILE_NAME, jsonObject.toString());
		} else if (USE_DATABASE) {
			PrefDataSource dataSrc = new PrefDataSource(context);
			dataSrc.open();
			SettingItem item = dataSrc.getItemByKey(prefName, key);
			item.prefName = prefName;
			item.key = key;
			item.value = Boolean.toString(value);
			dataSrc.createItem(item);
			dataSrc.close();
		} else {
			Editor e = context.getSharedPreferences(prefName, MODE).edit();
			e.putBoolean(key, value);
	    	//try {
	    	//	e.apply();
	    	//} catch (Throwable ex) {
	    		e.commit();
	    	//}
		}
	}
	
	public static synchronized int getInt(Context context, String prefName, String key, int defValue) {
	    if (USE_JSON) {
			int result = defValue;
			try {
				JSONObject jsonObject = new JSONObject(getFileString(context, JSON_FILE_NAME));
				result = jsonObject.optInt(prefName + JSON_SEP_STR + key, defValue);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
	    } else if (USE_DATABASE) {
			int retsult = 0;
			PrefDataSource dataSrc = new PrefDataSource(context);
			dataSrc.open();
			SettingItem item = dataSrc.getItemByKey(prefName, key);
			if (item != null && item.id >= 0L) {
				try {
					retsult = Integer.parseInt(item.value);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			} else {
				retsult = defValue;
			}
			dataSrc.close();
			return retsult;
		} else {
			SharedPreferences sp = context.getSharedPreferences(prefName, MODE);
			return sp.getInt(key, defValue);
		}
	}
	
	public static synchronized void putInt(Context context, String prefName, String key, int value) {
		if (USE_JSON) {
			JSONObject jsonObject = null; 
			try {
				jsonObject = new JSONObject(getFileString(context, JSON_FILE_NAME));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (jsonObject == null) {
				jsonObject = new JSONObject();
			}
			try {
				jsonObject.put(prefName + JSON_SEP_STR + key, value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			putFileString(context, JSON_FILE_NAME, jsonObject.toString());
		} else if (USE_DATABASE) {
			PrefDataSource dataSrc = new PrefDataSource(context);
			dataSrc.open();
			SettingItem item = dataSrc.getItemByKey(prefName, key);
			item.prefName = prefName;
			item.key = key;
			item.value = Integer.toString(value);
			dataSrc.createItem(item);
			dataSrc.close();
		} else {
			Editor e = context.getSharedPreferences(prefName, MODE).edit();
			e.putInt(key, value);
	    	//try {
	    	//	e.apply();
	    	//} catch (AbstractMethodError unused) {
	    		e.commit();
	    	//}
	    }
	}
	
	
	public final static class PrefSQLiteOpenHelper extends SQLiteOpenHelper {
		public static final String TABLE_SETTING = "setting";
		public static final String COLUMN_ID = "_id";
		public static final String COLUMN_PREFNAME = "pref_name";
		public static final String COLUMN_KEY = "key";
		public static final String COLUMN_VALUE = "value";
		
		private static final String DATABASE_NAME = "prefutil.db";
		private static final int DATABASE_VERSION = 1;

		private static final String DATABASE_CREATE = 
				"create table " + TABLE_SETTING + 
				"( " + 
				COLUMN_ID + " integer primary key autoincrement, " + 
				COLUMN_PREFNAME + " text not null, " + 
				COLUMN_KEY + " text not null, " + 
				COLUMN_VALUE + " text not null " + 
				");";

		public PrefSQLiteOpenHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTING);
			onCreate(db);
		}
	}
	
	private final static class SettingItem {
		public long id = -1L;
		public String prefName, key, value;
	}
	
	private final static class PrefDataSource {
		private SQLiteDatabase database;
		private PrefSQLiteOpenHelper dbHelper;

		private final String[] allColumns = { 
			PrefSQLiteOpenHelper.COLUMN_ID,
			PrefSQLiteOpenHelper.COLUMN_PREFNAME,
			PrefSQLiteOpenHelper.COLUMN_KEY,
			PrefSQLiteOpenHelper.COLUMN_VALUE, 
		};

		public PrefDataSource(Context context) {
			dbHelper = new PrefSQLiteOpenHelper(context);
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

		public long createItem(SettingItem item) {
			ContentValues values = new ContentValues();
			long insertId;
			values.put(PrefSQLiteOpenHelper.COLUMN_PREFNAME, item.prefName);
			values.put(PrefSQLiteOpenHelper.COLUMN_KEY, item.key);
			values.put(PrefSQLiteOpenHelper.COLUMN_VALUE, item.value);
			if (item.id < 0L) {
				insertId = database.insert(PrefSQLiteOpenHelper.TABLE_SETTING, 
					null, values);
				return insertId;
			} else {
				insertId = item.id;
				database.update(PrefSQLiteOpenHelper.TABLE_SETTING,
					values, 
					PrefSQLiteOpenHelper.COLUMN_ID + " = " + insertId, 
					null);
				return insertId;
			}
		}

		public SettingItem getItemByKey(String prefName, String key) {
			SettingItem item = new SettingItem();
			Cursor cursor = database.query(PrefSQLiteOpenHelper.TABLE_SETTING,
					allColumns, 
					PrefSQLiteOpenHelper.COLUMN_PREFNAME + " = ? AND " + PrefSQLiteOpenHelper.COLUMN_KEY + " = ?", 
					new String[]{prefName, key}, 
					null, null, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				item.id = cursor.getLong(0);
				item.prefName = cursor.getString(1);
				item.key = cursor.getString(2);
				item.value = cursor.getString(3);
				cursor.moveToNext();
			}
			return item;
		}
	}
	
	
	private static String getFileString(Context context, String filename) {
		FileInputStream instr = null;
		InputStreamReader reader = null;
		BufferedReader buffer = null;
		StringBuffer sb = new StringBuffer();
		try {
			instr = context.openFileInput(filename);
			reader = new InputStreamReader(instr, "utf-8");
			buffer = new BufferedReader(reader);
			String line;
			while (null != (line = buffer.readLine())) {
				sb.append(line);
				sb.append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (instr != null) {
				try {
					instr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (D) {
			Log.e(TAG, " >>> \n" + sb.toString());
		}
		return sb.toString();
	}
	
	private static void putFileString(Context context, String filename, String str) {
		FileOutputStream outstr = null;
		OutputStreamWriter writer = null;
		BufferedWriter buffer = null;
		try {
			outstr = context.openFileOutput(filename, Context.MODE_PRIVATE);
			writer = new OutputStreamWriter(outstr, "utf-8");
			buffer = new BufferedWriter(writer);
			buffer.write(str != null ? str : "");
			if (D) {
				Log.e(TAG, " <<< \n" + str);
			}
			buffer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outstr != null) {
				try {
					outstr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
