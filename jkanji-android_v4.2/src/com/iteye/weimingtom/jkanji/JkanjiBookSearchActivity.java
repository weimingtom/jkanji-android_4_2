package com.iteye.weimingtom.jkanji;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.markupartist.android.widget.ActionBar;

public class JkanjiBookSearchActivity extends Activity implements OnItemClickListener {
	private final static boolean D = false;
	private final static String TAG = "JkanjiBookSearchActivity";
	
	private final static int MAX_COUNT = 20000;
	
	private final static int FULLTEXT_SEARCH_ALL = 0;
	private final static int FULLTEXT_SEARCH_WORD = 1;
	private final static int FULLTEXT_SEARCH_MEANING = 2;
	private final static String[] TYPE_NAMES = {
		"日中",
		"日文",
		"中文",
	};
	
	public final static String EXTRA_KEY_SEARCH_WORD = "com.iteye.weimingtom.jkanji.JkanjiBookSearchActivity.searchWord";

	private static final String SHARE_PREF_NAME = "pref";
	private static final String SHARE_PREF_BOOK_SEARCH_INPUT_TEXT = "bookSearchInputString";
	
	private ActionBar actionBar;
	private ListView viewListView;
	private EditText searchInput;
	private Button searchButton;
	private Typeface typeface;
	private List<WordRecord> records;
	private EfficientAdapter adapter;
	
	private Spinner spinnerSearchType;
	private ArrayAdapter<String> spinnerSearchTypeAdapter;
	private Spinner spinnerSearchDictId;
	private ArrayAdapter<String> spinnerSearchDictIdAdapter;
	
	private TextView textViewMessage;
	private LinearLayout linearLayoutHead;
	
	private TextView textViewLoading;

	private static final int REQUEST_HANDINPUT = 1;
	
	private boolean alreadyStarted = false;
	private boolean isHeadVisible = true;

	private final int[] DICT_FILE_MAX = {
		1137,
	};
	private final String[] DICT_PATH = {
		"markdown/book012/",
	};
	private final String[] DICT_NAME = {
	    "惯用句型大全",
	};
	
	private final static int CONTEXT_MENU_VIEW = ContextMenu.FIRST + 1;
	private final static int CONTEXT_MENU_EDIT = ContextMenu.FIRST + 2;
	
