package fi.harism.curl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.java.sen.StringTagger;
import net.java.sen.Token;

import com.iteye.weimingtom.jkanji.BookDrawTextUtil;
import com.iteye.weimingtom.jkanji.CharTrans;
import com.iteye.weimingtom.jkanji.JkanjiAozoraService;
import com.iteye.weimingtom.jkanji.JkanjiSettingActivity;
import com.iteye.weimingtom.jkanji.PrefUtil;
import com.iteye.weimingtom.jkanji.SenProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.view.Menu;
import android.widget.Toast;

public class BookInfoUtils {
	public final static boolean USE_CONTENT_PROVIDER = true;
	
	public final static String TXT_ASSET_NAME = 
			"hashire_merosu.txt";
			//"wagahaiwa_nekodearu.txt";
	
	public final static boolean USE_ASSETS = true;
	public final static boolean USE_PARSER = true;

	public final static int CURL_TYPE_CURL = 0;
	public final static int CURL_TYPE_SIMPLE = 1;
	public final static int CURL_TYPE_CURLSIMPLE = 2;
	public final static int CURL_TYPE_VIEWPAGER = 3;
	
	public final static int PARSER_TYPE_AOZORA = 0;
	public final static int PARSER_TYPE_PLAIN = 1;
	
	public final static int SCREEN_ORI_UNDEFINED = 0;
	public final static int SCREEN_ORI_LAND = 1;
	public final static int SCREEN_ORI_PORT = 2;
	
	private static final String SHARE_PREF_NAME = "aozora_pref";
	private static final String SHARE_PREF_PAGE = "page";
	private static final String SHARE_PREF_FILENAME = "filename";
	private static final String SHARE_PREF_BG_FILENAME = "bgfilename";
	private static final String SHARE_PREF_IS_FULLSCREEN = "isFullScreen";
	private static final String SHARE_PREF_HAS_ACTIONBAR = "hasActionbar";
	private static final String SHARE_PREF_ENABLE_SEN = "enableSen";
	private static final String SHARE_PREF_CURL_TYPE = "curlType";
	private static final String SHARE_PREF_CODE_PAGE = "codePage";
	private static final String SHARE_PREF_REVERSE_DIRECTION = "reverseDirection";
	private final static String SHARE_PREF_PARSER_TYPE = "parserType";
	private final static String SHARE_PREF_IS_VERTICAL = "isVertical";
	private final static String SHARE_PREF_MASK_BG = "maskBG";
	private final static String SHARE_PREF_POSITION = "position";
	private final static String SHARE_PREF_BASE_PAGE = "basePage";
	private final static String SHARE_PREF_SCREEN_ORI = "screenOri";
	private final static String SHARE_PREF_BLACK_BACK = "blackBack";
	private final static String SHARE_PREF_USE_VOLUME_KEY = "useVolumeKey";
	
	public static final int SWITCH_BASICSTRING_ID = Menu.FIRST;
	public static final int SWITCH_SEARCH_ID = Menu.FIRST + 1;
	public static final int SWITCH_SHARE_ID = Menu.FIRST + 2;
	public static final int SWITCH_SHELF_ID = Menu.FIRST + 3;
	public static final int SWITCH_SQLITE_ID = Menu.FIRST + 4;
	public static final int SWITCH_MEMO_ID = Menu.FIRST + 5;
	public static final int SWITCH_PAGE_ID = Menu.FIRST + 6;
	public static final int SWITCH_EXIT_ID = Menu.FIRST + 7;

	public static final int DIALOG_SINGLE_CHOICE = 1;
	public static final int DIALOG_SINGLE_CHOICE_ICU4C = 2;
	public static final int DIALOG_TEXT_ENTRY = 3;
	
	public static final int MARGIN = 25;
	
	public final static String SEN_HOME = "/sen";
	
