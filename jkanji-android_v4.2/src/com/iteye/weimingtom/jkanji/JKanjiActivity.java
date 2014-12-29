package com.iteye.weimingtom.jkanji;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.nick.wwwjdic.krad.KradDb;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.Time;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/**
 * 日语简易词典
 * 文本框注意单行和多行的区别
 * @author weimingtom
 * FIXME: task可能被new多次，执行多次loadData
 * FIXME: this.getResources().getString == this.getString
 */
public class JKanjiActivity extends Activity {
	public static final String EXTRA_KEY = "com.iteye.weimingtom.jkanji.JKanjiActivity";
	public static final String EXTRA_KEYWORD = "com.iteye.weimingtom.jkanji.JKanjiActivity.EXTRA_KEYWORD";
	public static final String EXTRA_KEY_SHEARCHTEXT = "com.iteye.weimingtom.jkanji.JKanjiActivity.searchText";
	
	private static final boolean D = false;
	private static final String TAG = "JKanjiActivity";
	
	private static final boolean FAST_SAVE = false;
	private static final boolean ASYNC_LOAD_DICT = true;
	private static final boolean ASYNC_QUERY = true;
	
	private static final int REQUEST_HANDINPUT = 1;
	private static final int REQUEST_HISTORY = 2;
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    
	private static final boolean CHANGE_INPUT_METHOD_PICKER = true;
	
	private static DataContext cacheDataContext;
	public static DataContext getCacheDataContext() {
		return cacheDataContext;
	}
	
	private static ArrayList<String> cacheGB2SJ;
	public static ArrayList<String> getCacheGB2SJ() {
		return cacheGB2SJ;
	}
	
	public static final String SHARE_PREF_NAME = "pref";
	private static final String SHARE_PREF_INPUT_TEXT = "inputString";
	public static final String SHARE_PREF_OUTPUT_TEXT = "outputString";
	private static final String SHARE_PREF_ENABLE_CACHE = "enableCache";
	private static final String SHARE_PREF_SEARCH_TYPE = "searchType";
	private static final String SHARE_PREF_WEB_TYPE = "webType";
	private static final String SHARE_PREF_SHOW_OPTIONS = "showOptions";
	
	// 定时器间隔
	private static final int CHECK_TIME_LENGTH = 50;
	
	private EditText inputText;
	private GridView gridView;
	private CheckBox toggleKeyboard;
	
	//private CheckBox toggleFulltextSearch;
	private Spinner spinnerSearchType;
	private ArrayAdapter<String> spinnerSearchTypeAdapter;
	private Spinner spinnerWebType;
	private ArrayAdapter<String> spinnerWebTypeAdapter;
	
	private CheckBox checkBoxEnableCache;
	private LinearLayout outputLayout;
	private TextView[] textViews;
	private ProgressBar progressBar1;
	private TextView textView1;
	private ProgressBar progressBar2;
	private TextView textView2;
	private ProgressBar progressBar3; //PLUGIN
	private TextView textView3;
	
	private LinearLayout progressLayout;
	private LinearLayout mainLayout;
	private ScrollView scrollView1;
	private ActionBar actionBar;
	
	private LinearLayout linearLayoutOptions;
	private FrameLayout frameLayoutMainOutput;
	private TextView textViewInfo;
	//private Button buttonDelete;
	
	private ViewPager panelswitch;
	private Button buttonKeyDel;
	
	private volatile DataContext dataContext;
	private PackFileReadTask task;
	private CheckProgressHandler checkProgressHandler; 
	private PackFileReadTask task0;
	private CheckProgressHandler checkProgressHandler0; 
	private PackFileReadTask task1;
	private CheckProgressHandler checkProgressHandler1;	
	private PackFileReadTask task2; //PLUGIN
	private CheckProgressHandler checkProgressHandler2;
	
	private Typeface typeface;
	private boolean isSearchMini;
	
	//only for separate version
	private int cur0, max0, cur1, max1, cur2, max2;
	
	private final static int DIALOG_LIST = 1; 
	private ArrayList<String> voiceChoises;
	private ArrayAdapter<String> voiceAdapter;
	
	private boolean isUseKeyPager = true;
	private Button buttonEdit;
	
	private LinearLayout linearLayoutToolButtons;
	private ImageView ivTrans, ivSearch, ivDel;
	
	private TextView textViewProgressInfo;
	
	private final static boolean SHOW_HIS_OUTPUT = false;
	private QueryTask queryTask;
	private static String cacheDefaultOutputText;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	if (D) {
    		Log.e(TAG, "!!!onCreate");
    	}
    	super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        this.isUseKeyPager = JkanjiSettingActivity.getUseKeyPager(this);
        final String[] kbnames = JapaneseKeyboard.getSoftKeyboard();
        
        checkBoxEnableCache = (CheckBox) this.findViewById(R.id.checkBoxEnableCache);
        boolean lastEnabledCache = getLastEnableCache();
        checkBoxEnableCache.setChecked(lastEnabledCache);
        //
        if (lastEnabledCache && cacheDataContext != null) {
        	dataContext = cacheDataContext;
        	dataContext.isLoadFinish = true;
        } else {
        	cacheDataContext = null;
	        dataContext = (DataContext) getLastNonConfigurationInstance();
        }
        if (dataContext == null) {
        	dataContext = new DataContext();
        }
        if (dataContext.words == null) {
        	dataContext.words = new ArrayList<Word>();
        }
        if (dataContext.enwords == null) { //PLUGIN
        	dataContext.enwords = new ArrayList<Word>();
        }
        if (dataContext.resultWords == null) {
        	dataContext.resultWords = new ArrayList<Word>();
        }
        if (dataContext.gb2sj == null) {
        	//dataContext.gb2sj = new HashMap<Character, Character>();
        	dataContext.gb2sj = new TreeMap<Character, Character>();
        }
    	if (dataContext.data == null) {
            dataContext.data = new PackFileReadTask.SessionSaveData(); 
        }
    	if (dataContext.data0 == null) {
            dataContext.data0 = new PackFileReadTask.SessionSaveData(); 
        }
    	if (dataContext.data1 == null) {
            dataContext.data1 = new PackFileReadTask.SessionSaveData(); 
        }
    	if (dataContext.data2 == null) { //PLUGIN
            dataContext.data2 = new PackFileReadTask.SessionSaveData(); 
        }
    	
    	//
        progressBar1 = (ProgressBar) this.findViewById(R.id.progressBar1);
        textView1 = (TextView) this.findViewById(R.id.textView1);
        
        progressBar2 = (ProgressBar) this.findViewById(R.id.progressBar2);
        textView2 = (TextView) this.findViewById(R.id.textView2);
        //PLUGIN
        progressBar3 = (ProgressBar) this.findViewById(R.id.progressBar3);
        textView3 = (TextView) this.findViewById(R.id.textView3);
        if (DataContext.USE_SEPARATE_PACK) {
        	progressBar2.setVisibility(ProgressBar.VISIBLE);
        	textView2.setVisibility(TextView.VISIBLE);
        	progressBar3.setVisibility(ProgressBar.VISIBLE); //PLUGIN
        	textView3.setVisibility(TextView.VISIBLE);
        } else {
        	progressBar2.setVisibility(ProgressBar.GONE); 
        	textView2.setVisibility(TextView.GONE);  
        	progressBar3.setVisibility(ProgressBar.GONE); //PLUGIN
        	textView3.setVisibility(TextView.GONE);
        }
        
        mainLayout = (LinearLayout) this.findViewById(R.id.mainLayout);
        progressLayout = (LinearLayout) this.findViewById(R.id.progressLayout);
        scrollView1 = (ScrollView) this.findViewById(R.id.scrollView1);
        linearLayoutOptions = (LinearLayout) this.findViewById(R.id.linearLayoutOptions);
        frameLayoutMainOutput = (FrameLayout) this.findViewById(R.id.frameLayoutMainOutput);
        textViewInfo = (TextView) this.findViewById(R.id.textViewInfo);
        
