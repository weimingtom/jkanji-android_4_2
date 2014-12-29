package com.iteye.weimingtom.jkanji;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.iteye.weimingtom.jkanji.PackFileReadTask.ReadPackException;
import com.iteye.weimingtom.jkanji.PackFileReadTask.SessionSaveData;
import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

/**
 * 布局素材来自 译典通Dr.eye（去除机型限制）
 * @see http://bbs.gfan.com/android-2553656-1-1.html
 * @author Administrator
 *
 *
 * NOTE: DictSearchTask已废，请使用DictBinarySearchTask
 */
public class JkanjiTalkActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "JkanjiTalkActivity";
	
	private static final int REQUEST_HANDINPUT = 1;
	
	private static final int ID_SEARCH_FULLTEXT = 1;
	private static final int ID_SEARCH_PREFIX = 2;
	private static final int ID_SEARCH_CONTAIN = 3;
	private static final int ID_SEARCH_SUFFIX = 4;
	private static final int ID_SEARCH_WORD = 5;
	private static final int ID_SEARCH_LAUCHER = 6;
	private static final int ID_HISTORY = 7;
	private static final int ID_CLEAR = 8;
	private static final int ID_HELP = 9;
	
    private ListView listViewTalk;
	private TalkListAdapter adapter;
	private EditText talkInput;
	private Button talkInputClear, talkInputSearch;
	private QuickAction mQuickAction;
	private Typeface typeface;
	private ActionBar actionBar;
	
	//private DictSearchTask searchTask;
	private volatile DictBinarySearchTask searchTask;
	
	private static final int TALK_POS_NONE = -1;
	private static final int TALK_POS_SEARCH_HEAD = -2; //for TALK_SEARCH_HEAD
	private static final int TALK_POS_LAUNCHER_HEAD = -3; //for TALK_LAUCHER_HEAD
	private static final int TALK_POS_HELP_HEAD = -4;
	private static final int TALK_POS_HELP_ITEM = -5;
	
	private static final int TALK_NONE = 0;
	private static final int TALK_SEARCH_HEAD = 1;
	private static final int TALK_SEARCH_ITEM = 2;
	private static final int TALK_HISTORY_ITEM = 3;
	private static final int TALK_LAUCHER_HEAD = 4;
	private static final int TALK_LAUCHER_ITEM = 5;
	private static final int TALK_HELP_HEAD = 6;
	private static final int TALK_HELP_ITEM = 7;	
	
	private static final String FLAG_FULLTEXT = "fulltext";
	private static final String FLAG_PREFIX = "prefix";
	private static final String FLAG_CONTAIN = "contain";
	private static final String FLAG_SUFFIX = "suffix";
	private static final String FLAG_WORD = "word";
	
	private int currentSelectedType;
	private int currentSelectedPos;
	private String currentSelectedInfo;
	private List<String> searchHistory;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.talk);
        
        //typeface = Typefaces.get(this, "mplus-1m-regular.ttf");
        typeface = Typefaces.getFile(JkanjiSettingActivity.getFontFileName(this));	
        
        searchHistory = (List<String>) this.getLastNonConfigurationInstance();
        if (searchHistory == null) {
        	searchHistory = new ArrayList<String>();
        }
        talkInput = (EditText) this.findViewById(R.id.talkInput);
        talkInputClear = (Button) this.findViewById(R.id.talkInputClear);
        talkInputSearch = (Button) this.findViewById(R.id.talkInputSearch);
        
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("会话模式");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.nyaruko;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
        showActionBarButtons();
        
        talkInputClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchTask = null;
				if (actionBar != null) {
					actionBar.setTitle("会话模式");
				}
				talkInput.setText("");
				adapter.clear();
				talkInput.requestFocus();
				talkInput.requestFocusFromTouch();
				//see http://blog.3gstdy.com/?p=545
				InputMethodManager imm = (InputMethodManager)
						getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(talkInput, InputMethodManager.SHOW_FORCED);
			}
        });
        talkInputSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showQuickAction(v, TALK_SEARCH_HEAD, talkInput.getText().toString(), TALK_POS_SEARCH_HEAD);
			}
        });
        talkInput.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					InputMethodManager imm = (InputMethodManager)
							getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(talkInput.getWindowToken(), 0);
					
					String info = talkInput.getText().toString();
					if (info == null) {
						info = "";
					}
					if (actionBar != null) {
						actionBar.setTitle("全文搜索:" + info);
						removeActionBarButtons();
					}
					adapter.searchStart(info);
					searchTask = new DictBinarySearchTask(); //new DictSearchTask();
					searchTask.execute(info, FLAG_FULLTEXT);
				}
				return false;
			}
        });
        
		mQuickAction = new QuickAction(this);
		mQuickAction.addActionItem(new ActionItem(ID_SEARCH_FULLTEXT, "全文", null));
		mQuickAction.addActionItem(new ActionItem(ID_SEARCH_PREFIX, "前缀", null));
		mQuickAction.addActionItem(new ActionItem(ID_SEARCH_CONTAIN, "包含", null));
		mQuickAction.addActionItem(new ActionItem(ID_SEARCH_SUFFIX, "后缀", null));
		mQuickAction.addActionItem(new ActionItem(ID_SEARCH_WORD, "完全", null));
		mQuickAction.addActionItem(new ActionItem(ID_SEARCH_LAUCHER, "在线", null));
		mQuickAction.addActionItem(new ActionItem(ID_HISTORY, "历史", null));
		mQuickAction.addActionItem(new ActionItem(ID_CLEAR, "清空", null));
		mQuickAction.addActionItem(new ActionItem(ID_HELP, "帮助", null));
		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			@Override
			public void onItemClick(QuickAction quickAction, int pos, int actionId) {
				if (searchTask != null) {
					Toast.makeText(getApplicationContext(),
							"搜索未完成，请稍候",
							Toast.LENGTH_SHORT)
							.show();					
					return;
				}
				ActionItem actionItem = quickAction.getActionItem(pos);
				switch (actionId) {
				case ID_SEARCH_FULLTEXT:
					if (currentSelectedType == TALK_SEARCH_HEAD || 
						currentSelectedType == TALK_HISTORY_ITEM ||
						currentSelectedType == TALK_LAUCHER_HEAD) {
						if (actionBar != null) {
							actionBar.setTitle("全文搜索:" + 
								(currentSelectedInfo != null ? currentSelectedInfo : ""));
							removeActionBarButtons();
						}
						adapter.searchStart(currentSelectedInfo);
						searchTask = new DictBinarySearchTask(); //new DictSearchTask();
						searchTask.execute(currentSelectedInfo, FLAG_FULLTEXT);
					}
					break;
					
				case ID_SEARCH_PREFIX:
					if (currentSelectedType == TALK_SEARCH_HEAD || 
						currentSelectedType == TALK_HISTORY_ITEM ||
						currentSelectedType == TALK_LAUCHER_HEAD) {
						if (actionBar != null) {
							actionBar.setTitle("前缀搜索:" + 
								(currentSelectedInfo != null ? currentSelectedInfo : ""));
							removeActionBarButtons();
						}
						adapter.searchStart(currentSelectedInfo);
						searchTask = new DictBinarySearchTask(); //new DictSearchTask();
						searchTask.execute(currentSelectedInfo, FLAG_PREFIX);
					}
					break;
					
				case ID_SEARCH_CONTAIN:
					if (currentSelectedType == TALK_SEARCH_HEAD || 
						currentSelectedType == TALK_HISTORY_ITEM ||
						currentSelectedType == TALK_LAUCHER_HEAD) {
						if (actionBar != null) {
							actionBar.setTitle("包含搜索:" + 
								(currentSelectedInfo != null ? currentSelectedInfo : ""));
							removeActionBarButtons();
						}
						adapter.searchStart(currentSelectedInfo);
						searchTask = new DictBinarySearchTask(); //new DictSearchTask();
						searchTask.execute(currentSelectedInfo, FLAG_CONTAIN);
					}
					break;

				case ID_SEARCH_SUFFIX:
					if (currentSelectedType == TALK_SEARCH_HEAD || 
						currentSelectedType == TALK_HISTORY_ITEM ||
						currentSelectedType == TALK_LAUCHER_HEAD) {
						if (actionBar != null) {
							actionBar.setTitle("后缀搜索:" + 
								(currentSelectedInfo != null ? currentSelectedInfo : ""));
							removeActionBarButtons();
						}
						adapter.searchStart(currentSelectedInfo);
						searchTask = new DictBinarySearchTask(); //new DictSearchTask();
						searchTask.execute(currentSelectedInfo, FLAG_SUFFIX);
					}
					break;
					
				case ID_SEARCH_WORD:
					if (currentSelectedType == TALK_SEARCH_HEAD || 
						currentSelectedType == TALK_HISTORY_ITEM ||
						currentSelectedType == TALK_LAUCHER_HEAD) {
						if (actionBar != null) {
							actionBar.setTitle("完全搜索:" + 
								(currentSelectedInfo != null ? currentSelectedInfo : ""));
							removeActionBarButtons();
						}
						adapter.searchStart(currentSelectedInfo);
						searchTask = new DictBinarySearchTask(); //new DictSearchTask();
						searchTask.execute(currentSelectedInfo, FLAG_WORD);
					}
					break;
					
				case ID_SEARCH_LAUCHER:
					if (currentSelectedType == TALK_SEARCH_HEAD || 
						currentSelectedType == TALK_HISTORY_ITEM ||
						currentSelectedType == TALK_LAUCHER_HEAD) {
						if (actionBar != null) {
							actionBar.setTitle("web搜索:" + 
								(currentSelectedInfo != null ? currentSelectedInfo : ""));
						}
						adapter.showLauncher(currentSelectedInfo);
					}
					break;
					
				case ID_HISTORY:
					if (actionBar != null) {
						actionBar.setTitle("历史");
					}
					adapter.showHistory();
					break;
					
				case ID_CLEAR:
					if (actionBar != null) {
						actionBar.setTitle("会话模式");
					}
					adapter.clear();
					break;
					
				case ID_HELP:
					if (actionBar != null) {
						actionBar.setTitle("帮助");
					}
					adapter.showHelp();
					break;
				}
				currentSelectedType = TALK_NONE;
				currentSelectedInfo = null;
				currentSelectedPos = TALK_POS_NONE;
			}
		});
		mQuickAction.setOnDismissListener(new QuickAction.OnDismissListener() {
			@Override
			public void onDismiss() {
				/*
				Toast.makeText(getApplicationContext(), 
					"Ups..dismissed", 
					Toast.LENGTH_SHORT)
					.show();
				*/
				currentSelectedType = TALK_NONE;
				currentSelectedInfo = null;
				currentSelectedPos = TALK_POS_NONE;
			}
		});
		
        adapter = new TalkListAdapter(this);
        listViewTalk = (ListView) this.findViewById(R.id.listViewTalk);
        listViewTalk.setAdapter(adapter);
    }

    private void removeActionBarButtons() {
    	actionBar.removeAllActions();
    }
    
    private void showActionBarButtons() {
    	removeActionBarButtons();
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.search;
			}

			@Override
			public void performAction(View view) {
				InputMethodManager imm = (InputMethodManager)
						getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(talkInput.getWindowToken(), 0);
				
				String info = talkInput.getText().toString();
				if (info == null) {
					info = "";
				}
				if (actionBar != null) {
					actionBar.setTitle("全文搜索:" + info);
					removeActionBarButtons();
				}
				adapter.searchStart(info);
				searchTask = new DictBinarySearchTask(); //new DictSearchTask();
				searchTask.execute(info, FLAG_FULLTEXT);
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.search_web;
			}

			@Override
			public void performAction(View view) {
				if (talkInput != null) {
					String str = talkInput.getText().toString();
					startActivity(new Intent(JkanjiTalkActivity.this, DictWebListActivity.class)
						.putExtra(DictWebListActivity.EXTRA_KEY, str));
				}
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.write_input;
			}

			@Override
			public void performAction(View view) {
				Intent intent = new Intent(JkanjiTalkActivity.this, 
						JkanjiHandActivity.class);
				intent.putExtra(JkanjiHandActivity.EXTRA_KEY_INIT_STRING, 
						talkInput.getText().toString());
				startActivityForResult(intent, REQUEST_HANDINPUT);
			}
        });
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return searchHistory;
	}

	private void showQuickAction(View v, int type, String info, int pos) {
    	this.currentSelectedType = type;
    	this.currentSelectedInfo = info;
    	this.currentSelectedPos = pos;
		mQuickAction.show(v);
    }
    
    private void launcherStart(View v, int type, String info, int pos, String title, String detail) {
    	if (type == TALK_LAUCHER_ITEM) {
    		if (false) {
		    	Toast.makeText(getApplicationContext(),
		    			"launch " + pos + " : " + info,
		    			Toast.LENGTH_SHORT)
		    			.show();
    		}
    		/**
    		 * @see showLauncher
    		 */
    		Intent intent;
    		switch(pos) {
    		case 0:
    			onlineSearch("http://dict.hjenglish.com/jp/jc/%s", info);
				break;
    			
    		case 1:
    			onlineSearch("http://www.excite.co.jp/dictionary/japanese/?match=beginswith&search=%s", info);
    			break;
				
    		case 2:
    			onlineSearch("http://dic.search.yahoo.co.jp/search?ei=UTF-8&p=%s", info);
				break;
				
    		case 3:
				intent = new Intent(JkanjiTalkActivity.this, SQLiteReaderActivity.class);
				intent.putExtra(SQLiteReaderActivity.EXTRA_KEY_SEARCH_WORD, info);
				startActivity(intent);
    			break;
    			
    		case 4:
				intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, info);
                intent.putExtra(Intent.EXTRA_TEXT, info);
                //startActivity(Intent.createChooser(intent, "共享方式"));
                try {
                	startActivity(intent);
                } catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiTalkActivity.this, 
						"共享方式出错", Toast.LENGTH_SHORT)
						.show();
                }
                break;
    		}
    	} else if (type == TALK_SEARCH_ITEM) {
    		Intent intent;
    		intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, title);
            intent.putExtra(Intent.EXTRA_TEXT, (title != null) ? (title + "\n" + detail) : detail);
            //startActivity(Intent.createChooser(intent, "共享方式"));
            try {
            	startActivity(intent);
            } catch (Throwable e) {
				e.printStackTrace();
				Toast.makeText(JkanjiTalkActivity.this, 
					"共享方式出错", Toast.LENGTH_SHORT)
					.show();
            }
    	}
    }

    private void onlineSearch(String format, String keyword) {
		Intent intent;
		String url;
    	intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		url = String.format(format, Uri.encode(keyword));
		intent.setData(Uri.parse(url));
		try {
			startActivity(intent);
		} catch (Throwable e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(),
				"找不到可用的应用程序", Toast.LENGTH_SHORT)
				.show();
		}
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			String resultString = data.getStringExtra(JkanjiHandActivity.EXTRA_KEY_RESULT_STRING);
			switch (requestCode) {
			case REQUEST_HANDINPUT:
				if (resultString != null) {
					talkInput.setText("");
					talkInput.append(resultString);
				}
				showQuickAction(talkInputSearch, TALK_SEARCH_HEAD, talkInput.getText().toString(), TALK_POS_SEARCH_HEAD);
				break;
			}
		}
	}
    
    private final static class TalkListData {    	
    	public int type;
    	public String info;
    	public int pos;
    	
    	public String title1;
    	public String detail1;
    	public String title2;
    	public String detail2;
    }
    
    /**
     * 纯文本的解包
     * @deprecated
     * @author Administrator
     *
     */
    private class DictSearchTask extends AsyncTask<String, String, Boolean> {
    	private final static int FULLTEXT = 0;
    	private final static int PREFIX = 1;
    	private final static int CONTAIN = 2;
    	private final static int SUFFIX = 3;
    	private final static int WORD = 4;
		private int searchtype;
		
    	private String error;
		private String keyword;
		
    	@Override
		protected Boolean doInBackground(String... params) {
    		InputStream instr = null;
    		InputStreamReader reader = null;
    		BufferedReader buf = null;
    		try {
    			this.keyword = params[0];
    			if (params[1] != null) {
    				if (FLAG_FULLTEXT.equals(params[1])) {
    					this.searchtype = FULLTEXT;
    				} else if (FLAG_PREFIX.equals(params[1])) {
    					this.searchtype = PREFIX;
    				} else if (FLAG_CONTAIN.equals(params[1])) {
    					this.searchtype = CONTAIN;
    				} else if (FLAG_SUFFIX.equals(params[1])) {
    					this.searchtype = SUFFIX;
    				} else if (FLAG_WORD.equals(params[1])) {
    					this.searchtype = WORD;
    				} else {
    					this.searchtype = FULLTEXT;
    				}
    			} else {
    				this.searchtype = FULLTEXT;
    			}
    			if (keyword == null || keyword.length() == 0) {
    				this.keyword = "";
    				return true;
    			}
    			instr = getAssets().open("jpwords.csv");
    			reader = new InputStreamReader(instr, "utf-8");
    			buf = new BufferedReader(reader);
    			String line;
    			
    			while (null != (line = buf.readLine()) && searchTask != null) {
    				boolean isMatch = false;
    				Word word = new Word(line);
    				String kanji = word.kanji;
    				String reading = word.reading;
    				String mean = word.mean;
    				switch (searchtype) {
    				case FULLTEXT:
        				if ((kanji != null && kanji.contains(keyword)) ||
        					(reading != null && reading.contains(keyword)) ||
        					(mean != null && mean.contains(keyword))) {
        					isMatch = true;
        				}
    					break;
    					
    				case PREFIX:
        				if ((kanji != null && kanji.startsWith(keyword)) ||
        					(reading != null && reading.startsWith(keyword))) {
        					isMatch = true;
        				}
    					break;
    					
    				case CONTAIN:
        				if ((kanji != null && kanji.contains(keyword)) ||
        					(reading != null && reading.contains(keyword))) {
        					isMatch = true;
        				}
    					break;
    					
    				case SUFFIX:
        				if ((kanji != null && kanji.endsWith(keyword)) ||
        					(reading != null && reading.endsWith(keyword))) {
        					isMatch = true;
        				}
    					break;
    					
    				case WORD:
        				if ((kanji != null && kanji.equals(keyword)) ||
        					(reading != null && reading.equals(keyword))) {
        					isMatch = true;
        				}
    					break;
    				}
    				if (isMatch) {
    					this.publishProgress(keyword, line);
    				}
    			}
    		} catch (Throwable e) {
    			error = e.toString();
    		} finally {
    			if (buf != null) {
    				try {
						buf.close();
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
			return true;
		}
    	
    	@Override
        protected void onProgressUpdate(String... progress) {
    		if (searchTask != null) {
    			adapter.searchProgress(progress[0], progress[1]);
    		}
    	}
    	
    	@Override
        protected void onPostExecute(Boolean result) {
    		if (result && !isFinishing()) {

            } else if (result == false) {
            	Toast.makeText(getApplicationContext(), 
            			error, 
            			Toast.LENGTH_SHORT)
            			.show();
            }
    		searchTask = null;
    		adapter.searchEnd(this.keyword);
        }
    }
    
	private final class ReadPackException extends Exception {
		private static final long serialVersionUID = 1L;
		
		public ReadPackException(String message) {
			super(message);
		}
	}
    
	/**
	 * PACK解包
	 * @author Administrator
	 *
	 */
    private class DictBinarySearchTask extends AsyncTask<String, String, Boolean> {
    	private final static int FULLTEXT = 0;
    	private final static int PREFIX = 1;
    	private final static int CONTAIN = 2;
    	private final static int SUFFIX = 3;
    	private final static int WORD = 4;
    	
		private int searchtype;
		private String keyword;
		private String error;
		
    	private static final int MAX_FIELD_SIZE = 5;
    	
    	private int pos;
		private byte[] bytes;
    	
		private int[] dict_size;
		String[] currentFields;
		
		private int currentProgress;
    	private int maxProgress;
    	
    	@Override
		protected Boolean doInBackground(String... params) {
    		try {
    			this.keyword = params[0];
    			if (params[1] != null) {
    				if (FLAG_FULLTEXT.equals(params[1])) {
    					this.searchtype = FULLTEXT;
    				} else if (FLAG_PREFIX.equals(params[1])) {
    					this.searchtype = PREFIX;
    				} else if (FLAG_CONTAIN.equals(params[1])) {
    					this.searchtype = CONTAIN;
    				} else if (FLAG_SUFFIX.endsWith(params[1])) {
    					this.searchtype = SUFFIX;
    				} else if (FLAG_WORD.endsWith(params[1])) {
    					this.searchtype = WORD;
    				} else {
    					this.searchtype = FULLTEXT;
    				}
    			} else {
    				this.searchtype = FULLTEXT;
    			}
    			if (keyword == null || keyword.length() == 0) {
    				this.keyword = "";
    				return true;
    			}
    			loadAllBytes();
				while (readItem() && searchTask != null) {
    				boolean isMatch = false;
    				String catalog = currentFields[0];
    				String reading = currentFields[1];
    				String kanji = currentFields[2];
    				String mean = currentFields[3];
    				String etc = currentFields[4];
    				switch (searchtype) {
    				case FULLTEXT:
        				if ((kanji != null && kanji.contains(keyword)) ||
        					(reading != null && reading.contains(keyword)) ||
        					(mean != null && mean.contains(keyword))) {
        					isMatch = true;
        				}
    					break;
    					
    				case PREFIX:
        				if ((kanji != null && kanji.startsWith(keyword)) ||
        					(reading != null && reading.startsWith(keyword))) {
        					isMatch = true;
        				}
    					break;
    					
    				case CONTAIN:
        				if ((kanji != null && kanji.contains(keyword)) ||
        					(reading != null && reading.contains(keyword))) {
        					isMatch = true;
        				}
    					break;
    					
    				case SUFFIX:
        				if ((kanji != null && kanji.endsWith(keyword)) ||
        					(reading != null && reading.endsWith(keyword))) {
        					isMatch = true;
        				}
    					break;
    					
    				case WORD:
        				if ((kanji != null && kanji.equals(keyword)) ||
        					(reading != null && reading.equals(keyword))) {
        					isMatch = true;
        				}
    					break;
    				}
    				String progressInfo = currentProgress + "/" + maxProgress;
    				if (isMatch) {
    					this.publishProgress(keyword, 
    							getTitle(kanji, reading, etc), 
    							getDetail(catalog, mean),
    							progressInfo);
    				} else {
    					if (currentProgress % (maxProgress / 20) == 0) {
        					this.publishProgress(keyword,
        							progressInfo);    						
    					}
    				}
				}
			} catch (ReadPackException e) {
				e.printStackTrace();
				error = e.toString();
			}
    		return true;
		}

    	/**
    	 * @see publishProgress
    	 */
    	@Override
		protected void onProgressUpdate(String... values) {
    		if (searchTask != null) {
	    		if (values.length >= 4) {
	    			adapter.searchPackProgress(values[0], values[1], values[2], values[3]);
	    		} else {
	    			adapter.searchPackUpdateProgress(values[0], values[1]);
	    		}
    		}
		}

    	@Override
        protected void onPostExecute(Boolean result) {
    		if (result && !isFinishing()) {

            } else if (result == false) {
            	Toast.makeText(getApplicationContext(), 
            			error, 
            			Toast.LENGTH_SHORT)
            			.show();
            }
    		
    		//GC
    		this.bytes = null;
    		this.dict_size = null;
    		this.currentFields = null;
    		
    		searchTask = null;
    		adapter.searchEnd(this.keyword);
        }
    	
		public String getTitle(String kanji, String reading, String etc) {
    		String kanji2 = kanji;
    		if (kanji == null) {
    			kanji2 = reading;
    		}
    		String title = "";
    		if (kanji2 != null && kanji2.length() > 0) {
    			title = kanji2 + ((reading != null && reading.length() > 0) ? "【" + reading + "】" : "");
    		} else {
    			title = (reading != null ? reading : "");
    		}
    		if (etc != null) {
    			title = title + Word.getAccentStr(etc); 
    		}
    		return title;
    	}
    	
    	public String getDetail(String catalog, String mean) {
    		return ((catalog != null) ? "（" + catalog + "）\n" : "") + 
    				((mean != null) ? mean : "");    		
    	}
    	
    	public void loadAllBytes() throws ReadPackException {
			InputStream inputstream = null;
			try {
				String dataFileName;
				if (DataContext.USE_SEPARATE_PACK) {
					dataFileName = DataContext.DATA0_FILE_NAMES;
				} else {
					dataFileName = DataContext.DATA_FILE_NAMES;
				}
				inputstream = JkanjiTalkActivity.this.getAssets().open(
						dataFileName, 
						AssetManager.ACCESS_STREAMING);
				this.bytes = new byte[inputstream.available()];
				inputstream.read(this.bytes);
				this.pos = 0;
				this.dict_size = null;
				this.currentProgress = 0;
				this.maxProgress = 100;
				this.currentFields = new String[MAX_FIELD_SIZE];
			} catch (IOException e) {
				e.printStackTrace();
				throw new ReadPackException("loadAllBytes read file error");
			} finally {
				if (inputstream != null) {
					try {
						inputstream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					inputstream = null;
				}
			}
			readMagic();
			readItemCount();
    	}
    	
    	private void readMagic() throws ReadPackException {
    		if (pos + 4 > bytes.length) {
    			throw new ReadPackException("readMagic overflow");
    		}
    		if (bytes[pos++] != 'P' ||
    			bytes[pos++] != 'A' ||
    			bytes[pos++] != 'C' ||
    			bytes[pos++] != 'K') {
    			throw new ReadPackException("readMagic not PACK magic");
    		}
    	}
    	
    	public int readInt32() throws ReadPackException {
    		if (pos + 4 > bytes.length) {
    			throw new ReadPackException("readInt32 overflow");
    		}
    		int result = 0;
    		result |= (bytes[pos++] & 0xff) << 24;
    		result |= (bytes[pos++] & 0xff) << 16;
    		result |= (bytes[pos++] & 0xff) << 8;
    		result |= (bytes[pos++] & 0xff) << 0;
    		return result;
    	}
    	
    	public byte[] readBytes(int length) throws ReadPackException  {
    		if (pos + length > bytes.length) {
    			throw new ReadPackException("readInt32 overflow");
    		}
    		byte[] b = new byte[length];
    		System.arraycopy(this.bytes, pos, b, 0, length);
    		pos += length;
    		return b;
    	}
    	
    	public void readItemCount() throws ReadPackException {
    		currentProgress = 0;
    		dict_size = new int[readInt32()];
    		int progress = 0; //总长度
    		for (int i = 0; i < dict_size.length; i++) {
    			int n = readInt32();
    			dict_size[i] = n;
    			progress += n;
    		}
    		//dict_size[0] == 字典长度
    		//dict_size[1] == 转换表长度
    		this.maxProgress = dict_size[0]; //progress;
    	}
    	
    	public boolean readItem() throws ReadPackException {
    		if (currentProgress >= maxProgress) {
    			return false;
    		}
    		//if (D) {
    		//	Log.d(TAG, "readItem:" + currentProgress + " / " + maxProgress);
    		//}
    		int currentProgress = readInt32();
    		assert this.currentProgress == currentProgress;
    		int length = readInt32();
    		byte[] bytes = readBytes(length);
    		int p = 0;
    		for (int i = 0; i < MAX_FIELD_SIZE; i++) {
    			int strlen = 0;
    			strlen |= (bytes[p++] & 0xff) << 24;
    			strlen |= (bytes[p++] & 0xff) << 16;
    			strlen |= (bytes[p++] & 0xff) << 8;
    			strlen |= (bytes[p++] & 0xff) << 0;
    			String str = null;
    			if (strlen > 0) {
    				try {
    					str = new String(bytes, p, strlen, "UTF8");
    					p += strlen;
    				} catch (UnsupportedEncodingException e) {
    					e.printStackTrace();
    					throw new ReadPackException("readItem read string error");
    				}
    			}
    			//dict_items[currentProgress][i] = str;
    			currentFields[i] = str;
    		}
    		this.currentProgress++;
    		return true;
    	}
    }
    
    private final static class Word {
    	public String record;
    	
    	public String catalog;
    	public String reading;
    	public String kanji;
    	public String mean;	
    	public String etc;
    	
    	public Word(String record) {
    		this.record = record;
    		parse(record);
    	}
    	
    	private void parse(String record) {
    		String[] fields = record.split(",");
    		for(int i = 0; i < fields.length; i++) {
    			switch (i) {
    			case 0:
    				this.catalog = fields[i];
    				break;
    				
    			case 1:
    				this.reading = fields[i];
    				break;
    				
    			case 2:
    				this.kanji = fields[i];
    				break;
    				
    			case 3:
    				this.mean = fields[i];
    				break;
    				
    			case 4:
    				this.etc = fields[i];
    				break;
    				
    			default:
    				assert false;
    			}
    		}
    	}
    	
    	public String getTitle() {
    		String kanji2 = kanji;
    		if (kanji == null) {
    			kanji2 = reading;
    		}
    		if (kanji2 != null && kanji2.length() > 0) {
    			return kanji2 + ((reading != null && reading.length() > 0) ? "【" + reading + "】" : "");
    		} else {
    			return (reading != null ? reading : "");
    		}
    	}
    	
    	public String getDetail() {
    		return ((catalog != null && catalog.length() > 0) ? "【" + catalog + "】\n" : "") + 
    				((mean != null) ? CharTrans.formatMean(mean, false) : "");    		
    	}
    	
    	private final static String[] NUM_STRS = {
    		"(0)", "(1)", "(2)", "(3)", "(4)",
    		"(5)", "(6)", "(7)", "(8)", "(9)",
    	};
    	public static String getAccentStr(String etc) {
    		if (etc == null || etc.length() == 0) {
    			return "";
    		}
    		int len = etc.length();
    		StringBuffer sb = new StringBuffer();
    		for (int i = 0; i < len; i++){
    			String ch = etc.substring(i, i + 1);
    			try {
    				int num = Integer.parseInt(ch);
    				if (num >= 0 && num < NUM_STRS.length) {
    					sb.append(NUM_STRS[num]);
    				}
    			} catch (Throwable e) {
    				
    			}
    		}
    		return sb.toString();
    	}
    }
    
    private class TalkListAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private List<TalkListData> dataList;
		private long lastTime;
		private boolean showHead;
		private String mKeyword;
		
	    private class ViewHolder {
	        TextView textViewTalkTitle1;
	        TextView textViewTalkDetail1;
	        LinearLayout linearLayoutKuko;
	        LinearLayout linearLayoutKukoDlg;
	        TextView textViewTalkTitle2;
	        TextView textViewTalkDetail2;
	        LinearLayout linearLayoutNyaruko;
	        LinearLayout linearLayoutNyarukoDlg;
	        ImageView imageViewKugo;
	        ImageView imageViewNyaruko;
	    }
	    
    	public TalkListAdapter(Context context) {
    		inflater = LayoutInflater.from(context);
    		dataList = new ArrayList<TalkListData>();
    		this.showHead = JkanjiSettingActivity.getShowTalkIcon(context);
		}
    	
    	public void showHistory() {
    		clear();
    		for (int i = searchHistory.size() - 1; i >= 0; i--) {
    			String str = searchHistory.get(i);
    			__setItem(getCount(), JkanjiTalkActivity.TALK_HISTORY_ITEM, 
    					str, getCount(),
    					"搜索历史", str, null, null);
    		}
    	}
    	
    	/**
    	 * @see DictWebListActivity
    	 * @param keyword
    	 */
    	public void showLauncher(String keyword) {
    		if (keyword != null) {
    			talkInput.setText("");
    			talkInput.append(keyword);
    		}
    		searchHistory.add(keyword);
    		clear();
    		__setItem(0, JkanjiTalkActivity.TALK_LAUCHER_HEAD, 
    				keyword, JkanjiTalkActivity.TALK_POS_LAUNCHER_HEAD, 
    				"在线搜索", keyword, null, null);
    		__setItem(1, JkanjiTalkActivity.TALK_LAUCHER_ITEM, 
    				keyword, 0,
    				null, null, "HJdict（沪江小d）", null);
    		__setItem(2, JkanjiTalkActivity.TALK_LAUCHER_ITEM, 
    				keyword, 1, 
    				null, null, "excite", null);
    		__setItem(3, JkanjiTalkActivity.TALK_LAUCHER_ITEM, 
    				keyword, 2, 
    				null, null, "Yahoo!辞書", null);
    		__setItem(4, JkanjiTalkActivity.TALK_LAUCHER_ITEM, 
    				keyword, 3,
    				null, null, "SQLite搜索器", null);
    		__setItem(5, JkanjiTalkActivity.TALK_LAUCHER_ITEM, 
    				keyword, 4,
    				null, null, "共享方式", null);
    	}
    	
    	public void showHelp() {
    		clear();
    		__setItem(0, JkanjiTalkActivity.TALK_HELP_HEAD, 
    				null, JkanjiTalkActivity.TALK_POS_HELP_HEAD, 
    				null, "如何使用搜索会话？", null, null);
    		__setItem(1, JkanjiTalkActivity.TALK_HELP_ITEM, 
    				null, TALK_POS_HELP_ITEM,
    				null, null, null, "搜索会话以会话的形式显示搜索结果。");
    		__setItem(2, JkanjiTalkActivity.TALK_HELP_ITEM, 
    				null, TALK_POS_HELP_ITEM, 
    				null, null, null, "点击顶部的搜索图标或红色对话框将弹出一个快速动作菜单。点击白色对话框则弹出共享方式对话框。");
    		__setItem(3, JkanjiTalkActivity.TALK_HELP_ITEM, 
    				null, TALK_POS_HELP_ITEM, 
    				null, null, null, "支持前缀，包含与全文三种搜索匹配模式，但不支持日语假名输入，也不支持转换为日语汉字（繁异体变形）的匹配。");
    		__setItem(4, JkanjiTalkActivity.TALK_HELP_ITEM, 
    				null, TALK_POS_HELP_ITEM,
    				null, null, null, "另外，还可以选择在线翻译，然后点击白色对话框中要发送到的在线翻译网站名称，通过浏览器把搜索内容交给在线翻译网站进行查询（需要联网）。");
    		__setItem(5, JkanjiTalkActivity.TALK_HELP_HEAD, 
    				null, JkanjiTalkActivity.TALK_POS_HELP_HEAD, 
    				null, "为什么我的发带不是黑色的？为什么你的眼睛不是绿色的？", null, null);
    		__setItem(6, JkanjiTalkActivity.TALK_HELP_ITEM, 
    				null, TALK_POS_HELP_ITEM,
    				null, null, "......", null);    		
    		//
    		__setItem(7, JkanjiTalkActivity.TALK_HELP_ITEM, 
    				null, TALK_POS_HELP_ITEM,
    				null, null, "都是キャラクターなんとか機的错。", null);
    	}
    	
    	public void searchStart(String keyword) {
    		lastTime = System.currentTimeMillis();
    		if (keyword != null) {
    			talkInput.setText("");
    			talkInput.append(keyword);
    		}
    		searchHistory.add(keyword);
    		setKeyword(keyword);
    		clear();
    		__setItem(0, JkanjiTalkActivity.TALK_SEARCH_HEAD, 
    				keyword, JkanjiTalkActivity.TALK_POS_SEARCH_HEAD,  
    				"搜索开始:" + keyword, "开始搜索", null, null);
    	}
    	
    	public void searchProgress(String keyword, String meaning) {
    		__setItem(0, JkanjiTalkActivity.TALK_SEARCH_HEAD, 
    				keyword, JkanjiTalkActivity.TALK_POS_SEARCH_HEAD, 
    				"搜索中:" + keyword, "发现匹配项" + (getCount() - 1), null, null);
    		
    		Word word = new Word(meaning);
    		String title = word.getTitle();
    		String detail = word.getDetail();
    		
    		__setItem(getCount(), JkanjiTalkActivity.TALK_SEARCH_ITEM, 
    				meaning, getCount(), 
    				null, null, title, detail);
    	}
    	
    	public void searchPackProgress(String keyword, String title, String detail, String progressInfo) {
    		__setItem(0, JkanjiTalkActivity.TALK_SEARCH_HEAD, 
    				keyword, JkanjiTalkActivity.TALK_POS_SEARCH_HEAD, 
    				"搜索中:" + keyword, "(" + progressInfo + ")发现匹配项" + (getCount() - 1), null, null);
    		
    		String meaning = (title != null) ? (title + detail) : "";
    		
    		__setItem(getCount(), JkanjiTalkActivity.TALK_SEARCH_ITEM, 
    				meaning, getCount(), 
    				null, null, title, detail);
    	}
    	
    	public void searchPackUpdateProgress(String keyword, String progressInfo) {
    		__setItem(0, JkanjiTalkActivity.TALK_SEARCH_HEAD, 
    				keyword, JkanjiTalkActivity.TALK_POS_SEARCH_HEAD, 
    				"搜索中:" + keyword, "(" + progressInfo + ")发现匹配项" + (getCount() - 1), null, null);    		
    	}
    	
    	public void searchEnd(String keyword) {
    		__setItem(0, JkanjiTalkActivity.TALK_SEARCH_HEAD, 
    				keyword, JkanjiTalkActivity.TALK_POS_SEARCH_HEAD, 
    				"搜索完成:" + keyword, "发现匹配项" + (getCount() - 1) + ", 耗时" + ((System.currentTimeMillis() - lastTime) / 1000.0) + "s", null, null);
    		lastTime = 0;
    		if (actionBar != null) {
    			showActionBarButtons();
    		}
    	}
    	
    	private void __setItem(int index, 
    			int type, String info, int pos,
    			String title1, String detail1, 
    			String title2, String detail2) {
    		if (dataList.size() - 1 < index) {
    			for (int i = dataList.size(); i <= index; i++) {
    				dataList.add(new TalkListData());
    			}
    		}
    		if (dataList != null && index >= 0 && index < dataList.size()) {
	    		TalkListData data = dataList.get(index);
	    		data.type = type;
	    		data.info = info;
	    		data.pos = pos;
	    		data.title1 = title1;
	    		data.detail1 = detail1;
	    		data.title2 = title2;
	    		data.detail2 = detail2;
	    	}
    		this.notifyDataSetChanged();
    	}
    	
    	public void clear() {
    		if (dataList != null) {
    			dataList.clear();
    		}
    		this.notifyDataSetChanged();
    	}
    	
    	@Override
		public int getCount() {
    		if (dataList == null) {
    			return 0;
    		}
			return dataList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.talk_item, null);
				holder = new ViewHolder();
				
				holder.linearLayoutKuko = (LinearLayout) convertView.findViewById(R.id.linearLayoutKuko);
				holder.linearLayoutKukoDlg = (LinearLayout) convertView.findViewById(R.id.linearLayoutKukoDlg);
				holder.textViewTalkTitle1 = (TextView) convertView.findViewById(R.id.textViewTalkTitle1);
				holder.textViewTalkDetail1 = (TextView) convertView.findViewById(R.id.textViewTalkDetail1);				
				holder.linearLayoutNyaruko = (LinearLayout) convertView.findViewById(R.id.linearLayoutNyaruko);
				holder.linearLayoutNyarukoDlg = (LinearLayout) convertView.findViewById(R.id.linearLayoutNyarukoDlg);
				holder.textViewTalkTitle2 = (TextView) convertView.findViewById(R.id.textViewTalkTitle2);
				holder.textViewTalkDetail2 = (TextView) convertView.findViewById(R.id.textViewTalkDetail2);	
				holder.imageViewKugo = (ImageView) convertView.findViewById(R.id.imageViewKuko);
				holder.imageViewNyaruko = (ImageView) convertView.findViewById(R.id.imageViewNyaruko);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			//FIXME: Japanese Font
			holder.textViewTalkTitle2.setTypeface(typeface);
			
			if (dataList != null && position >= 0 && position < dataList.size()) {
				TalkListData data = dataList.get(position);
				if (data.title1 == null && data.detail1 == null) {
					holder.textViewTalkTitle1.setText(null);
					holder.textViewTalkDetail1.setText(null);
					holder.linearLayoutKuko.setVisibility(LinearLayout.GONE);
				} else {
					if (data.title1 == null) {
						holder.textViewTalkTitle1.setVisibility(TextView.GONE);
					} else {
						holder.textViewTalkTitle1.setVisibility(TextView.VISIBLE);
					}
					if (data.detail1 == null) {
						holder.textViewTalkDetail1.setVisibility(TextView.GONE);
					} else {
						holder.textViewTalkDetail1.setVisibility(TextView.VISIBLE);
					}
					holder.textViewTalkTitle1.setText(data.title1);
					holder.textViewTalkDetail1.setText(data.detail1);
					holder.linearLayoutKuko.setVisibility(LinearLayout.VISIBLE);
				}
				if (data.title2 == null && data.detail2 == null) {
					holder.textViewTalkTitle2.setText(null);
					holder.textViewTalkDetail2.setText(null);
					holder.linearLayoutNyaruko.setVisibility(LinearLayout.GONE);
				} else {
					if (data.title2 == null) {
						holder.textViewTalkTitle2.setVisibility(TextView.GONE);
					} else {
						holder.textViewTalkTitle2.setVisibility(TextView.VISIBLE);
					}
					if (data.detail2 == null) {
						holder.textViewTalkDetail2.setVisibility(TextView.GONE);
					} else {
						holder.textViewTalkDetail2.setVisibility(TextView.VISIBLE);
					}
					holder.textViewTalkTitle2.setText(getColorString(data.title2, mKeyword));
					holder.textViewTalkDetail2.setText(getColorString(data.detail2, mKeyword));
					holder.linearLayoutNyaruko.setVisibility(LinearLayout.VISIBLE);
				}
				final int type = data.type;
				final String info = data.info;
				final int pos = data.pos;
				final String title2 = data.title2;
				final String detail2 = data.detail2;
				holder.linearLayoutKukoDlg.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						showQuickAction(v, type, info, pos);
					}
				});
				holder.linearLayoutNyarukoDlg.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						launcherStart(v, type, info, pos, title2, detail2);
					}
				});
			} else {
				holder.textViewTalkTitle1.setText(null);
				holder.textViewTalkDetail1.setText(null);
				holder.linearLayoutKuko.setVisibility(LinearLayout.GONE);
				holder.textViewTalkTitle2.setText(null);
				holder.textViewTalkDetail2.setText(null);
				holder.linearLayoutNyaruko.setVisibility(LinearLayout.GONE);				
				holder.linearLayoutKukoDlg.setOnClickListener(null);
				holder.linearLayoutNyarukoDlg.setOnClickListener(null);
			}
			if (showHead) {
				holder.imageViewKugo.setVisibility(ImageView.VISIBLE);
				holder.imageViewNyaruko.setVisibility(ImageView.VISIBLE);
			} else {
				holder.imageViewKugo.setVisibility(ImageView.GONE);
				holder.imageViewNyaruko.setVisibility(ImageView.GONE);
			}
			return convertView;
		}
		
		public void setKeyword(String keyword) {
			mKeyword = keyword;
		}
    }
    
    private CharSequence getColorString(String str, String keyword) {
    	if (str != null) {
    		SpannableString spannable = new SpannableString(str);
    		if (keyword != null && keyword.length() > 0) {
	    		switch (JkanjiSettingActivity.getHLType(this)) {
	    		default:
	        	case JkanjiSettingActivity.HL_TYPE_CHAR: {
		    			for (int i = 0; i < str.length(); i++) {
		    				if (keyword.indexOf(str.charAt(i)) >= 0) {
		    					ForegroundColorSpan span = new ForegroundColorSpan(Color.RED);
		    					spannable.setSpan(span, i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		    				}
		    			}
		        	}
	        		break;
	        		
	    		case JkanjiSettingActivity.HL_TYPE_STRING:
	    			int start = 0;
	    			while (start < str.length()) {
	    				int index = str.indexOf(keyword, start);
	    				if (index == -1) {
	    					break;
	    				} else {
	    					ForegroundColorSpan span = new ForegroundColorSpan(Color.RED);
	    					spannable.setSpan(span, index, index + keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    					start = index + keyword.length();
	    				}
	    			}
	    			break;
	    			
	    		case JkanjiSettingActivity.HL_TYPE_NONE:
	    			break;
	    		}
    		}
    		return spannable;
    	} else {
    		return null;
    	}
    }
}
