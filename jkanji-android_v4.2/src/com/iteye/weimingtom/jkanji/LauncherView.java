package com.iteye.weimingtom.jkanji;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.iteye.weimingtom.jkanji.R;

public class LauncherView extends View implements View.OnTouchListener {
	private final static boolean D = false;
	private final static String TAG = "LauncherView";
	
	public interface OnSelectIconListener {
		public void onSelectIcon(int index);
	}
	
    private Integer[] thumbIds = {
            R.drawable.search, R.drawable.view, 
            //R.drawable.bookmark, R.drawable.game, 
            R.drawable.book, R.drawable.help};
	private String[] gallery_labels;
	private String[] note_labels;
	
    private Bitmap[] thumbBitmaps;
    private Rect[] thumbRects;
	private static final int DELAY = 1000 / 12;
	private static final int ROTATE_DEGREE = - 36;
	private long lastTick = 0;
	private Matrix matrix = new Matrix();
	private Matrix rotateMatrix = new Matrix();
	private Bitmap halftone;
	private Paint magicCirclePaint;
	private Paint subjectPaint;
	private Paint descriptionPaint;
	private Paint subjectMaskPaint;
	private Paint iconPaint;
	private Paint iconTextPaint;
	private Paint iconFocusTextPaint;
	private Paint iconShadowPaint;
	private RectF subjectMaskRect;
	private Paint bgPaint;
	private Matrix bgMatrix = new Matrix();
	
	private boolean isStopped = false;
	private Rect bounds = new Rect();
		
	private String subject;
	private int subjectAlpha = 0;
	private String description;
	private int selectedIndex = -1;
	
	private OnSelectIconListener selectIconListener;
	
	public LauncherView(Context context) {
		super(context);
		init(context);
	}
	
	public LauncherView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public LauncherView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		setFocusable(true);
		requestFocus();
		
		bgPaint = new Paint();
		bgPaint.setAntiAlias(true);
		bgPaint.setFilterBitmap(true);
		
		iconPaint = new Paint();
		iconPaint.setAntiAlias(true);
		iconPaint.setFilterBitmap(true);
		
		float scale = this.getContext().getResources().getDisplayMetrics().scaledDensity;
		
		iconTextPaint = new Paint();
		iconTextPaint.setAntiAlias(true);
		iconTextPaint.setTextAlign(Paint.Align.CENTER);
		iconTextPaint.setColor(Color.WHITE);
		iconTextPaint.setTextSize(12 * scale);
		iconTextPaint.setShadowLayer(3, 1, 1, Color.BLACK);

		iconFocusTextPaint = new Paint();
		iconFocusTextPaint.setAntiAlias(true);
		iconFocusTextPaint.setTextAlign(Paint.Align.CENTER);
		iconFocusTextPaint.setColor(Color.BLUE);
		iconFocusTextPaint.setTextSize(12 * scale);
		
		iconShadowPaint = new Paint();
		iconShadowPaint.setAntiAlias(true);
		iconShadowPaint.setFilterBitmap(true);
		iconShadowPaint.setColor(Color.WHITE);
		//iconShadowPaint.setAlpha(0x80);
		//iconShadowPaint.setShadowLayer(2, 2, 2, Color.BLACK);
		
		magicCirclePaint = new Paint();
		magicCirclePaint.setAntiAlias(true);
		magicCirclePaint.setFilterBitmap(true);
		
		subjectPaint = new Paint();
		subjectPaint.setAntiAlias(true);
		subjectPaint.setColor(Color.WHITE);
		subjectPaint.setTextSize(36 * scale);
		subjectPaint.setTextAlign(Paint.Align.RIGHT);
		//subjectPaint.setShadowLayer(2, 1, 1, Color.BLACK);

		descriptionPaint = new Paint();
		descriptionPaint.setAntiAlias(true);
		descriptionPaint.setColor(Color.BLACK);
		descriptionPaint.setTextSize(18 * scale);
		descriptionPaint.setTextAlign(Paint.Align.RIGHT);
		
		subjectMaskPaint = new Paint();
		subjectMaskPaint.setAntiAlias(true);
		subjectMaskPaint.setColor(Color.BLUE);
		subjectMaskPaint.setAlpha(0x80);
		subjectMaskRect = new RectF();
		