    public final static String TEST_TEXT = 
    		"(1)012345678901234567890123456789012345678901234567890123456789\n" + 
    		"(2)abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz\n" + 
    		"(3)ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ\n" + 
    		"(4)〇一二三四五六七八九〇一二三四五六七八九〇一二三四五六七八九〇一二三四五六七八九〇\n" + 
    		"(5)012345678901234567890123456789012345678901234567890123456789\n" + 
    		"(6)012345678901234567890123456789012345678901234567890123456789\n" + 
    		"(7)012345678901234567890123456789012345678901234567890123456789\n" + 
    		"(8)012345678901234567890123456789012345678901234567890123456789\n" + 
    		"(9)012345678901234567890123456789012345678901234567890123456789\n" + 
    		"(0)012345678901234567890123456789012345678901234567890123456789\n" + 
    		"(1)012345678901234567890123456789012345678901234567890123456789\n";
    
	
	public final static String EXTRA_KEY_IS_FULL_SCREEN = "fi.harism.curl.CurlActivity.isFullscreen";
	public final static String EXTRA_KEY_FILE_NAME = "fi.harism.curl.CurlActivity.fileName";
	public final static String EXTRA_KEY_ENABLE_SEN = "fi.harism.curl.CurlActivity.isEnableSen";
	public final static String EXTRA_KEY_HAS_ACTIONBAR = "fi.harism.curl.CurlActivity.hasActionbar";
	public final static String EXTRA_KEY_CURL_TYPE = "fi.harism.curl.CurlActivity.curlType";
	public final static String EXTRA_KEY_PAGE = "fi.harism.curl.CurlActivity.page";
	public final static String EXTRA_KEY_CODEPAGE = "fi.harism.curl.CurlActivity.codePage";
	public final static String EXTRA_KEY_BG_FILE_NAME = "fi.harism.curl.CurlActivity.bgFileName";
	public final static String EXTRA_KEY_REVERSE_DIRECTION = "fi.harism.curl.CurlActivity.reverseDirection";
	public final static String EXTRA_KEY_PARSER_TYPE = "fi.harism.curl.CurlActivity.parserType";
	public final static String EXTRA_KEY_IS_VERTICAL = "fi.harism.curl.CurlActivity.isVertical";
	public final static String EXTRA_KEY_MASK_BG = "fi.harism.curl.CurlActivity.maskBG";
	public final static String EXTRA_KEY_POSITION = "fi.harism.curl.CurlActivity.position";
	public final static String EXTRA_KEY_BASE_PAGE = "fi.harism.curl.CurlActivity.basePage";
	public final static String EXTRA_KEY_SCREEN_ORI = "fi.harism.curl.CurlActivity.screenOri";
	public final static String EXTRA_KEY_BLACK_BACK = "fi.harism.curl.CurlActivity.blackBack";
	public final static String EXTRA_KEY_HISTORY_ID = "fi.harism.curl.CurlActivity.historyId";
	public final static String EXTRA_KEY_USE_VOLUME_KEY = "fi.harism.curl.CurlActivity.useVolumeKey";
	public final static String EXTRA_KEY_DESC = "fi.harism.curl.CurlActivity.desc";
	
	public final static int DEFAULT_RB_SIZE = 18;
	public final static int DEFAULT_RT_SIZE = 10;
	public final static int DEFAULT_SPACE_SIZE = 2;
	
