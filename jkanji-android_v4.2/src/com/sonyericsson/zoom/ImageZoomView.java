package com.sonyericsson.zoom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * @see http://www.icodelogic.com/?p=601
 * @author Administrator
 *
 */
public class ImageZoomView extends View {
	private final static boolean D = false;
	private final static String TAG = "ImageZoomView";
	
	private static final boolean USE_GESTURE_DETECTOR = false;
//    private static final float MIN_ZOOM = 1;
    private static final float MAX_ZOOM = 16;
	private float mMinZoom = 1;
    
    private Paint mPaint;
    private Rect mRectSrc;
    private Rect mRectDst;
    private float mAspectQuotient;
    private Bitmap mBitmap;
    private float mZoom;
    private float mPanX;
    private float mPanY;
    private boolean mEnableMultiTouch = true;
    
    private boolean isUseMask = false;
    private boolean isShowFileName = false;
    private boolean isUseUpDown = false;
    private Paint bgRectPaint, fileNamePaint;
    private String mFileName;
    
    public interface OnPrevNextListener {
    	public void onPrev();
    	public void onNext();
    };
    private OnPrevNextListener mOnPrevNextListener;
    
    public enum ControlType {
        PAN, ZOOM
    }
    private ControlType mControlType = ControlType.PAN;
    
    private final class ImageOnTouchListener implements View.OnTouchListener {
        private float mX;
        private float mY;
        private float mDownX;
        private float mDownY;
        private float mDownS;
        private float mCount = 0;
        private float mDownSX;
        private float mDownSY;

        public void reset() {
            mX = 0;
            mY = 0;
            mDownX = 0;
            mDownY = 0;
            mDownS = 0;
            mCount = 0;
            mDownSX = 0;
            mDownSY = 0;
        }
        
