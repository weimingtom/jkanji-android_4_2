package com.iteye.weimingtom.jkanji;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.elgubbo.sharetoclipboard.ShareToClipboardActivity;
import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.media.AudioTrack;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @see setOnScrollListener
 * @author Administrator
 *
 */
public class JkanjiViewerActivity extends Activity implements OnScrollListener, OnItemClickListener {
	private static final boolean D = false;
	private static final String TAG = "JkanjiViewerActivity";
	
	private static final int REQUEST_HANDINPUT = 1;
	
	private static final String SHARE_PREF_NAME = "pref";
	private static final String SHARE_PREF_LIST_POS = "listPos";
	private static final String SHARE_SEARCH_VISIBLE = "searchVisible";
	private static final String SHARE_SEARCH_CONTENT = "searchContent";
	private static final String SHARE_PREF_WEB_TYPE = "viewerWebType";

	private static final String SHARE_PREF_LIST_POS_EN = "listPosEn";
	private static final String SHARE_SEARCH_VISIBLE_EN = "searchVisibleEn";
	private static final String SHARE_SEARCH_CONTENT_EN = "searchContentEn";
	private static final String SHARE_PREF_WEB_TYPE_EN = "viewerWebTypeEn";
	
	private int listPos;
	
	private final static int SEARCH_CONTENT_KANA = 0;
	private final static int SEARCH_CONTENT_KANJI = 1;
	private final static int SEARCH_CONTENT_VOICE = 2;
	
	private ActionBar actionBar;
	private ListView viewListView;
	private EditText searchInput;
	private Button searchButton;
	private LinearLayout searchLinearLayout;
	private RadioButton radioButtonSearch, radioButtonSearchKanji, radioButtonPlayVoice; 
	
	private TextView textViewMessage;
	
	private static final int SWITCH_INPUT_ID = Menu.FIRST;
	
	private Typeface typeface;
	
	// 加速用，基于JKanjiActivity的缓存
	private List<Word> words;
	
	private AudioTrack audioTrack;
	private String zipFilename = "/tts/tts-20120809.zip";
	private static final boolean USE_STREAM = false;
	private static final boolean USE_TOAST = true;
	
	private Spinner spinnerWebType;
	private ArrayAdapter<String> spinnerWebTypeAdapter;
	
	private TextToSpeech mTts;
	private boolean enableTTS = true;
	
