package com.iteye.weimingtom.jkanji;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import fuku.eb4j.Book;
import fuku.eb4j.EBException;
import fuku.eb4j.Result;
import fuku.eb4j.Searcher;
import fuku.eb4j.SubBook;
import fuku.eb4j.hook.DefaultHook;
import fuku.eb4j.hook.Hook;
import fuku.eb4j.util.HexUtil;
import fuku.eb4j.util.StringUtils;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnShowListener;
import android.content.SharedPreferences.Editor;
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
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class JkanjiEb4jActivity extends Activity implements OnItemClickListener {
	private final static boolean D = false;
	private final static String TAG = "JkanjiEb4jActivity";
	
	public final static String EXTRA_KEY_SEARCH_WORD = "com.iteye.weimingtom.jkanji.JkanjiEb4jActivity.searchWord";
	
	private final static int SEARCHER_MAX = 2000;
	private final static int SEARCHER_EXACT = 1;
	private final static int SEARCHER_BEGIN = 2;
	private final static int SEARCHER_END = 3;
	
	private ActionBar actionBar;
	private EditText editTextKeyword;
	private Button buttonSearch;
	private RadioButton radioButtonSearchExact;
	private RadioButton radioButtonSearchBegin;
	private RadioButton radioButtonSearchEnd;
	private ListView listViewSearchResults;
	private LinearLayout linearLayoutHead;
	private TextView textViewMessage;
	private TextView textViewLoading;
	
	private EpwingSearchAdapter adapter;
	private List<WordRecord> records;
	
	private String bookpath = "";
	private Typeface typeface;
	
	private static final int REQUEST_HANDINPUT = 1;
	
	private boolean alreadyStarted = false;
	
	private final static boolean USE_TASK = true;

	private boolean isHeadVisible = true;
	
	private static final String SHARE_PREF_NAME = "pref";
	private static final String SHARE_PREF_EB4J_INPUT_TEXT = "eb4jInputString";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.eb_dict);
        
        String epwingFileName = JkanjiSettingActivity.getEpwingFileName(this);
        if (epwingFileName != null && epwingFileName.length() > 0) {
        	bookpath = epwingFileName.substring(0, epwingFileName.lastIndexOf(File.separator));;
        } else {
        	bookpath = "";
        }
        
        typeface = Typefaces.getFile(JkanjiSettingActivity.getFontFileName(this));	
        
        editTextKeyword = (EditText) this.findViewById(R.id.editTextKeyword);
        buttonSearch = (Button) this.findViewById(R.id.buttonSearch);
        radioButtonSearchExact = (RadioButton) this.findViewById(R.id.radioButtonSearchExact);
        radioButtonSearchBegin = (RadioButton) this.findViewById(R.id.radioButtonSearchBegin);
        radioButtonSearchEnd = (RadioButton) this.findViewById(R.id.radioButtonSearchEnd);
        listViewSearchResults = (ListView) this.findViewById(R.id.listViewSearchResults);
        linearLayoutHead = (LinearLayout) this.findViewById(R.id.linearLayoutHead);
        textViewMessage = (TextView) this.findViewById(R.id.textViewMessage);
        textViewLoading = (TextView) this.findViewById(R.id.textViewLoading);
        
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("epwing搜索器");
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
				return R.drawable.search_web;
			}

			@Override
			public void performAction(View view) {
				String str = editTextKeyword.getText().toString();
				startActivity(new Intent(JkanjiEb4jActivity.this, DictWebListActivity.class)
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
				Intent intent = new Intent(JkanjiEb4jActivity.this, 
						JkanjiHandActivity.class);
				intent.putExtra(JkanjiHandActivity.EXTRA_KEY_INIT_STRING, 
						editTextKeyword.getText().toString());
				startActivityForResult(intent, REQUEST_HANDINPUT);
			}
        });
        
        records = new ArrayList<WordRecord>();
        adapter = new EpwingSearchAdapter(this, records, typeface);
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
			isHeadVisible = savedInstanceState.getBoolean("isHeadVisible", true);
		}
	}
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		if (outState != null) {
			outState.putBoolean("alreadyStarted", true);
			outState.putBoolean("isHeadVisible", linearLayoutHead.getVisibility() == LinearLayout.VISIBLE);
		}
	}
    
    @Override
	protected void onResume() {
		super.onResume();
		if (D) {
			Log.d(TAG, "onResume");
		}
		if (!isHeadVisible) {
			linearLayoutHead.setVisibility(LinearLayout.GONE);
		} else {
			linearLayoutHead.setVisibility(LinearLayout.VISIBLE);
		}
        //search("明日", SEARCHER_EXACT);
		Intent intent = this.getIntent();
		if (intent != null) {
			String searchWord = intent.getStringExtra(EXTRA_KEY_SEARCH_WORD);
			//searchInput.setText(searchWord);
			//searchInput.setText("明日");
			if (!alreadyStarted) {
				alreadyStarted = true;
				editTextKeyword.setText("");
				editTextKeyword.append(getLastSearchInput());
				if (searchWord != null) {
					editTextKeyword.setText("");
					editTextKeyword.append(searchWord);
				} else {
					
				}
				onSearch();
			} else {
				
			}
		}
	}

	private void onSearch() {
		int type = SEARCHER_EXACT;
		if (radioButtonSearchExact.isChecked()) {
			type = SEARCHER_EXACT;
		} else if (radioButtonSearchBegin.isChecked()) {
			type = SEARCHER_BEGIN;
		} else if (radioButtonSearchEnd.isChecked()) {
			type = SEARCHER_END;
		} 
		if (!USE_TASK) {
			search(editTextKeyword.getText().toString(), type);
		} else {
			new LoadDataTask().execute(editTextKeyword.getText().toString(), Integer.toString(type));
		}
	}
    
	public void search(String keyword, int type) {
		if (bookpath == null || 
			(bookpath != null && bookpath.length() == 0)) {
			Toast.makeText(this,
				"尚未指定EPWING字典路径，请在全局设置中指定。", 
				Toast.LENGTH_SHORT)
				.show();
		} else {
//			records.clear();
			GaijiDataSource gaijiDataSrc;
			gaijiDataSrc = new GaijiDataSource(JkanjiEb4jActivity.this);
			gaijiDataSrc.open();
			try {
				//"/mnt/sdcard/jkanji/epwing/DreyeJC"
				Book book = new Book(bookpath);
				SubBook[] subbooks = book.getSubBooks();
				if (D) {
					Log.d(TAG, "subbooks.lenght == " + subbooks.length);
				}
				for (int i = 0; i < subbooks.length; i++) {
					SubBook subbook = subbooks[i];
					Hook<String> hook = new GaijiHook(subbook, gaijiDataSrc);
					if (subbook.hasExactwordSearch()) {
						Searcher searcher;
						if (type == SEARCHER_BEGIN) {
							searcher = subbook.searchWord(keyword);
						} else if (type == SEARCHER_END) {
							searcher = subbook.searchEndword(keyword);
						} else {
							searcher = subbook.searchExactword(keyword);
						}
						for (int j = 0; j < SEARCHER_MAX; j++) {
		                    Result result = searcher.getNextResult();
		                    if (result == null) {
		                        break;
		                    }
		                    String word = getHeading(subbook, result, hook);
		                    String meaning = getText(subbook, result, hook);
		                    records.add(new WordRecord(word, meaning));
		                }
					}
				}
			} catch (EBException e) {
				e.printStackTrace();
			}
			adapter.setKeyword(keyword);
			adapter.notifyDataSetChanged();
			listViewSearchResults.setSelection(0);
			if (gaijiDataSrc != null) {
				gaijiDataSrc.close();
				gaijiDataSrc = null;
			}
		}
	}
	
    private static String getHeading(SubBook subbook, Result result, Hook<String> hook) {
        String text = null;
        try {
            text = subbook.getHeading(result.getHeadingPosition(), hook);
        } catch (EBException e) {
        	
        }
        if (text == null) {
            text = "";
        }
        return text;
    }
    
    private static String getText(SubBook subbook, Result result, Hook<String> hook) {
        String text = null;
        try {
            text = subbook.getText(result.getTextPosition(), hook);
        } catch (EBException e) {
        }
        if (text == null) {
            text = "";
        }
        return text;
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
    
    private final static class EpwingSearchAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<WordRecord> records;
        private Typeface typeface;
        private String keyword;
        private Context mContext;
        
        public EpwingSearchAdapter(Context context, List<WordRecord> records, Typeface typeface) {
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
	
	@Override
	protected void onPause() {
		super.onPause();
		if (this.editTextKeyword != null && this.editTextKeyword.getText() != null) {
			String text = this.editTextKeyword.getText().toString();
			setLastSearchInput(text);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)
				getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editTextKeyword.getWindowToken(), 0);
	}
	
	private final static class GaijiHook extends DefaultHook {
		private GaijiDataSource gaijiDataSrc;
		
		public GaijiHook(SubBook sub, GaijiDataSource gaijiDataSrc) {
			super(sub);
			this.gaijiDataSrc = gaijiDataSrc;
		}

		@Override
		public void append(int code) {
	        String str = null;
	        if (_narrow) {
	            if (_appendix != null) {
	                try {
	                    str = _appendix.getNarrowFontAlt(code);
	                } catch (EBException e) {
	                }
	            }
	            if (StringUtils.isBlank(str)) {
		            String value = null;
		            if (gaijiDataSrc != null) {
		            	value = gaijiDataSrc.getItem("h" + HexUtil.toHexString(code).toUpperCase());
		            }
		            if (value != null) {
		            	try {
		            		char ch = (char)Integer.parseInt(value.substring(1), 16);
		            		str = Character.toString(ch);
		            	} catch (Throwable e) {
		            		e.printStackTrace();
		            		str = "[GAIJI=" + value + "]";
		            	}
		            } else {
	            		str = "[GAIJI=n" + HexUtil.toHexString(code) + "]";
	            	}
	            }
	        } else {
	            if (_appendix != null) {
	                try {
	                    str = _appendix.getWideFontAlt(code);
	                } catch (EBException e) {
	                }
	            }
	            if (StringUtils.isBlank(str)) {
		            String value = null;
		            if (gaijiDataSrc != null) {
		            	value = gaijiDataSrc.getItem("z" + HexUtil.toHexString(code).toUpperCase());
		            }
		            if (value != null) {
		            	try {
		            		char ch = (char)Integer.parseInt(value.substring(1), 16);
		            		str = Character.toString(ch);
		            	} catch (Throwable e) {
		            		e.printStackTrace();
		            		str = "[GAIJI=" + value + "]";
		            	}
		            } else {
		                str = "[GAIJI=w" + HexUtil.toHexString(code) + "]";
			        }
	            }
	        }
	        _buf.append(str);
		}
	}
	
	private class LoadDataTask extends AsyncTask<String, Void, Boolean> {
		private boolean loadResult = false;
		private List<WordRecord> tempRecords = new ArrayList<WordRecord>();
		private String keyword;
		private int type;
		private long searchTime = 0;
		private GaijiDataSource gaijiDataSrc;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			listViewSearchResults.setVisibility(View.INVISIBLE);
			textViewLoading.setVisibility(View.VISIBLE);
			buttonSearch.setEnabled(false);
			textViewMessage.setVisibility(View.GONE);
			hideKeyboard();
			gaijiDataSrc = new GaijiDataSource(JkanjiEb4jActivity.this);
			gaijiDataSrc.open();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				searchTime = System.currentTimeMillis();
				keyword = params[0];
				type = Integer.parseInt(params[1]);
				if (bookpath == null || 
					(bookpath != null && bookpath.length() == 0)) {
					loadResult = false;
				} else {
					tempRecords.clear();
					try {
						//"/mnt/sdcard/jkanji/epwing/DreyeJC"
						Book book = new Book(bookpath);
						SubBook[] subbooks = book.getSubBooks();
						if (D) {
							Log.d(TAG, "subbooks.lenght == " + subbooks.length);
						}
						for (int i = 0; i < subbooks.length; i++) {
							SubBook subbook = subbooks[i];
							Hook<String> hook = new GaijiHook(subbook, gaijiDataSrc);
							if (subbook.hasExactwordSearch()) {
								Searcher searcher;
								if (type == SEARCHER_BEGIN) {
									searcher = subbook.searchWord(keyword);
								} else if (type == SEARCHER_END) {
									searcher = subbook.searchEndword(keyword);
								} else {
									searcher = subbook.searchExactword(keyword);
								}
								for (int j = 0; j < SEARCHER_MAX; j++) {
				                    Result result = searcher.getNextResult();
				                    if (result == null) {
				                        break;
				                    }
				                    String word = getHeading(subbook, result, hook);
				                    String meaning = getText(subbook, result, hook);
				                    tempRecords.add(new WordRecord(word, meaning));
				                }
							}
						}
					} catch (EBException e) {
						e.printStackTrace();
					}
				}
				searchTime = System.currentTimeMillis() - searchTime;
				loadResult = true;
			} catch (Throwable e) {
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (gaijiDataSrc != null) {
				gaijiDataSrc.close();
				gaijiDataSrc = null;
			}
			listViewSearchResults.setVisibility(View.VISIBLE);
			textViewLoading.setVisibility(View.INVISIBLE);
			buttonSearch.setEnabled(true);
			textViewMessage.setVisibility(View.VISIBLE);
			if (result == true && !isFinishing()) {
				if (loadResult) {
					records.clear();
					for (WordRecord record : tempRecords) {
						records.add(record);
					}
					tempRecords.clear();
					adapter.setKeyword(keyword);
					adapter.notifyDataSetChanged();
					listViewSearchResults.postDelayed(new Runnable() {
						@Override
						public void run() {
							listViewSearchResults.setSelection(0);
						}
					}, 100);
					textViewMessage.setText("关键词:" + keyword + "," + "结果:" + records.size() + ",耗时:" + ((double)searchTime / 1000) + "s");
					hideKeyboard();
				} else {
					if (bookpath == null || 
					(bookpath != null && bookpath.length() == 0)) {
						final String mess = "尚未指定EPWING字典路径，请在全局设置中指定。";
						Toast.makeText(JkanjiEb4jActivity.this,
							mess, 
							Toast.LENGTH_SHORT)
							.show();
						textViewMessage.setText(mess);
					} else {
						textViewMessage.setText("epwing搜索失败");
					}
				}
			} else if (result == false) {
				finish();
			}
		}
    }
	
    private void setLastSearchInput(String str) {
		PrefUtil.putString(this, SHARE_PREF_NAME, 
				SHARE_PREF_EB4J_INPUT_TEXT, 
				str);
    }
    
    private String getLastSearchInput() {
		return PrefUtil.getString(this, SHARE_PREF_NAME, 
				SHARE_PREF_EB4J_INPUT_TEXT, 
				"");
    }
}
