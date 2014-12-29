package com.iteye.weimingtom.jkanji;

import java.util.Locale;

import com.elgubbo.sharetoclipboard.ShareToClipboardActivity;
import com.elgubbo.sharetoclipboard.db.ShareDataSource;
import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioTrack;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WordEditActivity extends Activity {
	public final static String EXTRA_DATA = "data";

	private ActionBar actionBar;
	private EditText catalog;
	private EditText reading;
	private EditText kanji;
	private EditText mean;
	private EditText etc;
	private Button share;
	private Button phaseSound;
	private Button buttonMemo;
	//private Button save;
	
	private AudioTrack audioTrack;
	private String zipFilename = "/tts/tts-20120809.zip";
	private static final boolean USE_STREAM = false;
	
	private Word word;

	/**
	 * @see https://github.com/manijshrestha/Android-Text-To-Speech/blob/master/src/com/manijshrestha/texttospeech/TextToSpeechActivity.java
	 */
	private TextToSpeech mTts;
	private boolean enableTTS = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.word_editer);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("搜索结果编辑");
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
				return R.drawable.shareto;
			}

			@Override
			public void performAction(View view) {
				shareWord();
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.search;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(WordEditActivity.this, 
						JKanjiActivity.class));
			}
        });
        
		catalog = (EditText) this.findViewById(R.id.catalog);
		reading = (EditText) this.findViewById(R.id.reading);
		kanji = (EditText) this.findViewById(R.id.kanji);
		mean = (EditText) this.findViewById(R.id.mean);
		etc = (EditText) this.findViewById(R.id.etc);
		share = (Button) this.findViewById(R.id.share);
		buttonMemo = (Button) this.findViewById(R.id.buttonMemo);
		//save = (Button) this.findViewById(R.id.save);
		phaseSound = (Button) this.findViewById(R.id.phaseSound);
		if (JkanjiSettingActivity.getUseTTS(WordEditActivity.this)) {
			phaseSound.setEnabled(true);
		} else {
			phaseSound.setEnabled(false);
		}
		phaseSound.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (JkanjiSettingActivity.getUseTTS(WordEditActivity.this)) {
					if (enableTTS) {
						if (word != null) {
							if (word.kanji != null && word.kanji.length() > 0) {
								mTts.speak(word.kanji, TextToSpeech.QUEUE_FLUSH, null);
							} else if (word.reading != null && word.reading.length() > 0){
								mTts.speak(word.reading, TextToSpeech.QUEUE_FLUSH, null);
							}
						} else {
							Toast.makeText(WordEditActivity.this, "单词为空", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(WordEditActivity.this, "请检查是否支持日文发音（退出此界面后再试）", Toast.LENGTH_SHORT).show();
					}
				} else {
					if (word != null && word.id >= 0) {
						String fileinfo;
						fileinfo = JkanjiSettingActivity.getDataPackPath(WordEditActivity.this) + zipFilename + ", " + "tts" + "/" + word.id + ".wav";
						try {
							audioTrack = AudioTrackUtils.playWav(audioTrack, 
									JkanjiSettingActivity.getDataPackPath(WordEditActivity.this) + zipFilename,
									"tts/" + word.id + ".wav", USE_STREAM);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (audioTrack == null) {
							Toast.makeText(WordEditActivity.this, 
								"播放" + fileinfo + "失败", 
								Toast.LENGTH_SHORT).show();
						} else {
							
						}
					}
				}
			}
		});
		share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				shareWord();
			}
		});
		buttonMemo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addMemo();
			}
		});
		
		Intent intent = this.getIntent();
		if (intent != null) {
			this.word = intent.getParcelableExtra(WordEditActivity.EXTRA_DATA);
			if (this.word != null) {
				catalog.setText(word.catalog);
				reading.setText(word.reading);
				kanji.setText(word.kanji);
				mean.setText(word.mean);
				etc.setText(word.etc);
				if (word.id >= 0) {
					phaseSound.setEnabled(true);
				}
			}
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
	
	private void shareWord() {
		if (word != null) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("text/plain");
			String subject = "";
			if (word.kanji != null) {
				subject = word.kanji;
			} else {
				if (word.reading != null) {
					subject = word.reading;
				} else {
					subject = "（无汉字或发音）";
				}
			}
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);  
            intent.putExtra(Intent.EXTRA_TEXT, word.toShareString());
            //startActivity(Intent.createChooser(intent, "共享方式"));
            try {
            	startActivity(intent);
            } catch (Throwable e) {
				e.printStackTrace();
				Toast.makeText(WordEditActivity.this, 
					"共享方式出错", Toast.LENGTH_SHORT)
					.show();
            }
		} else {
			Toast.makeText(this, 
				"没有要共享的内容", 
				Toast.LENGTH_SHORT)
				.show();
		}
	}
	
	private void addMemoAct() {
		Word sendWord = null;
		if (word == null) {
			String strCatalog = catalog.getText().toString();
			String strReading = reading.getText().toString();
			String strKanji = kanji.getText().toString();
			String strMean = mean.getText().toString();
			if (strCatalog != null && strCatalog.length() == 0) {
				strCatalog = null;
			}
			if (strReading != null && strReading.length() == 0) {
				strReading = null;
			}
			if (strKanji != null && strKanji.length() == 0) {
				strKanji = null;
			}
			if (strMean != null && strMean.length() == 0) {
				strMean = null;
			}
			sendWord = new Word(-1, new String[]{strCatalog, strReading, strKanji, strMean, null});
		} else {
			sendWord = word;
		}
		String searchString = "";
		if (sendWord.kanji != null) {
			searchString = sendWord.kanji;
		} else {
			if (sendWord.reading != null) {
				searchString = sendWord.reading;
			} else {
				searchString = "（无汉字或发音）";
			}
		}
		String shareString = sendWord.toShareString();
		Intent intent = new Intent();
		intent.setClass(WordEditActivity.this, ShareToClipboardActivity.class);
		intent.setAction(ShareToClipboardActivity.ACTION_SEND_CLIP);
		intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, searchString);
        if (shareString != null) {
        	intent.putExtra(Intent.EXTRA_TEXT, shareString);
        } else {
        	intent.putExtra(Intent.EXTRA_TEXT, searchString);
        }
        startActivity(intent);
	}
	
	private void addMemo() {
		Word sendWord = null;
		if (word == null) {
			String strCatalog = catalog.getText().toString();
			String strReading = reading.getText().toString();
			String strKanji = kanji.getText().toString();
			String strMean = mean.getText().toString();
			if (strCatalog != null && strCatalog.length() == 0) {
				strCatalog = null;
			}
			if (strReading != null && strReading.length() == 0) {
				strReading = null;
			}
			if (strKanji != null && strKanji.length() == 0) {
				strKanji = null;
			}
			if (strMean != null && strMean.length() == 0) {
				strMean = null;
			}
			sendWord = new Word(-1, new String[]{strCatalog, strReading, strKanji, strMean, null});
		} else {
			sendWord = word;
		}
		String searchString = "";
		if (sendWord.kanji != null) {
			searchString = sendWord.kanji;
		} else {
			if (sendWord.reading != null) {
				searchString = sendWord.reading;
			} else {
				searchString = "（无汉字或发音）";
			}
		}
		String shareString = sendWord.toShareString();
		String desctription;
		if (searchString != null) {
			desctription = searchString;
		} else {
			desctription = "";
		}
		String content;
		if (shareString != null) {
			content = shareString;
		} else if (searchString != null) {
			content = searchString;
		} else {
			content = "";
		}
		ShareDataSource dataSrc = new ShareDataSource(this);
		dataSrc.open();
		boolean res = dataSrc.createContent(content, desctription, "text", -1L);
		if (res) { 
			Toast.makeText(this, "添加到备忘录成功", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "添加到备忘录失败", Toast.LENGTH_SHORT).show();	
		}
		dataSrc.close();
	}
	
    @Override
	protected void onPause() {
		super.onPause();
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
	protected void onDestroy() {
		super.onDestroy();
		if (audioTrack != null) {
			audioTrack.release();
		}
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
            mTts = null;
        }
	}
}
