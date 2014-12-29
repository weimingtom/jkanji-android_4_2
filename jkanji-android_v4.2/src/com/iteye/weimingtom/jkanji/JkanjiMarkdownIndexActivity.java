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
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class JkanjiMarkdownIndexActivity extends Activity implements Runnable, OnItemClickListener {
	private static final boolean D = false;
	private static final String TAG = "JkanjiListReaderActivity";
	
	public final static String EXTRA_PATH = "com.iteye.weimingtom.jkanji.JkanjiMarkdownIndexActivity.EXTRA_PATH";
	public final static String EXTRA_TITLE = "com.iteye.weimingtom.jkanji.JkanjiMarkdownIndexActivity.EXTRA_TITLE";
		
	private static final int DELAY = 2;
	private static final int FILE_NUM_PER_TIME = 20;
	
	private ListView viewListView;
	private ActionBar actionBar;
	
	private List<String> items; //text
	private List<String> itemInfos1; //URL folder
	private List<String> itemInfos2; //URL file name
	private BaseAdapter adapter;
	
    private Handler handler;
	private Typeface typeface;
	private int currentIndex;
	
	private String path;
	private String title;
	
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
				convertView = mInflater.inflate(R.layout.list_item, null);
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.title);			
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
		this.setContentView(R.layout.markdown_index);
		
		viewListView = (ListView) this.findViewById(R.id.viewListView);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("Markdown列表");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.print;
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
				return R.drawable.search_sqlite;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(JkanjiMarkdownIndexActivity.this, 
					SQLiteReaderActivity.class));
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.search;
			}

			@Override
			public void performAction(View view) {
				startActivity(new Intent(JkanjiMarkdownIndexActivity.this, 
						JKanjiActivity.class));
			}
        });
        
        Intent intent = this.getIntent();
        if (intent != null) {
        	this.path = intent.getStringExtra(EXTRA_PATH);
        	this.title = intent.getStringExtra(EXTRA_TITLE);
        }
        if (this.path == null) {
        	this.path = "";
        }
        if (this.title != null) {
        	actionBar.setTitle(this.title);
        }
        
        currentIndex = 0;
		handler = new Handler();
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position >= 0 && position < itemInfos1.size() && position < items.size()) {
			this.startActivity(new Intent(this, 
				JkanjiMarkdownActivity.class)
				.putExtra(JkanjiMarkdownActivity.FILENAME_KEY, itemInfos1.get(position))
				.putExtra(JkanjiMarkdownActivity.TITLE_KEY, items.get(position))
			);
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
		boolean result = false;
		int beginFilenameLen = getBeginFilenameLen();
		for (int i = 0; i < FILE_NUM_PER_TIME; i++) {
			result = loadFromFile(currentIndex, beginFilenameLen);
			currentIndex++; // skip index 0
			if (currentIndex == 1) {
				continue;
			}
			if (result == false) {
				break;
			}
		}
		if (currentIndex == 1 || // skip index 0
			(result && !this.isFinishing())) {
			handler.postDelayed(this, DELAY);
		} else {
			
		}
	}
	
	private int getBeginFilenameLen() {
		InputStream istr = null;
		try {
			istr = this.getAssets().open(this.path + "001.txt");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (istr != null) {
				try {
					istr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return 3;
			}
		}
		try {
			istr = this.getAssets().open(this.path + "0001.txt");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (istr != null) {
				try {
					istr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return 4;
			}
		}
		return 0;
	}
	
	private boolean loadFromFile(int index, int beginFilenameLen) {
		String strIndex = "";
		if (beginFilenameLen == 3) { // "001.txt"
			if (index < 10) {
				strIndex = "00" + index; 
			} else if (index < 100) {
				strIndex = "0" + index;
			} else {
				strIndex = "" + index;
			}
		} else if (beginFilenameLen == 4) { // "0001.txt"
			if (index < 10) {
				strIndex = "000" + index; 
			} else if (index < 100) {
				strIndex = "00" + index;
			} else if (index < 1000) {
				strIndex = "0" + index;
			} else {
				strIndex = "" + index;
			}
		} else {
			return false;
		}
		String filename = this.path + strIndex + ".txt";
		String title = getTitleFromAsset(filename);
		if (title == null || title.length() == 0) {
			return false;
		} else {
			title = title.replace("> # ", "");
			title = title.replace("# ", "");
			items.add(title);
			itemInfos1.add(filename);
			adapter.notifyDataSetChanged();
			return true;
		}
	}
	
    private String getTitleFromAsset(String filename) {
    	StringBuffer sb = new StringBuffer();
		InputStream istr = null;
		InputStreamReader reader = null;
		BufferedReader rbuf = null;
		try {
			istr = this.getAssets().open(filename);
			reader = new InputStreamReader(istr, "utf8");
			rbuf = new BufferedReader(reader);
			String line;
			if (null != (line = rbuf.readLine())) {
				sb.append(line);
			}
		} catch (IOException e) {
			//e.printStackTrace();
		} finally {
			if (rbuf != null) {
				try {
					rbuf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (istr != null) {
				try {
					istr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
    }
}
