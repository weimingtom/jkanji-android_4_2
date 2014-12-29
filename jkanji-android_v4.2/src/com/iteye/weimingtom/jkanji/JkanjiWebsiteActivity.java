package com.iteye.weimingtom.jkanji;

import java.util.ArrayList;
import java.util.List;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class JkanjiWebsiteActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "DictWebListActivity";
	
	public static final String EXTRA_KEY = "com.iteye.weimingtom.jkanji.DictWebListActivity";
	public static final String EXTRA_KEY_SHARE = "com.iteye.weimingtom.jkanji.DictWebListActivity.share";
	
	private static final String SHARE_PREF_NAME = "pref";
	private static final String SHARE_PREF_WEBSITE_LIST_TYPE = "websiteListType";
	private static final String SHARE_PREF_WEBSITE_SHOW_CONFIG = "websiteShowConfig";
	
	private ActionBar actionBar;
	private ListView listViewWebSearch;
	private LinearLayout linearLayoutConfig;
	private List<WordRecord> records;
	private EfficientAdapter adapter;
	
	
	private final static int TYPE_ALL = 0;
	private final static int TYPE_GRAMMA = 1;
	private final static int TYPE_WORD = 2;
	private final static int TYPE_FULL_JP_CH = 3;
	private final static int TYPE_FULL_JP_EN = 4;
	private final static int TYPE_FULL_JP_ETC = 5;
	private final static int TYPE_FULL_JP_TRANS = 6;
	private final static int TYPE_FULL_JP_CROSS = 7;
	private final static int TYPE_PRO = 8;
	private final static int TYPE_ACG = 9;
	private final static int TYPE_IT_FORUM = 10;
	private static final String[] TYPE_NAME = {
		"全部", // 0
		"日语语法", // 1
		"辞典（词汇翻译）", // 2
		"全文翻译（支持日中）", // 3
		"全文翻译（支持日英）", // 4
		"外文辞典", // 5
		"链接集", // 6
		"交叉翻译", // 7
		"专业辞典", // 8
		"ACG", // 9
		"技术论坛", //10
	};
	
	private static final WordRecord[] sites = {
		//日语语法
		new WordRecord("NHK World 简明日语", "http://www3.nhk.or.jp/lesson/chinese/learn/list/index.html", TYPE_GRAMMA),
		new WordRecord("NHK World 广播和播客", "http://www3.nhk.or.jp/nhkworld/chinese/top/podcasting.html", TYPE_GRAMMA),
		new WordRecord("常用漢字表", "http://homepage3.nifty.com/jgrammar/ja/tools/jouyou.htm", TYPE_GRAMMA),
		new WordRecord("アニメ・マンガの日本語", "http://anime-manga.jp/chinese/", TYPE_GRAMMA),
		new WordRecord("北嶋千鶴子の日本語教室", "http://www.japanese-nihongo.com/index.html", TYPE_GRAMMA),
		new WordRecord("にほんごのページ", "http://web.ydu.edu.tw/~uchiyama/index.html", TYPE_GRAMMA),
		new WordRecord("文字コード（日本語漢字コード表）", "http://charset.7jp.net/", TYPE_GRAMMA),
		new WordRecord("Romaji Translator at Romaji.org", "http://www.romaji.org/index.php", TYPE_GRAMMA),
		
		//辞典（词汇翻译）
		new WordRecord("WWWJDIC", "http://www.csse.monash.edu.au/~jwb/cgi-bin/wwwjdic.cgi", TYPE_WORD),
		new WordRecord("沪江小D日语词典", "http://dict.hjenglish.com/jp/", TYPE_WORD),
		new WordRecord("有道词典", "http://dict.youdao.com/", TYPE_WORD),
		new WordRecord("Weblio辞書", "http://cjjc.weblio.jp/", TYPE_WORD),
		new WordRecord("goo辞書", "http://dictionary.goo.ne.jp/", TYPE_WORD),
		new WordRecord("エキサイト辞書（excite）", "http://www.excite.co.jp/dictionary/", TYPE_WORD),
		new WordRecord("Infoseekマルチ辞書", "http://dictionary.infoseek.ne.jp/", TYPE_WORD),
		new WordRecord("英辞郎 on the WEB", "http://www.alc.co.jp/smp/", TYPE_WORD),
		new WordRecord("和英／英和辞典", "http://rut.org/cgi-bin/j-e/jis/nihongo-de/dict/", TYPE_WORD),
		new WordRecord("PSPの無料オンライン日英，英日辞書", "http://dictionary.pspinc.com/indexj.htm", TYPE_WORD),
		new WordRecord("三省堂 Web Dictionary", "http://www.sanseido.net/", TYPE_WORD),
		
		//全文翻译（支持日中）
		new WordRecord("Google翻译", "http://translate.google.co.jp/", TYPE_FULL_JP_CH),
		new WordRecord("必应Bing在线翻译", "http://www.microsofttranslator.com/", TYPE_FULL_JP_CH),
		new WordRecord("沪江小D全文翻译", "http://dict.hjenglish.com/app/trans/", TYPE_FULL_JP_CH),
		new WordRecord("有道在线翻译", "http://fanyi.youdao.com/", TYPE_FULL_JP_CH),
		new WordRecord("Infoseek（楽天）", "http://translation.infoseek.co.jp/", TYPE_FULL_JP_CH),
		new WordRecord("livedoor", "http://translate.livedoor.com/", TYPE_FULL_JP_CH),
		new WordRecord("Yahoo!", "http://honyaku.yahoo.co.jp/", TYPE_FULL_JP_CH),
		new WordRecord("nifty", "http://honyaku.nifty.com/chinese/index.htm", TYPE_FULL_JP_CH),
		new WordRecord("So-net", "http://so-net.web.transer.com/text_trans_sn.php", TYPE_FULL_JP_CH),
		new WordRecord("Excite", "http://www.excite.co.jp/world/jiantizi/", TYPE_FULL_JP_CH),
		new WordRecord("OCN", "http://www.ocn.ne.jp/translation/", TYPE_FULL_JP_CH),
		new WordRecord("WorldLingo", "http://www.worldlingo.com/ja/products_services/worldlingo_translator.html", TYPE_FULL_JP_CH),
		new WordRecord("SYSTRAN", "http://www.systransoft.com/", TYPE_FULL_JP_CH),
		new WordRecord("Dictionary.com", "http://translate.reference.com/", TYPE_FULL_JP_CH),
		new WordRecord("Dr.eye 译典通", "http://www.dreye.com.cn/trans/", TYPE_FULL_JP_CH),
		
		//全文翻译（支持日英）
		new WordRecord("vil-net", "http://vil-net.dyndns.org/hon/textsp/", TYPE_FULL_JP_EN),
		new WordRecord("SDL|FreeTranslation.com", "http://www.freetranslation.com/", TYPE_FULL_JP_EN),
		new WordRecord("Yahoo! Babel Fish", "http://babelfish.yahoo.com/", TYPE_FULL_JP_EN),
		new WordRecord("T-Mail T-Text", "http://www.t-mail.com/t-text.shtml", TYPE_FULL_JP_EN),
		new WordRecord("PROMT", "http://www.online-translator.com/?prmtlang=en", TYPE_FULL_JP_EN),
		
		//外文辞典
		new WordRecord("Travlang", "http://dictionaries.travlang.com/", TYPE_FULL_JP_ETC),
		new WordRecord("freedict.com", "http://www.freedict.com/", TYPE_FULL_JP_ETC),
		new WordRecord("AllWords.com", "http://www.allwords.com/", TYPE_FULL_JP_ETC),
		
		//链接集
		new WordRecord("自動翻訳サイトのリンク集", "http://www.dio.ne.jp/user/bestsites/translate.html", TYPE_FULL_JP_TRANS),
		new WordRecord("オンライン辞書のリンク集", "http://www.dio.ne.jp/user/bestsites/dictionary.html", TYPE_FULL_JP_TRANS),
		new WordRecord("翻訳，通訳，辞書サイト検索", "http://search.feelwords.com/", TYPE_FULL_JP_TRANS),
		new WordRecord("翻訳サービスリンク集", "http://www.hir-net.com/link/dic/trans.html", TYPE_FULL_JP_TRANS),
		new WordRecord("翻訳でねっと！", "http://www.hnanayu.com/", TYPE_FULL_JP_TRANS),
		new WordRecord("英和辞書，和英辞書，英語ページ翻訳", "http://www.linksyu.com/p30.htm", TYPE_FULL_JP_TRANS),
		
		//交叉翻译
		new WordRecord("kotoba", "http://www.kotoba.ne.jp/", TYPE_FULL_JP_CROSS),
		new WordRecord("sukimania", "http://sukimania.ddo.jp/trans/trans_china.php", TYPE_FULL_JP_CROSS),
		
		//专业辞典
		new WordRecord("Wikipedia（ウィキペディア）", "http://ja.m.wikipedia.org/", TYPE_PRO),
		new WordRecord("IT用語辞典 e-Words", "http://sp.e-words.jp/", TYPE_PRO),
		new WordRecord("法令用語検索", "http://law.e-gov.go.jp/cgi-bin/idxsearch.cgi", TYPE_PRO),
		new WordRecord("名言集.com", "http://www.meigensyu.com/", TYPE_PRO),
		new WordRecord("人名歴史年表", "http://www.eonet.ne.jp/~libell/index.htm", TYPE_PRO),
		new WordRecord("地球の名言", "http://www.earth-words.net/", TYPE_PRO),
		new WordRecord("DQNネーム", "http://dqname.jp/", TYPE_PRO),
		
		//ACG
		new WordRecord("這いよれ！ ニャル子さん 元ネタwiki", "http://www15.atwiki.jp/nyaruko/", TYPE_ACG),
		new WordRecord("口袋百科", "http://www.pokemon.name/wiki/%E9%A6%96%E9%A1%B5", TYPE_ACG),
		new WordRecord("響 - HiBiKi Radio Station -", "http://hibiki-radio.jp/", TYPE_ACG),
		new WordRecord("Baka-Tsuki Translation Project", "http://www.baka-tsuki.org/", TYPE_ACG),
		new WordRecord("ライトノベルの書き方", "http://www.raitonoveru.jp/", TYPE_ACG),
		new WordRecord("acfun论坛动画版", "http://h.acfun.tv/m/%E5%8A%A8%E7%94%BB", TYPE_ACG),
		new WordRecord("himado", "http://himado.in/", TYPE_ACG),
		new WordRecord("acfun", "http://www.acfun.tv/", TYPE_ACG),
		new WordRecord("bilibili", "http://www.bilibili.tv/", TYPE_ACG),
		new WordRecord("bilibili新番", "http://www.bilibili.tv/video/bangumi-two-1.html", TYPE_ACG),
		new WordRecord("轻之国度", "http://www.lightnovel.cn/forum.php", TYPE_ACG),
		
		//技术论坛
		new WordRecord("eoe", "http://www.eoeandroid.com/forum-45-1.html", TYPE_IT_FORUM),		
		new WordRecord("csdn", "http://bbs.csdn.net/forums/Android", TYPE_IT_FORUM),
		new WordRecord("iteye", "http://www.iteye.com/", TYPE_IT_FORUM),
		new WordRecord("我的iteye博客", "http://weimingtom.iteye.com/", TYPE_IT_FORUM),
		new WordRecord("红米rom", "http://bbs.xiaomi.cn/thread-8491480-1-1.html", TYPE_IT_FORUM),
	};
	
	private Spinner spinnerSearchType;
	private ArrayAdapter<String> spinnerSearchTypeAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.web_bookmark);
        
        linearLayoutConfig = (LinearLayout) findViewById(R.id.linearLayoutConfig);
        if (getLastWebsiteShowConfig()) {
        	linearLayoutConfig.setVisibility(View.VISIBLE);
        } else {
        	linearLayoutConfig.setVisibility(View.GONE);
        }
        
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("网页书签");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.bookmark;
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
				//return R.drawable.icon_actionbar;
				return R.drawable.config;
			}

			@Override
			public void performAction(View view) {
				if (linearLayoutConfig.getVisibility() == View.VISIBLE) {
					linearLayoutConfig.setVisibility(View.GONE);
					setLastWebsiteShowConfig(false);
				} else {
					linearLayoutConfig.setVisibility(View.VISIBLE);
					setLastWebsiteShowConfig(true);
				}
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
    			updateList();
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
        
        records = new ArrayList<WordRecord>();
        adapter = new EfficientAdapter(this, records);
        listViewWebSearch = (ListView) this.findViewById(R.id.listViewWebSearch);
        listViewWebSearch.setAdapter(adapter);
        listViewWebSearch.setFastScrollEnabled(true);
        listViewWebSearch.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent;
				intent = new Intent();
				String url = records.get(position).meaning;//sites[position][1];
				if (JkanjiSettingActivity.getUseOpera(JkanjiWebsiteActivity.this)) {
					intent.setClassName(
							"com.oupeng.mini.android", 
							"com.opera.mini.android.Browser");
				}
				intent.setAction(Intent.ACTION_VIEW);
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.setDataAndType(Uri.parse(url), "*/*");
				//intent.setData(Uri.parse(url));
				if (D) {
					Log.d(TAG, "scheme == " + intent.getScheme());
					Log.d(TAG, "url == " + url);
				}
				try {
					startActivity(intent);
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiWebsiteActivity.this, 
						"找不到可用的应用程序", Toast.LENGTH_SHORT)
						.show();
				}
			}
        });
        
        updateList();
    }
    
    private void updateList() {
    	records.clear();
    	int type = spinnerSearchType.getSelectedItemPosition();
        for (int i = 0; i < sites.length; i++) {
        	if (type == TYPE_ALL || sites[i].type == type) {
        		records.add(sites[i]);
        	}
        }
        adapter.notifyDataSetChanged();
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
	
	public final static class WordRecord {
		public String word;
		public String meaning;
		public int type;
		
		public WordRecord(String word, String meaning, int type) {
			this.word = word;
			this.meaning = meaning;
			this.type = type;
		}
	}


    private void setLastWebsiteShowConfig(boolean value) {
		PrefUtil.putBoolean(this, SHARE_PREF_NAME,
				SHARE_PREF_WEBSITE_SHOW_CONFIG,
				value);
    }
    
    private boolean getLastWebsiteShowConfig() {
		return PrefUtil.getBoolean(this, SHARE_PREF_NAME,
				SHARE_PREF_WEBSITE_SHOW_CONFIG,
				true);
    }

    private void setLastWebsiteListType(int type) {
		PrefUtil.putInt(this, SHARE_PREF_NAME,
				SHARE_PREF_WEBSITE_LIST_TYPE,
				type);
    }
    
    private int getLastWebsiteListType() {
		return PrefUtil.getInt(this, SHARE_PREF_NAME,
				SHARE_PREF_WEBSITE_LIST_TYPE,
				TYPE_ALL);
    }
}
