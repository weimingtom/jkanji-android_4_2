package com.iteye.weimingtom.jkanji;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * @see http://d.hatena.ne.jp/hidecheck/20110420/1303306153
 * @author Administrator
 *
 */
public class MediaPlayerActivity extends Activity implements Runnable {
	private static final boolean D = false;
	private static final String TAG = "MediaPlayerActivity";

	private static final int ACCELERATION_VALUE = 5000;
	private static final int THREAD_RUNNING_INTERVAL = 500;
	private static final boolean IS_STOPPED_ON_PAUSE = false;
	
	private static final int REQUEST_MP3_PATH = 1;
	private static final int REQUEST_SSA_PATH1 = 2;
	private static final int REQUEST_SSA_PATH2 = 3;
	
	private TextView textViewTime;
	private Button buttonPlay;
	private Button buttonRew;
	private Button buttonFF;
	private Button buttonOpenMP3;
	private Button buttonOpenSSA1;
	private Button buttonOpenSSA2;
	private SeekBar seekbar;
	private EditText editTextSubtitle1;
	private ActionBar actionBar;
	
	private boolean isLoopStopped;
	private MediaPlayer player;
	private Handler handler;
	private int lastPosition = 0;
	private String lastAudioFileName;
	private String lastAudioFileName2;
	private String lastSSAFileName1;
	private SubtitleLoader subLoader1;
	private String lastSSAFileName2;
	private SubtitleLoader subLoader2;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.media_player);
        
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("ssa字幕播放器");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.media;
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
				String str = editTextSubtitle1.getText().toString();
				
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, str);
                intent.putExtra(Intent.EXTRA_TEXT, str);
                //startActivity(Intent.createChooser(intent, "共享方式"));
                try {
                	startActivity(intent);
                } catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(MediaPlayerActivity.this, 
						"共享方式出错", Toast.LENGTH_SHORT)
						.show();
                }
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.search;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(MediaPlayerActivity.this, 
						JKanjiActivity.class));
			}
        });
        
        textViewTime = (TextView) findViewById(R.id.textViewTime);
        textViewTime.setText(MSToString(0));
        
		buttonPlay = (Button) findViewById(R.id.button_play);
		buttonPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (player != null) {
					if (player.isPlaying()) {
						buttonPlay.setText("播放");
						player.pause();
					} else {
						buttonPlay.setText("暂停");
						player.start();						
					}
				} else {
					if (lastAudioFileName != null) {
						open(lastAudioFileName);
						buttonPlay.setText("暂停");
						player.start();
					} else {
						Toast.makeText(MediaPlayerActivity.this, 
							"音频文件名为空，请先指定mp3路径", 
							Toast.LENGTH_SHORT).show();
						openFile(".mp3", REQUEST_MP3_PATH);
					}
				}
			}
		});
		buttonRew = (Button) findViewById(R.id.button_rew);
		buttonRew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (player != null) {
					player.seekTo(player.getCurrentPosition() - ACCELERATION_VALUE);
				}
			}
		});
		buttonFF = (Button) findViewById(R.id.button_ff);
		buttonFF.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (player != null) {
					player.seekTo(player.getCurrentPosition() + ACCELERATION_VALUE);
				}
			}
		});
		seekbar = (SeekBar) findViewById(R.id.seekBar1);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser && player != null) {
					try {
						player.seekTo(progress);	
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		editTextSubtitle1 = (EditText) findViewById(R.id.editTextSubtitle1);
		
		buttonOpenMP3 = (Button) findViewById(R.id.buttonOpenMP3);
		buttonOpenMP3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openFile(".mp3", REQUEST_MP3_PATH);
			}
		});
		buttonOpenSSA1 = (Button) findViewById(R.id.buttonOpenSSA1);
		buttonOpenSSA1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openFile(".ssa", REQUEST_SSA_PATH1);
			}
		});
		buttonOpenSSA2 = (Button) findViewById(R.id.buttonOpenSSA2);
		buttonOpenSSA2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openFile(".ssa", REQUEST_SSA_PATH2);
			}
		});
		
		//lastFileName = "/sdcard/testsong.mp3";
		//lastAudioFileName = "/sdcard/jkanji/sub/AIR(-TV-)HDTV_01_baofeng.mp3";
		//lastSSAFileName = "/sdcard/jkanji/sub/[FLsnow][AIR TV][DVDRip][01][XVID+MP3+AC3]_Track5.ssa";
    }

    private void openFile(String suffix, int requestCode) {
    	Intent intent = new Intent(this, DirBrowser.class);
    	intent.putExtra(DirBrowser.EXTRA_KEY_SUFFIX, suffix);    	
    	this.startActivityForResult(intent, requestCode);
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			String resultPath = data.getStringExtra(DirBrowser.EXTRA_KEY_RESULT_PATH);
			if (resultPath != null) {
				switch (requestCode) {
				case REQUEST_MP3_PATH:
					this.buttonPlay.setText("播放");
					lastAudioFileName2 = lastAudioFileName;
					lastAudioFileName = resultPath;
					stopLoop();
					updateCurrentInfo();
					break;
				
				case REQUEST_SSA_PATH1:
					lastSSAFileName1 = resultPath;
					subLoader1 = new SubtitleLoader();
					subLoader1.load(lastSSAFileName1);
					break;
					
				case REQUEST_SSA_PATH2:
					lastSSAFileName2 = resultPath;
					subLoader2 = new SubtitleLoader();
					subLoader2.load(lastSSAFileName2);
					break;
				}
			}
		}
	}

	private void open(String path) {
		try {
			player = new MediaPlayer();
			player.setDataSource(path);
			player.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					stopLoop();
					lastPosition = 0;
					buttonPlay.setText("播放");
				}
			});
			player.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
				@Override
				public void onBufferingUpdate(MediaPlayer mp, int percent) {
					seekbar.setSecondaryProgress(percent);
				}
			});
			player.prepare();
			seekbar.setProgress(lastPosition);
			seekbar.setMax(player.getDuration());
			if(lastAudioFileName == null || 
				lastAudioFileName2 == null) {
				lastPosition = 0;
			} else if (lastAudioFileName != null && 
				lastAudioFileName2 != null && 
				!lastAudioFileName.equals(lastAudioFileName2)) {
				lastPosition = 0;
			}
			player.seekTo(lastPosition);
			startLoop();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(MediaPlayerActivity.this, 
					"音频文件打开失败", 
					Toast.LENGTH_SHORT).show();
		}
	}

	private void startLoop() {
		isLoopStopped = false;
		if (handler == null) {
			handler = new Handler();
			handler.post(this);
		}
	}
	
	private void stopLoop() {
		if (player != null) {
			player.release();
			player = null;
		}
		isLoopStopped = true;
		handler = null;
	}
	
	private void updateCurrentInfo() {
		StringBuffer sbTitle = new StringBuffer();
		if (this.lastAudioFileName != null) {
			sbTitle.append(this.lastAudioFileName);
			sbTitle.append("\n");
		}
		sbTitle.append(MSToString(lastPosition));
		textViewTime.setText(sbTitle.toString());
	}
	
	@Override
	public void run() {
		if (D) {
			Log.d(TAG, "handler seek");
		}
		if (seekbar != null && player != null && player.isPlaying()) {
			lastPosition = player.getCurrentPosition();
			seekbar.setProgress(lastPosition);
			updateCurrentInfo();
			StringBuffer sb = new StringBuffer();
			if (subLoader1 != null) {
				sb.append(subLoader1.getSubtitle(lastPosition));
			}
			if (subLoader2 != null) {
				sb.append(subLoader2.getSubtitle(lastPosition));
			}
			//editTextSubtitle1.setText(sb.toString());
			editTextSubtitle1.setText("");
			editTextSubtitle1.append(sb.toString());
		}
		if (isFinishing() || isLoopStopped) {
			//stop looping
		} else {
			if (handler != null) {
				handler.postDelayed(this, THREAD_RUNNING_INTERVAL);
			}
		}
	}
	
	@Override
	protected void onPause() {
		if (D) {
			Log.d(TAG, "onPause");
		}
		super.onPause();
		if (this.isFinishing()) {
			this.stopLoop();
			buttonPlay.setText("播放");			
		} else {
			if (IS_STOPPED_ON_PAUSE) {
				this.stopLoop();
				buttonPlay.setText("播放");
			}
		}
	}
	
	/*
	@Override
	protected void onStop() {
		if (D) {
			Log.d(TAG, "onStop");
		}
		super.onStop();
		this.stopLoop();
		buttonPlay.setText("播放");
	}
	*/

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.stopLoop();
	}
	
	private static String MSToString(int num) {
		int hour = 0;
		int min = 0;
		int sec = 0;
		int msec = 0;
		msec = num % 1000;
		min = sec = (num - msec) / 1000;
		sec %= 60;
		hour = min = (min - sec) / 60;
		min %= 60;
		hour = (hour - min) / 60;
		return String.format("%d:%02d:%02d.%03d", hour, min, sec, msec);
	}
}
