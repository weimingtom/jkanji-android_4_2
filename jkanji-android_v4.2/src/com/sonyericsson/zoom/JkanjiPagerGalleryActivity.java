package com.sonyericsson.zoom;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elgubbo.sharetoclipboard.ShareToClipboardActivity;
import com.iteye.weimingtom.jkanji.JKanjiActivity;
import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.jkanji.SQLiteReaderActivity;
import com.markupartist.android.widget.ActionBar;
import com.sonyericsson.zoom.ImagePagerView;

public class JkanjiPagerGalleryActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "JkanjiPagerGalleryActivity";
	
	/**
	 * 如果EXTRA_KEY_FILEID < 0 
	 * 则尝试根据EXTRA_KEY_FILENAME寻找fileid
	 */
	public static final String EXTRA_KEY_PATH = "JkanjiPagerGalleryActivity.EXTRA_KEY_PATH";
	public static final String EXTRA_KEY_FILEID = "JkanjiPagerGalleryActivity.EXTRA_KEY_FILEID";
	public static final String EXTRA_KEY_FILENAME = "JkanjiPagerGalleryActivity.EXTRA_KEY_FILENAME";
	public static final String EXTRA_KEY_ID = "JkanjiPagerGalleryActivity.EXTRA_KEY_ID";
	public static final String EXTRA_KEY_ZOOM = "JkanjiPagerGalleryActivity.EXTRA_KEY_ZOOM";
	public static final String EXTRA_KEY_PANX = "JkanjiPagerGalleryActivity.EXTRA_KEY_PANX";
	public static final String EXTRA_KEY_PANY = "JkanjiPagerGalleryActivity.EXTRA_KEY_PANY";
	public static final String EXTRA_KEY_MULTI = "JkanjiPagerGalleryActivity.EXTRA_KEY_MULTI";
	public static final String EXTRA_KEY_DESC = "JkanjiPagerGalleryActivity.EXTRA_KEY_DESC";
	public static final String EXTRA_KEY_ISRECORD = "JkanjiPagerGalleryActivity.EXTRA_KEY_ISRECORD";
	
	public static final String DEFAULT_PATH = "/mnt/sdcard/images/4";
	
	private static final int MENU_ID_LEFT = Menu.FIRST + 0;
	private static final int MENU_ID_SEARCH = Menu.FIRST + 1;
	private static final int MENU_ID_RIGHT = Menu.FIRST + 2;
	private static final int MENU_ID_SAVE = Menu.FIRST + 3;
	private static final int MENU_ID_MEMO = Menu.FIRST + 4;
	private static final int MENU_ID_SQLITE = Menu.FIRST + 5;
	//
	private static final int MENU_ID_PAGE = Menu.FIRST + 6;
	private static final int MENU_ID_RESET = Menu.FIRST + 7;
//    private static final int MENU_ID_MULTI = Menu.FIRST + 8;    
//    private static final int MENU_ID_PAN = Menu.FIRST + 9;
//    private static final int MENU_ID_ZOOM = Menu.FIRST + 10;
    private static final int MENU_ID_EXIT = Menu.FIRST + 11;
    private static final int MENU_ID_PREVIEW = Menu.FIRST + 12;
    private static final int MENU_ID_SHARE_PHOTO = Menu.FIRST + 13;
    private static final int MENU_ID_SIZE = Menu.FIRST + 14;
//    private static final int MENU_ID_USE_FADE = Menu.FIRST + 15;
//    private static final int MENU_ID_RESIZE_WIDTH = Menu.FIRST + 16;
    private static final int MENU_ID_CHANGE_ORI_LAND = Menu.FIRST + 17;
    private static final int MENU_ID_CHANGE_ORI_PORT = Menu.FIRST + 18;
    private static final int MENU_ID_CHANGE_FULLSCREEN = Menu.FIRST + 19;
    private static final int MENU_ID_CHANGE_NOT_FULLSCREEN = Menu.FIRST + 20;
    
    private static final int DIALOG_TEXT_ENTRY = 1;
	private AlertDialog.Builder builder1;
	private AlertDialog pageDialog;
	private TextView textViewInfo;
	private EditText editPage;
	private static final int DIALOG_SIZE = 2;
	private AlertDialog.Builder builder2;
	private AlertDialog dialogSize;
	
    private ActionBar actionBar;
    private LinearLayout linearLayoutContent;
