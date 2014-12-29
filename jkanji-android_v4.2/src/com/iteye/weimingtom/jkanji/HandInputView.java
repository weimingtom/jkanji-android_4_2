package com.iteye.weimingtom.jkanji;

import java.util.ArrayList;

import spark.tomoe.DictionaryItem;
import spark.tomoe.HiraganaDictionary;
import spark.tomoe.HiraganaExtraDictionary;
import spark.tomoe.NumberDictionary;
import spark.tomoe.ResultCandidate;
import spark.tomoe.Tomoe;

import android.content.Context;
import android.database.CursorJoiner.Result;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.graphics.PorterDuff;

public class HandInputView extends View {
	private static final boolean D = false;
	private static final String TAG = "HandInputView";
	
	private static final int MAX_OUT = 30;
	
	private String textTip;
	private Paint textPaint;
	private Bitmap mBitmap;
    private Path mPath;
    private Paint mBitmapPaint;
    private Canvas mCanvas;
    private Paint mPaint;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    
	private ArrayList<ArrayList<PointF>> inputStrokes;
	private ArrayList<PointF> inputStroke;
    
	private Tomoe tomoe;
	
	private ArrayAdapter<String> choiseAdapter;
	
	private RectF boundRect;
	
	private DictionaryItem currentDI;
	private Paint paintDI;
	
	private boolean useHiraganaExtra;
	
	public interface OnChoiseChangedHandler {
		public void onChoiseChanged(String text);
		public void onTouchDown();
	}
	
	private OnChoiseChangedHandler onChoiseChangedHandler;
	
	public void setOnChoiseChangedHandler(OnChoiseChangedHandler onChoiseChangedHandler) {
		this.onChoiseChangedHandler = onChoiseChangedHandler;
	}
	
	public HandInputView(Context context) {
		super(context);
		init();
	}
	
    public HandInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    @Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mPath = new Path();
        
