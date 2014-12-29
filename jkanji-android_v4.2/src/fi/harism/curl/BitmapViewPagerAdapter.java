package fi.harism.curl;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class BitmapViewPagerAdapter extends PagerAdapter {
	private final static boolean D = false;
	private final static String TAG = "BitmapViewPagerAdapter";
	
	private int pageCount = 1;
	private ViewPagerActivity mContext;

	private boolean isTryLoad = false;
	private int mTryLoadIndex = 0;
	
    public BitmapViewPagerAdapter(ViewPagerActivity context) {
        super();
        mContext = context;
    }

	public void setPageCount(int count) {
		this.pageCount = count;
	}
    
    @Override
    public int getCount() {
        return this.pageCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object obj) {
        return view == (View) obj;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
    	final BitmapFragmentView image = new BitmapFragmentView(mContext);
        image.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        //FIXME:
        if (false) {
	        image.setSizeChangedObserver(new BitmapFragmentView.SizeChangedObserver() {
				@Override
				public void onSizeChanged(int width, int height) {
					updatePage(mContext, image, width, height, position);
				}
	        });
        }
        image.setClickable(false);
        image.setTag(Integer.toString(position));
        if (D) {
        	Log.e(TAG, "setTag == " + Integer.toString(position));
        }
        if (isTryLoad && mTryLoadIndex == position) {
        	mContext.updatePagerAsync(image, mContext.mPager.getWidth(), mContext.mPager.getHeight(), position);
        	isTryLoad = false;
        }
        
        LinearLayout layout = new LinearLayout(mContext);
        layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        layout.addView(image);
        
        ((ViewPager) container).addView(layout);
        return layout;
    }
    
    @Override
    public void destroyItem(View container, int position, Object object) {
        ((ViewPager) container).removeView((View) object);
    }
    
	private void updatePage(ViewPagerActivity activity, BitmapFragmentView view, int width, int height, int index) {
		if (activity != null) {
			if (D) {
				Log.d(TAG, "updatePage " + index + ", " + width + "," + height);
			}
			activity.updatePager(view, width, height, index);
		}				
	}
	
	public void tryLoad(int index) {
		isTryLoad = true;
		mTryLoadIndex = index;
	}
}