//    private ImagePagerView mZoomView;// mZoomViewBack;
    private HackyViewPager viewPager;
    private Bitmap mBitmap, mBitmapBack;
    
    private volatile String path;
    private volatile int fileid;
    private volatile String[] files;
    private volatile String currentFileName;
    private volatile long mId = -1L;
    private volatile String desc;
    private volatile boolean isRecord = true;
    
    private Toast toastFirst, toastNull, toastLast;
    
    private TextView textViewLoading;
    
    private volatile float oriZoom = 1.0f;
    private volatile float oriPanX = 0.5f;
    private volatile float oriPanY = 0.5f;
    private volatile boolean oriEnableMulti = true;
    private volatile String oriFilename = null;
    
    private final class SaveData {
    	public String path;
    	public int fileid;
    	public float zoom, panX, panY;
    	public long id = -1L;
    	public boolean enableMulti = true;
    	public String desc;
    	public boolean isRecord = true;
    }
    
    private volatile boolean isSortFilenameNum = false;
    
    private ImageGallery2Dialog mGalleryDialog;
    
    private SamplePagerAdapter mAdapter = new SamplePagerAdapter();
    private OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
		@Override
		public void onPageScrollStateChanged(int state) {
			
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			
		}

		@Override
		public void onPageSelected(int position) {
			fileid = position;
			if (files != null) {
				currentFileName = files[fileid];
				actionBar.setTitle("" + (position + 1) + "/" + files.length);
				
				loadPage(position);
				
			} else {
				actionBar.setTitle("" + (position + 1));
			}
		}
    };
    
    private void loadPage(int position) {
		ImagePagerView zoomView = (ImagePagerView)viewPager.findViewWithTag(Integer.toString(position));
		if (zoomView != null) {
			zoomView.reset(path, files[position], 1.0f, 0.5f, 0.5f, true);
			if (D) {
				Log.e(TAG, "onPageSelected reset " + position);
			}
		} else {
			if (D) {
				Log.e(TAG, "onPageSelected reset failed " + position);
			}
		}
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideTitle();
        setContentView(R.layout.pager_gallery_view);
        linearLayoutContent = (LinearLayout) this.findViewById(R.id.linearLayoutContent);
        viewPager = (HackyViewPager) findViewById(R.id.viewPager);
        actionBar = (ActionBar) findViewById(R.id.actionbar);
        textViewLoading = (TextView) findViewById(R.id.textViewLoading);
        viewPager.setAdapter(mAdapter);
        viewPager.setPagingEnabled(true);
        /**
         * @see https://developer.android.com/reference/android/support/v4/view/ViewPager.OnPageChangeListener.html
         */
//        viewPager.setOnPageChangeListener(onPageChangeListener);
        
        isSortFilenameNum = JkanjiGallerySettingActivity.getSortFilenameNum(this);

        builder1 = new AlertDialog.Builder(this);
        builder2 = new AlertDialog.Builder(this);
        
    	toastFirst = Toast.makeText(this, 
	    		"已经是第一页", 
	    		Toast.LENGTH_SHORT);
    	toastNull = Toast.makeText(this, 
	    		"没有图片后缀文件", 
	    		Toast.LENGTH_SHORT);
    	toastLast = Toast.makeText(this, 
	    		"已经是最后一页",
	    		Toast.LENGTH_SHORT);
	    		
        actionBar.setTitle("图库查看器");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
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
				return R.drawable.del;
			}

			@Override
			public void performAction(View view) {
				prevPage();
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.forward;
			}

			@Override
			public void performAction(View view) {
				nextPage();
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.config;
			}

			@Override
			public void performAction(View view) {
				openOptionsMenu();
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
//				return R.drawable.jump;
				return R.drawable.search_sqlite;
			}

			@Override
			public void performAction(View view) {
//				preview();
				Intent intent = new Intent(JkanjiPagerGalleryActivity.this, SQLiteReaderActivity.class);
				startActivity(intent);
			}
        });
        
        if (JkanjiGallerySettingActivity.getShowAB(this)) {
        	actionBar.setVisibility(View.VISIBLE);
        } else {
        	actionBar.setVisibility(View.GONE);
        }
        
        SaveData saveData = (SaveData)this.getLastNonConfigurationInstance();
        if (saveData == null) {
	        Intent intent = this.getIntent();
	        if (intent != null) {
	        	path = intent.getStringExtra(EXTRA_KEY_PATH);
	        	fileid = intent.getIntExtra(EXTRA_KEY_FILEID, 0);
	        	oriFilename = intent.getStringExtra(EXTRA_KEY_FILENAME);
	        	mId = intent.getLongExtra(EXTRA_KEY_ID, -1L);
	        	oriZoom = intent.getFloatExtra(EXTRA_KEY_ZOOM, 1.0f);
	        	oriPanX = intent.getFloatExtra(EXTRA_KEY_PANX, 0.5f);
	        	oriPanY = intent.getFloatExtra(EXTRA_KEY_PANY, 0.5f);
	        	oriEnableMulti = intent.getBooleanExtra(EXTRA_KEY_MULTI, true);
	        	desc = intent.getStringExtra(EXTRA_KEY_DESC);
	        	isRecord = intent.getBooleanExtra(EXTRA_KEY_ISRECORD, true);
	        } else {
	        	oriZoom = 1.0f;
	        	oriPanX = 0.5f;
	        	oriPanY = 0.5f;
	        	oriEnableMulti = true;
	        	isRecord = true;
	        }
    	} else {
    		path = saveData.path;
    		fileid = saveData.fileid;
    		oriZoom = saveData.zoom;
    		oriPanX = saveData.panX;
    		oriPanY = saveData.panY;
    		mId = saveData.id;
    		oriEnableMulti = saveData.enableMulti;
    		desc = saveData.desc;
    		isRecord = saveData.isRecord;
    	}
