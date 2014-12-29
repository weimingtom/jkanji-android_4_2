/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sonyericsson.zoom;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.example.android.bitmapfun.provider.FileImageWorkerAdapter;
import com.example.android.bitmapfun2.ImageFetcher;
import com.example.android.bitmapfun2.RecyclingImageView;
import com.example.android.bitmapfun2.ImageCache.ImageCacheParams;
import com.iteye.weimingtom.jkanji.PrefUtil;
import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight forward GridView
 * implementation with the key addition being the ImageWorker class w/ImageCache to load children
 * asynchronously, keeping the UI nice and smooth and caching thumbnails for quick retrieval. The
 * cache is retained over configuration changes like orientation change so the images are populated
 * quickly if, for example, the user rotates the device.
 */
public class ImageGrid2Activity extends Activity implements AdapterView.OnItemClickListener {
    private static final boolean D = false;
	private static final String TAG = "ImageGrid2Activity";
    
	private static final String SHARE_PREF_NAME = "gallery_pref";
	private static final String SHARE_PREF_GRID_SHOW_CONFIG = "galleryGridShowConfig";
	private static final String SHARE_PREF_GRID_SHOW_TYPE = "galleryGridShowType";
	private static final String SHARE_PREF_GRID_POS =  "galleryGridPos";
	private static final String SHARE_PREF_DEFAULT_PATH = "galleryDefaultPath";
	
	private static final int SHOW_TYPE_DIALOG = 0;
	private static final int SHOW_TYPE_VIEW = 1;
	
	private final static int CONTEXT_MENU_DIALOG = ContextMenu.FIRST + 0;
	private final static int CONTEXT_MENU_PLAIN = ContextMenu.FIRST + 1;
	private final static int CONTEXT_MENU_SHARE_PHOTO = ContextMenu.FIRST + 2;
	
    private static final String IMAGE_CACHE_DIR = "thumbs";

    private final static String DEFAULT_ROOT = "/";
    
    private static final int REQUEST_GALLERY_OPEN = 7;
    
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageAdapter mAdapter;
    private ImageFetcher mImageFetcher;
    
    private FileImageWorkerAdapter fileAdapter;
    private GridView mGridView;
    private TextView textViewLoading;
    
    private ActionBar actionBar;
    
    private boolean isSortFilenameNum = false;
    
    private final static String KEY_CURRENT_DIR = "ImageGrid2Activity.KEY_CURRENT_DIR";
    private final static String KEY_IS_NO_RECORD = "ImageGrid2Activity.KEY_IS_NO_RECORD";
    private final static String KEY_FILENAME = "ImageGrid2Activity.KEY_FILENAME";
    
    public final static String EXTRA_NORECORD = "ImageGrid2Activity.EXTRA_NORECORD";
    public final static String EXTRA_FILENAME = "ImageGrid2Activity.EXTRA_FILENAME";
    
    private String strCurrDir = null;
    private boolean isNoRecord = false;
    private String mFilename = null;
    
    private int mListPos;
    
    private ImagePreviewDialog mImagePreviewDialog;
    
    private TextView textViewInfo;
    private TextView textViewPath;
    private RadioGroup radioGroupShow;
    private RadioButton radioButtonDialog;
    private RadioButton radioButtonView;
    
    private LinearLayout linearLayoutConfig;
    
    private LoadDataTask mTask = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        strCurrDir = null;
        if (savedInstanceState != null) {
        	strCurrDir = savedInstanceState.getString(KEY_CURRENT_DIR);
        	isNoRecord = savedInstanceState.getBoolean(KEY_IS_NO_RECORD, false);
        	mFilename = savedInstanceState.getString(KEY_FILENAME);
        }
        if (strCurrDir == null) {
	        Intent intent = this.getIntent();
	        if (intent != null) {
	        	isNoRecord = intent.getBooleanExtra(EXTRA_NORECORD, false);
	        	mFilename = intent.getStringExtra(EXTRA_FILENAME);
	        	if (mFilename != null) {
	        		File file = new File(mFilename);
	        		strCurrDir = file.getParent();
	        		mFilename = file.getAbsolutePath();
	        	}
	        } else {
	        	isNoRecord = false;
	        	mFilename = null;
	        }
        }
        