        float dpscale = this.getContext().getResources().getDisplayMetrics().density;
        
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFF0000FF);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(5 * dpscale); //FIXME:
        
        paintDI = new Paint();
        paintDI.setAntiAlias(true);
        paintDI.setDither(true);
        paintDI.setColor(0xFF000000);
        paintDI.setStyle(Paint.Style.STROKE);
        paintDI.setStrokeJoin(Paint.Join.ROUND);
        paintDI.setStrokeCap(Paint.Cap.ROUND);
        paintDI.setStrokeWidth(5 * dpscale); //FIXME:        
        
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setDither(true);
        textPaint.setColor(0xFF0000FF);
        float scale = this.getContext().getResources().getDisplayMetrics().scaledDensity;
        textPaint.setTextSize(16 * scale);
        
        clearBitmap();
	}

	private void init() {
		useHiraganaExtra = JkanjiSettingActivity.getUseHiraganaExtra(this.getContext());
		tomoe = new Tomoe(useHiraganaExtra);
		//tomoe.addDictionary(NumberDictionary.getDictionary());
		tomoe.addDictionary(HiraganaDictionary.getDictionary());
		if (useHiraganaExtra) {
			tomoe.addDictionary(HiraganaExtraDictionary.getDictionary());
		}
		boundRect = new RectF();
    }
	
    public void clearBitmap() {
    	if (D)
    		Log.e(TAG, "clearBitmap");
        mBitmapPaint = new Paint();
        if (false) {
        	mBitmapPaint.setAntiAlias(true);
        	mBitmapPaint.setStyle(Paint.Style.FILL);
        	mBitmapPaint.setColor(Color.WHITE/*0xFFCCCCCC*/);
        	mCanvas.drawRect(0, 0, mBitmap.getWidth(), mBitmap.getHeight(), mBitmapPaint);
        } else {
        	mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        mBitmapPaint.setStyle(Paint.Style.STROKE); 
        mBitmapPaint.setStrokeWidth(0);
        mBitmapPaint.setColor(Color.BLACK);
        //mCanvas.drawRect(0, 0, 300, 300, mBitmapPaint);
        getBoundRect(0, 0, this.getWidth()-1, this.getHeight()-1, boundRect);
        mCanvas.drawRect(boundRect, mBitmapPaint);
        mCanvas.drawLine(
        		boundRect.left, 
        		boundRect.top + boundRect.height() / 2, 
        		boundRect.right, 
        		boundRect.top + boundRect.height() / 2, 
        		mBitmapPaint);
        mCanvas.drawLine(
        		boundRect.left + boundRect.width() / 2, 
        		boundRect.top,
        		boundRect.left + boundRect.width() / 2, 
        		boundRect.bottom, 
        		mBitmapPaint);
        
        textTip = "";
        
        invalidate();
        
        inputStrokes = new ArrayList<ArrayList<PointF>>();
    }
    
    private void getBoundRect(float x, float y, float width, float height, RectF result) {
    	float newX, newY, newWidth, newHeight;
    	if (width < height) {
    		newHeight = newWidth = width;
        } else {
    		newHeight = newWidth = height;
        }
    	newX = x + width / 2 - newWidth / 2;
    	newY = y + height / 2 - newHeight / 2;
    	if (result != null) {
    		result.set(newX, newY, newX + newWidth, newY + newHeight);
    	}
    }
    
    @Override
	protected void onDraw(Canvas canvas) {
    	//canvas.drawColor(0xFF000000);
        canvas.drawColor(Color.TRANSPARENT);
    	canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        
    	if (currentDI != null) {
    		int[][][] d = currentDI.d;
    		for (int i = 0; i < d.length; i++) {
    			float[] pts = new float[(d[i].length - 1) * 4];
    			for (int j = 0; j < d[i].length; j++) {
    				if (j == 0) {
    					pts[0] = boundRect.left + d[i][j][0] / 300.0f * boundRect.width();
    					pts[1] = boundRect.top + d[i][j][1] / 300.0f * boundRect.height();    					
    				} else if (j == d[i].length - 1) {
    					pts[2 + (j - 1) * 4] = boundRect.left + d[i][j][0] / 300.0f * boundRect.width();
    					pts[2 + (j - 1) * 4 + 1] = boundRect.top + d[i][j][1] / 300.0f * boundRect.height();
    				} else {
    					pts[2 + (j - 1) * 4] = pts[2 + (j - 1) * 4 + 2] = boundRect.left + d[i][j][0] / 300.0f * boundRect.width();
    					pts[2 + (j - 1) * 4 + 1] = pts[2 + (j - 1) * 4 + 3] = boundRect.top + d[i][j][1] / 300.0f * boundRect.height();
    				}
    			}
    			if (D) {
    				Log.d(TAG, "drawLines : " + pts.length);
    			}
    			canvas.drawLines(pts, paintDI);
    		}
    	}
        
        if (textTip != null) {
    		//canvas.drawText(textTip, 0, -textPaint.ascent(), textPaint);
        	canvas.drawText(textTip, 0, mBitmap.getHeight() - textPaint.descent(), textPaint);
    	}
        canvas.drawPath(mPath, mPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event == null) {
			return false;
		}
        float x = event.getX();
        float y = event.getY();
        
        float x2 = 0, y2 = 0;
        if (!boundRect.isEmpty()) {
        	x2 = (x - boundRect.left) / (boundRect.width()) * 300f;
        	y2 = (y - boundRect.top) / (boundRect.height()) * 300f;  
        }
        
    	switch(event.getAction()) {
    	case MotionEvent.ACTION_DOWN:
    		if (D)
    			Log.e(TAG, "processTouch down: x:" + x + ", y:" + y);
    		touch_start(x, y);
    		startStroke(x2, y2);
    		invalidate();
    		if (onChoiseChangedHandler != null) {
    			onChoiseChangedHandler.onTouchDown();
    		}
    		return true;
    		
    	case MotionEvent.ACTION_MOVE:
    		if (D)
    			Log.e(TAG, "processTouch move: x:" + x + ", y:" + y);
    		touch_move(x, y);
    		addPoint(x2, y2);
    		invalidate();
    		return true;
    		
    	case MotionEvent.ACTION_UP:
    		if (D)
    			Log.e(TAG, "processTouch up: x:" + x + ", y:" + y);
    		touch_up();
    		endStroke(x2, y2);
    		invalidate();
    		return true;
    	}
    	return super.onTouchEvent(event);
    }
	
    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }
    
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }
    
    private void touch_up() {
        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint);
        mPath.reset();
    }
	
	private void startStroke(float x, float y) {
		if (D)
			Log.e(TAG, "startStroke");
		inputStroke = new ArrayList<PointF>();
		inputStroke.add(new PointF(x, y));
	}
	
	private void addPoint(float x, float y) {
		inputStroke.add(new PointF(x, y));
	}
	
	private void endStroke(float x, float y) {
		if (D) {
			Log.e(TAG, "endStroke");
			Log.e(TAG, "inputStrokes " + inputStroke);
		}
		inputStrokes.add(inputStroke);
		long startTime = System.currentTimeMillis();
		ArrayList<ResultCandidate> res = tomoe.getMatched(inputStrokes, MAX_OUT);
		long progressTime = System.currentTimeMillis() - startTime;
		//
		if (choiseAdapter != null) {
			choiseAdapter.clear();
		}
		StringBuffer sb = new StringBuffer();
		if (res != null) {
			for (int i = 0; i < res.size(); ++i) {
				if (D)
					Log.e(TAG, "letter:" + res.get(i).letter + ", sccroe:" + res.get(i).score);
				sb.append(res.get(i).letter);
				if (choiseAdapter != null) {
					choiseAdapter.add(Character.toString(res.get(i).letter));
				}
			}
		}
		textTip = sb.toString();
		//
		int len = (res!=null) ? res.size() : 0;
		if (D)
			Log.e(TAG, len + "個候補を見つけました (処理時間 " + progressTime + "ms)");
		if (onChoiseChangedHandler != null) {
			onChoiseChangedHandler.onChoiseChanged(textTip);
		}
	}
	
	public void setChoiseAdapter(ArrayAdapter<String> adapter) {
		this.choiseAdapter = adapter;
	}
	
	public boolean study(char c) {
		return tomoe.study(c, inputStrokes);
	}
	
	public void setDictionaryItem(DictionaryItem item) {
		if (D) {
			if (item != null) {
				Log.d(TAG, "setDictionaryItem : not null");
			}
		}
		currentDI = item;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
	}
	
}
