/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.iteye.weimingtom.snowbook.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

/**
 * A BitmapDrawable that keeps track of whether it is being displayed or cached.
 * When the drawable is no longer being displayed or cached,
 * {@link Bitmap#recycle() recycle()} will be called on this drawable's bitmap.
 */
public class RecyclingBitmapDrawable extends BitmapDrawable {
	private static boolean D = false;
    private static final String TAG = "RecyclingBitmapDrawable";
    public static boolean isShowStack = false;

    private int mCacheRefCount = 0;
    private int mDisplayRefCount = 0;

    private boolean mHasBeenDisplayed;
    public static int numInstances = 0;

    public RecyclingBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
        if (D) {
        	if (bitmap != null) {
        		Log.e(TAG, "new RecyclingBitmapDrawable() "
                    + toString() + " - " + getBitmap().toString() + " = " + getBitmap().getWidth() + "x" + getBitmap().getHeight());
        	} else {
        		Log.e(TAG, "new RecyclingBitmapDrawable() "
                        + toString() + " - " + "null");
            }
        }
        if (bitmap != null) {
        	numInstances++;
        }
    }

    /**
     * Notify the drawable that the displayed state has changed. Internally a
     * count is kept so that the drawable knows when it is no longer being
     * displayed.
     *
     * @param isDisplayed - Whether the drawable is being displayed or not
     */
    public void setIsDisplayed(boolean isDisplayed) {
        synchronized (this) {
            if (isDisplayed) {
                mDisplayRefCount++;
                mHasBeenDisplayed = true;
            } else {
                mDisplayRefCount--;
            }
        }
        if (D) {
        	Log.e(TAG, "setIsDisplayed mDisplayRefCount "
                        + toString() + " - " + mDisplayRefCount);
        	if (isShowStack) {
            	Log.e(TAG, "setIsDisplayed size "
                        + getBitmap().getWidth() + " x " + getBitmap().getHeight());
        		DebugUtil.printStack();
        	}
        }
        
        // Check to see if recycle() can be called
        checkState();
    }

    /**
     * Notify the drawable that the cache state has changed. Internally a count
     * is kept so that the drawable knows when it is no longer being cached.
     *
     * @param isCached - Whether the drawable is being cached or not
     */
    public void setIsCached(boolean isCached) {
        synchronized (this) {
            if (isCached) {
                mCacheRefCount++;
            } else {
                mCacheRefCount--;
            }
        }

        // Check to see if recycle() can be called
        checkState();
    }

    private synchronized void checkState() {
        // If the drawable cache and display ref counts = 0, and this drawable
        // has been displayed, then recycle
        if (mCacheRefCount <= 0 && mDisplayRefCount <= 0 && mHasBeenDisplayed
                && hasValidBitmap()) {
            if (D) {
                Log.e(TAG, "No longer being used or cached so recycling. "
                        + toString() + " - " + getBitmap().toString() + " w=" + this.getBitmap().getWidth() + ",h=" + this.getBitmap().getHeight());
            }
            getBitmap().recycle();
            numInstances--;
        }
    }

    private synchronized boolean hasValidBitmap() {
        Bitmap bitmap = getBitmap();
        return bitmap != null && !bitmap.isRecycled();
    }
    
    //for GridView Bug
    public synchronized void forceDelete() {
    	if (hasValidBitmap()) {
            if (D) {
                Log.e(TAG, "Force delete cache . " + " - " +
                        toString() + " - " + getBitmap().toString() + " w=" + this.getBitmap().getWidth() + ",h=" + this.getBitmap().getHeight() + 
                        "|CacheRefCount=" + mCacheRefCount + "|mDisplayRefCount=" + mDisplayRefCount + "|HasBeenDisplayed=" + mHasBeenDisplayed);
            }
            getBitmap().recycle();
            numInstances--;
        }
    }
}
