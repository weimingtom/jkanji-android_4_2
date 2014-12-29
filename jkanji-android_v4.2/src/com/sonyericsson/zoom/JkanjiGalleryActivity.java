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
import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elgubbo.sharetoclipboard.ShareToClipboardActivity;
import com.iteye.weimingtom.jkanji.JKanjiActivity;
import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.jkanji.SQLiteReaderActivity;
import com.markupartist.android.widget.ActionBar;
import com.sonyericsson.zoom.ImageZoomView;

public class JkanjiGalleryActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "JkanjiGallActivity";
	
	/**
	 * 如果EXTRA_KEY_FILEID < 0 
	 * 则尝试根据EXTRA_KEY_FILENAME寻找fileid
	 */
	public static final String EXTRA_KEY_PATH = "JkanjiGallActivity.EXTRA_KEY_PATH";
	public static final String EXTRA_KEY_FILEID = "JkanjiGallActivity.EXTRA_KEY_FILEID";
	public static final String EXTRA_KEY_FILENAME = "JkanjiGallActivity.EXTRA_KEY_FILENAME";
	public static final String EXTRA_KEY_ID = "JkanjiGallActivity.EXTRA_KEY_ID";
	public static final String EXTRA_KEY_ZOOM = "JkanjiGallActivity.EXTRA_KEY_ZOOM";
	public static final String EXTRA_KEY_PANX = "JkanjiGallActivity.EXTRA_KEY_PANX";
	public static final String EXTRA_KEY_PANY = "JkanjiGallActivity.EXTRA_KEY_PANY";
	public static final String EXTRA_KEY_MULTI = "JkanjiGallActivity.EXTRA_KEY_MULTI";
	public static final String EXTRA_KEY_DESC = "JkanjiGallActivity.EXTRA_KEY_DESC";
	public static final String EXTRA_KEY_ISRECORD = "JkanjiGallActivity.EXTRA_KEY_ISRECORD";
	
	public static final String DEFAULT_PATH = "/mnt/sdcard/book5/dc2";
	
	private static final int MENU_ID_LEFT = Menu.FIRST + 0;
	private static final int MENU_ID_SEARCH = Menu.FIRST + 1;
	private static final int MENU_ID_RIGHT = Menu.FIRST + 2;
	private static final int MENU_ID_SAVE = Menu.FIRST + 3;
	private static final int MENU_ID_MEMO = Menu.FIRST + 4;
	private static final int MENU_ID_SQLITE = Menu.FIRST + 5;
	//
	private static final int MENU_ID_PAGE = Menu.FIRST + 6;
	private static final int MENU_ID_RESET = Menu.FIRST + 7;
    private static final int MENU_ID_MULTI = Menu.FIRST + 8;    
    private static final int MENU_ID_PAN = Menu.FIRST + 9;
    private static final int MENU_ID_ZOOM = Menu.FIRST + 10;
    private static final int MENU_ID_EXIT = Menu.FIRST + 11;
    private static final int MENU_ID_PREVIEW = Menu.FIRST + 12;
    private static final int MENU_ID_SHARE_PHOTO = Menu.FIRST + 13;
    private static final int MENU_ID_SIZE = Menu.FIRST + 14;
    private static final int MENU_ID_USE_FADE = Menu.FIRST + 15;
    private static final int MENU_ID_RESIZE_WIDTH = Menu.FIRST + 16;
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
    private ImageZoomView mZoomView, mZoomViewBack;
    private Bitmap mBitmap, mBitmapBack;

    private String path;
    private int fileid;
    private String[] files;
    private String currentFileName;
    private long mId = -1L;
    private String desc;
    private boolean isRecord = true;
    private boolean isUseFade = true;
    
    private final static boolean USE_TASK = true;
    private volatile boolean useOptions = true;
    private volatile boolean use16Bits = false;
    private BitmapFactory.Options options16Bits;
    private LoadDataTask task;
    
    private Toast toastFirst, toastNull, toastLast;
    
    private TextView textViewLoading;
    
    private final class SaveData {
    	public String path;
    	public int fileid;
    	public float zoom, panX, panY;
    	public long id = -1L;
    	public boolean enableMulti = true;
    	public String desc;
    	public boolean isRecord = true;
    	public boolean isUseFade = true;
    }
    
    private boolean isSortFilenameNum = false;
    
    private ImageGallery2Dialog mGalleryDialog;
    
    private boolean useViewContentProvider = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideTitle();
        setContentView(R.layout.gallery_view);
        linearLayoutContent = (LinearLayout) this.findViewById(R.id.linearLayoutContent);
        mZoomView = (ImageZoomView)findViewById(R.id.zoomview);
        mZoomViewBack = (ImageZoomView)findViewById(R.id.zoomviewback);
        actionBar = (ActionBar) findViewById(R.id.actionbar);
        textViewLoading = (TextView) findViewById(R.id.textViewLoading);
        
        isSortFilenameNum = JkanjiGallerySettingActivity.getSortFilenameNum(this);
        this.useViewContentProvider = JkanjiGallerySettingActivity.getViewContentProvider(this);
        
        builder1 = new AlertDialog.Builder(this);
        useOptions = JkanjiGallerySettingActivity.getCalSample(this);
        options16Bits = new BitmapFactory.Options();
		options16Bits.inPreferredConfig = Bitmap.Config.RGB_565;
		options16Bits.inPurgeable = true;  
		options16Bits.inInputShareable = true; 
        use16Bits = JkanjiGallerySettingActivity.getUse16Bits(this);
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
				Intent intent = new Intent(JkanjiGalleryActivity.this, SQLiteReaderActivity.class);
				startActivity(intent);
			}
        });
        
        if (JkanjiGallerySettingActivity.getShowAB(this)) {
        	actionBar.setVisibility(View.VISIBLE);
        } else {
        	actionBar.setVisibility(View.GONE);
        }
        
        mZoomView.setOnPrevNextListener(new ImageZoomView.OnPrevNextListener() {
			@Override
			public void onPrev() {
				if (mZoomView.getEnableMultiTouch()) {
					prevPage();
				}
			}

			@Override
			public void onNext() {
				if (mZoomView.getEnableMultiTouch()) {
					nextPage();
				}
			}
        });
        
        mZoomViewBack.disableTouch();
        
        final float zoom;// = 1.0f;
        final float panX;// = 0.5f;
        final float panY;// = 0.5f;
        final boolean enableMulti;// = true;
        String oriFilename = null;
        SaveData saveData = (SaveData)this.getLastNonConfigurationInstance();
        if (saveData == null) {
	        Intent intent = this.getIntent();
	        if (intent != null) {
	        	path = intent.getStringExtra(EXTRA_KEY_PATH);
	        	fileid = intent.getIntExtra(EXTRA_KEY_FILEID, 0);
	        	oriFilename = intent.getStringExtra(EXTRA_KEY_FILENAME);
	        	mId = intent.getLongExtra(EXTRA_KEY_ID, -1L);
	        	zoom = intent.getFloatExtra(EXTRA_KEY_ZOOM, 1.0f);
	        	panX = intent.getFloatExtra(EXTRA_KEY_PANX, 0.5f);
	        	panY = intent.getFloatExtra(EXTRA_KEY_PANY, 0.5f);
	        	enableMulti = intent.getBooleanExtra(EXTRA_KEY_MULTI, true);
	        	desc = intent.getStringExtra(EXTRA_KEY_DESC);
	        	isRecord = intent.getBooleanExtra(EXTRA_KEY_ISRECORD, true);
	        } else {
	        	zoom = 1.0f;
	        	panX = 0.5f;
	        	panY = 0.5f;
	        	enableMulti = true;
	        	isRecord = true;
	        }
    	} else {
    		path = saveData.path;
    		fileid = saveData.fileid;
    		zoom = saveData.zoom;
    		panX = saveData.panX;
    		panY = saveData.panY;
    		mId = saveData.id;
    		enableMulti = saveData.enableMulti;
    		desc = saveData.desc;
    		isRecord = saveData.isRecord;
    		isUseFade = saveData.isUseFade;
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
        	sortFiles(oriFilename, zoom, panX, panY, enableMulti);
        } else {
	    	Toast.makeText(this, 
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
    	SaveData saveData = new SaveData();
    	saveData.fileid = fileid;
    	saveData.path = path;
    	saveData.zoom = mZoomView.getZoom();
    	saveData.panX = mZoomView.getPanX();
    	saveData.panY = mZoomView.getPanY();
    	saveData.id = mId;
    	saveData.enableMulti = mZoomView.getEnableMultiTouch();
    	saveData.desc = desc;
    	saveData.isRecord = isRecord;
    	saveData.isUseFade = isUseFade;
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
        menu.add(Menu.NONE, MENU_ID_MULTI, 0, "单双指切换");
        menu.add(Menu.NONE, MENU_ID_PAN, 0, "单指平移模式");
        menu.add(Menu.NONE, MENU_ID_ZOOM, 0, "单指缩放模式");
        menu.add(Menu.NONE, MENU_ID_SHARE_PHOTO, 0, "共享图片");
        menu.add(Menu.NONE, MENU_ID_SIZE, 0, "图像大小信息");
        menu.add(Menu.NONE, MENU_ID_USE_FADE, 0, "切换是否允许淡入");
        menu.add(Menu.NONE, MENU_ID_RESIZE_WIDTH, 0, "适应宽度");
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
            reset(1.0f, 0.5f, 0.5f, true);
            break;
        	
        case MENU_ID_MULTI:
        	mZoomView.setEnableMultiTouch(!mZoomView.getEnableMultiTouch());
        	if (!mZoomView.getEnableMultiTouch()) {
        		mZoomView.setControlType(ImageZoomView.ControlType.PAN);
        		Toast.makeText(this, 
    	    			"切换至单指平移模式", 
    	    			Toast.LENGTH_SHORT).show();
        	} else {
        		Toast.makeText(this, 
    	    			"切换至双指模式", 
    	    			Toast.LENGTH_SHORT).show();        		
        	}
        	break;
        	
        case MENU_ID_PAN:
        	if (mZoomView.getEnableMultiTouch()) {
        		mZoomView.setEnableMultiTouch(false);
	        }
        	mZoomView.setControlType(ImageZoomView.ControlType.PAN);
    		Toast.makeText(this, 
	    			"切换至单指平移模式", 
	    			Toast.LENGTH_SHORT).show();
        	break;
        	
        case MENU_ID_ZOOM:
        	if (mZoomView.getEnableMultiTouch()) {
        		mZoomView.setEnableMultiTouch(false);
	        }
        	mZoomView.setControlType(ImageZoomView.ControlType.ZOOM);
    		Toast.makeText(this, 
	    			"切换至单指缩放模式", 
	    			Toast.LENGTH_SHORT).show();
            break;
            
        case MENU_ID_SHARE_PHOTO:
        	sharePhoto();
        	break;
            
        case MENU_ID_SIZE:
        	showDialog(DIALOG_SIZE);
        	break;
        
        case MENU_ID_USE_FADE:
        	if (this.isUseFade) {
        		this.isUseFade = false;
        		Toast.makeText(this, "不允许淡入动画", Toast.LENGTH_SHORT).show();
        	} else {
        		this.isUseFade = true;
        		Toast.makeText(this, "允许淡入动画（如果设置中已勾选）", Toast.LENGTH_SHORT).show();
        	}
        	break;
        	
        case MENU_ID_RESIZE_WIDTH:
        	mZoomView.resizeWidth();
        	break;
        	
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
    
    private void reset(float zoom, float panX, float panY, boolean enableMulti) {
    	mZoomView.setEnableMultiTouch(enableMulti);
    	mZoomView.setZoom(zoom);
    	mZoomView.setPanX(panX);
    	mZoomView.setPanY(panY);
    	if (path != null && currentFileName != null) {
    		File bitmapFile = new File(path, currentFileName);
    		//TODO:是否多线程加载
    		if (!USE_TASK) {
        		if (mBitmap != null) {
        			mBitmap.recycle();
        		}
	    		if (bitmapFile.isFile() && bitmapFile.canRead()) {
	    			double sampleSizeValue = JkanjiGallerySettingActivity.getSampleSizeValue(JkanjiGalleryActivity.this);
	    			if (sampleSizeValue < 0) {
	    				sampleSizeValue = 1;
	    			}
	    			if (!useOptions) {
	    				if (use16Bits) {
	    					mBitmap = BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(), options16Bits);
		    			} else {
	    					mBitmap = BitmapFactory.decodeFile(bitmapFile.getAbsolutePath());
	    				}
	    			} else {
	    				int viewWidth = 0;
	    				int viewHeight = 0;
	    				if (mZoomView == null || 
	    						mZoomView.getWidth() == 0 || 
	    						mZoomView.getHeight() == 0) {
	    						if (getResources() != null) {
	    							DisplayMetrics dm = getResources().getDisplayMetrics();
	    							if (dm != null) {
	    								viewWidth = (int)(dm.widthPixels * sampleSizeValue);
	    								viewHeight = (int)(dm.heightPixels * sampleSizeValue);
	    							}
	    						}
	    				} else {
	    					viewWidth = (int)(this.mZoomView.getWidth() * sampleSizeValue);
	    					viewHeight = (int)(this.mZoomView.getHeight() * sampleSizeValue);
	    				}
	    				mBitmap = decodeSampledBitmapFromFile(bitmapFile.getAbsolutePath(), viewWidth, viewHeight, use16Bits);
	    			}
	    		} else {
	    			mBitmap = null;
	    		}
	    		if (mBitmap == null) {
			    	Toast.makeText(this, 
				    	"无法读取图片：" + currentFileName, 
				    	Toast.LENGTH_SHORT).show();
	    		}
	    		mZoomView.setImage(mBitmap, bitmapFile.getName());
	    		mZoomView.invalidate();
    		} else {
	    		if (bitmapFile.isFile() && bitmapFile.canRead()) {
	    			if (task != null) {
	    				task.setCancel(true);
	    			}
	    			task = new LoadDataTask();
	    			task.execute(bitmapFile.getAbsolutePath());
	    		} else {
	    			mBitmap = null;
			    	Toast.makeText(this, 
					    	"无法读取图片：" + currentFileName, 
					    	Toast.LENGTH_SHORT).show();
		    		mZoomView.setImage(mBitmap, bitmapFile.getName());
		    		mZoomView.invalidate();
	    		}
    		}
    	}
    }

    private static synchronized Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight, boolean isUse16Bits) {
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
    
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
    		reset(mZoomView.getZoom(), mZoomView.getPanX(), mZoomView.getPanY(), mZoomView.getEnableMultiTouch());
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
	    		reset(mZoomView.getZoom(), mZoomView.getPanX(), mZoomView.getPanY(), mZoomView.getEnableMultiTouch());
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
	    		reset(mZoomView.getZoom(), mZoomView.getPanX(), mZoomView.getPanY(), mZoomView.getEnableMultiTouch());
	    	}
	    } else {
    		if (toastNull != null) {
    			toastNull.show();
    		}
	    }
    }
    
    private void saveGallery(long id) {
    	if (files != null && files.length > 0) {
    		int plainPage = fileid;
    		int plainTotalPage = files.length;
	    	JkanjiGalleryHistoryDataSource dataSrc = new JkanjiGalleryHistoryDataSource(this);
	    	dataSrc.open();
	    	JkanjiGalleryHistoryItem item = new JkanjiGalleryHistoryItem();
	    	item.setId(id);
	    	item.setPlainZoom(mZoomView.getZoom());
	    	item.setPlainPanX(mZoomView.getPanX());
	    	item.setPlainPanY(mZoomView.getPanY());
	    	item.setPlainPage(plainPage);
	    	item.setPlainTotalPage(plainTotalPage);
	    	if (fileid >= 0 && fileid < files.length) {
	    		item.setPlainFileName(files[fileid]);
	    	} else {
	    		item.setPlainFileName("");
	    	}
	    	item.setPlainPathName(this.path);
	    	item.setPlainEnableMulti(mZoomView.getEnableMultiTouch());
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
    
	private class LoadDataTask extends AsyncTask<String, Void, Boolean> {
		private boolean loadResult = false;
		private Bitmap bitmap;
		
		private volatile boolean isCancel = false;
		private String filename;
		private int viewWidth;
		private int viewHeight;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			double sampleSizeValue = JkanjiGallerySettingActivity.getSampleSizeValue(JkanjiGalleryActivity.this);
			if (sampleSizeValue < 0) {
				sampleSizeValue = 1;
			}
			if (isUseFade && JkanjiGallerySettingActivity.getUseFade(JkanjiGalleryActivity.this)) {
				if (mBitmapBack != null && !mBitmapBack.isRecycled()) {
					mBitmapBack.recycle();
					mBitmapBack = null;
				}
				if (mBitmap != null && !mBitmap.isRecycled()) {
					if (use16Bits) {
						mBitmapBack = mBitmap.copy(Bitmap.Config.RGB_565, true);
					} else {
						mBitmapBack = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
					}
					mZoomViewBack.setZoom(mZoomView.getZoom());
					mZoomViewBack.setPanX(mZoomView.getPanX());
					mZoomViewBack.setPanY(mZoomView.getPanY());
					mZoomViewBack.setImage(mBitmapBack, null);
					mZoomViewBack.invalidate();
					mZoomViewBack.setVisibility(View.VISIBLE);
				}
	    	}
			mZoomView.disableTouch();
			if (mZoomView == null || 
				mZoomView.getWidth() == 0 || 
				mZoomView.getHeight() == 0) {
				if (getResources() != null) {
					DisplayMetrics dm = getResources().getDisplayMetrics();
					if (dm != null) {
						viewWidth = (int)(dm.widthPixels * sampleSizeValue);
						viewHeight = (int)(dm.heightPixels * sampleSizeValue);
					}
				}
			} else {
				viewWidth = (int)(mZoomView.getWidth() * sampleSizeValue);
				viewHeight = (int)(mZoomView.getHeight() * sampleSizeValue);
			}
			actionBar.setTitle("加载中...");
		}

		public void setCancel(boolean _isCancel) {
			this.isCancel = _isCancel;
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				filename = params[0];
				
    			if (!useOptions) {
    				if (use16Bits) { 
    					if (useViewContentProvider) {
    						bitmap = ThumbsHelper.decode(JkanjiGalleryActivity.this, ThumbsProvider.THUMB_PREFIX + filename, 1, 1, true, false);
    					} else {
    						bitmap = BitmapFactory.decodeFile(filename, options16Bits);
    					}
    				} else {
    					if (useViewContentProvider) {
    						bitmap = ThumbsHelper.decode(JkanjiGalleryActivity.this, ThumbsProvider.THUMB_PREFIX + filename, 1, 1, false, false);
    					} else {
    						bitmap = BitmapFactory.decodeFile(filename);
    					}
    				}
    			} else {
					if (useViewContentProvider) {
						bitmap = ThumbsHelper.decode(JkanjiGalleryActivity.this, ThumbsProvider.THUMB_PREFIX + filename, viewWidth, viewHeight, false, true);
					} else {
						bitmap = decodeSampledBitmapFromFile(filename, viewWidth, viewHeight, use16Bits);
					}
    			}
				if (bitmap != null) {
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
			if (this.isCancel == false) {
				if (result == true && !JkanjiGalleryActivity.this.isFinishing()) {
					if (false) {
						if (filename != null) {
							try {
								File file = new File(filename);
								actionBar.setTitle(file.getName());
							} catch (Throwable e) {
								e.printStackTrace();
							}
						} else {
							actionBar.setTitle("");
						}
					} else {
						if (files != null) {
							actionBar.setTitle("" + (fileid + 1) + "/" + files.length);
						} else {
							actionBar.setTitle("");
						}
					}
					if (loadResult) {
		        		if (mBitmap != null && !mBitmap.isRecycled()) {
		        			mBitmap.recycle();
		        		}
						mBitmap = bitmap;
			    		if (mBitmap == null) {
					    	Toast.makeText(JkanjiGalleryActivity.this, 
						    	"无法读取图片：" + currentFileName, 
						    	Toast.LENGTH_SHORT).show();
			    		} else {
			    			File file = new File(filename);
			    			mZoomView.setImage(mBitmap, file.getName());
			    			mZoomView.invalidate();
			    			mZoomView.enableTouch();
			    			if (isUseFade && JkanjiGallerySettingActivity.getUseFade(JkanjiGalleryActivity.this)) {
			    				mZoomView.clearAnimation();
			    				Animation ani = AnimationUtils.loadAnimation(JkanjiGalleryActivity.this, R.anim.bubble_fade);
			    				ani.setAnimationListener(new Animation.AnimationListener() {
									@Override
									public void onAnimationEnd(Animation animation) {
										mZoomViewBack.setVisibility(View.INVISIBLE);
									}

									@Override
									public void onAnimationRepeat(Animation animation) {
										
									}

									@Override
									public void onAnimationStart(Animation animation) {
										
									}
			    				});
			    				mZoomView.startAnimation(ani);
					    	}
			    		}
					} else {
						if (toastNull != null) {
							toastNull.show();
						}
					}
				} else if (result == false) {
					finish();
				}
			}
			task = null;
			bitmap = null;
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
    		mZoomView.postDelayed(new Runnable() {
				@Override
				public void run() {
					reset(zoom, panX, panY, enableMulti);
				}
    		}, 10);
    	} else {
	    	Toast.makeText(this, 
			    	"没有指定目录或没有图片文件", 
			    	Toast.LENGTH_SHORT).show();
    		files = null;
    		fileid = 0;
    	}
    }
	
	
    private void sortFiles(String oriFilename, final float zoom, final float panX, final float panY, final boolean enableMulti) {
    	new SortTask().execute(oriFilename, Float.toString(zoom), Float.toString(panX), Float.toString(panY), Boolean.toString(enableMulti));
    }
    
    
	
	private class SortTask extends AsyncTask<String, Void, Boolean> {
		private boolean loadResult = false;
		
		private String oriFilename;
		private float zoom, panX, panY;
		private boolean enableMulti;
		
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
				zoom = Float.parseFloat(params[1]);
				panX = Float.parseFloat(params[2]);
				panY = Float.parseFloat(params[3]);
				enableMulti = Boolean.parseBoolean(params[4]);
				
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
		    		currentFileName = files[fileid];
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
				if (loadResult) {
					if (false) {
			    		mZoomView.postDelayed(new Runnable() {
							@Override
							public void run() {
								reset(zoom, panX, panY, enableMulti);
							}
			    		}, 10);	
					} else {
						reset(zoom, panX, panY, enableMulti);
					}
				} else {
			    	Toast.makeText(JkanjiGalleryActivity.this, 
					    	"没有指定目录或没有图片文件", 
					    	Toast.LENGTH_SHORT).show();
		    		files = null;
		    		fileid = 0;					
				}
			} else if (result == false) {
				finish();
			}
		}
    }
	
}
