package com.sonyericsson.zoom;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Hacky fix for Issue #4 and
 * http://code.google.com/p/android/issues/detail?id=18990
 * 
 * ScaleGestureDetector seems to mess up the touch events, which means that
 * ViewGroups which make use of onInterceptTouchEvent throw a lot of
 * IllegalArgumentException: pointerIndex out of range.
 * 
 * There's not much I can do in my code for now, but we can mask the result by
 * just catching the problem and ignoring it.
 * 
 * @author Chris Banes
 * 
 * @see uk.co.senab.photoview.sample.HackyViewPager
 * 
 * @see https://github.com/blork/anpod/blob/2a7faf1323d33ba8f615a308079b3e92647ee7ad/src/com/blork/anpod/view/CustomViewPager.java
 * 
 */
public class HackyViewPager extends ViewPager {
	private boolean enabled;
	
	public HackyViewPager(Context context) {
		super(context);
		init();
	}

    public HackyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
    	this.enabled = true;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.enabled) {
            try {
            	return super.onTouchEvent(event);
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
    
    /**
     * @see http://www.wandoujia.com/apps/com.xotof
     * @see AndroidTouchGallery
     */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean isLimitPan = true;
		try {
			ImagePagerView zoomView = (ImagePagerView)(this.findViewWithTag(Integer.toString(this.getCurrentItem())));
			isLimitPan = zoomView.isLimitPan();
//			Log.d("HackyViewPager", "isLimitPan == " + isLimitPan);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (this.enabled && isLimitPan) {
			try {
				return super.onInterceptTouchEvent(ev);
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
