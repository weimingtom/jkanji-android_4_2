package com.sonyericsson.zoom;

import java.io.File;
import java.util.ArrayList;

import com.iteye.weimingtom.jkanji.PrefUtil;
import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.Toast;

public class JkanjiGallerySettingActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "JkanjiGallerySettingActivity";
	
	private static final String SHARE_PREF_NAME = "gallery_pref";
	private static final String SHARE_PREF_FULL_SCREEN = "galleryFullScreen";
	private static final String SHARE_PREF_SHOW_AB = "galleryShowAB";
	private static final String SHARE_PREF_SCREEN_ORI = "galleryScreenOri";
	private static final String SHARE_PREF_SCREEN_ORI2 = "galleryScreenOri2";
	private static final String SHARE_PREF_CAL_SAMPLE = "galleryCalSample";
	private static final String SHARE_PREF_SORT_FILENAME_NUM = "gallerySortFilenameNum";
	private static final String SHARE_PREF_USE_MASK = "galleryUseMask";
	private static final String SHARE_PREF_SHOW_FILE_NAME = "galleryShowFileName";
	private static final String SHARE_PREF_USE_UP_DOWN = "galleryUseUpDown";
	private static final String SHARE_PREF_USE_GRID = "galleryUseGrid";
	private static final String SHARE_PREF_USE_FADE = "galleryUseFade";
	private static final String SHARE_PREF_SAMPLE_SIZE = "gallerySampleSize";
	private static final String SHARE_PREF_USE_16_BITS = "galleryUse16Bits";
	private static final String SHARE_PREF_SHOW_THUMB = "galleryShowThumb";
	private static final String SHARE_PREF_USE_DOUBLE_TAP = "galleryUseDoubleTap";
	private static final String SHARE_PREF_USE_16_BITS_THUMB = "galleryUse16BitsThumb";
	private static final String SHARE_PREF_THUMB_CONTENT_PROVIDER = "galleryThumbContentProvider";
	private static final String SHARE_PREF_VIEW_CONTENT_PROVIDER = "galleryViewContentProvider";
	private static final String SHARE_PREF_AUTO_CALC_ORI = "galleryAutoCalcOri";
	
	public static final int SCREEN_ORI_DEFAULT = 0;
	public static final int SCREEN_ORI_PORT = 1;
	public static final int SCREEN_ORI_LAND = 2;
	
	public static final int SAMPLE_SIZE_X2 = 0;
	public static final int SAMPLE_SIZE_X1 = 1;
	public static final int SAMPLE_SIZE_X05 = 2;
	
	private ActionBar actionBar;
	private CheckBox checkBoxGalleryShowNotification;
	private CheckBox checkBoxGalleryAB;
	private RadioButton radioButtonScreenOriDef, radioButtonScreenOriPort, radioButtonScreenOriLand;
	private RadioButton radioButtonScreenOriDef2, radioButtonScreenOriPort2, radioButtonScreenOriLand2;
	private RadioButton radioButtonSampleSizeX2, radioButtonSampleSizeX1, radioButtonSampleSizeX05;
	private CheckBox checkBoxCalSample;
	private CheckBox checkBoxSortFilenameNum;
	private CheckBox checkBoxUseMask;
	private CheckBox checkBoxShowFileName;
	private CheckBox checkBoxUseUpDown;
	private CheckBox checkBoxUseGrid;
	private CheckBox checkBoxUseFade;
	private CheckBox checkBoxUse16Bits;
	private Button buttonRemoveNotExistFolder;
	private Button buttonMediaScanner;
	private CheckBox checkBoxShowThumb;
	private CheckBox checkBoxUseDoubleTap;
	private CheckBox checkBoxUse16BitsThumb;
	private CheckBox checkBoxThumbContentProvider;
	private CheckBox checkBoxViewContentProvider;
	private Button buttonClearThumbProvider;
	private CheckBox checkBoxAutoCalcOri;
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
                Toast.makeText(JkanjiGallerySettingActivity.this, 
                	"扫描开始: " + intent.getData().getPath(),
                	Toast.LENGTH_SHORT).show();
            } else if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                Toast.makeText(JkanjiGallerySettingActivity.this, 
                	"扫描结束: " + intent.getData().getPath(),
                	Toast.LENGTH_SHORT).show();
            }
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.gallery_setting);
		
		checkBoxGalleryShowNotification = (CheckBox) this.findViewById(R.id.checkBoxGalleryShowNotification);
		checkBoxGalleryAB = (CheckBox) this.findViewById(R.id.checkBoxGalleryAB);
		radioButtonScreenOriDef = (RadioButton) this.findViewById(R.id.radioButtonScreenOriDef);
		radioButtonScreenOriPort = (RadioButton) this.findViewById(R.id.radioButtonScreenOriPort);
		radioButtonScreenOriLand = (RadioButton) this.findViewById(R.id.radioButtonScreenOriLand);
		radioButtonScreenOriDef2 = (RadioButton) this.findViewById(R.id.radioButtonScreenOriDef2);
		radioButtonScreenOriPort2 = (RadioButton) this.findViewById(R.id.radioButtonScreenOriPort2);
		radioButtonScreenOriLand2 = (RadioButton) this.findViewById(R.id.radioButtonScreenOriLand2);
		checkBoxCalSample = (CheckBox) this.findViewById(R.id.checkBoxCalSample);
		checkBoxSortFilenameNum = (CheckBox) this.findViewById(R.id.checkBoxSortFilenameNum);
		checkBoxUseMask = (CheckBox) this.findViewById(R.id.checkBoxUseMask);
		checkBoxShowFileName = (CheckBox) this.findViewById(R.id.checkBoxShowFileName);
		checkBoxUseUpDown = (CheckBox) this.findViewById(R.id.checkBoxUseUpDown);
		checkBoxUseGrid = (CheckBox) this.findViewById(R.id.checkBoxUseGrid);
		checkBoxUseFade = (CheckBox) this.findViewById(R.id.checkBoxUseFade);
		radioButtonSampleSizeX2 = (RadioButton) this.findViewById(R.id.radioButtonSampleSizeX2);
		radioButtonSampleSizeX1 = (RadioButton) this.findViewById(R.id.radioButtonSampleSizeX1);
		radioButtonSampleSizeX05 = (RadioButton) this.findViewById(R.id.radioButtonSampleSizeX05);
		checkBoxUse16Bits = (CheckBox) this.findViewById(R.id.checkBoxUse16Bits);
		buttonRemoveNotExistFolder = (Button) this.findViewById(R.id.buttonRemoveNotExistFolder);
		buttonMediaScanner = (Button) this.findViewById(R.id.buttonMediaScanner);
		checkBoxShowThumb = (CheckBox) this.findViewById(R.id.checkBoxShowThumb);
		checkBoxUseDoubleTap = (CheckBox) this.findViewById(R.id.checkBoxUseDoubleTap);
		checkBoxUse16BitsThumb = (CheckBox) this.findViewById(R.id.checkBoxUse16BitsThumb);
		checkBoxThumbContentProvider = (CheckBox) this.findViewById(R.id.checkBoxThumbContentProvider);
		checkBoxViewContentProvider = (CheckBox) this.findViewById(R.id.checkBoxViewContentProvider);
		buttonClearThumbProvider = (Button) this.findViewById(R.id.buttonClearThumbProvider);
		checkBoxAutoCalcOri = (CheckBox) this.findViewById(R.id.checkBoxAutoCalcOri);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("图库设置");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				//return R.drawable.config;
				return R.drawable.config2;
			}

			@Override
			public void performAction(View view) {
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
		
        checkBoxGalleryShowNotification.setChecked(!getFullScreen(this));
        checkBoxGalleryAB.setChecked(getShowAB(this));
        checkBoxCalSample.setChecked(getCalSample(this));
        checkBoxSortFilenameNum.setChecked(getSortFilenameNum(this));
        checkBoxUseMask.setChecked(getUseMask(this));
        checkBoxShowFileName.setChecked(getShowFileName(this));
        checkBoxUseUpDown.setChecked(getUseUpDown(this));
        checkBoxUseGrid.setChecked(getUseGrid(this));
        checkBoxUseFade.setChecked(getUseFade(this));
        checkBoxUse16Bits.setChecked(getUse16Bits(this));
        checkBoxShowThumb.setChecked(getShowThumb(this));
        checkBoxUseDoubleTap.setChecked(getUseDoubleTap(this));
        checkBoxUse16BitsThumb.setChecked(getUse16BitsThumb(this));
        checkBoxThumbContentProvider.setChecked(getThumbContentProvider(this));
        checkBoxViewContentProvider.setChecked(getViewContentProvider(this));
        checkBoxAutoCalcOri.setChecked(getAutoCalcOri(this));
                
		switch (getScreenOri(this)) {
		case SCREEN_ORI_PORT:
			radioButtonScreenOriPort.setChecked(true);
			break;
			
		case SCREEN_ORI_LAND:
			radioButtonScreenOriLand.setChecked(true);
			break;
			
		case SCREEN_ORI_DEFAULT:
		default:
			radioButtonScreenOriDef.setChecked(true);
			break;
		}
		switch (getScreenOri2(this)) {
		case SCREEN_ORI_PORT:
			radioButtonScreenOriPort2.setChecked(true);
			break;
			
		case SCREEN_ORI_LAND:
			radioButtonScreenOriLand2.setChecked(true);
			break;
			
		case SCREEN_ORI_DEFAULT:
		default:
			radioButtonScreenOriDef2.setChecked(true);
			break;
		}        
		switch (getSampleSize(this)) {
		case SAMPLE_SIZE_X1:
			radioButtonSampleSizeX1.setChecked(true);
			break;
			
		case SAMPLE_SIZE_X05:
			radioButtonSampleSizeX05.setChecked(true);
			break;
			
		case SAMPLE_SIZE_X2:
		default:
			radioButtonSampleSizeX2.setChecked(true);
			break;
		}  
		
		checkBoxGalleryShowNotification.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setFullScreen(JkanjiGallerySettingActivity.this, !isChecked);
			}
		});
		
		checkBoxGalleryAB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setShowAB(JkanjiGallerySettingActivity.this, isChecked);
			}
		});
		
		checkBoxCalSample.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setCalSample(JkanjiGallerySettingActivity.this, isChecked);
			}
		});

		checkBoxSortFilenameNum.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setSortFilenameNum(JkanjiGallerySettingActivity.this, isChecked);
			}
		});

		checkBoxUseMask.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setUseMask(JkanjiGallerySettingActivity.this, isChecked);
			}
		});
		
		checkBoxShowFileName.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setShowFileName(JkanjiGallerySettingActivity.this, isChecked);
			}
		});
		
		checkBoxUseUpDown.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setUseUpDown(JkanjiGallerySettingActivity.this, isChecked);
			}
		});
		checkBoxUseGrid.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setUseGrid(JkanjiGallerySettingActivity.this, isChecked);
			}
		});
		checkBoxUseFade.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setUseFade(JkanjiGallerySettingActivity.this, isChecked);
			}
		});
		checkBoxUse16Bits.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setUse16Bits(JkanjiGallerySettingActivity.this, isChecked);
			}
		});
		checkBoxShowThumb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setResult(RESULT_OK);
				setShowThumb(JkanjiGallerySettingActivity.this, isChecked);
			}
		});
		checkBoxUseDoubleTap.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setUseDoubleTap(JkanjiGallerySettingActivity.this, isChecked);
			}
		});
		checkBoxUse16BitsThumb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setUse16BitsThumb(JkanjiGallerySettingActivity.this, isChecked);
			}
		});
		checkBoxThumbContentProvider.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setThumbContentProvider(JkanjiGallerySettingActivity.this, isChecked);
			}
		});
		checkBoxViewContentProvider.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setViewContentProvider(JkanjiGallerySettingActivity.this, isChecked);
			}
		});
		checkBoxAutoCalcOri.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				setAutoCalcOri(JkanjiGallerySettingActivity.this, isChecked);
			}
		});
		
		radioButtonScreenOriDef.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setScreenOri(JkanjiGallerySettingActivity.this, SCREEN_ORI_DEFAULT);
				}
			}
		});
		radioButtonScreenOriPort.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setScreenOri(JkanjiGallerySettingActivity.this, SCREEN_ORI_PORT);
				}
			}
		});
		radioButtonScreenOriLand.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setScreenOri(JkanjiGallerySettingActivity.this, SCREEN_ORI_LAND);
				}
			}
		});
		
		
		radioButtonScreenOriDef2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setScreenOri2(JkanjiGallerySettingActivity.this, SCREEN_ORI_DEFAULT);
				}
			}
		});
		radioButtonScreenOriPort2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setScreenOri2(JkanjiGallerySettingActivity.this, SCREEN_ORI_PORT);
				}
			}
		});
		radioButtonScreenOriLand2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setScreenOri2(JkanjiGallerySettingActivity.this, SCREEN_ORI_LAND);
				}
			}
		});
		
		
		radioButtonSampleSizeX2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setSampleSize(JkanjiGallerySettingActivity.this, SAMPLE_SIZE_X2);
				}
			}
		});
		radioButtonSampleSizeX1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setSampleSize(JkanjiGallerySettingActivity.this, SAMPLE_SIZE_X1);
				}
			}
		});
		radioButtonSampleSizeX05.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setSampleSize(JkanjiGallerySettingActivity.this, SAMPLE_SIZE_X05);
				}
			}
		});
		
		buttonRemoveNotExistFolder.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				int removeNum = 0;
				JkanjiGalleryHistoryDataSource dataSource = new JkanjiGalleryHistoryDataSource(JkanjiGallerySettingActivity.this);
				dataSource.open();
				ArrayList<JkanjiGalleryHistoryItem> items = dataSource.getAllItems();
				if (items != null) {
					for (JkanjiGalleryHistoryItem item : items) {
						if (item != null) {
							String pathname = item.getPlainPathName();
							if (pathname != null) {
								File path = new File(pathname);
								if (path.isDirectory() && path.exists()) {
									// do nothing
								} else {
									dataSource.deleteItem(item);
									removeNum++;
								}
							}
						}
					}
				}
				dataSource.close();
				Toast.makeText(JkanjiGallerySettingActivity.this, 
					"移除文件夹不存在记录:" + removeNum, Toast.LENGTH_SHORT).show();
				setResult(RESULT_OK);
			}
		});
		buttonMediaScanner.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
						Uri.fromFile(Environment.getExternalStorageDirectory())
					));
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(JkanjiGallerySettingActivity.this, 
							"请求更新系统媒体库失败", 
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		buttonClearThumbProvider.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ThumbsHelper.clear(JkanjiGallerySettingActivity.this);
				Toast.makeText(JkanjiGallerySettingActivity.this, 
					"清除内容提供者缓存", 
					Toast.LENGTH_SHORT).show();				
			}
		});
		
		setResult(RESULT_CANCELED);
	}

	@Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        registerReceiver(mReceiver, intentFilter);
	}
	
	@Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }	
	
    public static void setFullScreen(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_FULL_SCREEN, 
    			enable);
    }
    
    public static boolean getFullScreen(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_FULL_SCREEN, 
				false);
    }

    public static void setShowAB(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_SHOW_AB, 
    			enable);
    }
        
    public static boolean getShowAB(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_SHOW_AB, 
				true);
    }
    
    public static void setScreenOri(Context context, int value) {
    	PrefUtil.putInt(context, SHARE_PREF_NAME,
    			SHARE_PREF_SCREEN_ORI, 
				value);
    }

    public static int getScreenOri(Context context) {
		return PrefUtil.getInt(context, SHARE_PREF_NAME,
				SHARE_PREF_SCREEN_ORI, 
				SCREEN_ORI_DEFAULT);
    }

    public static void setSampleSize(Context context, int value) {
    	PrefUtil.putInt(context, SHARE_PREF_NAME,
    			SHARE_PREF_SAMPLE_SIZE, 
				value);
    }

    public static int getSampleSize(Context context) {
		return PrefUtil.getInt(context, SHARE_PREF_NAME,
				SHARE_PREF_SAMPLE_SIZE, 
				SAMPLE_SIZE_X2);
    }
    
    public static double getSampleSizeValue(Context context) {
		switch (getSampleSize(context)) {
		default:
		case SAMPLE_SIZE_X2:
			return 2;
			
		case SAMPLE_SIZE_X1:
			return 1;
			
		case SAMPLE_SIZE_X05:
			return 0.5;
		}
    }

    public static void setScreenOri2(Context context, int value) {
    	PrefUtil.putInt(context, SHARE_PREF_NAME,
    			SHARE_PREF_SCREEN_ORI2, 
				value);
    }
    
    /**
     * 只读模式的屏幕方向
     * @param context
     * @return
     */
    public static int getScreenOri2(Context context) {
		return PrefUtil.getInt(context, SHARE_PREF_NAME,
				SHARE_PREF_SCREEN_ORI2, 
				SCREEN_ORI_DEFAULT);
    }

    public static void setCalSample(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_CAL_SAMPLE, 
				enable);
    }
    
    public static boolean getCalSample(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_CAL_SAMPLE, 
				true);
    }
    
    public static void setSortFilenameNum(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_SORT_FILENAME_NUM, 
				enable);
    }
   
    public static boolean getSortFilenameNum(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_SORT_FILENAME_NUM, 
				false);
    }
    
    public static void setUseMask(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_USE_MASK, 
				enable);
    }
   
    public static boolean getUseMask(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_USE_MASK, 
				false);
    }
    
    public static void setShowFileName(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_SHOW_FILE_NAME, 
				enable);
    }
   
    public static boolean getShowFileName(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_SHOW_FILE_NAME, 
				false);
    }
    
    public static void setUseUpDown(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_USE_UP_DOWN, 
				enable);
    }
   
    public static boolean getUseUpDown(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_USE_UP_DOWN, 
				false);
    }
    
    public static void setUseGrid(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_USE_GRID, 
				enable);
    }
   
    public static boolean getUseGrid(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_USE_GRID, 
				true);
    }
    
    public static void setUseFade(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_USE_FADE, 
				enable);
    }
   
    public static boolean getUseFade(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_USE_FADE, 
				true);	
    }
    
    public static void setUse16Bits(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_USE_16_BITS, 
				enable);
    }
   
    public static boolean getUse16Bits(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_USE_16_BITS, 
				true);
    }
    
    public static void setShowThumb(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_SHOW_THUMB, 
				enable);
    }
   
    public static boolean getShowThumb(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_SHOW_THUMB, 
				true);
    }
    
    public static void setUseDoubleTap(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_USE_DOUBLE_TAP, 
				enable);
    }
   
    public static boolean getUseDoubleTap(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_USE_DOUBLE_TAP, 
				false);
    }
    
    public static void setUse16BitsThumb(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_USE_16_BITS_THUMB, 
				enable);
    }
    
    public static boolean getUse16BitsThumb(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_USE_16_BITS_THUMB, 
				false);
    }
    
    public static void setThumbContentProvider(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
    			SHARE_PREF_THUMB_CONTENT_PROVIDER, 
				enable);
    }
    
    public static boolean getThumbContentProvider(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_THUMB_CONTENT_PROVIDER, 
				false);
    }
    
    public static void setViewContentProvider(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_VIEW_CONTENT_PROVIDER, 
				enable);
    }
    
    public static boolean getViewContentProvider(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_VIEW_CONTENT_PROVIDER, 
				false);
    }
    
    public static void setAutoCalcOri(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_AUTO_CALC_ORI, 
				enable);
    }
    
    public static boolean getAutoCalcOri(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_AUTO_CALC_ORI, 
				false);
    }
    
    public static int calcOri(Context context, File file, int defaultValue) {
    	int result = defaultValue;
    	String filename = file.getAbsolutePath();
    	Point size = getBitmapSize(filename);
    	if (D) {
    		Log.e(TAG, "calcOri = " + size.x + "," + size.y + "," + filename);
    	}
    	if (size != null && size.x > 0 && size.y > 0) {
    		if (size.x > size.y) {
    			result = SCREEN_ORI_LAND;
    		} else {
    			result = SCREEN_ORI_PORT;
    		}
    	}
    	return result;
    }
    
    private static Point getBitmapSize(String filename) {
    	final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);
        return new Point(options.outWidth, options.outHeight);
    }
}