	public final static String EXTRA_DATA_TYPE = "EXTRA_DATA_TYPE";
	public final static int DATA_TYPE_JPWORDS = 0;
	public final static int DATA_TYPE_ENWORDS = 1;
	private int dataType = DATA_TYPE_JPWORDS;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.viewer);
		
		Intent intent = this.getIntent();
		if (intent != null) {
			dataType = intent.getIntExtra(EXTRA_DATA_TYPE, DATA_TYPE_JPWORDS);
		}
		
		viewListView = (ListView) this.findViewById(R.id.viewListView);
		searchButton = (Button) this.findViewById(R.id.searchButton);
		searchInput = (EditText) this.findViewById(R.id.searchInput);
		searchLinearLayout = (LinearLayout) this.findViewById(R.id.searchLinearLayout);
		radioButtonSearch = (RadioButton) this.findViewById(R.id.radioButtonSearch);
		radioButtonSearchKanji = (RadioButton) this.findViewById(R.id.radioButtonSearchKanji);
		radioButtonPlayVoice = (RadioButton) this.findViewById(R.id.radioButtonPlayVoice);
		textViewMessage = (TextView) this.findViewById(R.id.textViewMessage);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("查看器");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.view;
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
				return R.drawable.memo;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(JkanjiViewerActivity.this, 
						ShareToClipboardActivity.class));
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.config;
			}

			@Override
			public void performAction(View view) {
				// TODO Auto-generated method stub
				switchInput();
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
				Intent intent = new Intent(JkanjiViewerActivity.this, 
						JkanjiHandActivity.class);
				intent.putExtra(JkanjiHandActivity.EXTRA_KEY_INIT_STRING, 
						searchInput.getText().toString());
				startActivityForResult(intent, REQUEST_HANDINPUT);
			}
        });
		
		viewListView.setAdapter(new EfficientAdapter(this));
		viewListView.setFastScrollEnabled(true);
		listPos = getLastListPos();
		if (D) {
			Log.e(TAG, "listPos = " + listPos);
		}
		viewListView.setSelection(listPos);
		viewListView.setOnScrollListener(this);
		viewListView.setOnItemClickListener(this);
		
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doSearch();
			}
		});
		
		switch (getSearchContent()) {
		case SEARCH_CONTENT_KANA:
			radioButtonSearch.setChecked(true);
			radioButtonSearchKanji.setChecked(false);
			radioButtonPlayVoice.setChecked(false);
			break;
			
		case SEARCH_CONTENT_KANJI:
			radioButtonSearch.setChecked(false);
			radioButtonSearchKanji.setChecked(true);
			radioButtonPlayVoice.setChecked(false);
			break;
			
		case SEARCH_CONTENT_VOICE:
			radioButtonSearch.setChecked(false);
			radioButtonSearchKanji.setChecked(false);
			radioButtonPlayVoice.setChecked(true);
			break;
		}
		radioButtonSearch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setSearchContent(SEARCH_CONTENT_KANA);
				}
			}
		});
		radioButtonSearchKanji.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setSearchContent(SEARCH_CONTENT_KANJI);
				}
			}
		});
		radioButtonPlayVoice.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setSearchContent(SEARCH_CONTENT_VOICE);
				}
			}
		});
		
        spinnerWebType = (Spinner) this.findViewById(R.id.spinnerWebType);
        spinnerWebTypeAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_spinner_item);
        spinnerWebTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWebTypeAdapter.add("列表");
        String[] webTitles = DictWebListActivity.getWebSearchTitles();
        for (int i = 0; i < webTitles.length; i++) {
        	spinnerWebTypeAdapter.add(webTitles[i]);
        }
        spinnerWebType.setAdapter(spinnerWebTypeAdapter);
        spinnerWebType.setSelection(getLastWebType());
        spinnerWebType.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int postion, long id) {
				setLastWebType(postion);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
        });
		
		
		if (this.getSearchVisible()) {
			searchLinearLayout.setVisibility(LinearLayout.VISIBLE);
		} else {
			searchLinearLayout.setVisibility(LinearLayout.GONE);
		}
		if (!DataContext.DONNOT_LOAD_TYPEFACE) {
			//typeface = Typeface.createFromAsset(getAssets(), "mplus-1m-regular.ttf"); //fonts/samplefont.ttf
			//typeface = Typefaces.get(this, "mplus-1m-regular.ttf");
			typeface = Typefaces.getFile(JkanjiSettingActivity.getFontFileName(this));	
		}
		//Log.e(TAG, "======================load typeface " + typeface);
		
		DataContext dc = JKanjiActivity.getCacheDataContext();
		if (dc != null && dc.words != null) {
			if (dataType == DATA_TYPE_ENWORDS) {
				this.words = dc.enwords;
			} else {
				this.words = dc.words;
			}
		}
		if (this.words == null) {
			textViewMessage.setText("小提示：进入搜索器加载字典到内存，可以提高查看器滑动性能");
			textViewMessage.setVisibility(View.VISIBLE);
		}
		
		mTts = new TextToSpeech(this, new OnInitListener() {
			@Override
			public void onInit(int status) {
				// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR
		        if (status == TextToSpeech.SUCCESS) {
		            int result = mTts.setLanguage(Locale.JAPAN);
		            if (result == TextToSpeech.LANG_MISSING_DATA ||
		                result == TextToSpeech.LANG_NOT_SUPPORTED) {
		                //Log.e("404","Language is not available.");
		            	enableTTS = false;
		            }
		        } else {
		            // Initialization failed.
		            //Log.e("404", "Could not initialize TextToSpeech.");
		            // May be its not installed so we prompt it to be installed
		        	enableTTS = false;
		        	if (false) {
			        	Intent installIntent = new Intent();
			            installIntent.setAction(
			                TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
			            startActivity(installIntent);
			        }
		        }
			}
		});
	}
	
	private void doSearch() {
		//searchInput.setText("いう");
		//FIXME:未考虑空字符串 & pos == total情况
		if (searchInput != null && searchInput.getText() != null) {
			String text = searchInput.getText().toString();
			if (text != null && text.length() > 0) {
				int pos = searchWord(text);
				if (D) {
					Log.d(TAG, "pos == " + pos);
				}
				if (pos >= 0) {
					viewListView.setSelection(pos);
					this.listPos = pos;
					setLastListPos(this.listPos);
				}
			}
		}
	}
	
    @Override
	protected void onPause() {
		super.onPause();
		setLastListPos(this.listPos);
		if (audioTrack != null) {
			audioTrack.setStereoVolume(0.0f, 0.0f);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (audioTrack != null) {
			audioTrack.setStereoVolume(1.0f, 1.0f);
		}
	}
	
	
    
	@Override
	protected void onStop() {
		super.onStop();
		setLastListPos(this.listPos);
	}

	private class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public EfficientAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public int getCount() {
        	if (words != null) {
        		return words.size();
        	}
    		try {
    			if (dataType == DATA_TYPE_ENWORDS) {
    				return RandomDictLoader.getTotal(getAssets(), RandomDictLoader.TYPE_ENWORDS);
    			} else {
    				return RandomDictLoader.getTotal(getAssets(), RandomDictLoader.TYPE_JPWORDS);
        		}
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		return 0;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }
        
        private Word getWord(int index) {
        	if (words != null) {
        		if (index >= 0 && index < words.size()) {
        			return words.get(index);
        		} else {
        			return null;
        		}
        	}
    		try {
    			if (dataType == DATA_TYPE_ENWORDS) {
        			return RandomDictLoader.getWord(getAssets(), index, RandomDictLoader.TYPE_ENWORDS);
    			} else {
    				return RandomDictLoader.getWord(getAssets(), index, RandomDictLoader.TYPE_JPWORDS);
    			}
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		return null;
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
            Word word = getWord(position);
			if (word != null) {
				String reading = (word.reading != null ? ("【" + word.reading + "】") : "");
				String reading2 = (word.reading != null ? word.reading : "");
				String kanji = ((word.kanji != null && word.kanji.length() > 0) ? word.kanji : reading2);
				title = kanji + reading + word.getAccent(); 
				text = ((word.catalog != null && word.catalog.length() > 0) ? ("【" + word.catalog + "】\n") : "") +
					(word.mean != null ? CharTrans.formatMean(word.mean, false) : "");
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

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && view != null){
        	listPos = view.getFirstVisiblePosition();
		}
	}
	
    private void setLastListPos(int listPos) {
    	if (D) {
    		Log.e(TAG, "setLastListPos = " + listPos);
    	}
    	if (dataType == DATA_TYPE_ENWORDS) {
			PrefUtil.putInt(this, SHARE_PREF_NAME,
    				SHARE_PREF_LIST_POS_EN, 
    				listPos);
		} else {
			PrefUtil.putInt(this, SHARE_PREF_NAME,
    				SHARE_PREF_LIST_POS, 
    				listPos);
		}
	}
    
    private int getLastListPos() {
    	if (dataType == DATA_TYPE_ENWORDS) {
    		return PrefUtil.getInt(this, SHARE_PREF_NAME,
    				SHARE_PREF_LIST_POS_EN,
    				0);
    	} else {
    		return PrefUtil.getInt(this, SHARE_PREF_NAME,
    				SHARE_PREF_LIST_POS,
    				0);
    	}
    }

    private void setSearchVisible(boolean searchVisible) {
    	if (dataType == DATA_TYPE_ENWORDS) {
    		PrefUtil.putBoolean(this, SHARE_PREF_NAME,
    				SHARE_SEARCH_VISIBLE_EN,
					searchVisible);
    	} else {
    		PrefUtil.putBoolean(this, SHARE_PREF_NAME,
    				SHARE_SEARCH_VISIBLE,
					searchVisible);
    	}
    }
    
    private boolean getSearchVisible() {
    	if (dataType == DATA_TYPE_ENWORDS) {
    		return PrefUtil.getBoolean(this, SHARE_PREF_NAME,
    				SHARE_SEARCH_VISIBLE_EN,
    				true);
    	} else {
    		return PrefUtil.getBoolean(this, SHARE_PREF_NAME,
    				SHARE_SEARCH_VISIBLE,
    				true);
    	}
    }
    
    private void setSearchContent(int searchContent) {
    	if (dataType == DATA_TYPE_ENWORDS) {
    		PrefUtil.putInt(this, SHARE_PREF_NAME,
					SHARE_SEARCH_CONTENT_EN,
					searchContent);
    	} else {
    		PrefUtil.putInt(this, SHARE_PREF_NAME,
    				SHARE_SEARCH_CONTENT,
					searchContent);
    	}
    }
    
    private int getSearchContent() {
    	if (dataType == DATA_TYPE_ENWORDS) {
    		return PrefUtil.getInt(this, SHARE_PREF_NAME,
    				SHARE_SEARCH_CONTENT_EN,
    				SEARCH_CONTENT_KANA);
    	} else {
    		return PrefUtil.getInt(this, SHARE_PREF_NAME,
    				SHARE_SEARCH_CONTENT,
    				SEARCH_CONTENT_KANA);
    	}
    }
    
    private void setLastWebType(int webType) {
		if (dataType == DATA_TYPE_ENWORDS) {
			PrefUtil.putInt(this, SHARE_PREF_NAME,
    				SHARE_PREF_WEB_TYPE_EN,
    				webType);
		} else {
			PrefUtil.putInt(this, SHARE_PREF_NAME,
					SHARE_PREF_WEB_TYPE,
    				webType);
		}
    }
    
    private int getLastWebType() {
    	if (dataType == DATA_TYPE_ENWORDS) {
    		return PrefUtil.getInt(this, SHARE_PREF_NAME,
    				SHARE_PREF_WEB_TYPE_EN,
    				0);
    	} else {
    		return PrefUtil.getInt(this, SHARE_PREF_NAME,
    				SHARE_PREF_WEB_TYPE,
    				0);
    	}
    }
    
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Word word = getWord(position);
		String searchString = null;
		if (this.radioButtonPlayVoice.isChecked()) {
			if (JkanjiSettingActivity.getUseTTS(JkanjiViewerActivity.this)) {
				if (enableTTS) {
					if (word != null) {
						if (word.kanji != null && word.kanji.length() > 0) {
							mTts.speak(word.kanji, TextToSpeech.QUEUE_FLUSH, null);
						} else if (word.reading != null && word.reading.length() > 0){
							mTts.speak(word.reading, TextToSpeech.QUEUE_FLUSH, null);
						}
					} else {
						Toast.makeText(JkanjiViewerActivity.this, "单词为空", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(JkanjiViewerActivity.this, "请检查是否支持日文发音（退出此界面后再试）", Toast.LENGTH_SHORT).show();
				}
			} else if (dataType == DATA_TYPE_JPWORDS){
				String fileinfo;
				fileinfo = JkanjiSettingActivity.getDataPackPath(this) + zipFilename + ", " + "tts" + "/" + word.id + ".wav";
				try {
					audioTrack = AudioTrackUtils.playWav(audioTrack, 
							JkanjiSettingActivity.getDataPackPath(this) + zipFilename,
							"tts/" + word.id + ".wav", USE_STREAM);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (audioTrack == null) {
					if (USE_TOAST) {
						Toast.makeText(this, "播放" + fileinfo + "失败", Toast.LENGTH_SHORT).show();
					} else {
						
					}
				} else {
					
				}
			}
		} else {
			if (D) {
				if (word.reading != null) {
					Log.d(TAG, "word.reading == " + word.reading);
				}
				if (word.kanji != null) {
					Log.d(TAG, "word.kanji == " + word.kanji);
				}
			}
			if (this.radioButtonSearchKanji.isChecked()) {
				searchString = word.kanji;
			} else {
				searchString = word.reading;
			}
			if (searchString != null) {
				int pos = spinnerWebType.getSelectedItemPosition();
				if (pos == 0) {
					startActivity(new Intent(this, DictWebListActivity.class)
						.putExtra(DictWebListActivity.EXTRA_KEY, searchString)
						.putExtra(DictWebListActivity.EXTRA_KEY_SHARE, word.toShareString())
					);
				} else {
					DictWebListActivity.execute(this, pos - 1, searchString, word.toShareString());
				}
			} else {
				Toast.makeText(this, 
					"搜索关键词为空", 
					Toast.LENGTH_SHORT)
					.show();
			}
		}
	}
	
    private int getCount() {
    	if (words != null) {
    		return words.size();
    	}
    	try {
    		if (dataType == DATA_TYPE_ENWORDS) {
    			return RandomDictLoader.getTotal(getAssets(), RandomDictLoader.TYPE_ENWORDS);
    		} else {
    			return RandomDictLoader.getTotal(getAssets(), RandomDictLoader.TYPE_JPWORDS);
        	}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
    }
	
    private Word getWord(int index) {
    	if (words != null) {
    		try {
    			return words.get(index);
    		} catch (Throwable e) {
    			e.printStackTrace();
    		}
    	}
		try {
			if (dataType == DATA_TYPE_ENWORDS) {
				return RandomDictLoader.getWord(getAssets(), index, RandomDictLoader.TYPE_ENWORDS);
			} else {
				return RandomDictLoader.getWord(getAssets(), index, RandomDictLoader.TYPE_JPWORDS);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
    }
    
    private int searchWord(String searchStr) {
    	int total = getCount();
    	if (dataType == DATA_TYPE_JPWORDS) {
    		searchStr = CharTrans.zenkakuHiraganaToZenkakuKatakana(searchStr);
    	}
    	if (total > 0) {
	    	int start = 0;
	    	int end = total - 1;
	    	int count = 0;
	    	while (true) {
	    		if (count >= total) {
	    			return -1;
	    		}
	    		count++;
	    		int mid = (start + end) >> 1;
    			if (D) {
    				Log.e(TAG, "mid == " + mid);
    			}
	    		if (mid == start || mid == end) {
	    			if (D) {
	    				Log.e(TAG, "word:" + getWord(mid).toString());
	    			}
	    			return mid;
	    		}
	    		Word word = getWord(mid);
    			if (dataType == DATA_TYPE_JPWORDS) {
		    		if (word != null && word.reading != null) {
		    			String wordStr = CharTrans.zenkakuHiraganaToZenkakuKatakana(word.reading);
			    		int r = compareStr(searchStr, wordStr);
			    		if (r == 0) {
			    			return mid;
			    		} else if (r < 0) { 
			    			end = mid;
			    		} else {
			    			start = mid;
			    		}
		    		} else {
		    			return mid;
		    		}
	    		} else { //dataType == DATA_TYPE_ENWORDS
	    			if (word != null && word.kanji != null) {
		    			String wordStr = word.kanji;
			    		int r = compareStr(searchStr, wordStr);
			    		if (r == 0) {
			    			return mid;
			    		} else if (r < 0) { 
			    			end = mid;
			    		} else {
			    			start = mid;
			    		}
		    		} else {
		    			return mid;
		    		}
	    		}
	    	}
    	} else {
    		return -1;
    	}
    }
    
    private int compareStr(String str1, String str2) {
    	if (str1 == null) {
    		return -1;
    	} else if (str2 == null) {
    		return 1;
    	} else {
    		return str1.compareTo(str2);
    	}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, SWITCH_INPUT_ID, Menu.NONE, "开/关搜索框");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case SWITCH_INPUT_ID:
			switchInput();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void switchInput() {
		if (searchLinearLayout.getVisibility() == LinearLayout.VISIBLE) {
			searchLinearLayout.setVisibility(LinearLayout.GONE);
			setSearchVisible(false);
		} else {
			searchLinearLayout.setVisibility(LinearLayout.VISIBLE);
			setSearchVisible(true);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (audioTrack != null) {
			audioTrack.release();
		}
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
            mTts = null;
        }
        this.words = null;
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
				doSearch();
				break;
			}
		}
	}
}
