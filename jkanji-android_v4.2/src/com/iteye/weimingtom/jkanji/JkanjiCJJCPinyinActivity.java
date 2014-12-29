package com.iteye.weimingtom.jkanji;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * @see G:\backup\mdict\[iPhone] [1.4]日中中日辞書(ピンイン付け版)
 * @author Administrator
 *
 */
public class JkanjiCJJCPinyinActivity extends Activity implements OnItemClickListener {
	private final static boolean D = false;
	private final static String TAG = "JkanjiCJJCPinyinActivity";
	
	public final static String EXTRA_KEY_SEARCH_WORD = "com.iteye.weimingtom.jkanji.JkanjiCJJCPinyinActivity.searchWord";
	
	private static final String PATH = "/cjjc/dict12.sqlite3";
	
	private final static int SEARCHER_MAX = 2000;
	private final static int SEARCHER_JP = 1;
	private final static int SEARCHER_JP_BEGIN = 2;
	private final static int SEARCHER_CN = 3;
	private final static int SEARCHER_CN_BEGIN = 4;
	private final static int SEARCHER_JP2 = 5;
	
	private ActionBar actionBar;
	private EditText editTextKeyword;
	private Button buttonSearch;
	private RadioButton radioButtonSearchJP;
	private RadioButton radioButtonSearchJPBegin;
	private RadioButton radioButtonSearchCN;
	private RadioButton radioButtonSearchCNBegin;
	private RadioButton radioButtonSearchJP2;
	private ListView listViewSearchResults;
	private TextView textViewWarning;
	
	private HorrySearchAdapter adapter;
	private List<WordRecord> records;
	
	private Typeface typeface;
	
	private static final int REQUEST_HANDINPUT = 1;
	
