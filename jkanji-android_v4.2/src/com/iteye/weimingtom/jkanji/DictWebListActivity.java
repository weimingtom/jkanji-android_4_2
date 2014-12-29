package com.iteye.weimingtom.jkanji;

import java.util.ArrayList;
import java.util.List;

import com.elgubbo.sharetoclipboard.ShareToClipboardActivity;
import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.jkanji.JkanjiWebsiteActivity.WordRecord;

import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * @author Administrator
 *
 */
public class DictWebListActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "DictWebListActivity";
	
	public static final String EXTRA_KEY = "com.iteye.weimingtom.jkanji.DictWebListActivity";
	public static final String EXTRA_KEY_SHARE = "com.iteye.weimingtom.jkanji.DictWebListActivity.share";
	
	private static final String SHARE_PREF_NAME = "pref";
	private static final String SHARE_PREF_SHOW_WEB_FAV = "showWebFav";
	private static final String SHARE_PREF_SHOW_WEB_CFG = "showWebCfg";
	private static final String SHARE_PREF_WEB_LIST_TYPE = "webListType";
	
	private ActionBar actionBar;
	private ListView listViewWebSearch;
	private List<WordRecord> records;
	private EfficientAdapter adapter;
	private ArrayList<Integer> favIds;
	private ArrayList<Integer> listIds;
	
	private LinearLayout linearLayoutConfig;
	private CheckBox checkBoxMod;
	private RadioButton radioButtonList, radioButtonFav;
	
	
	private final static int TYPE_ALL = 0;
	private final static int TYPE_LOCAL = 1;
	private final static int TYPE_JC = 2;
	private final static int TYPE_CJ = 3;
	private final static int TYPE_JJ = 4;
	private final static int TYPE_CC = 5;
	private final static int TYPE_TTS = 6;
	private final static int TYPE_JE = 7;
	private final static int TYPE_EJ = 8;
	private final static int TYPE_EE = 9;
	private final static int TYPE_ACG = 10;
	private final static int TYPE_SOUND = 11;
	private final static int TYPE_EC = 12;
	private final static int TYPE_CE = 13;
	
	private static final String[] TYPE_NAME = {
		"全部", // 0
		"本地", // 1
		"日中", // 2
		"中日", // 3
		"日日", // 4
		"中中", // 5
		"TTS", // 6
		"日英", // 7
		"英日", // 8
		"英英", // 9
		"ACG", // 10
		"标注", //11
		"英中", //12
		"中英", //13
	};
	//titles && bases
	private static final WordRecord[] sites = {
		//共享与本地工具
		new WordRecord("共享", "印象笔记，谷歌翻译", TYPE_LOCAL), // 0
		new WordRecord("SQLite搜索器", "内部功能（需数据包）", TYPE_LOCAL), // 1
		new WordRecord("SQLite日英例句搜索器", "内部功能（需数据包）", TYPE_LOCAL), // 2
		new WordRecord("复制至剪贴板", "SeederDict", TYPE_LOCAL), // 3
		new WordRecord("全局搜索", "谷歌搜索，ColorDict，MDict", TYPE_LOCAL),  // 4
		new WordRecord("弹出ColorDict", "ColorDict", TYPE_LOCAL), // 5
		new WordRecord("epwing搜索器", "内部功能（需数据包，目前仅测试支持広辞苑5和DreyeJC中日日中辞書）", TYPE_LOCAL), // 6
		new WordRecord("共享关键词", "SeederDict", TYPE_LOCAL), // 7
		new WordRecord("搜索结果编辑、系统/数据包TTS发音", "内部功能，系统TTS发音功能需要安装支持日文的TTS引擎（如N2TTS）", TYPE_LOCAL), // 8
		new WordRecord("备忘录", "内部功能（数据在卸载后将消失，请勿存放重要数据，建议共享至印象笔记或其它云笔记服务）", TYPE_LOCAL), // 9
		
		new WordRecord("HJdict（沪江小d,日中）", "http://dict.hjenglish.com/jp/jc/%s", TYPE_JC),
		new WordRecord("excite (ja beginswith)", "http://www.excite.co.jp/dictionary/japanese/?match=beginswith&search=%s", TYPE_JJ),
		new WordRecord("excite (ja contains)", "http://www.excite.co.jp/dictionary/japanese/?match=contains&search=%s", TYPE_JJ),
		new WordRecord("excite (ja_ch beginswith)", "http://www.excite.co.jp/dictionary/japanese_chinese/?match=beginswith&search=%s", TYPE_JC),
		new WordRecord("excite (ja_ch contains)", "http://www.excite.co.jp/dictionary/japanese_chinese/?match=contains&search=%s", TYPE_JC),
		new WordRecord("ctrans (jp)", "http://www.ctrans.org/search.php?word=%s&opts=jp", TYPE_JC),
		new WordRecord("ctrans (ch)", "http://www.ctrans.org/search.php?word=%s&opts=ch", TYPE_CJ),
		new WordRecord("glosbe (ja-zh)", "http://ja.glosbe.com/ja/zh/%s", TYPE_JC),
		new WordRecord("glosbe (zh-ja)", "http://ja.glosbe.com/zh/ja/%s", TYPE_CJ),
		new WordRecord("wikipedia mobile (ja)", "http://ja.m.wikipedia.org/wiki/%s", TYPE_JJ), 
		new WordRecord("wikipedia (ja)", "http://ja.wikipedia.org/wiki/%s", TYPE_JJ),
		new WordRecord("wikipedia (zh)", "http://zh.wikipedia.org/wiki/%s", TYPE_CC),
		new WordRecord("wiktionary (ja)", "http://ja.wiktionary.org/wiki/%s", TYPE_JJ),
		new WordRecord("wiktionary (zh)", "http://zh.wiktionary.org/wiki/%s", TYPE_CC),
		new WordRecord("gimite JaTTS (male01)", "http://gimite.net/speech?format=wav&speaker=male01&text=%s", TYPE_TTS),
		new WordRecord("gimite JaTTS (female01)", "http://gimite.net/speech?format=wav&speaker=female01&text=%s", TYPE_TTS),
		new WordRecord("gimite JaTTS (male02)", "http://gimite.net/speech?format=wav&speaker=male02&text=%s", TYPE_TTS),
		new WordRecord("weblio", "http://ejje.weblio.jp/content/%s", TYPE_JE), // or EJ
		new WordRecord("goo", "http://dictionary.goo.ne.jp/srch/all/%s/m0u/", TYPE_JJ), // or other
		new WordRecord("kotobank.jp", "http://m.kotobank.jp/word/%s", TYPE_JJ),
		new WordRecord("ALC", "http://eow.alc.co.jp/search?q=%s", TYPE_EJ), //???
		new WordRecord("Google", "http://translate.google.com/translate_t?ie=UTF8&langpair=ja|zh-CN&text=%s", TYPE_JC), //???
		new WordRecord("Google mobile", "http://translate.google.com/m/translate?ie=UTF8&langpair=ja|zh-CN&twu=1&q=%s&hl=zh-CN&vi=m&sl=ja&tl=zh-CN", TYPE_JC), //???
		new WordRecord("Yahoo!", "http://honyaku.yahoo.co.jp/transtext?both=TH&eid=CR-JC-CN&text=%s", TYPE_JC), //???
		new WordRecord("moegirl wiki", "http://wiki.moegirl.org/%s", TYPE_ACG), 
		new WordRecord("pixiv dic (ja)", "http://dic.pixiv.net/search?query=%s", TYPE_ACG), 
		new WordRecord("pixiv dic (en)", "http://en.dic.pixiv.net/search?query=%s", TYPE_ACG), 
		new WordRecord("百度百科（搜索词条）", "http://baike.baidu.com/search/word?word=%s&enc=utf8", TYPE_CC),
		new WordRecord("百度百科（进入词条）", "http://baike.baidu.com/search/word?word=%s&pic=1&sug=1&enc=utf8", TYPE_CC),
		new WordRecord("Hatena::Keyword", "http://d.hatena.ne.jp/keyword/%s", TYPE_JJ),
		new WordRecord("中国語辞書 - BitEx中国語", "http://bitex-cn.com/search_result.php?keywords=%s", TYPE_CJ),
		new WordRecord("animecharactersdatabase.com", "http://www.animecharactersdatabase.com/jp/find.php?search=%s&searchin=c", TYPE_ACG),
		new WordRecord("日本語俗語辞書", "http://search.zokugo-dict.com/search.cgi?charset=utf8&q=%s", TYPE_JJ),
		new WordRecord("bab.la Dictionary", "http://en.bab.la/dictionary/japanese-english/%s", TYPE_JE),
		new WordRecord("Tangorin Japanese Dictionary", "http://tangorin.com/#general/%s", TYPE_JE), //EJ
		new WordRecord("隠語・誘導語データベース", "http://kkyg.jp/search/?keyword=%s", TYPE_JJ),
		new WordRecord("有道词典（汉日互译）", "http://dict.youdao.com/search?le=jap&q=%s", TYPE_JC), //CJ
		new WordRecord("YOMOYOMO - Nihongo wo yomou!", "http://yomoyomo.jp/text.php?inputtext=%s", TYPE_SOUND), //???
		new WordRecord("Yahoo!辞書", "http://dic.search.yahoo.co.jp/search?ei=UTF-8&p=%s", TYPE_JJ), //EJ/JE
		new WordRecord("StarDict", "http://www.stardict.cn/query.php?q=%s", TYPE_JC), //CJ
		new WordRecord("Japanese Kanji Dictionary", "http://www.saiga-jp.com/cgi-bin/dic.cgi?m=search&sc=0&f=0&j=%s", TYPE_JE),
		new WordRecord("萌否电台", "http://moe.fm/search?q=%s", TYPE_ACG),  
		new WordRecord("ゴガクル - フレーズ、例文、表現", "http://gogakuru.com/chinese/phrase/keyword/%s.html?condMovie=0", TYPE_CJ), //JC?
		new WordRecord("Tatoeba project", "http://tatoeba.org/chi/sentences/search?query=%s&from=jpn&to=cmn", TYPE_JC), //JE?
		new WordRecord("谷歌搜索", "https://www.google.com.hk/m?hl=zh-CN&gl=cn&source=android-unknown&q=%s", TYPE_CC), 
		new WordRecord("三省堂 (设定词典方可用)(前方)", "http://www.sanseido.net/sp/Search?target_words=%s&search_type=0&start_index=0&selected_dic=", TYPE_JJ),
		new WordRecord("三省堂 (完全)", "http://www.sanseido.net/sp/Search?target_words=%s&search_type=1&start_index=0&selected_dic=", TYPE_JJ),
		new WordRecord("三省堂 (后方)", "http://www.sanseido.net/sp/Search?target_words=%s&search_type=2&start_index=0&selected_dic=", TYPE_JJ),
		new WordRecord("三省堂 (部分)", "http://www.sanseido.net/sp/Search?target_words=%s&search_type=5&start_index=0&selected_dic=", TYPE_JJ),
		new WordRecord("三省堂 (全文)", "http://www.sanseido.net/sp/Search?target_words=%s&search_type=3&start_index=0&selected_dic=", TYPE_JJ),
		new WordRecord("BIGLOBE(前方一致)", "http://jisyo.search.biglobe.ne.jp/cgi-bin/sp/search_key_sp?q=%s&ej=1&je=1&jj=1&type=0&ie=utf8", TYPE_JJ), //JE, EJ
		new WordRecord("BIGLOBE(后方一致)", "http://jisyo.search.biglobe.ne.jp/cgi-bin/sp/search_key_sp?q=%s&ej=1&je=1&jj=1&type=1&ie=utf8", TYPE_JJ), //JE, EJ
		new WordRecord("BIGLOBE(完全一致)", "http://jisyo.search.biglobe.ne.jp/cgi-bin/sp/search_key_sp?q=%s&ej=1&je=1&jj=1&type=2&ie=utf8", TYPE_JJ), //JE, EJ
		new WordRecord("海词词典(dict.cn,英汉)", "http://dict.cn/mini.php?q=%s", TYPE_EC),
		new WordRecord("爱词霸(iciba,英汉)", "http://www.iciba.com/%s", TYPE_EC),
		new WordRecord("dreye(简，英汉)", "http://www.dreye.com.cn/mws/dict.php?w=%s&hidden_codepage=01&ua=dc_cont&project=nd", TYPE_EC),
		new WordRecord("dreye(繁，英汉)", "http://www.dreye.com.tw/mws/dict.php?w=%s&hidden_codepage=01&ua=dc_cont&project=nd", TYPE_EC),
		new WordRecord("词典网(日汉词典)", "http://www.cidianwang.com/search/jp/?q=%s&y=1", TYPE_JC), 
		new WordRecord("词典网(汉日词典)", "http://www.cidianwang.com/search/jp/?q=%s&y=0", TYPE_CJ), 
		new WordRecord("NAVER中国語辞書", "http://cndic.naver.jp/srch/all/1/%s", TYPE_CJ), 
		new WordRecord("Google ニュース検索", "https://www.google.co.jp/m/search?tbm=nws&hl=ja&q=%s", TYPE_JJ), 
		new WordRecord("Google 画像検索", "https://www.google.co.jp/m/search?tbm=isch&hl=ja&q=%s", TYPE_JJ), 
		new WordRecord("HJdict（沪江小d,中日）", "http://dict.hjenglish.com/jp/cj/%s", TYPE_CJ),
		new WordRecord("Weblio日中中日辞典", "http://cjjc.weblio.jp/content/%s", TYPE_JC), //CJ
		new WordRecord("excite (ch_ja beginswith)", "http://www.excite.co.jp/dictionary/japanese_chinese/?match=beginswith&search=%s", TYPE_CJ), 
		new WordRecord("excite (ch_ja contains)", "http://www.excite.co.jp/dictionary/japanese_chinese/?match=contains&search=%s", TYPE_CJ),
		new WordRecord("Google日文翻译TTS", "http://translate.google.co.jp/translate_tts?ie=UTF-8&q=%s&tl=ja", TYPE_TTS), 
		new WordRecord("百度词典", "http://dict.baidu.com/s?wd=%s", TYPE_CC), 
		new WordRecord("百度英语论文写作助手(百度翻译例句)", "http://fanyi.baidu.com/writing/s?query=%s", TYPE_EC), //CE
		new WordRecord("Google日文翻译TTS(https协议)", "https://translate.google.co.jp/translate_tts?ie=UTF-8&q=%s&tl=ja", TYPE_JC), 
		new WordRecord("hbksugar google tts下载器", "HBKSugar TTS", TYPE_LOCAL), 
		new WordRecord("浩叡日中词典", "内部功能（需数据包）", TYPE_LOCAL),
		new WordRecord("MDict", "支持mdx词典搜索，如新日漢大辭典（请先关闭MDict的自动剪贴板功能）", TYPE_LOCAL),
		new WordRecord("SeederDict", "支持多种词典格式搜索", TYPE_LOCAL),
		new WordRecord("Google define", "https://www.google.com.hk/m/search?q=define:%s", TYPE_CC), //???
		new WordRecord("Universal dictionary", "http://www.dicts.info/ud.php?w=%s&l1=Japanese", TYPE_JE), //???
		new WordRecord("Denshi Jisho (Words, Edict)", "http://jisho.org/words?jap=%s&eng=&dict=edic", TYPE_JE),
		new WordRecord("Denshi Jisho (Words, Compdic)", "http://jisho.org/words?jap=%s&eng=&dict=compdic", TYPE_JE),
		new WordRecord("Denshi Jisho (Words, Engscidic)", "http://jisho.org/words?jap=%s&eng=&dict=engscidic", TYPE_JE),
		new WordRecord("Denshi Jisho (Words, JMnedict)", "http://jisho.org/words?jap=%s&eng=&dict=enamdic", TYPE_JE),
		new WordRecord("Denshi Jisho (Sentences)", "http://jisho.org/sentences?jap=%s&eng=", TYPE_JE),
		new WordRecord("OLDict.com", "http://zh-cn.oldict.com/%s/", TYPE_JC),  //???
		new WordRecord("文国词霸", "http://dict.wenguo.com/mini/jp/w/%s", TYPE_JC),
		new WordRecord("weblio日日", "http://www.weblio.jp/content/%s", TYPE_JJ), // or EJ
	};
	
	public static final String[] getWebSearchTitles() {
		String[] titles = new String[sites.length];
		for (int i = 0; i < sites.length; i++) {
			titles[i] = sites[i].word;
		}
		return titles;
	}
	
	private String searchString;
	private String shareString;
	
	private Spinner spinnerSearchType;
	private ArrayAdapter<String> spinnerSearchTypeAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.web_search);
        
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("web搜索器");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.search_web;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.config;
			}

			@Override
			public void performAction(View view) {
				if (linearLayoutConfig.getVisibility() == LinearLayout.VISIBLE) {
					linearLayoutConfig.setVisibility(LinearLayout.GONE);
					setLastShowWebCfg(false);
				} else {
					linearLayoutConfig.setVisibility(LinearLayout.VISIBLE);
					setLastShowWebCfg(true);
				}
			}
        });
        
    	linearLayoutConfig = (LinearLayout) findViewById(R.id.linearLayoutConfig);
    	checkBoxMod = (CheckBox) findViewById(R.id.checkBoxMod);
    	radioButtonList = (RadioButton) findViewById(R.id.radioButtonList);
    	radioButtonFav = (RadioButton) findViewById(R.id.radioButtonFav);
    	
    	if (getLastShowWebCfg()) {
    		linearLayoutConfig.setVisibility(LinearLayout.VISIBLE);
    	} else {
    		linearLayoutConfig.setVisibility(LinearLayout.GONE);
    	}
    	
        records = new ArrayList<WordRecord>();
        adapter = new EfficientAdapter(this, records);
        listViewWebSearch = (ListView) this.findViewById(R.id.listViewWebSearch);
        listViewWebSearch.setAdapter(adapter);
        listViewWebSearch.setFastScrollEnabled(true);
        searchString = this.getIntent().getStringExtra(EXTRA_KEY);
        if (searchString != null) {
        	this.setTitle("查询：" + searchString);
        	actionBar.setTitle("web搜索器:" + searchString);
        }
        if (searchString == null ||
        	(searchString != null && searchString.length() == 0)) {
        	Toast.makeText(this, "关键词为空", Toast.LENGTH_SHORT).show();
        }
        shareString = this.getIntent().getStringExtra(EXTRA_KEY_SHARE);
        listViewWebSearch.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (checkBoxMod.isChecked()) {
					modifySelections(position);
				} else {
					
					if (radioButtonFav.isChecked()) {
						int pos = 0;
						if (position >= 0 && position < favIds.size()) {
							pos = favIds.get(position);
						}
						execute(DictWebListActivity.this, pos, searchString, shareString);
					} else {
						int pos = 0;
						if (position >= 0 && position < listIds.size()) {
							pos = listIds.get(position);
						}
						execute(DictWebListActivity.this, pos, searchString, shareString);
					}
				}
			}
        });
        
        
        if (getLastShowWebFav()) {
        	radioButtonFav.setChecked(true);
        	radioButtonList.setChecked(false);
        } else {
        	radioButtonFav.setChecked(false);
        	radioButtonList.setChecked(true);        	
        }
        radioButtonFav.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setLastShowWebFav(isChecked);
				loadSelections(isChecked);
			}
    	});
        
        
        
        spinnerSearchType = (Spinner) this.findViewById(R.id.spinnerSearchType);
        spinnerSearchTypeAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_spinner_item, TYPE_NAME);
        spinnerSearchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSearchType.setAdapter(spinnerSearchTypeAdapter);
        spinnerSearchType.setOnItemSelectedListener(new OnItemSelectedListener() {
    		@Override
    		public void onItemSelected(AdapterView<?> parent, View view, int postion, long id) {
    			setLastWebsiteListType(postion);
    			updateListIds();
    			loadSelections(radioButtonFav.isChecked());
    		}

    		@Override
    		public void onNothingSelected(AdapterView<?> arg0) {
    			
    		}
        });
        int listtype = getLastWebsiteListType();
        if (D) {
        	Log.e(TAG, "listtype == " + listtype);
        }
        spinnerSearchType.setSelection(listtype);
        
        
        
        
        
        
        favIds = new ArrayList<Integer>();
        listIds = new ArrayList<Integer>();
        
        ArrayList<String> items = new ArrayList<String>();
        JkanjiHistoryActivity.readItems(this, items, DataContext.WEB_FAVOURITE_FILENAME);
        favIds.clear();
        listIds.clear();
        for (String item : items) {
        	if (item != null) {
        		Integer intItem = 0;
        		try {
        			intItem = Integer.parseInt(item);
        		} catch (Throwable e) {
        			e.printStackTrace();
        		}
        		favIds.add(intItem);
        	}
        }
        updateListIds();
        
        
        
        
        loadSelections(getLastShowWebFav());
        
    }
    
    private void updateListIds() {
    	this.listIds.clear();
    	int type = spinnerSearchType.getSelectedItemPosition();
        for (int i = 0; i < sites.length; i++) {
        	if (type == TYPE_ALL || sites[i].type == type) {
        		this.listIds.add(Integer.valueOf(i));
        	}
        }
    }
    
    private void loadSelections(boolean isFav) {
    	records.clear();
    	if (isFav) {
        	for (Integer id : favIds) {
        		if (id >= 0 && id < sites.length) {
        			records.add(sites[id]);
        		} else {
        			records.add(new WordRecord("", "", 0));
        		}
        	}
        } else {
//        	for (int i = 0; i < sites.length; i++) {
//            	records.add(sites[i]);
//          }
        	for (Integer id : listIds) {
        		if (id >= 0 && id < sites.length) {
        			records.add(sites[id]);
        		} else {
        			records.add(new WordRecord("", "", 0));
        		}        	
        	}
        }
    	adapter.notifyDataSetChanged();
    	this.listViewWebSearch.setSelection(0);
    }
    
    private void modifySelections(int position) {
    	if (this.radioButtonFav.isChecked()) {
            ArrayList<String> items = new ArrayList<String>();
            JkanjiHistoryActivity.readItems(this, items, DataContext.WEB_FAVOURITE_FILENAME);
            Integer id = 0;
            if (position >= 0 && position < favIds.size()) {
            	id = favIds.get(position);
            }
    		JkanjiHistoryActivity.removeItem(this, items, Integer.toString(id), DataContext.WEB_FAVOURITE_FILENAME);
            favIds.clear();
            for (String item : items) {
            	if (item != null) {
            		Integer intItem = 0;
            		try {
            			intItem = Integer.parseInt(item);
            		} catch (Throwable e) {
            			e.printStackTrace();
            		}
            		if (D) {
            			Log.d(TAG, "fav after removing : " + intItem);
            		}
            		favIds.add(intItem);
            	}
            }
            loadSelections(true);
            Toast.makeText(this, "移除网页收藏夹", Toast.LENGTH_SHORT).show();
    	} else {
    		JkanjiHistoryActivity.writeItem(this, Integer.toString(position), DataContext.WEB_FAVOURITE_FILENAME);
    		ArrayList<String> items = new ArrayList<String>();
    		JkanjiHistoryActivity.readItems(this, items, DataContext.WEB_FAVOURITE_FILENAME);
    		favIds.clear();
            for (String item : items) {
            	if (item != null) {
            		Integer intItem = 0;
            		try {
            			intItem = Integer.parseInt(item);
            		} catch (Throwable e) {
            			e.printStackTrace();
            		}
            		if (D) {
            			Log.d(TAG, "fav after adding : " + intItem);
            		}
            		favIds.add(intItem);
            	}
            }
            loadSelections(false);
            Toast.makeText(this, "添加网页收藏夹", Toast.LENGTH_SHORT).show();
    	}
    }
    
    private void setLastShowWebFav(boolean isEnabled) {
		PrefUtil.putBoolean(this, SHARE_PREF_NAME, 
				SHARE_PREF_SHOW_WEB_FAV, 
				isEnabled);
    }
    
    private boolean getLastShowWebFav() {
    	return PrefUtil.getBoolean(this, SHARE_PREF_NAME, 
    			SHARE_PREF_SHOW_WEB_FAV, 
				false);
    }

    private void setLastShowWebCfg(boolean isEnabled) {
		PrefUtil.putBoolean(this, SHARE_PREF_NAME, 
				SHARE_PREF_SHOW_WEB_CFG, 
				isEnabled);
    }
    
    private boolean getLastShowWebCfg() {
    	return PrefUtil.getBoolean(this, SHARE_PREF_NAME, 
				SHARE_PREF_SHOW_WEB_CFG, 
				true);
    }
    
    private void setLastWebsiteListType(int type) {
		PrefUtil.putInt(this, SHARE_PREF_NAME,
				SHARE_PREF_WEB_LIST_TYPE,
				type);
    }
    
    private int getLastWebsiteListType() {
		return PrefUtil.getInt(this, SHARE_PREF_NAME,
				SHARE_PREF_WEB_LIST_TYPE,
				TYPE_ALL);
    }
    
    public static void execute(Context context, int position, String searchString, String shareString) {
		Intent intent;
		if (searchString != null) {
			if (position == 0) {
				intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, searchString);
                if (shareString != null) {
                	intent.putExtra(Intent.EXTRA_TEXT, shareString);
                } else {
                	intent.putExtra(Intent.EXTRA_TEXT, searchString);
                }
				try {
					//startActivity(Intent.createChooser(intent, "共享方式"));
					context.startActivity(intent);
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(context, 
						"共享方式出错", Toast.LENGTH_SHORT)
						.show();
				}
			} else if (position == 1) {
				intent = new Intent(context, SQLiteReaderActivity.class);
				intent.putExtra(SQLiteReaderActivity.EXTRA_KEY_SEARCH_WORD, searchString);
				context.startActivity(intent);
			} else if (position == 2) {
				intent = new Intent(context, SQLiteReaderActivity.class);
				intent.putExtra(SQLiteReaderActivity.EXTRA_KEY_SEARCH_WORD, searchString);
				intent.putExtra(SQLiteReaderActivity.EXTRA_KEY_SEARCH_TYPE, SQLiteReaderActivity.TYPE_EXAMPLES);
				context.startActivity(intent);
			} else if (position == 3) {
	            ClipboardManager cm =
	                    (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
	            cm.setText(searchString);
	            Toast.makeText(context, "「" + searchString + "」已复制至剪贴板",
	                    Toast.LENGTH_SHORT).show();
			} else if (position == 4) {
				intent = new Intent();
				intent.setAction(SearchManager.INTENT_ACTION_GLOBAL_SEARCH); //Intent.ACTION_SEARCH
				//intent.setType("text/plain");
                intent.putExtra(SearchManager.QUERY, searchString);
                //intent.setDataAndType(null, "*/*");
                //intent.setData(null);
                //intent.setClassName("com.socialnmobile.colordict", 
                //		"com.socialnmobile.colordict.activity.Main");
				try {
			    	//SearchManager sm = (SearchManager)getSystemService(SEARCH_SERVICE);
			    	//sm.startSearch(searchString, false, null, null, false);
					//startActivity(intent);
					//startActivity(Intent.createChooser(intent, "搜索方式"));
					context.startActivity(intent);
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(context, 
						"搜索方式出错", Toast.LENGTH_SHORT)
						.show();
				}
			} else if (position == 5) {
				intent = new Intent();
				intent.setComponent(new ComponentName(
						"com.socialnmobile.colordict", 
						"com.socialnmobile.colordict.activity.Search"));
				intent.setAction("colordict.intent.action.SEARCH");
                intent.putExtra("EXTRA_QUERY", searchString);
				try {
					context.startActivity(intent);
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(context, 
						"打开ColorDict出错", Toast.LENGTH_SHORT)
						.show();
				}
			} else if (position == 6) {
				intent = new Intent(context, JkanjiEb4jActivity.class);
				intent.putExtra(JkanjiEb4jActivity.EXTRA_KEY_SEARCH_WORD, searchString);
				context.startActivity(intent);
			} else if (position == 7) {
				intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, searchString);
                intent.putExtra(Intent.EXTRA_TEXT, searchString);
                try {
					//startActivity(Intent.createChooser(intent, "共享方式"));
                	context.startActivity(intent);
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(context, 
						"共享方式出错", Toast.LENGTH_SHORT)
						.show();
				}
			} else if (position == 8) {
				Word word = new Word(-1, new String[]{null, null, searchString, 
						(shareString != null ? shareString : searchString), null});
				context.startActivity(new Intent(context, WordEditActivity.class)
					.putExtra(WordEditActivity.EXTRA_DATA, word));
			} else if (position == 9) {
				intent = new Intent();
				intent.setClass(context, ShareToClipboardActivity.class);
				intent.setAction(ShareToClipboardActivity.ACTION_SEND_CLIP);
				intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, searchString);
                if (shareString != null) {
                	intent.putExtra(Intent.EXTRA_TEXT, shareString);
                } else {
                	intent.putExtra(Intent.EXTRA_TEXT, searchString);
                }
				context.startActivity(intent);
			} else if (sites[position].word.startsWith("浩叡日中词典")) {
				intent = new Intent(context, JkanjiHorryActivity.class);
				intent.putExtra(JkanjiHorryActivity.EXTRA_KEY_SEARCH_WORD, searchString);
				context.startActivity(intent);
			} else if (sites[position].word.startsWith("MDict")) {
				intent = new Intent();
				intent.setComponent(new ComponentName(
						"cn.mdict", 
						"cn.mdict.MainForm"));
				intent.setAction(Intent.ACTION_SEARCH);
				intent.putExtra(SearchManager.QUERY, searchString);
				try {
					context.startActivity(intent);
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(context, 
						"打开MDict出错", Toast.LENGTH_SHORT)
						.show();
				}
			} else if (sites[position].word.startsWith("SeederDict")) {
				intent = new Intent();
				intent.setComponent(new ComponentName(
						"com.yhfu.SeederDict", 
						"com.yhfu.SeederDict.ActivityNotifaction"));
				
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, searchString);
				
				try {
					context.startActivity(intent);
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(context, 
						"打开SeederDict出错", Toast.LENGTH_SHORT)
						.show();
				}
			} else {
				intent = new Intent();
				if (sites[position].meaning.startsWith("HBKSugar TTS")) {
					intent.setClassName("com.iteye.weimingtom.hbksuger", "com.iteye.weimingtom.hbksuger.HBKGoogleTTSActivity");
					intent.setAction(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_TEXT, searchString);
				} else {
					if (sites[position].word.startsWith("gimite")) {
						//intent.setAction(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
						intent.setAction(Intent.ACTION_VIEW);
					} else {
						intent.setAction(Intent.ACTION_VIEW);
					}
					String url = String.format(sites[position].meaning, Uri.encode(searchString));
					
					if (sites[position].word.startsWith("gimite")) {
						//intent.setDataAndType(Uri.parse(url), "audio/*");
						intent.setDataAndType(Uri.parse(url), "*/*");
					} else {
						if (JkanjiSettingActivity.getUseOpera(context)) {
							intent.setClassName(
									"com.oupeng.mini.android", 
									"com.opera.mini.android.Browser");
						}
						intent.addCategory(Intent.CATEGORY_DEFAULT);
						intent.addCategory(Intent.CATEGORY_BROWSABLE);
						intent.setDataAndType(Uri.parse(url), "*/*");
						//intent.setData(Uri.parse(url));
						if (D) {
							Log.d(TAG, "scheme == " + intent.getScheme());
						}
					}
				}
				
				try {
					context.startActivity(intent);
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(context, 
						"找不到可用的应用程序", Toast.LENGTH_SHORT)
						.show();
				}
			}
		}
    }
    
	private final static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<WordRecord> records;
        
        public EfficientAdapter(Context context, List<WordRecord> records) {
            this.mInflater = LayoutInflater.from(context);
            this.records = records;
        }
        
        public int getCount() {
        	if (records == null) {
        		return 0;
        	}
        	return records.size();
        }
        
        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }
        
        private WordRecord getWord(int index) {
        	if (records == null) {
        		return null;
        	}
        	if (index < 0 || index >= records.size()) {
        		return null;
        	}
    		return records.get(index);
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String title = "";
            String text = "";
            WordRecord word = getWord(position);
			if (word != null) {
				title = word.word;
				text = word.meaning;
			}
            holder.title.setText(title);
            holder.text.setText(text);
            
            return convertView;
        }

        private class ViewHolder {
        	TextView title;
            TextView text;
        }
    }
	
	private final static class WordRecord {
		public String word;
		public String meaning;
		public int type;
		
		public WordRecord(String word, String meaning, int type) {
			this.word = word;
			this.meaning = meaning;
			this.type = type;
		}
	}
}
