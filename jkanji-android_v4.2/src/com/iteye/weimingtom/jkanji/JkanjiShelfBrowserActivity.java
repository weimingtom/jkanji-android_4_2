package com.iteye.weimingtom.jkanji;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import fi.harism.curl.BookInfoUtils;
import fi.harism.curl.CurlActivity;
import fi.harism.curl.CurlLandActivity;
import fi.harism.curl.CurlPortActivity;
import fi.harism.curl.ViewPagerActivity;
import fi.harism.curl.ViewPagerLandActivity;
import fi.harism.curl.ViewPagerPortActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class JkanjiShelfBrowserActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "JkanjiShelfBrowserActivity";

	private final static String DEFAULT_ROOT = "/";
	private final static String DEFAULT_ROOT2 = "/sdcard/jkanji";
	private final static String DEFAULT_ROOT3 = "/sdcard/aodowner";
	
	private ActionBar actionBar;
	private TextView textViewInfo;
	private ListView lvFiles;
	private Spinner spinnerEncoding;
	private ArrayAdapter<String> spinnerEncodingAdapter;
	
	private FileAdapter fileAdapter;
	private File currDir;
	
	private Button buttonJump, buttonJumpAodowner, buttonUp;
	
	private final static String KEY_CURRENT_DIR = "KEY_CURRENT_DIR";
	private String strCurrDir = null;
	
	private Comparator<File> fileComp = new Comparator<File>() {
		@Override
		public int compare(File f1, File f2) {
			if (f1.isDirectory() && !f2.isDirectory()) {
				return -1;
			} else if (!f1.isDirectory() && f2.isDirectory()) {
				return 1;
			} else {
				return f1.getName().compareToIgnoreCase(f2.getName());
			}
		}
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.shelf_browser);
        
        if (savedInstanceState != null) {
        	strCurrDir = savedInstanceState.getString(KEY_CURRENT_DIR);
        }
        if (strCurrDir == null) { 
        	strCurrDir = DEFAULT_ROOT;
        }
        
    	actionBar = (ActionBar) this.findViewById(R.id.actionbar);
		actionBar.setTitle("书架浏览");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.shelf_folder;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        textViewInfo = (TextView) findViewById(R.id.textViewInfo);
        lvFiles = (ListView)findViewById(R.id.lvFiles);
		spinnerEncoding = (Spinner) this.findViewById(R.id.spinnerEncoding);
		
		lvFiles.setFastScrollEnabled(true);
        fileAdapter = new FileAdapter(this, R.layout.shelf_browser_item, new ArrayList<File>());
        lvFiles.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				File f = fileAdapter.getItem(position);
				if (f.canRead()) {
					if (f.isFile()) {
						openFile(f);
					} else if (f.isDirectory()) {
						if (f.getName().endsWith("..")) {
							if (currDir.getParentFile() != null) {
								setCurrentDir(currDir.getParentFile());
							}
						} else {
							setCurrentDir(f);
						}
					} else {
						Toast.makeText(JkanjiShelfBrowserActivity.this, 
							"非文件或目录", Toast.LENGTH_SHORT)
							.show();
					}
				} else {
					Toast.makeText(JkanjiShelfBrowserActivity.this, 
						"文件不可读", Toast.LENGTH_SHORT)
						.show();
				}
			}
		});
        
        if (D) {
        	Log.d(TAG, "strCurrDir == " + strCurrDir);
        }
        File file = new File(strCurrDir);
        if (file.exists() && file.isDirectory()) {
        	setCurrentDir(file);
        } else {
        	strCurrDir = DEFAULT_ROOT;
        	setCurrentDir(new File(strCurrDir));
        }
        
    	spinnerEncodingAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_spinner_item);
    	spinnerEncodingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spinnerEncodingAdapter.add("shift-jis");
    	spinnerEncodingAdapter.add("utf8");
    	spinnerEncodingAdapter.add("gbk");
    	spinnerEncodingAdapter.add("unicode");
    	spinnerEncoding.setAdapter(spinnerEncodingAdapter);
    	
    	buttonJump = (Button) this.findViewById(R.id.buttonJump);
    	buttonJump.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				File file = new File(DEFAULT_ROOT2);
				if (file.exists() && file.isDirectory()) {
					setCurrentDir(file);
				} else {
					Toast.makeText(JkanjiShelfBrowserActivity.this, 
						"目录" + DEFAULT_ROOT2 + "不存在，请手动创建该目录", 
						Toast.LENGTH_SHORT).show();
				}
			}
    	});
    	
    	buttonJumpAodowner = (Button) this.findViewById(R.id.buttonJumpAodowner);
    	buttonJumpAodowner.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				File file = new File(DEFAULT_ROOT3);
				if (file.exists() && file.isDirectory()) {
					setCurrentDir(file);
				} else {
					Toast.makeText(JkanjiShelfBrowserActivity.this, 
						"目录" + DEFAULT_ROOT3 + "不存在，请手动创建该目录", 
						Toast.LENGTH_SHORT).show();
				}
			}
    	});
    	
    	buttonUp = (Button) this.findViewById(R.id.buttonUp);
    	buttonUp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currDir.getParentFile() != null) {
					setCurrentDir(currDir.getParentFile());
				}
			}
    	});
    	
    	setResult(RESULT_CANCELED);
    }
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (outState != null) {
			outState.putString(KEY_CURRENT_DIR, strCurrDir);
		}
	}



	private void openFile(File f) {
		if (f != null && f.canRead()) {
			int pos = spinnerEncoding.getSelectedItemPosition();
			if (pos < 0 || pos >= spinnerEncodingAdapter.getCount()) {
				pos = 0;
			}
			String encoding = spinnerEncodingAdapter.getItem(pos);
			if (D) {
				Log.d(TAG, "browser encoding == " + encoding);
			}
			startActivity(new Intent(
				JkanjiShelfBrowserActivity.this, JkanjiShelfPlainReaderActivity.class)
				.putExtra(JkanjiShelfPlainReaderActivity.EXTRA_ID, -1L)
				.putExtra(JkanjiShelfPlainReaderActivity.EXTRA_PLAIN_FILE_NAME, f.getAbsolutePath())
				.putExtra(JkanjiShelfPlainReaderActivity.EXTRA_PLAIN_ENCODING, encoding)
			);
			setResult(RESULT_OK);
			finish();
		} else {
			Toast.makeText(JkanjiShelfBrowserActivity.this, 
				"文件不可读", Toast.LENGTH_SHORT)
				.show();
		}
	}
	
	private void setCurrentDir(File dir) {
		currDir = dir;
		strCurrDir = currDir.getAbsolutePath();
		if (dir.isDirectory()) {
			fileAdapter.clear();
			File [] files = dir.listFiles();
			Arrays.sort(files, fileComp);
			if (!dir.getName().equals("")) {
				fileAdapter.add(new File(".."));
			} 
			for (File f: files) {
				fileAdapter.add(f);
			}
			lvFiles.setAdapter(fileAdapter);
			textViewInfo.setText(dir.getAbsolutePath() + "\n (" + formatDesc(dir) + ")");
		}
	}
	
	private final static String formatDesc(File f) {
		if (f.getName().equals("..")) {
			return "上一级目录";
		} else if (f.isDirectory()) {
			File [] files = f.listFiles();
			if (files == null) {
				return "没有子文件或目录";
			} else {
				return String.format("子文件或目录%d个", files.length);
			}
		} else {
			long sz = f.length();
			DecimalFormat df = new DecimalFormat("#,###.#");
			if (sz > 10000000) {
				return String.format("%s MB", df.format(sz/1000000.0));
			} else if (sz > 10000) {
				return String.format("%s KB", df.format(sz/1000.0));
			} else {
				return String.format("%s B", df.format(sz));
			}
		}
	}
		
	private final static class ViewHolder {
		ImageView ivIcon;
		TextView tvFilename;
		TextView tvDesc;
	}
	
	private static class FileAdapter extends ArrayAdapter<File> {
		private LayoutInflater inflater;
		private int textViewId;
		
		public FileAdapter(Context context, int textViewResourceId, List<File> files) {
			super(context, textViewResourceId, files);
			textViewId = textViewResourceId;
			inflater = LayoutInflater.from(context);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(textViewId, null);
				holder = new ViewHolder();
				holder.ivIcon = (ImageView)convertView.findViewById(R.id.ivIcon);
				holder.tvFilename = (TextView)convertView.findViewById(R.id.tvFilename);
				holder.tvDesc = (TextView)convertView.findViewById(R.id.tvDesc);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			File f = getItem(position);
			if (f != null) {
				holder.tvFilename.setText(f.getName());
				if (f.isFile() && !f.canRead() || f.isDirectory() && !f.canWrite()) {
					holder.tvFilename.setTextColor(Color.GRAY);
				} else {
					holder.tvFilename.setTextColor(Color.BLACK);
				}
				holder.tvDesc.setText(formatDesc(f));
				if (f.isDirectory()) {
					if (!f.canRead()) {
						holder.ivIcon.setImageResource(R.drawable.shelf_folder_disable);
					} else {
						holder.ivIcon.setImageResource(R.drawable.shelf_folder);
					}
				} else {
					holder.ivIcon.setImageResource(R.drawable.shelf_file);
				}
			} else {
				holder.tvFilename.setText(null);
				holder.tvDesc.setText(null);
				holder.ivIcon.setImageBitmap(null);
			}
			return convertView;
		}
	}
}