	private boolean alreadyStarted = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cjjc_dict);
        
        
        
        typeface = Typefaces.getFile(JkanjiSettingActivity.getFontFileName(this));	
        
        editTextKeyword = (EditText) this.findViewById(R.id.editTextKeyword);
        buttonSearch = (Button) this.findViewById(R.id.buttonSearch);
        radioButtonSearchJP = (RadioButton) this.findViewById(R.id.radioButtonSearchJP);
        radioButtonSearchJP2 = (RadioButton) this.findViewById(R.id.radioButtonSearchJP2);
        radioButtonSearchJPBegin = (RadioButton) this.findViewById(R.id.radioButtonSearchJPBegin);
        radioButtonSearchCN = (RadioButton) this.findViewById(R.id.radioButtonSearchCN);
        radioButtonSearchCNBegin = (RadioButton) this.findViewById(R.id.radioButtonSearchCNBegin);
        listViewSearchResults = (ListView) this.findViewById(R.id.listViewSearchResults);
        textViewWarning = (TextView) this.findViewById(R.id.textViewWarning);
        
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("日中中日辞書");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
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
				//return R.drawable.icon_actionbar;
				return R.drawable.write_input;
			}

			@Override
			public void performAction(View view) {
				Intent intent = new Intent(JkanjiCJJCPinyinActivity.this, 
						JkanjiHandActivity.class);
				intent.putExtra(JkanjiHandActivity.EXTRA_KEY_INIT_STRING, 
						editTextKeyword.getText().toString());
				startActivityForResult(intent, REQUEST_HANDINPUT);
			}
        });
        
        records = new ArrayList<WordRecord>();
        adapter = new HorrySearchAdapter(this, records, typeface);
        listViewSearchResults.setAdapter(adapter);
        listViewSearchResults.setOnItemClickListener(this);
        listViewSearchResults.setFastScrollEnabled(true);
        
        buttonSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onSearch();
			}
        });
        editTextKeyword.setOnEditorActionListener(new OnEditorActionListener() {
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
		}
    }
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putBoolean("alreadyStarted", true);
	}
    
    @Override
	protected void onResume() {
		super.onResume();
		if (D) {
			Log.d(TAG, "onResume");
		}
		//FIXME:
        //search("明日", SEARCHER_EXACT);
		Intent intent = this.getIntent();
		if (intent != null) {
			String searchWord = intent.getStringExtra(EXTRA_KEY_SEARCH_WORD);
			if (!alreadyStarted) {
				alreadyStarted = true;
				if (searchWord != null) {
					editTextKeyword.setText("");
					editTextKeyword.append(searchWord);
				} else {
					editTextKeyword.setText("");
				}
				onSearch();
			} else {
				
			}
		}
	}

	private void onSearch() {
		int type = SEARCHER_JP;
		if (radioButtonSearchJP.isChecked()) {
			type = SEARCHER_JP;
		} else if (radioButtonSearchJPBegin.isChecked()) {
			type = SEARCHER_JP_BEGIN;
		} else if (radioButtonSearchJP2.isChecked()) {
			type = SEARCHER_JP2;
		} else if (radioButtonSearchCN.isChecked()) {
			type = SEARCHER_CN;
		} else if (radioButtonSearchCNBegin.isChecked()) {
			type = SEARCHER_CN_BEGIN;
		}  
		search(editTextKeyword.getText().toString(), type);
    }
    
	/**
SELECT
dict_index.word, dict_index.pronounce, dict.comment
FROM 
dict_index, dict
WHERE
dict_index.id = dict.id
AND
dict_index.country = 1
AND
(dict_index.word = "明日" OR dict_index.pronounce LIKE "%明日%")
;

	 */
	
	/**
	 * 
	 * FIXME: records.clear() 不要在任务线程中执行，
	 * 参考SqliteReaderActivity的写法！
	 * 
	 * @param keyword
	 * @param type
	 */
	public void search(String keyword, int type) {
		final long lastTime = System.currentTimeMillis();
		records.clear();
		//records.add(new WordRecord(word, meaning));
		
		boolean result = true;
		if (keyword == null || keyword.trim().length() == 0) {
			
		} else {
			SQLiteDatabase db = null;
			String dbPath = JkanjiSettingActivity.getDataPackPath(this) + PATH;
			try {
				db = SQLiteDatabase.openDatabase(dbPath, 
					null, 
					SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
				db.setLockingEnabled(false);
				Cursor cursor = null;
				String tableName = null;
				String selection = null;
				String[] selectionArgs = null;
				String[] columns = null;
				String orderBy = null;
				switch (type) {
				default:
				case SEARCHER_JP:
					tableName = "dict_index, dict";
					selection = "dict_index.id = dict.id AND dict_index.country = 0 AND (dict_index.word MATCH ?)";
					selectionArgs = new String[]{keyword};
					columns = new String[]{"dict_index.word", "dict_index.pronounce", "dict.comment"};
					orderBy = "dict_index.pronounce ASC";
					break;

				case SEARCHER_JP2:
					tableName = "dict_index, dict";
					selection = "dict_index.id = dict.id AND dict_index.country = 0 AND (dict_index.pronounce MATCH ?)";
					selectionArgs = new String[]{keyword + "*"};
					columns = new String[]{"dict_index.word", "dict_index.pronounce", "dict.comment"};
					orderBy = "dict_index.pronounce ASC";
					break;
					
				case SEARCHER_JP_BEGIN:
					tableName = "dict_index, dict";
					selection = "dict_index.id = dict.id AND dict_index.country = 0 AND (dict_index.word MATCH ?)";
					selectionArgs = new String[]{keyword + "*"};
					columns = new String[]{"dict_index.word", "dict_index.pronounce", "dict.comment"};
					orderBy = "dict_index.pronounce ASC";
					break;
					
				case SEARCHER_CN:
					tableName = "dict_index, dict";
					selection = "dict_index.id = dict.id AND dict_index.country = 1 AND (dict_index.word MATCH ?)";
					selectionArgs = new String[]{keyword};
					columns = new String[]{"dict_index.word", "dict_index.pronounce", "dict.comment"};
					orderBy = "dict_index.pronounce ASC";
					break;
					
				case SEARCHER_CN_BEGIN:
					tableName = "dict_index, dict";
					selection = "dict_index.id = dict.id AND dict_index.country = 1 AND (dict_index.word MATCH ?)";
					selectionArgs = new String[]{keyword + "*"};
					columns = new String[]{"dict_index.word", "dict_index.pronounce", "dict.comment"};
					orderBy = "dict_index.pronounce ASC";
					break;
				}
				cursor = db.query(false, //destinct
						tableName, //table
						columns, //columns
						selection, //"word LIKE ?", //selection
						selectionArgs, //new String[]{"%明日%"}, //selectionArgs
						null, //groupBy
						null, //having
						orderBy, //orderBy
						"0, " + SEARCHER_MAX); //"0, 10"); //limit
				try {
					records.clear();
					while (cursor.moveToNext()) {
						switch (type) {
						default:
						case SEARCHER_JP:
						case SEARCHER_JP_BEGIN:
							{
								String dic_t_word = cursor.getString(0);
								String dic_roma = cursor.getString(1);
								String dic_trans_word = cursor.getString(2);
								StringBuffer sb = new StringBuffer();
								if (dic_t_word != null && dic_t_word.length() > 0) {
									sb.append(dic_t_word);
									sb.append("/");
								}
								if (dic_roma != null && dic_roma.length() > 0) {
									sb.append(dic_roma);
									//sb.append("/");
								}
								String word = sb.toString();
								String meaning = "";
								if (dic_trans_word != null && dic_trans_word.length() > 0) {
									//meaning = Html.fromHtml(dic_trans_word).toString();
									meaning = prettyFormat(dic_trans_word, 2);
								}
								if (meaning != null) {
									meaning = meaning.replace("\n\n", "\n");
								} else {
									meaning = "";
								}
								records.add(new WordRecord(word, meaning));
							}
							break;
							
						case SEARCHER_CN:
						case SEARCHER_CN_BEGIN:
							{
								String dic_cj_word = cursor.getString(0);
								String dic_pinyin = cursor.getString(1);
								String dic_con = cursor.getString(2);
								StringBuffer sb = new StringBuffer();
								if (dic_cj_word != null && dic_cj_word.length() > 0) {
									sb.append(dic_cj_word);
									sb.append("/");
								}
								if (dic_pinyin != null && dic_pinyin.length() > 0) {
									sb.append(dic_pinyin);
									//sb.append("/");
								}
								String word = sb.toString();
								String meaning = "";
								if (dic_con != null && dic_con.length() > 0) {
									//meaning = Html.fromHtml(dic_con).toString();
									meaning = prettyFormat(dic_con, 2);
								}
								if (meaning != null) {
									meaning = meaning.replace("\n\n", "\n");
								} else {
									meaning = "";
								}
								records.add(new WordRecord(word, meaning));
							}
							break;
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
					result = false;
				} finally {
					if (cursor != null) {
						cursor.close();
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
				result = false;
			} finally {
				if (db != null) {
					db.close();
				} 
			}
		}
		adapter.setKeyword(keyword);
		adapter.notifyDataSetChanged();
		listViewSearchResults.setSelection(0);

		float useTime = ((float)(System.currentTimeMillis() - lastTime)) / 1000.0f;
		int resultNum = records.size();
		
		File dbFile = new File(JkanjiSettingActivity.getDataPackPath(this) + PATH);
		if (dbFile.isFile() && dbFile.canRead() && dbFile.exists()) {
			if (result) {
				textViewWarning.setVisibility(TextView.VISIBLE);
				textViewWarning.setTextColor(Color.BLACK);
				textViewWarning.setText("关键词:" + keyword + ", 结果: " + resultNum+ ", 耗时:" + useTime + "s");
			} else {
				textViewWarning.setVisibility(TextView.VISIBLE);
				textViewWarning.setTextColor(Color.RED);
				textViewWarning.setText("数据库查询出错");
			}
		} else {
			textViewWarning.setVisibility(TextView.VISIBLE);
			textViewWarning.setTextColor(Color.RED);
			textViewWarning.setText("数据包文件不存在：" + JkanjiSettingActivity.getDataPackPath(this) + PATH);
		}
	}
    
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);		
		if (D) {
			Log.d(TAG, "onActivityResult");
		}
		if (data != null) {
			String resultString = data.getStringExtra(JkanjiHandActivity.EXTRA_KEY_RESULT_STRING);
			switch (requestCode) {
			case REQUEST_HANDINPUT:
				if (resultString != null) {
					editTextKeyword.setText("");
					editTextKeyword.append(resultString);
				}
				onSearch();
				break;
			}
		}
	}
    
	private class WordRecord {
		public String word;
		public String meaning;
		
		public WordRecord(String word, String meaning) {
			this.word = word;
			this.meaning = meaning;
		}
	}
    
    private final static class HorrySearchAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<WordRecord> records;
        private Typeface typeface;
        private String keyword;
        private Context mContext;
        
        public HorrySearchAdapter(Context context, List<WordRecord> records, Typeface typeface) {
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
                holder.text.setTypeface(typeface);
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		WordRecord wd = records.get(position);
		if (wd != null) {
			Word word = new Word(-1, new String[]{null, null, wd.word, wd.meaning, null});
			startActivity(new Intent(this, WordEditActivity.class)
				.putExtra(WordEditActivity.EXTRA_DATA, word));
		}
	}
	
	public static String prettyFormat(String input, int indent) {
		return Html.fromHtml(input.replace("><", "><br/><")).toString();
	}
}
