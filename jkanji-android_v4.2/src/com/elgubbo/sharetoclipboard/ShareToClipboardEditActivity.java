package com.elgubbo.sharetoclipboard;

import java.util.Locale;

import com.elgubbo.sharetoclipboard.db.ShareDataSource;
import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.jkanji.WordEditActivity;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ShareToClipboardEditActivity extends Activity {
	/**
	 *  insert: id == -1L 
	 *  update: id != -1L
	 */
	public final static String EXTRA_ID = "id"; 
	public final static String EXTRA_DESCRIPTION = "description";
	public final static String EXTRA_CONTENT = "content";
	
	private ActionBar actionBar;
	private EditText editTextDescription;
	private EditText editTextContent;
	private Button buttonCommit;
	private Button buttonCancel;
	private Button buttonSound1;
	private Button buttonSound2;
	
	private long currId = -1L;
	private String currDescription;
	private String currContent;
	
	private TextToSpeech mTts;
	private boolean enableTTS = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.memo_edit);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("备忘录编辑");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.memo;
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
				return R.drawable.ok;
			}

			@Override
			public void performAction(View view) {
				addMemo();
			}
        });
        
        editTextDescription = (EditText) this.findViewById(R.id.editTextDescription);
        editTextContent = (EditText) this.findViewById(R.id.editTextContent);
        buttonCommit = (Button) this.findViewById(R.id.buttonCommit);
        buttonCancel = (Button) this.findViewById(R.id.buttonCancel);
        buttonSound1 = (Button) this.findViewById(R.id.buttonSound1);
        buttonSound2 = (Button) this.findViewById(R.id.buttonSound2);
        
        buttonCommit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addMemo();
			}
        });
        
        buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
        });
        
        buttonSound1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sound(0);
			}
        });
        buttonSound2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sound(1);
			}
        });
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
        
        Intent intent = this.getIntent();
        if (intent != null) {
        	currId = intent.getLongExtra(EXTRA_ID, -1L);
        	currDescription = intent.getStringExtra(EXTRA_DESCRIPTION);
        	editTextDescription.setText(currDescription);
        	currContent = intent.getStringExtra(EXTRA_CONTENT);
        	editTextContent.setText(currContent);
        }
	}
	
	private void sound(int type) {
		if (enableTTS) {
			String str;
			if (type == 0) {
				str = editTextDescription.getText().toString();
			} else {
				str = editTextContent.getText().toString();
			}
			if (str != null && str.length() > 0) {
				mTts.speak(str, TextToSpeech.QUEUE_FLUSH, null);
			} else {
				if (type == 0) {
					Toast.makeText(this, "描述为空", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(this, "内容为空", Toast.LENGTH_SHORT).show();
				}
			}
		} else {
			Toast.makeText(this, "请检查是否支持日文发音（退出此界面后再试）", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void addMemo() {
		String desctription = this.editTextDescription.getText().toString();
		if (desctription == null) {
			desctription = "";
		}
		String content = this.editTextContent.getText().toString();
		if (content == null) {
			content = "";
		}
		ShareDataSource dataSrc = new ShareDataSource(this);
		dataSrc.open();
		boolean res = dataSrc.createContent(content, desctription, "text", currId);
		if (res) { 
			Toast.makeText(this, "保存到备忘录成功", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "保存到备忘录失败", Toast.LENGTH_SHORT).show();	
		}
		dataSrc.close();
		finish();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
            mTts = null;
        }
	}
}
