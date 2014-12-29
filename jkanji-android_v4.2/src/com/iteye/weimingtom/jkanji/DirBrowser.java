package com.iteye.weimingtom.jkanji;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DirBrowser extends Activity implements OnItemClickListener {
	private final static boolean D = false;
	private final static String TAG = "DirBrowser";
	
	public static final String EXTRA_ID = "com.iteye.weimingtom.jkanji.DirBrowser";
	public static final String EXTRA_KEY_SUFFIX = "com.iteye.weimingtom.jkanji.DirBrowser.suffix";
	public static final String EXTRA_KEY_RESULT_PATH = "com.iteye.weimingtom.jkanji.DirBrowser.resultPath";
	
	private static final String DEFAULT_PATH = "/mnt/sdcard/jkanji";
	private final static String DEFAULT_ROOT2 = DEFAULT_PATH;
	
	private static final class DirItem {
		public String name;
		public String path;
		public boolean isDir;
		public long length;
		
		public DirItem(String name, String path, boolean isDir, long length) {
			this.name = name;
			this.path = path;
			this.isDir = isDir;
			this.length = length;
		}
	}
	
	private ArrayList<DirItem> dirItems = null;
	private DirItemArrayAdapter adapter;
	private DirItem selectedItem;
	private ListView listView;
	private TextView currentFolder;
	private String suffix;
	
	private ActionBar actionBar;
	
	private Button buttonJump, buttonUp;
	private File currDir;
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dir_browser);
		
		this.suffix = this.getIntent().getStringExtra(EXTRA_KEY_SUFFIX);
		
		this.listView = (ListView) findViewById(R.id.listView); 
		this.currentFolder = (TextView) findViewById(R.id.currentfolder);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		if (this.suffix != null) {
			this.suffix = this.suffix.toLowerCase();
			actionBar.setTitle("文件浏览器 : " + this.suffix);
		} else {
			actionBar.setTitle("文件浏览器");
		}
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.config;
				return R.drawable.shelf_folder;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
		
		final Button btnSelect = (Button) findViewById(R.id.btnSelect);
		btnSelect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectFile();
			}
		});
		
    	buttonJump = (Button) this.findViewById(R.id.buttonJump);
    	buttonJump.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				File file = new File(DEFAULT_ROOT2);
				if (file.exists() && file.isDirectory()) {
					listDir(file);
				} else {
					Toast.makeText(DirBrowser.this, 
						"目录" + DEFAULT_ROOT2 + "不存在，请手动创建该目录", 
						Toast.LENGTH_SHORT).show();
				}
			}
    	});
    	
    	buttonUp = (Button) this.findViewById(R.id.buttonUp);
    	buttonUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currDir.getParentFile() != null) {
					listDir(currDir.getParentFile());
				}
			}
    	});
		
		this.listView.setOnItemClickListener(this);
		this.listView.setFastScrollEnabled(true);
		this.listDir(new File(DEFAULT_PATH));
	}
	
	private void selectFile() {
		if (selectedItem != null && 
			selectedItem.path != null && 
			selectedItem.path.toLowerCase().endsWith(suffix)) {
			Intent data = new Intent();
			data.putExtra(EXTRA_KEY_RESULT_PATH, selectedItem.path);
			setResult(RESULT_OK, data);
			finish();
		} else {
			Toast.makeText(DirBrowser.this, 
				"路径名后缀不正确", 
				Toast.LENGTH_SHORT).show();
		}
	}
	
	private void listDir(File dir) {
		currDir = dir;
		currentFolder.setText(dir.toString());
		File[] files = dir.listFiles();
		dirItems = new ArrayList<DirItem>();
		dirItems.add(new DirItem("转至根目录", "/", true, 0));
		dirItems.add(new DirItem("转至/mnt/sdcard/jkanji", "/mnt/sdcard/jkanji", true, 0));
		if (dir.isDirectory()) {
			String parentPath = (dir.getParent() == null) ? "" : dir.getParent();
			dirItems.add(new DirItem("向上", parentPath, true, 0));
		}
		ArrayList<DirItem> dirItems1 = new ArrayList<DirItem>();
		ArrayList<DirItem> dirItems2 = new ArrayList<DirItem>();
		if (files != null) {
			for (File file : files) {
				if (file != null) {
					if (file.isDirectory()) {
						dirItems1.add(new DirItem(file.getName(), file.getPath(), true, 0));
					} else if (file.getPath() != null && file.getPath().toLowerCase().endsWith(suffix)) {
						dirItems2.add(new DirItem(file.getName(), file.getPath(), false, file.length()));
					}
				}
			}
		}
		Collections.sort(dirItems1, new Comparator<DirItem>() {
			@Override
			public int compare(DirItem arg0, DirItem arg1) {
				if (arg0.path == null) {
					return -1;
				}
				if (arg1.path == null) {
					return 1;
				}
				return arg0.path.compareToIgnoreCase(arg1.path);
			}
		});
		Collections.sort(dirItems2, new Comparator<DirItem>() {
			@Override
			public int compare(DirItem arg0, DirItem arg1) {
				if (arg0.path == null) {
					return -1;
				}
				if (arg1.path == null) {
					return 1;
				}
				return arg0.path.compareTo(arg1.path);
			}
		});;
		dirItems.addAll(dirItems1);
		dirItems.addAll(dirItems2);
		this.adapter = new DirItemArrayAdapter(this, dirItems);
		this.listView.setAdapter(this.adapter);
	}
	
	private static final class DirItemArrayAdapter extends BaseAdapter {
        private final static class ViewHolder {
			TextView text;
			ImageView icon;
			TextView textSize;
        }
		
		private ArrayList<DirItem> items;
		private LayoutInflater inflater;
		
		public DirItemArrayAdapter(Context context, ArrayList<DirItem> items) {
			this.items = items;
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
//            	convertView = inflater.inflate(R.layout.dir_browser_row, null);
//				holder = new ViewHolder();
//				holder.text = (TextView) convertView.findViewById(R.id.text);
//				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
//				holder.textSize = (TextView) convertView.findViewById(R.id.textSize);
//              
            	convertView = inflater.inflate(R.layout.main_menu_item, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.sItemTitle);
				holder.icon = (ImageView) convertView.findViewById(R.id.sItemIcon);
				holder.textSize = (TextView) convertView.findViewById(R.id.sItemInfo);
                            	
				convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            DirItem item = null;
            if (items != null && position >= 0 && position < items.size()) {
            	item = items.get(position);
            }
            if (item != null) {
				holder.text.setText(item.name);
				if (item.isDir == false) {
					//holder.icon.setImageBitmap(null);
					holder.icon.setBackgroundResource(R.drawable.shelf_file);
					holder.textSize.setVisibility(TextView.VISIBLE);
					holder.textSize.setText(formatByteSize(item.length));
				} else {
					//holder.icon.setImageResource(R.drawable.folder);
					holder.icon.setBackgroundResource(R.drawable.shelf_folder);
					holder.textSize.setVisibility(TextView.GONE);
				}
			} else {
				holder.text.setText(null);
				holder.icon.setBackgroundDrawable(null);
				holder.textSize.setVisibility(TextView.GONE);				
			}
			return convertView;
		}

		@Override
		public int getCount() {
			return this.items != null ? this.items.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position == 0) {
			listDir(new File("/"));
		} else if (position == 1) {
			listDir(new File("/mnt/sdcard/jkanji"));
		} else {
			this.selectedItem = dirItems.get(position);
			final File file = new File(dirItems.get(position).path);
			if (file.isDirectory()) {
				listDir(file);
			} else {
				currentFolder.setText(file.toString());
				selectFile();
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
	
	private static String formatByteSize(long sz) {
		DecimalFormat df = new DecimalFormat("#,###.#");
		if (sz > 1000000) return String.format("%s MB", df.format(sz / 1000000.0));
		if (sz > 1000) return String.format("%s KB", df.format(sz / 1000.0));
		return String.format("%s B", df.format(sz));
	}
}