        textViewProgressInfo = (TextView) this.findViewById(R.id.textViewProgressInfo);
        textViewProgressInfo.setText("(1/2)加载数据中..."); 
        
        inputText = (EditText) findViewById(R.id.inputText);
        
        panelswitch = (ViewPager) findViewById(R.id.panelswitch);
        panelswitch.setAdapter(new PageAdapter(panelswitch, kbnames));
        
        buttonKeyDel = (Button) findViewById(R.id.buttonKeyDel);
        buttonKeyDel.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		backDelete();
			}
        });
        buttonKeyDel.setOnLongClickListener(new OnLongClickListener() {
        	@Override
			public boolean onLongClick(View v) {
        		inputText.setText("");
        		return true;
			}
        });
        
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("搜索");
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
        /*
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.del;
			}

			@Override
			public void performAction(View view) {
				backDelete();
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.grid_key;
			}

			@Override
			public void performAction(View view) {
				if (getKeyVisibility() == View.VISIBLE) {
					toggleJapIME(false);
				} else {
					toggleJapIME(true);
				}
				hideKeyboard();
			}
        });
        */
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.config;
			}

			@Override
			public void performAction(View view) {
				boolean showOptions = getLastShowOptions();
				showOptions = !showOptions;
				setLastShowOptions(showOptions);
				if (showOptions) {
					linearLayoutOptions.setVisibility(LinearLayout.VISIBLE);
				} else {
					linearLayoutOptions.setVisibility(LinearLayout.GONE);
				}
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.search_web;
			}

			@Override
			public void performAction(View view) {
				if (mainLayout.getVisibility() == LinearLayout.VISIBLE) {
					if (inputText != null) {
						String str = inputText.getText().toString();
						int pos = spinnerWebType.getSelectedItemPosition();
						if (pos == 0) {
							startActivity(new Intent(JKanjiActivity.this, DictWebListActivity.class)
								.putExtra(DictWebListActivity.EXTRA_KEY, str));
						} else {
							DictWebListActivity.execute(JKanjiActivity.this, pos - 1, str, "");
						}
					}
				}
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.write_input;
			}

			@Override
			public void performAction(View view) {
				Intent intent = new Intent(JKanjiActivity.this, 
						JkanjiHandActivity.class);
				intent.putExtra(JkanjiHandActivity.EXTRA_KEY_INIT_STRING, 
						inputText.getText().toString());
				startActivityForResult(intent, REQUEST_HANDINPUT);
			}
        });
        
        if (!DataContext.DONNOT_LOAD_TYPEFACE) {
        	//typeface = Typeface.createFromAsset(getAssets(), "mplus-1m-regular.ttf");
        	//typeface = Typefaces.get(this, "mplus-1m-regular.ttf");
        	typeface = Typefaces.getFile(JkanjiSettingActivity.getFontFileName(this));	
        }
        final String defaultInputText = getLastSearchInput(); 
        if (defaultInputText != null) {
        	inputText.getEditableText().replace(inputText.getSelectionStart(),
					inputText.getSelectionEnd(), defaultInputText);
        }
        inputText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					toggleJapIME(false);
					if (inputText != null) {
						setLastSearchInput(inputText.getText().toString());
						queryDict(inputText.getText().toString(), false);
					}
					return true;
				}
				return false;
			}
        });
        
        outputLayout = (LinearLayout) this.findViewById(R.id.outputLayout);
        textViews = new TextView[DataContext.TEXT_LINE_MAX * 2 + 5]; //保险起见

        /**
         * FIXME: see endLoadOutputText
         */
        if (false) {
        	if(!FAST_SAVE) {
		        final String defaultOutputText = getLastSearchOutput();
		        endLoadOutputText(defaultOutputText);
        	}
        } else {
        	if (cacheDefaultOutputText != null) {
        		if (false) {
        			endLoadOutputText(cacheDefaultOutputText);
        		} else {
        			if (SHOW_HIS_OUTPUT) {
	        			textViewInfo.setText("加载上一次搜索结果...");
	        			outputLayout.postDelayed(new Runnable() {
							@Override
							public void run() {
								endLoadOutputText(cacheDefaultOutputText);
								textViewInfo.setText("");
							}
	        			}, 500);
	        		}
        		}
        	}
        }
        
        final Button buttonStartSearch = (Button) this.findViewById(R.id.buttonStartSearch);
        buttonStartSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isSearchMini) {
					miniSearchStart();
				} else {
					toggleJapIME(false);
					if (inputText != null) {
						if (!FAST_SAVE) {
							setLastSearchInput(inputText.getText().toString());
						}
						queryDict(inputText.getText().toString(), false);
					}
				}
			}
        });
        buttonStartSearch.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (inputText != null) {
					String str = inputText.getText().toString();
					startActivity(new Intent(JKanjiActivity.this, DictWebListActivity.class).putExtra(DictWebListActivity.EXTRA_KEY, str));
				}
		        return true;
			}
        });
        
        buttonEdit = (Button) this.findViewById(R.id.buttonEdit);
        buttonEdit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getKeyVisibility() == View.VISIBLE) {
					//变换
					if (false) {
						String str = inputText.getEditableText().toString();
						String right = JapaneseKeyboard.nextChar(str, inputText.getSelectionStart() - 1);
						if (right != null) {
							inputText.getEditableText().replace(inputText.getSelectionStart() - 1,
									inputText.getSelectionEnd(), right);
						}
					} else {
						String str = inputText.getEditableText().toString();
						int selpos = inputText.getSelectionStart() - 1;
						String right = JapaneseKeyboard.nextChar(str, selpos);
						if (right != null) {
							inputText.getEditableText().replace(selpos,
									inputText.getSelectionEnd(), right);
						} else {
							String right2 = nextKanji(str, selpos);
							if (right2 != null) {
								inputText.getEditableText().replace(selpos,
										inputText.getSelectionEnd(), right2);
							}
						}
					}
				} else {
					miniSearchStart();
				}
			}
        });
        buttonEdit.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
    			startActivityForResult(
    				new Intent(JKanjiActivity.this, JkanjiHistoryActivity.class)
    					.putExtra(JkanjiHistoryActivity.EXTRA_GET, true),
    				REQUEST_HISTORY);
				return true;
			}
        });
        
        //
        
        gridView = (GridView) findViewById(R.id.gridView1);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.grid_text_view, kbnames);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				if (inputText != null) {
					//TODO: special soft keyboard!
					if (kbnames[position].equals("°")) {
						String str = inputText.getEditableText().toString();
						String right = JapaneseKeyboard.nextChar(str, inputText.getSelectionStart() - 1);
						if (right != null) {
							inputText.getEditableText().replace(inputText.getSelectionStart() - 1,
									inputText.getSelectionEnd(), right);
						}
					} else {
						inputText.getEditableText().replace(inputText.getSelectionStart(),
							inputText.getSelectionEnd(), kbnames[position]);
					}
				}
			}
        });
        
        toggleKeyboard = (CheckBox) this.findViewById(R.id.toggleKeyboard);
        /*
        toggleKeyboard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleJapIME(toggleKeyboard.isChecked());
			}
        });
        */
        toggleKeyboard.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				toggleJapIME(isChecked);
			}
        });
        toggleKeyboard.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				/**
				 * Change input method
				 * @see http://stackoverflow.com/questions/9777406/how-to-set-call-an-new-input-method-in-android
				 */
				if (CHANGE_INPUT_METHOD_PICKER) {
					InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					if (mgr != null) {
						mgr.showInputMethodPicker();
					}
				} else {
					startActivityForResult(
						    new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS), 0);
				}
				return true;
			}
        });
        if (dataContext != null) {
        	toggleJapIME(dataContext.isKeyboardChecked);
        }
        if (toggleKeyboard != null) {
        	//toggleKeyboard.setFocusableInTouchMode(true);
        	toggleKeyboard.requestFocus();
        	//toggleKeyboard.requestFocusFromTouch();
        }
        
        /*
        toggleFulltextSearch = (CheckBox) this.findViewById(R.id.toggleFulltextSearch);
        toggleFulltextSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
        });
        */
        spinnerSearchType = (Spinner) this.findViewById(R.id.spinnerSearchType);
        spinnerSearchTypeAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_spinner_item);
        spinnerSearchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSearchTypeAdapter.add("全文"); //全文搜索（搜索含义）
        spinnerSearchTypeAdapter.add("包含"); //词条搜索（不搜索含义）
        spinnerSearchTypeAdapter.add("前缀"); //词条前缀搜索（不搜索含义）
        spinnerSearchTypeAdapter.add("后缀"); //词条后缀搜索（不搜索含义）
        spinnerSearchTypeAdapter.add("完全");
        spinnerSearchTypeAdapter.add("英文全文"); //英文全文搜索（搜索含义）
        spinnerSearchTypeAdapter.add("英文包含"); //英文词条搜索（不搜索含义）
        spinnerSearchTypeAdapter.add("英文前缀"); //英文词条前缀搜索（不搜索含义）
        spinnerSearchTypeAdapter.add("英文后缀"); //英文词条后缀搜索（不搜索含义）
        spinnerSearchTypeAdapter.add("英文完全");
        spinnerSearchType.setAdapter(spinnerSearchTypeAdapter);
        spinnerSearchType.setSelection(getLastSearchType());
        spinnerSearchType.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int postion, long id) {
				setLastSearchType(postion);
				if (mainLayout.getVisibility() == LinearLayout.VISIBLE) {
					toggleJapIME(false);
					if (inputText != null) {
						if (!FAST_SAVE) {
							setLastSearchInput(inputText.getText().toString());
						}
						if (true) {
							queryDict(inputText.getText().toString(), false);
						}
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
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
        
        linearLayoutToolButtons = (LinearLayout) this.findViewById(R.id.linearLayoutToolButtons);
        ivTrans = (ImageView) this.findViewById(R.id.ivTrans);
        ivSearch = (ImageView) this.findViewById(R.id.ivSearch);
        ivDel = (ImageView) this.findViewById(R.id.ivDel);
        
        
        if (JkanjiSettingActivity.getUseToolButtons(this)) {
        	linearLayoutToolButtons.setVisibility(View.VISIBLE);
        } else {
        	linearLayoutToolButtons.setVisibility(View.GONE);
        }
        
        ivTrans.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String str = inputText.getEditableText().toString();
				int selpos = inputText.getSelectionStart() - 1;
				String right = JapaneseKeyboard.nextChar(str, selpos);
				if (right != null) {
					inputText.getEditableText().replace(selpos,
							inputText.getSelectionEnd(), right);
				} else {
					String right2 = nextKanji(str, selpos);
					if (right2 != null) {
						inputText.getEditableText().replace(selpos,
								inputText.getSelectionEnd(), right2);
					}
				}
			}
        });
        ivSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isSearchMini) {
					miniSearchStart();
				} else {
					toggleJapIME(false);
					if (inputText != null) {
						if (!FAST_SAVE) {
							setLastSearchInput(inputText.getText().toString());
						}
						queryDict(inputText.getText().toString(), false);
					}
				}
			}
        });
        ivDel.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		backDelete();
			}
        });
        ivDel.setOnLongClickListener(new OnLongClickListener() {
        	@Override
			public boolean onLongClick(View v) {
        		inputText.setText("");
				InputMethodManager imm = (InputMethodManager)
						getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null) {
					imm.showSoftInput(inputText, InputMethodManager.SHOW_FORCED);
				}
				return true;
			}
        });
        
        
        
        boolean showOptions = getLastShowOptions();
		if (showOptions) {
			linearLayoutOptions.setVisibility(LinearLayout.VISIBLE);
		} else {
			linearLayoutOptions.setVisibility(LinearLayout.GONE);
		}
        
        voiceChoises = new ArrayList<String>();
        voiceAdapter = new ArrayAdapter<String>(this,
                android.R.layout.select_dialog_item, voiceChoises);
        
        this.checkSearchMini();
        
        Intent intent = this.getIntent();
        if (intent != null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEND)) {
        	String searchText = intent.getStringExtra(Intent.EXTRA_TEXT);
        	if (searchText != null) {
        		this.inputText.setText("");
        		this.inputText.append(searchText);
        		dataContext.isPreSearch = true;
        	}
        } else if (intent != null && intent.getStringExtra(EXTRA_KEY_SHEARCHTEXT) != null) {
        	String searchText = intent.getStringExtra(EXTRA_KEY_SHEARCHTEXT);
        	if (searchText != null) {
        		this.inputText.setText("");
        		this.inputText.append(searchText);
        		dataContext.isPreSearch = true;
        	}
        }
    }
    
    /**
     * FIXME:
     */
    private void voiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "日语简易词典语音输入");
        try {
        	startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } catch (Throwable e) {
        	Toast.makeText(JKanjiActivity.this, 
        			"找不到合适的应用程序", 
        			Toast.LENGTH_SHORT).show();
        	e.printStackTrace();
        }
    }
    
    /**
     * FIXME:
     * 
     */
    private void backDelete() {
		if (inputText != null) {
			int st = inputText.getSelectionStart() - 1;
			int ed = inputText.getSelectionEnd();
			if (st >= 0 && ed >= 0) {
				inputText.getEditableText().delete(st, ed);				
			}
		}
    }
    
    /**
     * FIXME:
     * 
     */
    private void deleteAll() {
		if (inputText != null) {
			inputText.setText(null);
		}
		toggleJapIME(true);
		hideKeyboard();
    }
    
    /**
     * @see http://orgcent.com/android-dpsppx-unit-conversion/
     * @see http://blog.csdn.net/zhangnianxiang/article/details/6723648
     */
    
    /**
    * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
    */
