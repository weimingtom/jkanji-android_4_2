package com.iteye.weimingtom.jkanji;

import java.util.ArrayList;
import java.util.List;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import fi.harism.curl.CurlActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * D:/ugame/mydroid_data/database
 * @author Administrator
 *
 */
public class SQLiteReaderActivity extends Activity implements OnItemClickListener {
	private final static boolean D = false;
	private final static String TAG = "SQLiteReaderActivity";
		
	private final static String TIPS = "小提示：建议选择字典类型";
	private final static String TIPS2 = "小提示：使用手写板变换日文汉字。";
	
	private final static boolean USE_FULLTEXT_LIKE = true;
	
	private final static int MAX_COUNT = 20000;
	
	private final static int FULLTEXT_SEARCH_FULL = 0;
	private final static int FULLTEXT_SEARCH_PREFIX = 1;
	private final static int FULLTEXT_SEARCH_INFIX = 2;
	private final static int FULLTEXT_SEARCH_INFIX_ALL = 3;	
	private final static int FULLTEXT_SEARCH_INFIX2 = 4;
	private final static int FULLTEXT_SEARCH_INFIX_ALL2 = 5;	
	
	public final static String EXTRA_KEY_SEARCH_WORD = "com.iteye.weimingtom.jkanji.SQLiteReaderActivity.searchWord";
	public final static String EXTRA_KEY_SEARCH_TYPE = "com.iteye.weimingtom.jkanji.SQLiteReaderActivity.searchType";
		
	private static final boolean IS_FULL_TEXT = true; // no fulltext:37207; fulltext:
	private static final String PATH;
	static {
		if (IS_FULL_TEXT) {
			PATH = "/db/dict_fulltext.sqlite";
		} else {
			PATH = "/db/dict.sqlite";
		}
	}
	private static final String PATH_EXAMPLES;
	static {
		if (IS_FULL_TEXT) {
			PATH_EXAMPLES = "/db/examples_fulltext.sqlite";
		} else {
			PATH_EXAMPLES = "/db/examples.sqlite";
		}
	}
	
	private static final boolean USE_TASK = true;
	
	private static final String TABLE_NAME = "words";
	
	private static final String SHARE_PREF_NAME = "pref";
	private static final String SHARE_PREF_SQLITE_READER_INPUT_TEXT = "sqliteReaderInputString";
	
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
	
	private RadioButton radioButtonDict, radioButtonExamples;
	private TextView textViewMessage;
	private LinearLayout linearLayoutHead;
	
	private TextView textViewLoading;
	
	private static final int DIALOG_LOADING_ID = 1;
	
	private AlertDialog.Builder builder;
	
	private static final int REQUEST_HANDINPUT = 1;
	
	public static final int TYPE_EXAMPLES = 1;
	
	private boolean alreadyStarted = false;
	private boolean isHeadVisible = true;
	
	private final String[] DICT_NAME = {
	    "不限词典类型",
	    "edict（日英）",
	    "edict2（日英）",
	    "WadokuJT（日德）",
	    "jpwords（日中未校对版）",
	    "MuiltDic（日中）",
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.sqlite_reader);
		
		viewListView = (ListView) this.findViewById(R.id.viewListView);
		searchButton = (Button) this.findViewById(R.id.searchButton);
		searchInput = (EditText) this.findViewById(R.id.searchInput);
		textViewLoading = (TextView) this.findViewById(R.id.textViewLoading);
		
		textViewMessage = (TextView) this.findViewById(R.id.textViewMessage);
		//buttonThreadStart = (Button) this.findViewById(R.id.buttonThreadStart);
		//buttonThreadEnd = (Button) this.findViewById(R.id.buttonThreadEnd);
		
		radioButtonDict = (RadioButton) this.findViewById(R.id.radioButtonDict);
		radioButtonExamples = (RadioButton) this.findViewById(R.id.radioButtonExamples);
		linearLayoutHead = (LinearLayout) this.findViewById(R.id.linearLayoutHead);
		
