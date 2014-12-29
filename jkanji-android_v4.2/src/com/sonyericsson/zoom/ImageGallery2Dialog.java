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

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bitmapfun.provider.FileImageWorkerAdapter;
import com.example.android.bitmapfun2.ImageFetcher;
import com.example.android.bitmapfun2.RecyclingImageView;
import com.example.android.bitmapfun2.ImageCache.ImageCacheParams;
import com.iteye.weimingtom.jkanji.R;

public class ImageGallery2Dialog extends Dialog implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener{
    private static final boolean D = false;
	private static final String TAG = "ImageGallery2Activity";
    
    private static final String IMAGE_CACHE_DIR = "thumbs";
    public static final String EXTRA_KEY_FILENAME = "ImageGalleryActivity.EXTRA_KEY_FILENAME";
    
    private int mImageThumbSize;
    private ImageAdapter mAdapter;
    private ImageFetcher mImageFetcher;
    
    private FileImageWorkerAdapter fileAdapter;
    private Gallery mGallery;
    private TextView textViewLoading;
    private TextView textViewTitle;
    
    private boolean mIsSortFilenameNum;
    private String mFilename;
    private String mSelectedFilename;
    private int mSelectedPos;
    private boolean isLoading = true;
    
    private String[] mFileNames;
    
    public ImageGallery2Dialog(Context context, String filename, boolean isSortFilenameNum, String[] fileNames) {
		super(context);
		this.mFilename = filename;
		this.mIsSortFilenameNum = isSortFilenameNum;
		this.mFileNames = fileNames;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        
        mImageThumbSize = getContext().getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);

        mAdapter = new ImageAdapter(getContext());

        ImageCacheParams cacheParams = new ImageCacheParams(getContext(), IMAGE_CACHE_DIR);

