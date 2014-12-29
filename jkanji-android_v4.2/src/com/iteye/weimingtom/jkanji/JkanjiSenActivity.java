package com.iteye.weimingtom.jkanji;

import java.io.File;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import net.java.sen.StringTagger;
import net.java.sen.Token;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @see G:\jap\sen\sen
 * @author Administrator
 * 
 */
public class JkanjiSenActivity extends Activity {
	/**
	 * True if use content provider in remote process.
	 * False if use static variable in local process.
	 */
	private static final boolean USE_CONTENT_PROVIDER = true;
	
	public final static String INPUT_KEY = "com.iteye.weimingtom.jkanji.JkanjiSenActivity.input";
	
	private static final int DIALOG_WARNING_ID = 0;
	private static final int DIALOG_LOADING_ID = 1;
	
	// NOTE: it is only available when USE_CONTENT_PROVIDER == false,
	// but it must equals the value in content provider !
	private static boolean isFirstStarted = true; 
	private final static String SEN_HOME = "/sen"; 
	private final static String[] FILENAMES = {
		"/sen/conf/sen.xml",
		"/sen/conf/sen-processor.xml",
		"/sen/dic/da.sen",
		"/sen/dic/matrix.sen",
		"/sen/dic/posInfo.sen",
		"/sen/dic/token.sen",
	};
	
	private final static String TEST_TEXT = "なにか日本語を入力して試そう。" +
		"Sen は Java で書かれた日本語形態素解析システム。" +
		"C++ で開発されている MeCab を Java に移植したもの。" +
		"辞書は MeCab、茶筌と同じIPAの辞書を利用。" + 
		"Sen配布ページ" + 
		"辞書の構築に Apache Ant が必要。" +
		"このページは Sen version 1.2.2.1 用である。";
	private final static String TEST_TEXT2 = "なにか日本語を入力して試そう。";
	
	private Button buttonAnalyze;
	private Button buttonDetail;
	private EditText inputEditText;
	private TextView outputEditText;
	private LinearLayout linearLayoutInput;
	private ScrollView scrollViewOutput;
	private ScrollView scrollViewSen;
	private Button buttonSearch;
	private Button buttonTranslate;
	private ActionBar actionBar;
	private FrameLayout frameLayout1;
	private TextView textViewLoading;
	private SenAutoWrapViewGroup senViewGroup;
	
	private AlertDialog.Builder builder;