        spinnerSearchType = (Spinner) this.findViewById(R.id.spinnerSearchType);
        spinnerSearchTypeAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_spinner_item);
        spinnerSearchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSearchTypeAdapter.add("完全");
        spinnerSearchTypeAdapter.add("前缀");
        spinnerSearchTypeAdapter.add("包含");
        spinnerSearchTypeAdapter.add("全文");
        spinnerSearchTypeAdapter.add("精确包含");
        spinnerSearchTypeAdapter.add("精确全文");
        spinnerSearchType.setAdapter(spinnerSearchTypeAdapter);
        spinnerSearchType.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int postion, long id) {
//				if (postion > 1) {
//					showInfo(TIPS);
//				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
        });
        
        spinnerSearchDictId = (Spinner) this.findViewById(R.id.spinnerSearchDictId);
        spinnerSearchDictIdAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_spinner_item);
        spinnerSearchDictIdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (int i = 0; i < DICT_NAME.length; i++) {
        	spinnerSearchDictIdAdapter.add(DICT_NAME[i]);
        }
        spinnerSearchDictId.setAdapter(spinnerSearchDictIdAdapter);
        
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("sqlite搜索器");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.search_sqlite;
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
				startActivity(new Intent(SQLiteReaderActivity.this, DictWebListActivity.class)
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
				Intent intent = new Intent(SQLiteReaderActivity.this, 
						JkanjiHandActivity.class);
				intent.putExtra(JkanjiHandActivity.EXTRA_KEY_INIT_STRING, 
						searchInput.getText().toString());
				startActivityForResult(intent, REQUEST_HANDINPUT);
			}
        });
        
		builder = new AlertDialog.Builder(this);
		
		records = new ArrayList<WordRecord>();
		//typeface = Typefaces.get(this, "mplus-1m-regular.ttf");
		typeface = Typefaces.getFile(JkanjiSettingActivity.getFontFileName(this));	
		adapter = new EfficientAdapter(this, records, typeface);
		viewListView.setAdapter(adapter);
		viewListView.setFastScrollEnabled(true);
		viewListView.setSelection(0);
		viewListView.setOnItemClickListener(this);
		
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
		