//		if (!isRecord) {
//			Toast.makeText(this, "不记录到图库", Toast.LENGTH_SHORT).show();
//		}
        if (path == null) {
        	path = DEFAULT_PATH;
        }
        
        if (path != null) {
        	if (D) {
            	Log.d(TAG, "path = " + path);
            }
        	sortFiles(oriFilename);
        } else {
	    	Toast.makeText(JkanjiPagerGalleryActivity.this, 
			    	"没有指定目录或没有图片文件", 
			    	Toast.LENGTH_SHORT).show();
    		files = null;
    		fileid = 0;
    	}
        
        if (!this.isRecord) {
        	setResult(RESULT_CANCELED);
        } else {
        	setResult(RESULT_OK);
        }
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		if (this.isRecord) {
			saveGallery(mId);
		} else {
//			if (isFinishing()) {
//				Toast.makeText(this, "没有记录到图库", Toast.LENGTH_SHORT).show();
//			}
		}
		if (mGalleryDialog != null && mGalleryDialog.isShowing()) {
			mGalleryDialog.dismiss();
		} 
		mGalleryDialog = null;
		if (D) {
			Log.e(TAG, "onPause");
		}
	}
    
	@Override
	protected void onStop() {
		super.onStop();
		if (mGalleryDialog != null && mGalleryDialog.isShowing()) {
			mGalleryDialog.dismiss();
		} 
		mGalleryDialog = null;
		if (D) {
			Log.e(TAG, "onStop");
		}
	}

	@Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmap != null && !mBitmap.isRecycled()) {
        	mBitmap.recycle();
        	mBitmap = null;
        }
        if (mBitmapBack != null && !mBitmapBack.isRecycled()) {
        	mBitmapBack.recycle();
        	mBitmapBack = null;
        }
		if (mGalleryDialog != null && mGalleryDialog.isShowing()) {
			mGalleryDialog.dismiss();
		} 
		mGalleryDialog = null;
		if (D) {
			Log.e(TAG, "onDestroy");
		}
    }

    @Override
	public void onBackPressed() {
		// super.onBackPressed();
    	if (isRecord) {
    		this.openOptionsMenu();
    	} else {
    		super.onBackPressed();
    	}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
    	ImagePagerView zoomView = null;
    	try {
    		zoomView = (ImagePagerView) viewPager.findViewById(viewPager.getCurrentItem());
		} catch (Throwable e) {
			e.printStackTrace();
		}
    	SaveData saveData = new SaveData();
    	saveData.fileid = fileid;
    	saveData.path = path;
    	saveData.zoom = zoomView != null ? zoomView.getZoom() : 1.0f;
    	saveData.panX = zoomView != null ? zoomView.getPanX() : 0.5f;
    	saveData.panY = zoomView != null ? zoomView.getPanY() : 0.5f;
    	saveData.id = mId;
    	saveData.enableMulti = zoomView != null ? zoomView.getEnableMultiTouch() : true;
    	saveData.desc = desc;
    	saveData.isRecord = isRecord;
		return saveData;
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, MENU_ID_LEFT, 0, "上一张").setIcon(android.R.drawable.ic_media_previous);
        menu.add(Menu.NONE, MENU_ID_SEARCH, 0, "字典").setIcon(android.R.drawable.ic_menu_search);
        menu.add(Menu.NONE, MENU_ID_RIGHT, 0, "下一张").setIcon(android.R.drawable.ic_media_next);
        menu.add(Menu.NONE, MENU_ID_PREVIEW, 0, "预览").setIcon(android.R.drawable.ic_menu_gallery);
        menu.add(Menu.NONE, MENU_ID_SQLITE, 0, "sqlite搜索").setIcon(android.R.drawable.ic_menu_search);
        menu.add(Menu.NONE, MENU_ID_MEMO, 0, "备忘录").setIcon(android.R.drawable.ic_menu_view);
        menu.add(Menu.NONE, MENU_ID_SAVE, 0, "另存至图库").setIcon(android.R.drawable.ic_menu_save);
        menu.add(Menu.NONE, MENU_ID_PAGE, 0, "跳转至"); 
        menu.add(Menu.NONE, MENU_ID_RESET, 0, "重置"); 
