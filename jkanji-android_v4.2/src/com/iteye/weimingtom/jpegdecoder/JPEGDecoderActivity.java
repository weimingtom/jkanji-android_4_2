package com.iteye.weimingtom.jpegdecoder;

import java.io.File;
import java.io.FileInputStream;

import com.iteye.weimingtom.jkanji.R;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

public class JPEGDecoderActivity extends Activity {
	private static final int REQUEST_PICK_IMAGE = 1;
	private final static String SAMPLE_FILE_NAME = "/sdcard/infinite_stratos_00000001.jpg";
	
	private ActionBar actionBar;
	private ImageView imageView1;
    private JpegDecoderTask task;
    private Bitmap mBitmap;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.jpeg_decoder);
        
        imageView1 = (ImageView) findViewById(R.id.imageView1);
        actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("jpeg极慢速软解码");
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
        
        
    }
    
    private void pickImage() {
		try {
	        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
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
            if (resultCode == RESULT_OK && 
            	data != null &&
				data.getData() != null) {
				String resultPath = data.getData().getPath();
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
					if (task != null) {
						task.setStop(true);
					}
					task = new JpegDecoderTask(file.getAbsolutePath());
	        		task.execute();
				}
            }
            break;

        default:
            super.onActivityResult(requestCode, resultCode, data);
            break;
        }
    }
    
    
    @Override
	protected void onStop() {
		super.onStop();
		if (task != null) {
			task.setStop(true);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (task != null) {
			task.setStop(true);
		}
		if (mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
			mBitmap = null;
		}
	}

	private final class JpegDecoderTask extends AsyncTask<Void, Void, Void> implements PixelArray {
        private JPEGDecoder j = null;
    	private String file;
    	
    	private Bitmap bitmap;
        private volatile boolean isStop = false;

        public void setStop(boolean value) {
        	this.isStop = value;
        }
        
        public JpegDecoderTask(String s) {
			file = s;
        }
        
        @Override
        public void setSize(int width, int height) {
        	this.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    		publishProgress();
    		if (isStop) {
    			throw new RuntimeException("Activity has stopped");
    		}
    	}

        @Override
        public void setPixel(int x, int y, int argb) {
        	if (this.bitmap != null) {
        		bitmap.setPixel(x, y, argb);
        	}
        	publishProgress();
    		if (isStop) {
    			throw new RuntimeException("Activity has stopped");
    		}
        }

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			j = new JPEGDecoder();
			actionBar.setTitle("Bild:" + file);
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
			    FileInputStream in = new FileInputStream(file);
			    j.decode(in, this);
			    in.close();
			} catch (Throwable e) {
			    System.out.println("Etwas ging schief: " + e);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			if (isStop) {
				if (this.bitmap != null && !this.bitmap.isRecycled()) {
					this.bitmap.recycle();
					this.bitmap = null;
				}
			} else {
				actionBar.setTitle("Progress:" + j.progress() + "%");
				imageView1.setImageBitmap(this.bitmap);
				mBitmap = this.bitmap;
			}
		}
    }
}