//		radioButton1.setSelected(true);
//		radioButton1.setChecked(true);
//		radioButtonDict.setSelected(true);
//		radioButtonDict.setChecked(true);

		radioButtonDict.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					spinnerSearchDictId.setEnabled(true);
				}
			}
		});
		radioButtonExamples.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					spinnerSearchDictId.setEnabled(false);
				}
			}
		});
		
		if (savedInstanceState != null) {
			alreadyStarted = savedInstanceState.getBoolean("alreadyStarted", false);
			isHeadVisible = savedInstanceState.getBoolean("isHeadVisible", true);
		}
		
		/*
		buttonThreadStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sqliteThread = new SqliteThread();
				sqliteThread.start();
			}
		});
		buttonThreadEnd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (sqliteThread != null) {
					sqliteThread.closeDB();
					try {
						sqliteThread.join(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					sqliteThread = null;
				}
			}
		});
		*/	
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
		/*
		InputMethodManager inputMethodManager = (InputMethodManager)
				getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(searchInput.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
		*/
		if (!isHeadVisible) {
			linearLayoutHead.setVisibility(LinearLayout.GONE);
		} else {
			linearLayoutHead.setVisibility(LinearLayout.VISIBLE);
		}
		
		Intent intent = this.getIntent();
		if (intent != null) {
			String searchWord = intent.getStringExtra(EXTRA_KEY_SEARCH_WORD);
			//searchInput.setText(searchWord);
			//searchInput.setText("明日");
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
				int searchType = intent.getIntExtra(EXTRA_KEY_SEARCH_TYPE, 0);
				if (searchType == TYPE_EXAMPLES) {
					radioButtonExamples.setChecked(true);
				} else {
					radioButtonDict.setChecked(true);
				}
				onSearch();
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
		public int id;
		public String word;
		public String meaning;
		public int dict_id;
		
		public WordRecord(int id, String word, String meaning, int dict_id) {
			this.id = id;
			this.word = word;
			this.meaning = meaning;
			this.dict_id = dict_id;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		WordRecord wd = records.get(position);
		if (wd != null) {
			//FIXME:wd.id
			Word word = new Word(/*wd.id*/-1, new String[]{null, null, wd.word, wd.meaning, null});
			startActivity(new Intent(this, WordEditActivity.class)
				.putExtra(WordEditActivity.EXTRA_DATA, word));
		}
	}
	
	private static String tokenize(String str) {
		//return new StringBuffer(str).reverse().toString();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			sb.append(c);
			if (c > 256) {
				sb.append(' ');
			}
		}
		return sb.toString();
	}
	
	/**
	 * 用户线程或后台线程
	 * !!!NOTE!!! Single Thread Query
	 * @see com.iteye.weimingtom.jkanji.tools.DictDump
	 */
	private ArrayList<WordRecord> search(String keyword, int type, String dbPath, int dictId) {
		SQLiteDatabase db = null;
		ArrayList<WordRecord> result = new ArrayList<WordRecord>();
		//
		try {
			db = SQLiteDatabase.openDatabase(JkanjiSettingActivity.getDataPackPath(this) + dbPath, 
				null, 
				SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
//			db.setLockingEnabled(false);
			Cursor cursor = null;
			String selection = null;
			String[] selectionArgs = null;
			String[] columns = null;
			if (IS_FULL_TEXT) {
				switch (type) {
				case FULLTEXT_SEARCH_FULL:
					if (dictId > 0 && PATH.equals(dbPath)) {
						selection = "dict_id == " + dictId + " AND word MATCH ?";
					} else {
						selection = "word MATCH ?";
					}
					selectionArgs = new String[]{keyword};
					columns = new String[]{"rowid", "word", "meaning", "dict_id"};
					break;
					
				case FULLTEXT_SEARCH_PREFIX:
					if (dictId > 0 && PATH.equals(dbPath)) {
						selection = "dict_id == " + dictId + " AND word MATCH ?";
					} else {
						selection = "word MATCH ?";
					}
					selectionArgs = new String[]{"\"" + keyword + "*\""};
					columns = new String[]{"rowid", "word", "meaning", "dict_id"};
					break;
					
				case FULLTEXT_SEARCH_INFIX:
					if (dictId > 0 && PATH.equals(dbPath)) {
						selection = "dict_id == " + dictId + " AND word_tokenize MATCH ?";
					} else {
						selection = "word_tokenize MATCH ?";
					}
					//selectionArgs = new String[]{"\"" + tokenize(keyword) + "*\""};
					selectionArgs = new String[]{tokenize(keyword) + "*"};
					columns = new String[]{"rowid", "word", "meaning", "dict_id"};
					break;
					
				case FULLTEXT_SEARCH_INFIX2:
					if (!USE_FULLTEXT_LIKE) {
						if (dictId > 0 && PATH.equals(dbPath)) {
							selection = "dict_id == " + dictId + " AND word_tokenize MATCH ?";
						} else {
							selection = "word_tokenize MATCH ?";
						}
						//selectionArgs = new String[]{"\"" + tokenize(keyword) + "*\""};
						selectionArgs = new String[]{tokenize(keyword) + "*"};
						columns = new String[]{"rowid", "word", "meaning", "dict_id"};
					} else {
						if (dictId > 0 && PATH.equals(dbPath)) {
							selection = "dict_id == " + dictId + " AND word_tokenize MATCH ? AND word LIKE ?";
						} else {
							selection = "word_tokenize MATCH ? AND word LIKE ?";
						}
						//selectionArgs = new String[]{"\"" + tokenize(keyword) + "*\""};
						selectionArgs = new String[]{tokenize(keyword) + "*",  "%" + keyword + "%"};
						columns = new String[]{"rowid", "word", "meaning", "dict_id"};
					}
					break;
					
				case FULLTEXT_SEARCH_INFIX_ALL:
					if (dictId > 0 && PATH.equals(dbPath)) {
						selection = "dict_id == " + dictId + " AND words MATCH ?";
					} else {
						selection = "words MATCH ?";
						//selectionArgs = new String[]{"\"" + tokenize(keyword) + "*\""};
					}
					selectionArgs = new String[]{tokenize(keyword) + "*"};
					columns = new String[]{"rowid", "word", "meaning", "dict_id"};
					break;
					
				case FULLTEXT_SEARCH_INFIX_ALL2:
					if (!USE_FULLTEXT_LIKE) {
						if (dictId > 0 && PATH.equals(dbPath)) {
							selection = "dict_id == " + dictId + " AND words MATCH ?";
						} else {
							selection = "words MATCH ?";
						}
						//selectionArgs = new String[]{"\"" + tokenize(keyword) + "*\""};
						selectionArgs = new String[]{tokenize(keyword) + "*"};
						columns = new String[]{"rowid", "word", "meaning", "dict_id"};
					} else {
						if (dictId > 0 && PATH.equals(dbPath)) {
							selection = "dict_id == " + dictId + " AND words MATCH ? AND (word LIKE ? OR meaning LIKE ?)";
						} else {
							selection = "words MATCH ? AND (word LIKE ? OR meaning LIKE ?)";
						}
						//selectionArgs = new String[]{"\"" + tokenize(keyword) + "*\""};
						selectionArgs = new String[]{tokenize(keyword) + "*", "%" + keyword + "%", "%" + keyword + "%"};
						columns = new String[]{"rowid", "word", "meaning", "dict_id"};
					}
					break;
				}
			} else {
				if (dictId > 0 && PATH.equals(dbPath)) {
					selection = "dict_id == " + dictId + " AND word LIKE ?";
				} else {
					selection = "word LIKE ?";
				}
				selectionArgs = new String[]{"%" + keyword + "%"};
				columns = new String[]{"id", "word", "meaning", "dict_id"};
			}
			cursor = db.query(false, //destinct
					TABLE_NAME, //table
					columns, //columns
					selection, //"word LIKE ?", //selection
					selectionArgs, //new String[]{"%明日%"}, //selectionArgs
					null, //groupBy
					null, //having
					"word ASC", //orderBy
					"0, " + MAX_COUNT); //"0, 10"); //limit
			try {
//				records.clear();
				while (cursor.moveToNext()) {
					int id = cursor.getInt(0);
					String word = cursor.getString(1);
					String meaning = cursor.getString(2);
					int dict_id = cursor.getInt(3);
					if (D) {
						Log.d(TAG,
							"id = " + id + "," +  
							"word = " + word + "," + 
							"meaning = " + meaning + "," + 
							"dict_id = " + dict_id);
					}
					if (!USE_FULLTEXT_LIKE) {
						if (type == FULLTEXT_SEARCH_INFIX2) {
							if (keyword != null && word != null && word.indexOf(keyword) < 0) {
								continue;
							}
						} else if (type == FULLTEXT_SEARCH_INFIX_ALL2) {
							if ((keyword != null && word != null && word.indexOf(keyword) < 0) &&
								(keyword != null && meaning != null && meaning.indexOf(keyword) < 0)) {
								continue;
							}
						}
					}
					result.add(new WordRecord(id, word, meaning, dict_id));
				}
			} catch (Throwable e) {
				e.printStackTrace();
				result = null;
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			result = null;
		} finally {
			if (db != null) {
				db.close();
			} 
		}
		return result;
	}
	
	private int getSearchType() {
		switch (spinnerSearchType.getSelectedItemPosition()) {
		case 0:
			return FULLTEXT_SEARCH_FULL;
		
		case 1:
			return FULLTEXT_SEARCH_PREFIX;
		
		case 2:
			return FULLTEXT_SEARCH_INFIX;
		
		case 3:
			return FULLTEXT_SEARCH_INFIX_ALL;
		
		case 4:
			return FULLTEXT_SEARCH_INFIX2;
				
		case 5:
			return FULLTEXT_SEARCH_INFIX_ALL2;
			
		default:
			return FULLTEXT_SEARCH_FULL;
		}
	}
	
	private int getSearchDict() {
		return spinnerSearchDictId.getSelectedItemPosition();
	}
	
	private String getDBPath() {
		String dbPath = PATH;
		if (this.radioButtonDict.isChecked()) {
			dbPath = PATH;
		} else if (this.radioButtonExamples.isChecked()) {
			dbPath = PATH_EXAMPLES;
		}
		return dbPath;
	}

	/**
	 * 用户线程
	 */
	private String getTypeInfo(int type) {
		String typeInfo = "";
		if (this.radioButtonDict.isChecked()) {
			typeInfo = "词典";
			int dictId = getSearchDict();
			if (dictId >= 0 && dictId < DICT_NAME.length) {
				typeInfo += "," + DICT_NAME[dictId];
			}
		} else if (this.radioButtonExamples.isChecked()) {
			typeInfo = "例句";
		}
		switch (type) {
		case FULLTEXT_SEARCH_FULL:
			typeInfo += ",完全";
			break;
			
		case FULLTEXT_SEARCH_PREFIX:
			typeInfo += ",前缀";
			break;
			
		case FULLTEXT_SEARCH_INFIX:
			typeInfo += ",包含";
			break;
			
		case FULLTEXT_SEARCH_INFIX_ALL:
			typeInfo += ",全文";
			break;
			
		case FULLTEXT_SEARCH_INFIX2:
			typeInfo += ",精确包含";
			break;
			
		case FULLTEXT_SEARCH_INFIX_ALL2:
			typeInfo += ",精确全文";
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
			long lastTime = System.currentTimeMillis();
			int type = getSearchType();
			String dbPath = getDBPath();
			int dictId = getSearchDict();
			String typeInfo = getTypeInfo(type);
			if (type == FULLTEXT_SEARCH_FULL || type == FULLTEXT_SEARCH_PREFIX) {
				if (!USE_TASK) {
					List<WordRecord> result = search(text, type, dbPath, dictId);
					records.clear();
					adapter.notifyDataSetChanged();
					adapter.setKeyword(text);
					if (result != null) {
						for (WordRecord record : result) {
							records.add(record);
						}
						adapter.notifyDataSetChanged();
						viewListView.setSelection(0);
						long time = (System.currentTimeMillis() - lastTime);
						//System.out.println("time : " + time);
						int count = adapter.getCount();
						if (text.length() > 0) {
							if (count == 0) {
								showInfo(
									typeInfo + "" +
									":" + text + "," +
									"" + (time / 1000.0) + "s," + 
									"" + count + "\n" + 
									"请改用全文搜索,缩短关键词,或使用其他在线搜索引擎。"
								);
							} else if (count < MAX_COUNT) {
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
									"警告:结果在" + MAX_COUNT + "条以上,请输入更长的关键词" 
								);		
							}
						} else {
							showInfo(  
								"请输入关键词。\n" +
								"注意:如果选择“包含”或“全文”单选按钮,搜索可能会花费较长时间（约1分钟）。"
							);
						}
					} else {
						showInfo(  
							"SQLite文件无法打开或搜索错误。此工具需要在SD卡内安装数据包。\n" +
							"（/mnt/sdcard/jkanji/db/或全局设置中指定的目录下的dict_fulltext.sqlite和examples_fulltext.sqlite）"
						);
						records.clear();
					}
					hideKeyboard();
				} else {
					new LoadDataTask().execute(text, Integer.toString(type), dbPath, Integer.toString(dictId));
				}
			} else {
				if (false) {
					showDialog(DIALOG_LOADING_ID);
				} else {
					new LoadDataTask().execute(text, Integer.toString(type), dbPath, Integer.toString(dictId));
				}
			}
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
		SQLiteDatabase.releaseMemory();
	}

	private void showInfo(String text) {
		if (false) {
			Toast.makeText(SQLiteReaderActivity.this,
				text, Toast.LENGTH_SHORT).show();
		} else {
			this.textViewMessage.setText(text);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog = null;
	    switch(id) {
	    case DIALOG_LOADING_ID:
	    	if (builder != null) {
	    		return builder
	    			.setTitle("包含或全文搜索")
	    			.setMessage("\nSQLite在执行包含或全文搜索时,可能需要2至3分钟时间,请尽量避免搜索纯假名和长字符串。是否开始？")
	    			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog, int id) {
	    					String text = searchInput.getText().toString();
	    					int type = getSearchType();
	    					String dbPath = getDBPath();
	    					int dictId = getSearchDict();
	    					new LoadDataTask().execute(text, Integer.toString(type), dbPath, Integer.toString(dictId));
	    				}
	    			})
	    			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog, int id) {
	    					
	    				}
	    			})
	    	       .create();
	    	}
	    	break;
	    }
	    return dialog;
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
    
	private class LoadDataTask extends AsyncTask<String, Void, Boolean> {
		private boolean loadResult = false;
		private String text;
		private long lastTime;
		private List<WordRecord> resultRecords; 
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
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
				String dbPath = params[2];
				int dictId = Integer.parseInt(params[3]);
				this.lastTime = System.currentTimeMillis();
				resultRecords = search(text, type, dbPath, dictId);
				if (resultRecords != null) {
					loadResult = true;
				} else {
					loadResult = false;
				}
			} catch (Throwable e) {
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			textViewLoading.setVisibility(View.INVISIBLE);
			viewListView.setVisibility(View.VISIBLE);
			searchButton.setEnabled(true);
			textViewMessage.setVisibility(View.VISIBLE);
			if (result == true && !SQLiteReaderActivity.this.isFinishing()) {
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
								"" + count //+ 
								//"" + ("\n" + TIPS2) + 
								//((radioButtonDict.isChecked() && getSearchType() > 1 && getSearchDict() == 0)? ("\n" + TIPS) : "")
							);
						} else {
							showInfo( 
								typeInfo + "" +
								":" + text + "," +
								"" + (time / 1000.0) + "s\n" + 
								"警告:结果超过" + MAX_COUNT + "条,请选择字典类型" //+ 
								//((radioButtonDict.isChecked() && getSearchType() > 1 && getSearchDict() == 0) ? ("\n" + TIPS) : "")
							);
						}
					} else {
						showInfo(  
							"请输入关键词,可选择字典类型。\n" //+
							//((radioButtonDict.isChecked() && getSearchType() > 1 && getSearchDict() == 0) ? ("\n" + TIPS) : "")
						);
					}
				} else {
					showInfo(  
						"SQLite文件无法打开或搜索错误。此工具需要在SD卡内安装数据包。\n" +
						"（/mnt/sdcard/jkanji/db/或全局设置中指定的目录下的dict_fulltext.sqlite和examples_fulltext.sqlite）"
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
				SHARE_PREF_SQLITE_READER_INPUT_TEXT,
				str);
    }
    
    private String getLastSearchInput() {
		return PrefUtil.getString(this, SHARE_PREF_NAME,
				SHARE_PREF_SQLITE_READER_INPUT_TEXT,
				"");
    }
}