//        menu.add(Menu.NONE, MENU_ID_MULTI, 0, "单双指切换");
//        menu.add(Menu.NONE, MENU_ID_PAN, 0, "单指平移模式");
//        menu.add(Menu.NONE, MENU_ID_ZOOM, 0, "单指缩放模式");
        menu.add(Menu.NONE, MENU_ID_SHARE_PHOTO, 0, "共享图片");
        menu.add(Menu.NONE, MENU_ID_SIZE, 0, "图像大小信息");
//        menu.add(Menu.NONE, MENU_ID_USE_FADE, 0, "切换是否允许淡入");
//        menu.add(Menu.NONE, MENU_ID_RESIZE_WIDTH, 0, "适应宽度");
        menu.add(Menu.NONE, MENU_ID_CHANGE_ORI_LAND, 0, "手动横屏");
        menu.add(Menu.NONE, MENU_ID_CHANGE_ORI_PORT, 0, "手动竖屏");
        menu.add(Menu.NONE, MENU_ID_CHANGE_FULLSCREEN, 0, "手动全屏");
        menu.add(Menu.NONE, MENU_ID_CHANGE_NOT_FULLSCREEN, 0, "显示动作栏和状态栏");
        menu.add(Menu.NONE, MENU_ID_EXIT, 0, "退出");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ID_LEFT:
        	prevPage();
        	break;
        	
        case MENU_ID_SEARCH:
        	startActivity(new Intent(this, JKanjiActivity.class));
        	break;
        	
        case MENU_ID_RIGHT:
        	nextPage();
        	break;
        
        case MENU_ID_SAVE:
        	saveGallery(-1L);
        	Toast.makeText(this, 
        		"保存至图库", 
        		Toast.LENGTH_SHORT).show();
        	break;
        	
        case MENU_ID_SQLITE:
        	startActivity(new Intent(this, SQLiteReaderActivity.class));
        	break;
        	
        case MENU_ID_MEMO:
			startActivity(new Intent(this, ShareToClipboardActivity.class));
        	break;
        
        case MENU_ID_PREVIEW:
        	preview();
        	break;
        	
        case MENU_ID_PAGE:
        	this.showDialog(DIALOG_TEXT_ENTRY);
        	break;
        	
        case MENU_ID_RESET:
	        {
	        	ImagePagerView zoomView = null;
	        	try {
	        		zoomView = (ImagePagerView) viewPager.findViewById(viewPager.getCurrentItem());
				} catch (Throwable e) {
					e.printStackTrace();
				}
	        	if (zoomView != null) {
	        		zoomView.reset(this.path, this.currentFileName, 1.0f, 0.5f, 0.5f, true);
	        	}
	        }
            break;
            
        case MENU_ID_SHARE_PHOTO:
        	sharePhoto();
        	break;
            
        case MENU_ID_SIZE:
        	showDialog(DIALOG_SIZE);
        	break;
        	
