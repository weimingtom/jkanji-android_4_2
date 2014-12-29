package com.iteye.weimingtom.jkanji;

import net.java.sen.StringTagger;
import net.java.sen.Token;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

public class SenProvider extends ContentProvider {
    private static final boolean D = false;
    private static final String TAG = "SenProvider";
	
	public static final String PARSER_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.iteye.weimingtom.jkanji";
	public static final String BASICSTRING_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.iteye.weimingtom.jkanji";
	public static final String PARSERSINGLE_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
            "/vnd.iteye.weimingtom.jkanji"; 
	
    public final static String AUTHORITY = "com.iteye.weimingtom.jkanji.SenProvider";
    
    public static final Uri CONTENT_URI_PARSER = Uri.parse("content://" + AUTHORITY + "/parser");
    public static final Uri CONTENT_URI_BASICSTRING = Uri.parse("content://" + AUTHORITY + "/basicstring");
    public static final Uri CONTENT_URI_PARSERSINGLE = Uri.parse("content://" + AUTHORITY + "/parsersingle");
    
    private static final int PARSER = 0;
    private static final int BASICSTRING = 1;
    private static final int PARSERSINGLE = 2;
    private static final UriMatcher sURIMatcher = buildUriMatcher();
    
    //sen
	private final static String SEN_HOME = "/sen";

	private static Object objLock = new Object();
	
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, "parser", PARSER);
        matcher.addURI(AUTHORITY, "basicstring", BASICSTRING);
        matcher.addURI(AUTHORITY, "parsersingle", PARSERSINGLE);
        return matcher;
    }
    
	private final static String[] COLUMNNAMES = new String[] {
		"name",
		"reading",
		"startPos",
	};

	@Override
	public boolean onCreate() {
		boolean result = true;
		if (D) {
			Log.d(TAG, "SenProvider onCreate");
		}
		System.setProperty("sen.home", JkanjiSettingActivity.getDataPackPath(this.getContext()) + SEN_HOME);
		if (false) {
			try {
				StringTagger tagger = StringTagger.getInstance();
			} catch (Throwable e) {
				e.printStackTrace();
				result = false;
			}
		}
		return result;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}


	@Override
	public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
        case PARSER:
            return PARSER_MIME_TYPE;
		
        case BASICSTRING:
        	return BASICSTRING_MIME_TYPE;

        case PARSERSINGLE:
        	return PARSERSINGLE_MIME_TYPE;
        	
        default:
        	throw new IllegalArgumentException("Unknown URL " + uri);
        }
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		String arg0, arg1;
		if (D) {
			Log.d(TAG, "SenProvider query : " + uri);
		}
		switch (sURIMatcher.match(uri)) {
        case PARSER:
            if (selectionArgs == null || selectionArgs.length < 2) {
            	throw new IllegalArgumentException(
            			"selectionArgs must be provided for the Uri: " + uri);
            }
            arg0 = selectionArgs[0];
            arg1 = selectionArgs[1];
            if (arg0 == null) {
            	arg0 = "";
            }
            if (arg1 == null) {
            	arg1 = "";
            }
            return getParse(arg0, arg1);
            
        case BASICSTRING:
            if (selectionArgs == null || selectionArgs.length < 2) {
                throw new IllegalArgumentException(
                		"selectionArgs must be provided for the Uri: " + uri);
            }
            arg0 = selectionArgs[0];
            arg1 = selectionArgs[1];
            if (arg0 == null) {
            	arg0 = "";
            }
            if (arg1 == null) {
            	arg1 = "";
            }
            return getBasicString(arg0, arg1);  
            
        case PARSERSINGLE:
            if (selectionArgs == null || selectionArgs.length < 2) {
            	throw new IllegalArgumentException(
            			"selectionArgs must be provided for the Uri: " + uri);
            }
            arg0 = selectionArgs[0];
            arg1 = selectionArgs[1];
            if (arg0 == null) {
            	arg0 = "";
            }
            if (arg1 == null) {
            	arg1 = "";
            }
            return getParseSingle(arg0, arg1);
            
        default:
            throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
	}
	
    private Cursor getParse(String str, String startpos) {
        return analyze(str, Integer.parseInt(startpos));
    }
    
    private Cursor getBasicString(String str, String startpos) {
        return basicString(str, Integer.parseInt(startpos));
    }
    
    private Cursor getParseSingle(String str, String startpos) {
        return analyzeSingle(str, Integer.parseInt(startpos));
    }
    
	private Cursor analyze(String str, int startpos) {
		MatrixCursor result = new MatrixCursor(COLUMNNAMES);
		try {
			StringTagger tagger = StringTagger.getInstance();
			Token[] token = tagger.analyze(str);
			if (token != null) {
				for (int i = 0; i < token.length; i++) {
					int startPos = token[i].start();
					String name = token[i].toString();
					String reading = token[i].getReading();
					String reading2 = null;
					if (reading != null) {
						reading2 = CharTrans.zenkakuHiraganaToZenkakuKatakana(reading);
					} else {
						reading2 = "";
					}
					if (reading != null && 
						reading.length() > 0 && 
						!reading.equals(name) &&
						!reading2.equals(name)) {
						result.addRow(new Object[]{
							name, reading2, startPos + startpos
						});
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private Cursor basicString(String str, int startpos) {
		MatrixCursor result = new MatrixCursor(COLUMNNAMES);
		try {
			StringTagger tagger = StringTagger.getInstance();
			Token[] token = tagger.analyze(str);
			if (token != null) {
				for (int i = 0; i < token.length; i++) {
					int startPos = token[i].start();
					String name = token[i].toString();
					String basicString = token[i].getBasicString();
					result.addRow(new Object[]{
						name, basicString, startPos + startpos
					});
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private Cursor analyzeSingle(String str, int parseSingleType) {
		MatrixCursor result = new MatrixCursor(COLUMNNAMES);
		StringBuffer sb = new StringBuffer();
		try {
			StringTagger tagger = StringTagger.getInstance();
			Token[] token = tagger.analyze(str);
			if (token != null) {
				for (int i = 0; i < token.length; i++) {
					switch (parseSingleType) {
					case 0: 
						if (token[i] != null) {
							String name = token[i].toString();
							String reading = token[i].getReading();
							String reading2 = null;
							if (reading != null) {
								reading2 = CharTrans.zenkakuHiraganaToZenkakuKatakana(reading);
							} else {
								reading2 = "";
							}
							sb.append(name);
							if (reading != null && 
								reading.length() > 0 && 
								!reading.equals(name) &&
								!reading2.equals(name)) {
								sb.append("(");
								sb.append(reading2);
								sb.append(")");
							}
						}
						break;
					
					case 1:
						if (token[i] != null) {
							String name = token[i].toString();
							String termInfo = token[i].getTermInfo();
							if (termInfo == null) {
								termInfo = "";
							}
							sb.append(name);
							sb.append(" => "); 
							sb.append(termInfo);
							sb.append("\n");
						}
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		result.addRow(new Object[]{
				str, sb.toString(), parseSingleType
			});
		return result;
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
}