	private LoadDataTask mTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.book_search);
		
		viewListView = (ListView) this.findViewById(R.id.viewListView);
		searchButton = (Button) this.findViewById(R.id.searchButton);
		searchInput = (EditText) this.findViewById(R.id.searchInput);
		textViewLoading = (TextView) this.findViewById(R.id.textViewLoading);
		
		textViewMessage = (TextView) this.findViewById(R.id.textViewMessage);
		//buttonThreadStart = (Button) this.findViewById(R.id.buttonThreadStart);
		//buttonThreadEnd = (Button) this.findViewById(R.id.buttonThreadEnd);
		
		linearLayoutHead = (LinearLayout) this.findViewById(R.id.linearLayoutHead);
		
        spinnerSearchType = (Spinner) this.findViewById(R.id.spinnerSearchType);
        spinnerSearchTypeAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_spinner_item, TYPE_NAMES);
        spinnerSearchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSearchType.setAdapter(spinnerSearchTypeAdapter);
        
        spinnerSearchDictId = (Spinner) this.findViewById(R.id.spinnerSearchDictId);
        spinnerSearchDictIdAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_spinner_item, DICT_NAME);
        spinnerSearchDictIdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSearchDictId.setAdapter(spinnerSearchDictIdAdapter);
        
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("书籍搜索");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.search;
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
				return R.drawable.search_web;
			}

			@Override
			public void performAction(View view) {
				String str = searchInput.getText().toString();
				startActivity(new Intent(JkanjiBookSearchActivity.this, DictWebListActivity.class)
					.putExtra(DictWebListActivity.EXTRA_KEY, str));
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.config;
			}

			@Override
			public void performAction(View view) {
				if (linearLayoutHead.getVisibility() == LinearLayout.VISIBLE) {
					linearLayoutHead.setVisibility(LinearLayout.GONE);
					isHeadVisible = false;
				} else {
					linearLayoutHead.setVisibility(LinearLayout.VISIBLE);
					isHeadVisible = true;
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
				Intent intent = new Intent(JkanjiBookSearchActivity.this, 
						JkanjiHandActivity.class);
				intent.putExtra(JkanjiHandActivity.EXTRA_KEY_INIT_STRING, 
						searchInput.getText().toString());
				startActivityForResult(intent, REQUEST_HANDINPUT);
			}
        });
        
		records = new ArrayList<WordRecord>();
		typeface = Typefaces.getFile(JkanjiSettingActivity.getFontFileName(this));	
		adapter = new EfficientAdapter(this, records, typeface);
		viewListView.setAdapter(adapter);
		viewListView.setFastScrollEnabled(true);
		viewListView.setSelection(0);
		viewListView.setOnItemClickListener(this);
		registerForContextMenu(viewListView);
		
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onSearch();
			}
		});
		searchInput.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					onSearch();
				}
				return false;
			}
        });
		
		if (savedInstanceState != null) {
			alreadyStarted = savedInstanceState.getBoolean("alreadyStarted", false);
			isHeadVisible = savedInstanceState.getBoolean("isHeadVisible", true);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (outState != null) {
			outState.putBoolean("alreadyStarted", true);
			outState.putBoolean("isHeadVisible", linearLayoutHead.getVisibility() == LinearLayout.VISIBLE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!isHeadVisible) {
			linearLayoutHead.setVisibility(LinearLayout.GONE);
		} else {
			linearLayoutHead.setVisibility(LinearLayout.VISIBLE);
		}
		
		Intent intent = this.getIntent();
		if (intent != null) {
			String searchWord = intent.getStringExtra(EXTRA_KEY_SEARCH_WORD);
			if (!alreadyStarted) {
				alreadyStarted = true;
				searchInput.setText("");
				searchInput.append(getLastSearchInput());
				if (searchWord != null) {
					searchInput.setText("");
					searchInput.append(searchWord);
					isHeadVisible = false;
					linearLayoutHead.setVisibility(LinearLayout.GONE);
				} else {
					
				}
				//onSearch();
			} else {
				
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	private final static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<WordRecord> records;
        private Typeface typeface;
        private String keyword;
        private Context mContext;
        
        public EfficientAdapter(Context context, List<WordRecord> records, Typeface typeface) {
            this.mContext = context;
            this.mInflater = LayoutInflater.from(context);
            this.records = records;
            this.typeface = typeface;
        }
        
        public void setKeyword(String str) {
        	keyword = str;
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
                holder.title.setTypeface(typeface);
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
            holder.title.setText(getColorString(title, keyword));
            holder.text.setText(getColorString(text, keyword));
            
            return convertView;
        }
        
        private CharSequence getColorString(String str, String keyword) {
        	if (str != null) {
        		SpannableString spannable = new SpannableString(str);
        		if (keyword != null && keyword.length() > 0) {
    	    		switch (JkanjiSettingActivity.getHLType(mContext)) {
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

        private class ViewHolder {
        	TextView title;
            TextView text;
        }
    }
	
	private class WordRecord {
		public String word;
		public String meaning;
		public int dict_id;
		public String filename;
		
		public WordRecord(String word, String meaning, int dict_id, String filename) {
			this.word = word;
			this.meaning = meaning;
			this.dict_id = dict_id;
			this.filename = filename;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (false) {
			WordRecord wd = records.get(position);
			if (wd != null) {
				//FIXME:wd.id
				Word word = new Word(/*wd.id*/-1, new String[]{null, null, wd.word, wd.meaning, null});
				startActivity(new Intent(this, WordEditActivity.class)
					.putExtra(WordEditActivity.EXTRA_DATA, word));
			}
		} else {
			this.openContextMenu(view);
		}
	}
	
	private int getSearchType() {
		switch (spinnerSearchType.getSelectedItemPosition()) {
		case 0:
			return FULLTEXT_SEARCH_ALL;
		
		case 1:
			return FULLTEXT_SEARCH_WORD;
		
		case 2:
			return FULLTEXT_SEARCH_MEANING;
			
		default:
			return FULLTEXT_SEARCH_ALL;
		}
	}
	
	private int getSearchDict() {
		return spinnerSearchDictId.getSelectedItemPosition();
	}

	//FIXME:
	/**
	 * 用户线程
	 */
	private String getTypeInfo(int type) {
		String typeInfo = "";
		int dictId = getSearchDict();
		if (dictId >= 0 && dictId < DICT_NAME.length) {
			typeInfo += DICT_NAME[dictId];
		}
		switch (type) {
		case FULLTEXT_SEARCH_ALL:
			typeInfo += ",日中";
			break;
			
		case FULLTEXT_SEARCH_WORD:
			typeInfo += ",日文";
			break;
			
		case FULLTEXT_SEARCH_MEANING:
			typeInfo += ",中文";
			break;
		}
		return typeInfo;
	}
	
	/**
	 * 用户线程搜索
	 */
	private void onSearch() {
		if (searchInput != null && searchInput.getText() != null) {
			String text = searchInput.getText().toString();
			int type = getSearchType();
			int dictId = getSearchDict();
			mTask = new LoadDataTask();
			mTask.execute(text, Integer.toString(type), Integer.toString(dictId));
		}
	}
	
	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)
				getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (mTask != null) {
			mTask.setStop(true);
		}
	}

	private void showInfo(String text) {
		this.textViewMessage.setText(text);
	}
		
    @Override
	protected void onPause() {
		super.onPause();
		String text = this.searchInput.getText().toString();
		setLastSearchInput(text);
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
					searchInput.setText("");
					searchInput.append(resultString);
				}
				linearLayoutHead.setVisibility(LinearLayout.VISIBLE);
				this.isHeadVisible = true;
				onSearch();
				break;
			}
		}
	}
    
	private class LoadDataTask extends AsyncTask<String, Integer, Boolean> {
		private boolean loadResult = false;
		private String text;
		private long lastTime;
		private List<WordRecord> resultRecords;
		private String path = "";
		private int maxFileNum = 0;
		private boolean isStop = false;
		
		public void setStop(boolean value) {
			isStop = value;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			textViewLoading.setText("书籍搜索中...");
			textViewLoading.setVisibility(View.VISIBLE);
			viewListView.setVisibility(View.INVISIBLE);
			searchButton.setEnabled(false);
			textViewMessage.setVisibility(View.GONE);
			hideKeyboard();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				this.text = params[0];
				int type = Integer.parseInt(params[1]);
				int dictId = Integer.parseInt(params[2]);
				path = DICT_PATH[dictId];
				maxFileNum = DICT_FILE_MAX[dictId];
				this.lastTime = System.currentTimeMillis();
				resultRecords = search(text, type, dictId);
				if (resultRecords != null) {
					loadResult = true;
				} else {
					loadResult = false;
				}
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		private ArrayList<WordRecord> search(String keyword, int type, int dictId) {
			ArrayList<WordRecord> result = new ArrayList<WordRecord>();
			if (keyword != null && keyword.length() > 0) {
				int beginFilenameLen = getBeginFilenameLen();
				for (int index = 1; 
					!isStop && loadFromFile(result, keyword, type, dictId, index, beginFilenameLen); 
					++index) {
					;
				}
			}
			return result;
		}
		
		private int getBeginFilenameLen() {
			InputStream istr = null;
			try {
				istr = getAssets().open(this.path + "001.txt");
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (istr != null) {
					try {
						istr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return 3;
				}
			}
			try {
				istr = getAssets().open(this.path + "0001.txt");
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (istr != null) {
					try {
						istr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return 4;
				}
			}
			return 0;
		}
		
		private boolean loadFromFile(ArrayList<WordRecord> result, 
				String keyword, int type, int dictId, 
				int index, int beginFilenameLen) {
			String strIndex = "";
			if (beginFilenameLen == 3) { // "001.txt"
				if (index < 10) {
					strIndex = "00" + index; 
				} else if (index < 100) {
					strIndex = "0" + index;
				} else {
					strIndex = "" + index;
				}
			} else if (beginFilenameLen == 4) { // "0001.txt"
				if (index < 10) {
					strIndex = "000" + index; 
				} else if (index < 100) {
					strIndex = "00" + index;
				} else if (index < 1000) {
					strIndex = "0" + index;
				} else {
					strIndex = "" + index;
				}
			} else {
				return false;
			}
			String filename = this.path + strIndex + ".txt";
			InputStream istr = null;
			InputStreamReader reader = null;
			BufferedReader rbuf = null;
			try {
				istr = getAssets().open(filename);
				reader = new InputStreamReader(istr, "utf8");
				rbuf = new BufferedReader(reader);
				String line;
				String word = null;
				String mean = null;
				while (null != (line = rbuf.readLine())) {
					if (line.startsWith("> # ")) {
						if (word == null) {
							word = line.replace("> # ", "");
						} else {
							word = word + "\n" + line.replace("> # ", "");
						}
					} else if (line.startsWith("> ")) {
						if (word == null) {
							word = line.replace("> ", "");
						} else {
							word = word + "\n" + line.replace("> ", "");
						}						
					} else if (line.length() > 0) {
						if (mean == null) {
							mean = line;
						} else {
							mean = mean + "\n" + line;
						}
					} else if (line.length() == 0) {
						if (mean != null) {
							if (keyword != null && keyword.length() > 0) {
								switch (type) {
								default:
								case FULLTEXT_SEARCH_ALL:
									if ((word != null && word.contains(keyword)) || 
										(mean != null && mean.contains(keyword))) {
										WordRecord record = new WordRecord(word, mean, dictId, filename);
										result.add(record);
									}
									break;
									
								case FULLTEXT_SEARCH_WORD:
									if (word != null && word.contains(keyword)) { 
										WordRecord record = new WordRecord(word, mean, dictId, filename);
										result.add(record);
									}
									break;
									
								case FULLTEXT_SEARCH_MEANING:
									if (mean != null && mean.contains(keyword)) { 
										WordRecord record = new WordRecord(word, mean, dictId, filename);
										result.add(record);
									}
									break;
								}
							}
							word = null;
							mean = null;
						}
					}
				}
				this.publishProgress(Integer.valueOf(index));
			} catch (IOException e) {
				//e.printStackTrace();
				return false;
			} finally {
				if (rbuf != null) {
					try {
						rbuf.close();
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
				if (istr != null) {
					try {
						istr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			textViewLoading.setText("书籍搜索中(" + values[0] + " / " + maxFileNum + ")");
		}

		@Override
		protected void onPostExecute(Boolean result) {
			textViewLoading.setVisibility(View.INVISIBLE);
			viewListView.setVisibility(View.VISIBLE);
			searchButton.setEnabled(true);
			textViewMessage.setVisibility(View.VISIBLE);
			if (result == true && !JkanjiBookSearchActivity.this.isFinishing()) {
				records.clear();
				adapter.notifyDataSetChanged();
				adapter.setKeyword(text);
				if (loadResult) {
					if (result != null && resultRecords != null) {
						for (WordRecord record : resultRecords) {
							records.add(record);
						}
					}
					adapter.notifyDataSetChanged();
					viewListView.postDelayed(new Runnable() {
						@Override
						public void run() {
							viewListView.setSelection(0);
						}
					}, 100);
					long time = (System.currentTimeMillis() - lastTime);
					//System.out.println("time : " + time);
					int count = adapter.getCount();
					if (text.length() > 0) {
						String typeInfo = getTypeInfo(getSearchType());
						if (count < MAX_COUNT) {
							showInfo(
								typeInfo + "" +
								":" + text + "," +
								"" + (time / 1000.0) + "s," + 
								"" + count
							);
						} else {
							showInfo( 
								typeInfo + "" +
								":" + text + "," +
								"" + (time / 1000.0) + "s\n" + 
								"警告:结果超过" + MAX_COUNT + "条" //+ 
							);
						}
					} else {
						showInfo(  
							"请输入关键词。\n"
						);
					}
				} else {
					showInfo(  
						"搜索错误"
					);
					records.clear();
				}
				hideKeyboard();
			} else if (result == false) {
				finish();
			}
		}
    }
	
    private void setLastSearchInput(String str) {
		PrefUtil.putString(this, SHARE_PREF_NAME,
				SHARE_PREF_BOOK_SEARCH_INPUT_TEXT,
				str);
    }
    
    private String getLastSearchInput() {
		return PrefUtil.getString(this, SHARE_PREF_NAME,
				SHARE_PREF_BOOK_SEARCH_INPUT_TEXT,
				"");
    }
    
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
    	super.onCreateContextMenu(menu, v, info);
    	menu.add(0, CONTEXT_MENU_VIEW, 0, "查看");
    	menu.add(0, CONTEXT_MENU_EDIT, 0, "编辑");
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    	if (info != null) {
    		if (D) {
    			Log.e(TAG, "onContextItemSelected " + item.getItemId() + "," + info.position);
    		}
    		WordRecord wd;
    		switch (item.getItemId()) {
	    	case CONTEXT_MENU_VIEW:
	    		wd = records.get(info.position);
	    		if (wd != null) {
	    			this.startActivity(new Intent(this, 
    					JkanjiMarkdownActivity.class)
    					.putExtra(JkanjiMarkdownActivity.FILENAME_KEY, wd.filename)
    					.putExtra(JkanjiMarkdownActivity.TITLE_KEY, wd.filename)
    				);
	    		}
	    		break;
	    	
	    	case CONTEXT_MENU_EDIT:
	    		wd = records.get(info.position);
	    		if (wd != null) {
	    			Word word = new Word(-1, new String[]{null, null, wd.word, wd.meaning, null});
	    			startActivity(new Intent(this, WordEditActivity.class)
	    				.putExtra(WordEditActivity.EXTRA_DATA, word));
	    		}
	    		break;
	    	}
    	} else {
    		if (D) {
    			Log.e(TAG, "onContextItemSelected null");
    		}
    	}
    	return super.onContextItemSelected(item);
    }
    
    
}
