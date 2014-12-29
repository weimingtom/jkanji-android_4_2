package com.iteye.weimingtom.jkanji;

import java.io.BufferedReader;
import java.io.FileInputStream;
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
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class JkanjiTextListActivity extends Activity implements OnItemClickListener {
	private static final boolean D = false;
	private static final String TAG = "JkanjiTextListActivity";
	
	private static final String SHARE_PREF_NAME = "pref";
	private static final String SHARE_PREF_CALL_SEN = "callSen";
	
	private static final int REQUEST_PATH = 1;
	private static final int LOAD_LINE_PER_TIME = 100;
	
	private ListView viewListView;
	private Button buttonOpen;
	private CheckBox checkBoxSen; 
	private ActionBar actionBar;
	
	private List<String> items; //text
	private List<String> itemInfos1; //translate
	private BaseAdapter adapter;
	
	private String assetFilename = "digust_5.txt";;
	
	private Typeface typeface;
	
	private final static class RetainInfo {
		public List<String> items;
		public List<String> itemInfos1;
	}
	
	private final static class ReaderItemsAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<String> items;
		private List<String> itemInfos1;
		private Typeface typeface;
		
		public ReaderItemsAdapter(Context context, List<String> items, List<String> itemInfos1, Typeface typeface) {
			this.mInflater = LayoutInflater.from(context);
			this.items = items;
			this.itemInfos1 = itemInfos1;
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
//                convertView = mInflater.inflate(R.layout.text_list_reader_item, null);
//                holder = new ViewHolder();
//                holder.title = (TextView) convertView.findViewById(R.id.text1);
//                holder.mean = (TextView) convertView.findViewById(R.id.text2);
                
            	convertView = mInflater.inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.mean = (TextView) convertView.findViewById(R.id.text);
                
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
            if (itemInfos1 != null && position >= 0 && position < itemInfos1.size()) {
            	holder.mean.setText(itemInfos1.get(position));
            } else {
            	holder.mean.setText("");
            }
            return convertView;
		}
		
        private static final class ViewHolder {
        	TextView title;
        	TextView mean;
        }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.text_list_reader);
		
		viewListView = (ListView) this.findViewById(R.id.viewListView);
		buttonOpen = (Button) this.findViewById(R.id.buttonOpen);
		checkBoxSen = (CheckBox) this.findViewById(R.id.checkBoxSen);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("双列utf8文本查看器");
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
		
		/*
		try {
			inputStream = this.getAssets().open(assetFilename);
	        reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		
		RetainInfo info = (RetainInfo) this.getLastNonConfigurationInstance();
		if (info == null) {
			items = new ArrayList<String>();
			itemInfos1 = new ArrayList<String>();
			if (D) {
				Log.d(TAG, "start handler");
			}
			//handler.postDelayed(this, DELAY);
		} else {
			items = info.items;
			itemInfos1 = info.itemInfos1;
		}
		
		//typeface = Typefaces.get(this, "mplus-1m-regular.ttf");
		typeface = Typefaces.getFile(JkanjiSettingActivity.getFontFileName(this));	
		
		adapter = new ReaderItemsAdapter(this, items, itemInfos1, this.typeface);
		viewListView.setAdapter(adapter);
		viewListView.setFastScrollEnabled(true);
		viewListView.setOnItemClickListener(this);
		
		checkBoxSen.setChecked(this.getLastCallSen());
		
		buttonOpen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
					JkanjiTextListActivity.this, DirBrowser.class);
				intent.putExtra(DirBrowser.EXTRA_KEY_SUFFIX, ".txt");
				startActivityForResult(intent, REQUEST_PATH);
			}
		});
	}
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_PATH:
			if (data != null) {
				String resultPath = data.getStringExtra(DirBrowser.EXTRA_KEY_RESULT_PATH);
				loadDataFromFile(resultPath);
				if (viewListView != null) {
					viewListView.setSelection(0);
				}
			}
			break;
		}
	}
    
    private void loadDataFromFile(String path) {
		if (path != null) {
			InputStream inputStream = null;
			BufferedReader reader = null;
			String line;
			try {
				items.clear();
				itemInfos1.clear();
				inputStream = new FileInputStream(path);
		        reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				while ((line = reader.readLine()) != null) {
				    String[] strings = TextUtils.split(line, ",");
				    String str1 = null;
				    String str2 = null;
				    if (strings != null && strings.length >= 1) {
				    	str1 = strings[0];
				    }
				    if (strings != null && strings.length >= 2) {
				    	str2 = strings[1];
				    }				    
				    if (str1 != null) {
				    	items.add(str1);
				    	itemInfos1.add(str2);
				    }
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (adapter != null) {
					adapter.notifyDataSetChanged();
				}
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
    }


	@Override
	public Object onRetainNonConfigurationInstance() {
    	RetainInfo info = new RetainInfo();
    	info.items = this.items;
    	info.itemInfos1 = this.itemInfos1;
    	return info;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (D) {
			Log.d(TAG, "onItemClick " + position);
			Log.d(TAG, "itemInfos1[" + position + "] = " + itemInfos1.get(position));
		}
		if (checkBoxSen != null && checkBoxSen.isChecked()) {
			Intent intent = new Intent(JkanjiTextListActivity.this, JkanjiSenActivity.class);
			intent.putExtra(JkanjiSenActivity.INPUT_KEY, items.get(position));
			startActivity(intent);
		}
	}
    
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		setLastCallSen(this.checkBoxSen.isChecked());
	}

	private void setLastCallSen(boolean isShow) {
		PrefUtil.putBoolean(this, SHARE_PREF_NAME,
				SHARE_PREF_CALL_SEN,
				isShow);
    }
    
    private boolean getLastCallSen() {
		return PrefUtil.getBoolean(this, SHARE_PREF_NAME,
				SHARE_PREF_CALL_SEN,
				true);
    }
}
