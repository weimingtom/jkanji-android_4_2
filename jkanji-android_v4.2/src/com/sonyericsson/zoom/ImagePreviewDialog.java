package com.sonyericsson.zoom;

import com.iteye.weimingtom.jkanji.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ImagePreviewDialog extends Dialog {
	private String mFilename;
	private Bitmap mBitmap;
	private ImageView imageViewBitmap;
	private TextView textViewLoading;
	private FrameLayout frameLayoutTop;
    private boolean use16Bits = false;
    private BitmapFactory.Options options16Bits;
	
	public ImagePreviewDialog(Context context, String filename) {
		super(context);
		mFilename = filename;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        
        use16Bits = JkanjiGallerySettingActivity.getUse16Bits(this.getContext());
        
        this.setContentView(R.layout.bitmapfun_preview);
        textViewLoading = (TextView) this.findViewById(R.id.textViewLoading);
        imageViewBitmap = (ImageView) this.findViewById(R.id.imageViewBitmap);
        frameLayoutTop = (FrameLayout) this.findViewById(R.id.frameLayoutTop);
        frameLayoutTop.setOnClickListener(new ImageView.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
        });
        textViewLoading.setVisibility(View.VISIBLE);
        imageViewBitmap.setVisibility(View.INVISIBLE);
        textViewLoading.setText("加载中...");
        
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setCanceledOnTouchOutside(true);
        
        new LoadDataTask().execute(mFilename);
    }
    
    @Override
	protected void onStart() {
		super.onStart();
    }
    
	@Override
	protected void onStop() {
		super.onStop();
		imageViewBitmap.setImageBitmap(null);
		if (mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		imageViewBitmap.setImageBitmap(null);
		if (mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
	}

	private class LoadDataTask extends AsyncTask<String, Void, Boolean> {
		private boolean loadResult = false;
		private int viewWidth, viewHeight;
		private Bitmap bitmap;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			textViewLoading.setVisibility(View.VISIBLE);
			imageViewBitmap.setVisibility(View.INVISIBLE);
			if (getContext() != null && getContext().getResources() != null) {
				DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
				if (dm != null) {
					viewWidth = dm.widthPixels;
					viewHeight = dm.heightPixels;
				}
			}
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				bitmap = decodeSampledBitmapFromFile(params[0], viewWidth, viewHeight, use16Bits);
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
			imageViewBitmap.setVisibility(View.VISIBLE);
			if (loadResult && isShowing()) {
				mBitmap = bitmap;
				if (mBitmap != null && !mBitmap.isRecycled()) {
					imageViewBitmap.setImageBitmap(mBitmap);
	    			Animation ani = AnimationUtils.loadAnimation(getContext(), R.anim.bubble_fade);
	    			imageViewBitmap.startAnimation(ani);
				} else {
					textViewLoading.setVisibility(View.VISIBLE);
					imageViewBitmap.setVisibility(View.INVISIBLE);
					textViewLoading.setText("图片加载失败");
				}
			} else {
				textViewLoading.setVisibility(View.VISIBLE);
				imageViewBitmap.setVisibility(View.INVISIBLE);
				textViewLoading.setText("图片加载失败");
			}
		}
    }
	
    private static synchronized Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight, boolean isUse16Bits) {
    	final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
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
}