	private int parseSingleType = 0;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sen_input);
		//this.setTitle("sen日语发音标注");
		
		buttonAnalyze = (Button) this.findViewById(R.id.buttonAnalyze);
		inputEditText = (EditText) this.findViewById(R.id.inputEditText);
		outputEditText = (TextView) this.findViewById(R.id.outputEditText);
		buttonSearch = (Button) this.findViewById(R.id.buttonSearch);
		buttonTranslate = (Button) this.findViewById(R.id.buttonTranslate);
		buttonDetail = (Button) this.findViewById(R.id.buttonDetail);
		linearLayoutInput = (LinearLayout) this.findViewById(R.id.linearLayoutInput);
		scrollViewOutput = (ScrollView) this.findViewById(R.id.scrollViewOutput);
		frameLayout1 = (FrameLayout) this.findViewById(R.id.frameLayout1);
		textViewLoading = (TextView) this.findViewById(R.id.textViewLoading);
		scrollViewSen = (ScrollView) this.findViewById(R.id.scrollViewSen);
		senViewGroup = (SenAutoWrapViewGroup) this.findViewById(R.id.senViewGroup);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("sen标注");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
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
				return R.drawable.delete_normal;
			}

			@Override
			public void performAction(View view) {
				linearLayoutInput.setVisibility(LinearLayout.VISIBLE);
				scrollViewSen.setVisibility(View.INVISIBLE);
				scrollViewOutput.setVisibility(ScrollView.INVISIBLE);
				inputEditText.setText("");
			}
        });
        /*
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.search;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(JkanjiSenActivity.this, JKanjiActivity.class)
					.putExtra(JKanjiActivity.EXTRA_KEY_SHEARCHTEXT, inputEditText.getText().toString())
				);
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
				if (linearLayoutInput.getVisibility() == LinearLayout.VISIBLE) {
					linearLayoutInput.setVisibility(LinearLayout.INVISIBLE);
					scrollViewSen.setVisibility(View.INVISIBLE);
					scrollViewOutput.setVisibility(ScrollView.VISIBLE);
				} else {
					linearLayoutInput.setVisibility(LinearLayout.VISIBLE);
					scrollViewSen.setVisibility(View.INVISIBLE);
					scrollViewOutput.setVisibility(ScrollView.INVISIBLE);
				}
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.shareto;
			}

			@Override
			public void performAction(View view) {
				//inputEditText.setText(null);
				StringBuffer sb = new StringBuffer();
				sb.append(inputEditText.getText().toString());
				sb.append("\n");
				sb.append("\n");
				sb.append(outputEditText.getText().toString());
				sb.append("\n");
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                //startActivity(Intent.createChooser(intent, "共享方式"));
                try {
                	startActivity(intent);
                } catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiSenActivity.this, 
						"共享方式出错", Toast.LENGTH_SHORT)
						.show();
                }
			}
        });
        
		buttonAnalyze.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				parseSingleType = 0;
				onAnalyze();
			}
		});
		buttonDetail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				parseSingleType = 1;
				onAnalyze();
			}
		});
		buttonTranslate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(JkanjiSenActivity.this, DictWebListActivity.class)
					.putExtra(DictWebListActivity.EXTRA_KEY, inputEditText.getText().toString())
					.putExtra(DictWebListActivity.EXTRA_KEY_SHARE, inputEditText.getText().toString())
				);
			}
		});
		buttonSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (scrollViewSen.getVisibility() == View.INVISIBLE) {
					linearLayoutInput.setVisibility(LinearLayout.INVISIBLE);
					scrollViewSen.setVisibility(View.VISIBLE);
					scrollViewOutput.setVisibility(ScrollView.INVISIBLE);
					senViewGroup.outputMultiLine(JkanjiSenActivity.this, inputEditText.getText().toString());
				} else {
					linearLayoutInput.setVisibility(LinearLayout.VISIBLE);
					scrollViewSen.setVisibility(View.INVISIBLE);
					scrollViewOutput.setVisibility(ScrollView.INVISIBLE);					
				}
			}
		});
		
		System.setProperty("sen.home", JkanjiSettingActivity.getDataPackPath(this) + SEN_HOME);

        Intent intent = this.getIntent();
        if (intent != null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEND)) {
        	String searchText = intent.getStringExtra(Intent.EXTRA_TEXT);
        	if (searchText != null) {
        		this.inputEditText.setText("");
        		this.inputEditText.append(searchText);
        	}
        } else {
    		String input = this.getIntent().getStringExtra(INPUT_KEY);
    		if (input != null) {
    			this.inputEditText.setText(input);
    			//onAnalyze();
    		}	        	
        }
		
		builder = new AlertDialog.Builder(this);
		if (!this.checkFilesExist(FILENAMES)) {
			this.showDialog(DIALOG_WARNING_ID);
		} else {
			if (!USE_CONTENT_PROVIDER) {
				if (isFirstStarted) {
					this.showDialog(DIALOG_LOADING_ID);
				} else {
					this.onAnalyze();
				}
			} else {
				if (JkanjiSettingActivity.getShowSenDlg(this)) {
					this.showDialog(DIALOG_LOADING_ID);
				} else {
					this.onAnalyze();
				}
			}
		}
	}
	
	private String analyze(String s) {
		StringBuffer sb = new StringBuffer();
		try {
			StringTagger tagger = StringTagger.getInstance();
			Token[] token = tagger.analyze(s);
			if (token != null) {
				for (int i = 0; i < token.length; i++) {
					String name = token[i].toString();
					String reading = token[i].getReading();
					String reading2 = null;
					if (reading != null) {
						reading2 = CharTrans.zenkakuHiraganaToZenkakuKatakana(reading);
					} else {
						reading2 = "";
					}
					sb.append(name);
					if (reading != null && 
						reading.length() > 0 && 
						!reading.equals(name) &&
						!reading2.equals(name)) {
						sb.append("(");
						sb.append(reading2);
						sb.append(")");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			sb.append("Error: data files in " + 
				JkanjiSettingActivity.getDataPackPath(this) + SEN_HOME +
				" does not exist, or something goes wrong.");
		}
		return sb.toString();
	}
	
	private boolean analyzeTest() {
		StringTagger tagger;
		boolean result = true;
		try {
			tagger = StringTagger.getInstance();
			tagger.analyze(TEST_TEXT2);
		} catch (Throwable e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	private void onAnalyze() {
		if (!USE_CONTENT_PROVIDER) {
			isFirstStarted = false;
			String input = inputEditText.getText().toString();
			if (input != null && input.length() > 0) {
				String output = analyze(input);
				outputEditText.setText(output);
			} else {
				boolean result = analyzeTest(); // initialize on first time.
				if (result == false) {
					outputEditText.setText("Error: data files in " + 
							JkanjiSettingActivity.getDataPackPath(this) + SEN_HOME +
							" does not exist, or something goes wrong.");
				}
			}
		} else {
			new LoadDataTask().execute();
		}
	}
	
    @Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

    /*
	@Override
	public void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
		//this.onAnalyze();
	}
	*/
	
    /*
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		this.onAnalyze();
	}
	*/
	
	private boolean checkFilesExist(String[] filenames) {
		boolean result = true;
		for (int i = 0; i < filenames.length; i++) {
			File file = new File(JkanjiSettingActivity.getDataPackPath(this) + filenames[i]);
			if (!file.canRead() || !file.exists()) {
				result = false;
				//System.out.println("==================" + filenames[i]);
				break;
			}
		}
		return result;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
	    Dialog dialog = null;
	    switch(id) {
	    case DIALOG_WARNING_ID:
	    	if (builder != null) {
	    		return builder
	    			.setTitle("警告")
	    			.setMessage("\nsen需要的一些文件貌似不存在，请在/sdcard/jkanji/sen目录下安装数据包。")
	    			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog, int id) {
	    					
	    				}
	    			})
	    	       .create();
	    	}
	        break;
	        
	    case DIALOG_LOADING_ID:
	    	if (builder != null) {
	    		return builder
	    			.setTitle("sen初始化")
	    			.setMessage("\nsen将进行初始化，第一次初始化可能需要1至2分钟，是否开始？")
	    			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
	    				public void onClick(DialogInterface dialog, int id) {
	    					//isFirstStarted = false;
	    					onAnalyze();
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
	}
    
	private class LoadDataTask extends AsyncTask<Void, Void, Boolean> {
		private boolean loadResult = false;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			frameLayout1.setVisibility(View.INVISIBLE);
			textViewLoading.setVisibility(View.VISIBLE);
			buttonAnalyze.setEnabled(false);
			buttonDetail.setEnabled(false);
			buttonSearch.setEnabled(false);
			buttonTranslate.setEnabled(false);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				String result = getSenAnalyseSingle(JkanjiSenActivity.this, TEST_TEXT2, parseSingleType);
				if (result != null && result.length() > 0) {
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
			frameLayout1.setVisibility(View.VISIBLE);
			textViewLoading.setVisibility(View.INVISIBLE);
			buttonAnalyze.setEnabled(true);
			buttonDetail.setEnabled(true);
			buttonSearch.setEnabled(true);
			buttonTranslate.setEnabled(true);
			if (result == true && !JkanjiSenActivity.this.isFinishing()) {
				String input = inputEditText.getText().toString();
				if (loadResult) {
					startSenForgroundService();
					if (input != null && input.length() > 0) {
						//String output = analyze(input);
						//outputEditText.setText(output);
						String output = getSenAnalyseSingle(JkanjiSenActivity.this, input, parseSingleType);
						outputEditText.setText(output);
						linearLayoutInput.setVisibility(LinearLayout.INVISIBLE);
						scrollViewOutput.setVisibility(ScrollView.VISIBLE);
						scrollViewSen.setVisibility(ScrollView.INVISIBLE);
					} else {
						linearLayoutInput.setVisibility(LinearLayout.VISIBLE);
						scrollViewOutput.setVisibility(ScrollView.INVISIBLE);
						scrollViewSen.setVisibility(ScrollView.INVISIBLE);
					}
				} else {
//					Toast.makeText(JkanjiSenActivity.this, 
//						"sen初始化失败", Toast.LENGTH_SHORT).show();
					linearLayoutInput.setVisibility(LinearLayout.INVISIBLE);
					scrollViewSen.setVisibility(View.INVISIBLE);
					scrollViewOutput.setVisibility(ScrollView.VISIBLE);
					outputEditText.setText("Error: data files in " + 
							JkanjiSettingActivity.getDataPackPath(JkanjiSenActivity.this) + SEN_HOME +
							" does not exist, or something goes wrong.");
				}
			} else if (result == false) {
				finish();
			}
		}
    }
	
    public static String getSenAnalyseSingle(Activity activity, String query, int startpos) {
		String result = "";
    	try {
	    	Cursor cursor = activity.managedQuery(SenProvider.CONTENT_URI_PARSERSINGLE, null, null,
                    new String[] {query, Integer.toString(startpos)}, null);
			if (cursor != null) {
	        	while (cursor.moveToNext()) {
	        		String name = cursor.getString(0);
	            	String reading = cursor.getString(1);
	            	int startPos = cursor.getInt(2);
	            	result = reading;
	        	}
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return result;
    }
    
	public void startSenForgroundService() {
		if (JkanjiSettingActivity.getSenService(this)) {
			startService(
					new Intent(this, JkanjiSenService.class)
						.setAction(JkanjiSenService.ACTION_FOREGROUND));
		}
	}
}
