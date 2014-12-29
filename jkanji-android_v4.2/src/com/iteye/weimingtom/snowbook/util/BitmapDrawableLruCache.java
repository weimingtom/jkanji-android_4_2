package com.iteye.weimingtom.snowbook.util;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.LruCache;

public class BitmapDrawableLruCache extends LruCache<String, BitmapDrawable> {
	private boolean isClearAll = false;
	
	public BitmapDrawableLruCache() {
		super((int) (Runtime.getRuntime().maxMemory() / 1024));
	}
	
	public BitmapDrawableLruCache(int maxSize) {
		super(maxSize);
	}

	@Override
    protected void entryRemoved(boolean evicted, String key,
            BitmapDrawable oldValue, BitmapDrawable newValue) {
        if (oldValue instanceof RecyclingBitmapDrawable) {
            ((RecyclingBitmapDrawable) oldValue).setIsCached(false);
			if (isClearAll) {
				((RecyclingBitmapDrawable) oldValue).forceDelete();
			}
        }
    }

	@Override
	protected int sizeOf(final String key, final BitmapDrawable drawable) {
		Bitmap bitmap = drawable.getBitmap();
		int size = 0;
		if (bitmap != null) {
			size = bitmap.getRowBytes() * bitmap.getHeight() / 1024;
		}
		if (size == 0) {
        	size = 1;
        }
        return size;
	}
	
	public BitmapDrawable putDrawable(String rsid, BitmapDrawable drawable) {
		if (drawable instanceof RecyclingBitmapDrawable) {
			((RecyclingBitmapDrawable) drawable).setIsCached(true);
        }
		return super.put(rsid, drawable);
	}
	
	public BitmapDrawable getDrawable(Resources res, AssetManager am, String rsid) {
		BitmapDrawable drawable = get(rsid);
		if (drawable == null) {
			InputStream istr = null;
			try {
				istr = am.open(rsid);
				drawable = new RecyclingBitmapDrawable(res,
						BitmapFactory.decodeStream(istr));
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (istr != null) {
					try {
						istr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			this.putDrawable(rsid, drawable);
	    }
		return drawable;
	}
	
	public BitmapDrawable getDrawable(Resources res, int rsid) {
		String rsidValue = Integer.toString(rsid);
		BitmapDrawable drawable = get(rsidValue);
		if (drawable == null) {
			drawable = new RecyclingBitmapDrawable(res, 
					BitmapFactory.decodeResource(res, rsid));
			this.putDrawable(rsidValue, drawable);
	    }
		return drawable;
	}
	
	public void clearAll() {
		isClearAll = true;
		this.evictAll();
	}
}