//        case MENU_ID_RESIZE_WIDTH:
//        	{
//        		ImagePagerView zoomView = null;
//	        	try {
//	        		zoomView = (ImagePagerView) viewPager.findViewById(viewPager.getCurrentItem());
//				} catch (Throwable e) {
//					e.printStackTrace();
//				}
//	        	if (zoomView != null) {
//	        		zoomView.resizeWidth();
//	        	}
//        	}
//        	break;

        case MENU_ID_CHANGE_ORI_LAND:
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    		break;
    	
        case MENU_ID_CHANGE_ORI_PORT:
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        	break;
        	
        case MENU_ID_CHANGE_FULLSCREEN:
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        	actionBar.setVisibility(View.GONE);
        	linearLayoutContent.requestLayout();
        	break;
    	
        case MENU_ID_CHANGE_NOT_FULLSCREEN:
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        	actionBar.setVisibility(View.VISIBLE);
        	linearLayoutContent.requestLayout();
        	break;
        	
        case MENU_ID_EXIT:
        	finish();
        	break;
        }
        return super.onOptionsItemSelected(item);
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode) {
		case KeyEvent.KEYCODE_SEARCH:
			this.startActivity(new Intent(this, JKanjiActivity.class));
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

    private static Point getBitmapSize(String filename) {
    	final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);
        return new Point(options.outWidth, options.outHeight);
    }
    
    private void setPage(int id) {
    	if (files != null && files.length > 0) {
	    	fileid = id;
	    	if (fileid < 0) {
	    		fileid = 0;
	    		if (toastFirst != null) {
	    			toastFirst.show();
	    		}
	    	} else if (fileid >= files.length){
	    		fileid = files.length - 1;
	    		if (toastLast != null) {
	    			toastLast.show();
	    		}
	    	}
    		this.currentFileName = files[fileid];
    		this.viewPager.setCurrentItem(fileid, true);
	    } else {
	    	if (toastNull != null) {
	    		toastNull.show();
	    	}
	    }
    }
    
    private void prevPage() {
    	if (files != null && files.length > 0) {
	    	fileid--;
	    	if (fileid < 0) {
	    		fileid = 0;
	    		if (toastFirst != null) {
	    			toastFirst.show();
	    		}
	    	} else {
	    		this.currentFileName = files[fileid];
	    		this.viewPager.setCurrentItem(fileid, true);
	    	}
	    } else {
	    	if (toastNull != null) {
	    		toastNull.show();
	    	}
	    }
    }
    
    private void nextPage() {
    	if (files != null && files.length > 0) {
	    	fileid++;
	    	if (fileid >= files.length) {
	    		fileid = files.length - 1;
	    		if (toastLast != null) {
	    			toastLast.show();
	    		}
	    	} else {
	    		this.currentFileName = files[fileid];
	    		this.viewPager.setCurrentItem(fileid, true);
	    	}
	    } else {
    		if (toastNull != null) {
    			toastNull.show();
    		}
	    }
    }
    
    private void saveGallery(long id) {
    	ImagePagerView zoomView = null;
    	try {
    		zoomView = (ImagePagerView) viewPager.findViewById(viewPager.getCurrentItem());
		} catch (Throwable e) {
			e.printStackTrace();
		}
    	if (files != null && files.length > 0) {
    		int plainPage = fileid;
    		int plainTotalPage = files.length;
	    	JkanjiGalleryHistoryDataSource dataSrc = new JkanjiGalleryHistoryDataSource(this);
	    	dataSrc.open();
	    	JkanjiGalleryHistoryItem item = new JkanjiGalleryHistoryItem();
	    	item.setId(id);
	    	item.setPlainZoom(1.0f/*zoomView != null ? zoomView.getZoom() : 1.0f*/);
	    	item.setPlainPanX(0.5f/*zoomView != null ? zoomView.getPanX() : 0.5f*/);
	    	item.setPlainPanY(0.5f/*zoomView != null ? zoomView.getPanY() : 0.5f*/);
	    	item.setPlainPage(plainPage);
	    	item.setPlainTotalPage(plainTotalPage);
	    	if (fileid >= 0 && fileid < files.length) {
	    		item.setPlainFileName(files[fileid]);
	    	} else {
	    		item.setPlainFileName("");
	    	}
	    	item.setPlainPathName(this.path);
	    	item.setPlainEnableMulti(true /*zoomView != null ? zoomView.getEnableMultiTouch() : true*/);
	    	item.setPlainDesc(this.desc);
	    	mId = dataSrc.createItem(item);
	    	dataSrc.close();
	    	setResult(RESULT_OK);
    	} else {
    		if (toastNull != null) {
    			toastNull.show();
    		}
    	}
    }
    
    /**
     * @see http://embed.e800.com.cn/articles/2011/217/1297910848825_1.html
     */
    public void hideTitle() {
    	if (JkanjiGallerySettingActivity.getFullScreen(this)) {
    		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	}
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//    	switch (JkanjiGallerySettingActivity.getScreenOri(this)) {
//    	case JkanjiGallerySettingActivity.SCREEN_ORI_PORT:
//        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//    		break;
//    		
//    	case JkanjiGallerySettingActivity.SCREEN_ORI_LAND:
//        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//    		break;
//    		
//    	case JkanjiGallerySettingActivity.SCREEN_ORI_DEFAULT:
//    	default:
//    		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
//    		break;
//    	}
    }
    
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_TEXT_ENTRY:
			LayoutInflater factory = LayoutInflater.from(this);
            View textEntryView = factory.inflate(R.layout.set_page_dialog, null);
            textViewInfo = (TextView) textEntryView.findViewById(R.id.textViewInfo);
            editPage = (EditText) textEntryView.findViewById(R.id.editPage);
            pageDialog = builder1
            	.setTitle("指定页数")
                .setView(textEntryView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	try {
                    		String str = editPage.getText().toString();
                    		int pageId = Integer.parseInt(str) - 1;
                    		setPage(pageId);
                    	} catch (Throwable e) {
                    		e.printStackTrace();
                    	}
                    }
                })
                .setNeutralButton("第1页", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	setPage(0);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
                    }
                })
                .create();
            pageDialog.setOnShowListener(new OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					if (files != null) {
						textViewInfo.setText("指定页数(1-" + files.length + ")：");
						editPage.setEnabled(true);
						editPage.setText("");
			            editPage.append(Integer.toString(fileid + 1));
					} else {
						textViewInfo.setText("指定页数：");
						editPage.setEnabled(false);
					}
				}
            });
            return pageDialog;
		
		case DIALOG_SIZE:
			dialogSize = builder2
				.setTitle("图像大小")
	            .setMessage("图像大小")
	            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
                    }
                })
	            .create();
			dialogSize.setOnShowListener(new OnShowListener() {
				@Override
				public void onShow(DialogInterface dialog) {
					if (dialogSize != null) {
						dialogSize.setMessage(getSizeInfo());
					}
				}
			});
			return dialogSize;
		}
		return super.onCreateDialog(id);
	}
    
	private String getSizeInfo() {
    	if (path != null && currentFileName != null) {
    		File bitmapFile = new File(path, currentFileName);
    		String info = "";
    		if (bitmapFile.getAbsolutePath() != null) {
    			info = "图像路径：" + bitmapFile.getAbsolutePath();
    			Point point = getBitmapSize(bitmapFile.getAbsolutePath());
    			info = info + "\n" + "图像大小：" + point.x + " x " + point.y;
    		} else {
    			info = "图像路径未知或不是文件";
    		}
    		return info;
    	} else {
    		return "当前图片目录或文件名为空";
    	}
	}
	
    private void openGalleryDialog(String filename) {
		if (mGalleryDialog != null && !mGalleryDialog.isShowing()) {
			mGalleryDialog.dismiss();
			mGalleryDialog = null;
		}
		mGalleryDialog = new ImageGallery2Dialog(this, filename, this.isSortFilenameNum, files);
		mGalleryDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (mGalleryDialog != null) {
					String filename = mGalleryDialog.getSelectedFilename();
					int pos = mGalleryDialog.getSelectedPos();
					if (filename != null) {
						setPage(pos);
					}
				}
			}
        });
		mGalleryDialog.show();
    }
	
    private void preview() {
    	if (path != null && currentFileName != null) {
    		File bitmapFile = new File(path, currentFileName);
    		if (bitmapFile.isFile() && bitmapFile.exists() && bitmapFile.canRead()) {
    			openGalleryDialog(bitmapFile.getAbsolutePath());
    		} else {
    			Toast.makeText(this, "当前图片不存在或不可读", Toast.LENGTH_SHORT).show();
    		}
    	} else {
    		Toast.makeText(this, "当前图片目录或文件名为空", Toast.LENGTH_SHORT).show();
    	}
    }
    
	private void sharePhoto() {
    	if (path != null && currentFileName != null) {
    		File bitmapFile = new File(path, currentFileName);
    		Intent intent;
			intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("image/*");
			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(bitmapFile));
            try {
            	//startActivity(Intent.createChooser(intent, "共享方式"));
            	startActivity(intent);
            } catch (Throwable e) {
				e.printStackTrace();
				Toast.makeText(this, 
					"共享方式出错", Toast.LENGTH_SHORT)
					.show();
			}
    	} else {
    		Toast.makeText(this, "当前图片目录或文件名为空", Toast.LENGTH_SHORT).show();
    	}
	}
	
	
	
		
    private void sortFiles2(String oriFilename, final float zoom, final float panX, final float panY, final boolean enableMulti) {
    	File pathFile = new File(path);
    	files = pathFile.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if (filename != null && 
					(filename.toLowerCase().endsWith(".jpg") ||
					filename.toLowerCase().endsWith(".jpeg") ||
					filename.toLowerCase().endsWith(".png") ||
					filename.toLowerCase().endsWith(".gif"))) {
					return true;
				} else {
					return false;
				}
			}
    	});
    	if (D && files != null) {
    		for (int i = 0; i < files.length; i++) {
    			Log.d(TAG, "files[" + i + "] = " + files[i]);
    		}
    	}
    	if (files != null && files.length > 0) {
    		Arrays.sort(files, new Comparator<String>() {
				@Override
				public int compare(String lhs, String rhs) {
					if (lhs == null) {
						return -1;
					} else if (rhs == null) {
						return 1;
					} else {
						if (isSortFilenameNum) {
							return FileNameCompare.compareParts(lhs, rhs);
						} else {
							return lhs.compareToIgnoreCase(rhs);
						}
					}
				}
    		});
    		if (fileid < 0 || fileid >= files.length) {
    			if (oriFilename != null) {
    				boolean findId = false;
    				for (int i = 0; i < files.length; i++) {
    					if (files[i] != null && oriFilename.equals(files[i])) {
    						fileid = i;
    						findId = true;
    						break;
    					}
    				}
    				if (!findId) {
    					fileid = 0;
    				}
    			} else {
    				fileid = 0;
    			}
    		}
    		this.currentFileName = files[fileid];
        	ImagePagerView zoomView = null;
        	try {
        		zoomView = (ImagePagerView) viewPager.findViewById(viewPager.getCurrentItem());
			} catch (Throwable e) {
				e.printStackTrace();
			}
    		if (zoomView != null) {
    			if (zoomView != null) {
					zoomView.reset(path, currentFileName, 1.0f, 0.5f, 0.5f, true);
				}
	    	}
    	} else {
	    	Toast.makeText(this, 
			    	"没有指定目录或没有图片文件", 
			    	Toast.LENGTH_SHORT).show();
    		files = null;
    		fileid = 0;
    	}
    }
	
	
    private void sortFiles(String oriFilename) {
    	new SortTask().execute(oriFilename);
    }
    
	private class SortTask extends AsyncTask<String, Void, Boolean> {
		private boolean loadResult = false;
		
		private String oriFilename;
		private String[] files_temp;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			actionBar.setTitle("排序中...");
			textViewLoading.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				oriFilename = params[0];

				File pathFile = new File(path);
				files_temp = pathFile.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String filename) {
						if (filename != null && 
							(filename.toLowerCase().endsWith(".jpg") ||
							filename.toLowerCase().endsWith(".jpeg") ||
							filename.toLowerCase().endsWith(".png") ||
							filename.toLowerCase().endsWith(".gif"))) {
							return true;
						} else {
							return false;
						}
					}
		    	});
		    	if (D && files_temp != null) {
		    		for (int i = 0; i < files_temp.length; i++) {
		    			Log.d(TAG, "files_temp[" + i + "] = " + files_temp[i]);
		    		}
		    	}
		    	if (files_temp != null && files_temp.length > 0) {
		    		Arrays.sort(files_temp, new Comparator<String>() {
						@Override
						public int compare(String lhs, String rhs) {
							if (lhs == null) {
								return -1;
							} else if (rhs == null) {
								return 1;
							} else {
								if (isSortFilenameNum) {
									return FileNameCompare.compareParts(lhs, rhs);
								} else {
									return lhs.compareToIgnoreCase(rhs);
								}
							}
						}
		    		});
		    		if (fileid < 0 || fileid >= files_temp.length) {
		    			if (oriFilename != null) {
		    				boolean findId = false;
		    				for (int i = 0; i < files_temp.length; i++) {
		    					if (files_temp[i] != null && oriFilename.equals(files_temp[i])) {
		    						fileid = i;
		    						findId = true;
		    						break;
		    					}
		    				}
		    				if (!findId) {
		    					fileid = 0;
		    				}
		    			} else {
		    				fileid = 0;
		    			}
		    		}
		    		currentFileName = files_temp[fileid];
		    		loadResult = true;
		    	} else {
		    		loadResult = false;
		    	}
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			textViewLoading.setVisibility(View.INVISIBLE);
			if (result == true && !isFinishing()) {
				if (files_temp != null) {
					actionBar.setTitle("" + (fileid + 1) + "/" + files_temp.length);
				} else {
					actionBar.setTitle("" + (fileid + 1) + "/" + 0);
				}
				if (loadResult) {
					files = files_temp;
					mAdapter.notifyDataSetChanged();
					viewPager.requestLayout();
					if (viewPager.findViewWithTag(Integer.toString(fileid)) == null) {
						mAdapter.tryLoad(fileid);
					} else {
						loadPage(fileid);
					}
					viewPager.setCurrentItem(fileid, true);
					//FIXME:
					viewPager.setOnPageChangeListener(onPageChangeListener);
				} else {
			    	Toast.makeText(JkanjiPagerGalleryActivity.this, 
					    	"没有指定目录或没有图片文件", 
					    	Toast.LENGTH_SHORT).show();
		    		files = null;
		    		mAdapter.notifyDataSetChanged();
		    		fileid = 0;					
				}
			} else if (result == false) {
				finish();
			}
		}
    }
	
	private final class SamplePagerAdapter extends PagerAdapter {
		private boolean isTryLoad = false;
		private int mTryLoadIndex = 0;
		
		@Override
		public int getCount() {
			if (files == null) {
				return 0;
			}
			return files.length;
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			final ImagePagerView zoomView = new ImagePagerView(container.getContext());

	        zoomView.setOnPrevNextListener(new ImagePagerView.OnPrevNextListener() {
				@Override
				public void onPrev() {
					prevPage();
				}

				@Override
				public void onNext() {
					nextPage();
				}

				@Override
				public Bitmap actLoad(String filename, int reqWidth, int reqHeight, boolean isUse16Bits, boolean isSample) {
					return ThumbsHelper.decode(JkanjiPagerGalleryActivity.this, filename, reqWidth, reqHeight, isUse16Bits, isSample);
				}
	        });
	        zoomView.setTag(Integer.toString(position));
	        if (D) {
	        	Log.e(TAG, "setTag = " + Integer.toString(position));
	        }
			container.addView(zoomView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	        if (isTryLoad && mTryLoadIndex == position) {
	        	loadPage(position);
	        	isTryLoad = false;
	        }
			
			//currentFileName
//			zoomView.reset(path, files[position], 1.0f, 0.5f, 0.5f, true);
//			if (D) {
//				Log.d(TAG, "instantiateItem reset " + files[position]);
//			}
			return zoomView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			if (object != null) {
				((ImagePagerView)object).clearBitmap();
			}
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
		
		public void tryLoad(int index) {
			isTryLoad = true;
			mTryLoadIndex = index;
		}
	}
	
}
