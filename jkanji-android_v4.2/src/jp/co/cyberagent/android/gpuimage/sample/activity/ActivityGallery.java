/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.cyberagent.android.gpuimage.sample.activity;

import java.io.File;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.GPUImageView.OnPictureSavedListener;
import jp.co.cyberagent.android.gpuimage.sample.GPUImageFilterTools;
import jp.co.cyberagent.android.gpuimage.sample.GPUImageFilterTools.FilterAdjuster;
import jp.co.cyberagent.android.gpuimage.sample.GPUImageFilterTools.OnGpuImageFilterChosenListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class ActivityGallery extends Activity {
    private static final int REQUEST_PICK_IMAGE = 1;
    private static final String GPUIMAGE_CACHE = "gpuimage_temp.jpg";
    
    private ActionBar actionBar;
    private GPUImageFilter mFilter;
    private FilterAdjuster mFilterAdjuster;
    private GPUImageView mGPUImageView;
    private SeekBar mSeekBar;
    private Button buttonChooseFilter;
    private Button buttonSave;
    private int currentItem = -1;
    
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gpuimage_activity_gallery);
        
		actionBar = (ActionBar) findViewById(R.id.actionbar);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mGPUImageView = (GPUImageView) findViewById(R.id.gpuimage);
        buttonChooseFilter = (Button) findViewById(R.id.button_choose_filter);
        buttonSave = (Button) findViewById(R.id.button_save);
        
		actionBar.setTitle("GLES2图片滤镜");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.gpuimage;
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
				pickImage();
			}
        });

        mSeekBar.setVisibility(View.GONE);
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
		        if (mFilterAdjuster != null) {
		            mFilterAdjuster.adjust(progress);
		        }
		        mGPUImageView.requestRender();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}        	
        });

        buttonChooseFilter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                GPUImageFilterTools.showDialog(ActivityGallery.this, currentItem, new OnGpuImageFilterChosenListener() {
                    @Override
                    public void onGpuImageFilterChosenListener(final GPUImageFilter filter, int pos) {
                        switchFilterTo(filter);
        		        if (mFilterAdjuster != null) {
        		        	int progress = mSeekBar.getProgress();
        		            mFilterAdjuster.adjust(progress);
        		        }
                        mGPUImageView.requestRender();
                        currentItem = pos;
                    }
                });
			}
        });
        
//        buttonSave.setVisibility(View.GONE);
        buttonSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveImage();
			}
        });
        
//        pickImage();
    }

    private void pickImage() {
		try {
	        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
	        photoPickerIntent.setType("image/*");
	        startActivityForResult(photoPickerIntent, REQUEST_PICK_IMAGE);
        } catch (Throwable e) {
			e.printStackTrace();
			Toast.makeText(this, 
				"找不到可用的图片查看器", Toast.LENGTH_SHORT)
				.show();
		}
    }
    
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
        case REQUEST_PICK_IMAGE:
            if (resultCode == RESULT_OK && data != null) {
                mGPUImageView.setImage(data.getData());
		        if (mFilterAdjuster != null) {
		        	int progress = mSeekBar.getProgress();
		            mFilterAdjuster.adjust(progress);
		        }
		        mGPUImageView.requestRender();
            }
            break;

        default:
            super.onActivityResult(requestCode, resultCode, data);
            break;
        }
    }
    
    private void saveImage() {
        mGPUImageView.saveToPictures(GPUIMAGE_CACHE, new OnPictureSavedListener() {
			@Override
			public void onPictureSaved(Uri uri) {
				if (uri != null) {
					sharePhoto(uri);
				}
			}
        });
    }
    
    private void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null || 
        	(filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;
            mGPUImageView.setFilter(mFilter);
            mFilterAdjuster = new FilterAdjuster(mFilter);
            if (mFilterAdjuster.canAdjust()) {
            	mSeekBar.setVisibility(View.VISIBLE);
            } else {
            	mSeekBar.setVisibility(View.GONE);
            }
        }
    }

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		removeCache();
	}
	
	private void removeCache() {
		if (false) {
			try {
				File file = this.getFileStreamPath(GPUIMAGE_CACHE);
				file.delete();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else {
			try {
				this.deleteFile(GPUIMAGE_CACHE);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	private void sharePhoto(Uri uri) {
    	Intent intent;
		intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("image/*");
		intent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
        	startActivity(intent);
        } catch (Throwable e) {
			e.printStackTrace();
			Toast.makeText(this, 
				"共享方式出错,找不到合适的程序", Toast.LENGTH_SHORT)
				.show();
		}
	}
}