	/**
	 * use sen to analyse
	 * @param s
	 */
	public static ArrayList<BookDrawTextUtil.ExtraRubyInfo> analyze(Context context, BookDrawTextUtil.DrawTextInfo drawtextinfo) {
		ArrayList<BookDrawTextUtil.ExtraRubyInfo> result = 
				new ArrayList<BookDrawTextUtil.ExtraRubyInfo>();
		try {
			StringTagger tagger = StringTagger.getInstance();
			Token[] token = tagger.analyze(drawtextinfo.str);
			if (token != null) {
				for (int i = 0; i < token.length; i++) {
					int startPos = token[i].start();
					//int endPos = token[i].end();
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
						//FIXME:
						/*
						sb.append("(");
						sb.append(reading2);
						sb.append(")");
						*/
						BookDrawTextUtil.ExtraRubyInfo info = new BookDrawTextUtil.ExtraRubyInfo(name, reading2, startPos + drawtextinfo.startPos);
						result.add(info);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(context,
				"Error: data files in " + 
				JkanjiSettingActivity.getDataPackPath(context) + BookInfoUtils.SEN_HOME +
				" does not exist, or something goes wrong.",
				Toast.LENGTH_SHORT).show();
		}
		return result;
	}
	
    /**
     * Another version of analyze(), using content provider.
     * @param activity
     * @param query
     * @param startpos
     * @return
     */
    public static ArrayList<BookDrawTextUtil.ExtraRubyInfo> showResults(Activity activity, String query, int startpos) {
		ArrayList<BookDrawTextUtil.ExtraRubyInfo> result = 
				new ArrayList<BookDrawTextUtil.ExtraRubyInfo>();
		try {
	    	Cursor cursor = activity.managedQuery(SenProvider.CONTENT_URI_PARSER, null, null,
                    new String[] {query, Integer.toString(startpos)}, null);
			if (cursor != null) {
	        	while (cursor.moveToNext()) {
	        		String name = cursor.getString(0);
	            	String reading = cursor.getString(1);
	            	int startPos = cursor.getInt(2);
	        		result.add(new BookDrawTextUtil.ExtraRubyInfo(name, reading, startPos));
	        	}
	        }
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(activity,
				"Error: data files in " + 
				JkanjiSettingActivity.getDataPackPath(activity) + BookInfoUtils.SEN_HOME +
				" does not exist, or something goes wrong.",
				Toast.LENGTH_SHORT).show();
		}
    	return result;
    }
    
    /**
     * NOTE: 
     * if lineFirstPos == -1 && lineLastPos == -1
     * then return the words in single page, not in single line.
     * 
     * @param activity
     * @param query
     * @param startpos
     * @param pageFirstPos
     * @param pageLastPos
     * @param lineFirstPos
     * @param lineLastPos
     * @return
     */
    public static ArrayList<String> getBasicStrings(Activity activity, String query, int startpos, 
    		int pageFirstPos, int pageLastPos,
    		int lineFirstPos, int lineLastPos,
    		int positionPos, int[] outBasicIndex) {
		ArrayList<String> result = 
				new ArrayList<String>();
		try {
	    	Cursor cursor = activity.managedQuery(SenProvider.CONTENT_URI_BASICSTRING, null, null,
                    new String[] {query, Integer.toString(startpos)}, null);
			if (cursor != null) {
	        	while (cursor.moveToNext()) {
	        		//String name = cursor.getString(0);
	            	String basicString = cursor.getString(1);
	            	int startPos = cursor.getInt(2);
	        		//result.add(basicString + "," + (startPos - pageFirstPos));
	            	int rpos = startPos - pageFirstPos;
	            	int epos = pageLastPos - pageFirstPos;
	            	int wordlen = (basicString != null) ? basicString.length() : 0 ;
	            	boolean isInPage = false;
	            	boolean foundPosition = false;
	            	if (lineFirstPos < 0 && lineLastPos < 0) { // in page range
		            	if (rpos < 0 && rpos + wordlen > 0) {
		            		isInPage = true;
		            	} else if (rpos >= 0 && rpos /*<*/<= epos) {
		            		isInPage = true;
		            	}
	            	} else { // in line range
	            		int lineFirst = lineFirstPos - pageFirstPos;
	            		int lineEnd = lineLastPos - pageFirstPos;
		            	if (rpos < lineFirst && rpos + wordlen > lineFirst) {
		            		isInPage = true;
		            	} else if (rpos >= lineFirst && rpos /*<*/<= lineEnd) {
		            		isInPage = true;
		            	}
	            	}
	            	if (isInPage) {
	            		result.add(basicString);
		            	if (foundPosition == false &&
		            		positionPos >= 0 &&
		            		positionPos >= startPos && 
		            		outBasicIndex != null) {
		            		outBasicIndex[0] = result.size() - 1;
		            		foundPosition = true;
		            	}
	            	}
	        	}
	        }
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(activity,
				"Error: data files in " + 
				JkanjiSettingActivity.getDataPackPath(activity) + BookInfoUtils.SEN_HOME +
				" does not exist, or something goes wrong.",
				Toast.LENGTH_SHORT).show();
		}
    	return result;
    }
	
	
	public static String loadAssetsText(Context context, String codepage) {
		StringBuilder sb = new StringBuilder();
		InputStream instr = null;
		InputStreamReader reader = null;
		BufferedReader buffer = null;
		String str;
		try {
			instr = context.getAssets().open(BookInfoUtils.TXT_ASSET_NAME);
			reader = new InputStreamReader(instr, codepage);
			buffer = new BufferedReader(reader);
			while (null != (str = buffer.readLine())) {
				sb.append(str);
				sb.append('\n');
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
		return sb.toString();
	}
	
	public static void startForgroundService(Context context) {
		if (JkanjiSettingActivity.getAozoraService(context)) {
			context.startService(
					new Intent(context, JkanjiAozoraService.class)
						.setAction(JkanjiAozoraService.ACTION_FOREGROUND));
		}
	}
	
	public static void stopForgroundService(Context context) {
		if (JkanjiSettingActivity.getAozoraService(context)) {
			context.startService(
					new Intent(context, JkanjiAozoraService.class)
						.setAction(JkanjiAozoraService.ACTION_STOP));
		}
	}
	
    public static float getMargin(Context context) {
    	float scale = context.getResources().getDisplayMetrics().scaledDensity;
    	return BookInfoUtils.MARGIN * scale;
    }
	
    public static void setLastPage(Context context, int page) {
    	PrefUtil.putInt(context, SHARE_PREF_NAME,
    			SHARE_PREF_PAGE, 
    			page);
    }
    
    public static int getLastPage(Context context) {
		return PrefUtil.getInt(context, SHARE_PREF_NAME,
				SHARE_PREF_PAGE, 
				0);
    }

    public static void setLastFileName(Context context, String filename) {
    	PrefUtil.putString(context, SHARE_PREF_NAME,
    			SHARE_PREF_FILENAME, 
    			filename);
    }
    
    public static String getLastFileName(Context context) {
		return PrefUtil.getString(context, SHARE_PREF_NAME,
				SHARE_PREF_FILENAME, 
				"");
    }
    
    public static void setLastBGFileName(Context context, String filename) {
    	PrefUtil.putString(context, SHARE_PREF_NAME,
    			SHARE_PREF_BG_FILENAME, 
    			filename);
    }
    
    public static String getLastBGFileName(Context context) {
		return PrefUtil.getString(context, SHARE_PREF_NAME,
				SHARE_PREF_BG_FILENAME, 
				"");
    }

    public static void setLastIsFullScreen(Context context, boolean isFullScreen) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_IS_FULLSCREEN, 
    			isFullScreen);
    }
    
    public static boolean getLastIsFullScreen(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_IS_FULLSCREEN, 
				false);
    }
    
    public static void setLastHasActionbar(Context context, boolean enableSen) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_HAS_ACTIONBAR, 
    			enableSen);
    }
    
    public static boolean getLastHasActionbar(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_HAS_ACTIONBAR, 
				true);
    }
    
    public static void setLastEnableSen(Context context, boolean enableSen) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_ENABLE_SEN, 
    			enableSen);
    }
    
    public static boolean getLastEnableSen(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_ENABLE_SEN, 
				false);
    }
    
    public static void setLastCurlType(Context context, int curlType) {
    	PrefUtil.putInt(context, SHARE_PREF_NAME,
    			SHARE_PREF_CURL_TYPE, 
    			curlType);
	}
    
    public static int getLastCurlType(Context context) {
		return PrefUtil.getInt(context, SHARE_PREF_NAME,
				SHARE_PREF_CURL_TYPE, 
				0);
    }    
    
    public static void setLastCodePage(Context context, String codePage) {
    	PrefUtil.putString(context, SHARE_PREF_NAME,
    			SHARE_PREF_CODE_PAGE, 
    			codePage);
	}
    
    public static String getLastCodePage(Context context) {
		return PrefUtil.getString(context, SHARE_PREF_NAME,
				SHARE_PREF_CODE_PAGE, 
				"shift-jis");
    }
    
    public static void setLastReverseDirection(Context context, boolean reverseDirection) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_REVERSE_DIRECTION, 
    			reverseDirection);
    }
    
    public static boolean getLastReverseDirection(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_REVERSE_DIRECTION, 
				false);
    }  
    
    public static void setLastParserType(Context context, int parserType) {
    	PrefUtil.putInt(context, SHARE_PREF_NAME,
    			SHARE_PREF_PARSER_TYPE, 
    			parserType);
    }
    
    public static int getLastParserType(Context context) {
		return PrefUtil.getInt(context, SHARE_PREF_NAME,
				SHARE_PREF_PARSER_TYPE, 
				PARSER_TYPE_AOZORA);
    }  
    
    public static void setLastIsVertical(Context context, boolean isVertical) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_IS_VERTICAL, 
    			isVertical);
    }
    
    public static boolean getLastIsVertical(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_IS_VERTICAL, 
				false);
    } 

    public static void setLastMaskBG(Context context, boolean maskBG) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_MASK_BG, 
    			maskBG);
    }
    
    public static boolean getLastMaskBG(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_MASK_BG, 
				true);
    } 
    
    public static void setLastPosition(Context context, int position) {
    	PrefUtil.putInt(context, SHARE_PREF_NAME,
    			SHARE_PREF_POSITION, 
    			position);
    }
    
    public static int getLastPosition(Context context) {
		return PrefUtil.getInt(context, SHARE_PREF_NAME,
				SHARE_PREF_POSITION, 
				0);
    }
    
    public static void setLastBasePage(Context context, boolean basePage) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_BASE_PAGE, 
    			basePage);
    }
    
    public static boolean getLastBasePage(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_BASE_PAGE, 
				true);
    } 
    
    public static void setLastScreenOri(Context context, int type) {
    	PrefUtil.putInt(context, SHARE_PREF_NAME,
    			SHARE_PREF_SCREEN_ORI, 
    			type);
    }
    
    public static int getLastScreenOri(Context context) {
		return PrefUtil.getInt(context, SHARE_PREF_NAME,
				SHARE_PREF_SCREEN_ORI, 
				0);
    } 
    
    public static void setLastBlackBack(Context context, boolean blackBack) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_BLACK_BACK, 
    			blackBack);
    }
    
    public static boolean getLastBlackBack(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_BLACK_BACK, 
				false);
    } 
    
    public static void setLastUseVolumeKey(Context context, boolean useVolumeKey) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_USE_VOLUME_KEY, 
				useVolumeKey);
    }
    
    public static boolean getLastUseVolumeKey(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_USE_VOLUME_KEY, 
				true);
    }
}
