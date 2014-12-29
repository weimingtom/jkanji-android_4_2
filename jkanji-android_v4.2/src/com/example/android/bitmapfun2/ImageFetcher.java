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

package com.example.android.bitmapfun2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

import java.io.File;

import com.example.android.bitmapfun.util.DrawTextUtil;
import com.iteye.weimingtom.jkanji.R;

/**
 * A simple subclass of {@link ImageResizer} that fetches and resizes images fetched from a URL.
 */
public class ImageFetcher extends ImageResizer {
    private static final boolean D = false;
	private static final String TAG = "ImageFetcher";

    private Context mContext;
    private Bitmap bitmapFolder;
    
    /**
     * Initialize providing a target image width and height for the processing images.
     *
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public ImageFetcher(Context context, int imageWidth, int imageHeight) {
        super(context, imageWidth, imageHeight);
        init(context);
    }

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageSize
     */
    public ImageFetcher(Context context, int imageSize) {
        super(context, imageSize);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        bitmapFolder = BitmapFactory.decodeResource(this.mContext.getResources(), R.drawable.bitmapfun_folder);
    }

    private Bitmap processBitmap(String data) {
        if (D) {
            Log.d(TAG, "processBitmap - " + data);
        }
        final File f;
        //FIXME: local disk file
        if (data != null && data.startsWith("/")) {
            File file = new File(data);
            if (file.isFile() && file.exists()) {
            	f = file;
            	Bitmap bmp = decodeSampledBitmapFromFile(f.toString(), mImageWidth, mImageHeight, getImageCache());
            	if (bmp != null) {
            		return bmp;
            	} else {
                	Bitmap bitmap = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
                	Canvas canvas = new Canvas(bitmap);
                	canvas.drawColor(Color.WHITE);
                	DrawTextUtil tu = new DrawTextUtil();
                	float textSize = 16 * this.mContext.getResources().getDisplayMetrics().scaledDensity;
                	tu.initText(data, 0, 0, mImageWidth, mImageHeight, textSize, Color.BLACK, false, Color.BLACK);
                	tu.drawText(canvas);
                	return bitmap;
            	}
            } else if (file.isDirectory()) {
            	Bitmap bitmap = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
            	Canvas canvas = new Canvas(bitmap);
            	canvas.drawColor(Color.WHITE);
            	canvas.drawBitmap(bitmapFolder, 
            			new Rect(0, 0, bitmapFolder.getWidth(), bitmapFolder.getHeight()), 
            			new Rect(0, 0, mImageWidth, mImageHeight), 
            			null);
            	DrawTextUtil tu = new DrawTextUtil();
            	float textSize = 16 * this.mContext.getResources().getDisplayMetrics().scaledDensity;
            	String filename = file.getName();
            	tu.initText(filename, 0, 0, mImageWidth, mImageHeight, textSize, Color.BLACK, false, Color.BLACK);
            	tu.drawText(canvas);
            	return bitmap;
            }
        } else if (data != null) {
        	//向上，根目录
        	Bitmap bitmap = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
        	Canvas canvas = new Canvas(bitmap);
        	canvas.drawColor(Color.WHITE);
        	DrawTextUtil tu = new DrawTextUtil();
        	float textSize = 16 * this.mContext.getResources().getDisplayMetrics().scaledDensity;
        	tu.initText(data, 0, 0, mImageWidth, mImageHeight, textSize, Color.BLACK, false, Color.BLACK);
        	tu.drawText(canvas);
        	return bitmap;
        }
        return null;
    }
    
    @Override
    protected Bitmap processBitmap(Object data) {
        return processBitmap(String.valueOf(data));
    }
}
