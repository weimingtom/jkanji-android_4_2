package com.iteye.weimingtom.jkanji;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;

public class SenAutoWrapViewGroup extends AutoWrapViewGroup {
	private final static boolean D = false;
	private final static String TAG = "SenAutoWrapViewGroup";
	
	public SenAutoWrapViewGroup(Context context) {
		super(context);
	}

	public SenAutoWrapViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
    public void outputMultiLine(Activity activity, String inputStr) {
    	clearViews();
    	int startPos = 0;
    	while (true) {
	    	int pos = inputStr.indexOf("\n", startPos);
	    	if (pos == -1) {
	    		String str = inputStr.substring(startPos);
	    		if (!str.equals("\n")) {
	    			outputSingleLine(activity, str);
	    		} else {
	    			this.output("", "");
	    		}
	    		break;
	    	} else {
	    		String str = inputStr.substring(startPos, pos);
	    		if (!str.equals("\n")) {
	    			outputSingleLine(activity, str);
	    		}
	    		this.output("", "");
	    		startPos = pos + 1;
	    	}
    	}
    	this.requestLayout();
    }
    
    private void outputSingleLine(Activity activity, String inputStr) {
    	if (D) {
    		Log.e(TAG, "outputSingleLine : " + inputStr);
    	}
    	if (D) {
    		if (inputStr.contains("\n")) {
    			Log.e(TAG, "contains wrap");
    		}
    	}
    	if (inputStr == null) {
    		return;
    	}
    	ArrayList<RbInfo> info = splitRuby(activity, inputStr, 0);
    	for (int i = 0; i < info.size() + 1; i++) {
    		int lastEndPos = 0;
    		if (i > 0) {
    			lastEndPos = (info.get(i - 1).pos + info.get(i - 1).rt.length());
    		}
    		int curPos = inputStr.length();
    		if (i < info.size()) {
    			curPos = info.get(i).pos;
    		}
			if (lastEndPos < curPos) {
				String str = inputStr.substring(lastEndPos, curPos);
				if (D) {
					Log.d(TAG, "strstr");
				}
				if (str != null) {
					for (int j = 0; j < str.length(); j++) {
						String rtstr = str.substring(j, j + 1);
						this.output(rtstr, rtstr);
					}
				}
			}
			if (D) {
				Log.d(TAG, "rtrb");
			}
			if (i < info.size() && info.get(i) != null) {
				String rb = info.get(i).rb;
				String rt = info.get(i).rt;
				if (rb == null || 
					rb.length() == 0 ||
					(rb != null && rt != null && rb.equals(rt))) {
					if (rt != null) {
						for (int j = 0; j < rt.length(); j++) {
							this.output(rt.substring(j, j + 1), "");
						}
					}
				} else {
					this.output(rt, rb);
				}
			}
    	}
    }
    
    private final class RbInfo {
    	String rt;
    	String rb;
    	int pos;
    	
    	public RbInfo(String rt, String rb, int pos) {
    		this.rt = rt;
    		this.rb = rb;
    		this.pos = pos;
    	}
    }
    
    private ArrayList<RbInfo> splitRuby(Activity activity, String query, int startpos) {
    	//final String AUTHORITY = "com.iteye.weimingtom.jkanji.SenProvider";
        //final Uri CONTENT_URI_PARSER = Uri.parse("content://" + AUTHORITY + "/parser");
        
    	final Uri CONTENT_URI_PARSER = SenProvider.CONTENT_URI_PARSER;
    	
        ArrayList<RbInfo> result = new ArrayList<RbInfo>();
        
    	Cursor cursor = activity.managedQuery(CONTENT_URI_PARSER, null, null,
                new String[] {query, Integer.toString(startpos)}, null);
		if (cursor != null) {
        	while (cursor.moveToNext()) {
        		String name = cursor.getString(0);
            	String reading = cursor.getString(1);
            	int startPos = cursor.getInt(2);
            	if (D) {
            		Log.e(TAG, "splitRuby : " + name + "," + reading);
            	}
        		result.add(new RbInfo(name, reading, startPos));
        	}
        }
		return result;
    }
}
