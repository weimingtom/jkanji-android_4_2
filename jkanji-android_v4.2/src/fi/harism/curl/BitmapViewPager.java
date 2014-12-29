package fi.harism.curl;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

public class BitmapViewPager extends ViewPager {
	public interface SizeChangedObserver {
		public void onSizeChanged(int width, int height);
	}
	
	private SizeChangedObserver sizeChangedObserver;
	private int curPage;
	
	public BitmapViewPager(Context context) {
		super(context);
	}
	
	public BitmapViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setPage(int index) {
		PagerAdapter adapter = this.getAdapter();
		if (adapter != null) {
			if (index < 0) {
				index = 0;
			}
			if (index >= adapter.getCount()) {
				index = adapter.getCount() - 1;
			}
			curPage = index;
			this.setCurrentItem(curPage, true);
		}
	}
	
	public void setSizeChangedObserver(SizeChangedObserver sizeChangedObserver) {
		this.sizeChangedObserver = sizeChangedObserver;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (sizeChangedObserver != null) {
			sizeChangedObserver.onSizeChanged(w, h);
		}
	}
}