        if (strCurrDir == null) { 
        	strCurrDir = getLastDefaultPath();
        }
        
        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        isSortFilenameNum = JkanjiGallerySettingActivity.getSortFilenameNum(this);
        
        mAdapter = new ImageAdapter(this);

        ImageCacheParams cacheParams = new ImageCacheParams(this, IMAGE_CACHE_DIR);

        //FIXME:内存大小百分比
        cacheParams.setMemCacheSizePercent(0.50f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(this, mImageThumbSize);
        mImageFetcher.setLoadingImage(R.drawable.bitmapfun_empty_photo, 
            	R.drawable.bitmapfun_empty_photo_scale);
        mImageFetcher.addImageCache(cacheParams);

        this.setContentView(R.layout.bitmapfun_grid);
        
        actionBar = (ActionBar) findViewById(R.id.actionbar);
        if (this.isNoRecord) {
        	actionBar.setTitle("图库浏览(只读模式)");
        } else {
        	actionBar.setTitle("图库浏览(添加模式)");
        }
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.shelf_folder;
			}

			@Override
			public void performAction(View view) {
				setResult(RESULT_CANCELED, null);
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.config;
			}

			@Override
			public void performAction(View view) {
				if (linearLayoutConfig.getVisibility() == View.VISIBLE) {
					linearLayoutConfig.setVisibility(View.GONE);
					setShowConfig(ImageGrid2Activity.this, false);
				} else {
					linearLayoutConfig.setVisibility(View.VISIBLE);
					setShowConfig(ImageGrid2Activity.this, true);
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
				if (fileAdapter != null) {
					jumpTo(FileImageWorkerAdapter.JUMP_UP);
	    		}
			}
        });
        
        textViewLoading = (TextView) this.findViewById(R.id.textViewLoading);
        
        mGridView = (GridView) findViewById(R.id.gridViewBitmap);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    mImageFetcher.setPauseWork(true);
                } else {
                    mImageFetcher.setPauseWork(false);
                }
        		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE){
        			mListPos = mGridView.getFirstVisiblePosition();
        		}
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
            }
        });
        this.registerForContextMenu(mGridView);

        // This listener is used to get the final width of the GridView and then calculate the
        // number of columns and the width of each column. The width of each column is variable
        // as the GridView has stretchMode=columnWidth. The column width is used to set the height
        // of each view so we get nice square thumbnails.
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mAdapter.getNumColumns() == 0) {
                    final int numColumns = (int) Math.floor(
                            mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing * 2));
                    if (numColumns > 0) {
                        final int columnWidth =
                                (mGridView.getWidth() / numColumns) - mImageThumbSpacing * 2;
                        mAdapter.setNumColumns(numColumns);
                        mAdapter.setItemHeight(columnWidth);
                        if (D) {
                            Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
                        }
                    }
                }
            }
        });
        
        textViewInfo = (TextView) this.findViewById(R.id.textViewInfo);
        if (!isNoRecord) {
        	textViewInfo.setText("注意：长按弹出菜单。小内存手机可能无法使用此功能。");
        } else {
        	textViewInfo.setText("注意：长按弹出菜单。当前的所有操作均不保存到图库。小内存手机可能无法使用此功能。");
        }
        textViewPath = (TextView) this.findViewById(R.id.textViewPath);
        linearLayoutConfig = (LinearLayout) this.findViewById(R.id.linearLayoutConfig);
        if (getShowConfig(this)) {
        	linearLayoutConfig.setVisibility(View.VISIBLE);
        } else {
        	linearLayoutConfig.setVisibility(View.GONE);
        }
        radioGroupShow = (RadioGroup) this.findViewById(R.id.radioGroupShow);
        radioButtonDialog = (RadioButton) this.findViewById(R.id.radioButtonDialog);
        radioButtonView = (RadioButton) this.findViewById(R.id.radioButtonView);
        if (this.isNoRecord) {
        	radioGroupShow.setVisibility(View.VISIBLE);
        } else {
        	radioGroupShow.setVisibility(View.GONE);
        }
        switch (getShowType(this)) {
        default:
        case SHOW_TYPE_DIALOG:
        	radioButtonDialog.setChecked(true);
        	radioButtonView.setChecked(false);
        	break;
        	
        case SHOW_TYPE_VIEW:
        	radioButtonDialog.setChecked(false);
        	radioButtonView.setChecked(true);
        	break;
        }
        radioButtonDialog.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					setShowType(ImageGrid2Activity.this, SHOW_TYPE_DIALOG);
				}
			}
        });
        radioButtonView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					setShowType(ImageGrid2Activity.this, SHOW_TYPE_VIEW);
				}
			}
        });
        setResult(RESULT_CANCELED);
        startTask(strCurrDir);
    }
    
    private void startTask(String dir) {
    	mTask = new LoadDataTask();
    	mTask.execute(dir);
    }
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (outState != null) {
			outState.putString(KEY_CURRENT_DIR, strCurrDir);
			outState.putBoolean(KEY_IS_NO_RECORD, isNoRecord);
			outState.putString(KEY_FILENAME, mFilename);
		}
	}
    
    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
        if (isFinishing()) {
        	clearCache();
        }
		if (mImagePreviewDialog != null && mImagePreviewDialog.isShowing()) {
			mImagePreviewDialog.dismiss();
		}
		mImagePreviewDialog = null;
		setLastListPos(mListPos);
		if (isFinishing()) {
			mTask = null;
		}
    }

    @Override
	protected void onStop() {
		super.onStop();
		if (mImagePreviewDialog != null && mImagePreviewDialog.isShowing()) {
			mImagePreviewDialog.dismiss();
		}
		mImagePreviewDialog = null;
		mTask = null;
	}

	@Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
		if (mImagePreviewDialog != null && mImagePreviewDialog.isShowing()) {
			mImagePreviewDialog.dismiss();
		}
		mImagePreviewDialog = null;
		mTask = null;
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    	int pos = (int)id;
		if (fileAdapter != null && pos >= 0 && pos < fileAdapter.getSize()) {
	    	String filename = (String) fileAdapter.getItem(pos);
	    	jumpTo(filename);
		}
    }
    
    private void jumpTo(String filename) {
    	if (filename != null) {
	    	if (filename.equals(FileImageWorkerAdapter.JUMP_ROOT)) {
	    		filename = FileImageWorkerAdapter.ROOT_PATH;
	    	} else if (filename.equals(FileImageWorkerAdapter.JUMP_UP)) {
	    		if (fileAdapter == null) {
	    			return;
	    		}
	    		filename = fileAdapter.getParentPath();
	    		if (filename == null) {
	    			filename = FileImageWorkerAdapter.ROOT_PATH;
	    		}
	    	}
    	} else {
    		filename = FileImageWorkerAdapter.ROOT_PATH;
    	}
    	File file = new File(filename);
    	if (file.isDirectory()) {
    		setLastListPos(0);
    		strCurrDir = file.getAbsolutePath();
    		startTask(strCurrDir/*filename*/);
	    } else {
	    	if (!isNoRecord) {
	    		openFile(file, true, true);
	    	} else {
		        switch (getShowType(this)) {
		        default:
		        case SHOW_TYPE_DIALOG:
		        	openFile(file, !this.isNoRecord, !this.isNoRecord);
		        	break;
		        	
		        case SHOW_TYPE_VIEW:
		        	openFile(file, true, false);
		        	break;
		        }
	    	}
    	}
    }
    
	private void openFile(File f, boolean isRecord, boolean isRecordExtra) {
		if (f != null && f.canRead()) {
			Class<?> galleryClass = JkanjiGalleryActivity.class;
			int screenOri;
			if (isRecordExtra) {
				screenOri = JkanjiGallerySettingActivity.getScreenOri(this);
			} else {
				screenOri = JkanjiGallerySettingActivity.getScreenOri2(this);
			}
			if (JkanjiGallerySettingActivity.getAutoCalcOri(this)) {
				screenOri = JkanjiGallerySettingActivity.calcOri(this, f, screenOri);
			}
			switch (screenOri) {
	    	case JkanjiGallerySettingActivity.SCREEN_ORI_PORT:
	    		galleryClass = JkanjiGalleryPortActivity.class;
	    		break;
	    		
	    	case JkanjiGallerySettingActivity.SCREEN_ORI_LAND:
	    		galleryClass = JkanjiGalleryLandActivity.class;
	    		break;
	    	}
	    	if (isRecord) {
				startActivityForResult(new Intent(
					this, galleryClass)
					.putExtra(JkanjiGalleryActivity.EXTRA_KEY_PATH, f.getParent())
					.putExtra(JkanjiGalleryActivity.EXTRA_KEY_FILEID, -1)
					.putExtra(JkanjiGalleryActivity.EXTRA_KEY_FILENAME, f.getName())
					.putExtra(JkanjiGalleryActivity.EXTRA_KEY_ID, -1L)
					.putExtra(JkanjiGalleryActivity.EXTRA_KEY_ZOOM, 1.0f)
					.putExtra(JkanjiGalleryActivity.EXTRA_KEY_PANX, 0.5f)
					.putExtra(JkanjiGalleryActivity.EXTRA_KEY_PANY, 0.5f)
					.putExtra(JkanjiGalleryActivity.EXTRA_KEY_MULTI, true)
					.putExtra(JkanjiGalleryActivity.EXTRA_KEY_ISRECORD, isRecordExtra), REQUEST_GALLERY_OPEN
				);
				if (isRecordExtra) {
					setResult(RESULT_OK);
					finish();
				}
	    	} else {
	    		openImageDialog(f.getAbsolutePath());
	    	}
		} else {
			Toast.makeText(this, 
				"文件不可读", Toast.LENGTH_SHORT)
				.show();
		}
	}
    
    private void clearCache() {
        mImageFetcher.clearCache();
//        Toast.makeText(this, "清空缓存", Toast.LENGTH_SHORT).show();
    }

    private void openImageDialog(String filename) {
		if (mImagePreviewDialog != null && !mImagePreviewDialog.isShowing()) {
			mImagePreviewDialog.dismiss();
			mImagePreviewDialog = null;
		}
		mImagePreviewDialog = new ImagePreviewDialog(this, filename);
		mImagePreviewDialog.show();
    }
    
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
    	super.onCreateContextMenu(menu, v, info);
    	menu.add(0, CONTEXT_MENU_DIALOG, 0, "预览对话框");
    	menu.add(0, CONTEXT_MENU_PLAIN, 0, "图库查看器（只读模式）");
    	menu.add(0, CONTEXT_MENU_SHARE_PHOTO, 0, "共享图片");
	}
	
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    	int pos = -1;
    	switch (item.getItemId()) {
    	case CONTEXT_MENU_DIALOG:
    		pos = (int)info.id;
    		if (fileAdapter != null && pos >= 0 && pos < fileAdapter.getSize()) {
    	    	String filename = (String) fileAdapter.getItem(pos);
    	    	File file = new File(filename);
    	    	openFile(file, false, false);
    		}
    		return true;
    	
    	case CONTEXT_MENU_PLAIN:
    		pos = (int)info.id;
    		if (fileAdapter != null && pos >= 0 && pos < fileAdapter.getSize()) {
    	    	String filename = (String) fileAdapter.getItem(pos);
    	    	File file = new File(filename);
    	    	openFile(file, true, false);
    		}
    		return true;
    		
    	case CONTEXT_MENU_SHARE_PHOTO:
    		pos = (int)info.id;
    		if (fileAdapter != null && pos >= 0 && pos < fileAdapter.getSize()) {
    	    	String filename = (String) fileAdapter.getItem(pos);
    	    	File file = new File(filename);
    	    	sharePhoto(file);
    		}
    		break;
    	}
    	return super.onContextItemSelected(item);
    }
    
	private void sharePhoto(File file) {
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
			Toast.makeText(this, 
				"共享方式出错", Toast.LENGTH_SHORT)
				.show();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_GALLERY_OPEN:
			if (resultCode == RESULT_OK) {
				setResult(RESULT_OK);
			}
			break;
		}
	}
    
    /**
     * The main adapter that backs the GridView. This is fairly standard except the number of
     * columns in the GridView is used to create a fake top row of empty views as we use a
     * transparent ActionBar and don't want the real top row of images to start off covered by it.
     */
    private class ImageAdapter extends BaseAdapter {
        private final Context mContext;
        private int mItemHeight = 0;
        private int mNumColumns = 0;
        private GridView.LayoutParams mImageViewLayoutParams;

        public ImageAdapter(Context context) {
            super();
            mContext = context;
            mImageViewLayoutParams = new GridView.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }

        @Override
        public int getCount() {
            // Size + number of columns for top empty row
//            return (fileAdapter != null ? fileAdapter.getSize() : 0) + mNumColumns;
        	if (fileAdapter != null) {
        		return fileAdapter.getSize();
        	} else {
        		return 0;
        	}
        }

        @Override
        public Object getItem(int position) {
//            return position < mNumColumns ?
//                    null : (fileAdapter != null ? fileAdapter.getItem(position - mNumColumns) : 0);
        	if (fileAdapter != null && position >= 0 && position < fileAdapter.getSize()) {
        		return fileAdapter.getItem(position);
        	} else {
        		return null;
        	}
        }

        @Override
        public long getItemId(int position) {
//            return position < mNumColumns ? 0 : position - mNumColumns;
        	return position;
        }

//        @Override
//        public int getViewTypeCount() {
//            // Two types of views, the normal ImageView and the top row of empty views
//            return 2;
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            return (position < mNumColumns) ? 1 : 0;
//        }
//
        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            // First check if this is the top row
//            if (position < mNumColumns) {
//                if (convertView == null) {
//                    convertView = new View(mContext);
//                }
//                // Set empty view with height of ActionBar
//                convertView.setLayoutParams(new AbsListView.LayoutParams(
//                        ViewGroup.LayoutParams.MATCH_PARENT, 0));
//                return convertView;
//            }

            // Now handle the main ImageView thumbnails
            ImageView imageView;
            if (convertView == null) { // if it's not recycled, instantiate and initialize
                imageView = new RecyclingImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setLayoutParams(mImageViewLayoutParams);
                imageView.setPadding(mImageThumbSpacing, mImageThumbSpacing, 
                	mImageThumbSpacing, mImageThumbSpacing);
            } else { // Otherwise re-use the converted view
                imageView = (ImageView) convertView;
            }

            // Check the height matches our calculated column width
            if (imageView.getLayoutParams().height != mItemHeight) {
                imageView.setLayoutParams(mImageViewLayoutParams);
            }

            // Finally load the image asynchronously into the ImageView, this also takes care of
            // setting a placeholder image while the background thread runs
            if (fileAdapter != null && (position/* - mNumColumns*/) >= 0 && (position/* - mNumColumns*/) < fileAdapter.getSize()) {
            	mImageFetcher.loadImage(fileAdapter.getItem(position/* - mNumColumns*/), imageView);
            }
            return imageView;
        }

        /**
         * Sets the item height. Useful for when we know the column width so the height can be set
         * to match.
         *
         * @param height
         */
        public void setItemHeight(int height) {
            if (height == mItemHeight) {
                return;
            }
            mItemHeight = height;
            mImageViewLayoutParams =
                    new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
            mImageFetcher.setImageSize(height);
            notifyDataSetChanged();
        }

        public void setNumColumns(int numColumns) {
            mNumColumns = numColumns;
        }

        public int getNumColumns() {
            return mNumColumns;
        }
    }
    
	private class LoadDataTask extends AsyncTask<String, Void, Boolean> {
		private boolean loadResult = false;
		private FileImageWorkerAdapter mfileAdapter;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			textViewLoading.setVisibility(View.VISIBLE);
			mGridView.setVisibility(View.INVISIBLE);
            mImageFetcher.setPauseWork(false);
            mImageFetcher.setExitTasksEarly(true);
            mImageFetcher.flushCache();
            clearCache();
            mImageFetcher.setExitTasksEarly(false);
            textViewPath.setText("加载中...");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				mfileAdapter = new FileImageWorkerAdapter(params[0], true, isSortFilenameNum, null);
	    		loadResult = true;
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			textViewLoading.setVisibility(View.INVISIBLE);
			mGridView.setVisibility(View.VISIBLE);
			if (result == true && !isFinishing() && mTask == this) {
				if (loadResult) {
					textViewPath.setText(strCurrDir);
					fileAdapter = mfileAdapter;
			    	mAdapter.notifyDataSetChanged();
			    	mGridView.post(new Runnable() {
			    		@Override
						public void run() {
			    			if (mFilename != null && fileAdapter != null) {
			    				int size = fileAdapter.getSize();
				    			boolean isFound = false;
			    				for (int i = 0; i < size; i++) {
				    				String filename = (String) fileAdapter.getItem(i);
				    				if (filename != null && filename.equals(mFilename)) {
				    					isFound = true;
				    					mGridView.setSelection(i);
				    					mListPos = i;
				    					setLastListPos(mListPos);
				    					break;
				    				}
				    			}
			    				if (!isFound) {
			    					mGridView.setSelection(0);
			    					mListPos = 0;
			    					setLastListPos(mListPos);
			    				}
								setLastDefaultPath(strCurrDir);
				    			mFilename = null;
			    			} else {
			    				String lastPath = getLastDefaultPath();
			    				if (strCurrDir != null && lastPath != null && strCurrDir.equals(lastPath)) {
				    				mListPos = getLastListPos();
				    				mGridView.setSelection(mListPos);
				    			} else {
				    				mListPos = 0;
				    				mGridView.setSelection(mListPos);
			    				}
			    				setLastDefaultPath(strCurrDir);
			    				mFilename = null;
				    		}
						}
			    	});
			    } else {
					Toast.makeText(ImageGrid2Activity.this, "目录加载失败", Toast.LENGTH_SHORT).show();
					setLastDefaultPath(DEFAULT_ROOT);
					setResult(RESULT_CANCELED, null);
					finish();
				}
			} else if (result == false) {
				setResult(RESULT_CANCELED, null);
				finish();
			}
			mTask = null;
		}
    }
	
    public static void setShowConfig(Context context, boolean enable) {
    	PrefUtil.putBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_GRID_SHOW_CONFIG,
    			enable);
    }
    
    public static boolean getShowConfig(Context context) {
		return PrefUtil.getBoolean(context, SHARE_PREF_NAME,
				SHARE_PREF_GRID_SHOW_CONFIG,
    			true);
    }
    
    public static void setShowType(Context context, int type) {
    	PrefUtil.putInt(context, SHARE_PREF_NAME,
    			SHARE_PREF_GRID_SHOW_TYPE,
    			type);
    }
    
    public static int getShowType(Context context) {
		return PrefUtil.getInt(context, SHARE_PREF_NAME,
    			SHARE_PREF_GRID_SHOW_TYPE,
    			SHOW_TYPE_DIALOG);
    }
    
    private void setLastListPos(int listPos) {
    	PrefUtil.putInt(this, SHARE_PREF_NAME,
    			SHARE_PREF_GRID_POS,
    			listPos);
    }
    
    private int getLastListPos() {
    	return PrefUtil.getInt(this, SHARE_PREF_NAME,
    			SHARE_PREF_GRID_POS,
    			0);
    }
    
    private void setLastDefaultPath(String value) {
    	PrefUtil.putString(this, SHARE_PREF_NAME,
    			SHARE_PREF_DEFAULT_PATH,
    			value);
    }
    
    private String getLastDefaultPath() {
    	return PrefUtil.getString(this, SHARE_PREF_NAME,
    			SHARE_PREF_DEFAULT_PATH,
    			DEFAULT_ROOT);
    }
}
