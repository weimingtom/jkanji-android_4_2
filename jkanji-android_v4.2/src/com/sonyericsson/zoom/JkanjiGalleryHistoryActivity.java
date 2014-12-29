package com.sonyericsson.zoom;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.provider.MediaStore;
import android.text.format.Time;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.iteye.weimingtom.jkanji.JkanjiAozoraReaderActivity;
import com.iteye.weimingtom.jkanji.PrefUtil;
import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

public class JkanjiGalleryHistoryActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "JkanjiGalleryHistoryActivity";
	
	private final static String KEY_CURRENT_POS = "JkanjiGalleryHistoryActivity.KEY_CURRENT_POS";
	
	private final static int CONTEXT_MENU_PLAIN = ContextMenu.FIRST + 0;
	private final static int CONTEXT_MENU_GRID = ContextMenu.FIRST + 1;
	private final static int CONTEXT_MENU_DIALOG = ContextMenu.FIRST + 2;
	private final static int CONTEXT_MENU_PLAIN2 = ContextMenu.FIRST + 3;
	private final static int CONTEXT_MENU_SHARE_PHOTO = ContextMenu.FIRST + 4;
	private final static int CONTEXT_MENU_DESC = ContextMenu.FIRST + 5;
	private final static int CONTEXT_MENU_SHARE = ContextMenu.FIRST + 6;
	private final static int CONTEXT_MENU_DELETE = ContextMenu.FIRST + 7;
	private final static int CONTEXT_MENU_PLAIN_SCROLL = ContextMenu.FIRST + 8;
	private final static int CONTEXT_MENU_PLAIN_SCROLL2 = ContextMenu.FIRST + 9;
	private final static int CONTEXT_MENU_PLAIN_PAGER = ContextMenu.FIRST + 10;
	private final static int CONTEXT_MENU_PLAIN_PAGER2 = ContextMenu.FIRST + 11;
		
	private static final int REQUEST_RE_JPG_PATH = 5;
	private static final int REQUEST_EX = 6;
	private static final int REQUEST_GALLERY_OPEN = 7;
	private static final int REQUEST_GALLERY_SETTING = 8;
	private static final int REQUEST_GRID = 9;
	
	private static final int DIALOG_WARNING_ID = 1;
	private static final int DIALOG_LIST = 2;
	private static final int DIALOG_TEXT_ENTRY = 3;
	private static final int DIALOG_ADD_LIST = 4;
	
	private static final String SHARE_PREF_NAME = "pref";
	//FIXME: 注意修改字面值
	private static final String SHARE_PREF_SORT_TYPE = "gallerySortType";
	private static final String SHARE_GALLERY_SHOW_CONFIG = "galleryShowConfig";
	private static final String SHARE_GALLERY_OPEN_TYPE = "galleryOpenType";
	private static final String SHARE_GALLERY_LIST_POS = "galleryListPos";
	
	private final static int SORT_TYPE_CREATE = 0;
	private final static int SORT_TYPE_MODIFY = 1;
	private final static String[] SORT_TYPE_ITEMS = new String[] {
		"创建时间（倒序）",
		"最近阅读时间（倒序）",
	};
	private ArrayAdapter<String> sortAdapter;
	
	private ActionBar actionBar;
	private RecentFileAdapter adapter;
	private ListView listViewShelf;
	private Button buttonPreview;
	private Spinner spinnerMode;
	private ArrayAdapter<String> spinnerModeAdapter;