		halftone = BitmapFactory.decodeResource(context.getResources(), R.drawable.halftone);
		
		thumbBitmaps = new Bitmap[this.thumbIds.length];
		thumbRects = new Rect[this.thumbIds.length];
		for (int i = 0; i < this.thumbIds.length; i++) {
			thumbBitmaps[i] = BitmapFactory.decodeResource(context.getResources(), this.thumbIds[i]);
			thumbRects[i] = new Rect();
		}
		
		this.gallery_labels = getResources().getStringArray(R.array.gallery_labels);
		this.note_labels = getResources().getStringArray(R.array.note_labels);
		
		showInfo(-1);
		
		this.setOnTouchListener(this);
	}
	
	private Rect thumbRect = new Rect();
	private RectF thumbRectF = new RectF();
	private Rect thumbBitmapRect = new Rect();
	
	@Override
	public void onDraw(Canvas canvas) {
        long time = System.currentTimeMillis() - lastTick;
    	float w = this.getWidth();
    	float h = this.getHeight();
    	
    	if (w > 0 && h > 0) {
    		float bgScale;
    		if (w < h) {
		    	if (w / h > (float)this.halftone.getWidth() / this.halftone.getHeight()) {
		    		bgScale = w / (float)this.halftone.getWidth();
		    		if (D) {
		    			Log.d(TAG, "scale 1");
		    		}
		    	} else {
		    		bgScale = h / (float)this.halftone.getHeight();
		    		if (D) {
		    			Log.d(TAG, "scale 2");
		    		}
		    	}
    		} else {
		    	if (w / h < (float)this.halftone.getWidth() / this.halftone.getHeight()) {
		    		bgScale = h / (float)this.halftone.getHeight();
		    		if (D) {
		    			Log.d(TAG, "scale 3");
		    		}
		    	} else {
		    		bgScale = w / (float)this.halftone.getWidth();
		    		if (D) {
		    			Log.d(TAG, "scale 4");
		    		}
		    	}
    		}
	    	bgMatrix.setScale(bgScale, bgScale);
	    	canvas.drawBitmap(halftone, bgMatrix, bgPaint);
	    	
	    	int iny = 3;
	    	int idymax = (int)w;
	    	if (w > h) {
	    		iny = 3;
	    		idymax = (int)h;
	    	}
	    	int idh = (int)(idymax / iny);
	    	for (int i = 0; i < thumbBitmaps.length; i++) {
	    		thumbRect.set((i / iny) * idh + idh / 4, (i % iny) * idh + idh / 4, (i / iny) * idh + idh - idh / 4, (i % iny) * idh + idh - idh / 4);
	    		thumbRectF.set(thumbRect);
	    		thumbRects[i].set(thumbRect);
	    		if (i == selectedIndex) {
	    			iconShadowPaint.clearShadowLayer();
	    			canvas.drawRoundRect(thumbRectF, 5f, 5f, iconShadowPaint);
	    		} else {
	    			iconShadowPaint.setShadowLayer(2, 2, 2, Color.BLACK);
	    			canvas.drawRoundRect(thumbRectF, 5f, 5f, iconShadowPaint);
	    		}
	    		thumbBitmapRect.set(0, 0, thumbBitmaps[i].getWidth(), thumbBitmaps[i].getHeight());
	    		canvas.drawBitmap(thumbBitmaps[i], 
	    			thumbBitmapRect,
	    			thumbRect, 
	    			iconPaint);
	    		if (i == selectedIndex) {
		    		canvas.drawText(gallery_labels[i], 
			    			(i / iny) * idh + idh / 2, 
			    			(i % iny) * idh + idh, 
			    			iconFocusTextPaint);    			
	    		} else {
		    		canvas.drawText(gallery_labels[i], 
		    			(i / iny) * idh + idh / 2, 
		    			(i % iny) * idh + idh, 
		    			iconTextPaint);
	    		}
	    	}
	    	
	    	/*
	        if (time >= DELAY && isStopped == false) {
	        	lastTick = System.currentTimeMillis();
	        	float scalew = w / magiccircle.getWidth();
	        	float scaleh = h / magiccircle.getHeight();
	        	float scale = scalew < scaleh ? scalew: scaleh;
	        	float dx = 0;
	        	float dy = 0;
	        	if (scalew < scaleh) {
	        		dx = (w - scalew * magiccircle.getWidth() / 2);
	        		dy = (h - scalew * magiccircle.getHeight() / 2);
	        	} else {
	        		dx = (w - scaleh * magiccircle.getWidth() / 2);
	        		dy = (h - scaleh * magiccircle.getHeight() / 2);
	        	}
	        	matrix.setScale(scale, scale);
	        	rotateMatrix.postRotate(ROTATE_DEGREE, magiccircle.getWidth() / 2f, magiccircle.getHeight() / 2f);
	        	matrix.preConcat(rotateMatrix);
	        	matrix.postTranslate(dx, dy);
	        }
	    	canvas.drawBitmap(magiccircle, matrix, magicCirclePaint);
	    	*/
	    	
	    	if (subjectAlpha <= 255) {
	    		subjectPaint.clearShadowLayer();
	    		subjectPaint.setAlpha(subjectAlpha);
	    		subjectAlpha += 256 / 8;
	    		if (subjectAlpha > 255) {
	    			subjectPaint.setShadowLayer(2, 1, 1, Color.BLACK);
	    		}
	    	}
	    	subjectPaint.getTextBounds(subject, 0, subject.length(), bounds);
	    	subjectMaskRect.left = w - bounds.width() - 20;
	    	subjectMaskRect.right = w + bounds.width() + 20;
	    	subjectMaskRect.top = h - subjectPaint.descent() + descriptionPaint.descent() + descriptionPaint.ascent() - bounds.height() / 4;
	    	subjectMaskRect.bottom = subjectMaskRect.top + bounds.height() / 2;
	    	canvas.drawRoundRect(subjectMaskRect, 10, 10, subjectMaskPaint);
	    	canvas.drawText(subject, w + bounds.width() / 256.0f * (256 - subjectAlpha), h - subjectPaint.descent() + descriptionPaint.ascent(), subjectPaint);
	    	
	    	if (subjectAlpha >= 255) {
	    		canvas.drawText(description, w, h - descriptionPaint.descent(), descriptionPaint);
	    	}
	    	
	    	postInvalidateDelayed(DELAY);
    	}
	}
	
	public void stopAniamate() {
		isStopped = true;
	}
	
	public void startAnimate() {
		isStopped = false;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event == null) {
			return false;
		}
		float x = event.getX();
		float y = event.getY();
		int lastShowShadow = this.selectedIndex;
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			this.selectedIndex = -1;
			for (int i = 0; i < thumbRects.length; i++) {
				if (thumbRects[i].contains((int)x, (int)y)) {
					//if (lastShowShadow == i) {
					//	if (selectIconListener != null) {
					//		selectIconListener.onSelectIcon(i);
					//	}
					//}
					this.selectedIndex = i;
				}
			}
			showInfo(selectedIndex);
			return true;
			
		case MotionEvent.ACTION_UP:
			this.selectedIndex = -1;
			for (int i = 0; i < thumbRects.length; i++) {
				if (thumbRects[i].contains((int)x, (int)y)) {
					//if (lastShowShadow == i) {
						if (selectIconListener != null) {
							selectIconListener.onSelectIcon(i);
						}
					//}
					this.selectedIndex = i;
				}
			}
			//showInfo(showShadow);
			return true;
		}
		return false;
	}
	
	public void showInfo(int showShadow) {
		if (showShadow >= 0 && showShadow < gallery_labels.length) {
			this.subject = this.gallery_labels[showShadow];
			this.subjectAlpha = 0;
			this.description = this.note_labels[showShadow];
			this.selectedIndex = showShadow;
		} else {
			this.subject = "日语简易词典";
			this.subjectAlpha = 0;
			this.description = "点击按钮开始";
			this.selectedIndex = showShadow;
		}
	}
	
	public void setOnSelectIconListener(OnSelectIconListener selectIconListener) {
		this.selectIconListener = selectIconListener;
	}

	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		if (D) {
			Log.e(TAG, "onDetachedFromWindow");
		}
		try {
			if (thumbBitmaps != null) {
				for (int i = 0; i < thumbBitmaps.length; i++) {
					if (thumbBitmaps[i] != null) {
						thumbBitmaps[i].recycle();
					}
				}
			}
			if (this.halftone != null) {
				this.halftone.recycle();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
