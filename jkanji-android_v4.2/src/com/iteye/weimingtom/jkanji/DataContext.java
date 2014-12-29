package com.iteye.weimingtom.jkanji;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * !!!NOTE!!! 只可以放置原始Java类型或常量，
 * 不要放入任何与Context有关的数据以避免内存泄露（只在OnCreate()中初始化）。
 * @author weimingtom
 *
 */
public class DataContext {
	//FIXME: MUST match output_overflow_text
	public static final int TEXT_LINE_MAX = 300;

	//FIXME: SHOULD BE false, see loadData()
	public static final boolean DONNOT_LOAD_DIC = false;
	//FIXME: SHOULD BE false, see loadDict()
	public static final boolean ONLY_LOAD_TEST = false;
	public static final String DATA_FILE_NAMES;
	public static final String INDEX_FILE_NAMES;
	public static final String DATA0_FILE_NAMES;
	public static final String INDEX0_FILE_NAMES;
	public static final String DATA1_FILE_NAMES;
	public static final String INDEX1_FILE_NAMES;
	public static final String DATA2_FILE_NAMES; //PLUGIN
	public static final String INDEX2_FILE_NAMES;
	
	public static final boolean DONNOT_LOAD_PAC = false;
	public static final boolean DONNOT_LOAD_DICT = false;
	public static final boolean DONNOT_LOAD_WORDS = false;
	public static final boolean DONNOT_LOAD_ENWORDS = false;//PLUGIN
	public static final boolean DONNOT_LOAD_GB2SJ = false;
	public static final boolean DONNOT_LOAD_TYPEFACE = false;
	
	public static final boolean USE_SEPARATE_PACK = true;
	
	public static final String[] DIGUST_FILE_NAMES = {
		"digust_1.html",
		"digust_2.html",
		"digust_3.txt",
		"digust_4.dummy",
		"digust_5.dummy",
		"digust_6.dummy",
		"digust_7.dummy",
		"digust_8.dummy",
		"digust_9.dummy",
		"digust_10.dummy",
		"digust_11.dummy",
		"digust_12.dummy",
	};
	
	//这个变量无效
	public static final String[] DIGUST_TITLES = {
		"日语口语型",
		"常用漢字表",
		"小春音（需要数据包）",
		"sen日语发音标注（需数据包）",
		"双列csv查看器",
		"ssa字幕播放器",
		"sqlite搜索器（需数据包）",
		"日语学习用网址",
		"小游戏（游魂quiz改）",
		"历史与收藏夹",
		"青空文库阅读器",
		"epwing搜索器（需数据包，目前仅支持DreyeJC中日日中辞書）",
		"旧版主菜单（2.x）",
		"旧版帮助（2.x）",
		"神経衰弱",
	};
	
	static {
		if (ONLY_LOAD_TEST) {
			DATA_FILE_NAMES = "dict_test.pac";
			INDEX_FILE_NAMES = "dict_test.idx";
			DATA0_FILE_NAMES = "dict_test_0.pac";
			INDEX0_FILE_NAMES = "dict_test_0.idx";
			DATA1_FILE_NAMES = "dict_test_1.pac";
			INDEX1_FILE_NAMES = "dict_test_1.idx";
			DATA2_FILE_NAMES = "dict_test_2.pac"; //PLUGIN
			INDEX2_FILE_NAMES = "dict_test_2.idx";
		} else {
			DATA_FILE_NAMES = "dict.pac";
			INDEX_FILE_NAMES = "dict.idx";
			DATA0_FILE_NAMES = "dict_0.pac";
			INDEX0_FILE_NAMES = "dict_0.idx";
			DATA1_FILE_NAMES = "dict_1.pac";
			INDEX1_FILE_NAMES = "dict_1.idx";
			DATA2_FILE_NAMES = "dict_2.pac"; //PLUGIN
			INDEX2_FILE_NAMES = "dict_2.idx";
		}
	}
	
	public List<Word> words;
	public List<Word> enwords; //PLUGIN
	public ArrayList<Word> resultWords;
	public Map<Character, Character> gb2sj;
	public boolean isKeyboardChecked = false;
	
	public PackFileReadTask.SessionSaveData data;
	
	public PackFileReadTask.SessionSaveData data0;
	public PackFileReadTask.SessionSaveData data1;
	public PackFileReadTask.SessionSaveData data2; //PLUGIN
	
	public boolean isLoadFinish = false;
	
	public final static String HISTORY_FILENAME = "history.txt";
	public final static String FAVOURITE_FILENAME = "favorite.txt";
	public final static String WEB_FAVOURITE_FILENAME = "webfavorite.txt";
	
	public boolean isPreSearch = false;
	
	public final static int DICT_SERVICE_ID = 1;
	public final static int SEN_SERVICE_ID = 2;
	public final static int AOZORA_SERVICE_ID = 3;
}