        //FIXME:内存大小百分比
        cacheParams.setMemCacheSizePercent(0.50f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(getContext(), mImageThumbSize);
        mImageFetcher.setLoadingImage(R.drawable.bitmapfun_empty_photo, 
        	R.drawable.bitmapfun_empty_photo_scale);
        mImageFetcher.addImageCache(cacheParams);
        
        this.setContentView(R.layout.bitmapfun_gallery);
        
        textViewLoading = (TextView) this.findViewById(R.id.textViewLoading);
        
        mGallery = (Gallery) findViewById(R.id.galleryBitmap);
        mGallery.setAdapter(mAdapter);

        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        textViewTitle.setText("加载中...\n0 / 0");
        // This listener is used to get the final width of the GridView and then calculate the
        // number of columns and the width of each column. The width of each column is variable
        // as the GridView has stretchMode=columnWidth. The column width is used to set the height
        // of each view so we get nice square thumbnails.
        mGallery.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
//            	mImageFetcher.setImageSize(mGallery.getWidth(), mGallery.getHeight());
            }
        });
        
        //fileAdapter = new FileImageWorkerAdapter("/sdcard/book5/neco");
        String pathname = new File(mFilename).getParent();
        new LoadDataTask().execute(pathname);
        
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setCanceledOnTouchOutside(true);
    }
    
    @Override
	protected void onStart() {
		super.onStart();
        mImageFetcher.setExitTasksEarly(false);
        mAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
        clearCache();
	}

    @Override
	public void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		mImageFetcher.closeCache();
	}

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		int pos = (int)id;
		if (fileAdapter != null && pos >= 0 && pos < fileAdapter.getSize()) {
	    	String filename = (String) fileAdapter.getItem(pos);
	    	if (filename != null) {
		    	if (filename.equals(FileImageWorkerAdapter.JUMP_ROOT)) {
		    		filename = FileImageWorkerAdapter.ROOT_PATH;
		    	} else if (filename.equals(FileImageWorkerAdapter.JUMP_UP)) {
		    		filename = fileAdapter.getParentPath();
		    		if (filename == null) {
		    			filename = FileImageWorkerAdapter.ROOT_PATH;
		    		}
		    	}
	    	} else {
	    		filename = FileImageWorkerAdapter.ROOT_PATH;
	    	}
	    	if (new File(filename).isDirectory()) {
	    		new LoadDataTask().execute(filename);
		    } else {
//	    		Intent result = new Intent();
//	    		result.putExtra(EXTRA_KEY_FILENAME, filename);
//	    		this.setResult(RESULT_OK, result);
//	    		finish();
		    	if (!isLoading) {
		    		mSelectedFilename = filename;
		    		mSelectedPos = pos;
		    	} else {
		    		mSelectedFilename = null;
		    		mSelectedPos = 0;
		    	}
		    	dismiss();
	    	}
		}
    }
    
    private void clearCache() {
        mImageFetcher.clearCache();
//        Toast.makeText(this, "清空缓存", Toast.LENGTH_SHORT).show();
    }

    /**
     * The main adapter that backs the GridView. This is fairly standard except the number of
     * columns in the GridView is used to create a fake top row of empty views as we use a
     * transparent ActionBar and don't want the real top row of images to start off covered by it.
     */
    private class ImageAdapter extends BaseAdapter {
        private final Context mContext;
        private Gallery.LayoutParams layoutParams;
        
        public ImageAdapter(Context context) {
            super();
            mContext = context;
            layoutParams = new Gallery.LayoutParams(mImageThumbSize, mImageThumbSize);
        }

        @Override
        public int getCount() {
            // Size + number of columns for top empty row
            return (fileAdapter != null ? fileAdapter.getSize() : 0);
        }

        @Override
        public Object getItem(int position) {
            return position < 0 ?
                    null : (fileAdapter != null ? fileAdapter.getItem(position) : 0);
        }

        @Override
        public long getItemId(int position) {
            return position < 0 ? 0 : position;
        }

        @Override
        public int getViewTypeCount() {
            // Two types of views, the normal ImageView and the top row of empty views
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return (position < 0) ? 1 : 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            // First check if this is the top row
            if (position < 0) {
                if (convertView == null) {
                    convertView = new View(mContext);
                }
                return convertView;
            }
            
            
            FrameLayout frameLayout;
            ImageView imageView;
            if (convertView == null) {
            	imageView = new RecyclingImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                frameLayout = new FrameLayout(mContext);
                frameLayout.addView(imageView);
            } else {
            	frameLayout = (FrameLayout) convertView;
            	imageView = (ImageView)frameLayout.getChildAt(0);
            }
//            imageView.setAlpha(0x80);
            frameLayout.setLayoutParams(layoutParams);
            if (fileAdapter != null && position >= 0 && position < fileAdapter.getSize()) {
	            String filename = (String)fileAdapter.getItem(position);
	            mImageFetcher.loadImage(filename, imageView);
	        }
            
//            int size = fileAdapter.getSize();
//            textViewTitle.setText(filename + "\n" +
//            		Integer.toString(position) + " / " + Integer.toString(size));
            return frameLayout;
        }
    }
    
	private class LoadDataTask extends AsyncTask<String, Void, Boolean> {
		private boolean loadResult = false;
		private FileImageWorkerAdapter mfileAdapter;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			textViewLoading.setVisibility(View.VISIBLE);
			mGallery.setVisibility(View.INVISIBLE);
            mImageFetcher.setPauseWork(false);
            mImageFetcher.setExitTasksEarly(true);
            mImageFetcher.flushCache();
            clearCache();
            mImageFetcher.setExitTasksEarly(false);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				if (params[0] != null) { 
					mfileAdapter = new FileImageWorkerAdapter(params[0], false, mIsSortFilenameNum, mFileNames);
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
			if (isShowing()) {
				textViewLoading.setVisibility(View.INVISIBLE);
				mGallery.setVisibility(View.VISIBLE);
				if (loadResult) {
					fileAdapter = mfileAdapter;
			    	mAdapter.notifyDataSetChanged();
			    	setFileName(mFilename);
			    } else {
					Toast.makeText(ImageGallery2Dialog.this.getContext(), "目录加载失败", Toast.LENGTH_SHORT).show();
				}
			}
		}
    }

	public void setFileName(String filename) {
		if (filename != null && filename.length() > 0 && fileAdapter != null) {
			int size = fileAdapter.getSize();
			for (int i = 0; i < size; i++) {
				String fn = (String) fileAdapter.getItem(i);
				if (fn != null && filename.equals(fn)) {
					mGallery.setSelection(i);
					break;
				}
			}
		} else {
			mGallery.setSelection(0);
		}
        mGallery.setOnItemClickListener(this);
        mGallery.setOnItemSelectedListener(this);
        isLoading = false;
	}

	public String getSelectedFilename() {
		return mSelectedFilename;
	}
	
	public int getSelectedPos() {
		return mSelectedPos;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		int size = fileAdapter.getSize();
		if (position >= 0 && position < size) {
			String filename = (String)fileAdapter.getItem(position);	
			textViewTitle.setText(filename + "\n" +
				Integer.toString(position + 1) + " / " + Integer.toString(size));
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}
}