//	private JkanjiGalleryHistoryDataSource dataSource;

	private Button buttonSettings;
	private AlertDialog.Builder builder1, builder2, builder3, builder4;
	private LinearLayout linearLayoutConfig;
	private TextView textViewLoading;
	
	private int currentPos = -1;
	
	private EditText editDescription;
	private AlertDialog descDialog;
	
	private ImagePreviewDialog mImagePreviewDialog;

	private ArrayAdapter<String> addAdapter;
	private final static String[] ADD_TYPE_ITEMS = new String[] {
		"Root Explorer",
		"内置浏览器（无图）",
		"内置浏览器（缩略图）",
	};
	private final static int ADD_TYPE_RE = 0;
	private final static int ADD_TYPE_EX1 = 1;
	private final static int ADD_TYPE_EX2 = 2;
	
	private volatile Bitmap[] mThumbnails;
	private volatile LoadDataTask mLoadDataTask;
	private volatile boolean listViewShelfIdle = true;
	
	private volatile int listPos;
	
	private volatile AtomicInteger isStop = new AtomicInteger(0);
	
	private final static String ICONTAG = "ICONTAG";
	
	private volatile boolean useThumbContentProvider = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.gallery_history);
		
		if (savedInstanceState != null) {
			this.currentPos = savedInstanceState.getInt(KEY_CURRENT_POS, -1);
		}
		
    	actionBar = (ActionBar) this.findViewById(R.id.actionbar);
		actionBar.setTitle("图库");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.shelf_folder;
				return R.drawable.gallery;
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
				return R.drawable.adding;
			}

			@Override
			public void performAction(View view) {
				showDialog(DIALOG_ADD_LIST);
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.sort;
			}

			@Override
			public void performAction(View view) {
				showDialog(DIALOG_LIST);
			}
        });
        actionBar.addAction(new ActionBar.Action() {
 			@Override
 			public int getDrawable() {
 				return R.drawable.config;
 			}

 			@Override
 			public void performAction(View view) {
 				if (getLastGalleryShowConfig()) {
 					linearLayoutConfig.setVisibility(View.GONE);
 					setLastGalleryShowConfig(false);
 				} else {
 					linearLayoutConfig.setVisibility(View.VISIBLE);
 					setLastGalleryShowConfig(true);
 				}
 			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.config2;
			}

			@Override
			public void performAction(View view) {
				startActivityForResult(new Intent(
					JkanjiGalleryHistoryActivity.this, JkanjiGallerySettingActivity.class), REQUEST_GALLERY_SETTING
				);
			}
        });
        
        this.useThumbContentProvider = JkanjiGallerySettingActivity.getThumbContentProvider(this);
        
		listViewShelf = (ListView) this.findViewById(R.id.listViewShelf);
		spinnerMode = (Spinner) this.findViewById(R.id.spinnerMode);
		buttonSettings = (Button) this.findViewById(R.id.buttonSettings);
		buttonPreview = (Button) this.findViewById(R.id.buttonPreview);
		linearLayoutConfig = (LinearLayout) this.findViewById(R.id.linearLayoutConfig);
		textViewLoading = (TextView) this.findViewById(R.id.textViewLoading);
		
		builder1 = new AlertDialog.Builder(this);
		builder2 = new AlertDialog.Builder(this);
		builder3 = new AlertDialog.Builder(this);
		builder4 = new AlertDialog.Builder(this);
		sortAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.select_dialog_singlechoice);
		for (String str : SORT_TYPE_ITEMS) {
			sortAdapter.add(str);
		}
		addAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.select_dialog_item);
		for (String str : ADD_TYPE_ITEMS) {
			addAdapter.add(str);
		}
		
		spinnerModeAdapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_spinner_item);
		spinnerModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerModeAdapter.add("上下文菜单"); // 0
		spinnerModeAdapter.add("图库查看器"); // 1
		spinnerModeAdapter.add("目录表格预览（只读模式）"); // 2
		spinnerModeAdapter.add("预览对话框"); // 3
		spinnerModeAdapter.add("图库查看器（只读模式）"); // 4
		spinnerModeAdapter.add("卷轴查看器"); // 5
		spinnerModeAdapter.add("卷轴查看器（只读模式）"); // 6
		spinnerModeAdapter.add("平移查看器"); // 7
		spinnerModeAdapter.add("平移查看器（只读模式）"); // 8
		spinnerModeAdapter.add("共享图片"); // 9
		spinnerModeAdapter.add("修改备注"); // 10
		spinnerModeAdapter.add("共享设置"); // 11
		spinnerModeAdapter.add("删除模式"); // 12
		spinnerMode.setAdapter(spinnerModeAdapter);
		spinnerMode.setSelection(getLastOpenType());
		spinnerMode.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				setLastOpenType(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
//		dataSource = new JkanjiGalleryHistoryDataSource(this);
//		dataSource.open();
		
		adapter = new RecentFileAdapter(this, R.layout.gallery_history_item);
    	listViewShelf.setAdapter(adapter);
    	listViewShelf.setFastScrollEnabled(true);
    	
    	listPos = getLastListPos();
    	listViewShelf.setSelection(listPos);
    	
    	listViewShelf.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int itemPos = spinnerMode.getSelectedItemPosition();
				switch (itemPos) {					
				case 0:
					openContextMenu(view);
					break;

				case 1:
					openPlainItem(position, true);
					break;
					
				case 2:
					openGrid(position);
					break;
					
				case 3:
					openImageDialogPosition(position);
					break;

				case 4:
					openPlainItem(position, false);
					break;

				case 5:
					openPlainItemScroll(position, true);
					break;
					
				case 6:
					openPlainItemScroll(position, false);
					break;

				case 7:
					openPlainItemPager(position, true);
					break;
					
				case 8:
					openPlainItemPager(position, false);
					break;
					
				case 9:
					sharePhoto(position);
					break;
					
				case 10:
					currentPos = position;
					showDialog(DIALOG_TEXT_ENTRY);
					break;
					
				case 11:
					shareItem(position);
					break;
					
				case 12:
					//deleteItem(position);
					currentPos = position;
					showDialog(DIALOG_WARNING_ID);
					break;
				}
			}
    	});
    	registerForContextMenu(listViewShelf);
    	listViewShelf.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                	listViewShelfIdle = true;
                	if (absListView != null) {
                		listPos = absListView.getFirstVisiblePosition();
                	}
                } else {
                	listViewShelfIdle = false;
                }
        	}

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
            }
        });
    	
    	buttonSettings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(
					JkanjiGalleryHistoryActivity.this, JkanjiGallerySettingActivity.class), REQUEST_GALLERY_SETTING
				);
			}
    	});
    	
    	buttonPreview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(
					JkanjiGalleryHistoryActivity.this, ImageGrid2Activity.class)
					.putExtra(ImageGrid2Activity.EXTRA_NORECORD, true), REQUEST_GRID
				);
			}
    	});
    	
    	if (this.getLastGalleryShowConfig()) {
    		this.linearLayoutConfig.setVisibility(View.VISIBLE);
    	} else {
    		this.linearLayoutConfig.setVisibility(View.GONE);
    	}
    	
		updateItems();
    }
	
	private void addItemByRE() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		try {
			startActivityForResult(intent, REQUEST_RE_JPG_PATH);
		} catch (Throwable e) {
			e.printStackTrace();
			Toast.makeText(JkanjiGalleryHistoryActivity.this, 
				"找不到可用的应用程序", Toast.LENGTH_SHORT)
				.show();
		}
	}
	
	private void openPlainItem(int pos, boolean isRecord) {
		JkanjiGalleryHistoryItem item = adapter.getItem(pos);
		String fileName = item.getPlainPathName();
		File file = new File(fileName);
		if (file.exists()) {
			Class<?> galleryClass = JkanjiGalleryActivity.class;
			int screenOri;
			if (isRecord) {
				screenOri = JkanjiGallerySettingActivity.getScreenOri(this);
			} else {
				screenOri = JkanjiGallerySettingActivity.getScreenOri2(this);
			}
			if (JkanjiGallerySettingActivity.getAutoCalcOri(this)) {
				screenOri = JkanjiGallerySettingActivity.calcOri(this, 
					new File(item.getPlainPathName(), item.getPlainFileName()), 
					screenOri);
			}
			switch (screenOri) {
	    	case JkanjiGallerySettingActivity.SCREEN_ORI_PORT:
	    		galleryClass = JkanjiGalleryPortActivity.class;
	    		break;
	    		
	    	case JkanjiGallerySettingActivity.SCREEN_ORI_LAND:
	    		galleryClass = JkanjiGalleryLandActivity.class;
	    		break;
	    	}
			startActivityForResult(new Intent(
				this, galleryClass)
				.putExtra(JkanjiGalleryActivity.EXTRA_KEY_PATH, item.getPlainPathName())
				.putExtra(JkanjiGalleryActivity.EXTRA_KEY_FILEID, item.getPlainPage())
				.putExtra(JkanjiGalleryActivity.EXTRA_KEY_FILENAME, item.getPlainFileName())
				.putExtra(JkanjiGalleryActivity.EXTRA_KEY_ID, item.getId())
				.putExtra(JkanjiGalleryActivity.EXTRA_KEY_ZOOM, item.getPlainZoom())
				.putExtra(JkanjiGalleryActivity.EXTRA_KEY_PANX, item.getPlainPanX())
				.putExtra(JkanjiGalleryActivity.EXTRA_KEY_PANY, item.getPlainPanY())
				.putExtra(JkanjiGalleryActivity.EXTRA_KEY_MULTI, item.isPlainEnableMulti())
				.putExtra(JkanjiGalleryActivity.EXTRA_KEY_DESC, item.getPlainDesc())
				.putExtra(JkanjiGalleryActivity.EXTRA_KEY_ISRECORD, isRecord), REQUEST_GALLERY_OPEN
			);
		} else {
			Toast.makeText(this, 
				"目录不存在：" + fileName, 
				Toast.LENGTH_SHORT).show();
		}
	}
	
	private void openPlainItemScroll(int pos, boolean isRecord) {
		JkanjiGalleryHistoryItem item = adapter.getItem(pos);
		String fileName = item.getPlainPathName();
		File file = new File(fileName);
		if (file.exists()) {
			Class<?> galleryClass = JkanjiScrollGalleryActivity.class;
			int screenOri;
			if (isRecord) {
				screenOri = JkanjiGallerySettingActivity.getScreenOri(this);
			} else {
				screenOri = JkanjiGallerySettingActivity.getScreenOri2(this);
			}
			if (JkanjiGallerySettingActivity.getAutoCalcOri(this)) {
				screenOri = JkanjiGallerySettingActivity.calcOri(this, 
						new File(item.getPlainPathName(), item.getPlainFileName()), 
						screenOri);
			}
			switch (screenOri) {
	    	case JkanjiGallerySettingActivity.SCREEN_ORI_PORT:
	    		galleryClass = JkanjiScrollGalleryPortActivity.class;
	    		break;
	    		
	    	case JkanjiGallerySettingActivity.SCREEN_ORI_LAND:
	    		galleryClass = JkanjiScrollGalleryLandActivity.class;
	    		break;
	    	}
			startActivityForResult(new Intent(
				this, galleryClass)
				.putExtra(JkanjiScrollGalleryActivity.EXTRA_KEY_PATH, item.getPlainPathName())
				.putExtra(JkanjiScrollGalleryActivity.EXTRA_KEY_FILEID, item.getPlainPage())
				.putExtra(JkanjiScrollGalleryActivity.EXTRA_KEY_FILENAME, item.getPlainFileName())
				.putExtra(JkanjiScrollGalleryActivity.EXTRA_KEY_ID, item.getId())
				.putExtra(JkanjiScrollGalleryActivity.EXTRA_KEY_ZOOM, 1.0f) //FIXME:
				.putExtra(JkanjiScrollGalleryActivity.EXTRA_KEY_PANX, 0.5f) //FIXME:
				.putExtra(JkanjiScrollGalleryActivity.EXTRA_KEY_PANY, 0.0f) //FIXME:
				.putExtra(JkanjiScrollGalleryActivity.EXTRA_KEY_MULTI, item.isPlainEnableMulti())
				.putExtra(JkanjiScrollGalleryActivity.EXTRA_KEY_DESC, item.getPlainDesc())
				.putExtra(JkanjiScrollGalleryActivity.EXTRA_KEY_ISRECORD, isRecord), REQUEST_GALLERY_OPEN
			);
		} else {
			Toast.makeText(this, 
				"目录不存在：" + fileName, 
				Toast.LENGTH_SHORT).show();
		}
	}
	
	private void openPlainItemPager(int pos, boolean isRecord) {
		JkanjiGalleryHistoryItem item = adapter.getItem(pos);
		String fileName = item.getPlainPathName();
		File file = new File(fileName);
		if (file.exists()) {
			Class<?> galleryClass = JkanjiPagerGalleryActivity.class;
			int screenOri;
			if (isRecord) {
				screenOri = JkanjiGallerySettingActivity.getScreenOri(this);
			} else {
				screenOri = JkanjiGallerySettingActivity.getScreenOri2(this);
			}
			if (JkanjiGallerySettingActivity.getAutoCalcOri(this)) {
				screenOri = JkanjiGallerySettingActivity.calcOri(this, 
						new File(item.getPlainPathName(), item.getPlainFileName()), 
						screenOri);
			}
			switch (screenOri) {
	    	case JkanjiGallerySettingActivity.SCREEN_ORI_PORT:
	    		galleryClass = JkanjiPagerGalleryPortActivity.class;
	    		break;
	    		
	    	case JkanjiGallerySettingActivity.SCREEN_ORI_LAND:
	    		galleryClass = JkanjiPagerGalleryLandActivity.class;
	    		break;
	    	}
			startActivityForResult(new Intent(
				this, galleryClass)
				.putExtra(JkanjiPagerGalleryActivity.EXTRA_KEY_PATH, item.getPlainPathName())
				.putExtra(JkanjiPagerGalleryActivity.EXTRA_KEY_FILEID, item.getPlainPage())
				.putExtra(JkanjiPagerGalleryActivity.EXTRA_KEY_FILENAME, item.getPlainFileName())
				.putExtra(JkanjiPagerGalleryActivity.EXTRA_KEY_ID, item.getId())
				.putExtra(JkanjiPagerGalleryActivity.EXTRA_KEY_ZOOM, item.getPlainZoom())
				.putExtra(JkanjiPagerGalleryActivity.EXTRA_KEY_PANX, item.getPlainPanX())
				.putExtra(JkanjiPagerGalleryActivity.EXTRA_KEY_PANY, item.getPlainPanY())
				.putExtra(JkanjiPagerGalleryActivity.EXTRA_KEY_MULTI, item.isPlainEnableMulti())
				.putExtra(JkanjiPagerGalleryActivity.EXTRA_KEY_DESC, item.getPlainDesc())
				.putExtra(JkanjiPagerGalleryActivity.EXTRA_KEY_ISRECORD, isRecord), REQUEST_GALLERY_OPEN
			);
		} else {
			Toast.makeText(this, 
				"目录不存在：" + fileName, 
				Toast.LENGTH_SHORT).show();
		}
	}
	
	
	private void openGrid(int pos) {
		JkanjiGalleryHistoryItem item = adapter.getItem(pos);
		String fileName = item.getPlainPathName();
		File file = new File(fileName);
		if (file.exists()) {
			String name = item.getPlainFileName();
			File file2 = new File(fileName, name);
			startActivityForResult(new Intent(
				this, ImageGrid2Activity.class)
				.putExtra(ImageGrid2Activity.EXTRA_FILENAME, file2.getAbsolutePath())
				.putExtra(ImageGrid2Activity.EXTRA_NORECORD, true), REQUEST_GRID
			);
		} else {
			Toast.makeText(this, 
				"目录不存在：" + fileName, 
				Toast.LENGTH_SHORT).show();
		}
	}
	
    @Override
	protected void onDestroy() {
		super.onDestroy();
		if (mImagePreviewDialog != null && mImagePreviewDialog.isShowing()) {
			mImagePreviewDialog.dismiss();
		}
		mImagePreviewDialog = null;
		if (mThumbnails != null) {
			for (int i = 0; i < mThumbnails.length; i++) {
				if (mThumbnails[i] != null && !mThumbnails[i].isRecycled()) {
					mThumbnails[i].recycle();
					mThumbnails[i] = null;
				}
			}
		}	
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private void updateItems() {
		if (mLoadDataTask == null) {
			mLoadDataTask = new LoadDataTask();
			mLoadDataTask.execute();
		} else {
//			Toast.makeText(this, "缩略图更新未完成", Toast.LENGTH_SHORT).show();
			mLoadDataTask = null;
			this.isStop.set(1);
		}
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
    	super.onCreateContextMenu(menu, v, info);
    	menu.add(0, CONTEXT_MENU_PLAIN, 0, "图库查看器");
    	menu.add(0, CONTEXT_MENU_GRID, 0, "目录表格预览（只读模式）");
    	menu.add(0, CONTEXT_MENU_DIALOG, 0, "预览对话框");
    	menu.add(0, CONTEXT_MENU_PLAIN2, 0, "图库查看器（只读模式）");
    	menu.add(0, CONTEXT_MENU_PLAIN_SCROLL, 0, "卷轴查看器");
    	menu.add(0, CONTEXT_MENU_PLAIN_SCROLL2, 0, "卷轴查看器（只读模式）");
    	menu.add(0, CONTEXT_MENU_PLAIN_PAGER, 0, "平移查看器");
    	menu.add(0, CONTEXT_MENU_PLAIN_PAGER2, 0, "平移查看器（只读模式）");
    	menu.add(0, CONTEXT_MENU_SHARE_PHOTO, 0, "共享图片");
    	menu.add(0, CONTEXT_MENU_DESC, 0, "修改备注");
    	menu.add(0, CONTEXT_MENU_SHARE, 0, "共享设置");
    	menu.add(0, CONTEXT_MENU_DELETE, 0, "删除记录");
	}
	
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    	switch (item.getItemId()) {
    	case CONTEXT_MENU_DELETE:
    		//deleteItem((int)info.id);
    		currentPos = info.position;
    		showDialog(DIALOG_WARNING_ID);
    		return true;
    		
    	case CONTEXT_MENU_PLAIN:
    		openPlainItem(info.position/*(int)info.id*/, true);
    		return true;
    		
    	case CONTEXT_MENU_SHARE:
    		shareItem(info.position/*(int)info.id*/);
    		return true;
    		
    	case CONTEXT_MENU_DESC:
    		currentPos = info.position;
    		showDialog(DIALOG_TEXT_ENTRY);
    		return true;
    	
    	case CONTEXT_MENU_GRID:
    		openGrid(info.position);
    		return true;
    		
    	case CONTEXT_MENU_SHARE_PHOTO:
    		sharePhoto(info.position);
    		return true;
    		
    	case CONTEXT_MENU_DIALOG:
    		openImageDialogPosition(info.position);
    		return true;

    	case CONTEXT_MENU_PLAIN2:
    		openPlainItem(info.position/*(int)info.id*/, false);
    		return true;

    	case CONTEXT_MENU_PLAIN_SCROLL:
    		openPlainItemScroll(info.position/*(int)info.id*/, true);
    		return true;
    		
    	case CONTEXT_MENU_PLAIN_SCROLL2:
    		openPlainItemScroll(info.position/*(int)info.id*/, false);
    		return true;
    		
    	case CONTEXT_MENU_PLAIN_PAGER:
    		openPlainItemPager(info.position/*(int)info.id*/, true);
    		return true;
    		
    	case CONTEXT_MENU_PLAIN_PAGER2:
    		openPlainItemPager(info.position/*(int)info.id*/, false);
    		return true;
    	}
    	return super.onContextItemSelected(item);
    }
    

    @Override
	protected Dialog onCreateDialog(int id) {
    	Dialog dialog = null;
	    switch(id) {
	    case DIALOG_WARNING_ID:
    		return builder1
    			.setTitle("警告")
    			.setMessage("是否删除图库记录？")
    			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int id) {
    					deleteItem(currentPos);
    					currentPos = -1;
    				}
    			})
    			.setNegativeButton("取消", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int id) {
    					currentPos = -1;
    				}
    			})
    			.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						currentPos = -1;
					}
    			})
    	       .create();
	        
        case DIALOG_LIST:
    		dialog = builder2
                .setTitle("排序方式")
                .setSingleChoiceItems(sortAdapter, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setLastSortType(which);
                        updateItems();
                        dialog.cancel();
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
    		dialog.setOnShowListener(new OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					((AlertDialog) dialog).getListView().setItemChecked(getLastSortType(), true);
				}
    		});
            return dialog;
            
		case DIALOG_TEXT_ENTRY:
			LayoutInflater factory = LayoutInflater.from(this);
            View textEntryView = factory.inflate(R.layout.set_des_dialog, null);
            editDescription = (EditText) textEntryView.findViewById(R.id.editDescription);
            descDialog = builder3
            	.setTitle("备注")
                .setView(textEntryView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	try {
                    		String str = editDescription.getText().toString();
                    		setDesc(currentPos, str);
                    	} catch (Throwable e) {
                    		e.printStackTrace();
                    	}
                    	currentPos = -1;
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	currentPos = -1;
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						currentPos = -1;
					}
                })
                .create();
            descDialog.setOnShowListener(new OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					editDescription.setText("");
					String desc = getDesc(currentPos);
					if (desc != null) {
						editDescription.append(desc);
					}
				}
            });
            return descDialog;
            
        case DIALOG_ADD_LIST:
    		dialog = builder4
                .setTitle("添加方式")
                .setAdapter(addAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	switch (which) {
                    	default:
                    	case ADD_TYPE_RE:
                    		addItemByRE();
                    		break;
                    		
                    	case ADD_TYPE_EX1:
                    		startActivityForResult(new Intent(
    							JkanjiGalleryHistoryActivity.this, JkanjiGalleryBrowserActivity.class), REQUEST_EX
    						);
                    		break;
                    		
                    	case ADD_TYPE_EX2:
                    		startActivityForResult(new Intent(
    							JkanjiGalleryHistoryActivity.this, ImageGrid2Activity.class), REQUEST_EX
    						);
                    		break;
                    	}
                        dialog.cancel();
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
            return dialog;
	    }
    	return dialog;
	}

	private void deleteItem(int pos) {
		if (pos >= 0 && pos < adapter.getCount()) {
			JkanjiGalleryHistoryItem historyItem = adapter.getItem(pos);
			JkanjiGalleryHistoryDataSource dataSource = new JkanjiGalleryHistoryDataSource(this);
			dataSource.open();
			dataSource.deleteItem(historyItem);
			dataSource.close();
			adapter.remove(historyItem);
//			adapter.notifyDataSetChanged();
			updateItems();
		}
    }

	private void shareItem(int pos) {
		if (pos >= 0 && pos < adapter.getCount()) {
			JkanjiGalleryHistoryItem historyItem = adapter.getItem(pos);
			if (historyItem != null) {
				Intent intent;
				intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT, JkanjiAozoraReaderActivity.getTimeString() + ":图库预设");
				intent.putExtra(Intent.EXTRA_TEXT, historyItem.toShareString());
	            try {
	            	//startActivity(Intent.createChooser(intent, "共享方式"));
	            	startActivity(intent);
	            } catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiGalleryHistoryActivity.this, 
						"共享方式出错", Toast.LENGTH_SHORT)
						.show();
				}
			}
		}
	}
    
	private void sharePhoto(int pos) {
		if (pos >= 0 && pos < adapter.getCount()) {
			JkanjiGalleryHistoryItem historyItem = adapter.getItem(pos);
			if (historyItem != null) {
				File file = new File(historyItem.getPlainPathName(), historyItem.getPlainFileName());
				Intent intent;
				intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("image/*");
				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
	            try {
	            	//startActivity(Intent.createChooser(intent, "共享方式"));
	            	startActivity(intent);
	            } catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiGalleryHistoryActivity.this, 
						"共享方式出错", Toast.LENGTH_SHORT)
						.show();
				}
			}
		}
	}

	private void openImageDialogPosition(int pos) {
		if (pos >= 0 && pos < adapter.getCount()) {
			JkanjiGalleryHistoryItem historyItem = adapter.getItem(pos);
			if (historyItem != null) {
				File file = new File(historyItem.getPlainPathName(), historyItem.getPlainFileName());
				openImageDialog(file.getAbsolutePath());
			}
		}
	}
	
    private void setLastSortType(int type) {
		PrefUtil.putInt(this, SHARE_PREF_NAME,
				SHARE_PREF_SORT_TYPE,
    			type);
    }
    
    private int getLastSortType() {
		return PrefUtil.getInt(this, SHARE_PREF_NAME,
    			SHARE_PREF_SORT_TYPE,
    			SORT_TYPE_CREATE);
    }
	
    private void setLastGalleryShowConfig(boolean isEnabled) {
		PrefUtil.putBoolean(this, SHARE_PREF_NAME,
    			SHARE_GALLERY_SHOW_CONFIG,
    			isEnabled);
    }
    
    private boolean getLastGalleryShowConfig() {
    	return PrefUtil.getBoolean(this, SHARE_PREF_NAME,
				SHARE_GALLERY_SHOW_CONFIG,
    			false);
    }

    private void setLastOpenType(int type) {
    	PrefUtil.putInt(this, SHARE_PREF_NAME,
    			SHARE_GALLERY_OPEN_TYPE,
				type);
    }
    
    private int getLastOpenType() {
		return PrefUtil.getInt(this, SHARE_PREF_NAME,
    			SHARE_GALLERY_OPEN_TYPE,
    			0);
    }
    
    private void setLastListPos(int listPos) {
    	PrefUtil.putInt(this, SHARE_PREF_NAME,
				SHARE_GALLERY_LIST_POS,
				listPos);
    }
    
    private int getLastListPos() {
    	return PrefUtil.getInt(this, SHARE_PREF_NAME,
    			SHARE_GALLERY_LIST_POS,
    			0);
    }
    
    private void setDesc(int pos, String desc) {
		if (pos >= 0 && pos < adapter.getCount()) {
			JkanjiGalleryHistoryItem historyItem = adapter.getItem(pos);
			historyItem.setPlainDesc(desc);
			JkanjiGalleryHistoryDataSource dataSource = new JkanjiGalleryHistoryDataSource(this);
			dataSource.open();
			dataSource.createItem(historyItem);
			dataSource.close();
			adapter.notifyDataSetChanged();
//			updateItems();
		}
    }
    
    private String getDesc(int pos) {
    	String desc = null;
    	if (pos >= 0 && pos < adapter.getCount()) {
    		JkanjiGalleryHistoryItem item = adapter.getItem(pos);
    		desc = item.getPlainDesc();
    	}
    	return desc;
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_RE_JPG_PATH:
			if (resultCode == RESULT_OK && 
				data != null &&
				data.getData() != null) {
				String resultPath = data.getData().getPath();
				if (D) {
					Log.e(TAG, "resultPath == " + resultPath);
				}
				File file;
				if (resultPath != null) {
					if (resultPath.startsWith("/external")) {
	                    Uri originalUri = data.getData();
	                    String[] proj = {MediaStore.Images.Media.DATA};
	                    Cursor cursor = managedQuery(originalUri, proj, null, null, null); 
	                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	                    cursor.moveToFirst();
	                    String path = cursor.getString(column_index);
	                    file = new File(path);
					} else {
				    	file = new File(resultPath);
					}
					JkanjiGalleryHistoryDataSource dataSrc = new JkanjiGalleryHistoryDataSource(this);
			    	dataSrc.open();
			    	JkanjiGalleryHistoryItem item = new JkanjiGalleryHistoryItem();
			    	item.setId(-1L);
			    	item.setPlainZoom(1.0f);
			    	item.setPlainPanX(0.5f);
			    	item.setPlainPanY(0.5f);
			    	item.setPlainPage(-1);
			    	item.setPlainTotalPage(0);
			    	item.setPlainFileName(file.getName());
			    	item.setPlainPathName(file.getParent());
			    	item.setPlainEnableMulti(true);
			    	dataSrc.createItem(item);
			    	dataSrc.close();
			    	
					listPos = 0;
					setLastListPos(listPos);
			    	updateItems();
				}
			}
			break;
			
		case REQUEST_EX:
			if (resultCode == RESULT_OK) {
				listPos = 0;
				setLastListPos(listPos);
				updateItems();
			}
			break;
			
		case REQUEST_GALLERY_OPEN:
		case REQUEST_GALLERY_SETTING:
		case REQUEST_GRID:
			if (resultCode == RESULT_OK) {
				updateItems();
			}
			break;
		}
	}
	
	private final static class ViewHolder {
		ImageView ivIcon;
		TextView tvFilename;
		TextView tvDesc;
	}
	
	private final class RecentFileAdapter extends ArrayAdapter<JkanjiGalleryHistoryItem> {
		private LayoutInflater inflater;
		private int textViewId;
		
		public RecentFileAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
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
				holder = (ViewHolder)convertView.getTag();
			}
			holder.ivIcon.setTag(ICONTAG + position);
			JkanjiGalleryHistoryItem item = getItem(position);
			if (item != null) {
				File f = new File(item.getPlainFileName());
				holder.tvFilename.setText(f.getName());
				if (mThumbnails != null && position >= 0 && position < mThumbnails.length) {
					Bitmap bitmap = mThumbnails[position];
					if (bitmap != null && !bitmap.isRecycled()) {
						holder.ivIcon.setImageBitmap(bitmap);
					} else {
						holder.ivIcon.setImageResource(R.drawable.shelf_file);
					}
				} else {
					holder.ivIcon.setImageResource(R.drawable.shelf_file);
				}
				holder.tvDesc.setText(item.toHistoryDesc());
			} else {
				holder.tvFilename.setText(null);
				holder.ivIcon.setImageBitmap(null);
				holder.tvDesc.setText(null);
			}
			return convertView;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (outState != null) {
			outState.putInt(KEY_CURRENT_POS, this.currentPos);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		setLastListPos(this.listPos);
		if (mImagePreviewDialog != null && mImagePreviewDialog.isShowing()) {
			mImagePreviewDialog.dismiss();
		}
		mImagePreviewDialog = null;
	}

	@Override
	protected void onStop() {
		super.onStop();
		setLastListPos(this.listPos);
		if (mImagePreviewDialog != null && mImagePreviewDialog.isShowing()) {
			mImagePreviewDialog.dismiss();
		}
		mImagePreviewDialog = null;
		if (this.isFinishing()) {
			isStop.set(1);
		}
	}
	
    private void openImageDialog(String filename) {
		if (mImagePreviewDialog != null && !mImagePreviewDialog.isShowing()) {
			mImagePreviewDialog.dismiss();
			mImagePreviewDialog = null;
		}
		mImagePreviewDialog = new ImagePreviewDialog(this, filename);
		mImagePreviewDialog.show();
    }
	    
	private class LoadDataTask extends AsyncTask<Void, Void, Boolean> {
		private boolean loadResult = false;
		private ArrayList<JkanjiGalleryHistoryItem> items;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			listPos = getLastListPos();
			listViewShelf.setVisibility(View.INVISIBLE);
			textViewLoading.setVisibility(View.VISIBLE);
			adapter.clear();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				int sortType = getLastSortType();
				JkanjiGalleryHistoryDataSource dataSource = new JkanjiGalleryHistoryDataSource(JkanjiGalleryHistoryActivity.this);
				dataSource.open();
				items = dataSource.getAllItems();
				switch (sortType) {
				case SORT_TYPE_MODIFY:
					Collections.sort(items, new Comparator<JkanjiGalleryHistoryItem>() {
						@Override
						public int compare(JkanjiGalleryHistoryItem lhs, JkanjiGalleryHistoryItem rhs) {
							if (lhs == null) {
								return -1;
							} else if (rhs == null) {
								return 1;
							} else {
								Time time1 = lhs.getPlainTime();
								Time time2 = rhs.getPlainTime();
								if (time1 == null) {
									return -1;
								} else if (time2 == null) {
									return 1;
								} else {
									return Time.compare(time1, time2);
								}
							}
						}
					});
					break;
				
				case SORT_TYPE_CREATE:
				default:
					break;
				}
				dataSource.close();
				
				loadResult = true;				
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			listViewShelf.setVisibility(View.VISIBLE);
			textViewLoading.setVisibility(View.INVISIBLE);
			if (result == true && !JkanjiGalleryHistoryActivity.this.isFinishing()) {
				if (loadResult) {
					if (items != null) {
						for (int i = items.size() - 1; i >= 0; i--) {
							adapter.add(items.get(i));
						}
					}
					adapter.notifyDataSetChanged();
					listViewShelf.setSelection(listPos);
					setLastListPos(listPos);
					
					if (JkanjiGallerySettingActivity.getShowThumb(JkanjiGalleryHistoryActivity.this)) {
						new LoadThumbTask().execute();
			    	} else {
						if (mThumbnails != null) {
							for (int i = 0; i < mThumbnails.length; i++) {
								if (mThumbnails[i] != null && !mThumbnails[i].isRecycled()) {
									mThumbnails[i].recycle();
									mThumbnails[i] = null;
								}
							}
						}
						mLoadDataTask = null;
			    	}
				} else {
					mLoadDataTask = null;
				}
			} else if (result == false) {
				finish();
				mLoadDataTask = null;
				Toast.makeText(JkanjiGalleryHistoryActivity.this, "加载历史记录失败", Toast.LENGTH_SHORT).show();
			}
		}
    }
    
    
	private class LoadThumbTask extends AsyncTask<Void, Integer, Boolean> {
		private final static float PERCENT = 0.8f;
		
		private boolean loadResult = false;
		private int count;
		private int imageThumbSize = 100;
		private Bitmap[] oldThumbnails;
		private int firstLoad = -1;
		private boolean use16BitsThumb = false;
		private boolean[] loadSkip;
		private JkanjiGalleryHistoryItem[] items;
		private long totalsize = 0;
		
		private boolean isLoadSkip(int pos) {
			if (loadSkip == null) {
				return false;
			}
			if (pos < 0 || pos >= loadSkip.length) {
				return false;
			}
			return loadSkip[pos];
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			try {
				imageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			count = adapter.getCount();
			oldThumbnails = mThumbnails;
			mThumbnails = new Bitmap[count];
			loadSkip = new boolean[count];
			actionBar.setTitle("加载中");
			use16BitsThumb = JkanjiGallerySettingActivity.getUse16BitsThumb(JkanjiGalleryHistoryActivity.this);
			
			isStop.set(0);
			
			items = new JkanjiGalleryHistoryItem[count];
			for (int i = 0; i < count; i++) {
				items[i] = adapter.getItem(i);
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
//			try {
//				Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
//			} catch (Throwable e) {
//				e.printStackTrace();
//			}
			try {
				if (oldThumbnails != null) {
					for (int i = 0; i < oldThumbnails.length; i++) {
						if (oldThumbnails[i] != null && !oldThumbnails[i].isRecycled()) {
							oldThumbnails[i].recycle();
							oldThumbnails[i] = null;
						}
					}
				}
				this.publishProgress(0, count, 0);
				totalsize = 0;
				int loadnum = 0;
				while (true) {
					if (isStop.get() == 1) {
						break;
					}
//					if (!listViewShelfIdle) {
//						continue;
//					}
					int position = -1;
					if (firstLoad >= 0 && mThumbnails != null && mThumbnails[firstLoad] == null && !isLoadSkip(firstLoad)) {
						position = firstLoad;
					} else {
						for (int i = 0; i < count; i++) {
							if (mThumbnails != null && mThumbnails[i] == null && !isLoadSkip(i)) {
								position = i;
								break;
							}
						}
					}
					if (D) {
						Log.e(TAG, "position == " + position);
					}
					if (position < 0) {
						break;
					}
					JkanjiGalleryHistoryItem item = null;
					//item = adapter.getItem(position);
					if (position >= 0 && position < items.length) {
						item = items[position];
					}
					if (item != null) {
						String filename = item.getPlainFileName();
						String pathname = item.getPlainPathName();
						if (filename != null && pathname != null) {
							File file = new File(pathname, filename);
							if (file.exists() && file.canRead()) {
								if (useThumbContentProvider) {
									mThumbnails[position] = ThumbsHelper.decode(JkanjiGalleryHistoryActivity.this, file.getAbsolutePath(), imageThumbSize, imageThumbSize, use16BitsThumb, true);
								} else {
									mThumbnails[position] = decodeSampledBitmapFromFile(file.getAbsolutePath(), imageThumbSize, imageThumbSize, use16BitsThumb);
								}
								if (mThumbnails[position] == null) {
									loadSkip[position] = true;
								}
								Bitmap bitmap = mThumbnails[position];
								if (bitmap != null) {
									totalsize += bitmap.getRowBytes() * bitmap.getHeight();
								}
								if (D) {
									Log.e(TAG, "position == " + position + ", totalsize == " + totalsize + 
										", max == " + Runtime.getRuntime().maxMemory() * PERCENT);
								}
							} else {
								loadSkip[position] = true;
							}
						}
					}
					loadnum++;
					this.publishProgress(position + 1, count, loadnum);
					if (totalsize > Runtime.getRuntime().maxMemory() * PERCENT) {
						break;
					}
				}
				loadResult = true;				
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (totalsize > Runtime.getRuntime().maxMemory() * PERCENT) {
				Toast.makeText(JkanjiGalleryHistoryActivity.this, 
					"内存不足，请关闭缩略图或减少图库记录", 
					Toast.LENGTH_SHORT).show();
			}
			actionBar.setTitle("" + values[2] + "/" + values[1]);
			if (false) {
				adapter.notifyDataSetChanged();
			} else {
				final int position = values[0] - 1;
				final int total = values[1];
				final ImageView ivIcon = (ImageView) listViewShelf.findViewWithTag(ICONTAG + position);
				if (ivIcon != null && 
					mThumbnails != null &&
					position >= 0 &&
					mThumbnails[position] != null && 
					!mThumbnails[position].isRecycled()) {
					ivIcon.setImageBitmap(mThumbnails[position]);
				}
				int fp = listViewShelf.getFirstVisiblePosition();
				int lp = listViewShelf.getLastVisiblePosition();
				this.firstLoad = -1;
				if (mThumbnails != null) {
					for (int i = 0; i < mThumbnails.length; i++) {
						if (i >= fp && i <= lp && mThumbnails[i] == null && !isLoadSkip(i)) {
							this.firstLoad = i;
							break;
						}
					}
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			actionBar.setTitle("图库");
			if (result == true && !JkanjiGalleryHistoryActivity.this.isFinishing()) {
				if (loadResult) {
					adapter.notifyDataSetChanged();
				} else {
					
				}
			} else if (result == false) {
				Toast.makeText(JkanjiGalleryHistoryActivity.this, "加载缩略图失败，可能是因为内存不足", Toast.LENGTH_SHORT).show();
//				finish();
			}
			if (mLoadDataTask != null) {
				mLoadDataTask = null;
			} else {
				if (!JkanjiGalleryHistoryActivity.this.isFinishing()) {
					mLoadDataTask = new LoadDataTask();
					mLoadDataTask.execute();
				}
			}
		}
		
		private Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight, boolean isUse16Bits) {
	    	final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeFile(filename, options);
	        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	        if (D) {
	        	Log.e(TAG, "options.inSampleSize == " + options.inSampleSize + "," + reqWidth + "," + reqHeight);
	        }
	        options.inJustDecodeBounds = false;
			if (isUse16Bits) { 
				options.inPreferredConfig = Bitmap.Config.RGB_565;   
				options.inPurgeable = true;  
				options.inInputShareable = true;  
			}
	        return BitmapFactory.decodeFile(filename, options);
		}
	    
	    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	        final int height = options.outHeight;
	        final int width = options.outWidth;
	        int inSampleSize = 1;
	        if (height > reqHeight || width > reqWidth) {
	            if (width > height) {
	                inSampleSize = Math.round((float) height / (float) reqHeight);
	            } else {
	                inSampleSize = Math.round((float) width / (float) reqWidth);
	            }
	            final float totalPixels = width * height;
	            final float totalReqPixelsCap = reqWidth * reqHeight * 2;
	            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
	                inSampleSize++;
	            }
	        }
	        return inSampleSize;
	    }
    }
}
