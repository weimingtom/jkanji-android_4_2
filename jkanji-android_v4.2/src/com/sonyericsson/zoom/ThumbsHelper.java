package com.sonyericsson.zoom;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ThumbsHelper {
	private final static boolean D = false;
	private final static String TAG = "ThumbsHelper";
	
	public static Bitmap decode(Activity act, String filename, int reqWidth, int reqHeight, boolean isUse16Bits, boolean isSample) {
    	Bitmap bitmap = null;
    	try {
			Cursor cursor = act.managedQuery(ThumbsProvider.CONTENT_URI_PARSER, null, null,
	                new String[] {filename, 
					Integer.toString(reqWidth), 
					Integer.toString(reqHeight), 
					Boolean.toString(isUse16Bits),
					Boolean.toString(isSample)}, 
					null);
	    	if (cursor != null) {
	        	if (cursor.moveToNext()) {
	        		byte[] bytes = cursor.getBlob(0);
	        		String name = cursor.getString(1);
	        		if (D) {
	        			Log.e(TAG, "bytes == " + (bytes != null ? bytes.length : 0) + ", name == " + name);
	        		}
	        		if (name != null && name.length() > 0) {
	        			InputStream istr = null;
	        			try {
		        			istr = act.openFileInput(name);
		        			if (istr != null) {
		        				bitmap = BitmapFactory.decodeStream(istr);
		        			}
		        		} catch (IOException e) {
	        				e.printStackTrace();
	        			} finally {
	        				if (istr != null) {
	        					try {
	        						istr.close();
	        					} catch (IOException e) {
	        						e.printStackTrace();
	        					}
	        				}
	        			}
//	        			act.deleteFile(name);
	        		} else {
		        		if (bytes != null) {
			        		bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		        		}
	        		}
	        	}
	        }
    	} catch (Throwable e) {
    		e.printStackTrace();
    	}
		return bitmap;
	}
	
	public static void clear(Activity act) {
    	try {
			Cursor cursor = act.managedQuery(ThumbsProvider.CONTENT_URI_CLEAR, null, null,
	                null, 
					null);
	    	if (cursor != null) {
	        	if (cursor.moveToNext()) {
	        		
	        	}
	        }
    	} catch (Throwable e) {
    		e.printStackTrace();
    	}
	}
}