        public boolean onTouch(View v, MotionEvent event) {
        	if (v == null || event == null) {
        		return false;
        	}
        	if (mDetector != null) {
        		mDetector.onTouchEvent(event);
        	}
            final int action = event.getAction();
            final float x = event.getX();
            final float y = event.getY();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = x;
                    mDownY = y;
                    mX = x;
                    mY = y;
                    if (mEnableMultiTouch && event.getPointerCount() == 2) {
                    	mCount = 2;
            			float x_ = (event.getX(0) - event.getX(1)) / v.getWidth();
                        float y_ = (event.getY(0) - event.getY(1)) / v.getHeight();
                        mDownS = (float) Math.sqrt(x_ * x_ + y_ * y_);
                        mDownSX = (event.getX(0) + event.getX(1)) / 2;
                        mDownSY = (event.getY(0) + event.getY(1)) / 2;
                    }
                    break;

                case MotionEvent.ACTION_MOVE: {
                    final float dx = (x - mX) / v.getWidth();
                    final float dy = (y - mY) / v.getHeight();
                	if (!mEnableMultiTouch) {
	                    if (mControlType == ControlType.ZOOM) {
//                            if (D) {
//                            	Log.d(TAG, "1 : " + (float)Math.pow(20, -dy) + ", " + (-dy));
//                            }
	                    	zoom((float)Math.pow(20, -dy), 
	                    		mDownX / v.getWidth(), 
	                    		mDownY / v.getHeight());
	                    } else {
	                    	pan(-dx, -dy);
	                    }
                	} else {
                		switch(event.getPointerCount()) {
                		case 2:
                			float x_ = (event.getX(0) - event.getX(1)) / v.getWidth();
                            float y_ = (event.getY(0) - event.getY(1)) / v.getHeight();
                            float s_ = (float) Math.sqrt(x_ * x_ + y_ * y_);
                            if (mDownS == 0 || mCount != 2) {
                				mDownS = s_;
                                mDownSX = (event.getX(0) + event.getX(1)) / 2;
                                mDownSY = (event.getY(0) + event.getY(1)) / 2;
                			}
                            mCount = 2;
                            float ds = mDownS - s_;
//                            if (D) {
//                            	Log.d(TAG, "2 : " + ds);
//                            }
                            zoom((float)Math.pow(20, -ds), 
                            	mDownSX / v.getWidth(), 
                            	mDownSY / v.getHeight());
                            mDownS = s_;
                			break;
                			
                		case 1:
                			boolean enablePan = true;
                			if (mCount != 1) {
                				enablePan = false;
                			}
                			mCount = 1;
                			if (enablePan) {
                				pan(-dx, -dy);
                			}
                			break;
                			
                		default:
                			mCount = 0;
                			break;
                        }
                	}
                    mX = x;
                    mY = y;
                    break;
                }
                
                case MotionEvent.ACTION_UP: {
                	mDownS = 0;
                	mCount = 0;
                	break;
                }
            }
            return true;
        }
    };
    
    private ImageOnTouchListener mTouchListener;
    private GestureDetector mDetector;
    private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        	//if (D) {
        	//	Log.d(TAG, "onScroll");
        	//}
        	if (USE_GESTURE_DETECTOR && e1 != null && e2 != null) {
	        	final float dx = (e2.getX() - e1.getX()) / getWidth();
	        	final float dy = (e2.getY() - e1.getY()) / getHeight();
	        	if (mControlType == ControlType.ZOOM) {
	            	zoom((float)Math.pow(20, -dy), 
	            		e1.getX() / getWidth(), 
	            		e1.getY() / getHeight());
	            } else {
	            	pan(-dx, -dy);
	            }
	        	return true;
	        } else {
	        	return false;
	        }
        }

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (!JkanjiGallerySettingActivity.getUseDoubleTap(getContext())) {
				return false;
			}
			if (e != null && getWidth() > 0 && getHeight() > 0) {
	            final float x = e.getX();
	            final float y = e.getY();
	            if (mZoom < mMinZoom/*MIN_ZOOM*/ * 2.0f) {
	            	zoom((float)2.0f, x / getWidth(), y / getHeight());
	            } else {
	            	setZoom(1.0f);
	            	setPanX(0.5f);
	            	setPanY(0.5f);
	            	invalidate();
	            }
			}
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if (!USE_GESTURE_DETECTOR) {
				if (e != null && mOnPrevNextListener != null) {
					if (isUseUpDown) {
						if (e.getY() > (float)getHeight() / 2.0f) {
							mOnPrevNextListener.onPrev();
						} else {
							mOnPrevNextListener.onNext();
						}
					} else {
						if (e.getX() < (float)getWidth() / 2.0f) {
							mOnPrevNextListener.onPrev();
						} else {
							mOnPrevNextListener.onNext();
						}
					}
				}
				return true;
			} else {
				return false;
			}
		}
    };
    
    public ImageZoomView(Context context) {
    	super(context);
    	init();
    }
    
    public ImageZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ImageZoomView(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    	init();
    }
    
    private void init() {
        isUseMask = JkanjiGallerySettingActivity.getUseMask(this.getContext());
        isShowFileName = JkanjiGallerySettingActivity.getShowFileName(this.getContext());
		isUseUpDown = JkanjiGallerySettingActivity.getUseUpDown(this.getContext());
        
        bgRectPaint = new Paint();
		bgRectPaint.setColor(Color.BLACK);
		bgRectPaint.setAlpha(125);
		fileNamePaint = new Paint();
		fileNamePaint.setColor(Color.RED);
		fileNamePaint.setAntiAlias(true);
		fileNamePaint.setDither(true);
		fileNamePaint.setFilterBitmap(true);
		float scale = 1.0f;
		try {
			scale = getContext().getResources().getDisplayMetrics().scaledDensity;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		fileNamePaint.setTextSize(12 * scale);
		fileNamePaint.setFakeBoldText(true);
		
    	mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    	mRectSrc = new Rect();
    	mRectDst = new Rect();
    	
    	this.setClickable(true);
    	this.setFocusable(true);
    	this.setLongClickable(true);
		mTouchListener = new ImageOnTouchListener();
    	mDetector = new GestureDetector(this.getContext(), mSimpleOnGestureListener);
    	disableTouch();
    }
    
    public void enableTouch() {
    	if (USE_GESTURE_DETECTOR) {
    		setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                	return mDetector.onTouchEvent(event);
                }
            });
    	} else {
    		if (mTouchListener == null) {
    			mTouchListener = new ImageOnTouchListener();
    		}
    		setOnTouchListener(mTouchListener);
    	}    	
    }

    public void disableTouch() {
    	setOnTouchListener(null);
    	if (mTouchListener != null) {
    		mTouchListener.reset();
    	}
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            final int viewWidth = getWidth();
            final int viewHeight = getHeight();
            final int bitmapWidth = mBitmap.getWidth();
            final int bitmapHeight = mBitmap.getHeight();
            final float panX = mPanX;
            final float panY = mPanY;
            final float zoomX = getZoomX(mAspectQuotient) * viewWidth / bitmapWidth;
            final float zoomY = getZoomY(mAspectQuotient) * viewHeight / bitmapHeight;
            
            mRectSrc.left = (int)(panX * bitmapWidth - viewWidth / (zoomX * 2));
            mRectSrc.top = (int)(panY * bitmapHeight - viewHeight / (zoomY * 2));
            mRectSrc.right = (int)(mRectSrc.left + viewWidth / zoomX);
            mRectSrc.bottom = (int)(mRectSrc.top + viewHeight / zoomY);
            mRectDst.left = 0; //getLeft();
            mRectDst.top = 0; //getTop();
            mRectDst.right = viewWidth; //getRight();
            mRectDst.bottom = viewHeight; //getBottom();

            if (mRectSrc.left < 0) {
                mRectDst.left += -mRectSrc.left * zoomX;
                mRectSrc.left = 0;
            }
            if (mRectSrc.right > bitmapWidth) {
                mRectDst.right -= (mRectSrc.right - bitmapWidth) * zoomX;
                mRectSrc.right = bitmapWidth;
            }
            if (mRectSrc.top < 0) {
                mRectDst.top += -mRectSrc.top * zoomY;
            	//mRectDst.top = (int)(viewHeight * panY - zoomY * bitmapHeight / 2);
                mRectSrc.top = 0;
            }
            if (mRectSrc.bottom > bitmapHeight) {
                mRectDst.bottom -= (mRectSrc.bottom - bitmapHeight) * zoomY;
                //mRectDst.bottom = (int)(viewHeight * panY + zoomY * bitmapHeight / 2);
            	mRectSrc.bottom = bitmapHeight;
            }
//            if (D) {
//            	Log.d(TAG, "w:" + viewWidth + ", h:" + viewHeight + ", panx:" + panX + ", pany:" + panY + ", zoomX" + zoomX + ", zoomY" + zoomY +
//            			" w2:" + mRectDst.width() + ", h2:" + mRectDst.height() + 
//            			" w3:" + bitmapWidth + ", h3:" + bitmapHeight);
//            }
            canvas.drawBitmap(mBitmap, mRectSrc, mRectDst, mPaint);
            if (isUseMask) {
            	canvas.drawRect(mRectDst, bgRectPaint);
            }
            if (isShowFileName && mFileName != null) {
            	canvas.drawText(mFileName, 0, -fileNamePaint.ascent() + fileNamePaint.descent(), fileNamePaint);
            }
        }
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mBitmap != null) {
	        updateAspectQuotient(
	        		right - left, 
	        		bottom - top, 
	        		mBitmap.getWidth(),
	                mBitmap.getHeight());
	        updateBasicZoomControl();
	    }
    }

	/**
     * 改变缩放（基于中心点的相对位置）
     * @param f
     * @param x
     * @param y
     */
    public void zoom(float f, float x, float y) {
        final float prevZoomX = getZoomX(mAspectQuotient);
        final float prevZoomY = getZoomY(mAspectQuotient);
        setZoom(mZoom * f);
        limitZoom();
        final float newZoomX = getZoomX(mAspectQuotient);
        final float newZoomY = getZoomY(mAspectQuotient);
        setPanX(mPanX + (x - .5f) * (1f / prevZoomX - 1f / newZoomX));
        setPanY(mPanY + (y - .5f) * (1f / prevZoomY - 1f / newZoomY));
        limitPan();
        invalidate();
    }

    /**
     * 改变相对位置
     * @param dx
     * @param dy
     */
    public void pan(float dx, float dy) {
        setPanX(mPanX + dx / getZoomX(mAspectQuotient));
        setPanY(mPanY + dy / getZoomY(mAspectQuotient));
        limitPan();
        invalidate();
    }

    private float getMaxPanDelta(float zoom) {
        return Math.max(0f, .5f * ((zoom - 1) / zoom));
    }

    private void limitZoom() {
        if (mZoom < mMinZoom/*MIN_ZOOM*/) {
            setZoom(mMinZoom/*MIN_ZOOM*/);
        } else if (mZoom > MAX_ZOOM) {
            setZoom(MAX_ZOOM);
        }
    }

    private void limitPan() {
        final float zoomX = getZoomX(mAspectQuotient);
        final float zoomY = getZoomY(mAspectQuotient);
        final float panMinX = .5f - getMaxPanDelta(zoomX);
        final float panMaxX = .5f + getMaxPanDelta(zoomX);
        final float panMinY = .5f - getMaxPanDelta(zoomY);
        final float panMaxY = .5f + getMaxPanDelta(zoomY);
        if (mPanX < panMinX) {
            setPanX(panMinX);
        }
        if (mPanX > panMaxX) {
        	if (D) {
        		Log.e(TAG, "panMaxX:" + panMaxX + ",zoomX:" + zoomX + ",mAspectQuotient:" + mAspectQuotient + ",mZoom:" + mZoom);
        	}
            setPanX(panMaxX);
        }
        if (mPanY < panMinY) {
            setPanY(panMinY);
        }
        if (mPanY > panMaxY) {
            setPanY(panMaxY);
        }
    }

    private void updateBasicZoomControl() {
        limitZoom();
        limitPan();
    }
    
    /**
     * 改变相对位置和改变缩放
     * @param panX
     */
    public void setPanX(float panX) {
    	if (D) {
    		Log.e(TAG, "setPanX:" + panX);
    		if (panX == 0.5f) {
    			try {
    				throw new Exception("setPanX");
    			} catch (Throwable e) {
    				e.printStackTrace();
    			}
    		}
    	}
        if (panX != mPanX) {
            mPanX = panX;
            invalidate();
        }
    }
    
    public float getPanX() {
    	return mPanX;
    }

    public void setPanY(float panY) {
        if (panY != mPanY) {
            mPanY = panY;
            invalidate();
        }
    }

    public float getPanY() {
    	return mPanY;
    }

    
    public void setZoom(float zoom) {
        if (zoom != mZoom) {
            mZoom = zoom;
            invalidate();
        }
    }
    
    public float getZoom() {
    	return mZoom;
    }
    
    private float getZoomX(float aspectQuotient) {
        return Math.min(mZoom, mZoom * aspectQuotient);
    }

    private float getZoomY(float aspectQuotient) {
        return Math.min(mZoom, mZoom / aspectQuotient);
    }
    
    /**
     * 替换图片（改变长宽比）
     * @param bitmap
     */
    public void setImage(Bitmap bitmap, String filename) {
        mBitmap = bitmap;
        mFileName = filename;
        mMinZoom = 1.0f;
        if (mBitmap != null) {
        	int vw = getWidth();
        	int vh = getHeight();
        	int w = mBitmap.getWidth();
        	int h = mBitmap.getHeight();
        	if (vw > 0 && vh > 0 && 
        		w > 0 && h > 0 && 
        		w < vw && h < vh) {
        		mMinZoom = Math.min((float)w / vw, (float)h / vh);
        	}
        	updateAspectQuotient(
	        		getWidth(), 
	        		getHeight(), 
	        		mBitmap.getWidth(), 
	        		mBitmap.getHeight());
	        updateBasicZoomControl();
	    }
        invalidate();
    }
    
    private void updateAspectQuotient(float viewWidth, float viewHeight, float contentWidth, float contentHeight) {
        final float aspectQuotient = (contentWidth / contentHeight) / (viewWidth / viewHeight);
        if (aspectQuotient != mAspectQuotient) {
            mAspectQuotient = aspectQuotient;
            updateBasicZoomControl();
        }
    }
    
    /**
     * 设置手势模式
     * @param controlType
     */
    public void setControlType(ControlType controlType) {
        mControlType = controlType;
    }
    
    public void setEnableMultiTouch(boolean enableMultiTouch) {
    	mEnableMultiTouch = enableMultiTouch;
    }
    
    public boolean getEnableMultiTouch() {
    	return mEnableMultiTouch;
    }
    
    public void setOnPrevNextListener(OnPrevNextListener listener) {
    	this.mOnPrevNextListener = listener;
    }
    
    public void resizeWidth() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            final int viewWidth = getWidth();
            final int viewHeight = getHeight();
            final int bitmapWidth = mBitmap.getWidth();
            final int bitmapHeight = mBitmap.getHeight();
	    	float rate = 1;
	        if (bitmapWidth / bitmapHeight < (float)viewWidth / viewHeight) {
	        	rate = viewWidth / (bitmapWidth * viewHeight / bitmapHeight);
	        }
	        setZoom(rate);
	    }
    }

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}
}