//    public static int dip2px(Context context, float dpValue) {
//      final float scale = context.getResources().getDisplayMetrics().density;
//      return (int) (dpValue * scale + 0.5f);
//    }

    /**
    * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
    */
//    public static int px2dip(Context context, float pxValue) {
//      final float scale = context.getResources().getDisplayMetrics().density;
//      return (int) (pxValue / scale + 0.5f);
//    }
    
    private void setOutputText(String str, String keyword) {
    	String output_absent_text = this.getResources().getString(R.string.output_absent_text);
    	String output_overflow_text = this.getResources().getString(R.string.output_overflow_text);
    	if (str == null) {
    		str = "";
    	}
    	String texts[] = str.split("\\n");
    	int i;
    	outputLayout.removeAllViews();
    	//float scale = this.getResources().getDisplayMetrics().scaledDensity;
    	for (i = 0; i < textViews.length; i++) {
    		TextView tv = textViews[i];
    		String txt = i < texts.length ? texts[i] : null;
    		if (tv == null) {
    			tv = textViews[i] = new TextView(this);
    		}
    		if (txt == null) {
    			break;
    		} else if (txt.startsWith("(")) {
    			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
    			tv.setTextColor(Color.BLUE);
    			tv.setText(getColorString(txt, keyword));
    			tv.setTypeface(typeface);
    			tv.setVisibility(TextView.VISIBLE);
    		} else if (txt.startsWith(output_absent_text) || txt.startsWith(output_overflow_text)) {
    			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
    			tv.setTextColor(Color.RED);
    			tv.setText(txt);
    			tv.setTypeface(null);
    			tv.setVisibility(TextView.VISIBLE);
    		} else {
    			tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
    			tv.setTextColor(Color.BLACK);
    			tv.setText(getColorString(txt, keyword));
    			tv.setTypeface(null);
    			tv.setVisibility(TextView.VISIBLE);
    		}
        	outputLayout.addView(tv, new LinearLayout.LayoutParams(
        			LinearLayout.LayoutParams.MATCH_PARENT, 
        			LinearLayout.LayoutParams.WRAP_CONTENT));
    	}
    	if (scrollView1 != null) {
    		scrollView1.scrollTo(0, 0);
    	}
    }

    private CharSequence getColorString(String str, String keyword) {
    	if (str != null) {
    		SpannableString spannable = new SpannableString(str);
    		if (keyword != null && keyword.length() > 0) {
        		switch (JkanjiSettingActivity.getHLType(this)) {
        		default:
            	case JkanjiSettingActivity.HL_TYPE_CHAR: {
                		String keyword2 = getShiftJIS(keyword);
                		if (keyword2 != null) {
                			keyword = keyword + keyword2;
                		}
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
    
    private void toggleJapIME(boolean isChecked) {
		if (isChecked) {
			setKeyVisibility(View.VISIBLE);
			outputLayout.setVisibility(View.INVISIBLE);
		} else {
			setKeyVisibility(View.INVISIBLE);
			outputLayout.setVisibility(View.VISIBLE);
		}
		toggleKeyboard.setChecked(isChecked);
		if (dataContext != null) {
			dataContext.isKeyboardChecked = isChecked;
		}
    }
    
    @Override
	public Object onRetainNonConfigurationInstance() {
    	if (D) {
    		Log.e(TAG, "!!!onRetainNonConfigurationInstance");
    	}
		if (checkProgressHandler != null) {
			checkProgressHandler.setState(CheckProgressHandler.STOP);
		}
		if (checkProgressHandler0 != null) {
			checkProgressHandler0.setState(CheckProgressHandler.STOP);
		}
		if (checkProgressHandler1 != null) {
			checkProgressHandler1.setState(CheckProgressHandler.STOP);
		}
		//PLUGIN
		if (checkProgressHandler2 != null) {
			checkProgressHandler2.setState(CheckProgressHandler.STOP);
		}
		this.stopTask();
    	return dataContext;
	}
    
    private void stopTask() {
    	if (DataContext.USE_SEPARATE_PACK) {
			if (task0 != null) {
				task0.setIsRunning(false);
				if (task0.getCurrentProgress() < task0.getMaxProgress()) {
					try {
						task0.get(1000, TimeUnit.MILLISECONDS);
						if (D) {
							Log.e(TAG, "stopTask success");
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					} catch (TimeoutException e) {
						e.printStackTrace();
					}
				}
			}
			if (task1 != null) {
				task1.setIsRunning(false);
				if (task1.getCurrentProgress() < task1.getMaxProgress()) {
					try {
						task1.get(1000, TimeUnit.MILLISECONDS);
						if (D) {
							Log.e(TAG, "stopTask success");
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					} catch (TimeoutException e) {
						e.printStackTrace();
					}
				}
			}
			//PLUGIN
			if (task2 != null) {
				task2.setIsRunning(false);
				if (task2.getCurrentProgress() < task2.getMaxProgress()) {
					try {
						task2.get(1000, TimeUnit.MILLISECONDS);
						if (D) {
							Log.e(TAG, "stopTask success");
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					} catch (TimeoutException e) {
						e.printStackTrace();
					}
				}
			}
    	} else {
			if (task != null) {
				task.setIsRunning(false);
				if (task.getCurrentProgress() < task.getMaxProgress()) {
					try {
						task.get(1000, TimeUnit.MILLISECONDS);
						if (D) {
							Log.e(TAG, "stopTask success");
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					} catch (TimeoutException e) {
						e.printStackTrace();
					}
				}
			}
    	}
    }
    
    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
    	if (D) {
    		Log.e(TAG, "!!!onPause");
    	}
		if (FAST_SAVE) {
			setLastSearchInput(inputText.getText().toString());
		}
		//FIXME:
		if (checkProgressHandler != null) {
			checkProgressHandler.setState(CheckProgressHandler.STOP);
		}
		if (checkProgressHandler0 != null) {
			checkProgressHandler0.setState(CheckProgressHandler.STOP);
		}
		if (checkProgressHandler1 != null) {
			checkProgressHandler1.setState(CheckProgressHandler.STOP);
		}
		//PLUGIN
		if (checkProgressHandler2 != null) {
			checkProgressHandler2.setState(CheckProgressHandler.STOP);
		}
		this.stopTask();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
    	if (D) {
    		Log.e(TAG, "!!!onResume");
    	}
        if (dataContext.isLoadFinish) {
        	this.startForgroundService();
        	mainLayout.setVisibility(LinearLayout.VISIBLE);
			progressLayout.setVisibility(LinearLayout.INVISIBLE);
			if (dataContext.isPreSearch) {
				queryDict(inputText.getText().toString(), false);
				dataContext.isPreSearch = false;
			}
        } else {
        	mainLayout.setVisibility(LinearLayout.INVISIBLE);
			progressLayout.setVisibility(LinearLayout.VISIBLE);
			if (D) {
				Log.e(TAG, "onResume, loadData");
			}
			if (DataContext.USE_SEPARATE_PACK) {
				loadSeparateData();
			} else {
				loadData();
			}
        }
        if (toggleKeyboard != null) {
        	//toggleKeyboard.setFocusableInTouchMode(true);
        	toggleKeyboard.requestFocus();
        	//toggleKeyboard.requestFocusFromTouch();
        }
        //GC
        System.gc();
	}

	@Override
	protected void onStop() {
    	super.onStop();
    	if (D) {
    		Log.e(TAG, "!!!onStop");
    	}
    	
    	boolean enableCache = checkBoxEnableCache.isChecked();
    	this.setLastEnableCache(enableCache);
    	if (enableCache && 
    		this.dataContext.words != null && 
    		this.dataContext.gb2sj != null &&
    		this.dataContext.enwords != null &&
    		this.dataContext.isLoadFinish) {
    		cacheDataContext = new DataContext();
    		cacheDataContext.words = this.dataContext.words;
    		cacheDataContext.gb2sj = this.dataContext.gb2sj;
    		cacheDataContext.enwords = this.dataContext.enwords; //PLUGIN
    		cacheDataContext.isLoadFinish = false;
    	} else {
    		cacheDataContext = null;
    		cacheGB2SJ = null;
    	}
    	
		if (checkProgressHandler != null) {
			checkProgressHandler.setState(CheckProgressHandler.STOP);
			checkProgressHandler = null; //FIXME: GC
		}
		this.stopTask();
		//GC
		System.gc();
		/*
		if (this.dataContext != null) {
			if (dataContext.gb2sj != null) {
				dataContext.gb2sj.clear();
				dataContext.gb2sj = null;
			}
			if (dataContext.resultWords != null) {
				dataContext.resultWords.clear();
				dataContext.resultWords = null;
			}
			if (dataContext.words != null) {
				dataContext.words.clear();
				dataContext.words = null;
			}
		}
		*/
    }

	@Override
	protected Dialog onCreateDialog(int id) {
    	switch(id) {
        case DIALOG_LIST:
            return new AlertDialog.Builder(this)
                .setTitle("选择语音输入")
                .setAdapter(voiceAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = voiceAdapter.getItem(which);
                        if (input != null && input.length() > 0) {
                        	inputText.setText("");
                        	inputText.append(input);
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
                })
                .setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						
					}
                })
                .create();
        }
    	return null;
	}
    
    private void setLastSearchInput(String str) {
		PrefUtil.putString(this, SHARE_PREF_NAME, 
				SHARE_PREF_INPUT_TEXT, 
				str);
    }
    
    private String getLastSearchInput() {
		return PrefUtil.getString(this, SHARE_PREF_NAME, 
				SHARE_PREF_INPUT_TEXT, 
    			"");
    }
    
    private void setLastSearchOutput(String str) {
		PrefUtil.putString(this, SHARE_PREF_NAME, 
				SHARE_PREF_OUTPUT_TEXT, 
				str);
    }
    
    private String getLastSearchOutput() {
    	return PrefUtil.getString(this, SHARE_PREF_NAME, 
    			SHARE_PREF_OUTPUT_TEXT, 
    			this.getResources().getString(R.string.output_default_text));
    }

    private void setLastEnableCache(boolean isEnabled) {
		PrefUtil.putBoolean(this, SHARE_PREF_NAME, 
				SHARE_PREF_ENABLE_CACHE, 
				isEnabled);
    }
    
    private boolean getLastEnableCache() {
    	return PrefUtil.getBoolean(this, SHARE_PREF_NAME, 
    			SHARE_PREF_ENABLE_CACHE, 
				true);
    }
    
    private void setLastSearchType(int searchType) {
		PrefUtil.putInt(this, SHARE_PREF_NAME, 
				SHARE_PREF_SEARCH_TYPE, 
    			searchType);
    }
    
    private int getLastSearchType() {
    	return PrefUtil.getInt(this, SHARE_PREF_NAME, 
    			SHARE_PREF_SEARCH_TYPE, 
				0);
    }

    private void setLastWebType(int webType) {
		PrefUtil.putInt(this, SHARE_PREF_NAME, 
    			SHARE_PREF_WEB_TYPE, 
    			webType);
    }
    
    private int getLastWebType() {
    	return PrefUtil.getInt(this, SHARE_PREF_NAME, 
    			SHARE_PREF_WEB_TYPE, 
				0);
    }
    
    private void setLastShowOptions(boolean isEnabled) {
		PrefUtil.putBoolean(this, SHARE_PREF_NAME, 
				SHARE_PREF_SHOW_OPTIONS, 
				isEnabled);
    }
    
    private boolean getLastShowOptions() {
    	return PrefUtil.getBoolean(this, SHARE_PREF_NAME, 
				SHARE_PREF_SHOW_OPTIONS, 
				true);
    }

    private final static class QueryInfo {
    	String str;
    	String title;
    	int searchType;
    	String output;
    	long lastSearchTime;
    	int hisNum;
    }
    
    private void queryDict(String str, boolean isStartAct) {
    	if (ASYNC_QUERY) {
    		if (queryTask == null) {
    			queryTask = new QueryTask(this, str, isStartAct);
    			queryTask.execute();
    		} else {
    			Toast.makeText(this, "搜索未完成，请稍候", 
    				Toast.LENGTH_SHORT).show();
    		}
    	} else {
	    	QueryInfo info = queryDictPre(str);
	    	queryDictProgress(info);
			queryDictPost(info);
			if (isStartAct) {
				queryDictStartAct();
			}
    	}
    }
    
    private void queryDictStartAct() {
		startActivity(new Intent(JKanjiActivity.this, JkanjiEditerActivity.class)
			.putParcelableArrayListExtra(EXTRA_KEY, dataContext.resultWords)
			.putExtra(EXTRA_KEYWORD, inputText.getText().toString())
		);
    }
    
    private QueryInfo queryDictPre(String str) {
    	QueryInfo info = new QueryInfo();
    	info.str = str;
		String title = "搜索";
    	int searchType = spinnerSearchType.getSelectedItemPosition();		
    	switch (searchType) {
		default:
		case 0: // 全文
			title = "全文搜索";
			break;
			
		case 1: // 包含
			title = "包含搜索";
			break;
			
		case 2: // 前缀
			title = "前缀搜索";
			break;
			
		case 3: // 后缀
			title = "后缀搜索";
			break;
			
		case 4: // 完全
			title = "完全搜索";
			break;	

		case 5: // 英文全文
			title = "英文全文";
			break;
			
		case 6: // 英文包含
			title = "英文包含";
			break;
			
		case 7: // 英文前缀
			title = "英文前缀";
			break;
			
		case 8: // 英文后缀
			title = "英文后缀";
			break;
			
		case 9: // 英文完全
			title = "英文完全";
			break;	
		}
    	info.title = title;
    	info.searchType = searchType;
    	return info;
    }

    private void queryDictProgress(QueryInfo info) {
		StringBuffer sb = new StringBuffer();
		info.lastSearchTime = System.currentTimeMillis();
		info.hisNum = -1;
		if (!dataContext.isPreSearch) {
			info.hisNum = JkanjiHistoryActivity.writeItem(this, info.str, DataContext.HISTORY_FILENAME);
		}
		String text = info.str;
		int id = 1;
		boolean isOverflow = false;
		if (dataContext.resultWords != null) {
			dataContext.resultWords.clear();
		}
		if (text.length() > 0) {
			String text2 = getShiftJIS(text);
			//if (D)
			//	Log.e(TAG, text2);
			List<Word> searchWords = null;
			if (info.searchType > 4) { //英文搜索
				searchWords = dataContext.enwords;
			} else {
				searchWords = dataContext.words;
			}
			for (Word word : searchWords) {
				if (word != null) {
					boolean isMatch = false;
					if (spinnerSearchType != null) {
						//see spinnerSearchTypeAdapter!!!
						switch (info.searchType) {
						default:
						case 0: // 全文搜索
						case 5:
							if ((word.kanji != null && (word.kanji.indexOf(text2) != -1 || word.kanji.indexOf(text) != -1)) || 
								(word.reading != null && (word.reading.indexOf(text2) != -1 || word.reading.indexOf(text) != -1)) ||
								(word.mean != null && (word.mean.indexOf(text2) != -1 || word.mean.indexOf(text) != -1))) {
								isMatch = true;
							}
							break;
						
						case 1: // 包含搜索
						case 6:
							if ((word.kanji != null && (word.kanji.indexOf(text2) != -1 || word.kanji.indexOf(text) != -1)) || 
								(word.reading != null && (word.reading.indexOf(text2) != -1 || word.reading.indexOf(text) != -1))) {
								isMatch = true;
							}						
							break;
							
						case 2: // 词条前缀搜索
						case 7:
							if ((word.kanji != null && (word.kanji.startsWith(text2) || word.kanji.startsWith(text))) || 
								(word.reading != null && (word.reading.startsWith(text2) || word.reading.startsWith(text)))) {
								isMatch = true;
							}
							break;
							
						case 3: // 后缀搜索
						case 8:
							if ((word.kanji != null && (word.kanji.endsWith(text2) || word.kanji.endsWith(text))) || 
								(word.reading != null && (word.reading.endsWith(text2) || word.reading.endsWith(text)))) {
								isMatch = true;
							}						
							break;
							
						case 4: // 完全搜索
						case 9:
							if ((word.kanji != null && (word.kanji.equals(text2) || word.kanji.equals(text))) || 
								(word.reading != null && (word.reading.equals(text2) || word.reading.equals(text)))) {
								isMatch = true;
							}
							break;
						}
					}
					if (isMatch) {
						String reading = (word.reading != null ? ("【" + word.reading + "】") : "");
						String reading2 = (word.reading != null ? word.reading : "");
						String kanji = ((word.kanji != null && word.kanji.length() > 0) ? word.kanji : reading2);
						sb.append("(" + id + ") " + 
								kanji + 
								reading + word.getAccent() + "\n" + 
								((word.catalog != null && word.catalog.length() > 0)? "  【" + word.catalog + "】\n" : "") +
								(word.mean != null ? CharTrans.formatMean(word.mean, true) : /*output_nomean_text*/(this.getResources().getString(R.string.output_nomean_text) + "\n")) + "\n");
						dataContext.resultWords.add(word);
						id++;
					}
				}
				if (id > DataContext.TEXT_LINE_MAX) {
					isOverflow = true;
					break;
				}
			}
		} else {
			sb.append(this.getResources().getString(R.string.output_default_text));
		}
		if (sb.length() == 0) {
			sb.append(this.getResources().getString(R.string.output_absent_text) + text /*+ "。考虑选择全文搜索，或长按搜索按钮打开web搜索器，如果含有简体汉字，请用手写板变换按钮变换成合适的日语汉字。"*/);
		}
		info.output = "";
		if (isOverflow) {
			info.output += this.getResources().getString(R.string.output_overflow_text) + "\n";
		}
		info.output += sb.toString();
		info.output += "\n ";
		if (!FAST_SAVE) {
			if (SHOW_HIS_OUTPUT) { 
		    	cacheDefaultOutputText = info.output;
				this.setLastSearchOutput(info.output);
			}
		}
    }
    
    private void queryDictPost(QueryInfo info) {
		setOutputText(info.output, info.str);
		long searchTime = System.currentTimeMillis() - info.lastSearchTime;
		info.title = info.title + "," + info.str + ",历史:" + info.hisNum + ",结果:" + dataContext.resultWords.size() + ",耗时:" + (searchTime / 1000.0) + "s";
		//this.actionBar.setTitle(title);
		textViewInfo.setText(info.title);
		
		hideKeyboard();
    }
    
    private String getShiftJIS(String text) {
		StringBuffer tb = new StringBuffer();
		if (dataContext != null && dataContext.gb2sj != null) {
			for (int i = 0; i < text.length(); i++) {
				Character ch = text.charAt(i);
				Character ch2 = dataContext.gb2sj.get(ch);
				if (ch2 == null) {
					tb.append(ch);
				} else {
					tb.append(ch2);
				}
			}
		}
		return tb.toString();
    }
    
	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)
				getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(inputText.getWindowToken(), 0);
	}
    
    private void loadData() {
    	if (DataContext.DONNOT_LOAD_DIC)
    		return;
    	//FIXME: 可能多次执行loadData，待修改
    	if (task == null || (task != null && !task.getIsRunning())) {
    		if (D) {
    			Log.e(TAG, "loadData()===================");
    		}
    		task = new PackFileReadTask(this.getAssets(), DataContext.DATA_FILE_NAMES, dataContext.data);
    		checkProgressHandler = new CheckProgressHandler(task, CHECK_TIME_LENGTH) {
	        	@Override
	        	protected void onTimer(int cur, int max) {
	        		super.onTimer(cur, max);
	        		if (progressBar1 != null) {
	        			progressBar1.setMax(max);
	        			progressBar1.setProgress(cur);
	        		}
	        		if (textView1 != null) {
	        			textView1.setText("" + (int)(((float)cur / max) * 100) + "%");
	        		}
	        		if (cur > 0 && cur == max) {
	        			loadDict();
	        			endLoadSeparateDicts();
	        		}
	        	}
	        };
	        if (checkProgressHandler.startTimer()) {
	        	task.execute();
	        }
    	}
    }
    
    //fixme;
    /**
     * FIXME:
     */
    private void loadSeparateData() {
    	if (DataContext.DONNOT_LOAD_DIC)
    		return;
    	//FIXME: 可能多次执行loadData，待修改
    	if (task0 == null || 
    		(task0 != null && !task0.getIsRunning()) ||
    		task1 == null || 
    		(task1 != null && !task1.getIsRunning()) ||
    		task2 == null || //PLUGIN
    		(task2 != null && !task2.getIsRunning())
    		) {
    		if (D) {
    			Log.e(TAG, "loadSeparateData()===================");
    		}
    		task0 = new PackFileReadTask(this.getAssets(), DataContext.DATA0_FILE_NAMES, dataContext.data0);
    		checkProgressHandler0 = new CheckProgressHandler(task0, CHECK_TIME_LENGTH) {
	        	@Override
	        	protected void onTimer(int cur, int max) {
	        		super.onTimer(cur, max);
	        		if (progressBar1 != null) {
	        			progressBar1.setMax(max);
	        			progressBar1.setProgress(cur);
	        		}
	        		if (textView1 != null) {
	        			textView1.setText("加载中(jpwords_3_11.csv):" + (int)(((float)cur / max) * 100) + "%" + "\n" +
	        					cur + "/" + max);
	        		}
	        		cur0 = cur;
	        		max0 = max;
	        		checkSeparateFinish();
	        	}
	        };
	        if (checkProgressHandler0.startTimer()) {
	        	task0.execute();
	        }
    		task1 = new PackFileReadTask(this.getAssets(), DataContext.DATA1_FILE_NAMES, dataContext.data1);
    		checkProgressHandler1 = new CheckProgressHandler(task1, CHECK_TIME_LENGTH) {
	        	@Override
	        	protected void onTimer(int cur, int max) {
	        		super.onTimer(cur, max);
	        		if (progressBar2 != null) {
	        			progressBar2.setMax(max);
	        			progressBar2.setProgress(cur);
	        		}
	        		if (textView2 != null) {
	        			textView2.setText("加载中(sj2gb_3_7.txt):" + (int)(((float)cur / max) * 100) + "%" + "\n" +
	        					cur + "/" + max);
	        		}
	        		cur1 = cur;
	        		max1 = max;
	        		checkSeparateFinish();
	        	}
	        };
	        if (checkProgressHandler1.startTimer()) {
	        	task1.execute();
	        }
	        //PLUGIN
    		task2 = new PackFileReadTask(this.getAssets(), DataContext.DATA2_FILE_NAMES, dataContext.data2);
    		checkProgressHandler2 = new CheckProgressHandler(task2, CHECK_TIME_LENGTH) {
	        	@Override
	        	protected void onTimer(int cur, int max) {
	        		super.onTimer(cur, max);
	        		if (progressBar3 != null) {
	        			progressBar3.setMax(max);
	        			progressBar3.setProgress(cur);
	        		}
	        		if (textView3 != null) {
	        			textView3.setText("加载中(enwords_3_6.csv):" + (int)(((float)cur / max) * 100) + "%" + "\n" +
	        					cur + "/" + max);
	        		}
	        		cur2 = cur;
	        		max2 = max;
	        		checkSeparateFinish();
	        	}
	        };
	        if (checkProgressHandler2.startTimer()) {
	        	task2.execute();
	        }
    	}
    }
    
    private void checkSeparateFinish() {
		if (cur0 > 0 && cur0 == max0 && 
			cur1 > 0 && cur1 == max1 &&
			cur2 > 0 && cur2 == max2 //PLUGIN
			) {
			if (ASYNC_LOAD_DICT) {
				new LoadDataTask(this).execute();
			} else {
				loadSeparateDicts();
				endLoadSeparateDicts();
			}
		}
    }
    
    private void endLoadSeparateDicts() {
		if (dataContext != null) {
			dataContext.isLoadFinish = true;
			startForgroundService();
		}
		mainLayout.setVisibility(LinearLayout.VISIBLE);
		progressLayout.setVisibility(LinearLayout.INVISIBLE);
		if (dataContext.isPreSearch) {
			queryDict(inputText.getText().toString(), false);
			dataContext.isPreSearch = false;
		}
    }
    
    /**
     * 已废弃，用于合并包，现使用loadSeparateDicts
     */
    private void loadDict() {
    	if (D)
    		Log.e(TAG, "Begin loading files to memory hash map");
    	if (!DataContext.DONNOT_LOAD_DICT && dataContext != null && dataContext.data != null && 
    		dataContext.data.dict_size != null &&
    		dataContext.data.dict_size.length >= 3 && //PLUGIN 
    		dataContext.data.dict_items != null) {
    		int id = 0;
    		int[] dict_size = dataContext.data.dict_size;
    		String[][] dict_items = dataContext.data.dict_items;
    		//jpword.txt
			if (!DataContext.DONNOT_LOAD_WORDS && dataContext.words != null) {
				dataContext.words.clear();
    			for (; id < dict_size[0]; id++) {
    				Word word = new Word(id, dict_items[id]);
    				//if (D)
    				//	Log.e(TAG, word.toString());
    				dataContext.words.add(word);
    			}
            	if (D)
            		Log.e(TAG, "loading words:" + dataContext.words.size());
			}
    		//sj2gb.txt
			if (!DataContext.DONNOT_LOAD_GB2SJ && dataContext.gb2sj != null) {
				dataContext.gb2sj.clear();
				//if (cacheGB2SJ == null) {
					cacheGB2SJ = new ArrayList<String>();
				//}
				for (;id < dict_size[0] + dict_size[1]; id++) {
    				String gb = dict_items[id][1];
    				String sj = dict_items[id][0];
    				if (gb != null && 
        				sj != null && 
        				gb.length() == 1 && 
        				sj.length() == 1) {
    					if (!dataContext.gb2sj.containsKey(gb.charAt(0))) {
    						dataContext.gb2sj.put(gb.charAt(0), sj.charAt(0));
    					}
    				} else if ((gb != null && gb.length() == 0 && sj != null && sj.length() > 0) ||
    					(gb == null && sj != null && sj.length() > 0)) {
    					cacheGB2SJ.add(sj);
        			} else {
    	            	if (D)
    	            		Log.e(TAG, "wrong word 1 on :" + (id - dict_size[0]) + ", " + 
    	            				gb + ", " + sj);		
    				}
				}
            	if (D)
            		Log.e(TAG, "loading gb2sj:" + dataContext.gb2sj.size());
			}
    		//PLUGIN
    		//enword.txt
			if (!DataContext.DONNOT_LOAD_ENWORDS && dataContext.enwords != null) {
				dataContext.enwords.clear();
    			for (id = dict_size[0] + dict_size[1]; id < dict_size[0] + dict_size[1] + dict_size[2]; id++) {
    				Word word = new Word(id, dict_items[id]);
    				//if (D)
    				//	Log.e(TAG, word.toString());
    				dataContext.enwords.add(word);
    			}
            	if (D)
            		Log.e(TAG, "loading enwords:" + dataContext.enwords.size());
			}
			
    		//GC
    		dataContext.data.bytes = null;
    		dataContext.data.dict_items = null;
    		dataContext.data.dict_size = null;
    	}
    }
    
    private void loadSeparateDicts() {
    	if (D)
    		Log.e(TAG, "Begin loading files to memory hash map (separate version)");
    	if (!DataContext.DONNOT_LOAD_DICT && 
    		dataContext != null && 
    		dataContext.data0 != null &&
    		dataContext.data0.dict_size != null &&
    		dataContext.data0.dict_size.length >= 1 && 
    		dataContext.data0.dict_items != null &&
    	    dataContext.data1 != null &&
    		dataContext.data1.dict_size != null &&
    		dataContext.data1.dict_size.length >= 1 && 
    		dataContext.data1.dict_items != null && 
    		dataContext.data2 != null && //PLUGIN
    		dataContext.data2.dict_size != null &&
    		dataContext.data2.dict_size.length >= 1 && 
    		dataContext.data2.dict_items != null 
    		) {
    		int id = 0;
    		int[] dict0_size = dataContext.data0.dict_size;
    		String[][] dict0_items = dataContext.data0.dict_items;
    		int[] dict1_size = dataContext.data1.dict_size;
    		String[][] dict1_items = dataContext.data1.dict_items;
    		int[] dict2_size = dataContext.data2.dict_size; //PLUGIN
    		String[][] dict2_items = dataContext.data2.dict_items;
    		
    		//GC
    		dataContext.data0.bytes = null;
    		dataContext.data1.bytes = null;
    		dataContext.data2.bytes = null; //PLUGIN
    		
    		//jpword.txt
    		id = 0;
    		if (!DataContext.DONNOT_LOAD_WORDS && dataContext.words != null) {
				dataContext.words.clear();
    			for (; id < dict0_size[0]; id++) {
    				Word word = new Word(id, dict0_items[id]);
    				//if (D)
    				//	Log.e(TAG, word.toString());
    				dataContext.words.add(word);
    			}
            	if (D)
            		Log.e(TAG, "loading words:" + dataContext.words.size());
			}
    		
    		//GC
    		dataContext.data0.dict_items = null;
    		dataContext.data0.dict_size = null;
    		dict0_items = null;
    		dict0_size = null;
    		
    		//sj2gb.txt
			id = 0;
			if (!DataContext.DONNOT_LOAD_GB2SJ && dataContext.gb2sj != null) {
				dataContext.gb2sj.clear();
				//if (cacheGB2SJ == null) {
					cacheGB2SJ = new ArrayList<String>();
				//}
				for (;id < dict1_size[0]; id++) {
    				String gb = dict1_items[id][1];
    				String sj = dict1_items[id][0];
    				if (gb != null && 
        				sj != null && 
        				gb.length() == 1 && 
        				sj.length() == 1) {
    					if (!dataContext.gb2sj.containsKey(gb.charAt(0))) {
    						dataContext.gb2sj.put(gb.charAt(0), sj.charAt(0));
    					}
    				} else if ((gb != null && gb.length() == 0 && sj != null && sj.length() > 0) ||
    					(gb == null && sj != null && sj.length() > 0)) {
    					cacheGB2SJ.add(sj);
        			} else {
    	            	if (D)
    	            		Log.e(TAG, "wrong word 1 on :" + id + ", " + 
    	            				gb + ", " + sj);		
    				}
				}
            	if (D)
            		Log.e(TAG, "loading gb2sj:" + dataContext.gb2sj.size());
			}
			
			//GC
			dataContext.data1.dict_items = null;
    		dataContext.data1.dict_size = null;
    		dict1_items = null;
    		dict1_size = null;
    		
			//PLUGIN
			//enword.txt
			id = 0;
			if (!DataContext.DONNOT_LOAD_ENWORDS && dataContext.enwords != null) {
				dataContext.enwords.clear();
    			for (; id < dict2_size[0]; id++) {
    				Word word = new Word(id, dict2_items[id]);
    				//if (D)
    				//	Log.e(TAG, word.toString());
    				dataContext.enwords.add(word);
    			}
            	if (D)
            		Log.e(TAG, "loading enwords:" + dataContext.enwords.size());
			}
			
    		//GC
    		dataContext.data2.dict_items = null;
    		dataContext.data2.dict_size = null;
    		dict2_items = null;
    		dict2_size = null;
    	}
    }
    
    public void openWebViewActivity(String name) {
		startActivity(new Intent(this, WebViewActivity.class)
			.putExtra(EXTRA_KEY, name));
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		//checkSearchMini();
		if (data != null) {
			String resultString = data.getStringExtra(JkanjiHandActivity.EXTRA_KEY_RESULT_STRING);
			String historyString = data.getStringExtra(JKanjiActivity.EXTRA_KEY_SHEARCHTEXT);
			switch (requestCode) {
			case REQUEST_HANDINPUT:
				if (resultString != null) {
					inputText.setText("");
					inputText.append(resultString);
				}
				//TODO: search at once
				if (!this.isSearchMini) {
					toggleJapIME(false);
					if (inputText != null) {
						inputText.postDelayed(new Runnable() {
							@Override
							public void run() {
								if (!FAST_SAVE) {
									setLastSearchInput(inputText.getText().toString());
								}
								queryDict(inputText.getText().toString(), false);
							}
						}, 500);
					}
				}
				break;
				
			case REQUEST_HISTORY:
				if (historyString != null) {
					inputText.setText("");
					inputText.append(historyString);					
				}
				//TODO: search at once
				if (!this.isSearchMini) {
					toggleJapIME(false);
					if (inputText != null) {
						setLastSearchInput(inputText.getText().toString());
						queryDict(inputText.getText().toString(), false);
					}
				}
				break;
			}
		}
		
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
        	ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
        	voiceAdapter.clear();
        	for (String str : matches) {
            	voiceAdapter.add(str);
            }
        	voiceAdapter.notifyDataSetChanged();
        	showDialog(DIALOG_LIST);
        }
	}
	
	private void miniSearchStart() {
		if (dataContext.resultWords != null) {
			//toggleJapIME(false);
			if (inputText != null) {
				if (!FAST_SAVE) {
					setLastSearchInput(inputText.getText().toString());
				}
				queryDict(inputText.getText().toString(), true);
			}
		}
	}
	
	private void checkSearchMini() {
		this.isSearchMini = JkanjiSettingActivity.getSearchMini(this);
		if (isSearchMini) {
			frameLayoutMainOutput.setVisibility(FrameLayout.GONE);
		} else {
			frameLayoutMainOutput.setVisibility(FrameLayout.VISIBLE);
		}
	}
	
	public void startForgroundService() {
		if (JkanjiSettingActivity.getDictService(this)) {
			this.startService(
					new Intent(this, JkanjiDictService.class)
						.setAction(JkanjiDictService.ACTION_FOREGROUND));
		}
	}
	
	private int getKeyVisibility() {
		if (this.isUseKeyPager) {
			return panelswitch.getVisibility();
		} else {
			return gridView.getVisibility();
		}
	}
	
	private void setKeyVisibility(int value) {
		if (this.isUseKeyPager) {
			panelswitch.setVisibility(value);
			if (value == View.VISIBLE) {
				textViewInfo.setText("小提示:下面的键盘可左右滑动");
			}
		} else {
			gridView.setVisibility(value);
		}
		if (value == View.VISIBLE) {
			buttonEdit.setText("变换");
			buttonKeyDel.setVisibility(View.VISIBLE);
			checkBoxEnableCache.setVisibility(View.GONE);
		} else {
			buttonEdit.setText("列表");
			buttonKeyDel.setVisibility(View.GONE);
			checkBoxEnableCache.setVisibility(View.VISIBLE);
		}
	}
	
    private String nextKanji(String str, int sepIndex) {
    	ArrayList<String> cacheGB2SJ = JKanjiActivity.getCacheGB2SJ();
    	if (str == null || str.length() <= 0 || 
			sepIndex < 0 || sepIndex >= str.length() ||
			cacheGB2SJ == null) {
			return null;
		}
		Character right = str.charAt(sepIndex);
		for (String entry : cacheGB2SJ) {
			if (entry.indexOf(right) >= 0 && entry.length() > 1) {
				int index = entry.indexOf(right) + 1;
				if (index > entry.length() - 1) {
					index = 0;
				}
				return Character.toString(entry.charAt(index));
			}
		}
		return null;
    }
	
    private final class PageAdapter extends PagerAdapter {
        private View[] mPages;
        private int pageNum;
        private int mRow = 6;
        private int mCol = 5;

        public PageAdapter(ViewPager parent, String[] kbs) {
        	Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            int pageGridNum = (mRow * mCol);
            if (kbs == null) {
            	pageNum = 0;
            } else {
            	pageNum = (int)(Math.floor((double)kbs.length / pageGridNum) + 1);
            	if ((pageNum - 1) * pageGridNum == kbs.length) {
            		pageNum = pageNum - 1;
            	}
            }
            mPages = new View[pageNum];
            for (int i = 0; i < pageNum; i++) {
            	mPages[i] = inflater.inflate(R.layout.key_panel, parent, false);
            	final GridView gridViewKeys = (GridView) mPages[i].findViewById(R.id.gridViewKeys);
                final String[] kbnames = new String[pageGridNum];
                for (int j = 0; j < pageGridNum; j++) {
                	if (i * pageGridNum + j < kbs.length) {
                		kbnames[j] = kbs[i * pageGridNum + j];
                	} else {
                		kbnames[j] = "";
                	}
                }
            	final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                        R.layout.key_panel_grid, kbnames);
            	gridViewKeys.setAdapter(adapter);
            	gridViewKeys.setOnItemClickListener(new OnItemClickListener() {
        			@Override
        			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        				if (inputText != null) {
        					//TODO: special soft keyboard!
        					if (kbnames[position].equals("°")) {
        						String str = inputText.getEditableText().toString();
        						String right = JapaneseKeyboard.nextChar(str, inputText.getSelectionStart() - 1);
        						if (right != null) {
        							inputText.getEditableText().replace(inputText.getSelectionStart() - 1,
        									inputText.getSelectionEnd(), right);
        						}
        					} else {
        						inputText.getEditableText().replace(inputText.getSelectionStart(),
        							inputText.getSelectionEnd(), kbnames[position]);
        					}
        				}
        			}
                });
            }
        }

        @Override
        public int getCount() {
            return pageNum;
        }

        @Override
        public void startUpdate(View container) {
        }

        @Override
        public Object instantiateItem(View container, int position) {
            final View page = mPages[position];
            ((ViewGroup) container).addView(page);
            return page;
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewGroup) container).removeView((View) object);
        }

        @Override
        public void finishUpdate(View container) {
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }
    }
    
    
	private final static class LoadDataTask extends AsyncTask<Void, Void, Boolean> {
		private boolean loadResult = false;
		private WeakReference<JKanjiActivity> act;
		private Toast toast;
		private String defaultOutputText;
		
		public LoadDataTask(JKanjiActivity activity) {
			act = new WeakReference<JKanjiActivity>(activity);
			toast = Toast.makeText(act.get(), "加载出错或内存不足，请改用会话模式", Toast.LENGTH_SHORT);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (SHOW_HIS_OUTPUT) {
				defaultOutputText = act.get().getLastSearchOutput();
			}
			act.get().textViewProgressInfo.setText("(2/2)缓存中...");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				act.get().loadSeparateDicts();
				loadResult = true;				
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true && !act.get().isFinishing()) {
				if (loadResult) {
					act.get().endLoadSeparateDicts();
					if (SHOW_HIS_OUTPUT) {
						cacheDefaultOutputText = defaultOutputText;
						act.get().endLoadOutputText(defaultOutputText);
					}
				} else {
					
				}
			} else if (result == false) {
				toast.show();
				act.get().finish();
			}
		}
    }
	
	private void endLoadOutputText(String defaultOutputText) {
		if (SHOW_HIS_OUTPUT) {
			if (defaultOutputText != null) {
	        	if (inputText != null && inputText.getText() != null) {
	        		setOutputText(defaultOutputText, inputText.getText().toString());
	        	} else {
	        		setOutputText(defaultOutputText, null);
	        	}
	        }
		}
	}
	
	private final static class QueryTask extends AsyncTask<Void, Void, Boolean> {
		private boolean loadResult = false;
		private WeakReference<JKanjiActivity> act;
		private String mStr;
		private QueryInfo mInfo;
		private boolean mIsStartAct;
		
		public QueryTask(JKanjiActivity activity, String str, boolean isStartAct) {
			act = new WeakReference<JKanjiActivity>(activity);
			mStr = str;
			mIsStartAct = isStartAct;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mInfo = act.get().queryDictPre(mStr);
			act.get().textViewInfo.setText("搜索中...");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				act.get().queryDictProgress(mInfo);
				loadResult = true;				
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			act.get().queryTask = null;
			if (result == true && !act.get().isFinishing()) {
				if (loadResult) {
					act.get().queryDictPost(mInfo);
					if (mIsStartAct) {
						act.get().queryDictStartAct();
					}
				} else {
					
				}
			} else if (result == false) {
				act.get().finish();
			}
		}
    }
}
