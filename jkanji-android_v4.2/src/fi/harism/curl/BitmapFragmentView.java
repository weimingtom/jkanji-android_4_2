package fi.harism.curl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class BitmapFragmentView extends View {
	public interface SizeChangedObserver {
		public void onSizeChanged(int width, int height);
	}
	
	private Bitmap bitmap;
	private SizeChangedObserver sizeChangedObserver;
	private Paint textPaintLoading;
	
	public BitmapFragmentView(Context context) {
		super(context);
		init(context);
	}

	public BitmapFragmentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public BitmapFragmentView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		textPaintLoading = new Paint();
		float scale = this.getResources().getDisplayMetrics().scaledDensity;
		textPaintLoading.setTextSize(18 * scale);
		textPaintLoading.setAntiAlias(true);
		textPaintLoading.setTextAlign(Paint.Align.CENTER);
		textPaintLoading.setColor(Color.BLACK);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(Color.WHITE);
		if (bitmap != null) {
			canvas.drawBitmap(bitmap, 0, 0, null);
		} else {
			String progressInfo = "数据加载中...";
			canvas.drawText(progressInfo, this.getWidth() / 2, this.getHeight() / 2, textPaintLoading);
		}
	}
	
	public void setPageBitmap(Bitmap bitmap) {
		if (this.bitmap != null) {
			this.bitmap.recycle();
			this.bitmap = null;
		}
		this.bitmap = bitmap;
		this.invalidate();
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
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (sizeChangedObserver != null) {
			sizeChangedObserver.onSizeChanged(w, h);
		}
	}
}
