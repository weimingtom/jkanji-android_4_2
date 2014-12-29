package com.iteye.weimingtom.jkanji;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class JkanjiListReaderActivity extends Activity implements OnItemClickListener, Runnable{
	private static final boolean D = false;
	private static final String TAG = "JkanjiListReaderActivity";
	
	private static final int DELAY = 2;
	private static final int LOAD_LINE_PER_TIME = 100;
	private static final boolean USE_STREAM = false;
	private static final boolean USE_FILE = false;
	private static final boolean HAVE_NO_FOLDERS = false;
	private static final boolean USE_TOAST = false;
	
	private EditText currentText;
	private RadioButton radioButtonPlay, radioButtonNone, radioButtonSearch;
	private Button buttonStop;
	private TextView infoTextView;
	private ListView viewListView;
	private ActionBar actionBar;
	private LinearLayout linearLayoutHead;
	
	private List<String> items; //text
	private List<String> itemInfos1; //URL folder
	private List<String> itemInfos2; //URL file name
	private BaseAdapter adapter;
	
    private Handler handler;
	
	private String assetFilename = "digust_3.txt"; //"output.txt";
	//private String zipFilename = "/sdcard/jkanji/voice/amivoice_g01_20110618.zip";
	private String zipFilename = "/voice/amivoice.zip";
	private String filePathName = "/voice/amivoice_g01_20110618/aaa/";
	private InputStream inputStream;
	private BufferedReader reader;
	
	private AudioTrack audioTrack;
	
	private Typeface typeface;
	
	private boolean isHeadVisible = true;
	
	private final static class RetainInfo {
		public List<String> items;
		public List<String> itemInfos1;	
		public List<String> itemInfos2;	
	}
	
	private final static class ReaderItemsAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<String> items;
		private Typeface typeface;
		
		public ReaderItemsAdapter(Context context, List<String> items, Typeface typeface) {
			this.mInflater = LayoutInflater.from(context);
			this.items = items;
			this.typeface = typeface;
		}
		
		@Override
		public int getCount() {
			if (items != null) {
				return items.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
           ViewHolder holder;
            if (convertView == null) {
//                convertView = mInflater.inflate(R.layout.list_reader_item, null);
//                holder = new ViewHolder();
//                holder.title = (TextView) convertView.findViewById(R.id.text1);
//                
				convertView = mInflater.inflate(R.layout.list_item, null);
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.title);
//				final TextView text = (TextView) convertView.findViewById(R.id.text);
//				text.setVisibility(TextView.GONE);
				
            	if (this.typeface != null) {
                	holder.title.setTypeface(this.typeface);
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (items != null && position >= 0 && position < items.size()) {
            	holder.title.setText(items.get(position));
            } else {
            	holder.title.setText("");
            }
            return convertView;
		}
		
        private static final class ViewHolder {
        	TextView title;
        }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.list_reader);
		
		currentText = (EditText) this.findViewById(R.id.currentText);
		radioButtonPlay = (RadioButton) this.findViewById(R.id.radioButtonPlay);
		radioButtonNone = (RadioButton) this.findViewById(R.id.radioButtonNone);
		radioButtonSearch = (RadioButton) this.findViewById(R.id.radioButtonSearch);	
		buttonStop = (Button) this.findViewById(R.id.buttonStop);
		infoTextView = (TextView) this.findViewById(R.id.infoTextView);
		viewListView = (ListView) this.findViewById(R.id.viewListView);
		linearLayoutHead = (LinearLayout) this.findViewById(R.id.linearLayoutHead);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("小春音アミ");
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
				return R.drawable.shareto;
			}

			@Override
			public void performAction(View view) {
				boolean isOK = false;
				if (currentText != null) {
					String str = currentText.getText().toString();
					if (str != null && str.length() > 0) {
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
							Toast.makeText(JkanjiListReaderActivity.this, 
								"共享方式出错", Toast.LENGTH_SHORT)
								.show();
		                }
		                isOK = true;
					}
				}
				if (isOK == false) {
					Toast.makeText(JkanjiListReaderActivity.this, 
						"请先选择要共享的内容", 
						Toast.LENGTH_SHORT)
						.show();
				}
			}
        });
        
		handler = new Handler();
		try {
			inputStream = this.getAssets().open(assetFilename);
	        reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		RetainInfo info = (RetainInfo) this.getLastNonConfigurationInstance();
		if (info == null) {
			items = new ArrayList<String>();
			itemInfos1 = new ArrayList<String>();
			itemInfos2 = new ArrayList<String>();
			if (D) {
				Log.d(TAG, "start handler");
			}
			handler.postDelayed(this, DELAY);
		} else {
			items = info.items;
			itemInfos1 = info.itemInfos1;
			itemInfos2 = info.itemInfos2;
		}
		
		//typeface = Typefaces.get(this, "mplus-1m-regular.ttf");
		typeface = Typefaces.getFile(JkanjiSettingActivity.getFontFileName(this));	
		//adapter = new ArrayAdapter<String>(this, R.layout.list_reader_item, items);
		adapter = new ReaderItemsAdapter(this, items, this.typeface);
		viewListView.setAdapter(adapter);
		viewListView.setFastScrollEnabled(true);
		viewListView.setOnItemClickListener(this);
		
		infoTextView.setVisibility(TextView.GONE);
		
		buttonStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (audioTrack != null) {
					audioTrack.release();
					audioTrack = null;
				}
			}
		});
		
		if (savedInstanceState != null) {
			isHeadVisible = savedInstanceState.getBoolean("isHeadVisible", true);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("alreadyStarted", true);
		outState.putBoolean("isHeadVisible", linearLayoutHead.getVisibility() == LinearLayout.VISIBLE);
	}
	
    @Override
	public Object onRetainNonConfigurationInstance() {
    	RetainInfo info = new RetainInfo();
    	info.items = this.items;
    	info.itemInfos1 = this.itemInfos1;
    	info.itemInfos2 = this.itemInfos2;
    	return info;
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
		if (!isHeadVisible) {
			linearLayoutHead.setVisibility(LinearLayout.GONE);
		} else {
			linearLayoutHead.setVisibility(LinearLayout.VISIBLE);
		}
		if (audioTrack != null) {
			audioTrack.setStereoVolume(1.0f, 1.0f);
		}
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (audioTrack != null) {
			audioTrack.release();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (D) {
			Log.d(TAG, "onItemClick " + position);
			Log.d(TAG, "itemInfos1[" + position + "] = " + itemInfos1.get(position));
			Log.d(TAG, "itemInfos2[" + position + "] = " + itemInfos2.get(position));
		}
		if (currentText != null) {
			currentText.setText(items.get(position));
		}
		if (radioButtonPlay.isChecked()) {
			String fileinfo;
			if (USE_FILE) {
				if (HAVE_NO_FOLDERS) {
					fileinfo = JkanjiSettingActivity.getDataPackPath(this) + filePathName + itemInfos2.get(position);
					audioTrack = AudioTrackUtils.playWav(audioTrack, 
							null, 
							JkanjiSettingActivity.getDataPackPath(this) + filePathName + itemInfos2.get(position), USE_STREAM);
				} else {
					fileinfo = JkanjiSettingActivity.getDataPackPath(this) + filePathName + itemInfos1.get(position) + "/" + itemInfos2.get(position);
					audioTrack = AudioTrackUtils.playWav(audioTrack, 
							null, 
							filePathName + itemInfos1.get(position) + "/" + itemInfos2.get(position), USE_STREAM);				
				}
			} else {
				if (HAVE_NO_FOLDERS) {
					fileinfo = JkanjiSettingActivity.getDataPackPath(this) + zipFilename + ", " + itemInfos2.get(position);
					audioTrack = AudioTrackUtils.playWav(audioTrack, 
							JkanjiSettingActivity.getDataPackPath(this) + zipFilename, 
							itemInfos2.get(position), USE_STREAM);
				} else {
					fileinfo = JkanjiSettingActivity.getDataPackPath(this) + zipFilename + ", " + itemInfos1.get(position) + "/" + itemInfos2.get(position);
					audioTrack = AudioTrackUtils.playWav(audioTrack, 
							JkanjiSettingActivity.getDataPackPath(this) + zipFilename, 
							itemInfos1.get(position) + "/" + itemInfos2.get(position), USE_STREAM);
				}
			}
			if (audioTrack == null) {
				if (itemInfos2 != null && fileinfo != null) {
					if (USE_TOAST) {
						Toast.makeText(this, "播放" + fileinfo + "失败", Toast.LENGTH_SHORT).show();
					} else {
						infoTextView.setVisibility(TextView.VISIBLE);
						infoTextView.setText("播放" + fileinfo + "失败");
					}
				} else {
					if (USE_TOAST) {
						Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show();
					} else {
						infoTextView.setVisibility(TextView.VISIBLE);
						infoTextView.setText("播放失败");
					}
				}
			} else {
				infoTextView.setVisibility(TextView.GONE);
			}
		} else if (radioButtonSearch.isChecked()) {
			this.startActivity(new Intent(this, JKanjiActivity.class)
				.putExtra(JKanjiActivity.EXTRA_KEY_SHEARCHTEXT, items.get(position)));
		} else {
			infoTextView.setVisibility(TextView.GONE);
		}
	}
	
	/**
	 * work in UI thread
	 */
	@Override
	public void run() {
		if (D) {
			Log.d(TAG, "run");
		}
		boolean result = loadLinesFromFile();
		if (result && !this.isFinishing()) {
			handler.postDelayed(this, DELAY);
		} else {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
    private boolean loadLinesFromFile(){
        String line;
        try {
        	for (int i = 0; i < LOAD_LINE_PER_TIME; i++) {
				if ((line = reader.readLine()) != null) {
				    String[] strings = TextUtils.split(line, ",");
				    if (strings != null && strings.length == 3) {
				    	items.add(strings[2]);
				    	itemInfos1.add(strings[0]);
				    	itemInfos2.add(strings[1]);
						if (adapter != null) {
							adapter.notifyDataSetChanged();
						}
				    }
				} else {
					return false;
				}
        	}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	    return true;
    }
}
