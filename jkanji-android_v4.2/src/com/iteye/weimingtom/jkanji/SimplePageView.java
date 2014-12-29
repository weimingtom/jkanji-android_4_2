package com.iteye.weimingtom.jkanji;

import fi.harism.curl.CurlActivity;
import fi.harism.curl.CurlPage;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class SimplePageView extends View {
	public interface SizeChangedObserver {
		public void onSizeChanged(int width, int height);
	}
	
	public interface PageProvider {
		public void updatePage(SimplePageView view, int width, int height, int index);
	}
	
	private Bitmap bitmap;
	private SizeChangedObserver sizeChangedObserver;
	private PageProvider pageProvider;
	private int curPage;

	public SimplePageView(Context context) {
		super(context);
		init(context);
	}

	public SimplePageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SimplePageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.WHITE);
		if (bitmap != null) {
			canvas.drawBitmap(bitmap, 0, 0, null);
		}
	}
	
	public void setPageBitmap(Bitmap bitmap) {
		if (this.bitmap != null) {
			this.bitmap.recycle();
			this.bitmap = null;
		}
		this.bitmap = bitmap;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		//FIXME:
		if (this.bitmap != null) {
			this.bitmap.recycle();
			this.bitmap = null;			
		}
	}

	public void setSizeChangedObserver(SizeChangedObserver sizeChangedObserver) {
		this.sizeChangedObserver = sizeChangedObserver;
	}

	public void setPageProvider(PageProvider pageProvider) {
		this.pageProvider = pageProvider;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (sizeChangedObserver != null) {
			sizeChangedObserver.onSizeChanged(w, h);
		}
		setPage(curPage);
	}

	public void setPage(int index) {
		//FIXME:
		if (index < 0) {
			index = 0;
		}
		curPage = index;
		int w = this.getWidth();
		int h = this.getHeight();
		if (pageProvider != null && w != 0 && h != 0) {
			pageProvider.updatePage(this, w, h, index);
		}
		this.invalidate();
	}
}
