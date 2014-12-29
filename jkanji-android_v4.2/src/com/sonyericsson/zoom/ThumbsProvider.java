package com.sonyericsson.zoom;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.example.android.bitmapfun.util.LruCache;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

public class ThumbsProvider extends ContentProvider {
    private static final boolean D = false;
    private static final String TAG = "ThumbsProvider";
	
    private static final boolean USE_SHARE_FILE = false;
    private static final String SHARE_FILE_NAME = "ThumbsProvider_share.png";
    
    public static final String THUMB_PREFIX = "thumb:";
    
    private static final float LRU_PERCENT = 0.25f;
    
	public static final String PARSER_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.sonyericsson.zoom";
	public static final String CLEAR_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.sonyericsson.zoom";
	
    public final static String AUTHORITY = "com.sonyericsson.zoom.ThumbsProvider";
    
    public static final Uri CONTENT_URI_PARSER = Uri.parse("content://" + AUTHORITY + "/parser");
    public static final Uri CONTENT_URI_CLEAR = Uri.parse("content://" + AUTHORITY + "/clear");
    
    private static final int PARSER = 0;
    private static final int CLEAR = 1;
    private static final UriMatcher sURIMatcher = buildUriMatcher();
    
    private ByteArrayOutputStream ostr;
    
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, "parser", PARSER);
        matcher.addURI(AUTHORITY, "clear", CLEAR);
        return matcher;
    }

	private ThumbsSQLiteOpenHelper dbHelper;
	private SQLiteDatabase database = null;
	private LruThumbPool pool;

	@Override
	public boolean onCreate() {
		boolean result = true;
		dbHelper = new ThumbsSQLiteOpenHelper(this.getContext());
		database = dbHelper.getWritableDatabase();
		pool = new LruThumbPool();
		ostr = new ByteArrayOutputStream();
		return result;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (pool != null) {
			pool.clear();
		}
	}

	@Override
	public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
        case PARSER:
            return PARSER_MIME_TYPE;
            
        case CLEAR:
        	return CLEAR_MIME_TYPE; 
        	
        default:
        	throw new IllegalArgumentException("Unknown URL " + uri);
        }
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		String arg0, arg1, arg2, arg3, arg4;
		if (D) {
			Log.d(TAG, "ThumbsProvider query : " + uri);
		}
		switch (sURIMatcher.match(uri)) {
        case PARSER:
            if (selectionArgs == null || selectionArgs.length < 5) {
            	throw new IllegalArgumentException(
            			"selectionArgs must be provided for the Uri: " + uri);
            }
            arg0 = selectionArgs[0];
            arg1 = selectionArgs[1];
            arg2 = selectionArgs[2];
            arg3 = selectionArgs[3];
            arg4 = selectionArgs[4];
            if (arg0 == null) {
            	arg0 = "";
            }
            if (arg1 == null) {
            	arg1 = "";
            }
            if (arg2 == null) {
            	arg2 = "";
            }
            if (arg3 == null) {
            	arg3 = "";
            }
            if (arg4 == null) {
            	arg4 = "";
            }
            return getParse(arg0, arg1, arg2, arg3, arg4);
            
        case CLEAR:
        	if (pool != null) {
        		pool.clear();
        	}
            return null;
        	
        default:
            throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
	}
	
    private Cursor getParse(String filename, String reqw, String reqh, String use16, String isSample) {
        return analyze(filename, 
        	Integer.parseInt(reqw), 
        	Integer.parseInt(reqh), 
        	Boolean.parseBoolean(use16), 
        	Boolean.parseBoolean(isSample));
    }
    
	private Cursor analyze(String filename, int reqw, int reqh, boolean use16, boolean isSample) {
		byte[] bytes = null;
		if (pool != null) {
			bytes = pool.get(filename);
		}
		if (bytes == null) {
			Bitmap bitmap = null;
			String name = filename;
			if (filename.startsWith(THUMB_PREFIX)) {
				name = filename.substring(THUMB_PREFIX.length());
			}
			if (isSample) {
				bitmap = decodeSampledBitmapFromFile(name, reqw, reqh, use16);
			} else {
				if (use16) {
					BitmapFactory.Options options = new BitmapFactory.Options();
			        options.inPreferredConfig = Bitmap.Config.RGB_565;   
					options.inPurgeable = true;  
					options.inInputShareable = true;  
					bitmap = BitmapFactory.decodeFile(name, options);
				} else {
					bitmap = BitmapFactory.decodeFile(name);
				}
			}
			if (bitmap != null) {
				ostr.reset();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostr);
				try {
					ostr.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				bytes = ostr.toByteArray();
//					try {
//						ostr.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
				bitmap.recycle();
				if (pool != null) {
					pool.put(filename, bytes);
					if (D) {
						Log.e(TAG, "provider pool put...");
					}
				}
			}
		} else {
			if (D) {
				Log.e(TAG, "provider pool get");
			}
		}
		if (USE_SHARE_FILE && bytes != null) {
			FileOutputStream fstr = null;
			try {
				fstr = this.getContext().openFileOutput(SHARE_FILE_NAME, 0);
				fstr.write(bytes, 0, bytes.length);
				fstr.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					fstr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (D && bytes != null) {
			Log.e(TAG, "bytes == " + (bytes != null ? bytes.length : 0));
		}
		
		if (database != null) {
			database.delete(ThumbsSQLiteOpenHelper.TABLE_THUMBS, null, null);
			ContentValues values = new ContentValues();
			if (USE_SHARE_FILE) {
				values.put(ThumbsSQLiteOpenHelper.COLUMN_THUMBS_IMAGE, new byte[0]);
				values.put(ThumbsSQLiteOpenHelper.COLUMN_THUMBS_NAME, SHARE_FILE_NAME);
			} else {
				values.put(ThumbsSQLiteOpenHelper.COLUMN_THUMBS_IMAGE, bytes);
				values.put(ThumbsSQLiteOpenHelper.COLUMN_THUMBS_NAME, "");
			}
			database.insert(ThumbsSQLiteOpenHelper.TABLE_THUMBS, null, values);
			return database.query(ThumbsSQLiteOpenHelper.TABLE_THUMBS, 
				new String[] {
					ThumbsSQLiteOpenHelper.COLUMN_THUMBS_IMAGE,
					ThumbsSQLiteOpenHelper.COLUMN_THUMBS_NAME,
				}, 
				null, null, null, null, null);
		}
		
    	throw new IllegalArgumentException("load failed");
	}
    
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}
	
	private Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight, boolean isUse16Bits) {
    	final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        if (D) {
        	Log.e(TAG, "options.inSampleSize == " + options.inSampleSize + "," + reqWidth + "," + reqHeight);
        }
        options.inJustDecodeBounds = false;
		if (isUse16Bits) { 
			options.inPreferredConfig = Bitmap.Config.RGB_565;   
			options.inPurgeable = true;  
			options.inInputShareable = true;  
		}
        return BitmapFactory.decodeFile(filename, options);
    }
    
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
            final float totalPixels = width * height;
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;
            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }
    
    private final static class LruThumbPool {
    	public LruCache<String, byte[]> thumbs = new LruCache<String, byte[]>(Math.round(LRU_PERCENT * Runtime.getRuntime().maxMemory() / 1024)) {
            @Override
            protected void entryRemoved(boolean evicted, String key, byte[] oldValue, byte[] newValue) {
                if (D) {
                	Log.e(TAG, "entryRemoved : " + key);
                }
            }

            @Override
            protected int sizeOf(String key, byte[] value) {
                final int bitmapSize = ((value != null) ? (value.length / 1024) : 0);
                return bitmapSize == 0 ? 1 : bitmapSize;
            }
    	};
    	
    	public void put(String fileInfo, byte[] bytes) {
    		if (fileInfo != null && bytes != null) {
    			if (D) {
    				Log.e(TAG, "thumbs size :" + thumbs.size() + " / " + thumbs.maxSize() + ", info :" + thumbs.toString());
    			}
    			thumbs.put(fileInfo, bytes);
    		}
    	}
    	
    	public byte[] get(String fileInfo) {
    		return thumbs.get(fileInfo);
    	}
    	
    	public void clear() {
    		thumbs.evictAll();
    	}
    }
}
