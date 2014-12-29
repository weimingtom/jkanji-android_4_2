package com.sonyericsson.zoom;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * @see http://www.icodelogic.com/?p=601
 * @author Administrator
 *
 */
public class ImageScrollView extends View {
	private final static boolean D = false;
	private final static String TAG = "ImageScrollView";
	
	private static final int INVALIDATE_DELAY = 10;
	
	private static final boolean USE_GESTURE_DETECTOR = false;
    private static final float MIN_ZOOM = 1;
    private static final float MAX_ZOOM = 16;
	
    private Paint mPaint;
    private Rect mRectSrc;
    private Rect mRectDst;
    private float mAspectQuotient;
    private Point[] mBitmapSizes;
    private float mZoom;
    private float mPanX;
    private float mPanY;
    private boolean mEnableMultiTouch = true;
    
    private boolean isUseMask = false;
    private boolean isShowFileName = false;
    private boolean isUseUpDown = false;
    private Paint bgRectPaint, fileNamePaint;
    private String[] mFileName;
    private volatile boolean mUseOptions;
    private int mPageMargin;
    
    private volatile LoadDataTask task;
    
    private RectF mViewRect = new RectF();
    private RectF mDstRectAll = new RectF();
    private Rect mSrcRect = new Rect();
    private RectF mDstRect = new RectF();
    
    private float mBitmapTotalWidth, mBitmapTotalHeight;
    private PointF[] mHeightRates;
    
    private ArrayList<Integer> mLoadList = new ArrayList<Integer>();
    private ArrayList<Integer> mFailList = new ArrayList<Integer>();
    private Bitmap[] mLoadBitmaps;
    private Paint paintRect = new Paint();
    private volatile BitmapFactory.Options options16Bits;
    private volatile boolean use16Bits = false;
    
    private volatile boolean useViewContentProvider = false;
    
    public interface OnPrevNextListener {
    	public void onPrev();
    	public void onNext();
    	public void onPageUpdate(int page);
    	public Bitmap actLoad(String filename, int reqWidth, int reqHeight, boolean isUse16Bits, boolean isSample);
    };
    private volatile OnPrevNextListener mOnPrevNextListener;
    
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

        /*
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (e != null && getWidth() > 0 && getHeight() > 0) {
	            final float x = e.getX();
	            final float y = e.getY();
	        	float rate = 1;
	        	try {
		            if (mBitmapTotalWidth / mBitmapTotalHeight < (float)getWidth() / getHeight()) {
		            	rate = getWidth() / (mBitmapTotalWidth * getHeight() / mBitmapTotalHeight);
		            }
	        	} catch (Throwable ex) {
	        		ex.printStackTrace();
	        	}
	            if (mZoom < MAX_ZOOM * rate) {
	            	zoom((float)2.0f, x / getWidth(), y / getHeight());
	            } else {
	            	setZoom(1.0f * rate);
	            	setPanX(x / getWidth());
	            	setPanY(y / getHeight());
	            }
			}
			return true;
		}
        */
        
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
    
    public ImageScrollView(Context context) {
    	super(context);
    	init();
    }
    
    public ImageScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ImageScrollView(Context context, AttributeSet attrs, int defStyle) {
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
		
        options16Bits = new BitmapFactory.Options();
		options16Bits.inPreferredConfig = Bitmap.Config.RGB_565;
		options16Bits.inPurgeable = true;  
		options16Bits.inInputShareable = true; 
        use16Bits = JkanjiGallerySettingActivity.getUse16Bits(getContext());
        
        this.useViewContentProvider = JkanjiGallerySettingActivity.getViewContentProvider(this.getContext());
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
        if (D) {
        	Log.e(TAG, "onDraw");
        }
        final int viewWidth = getWidth();
        final int viewHeight = getHeight();
        
        if (mBitmapSizes != null && viewWidth > 0 && viewHeight > 0) {
            final float panX = mPanX;
            final float panY = mPanY;
            final float zoomX = getZoomX(mAspectQuotient) * viewWidth / mBitmapTotalWidth;
            final float zoomY = getZoomY(mAspectQuotient) * viewHeight / mBitmapTotalHeight;

            mViewRect.left = 0;
            mViewRect.top = 0;
            mViewRect.right = viewWidth;
            mViewRect.bottom = viewHeight;
            
            mDstRectAll.left = (viewWidth * 0.5f - mBitmapTotalWidth * 0.5f * zoomX + (0.5f - panX) * mBitmapTotalWidth * zoomX);
            mDstRectAll.top = (viewHeight * 0.5f - mBitmapTotalHeight * 0.5f * zoomY + (0.5f - panY) * mBitmapTotalHeight * zoomY);
            mDstRectAll.right = (viewWidth * 0.5f + mBitmapTotalWidth * 0.5f * zoomX + (0.5f - panX) * mBitmapTotalWidth * zoomX);
            mDstRectAll.bottom = (viewHeight * 0.5f + mBitmapTotalHeight * 0.5f * zoomY + (0.5f - panY) * mBitmapTotalHeight * zoomY);
            
            mLoadList.clear();
            boolean foundIntersect = false;
            String curFilename = "";
            for (int i = 0; i < mBitmapSizes.length; i++) {
            	if (mHeightRates[i] == null) {
            		continue;
            	}
	            mSrcRect.top = 0;
	            mSrcRect.left = 0;
	            mSrcRect.right = mBitmapSizes[i].x;
	            mSrcRect.bottom = mBitmapSizes[i].y;
	            mDstRect.left = mDstRectAll.left;
	            mDstRect.right = mDstRectAll.right;
	            mDstRect.top = mDstRectAll.top + mHeightRates[i].x * mDstRectAll.height();
	            mDstRect.bottom = mDstRectAll.top + mHeightRates[i].y * mDstRectAll.height();
	            
	            if (RectF.intersects(mDstRect, mViewRect)) {
	            	if (!foundIntersect) {
	            		mOnPrevNextListener.onPageUpdate(i);
	            		foundIntersect = true;
	            		if (mFileName != null && i >= 0 && i < mFileName.length) {
	            			curFilename = mFileName[i];
	            		}
	            	}
	            	//canvas.drawBitmap(mBitmap[i], mSrcRect, mDstRect, null);
	            	if (mLoadBitmaps[i] != null) {
//	            		Log.e(TAG, "mBitmap [" + i + "] :" + mBitmap[i].getWidth() + ", " + mBitmap[i].getHeight());
//	            		Log.e(TAG, "mLoadBitmaps [" + i + "] :" + mLoadBitmaps[i].getWidth() + ", " + mLoadBitmaps[i].getHeight());
	            		mSrcRect.left = 0;
	            		mSrcRect.top = 0;
	            		mSrcRect.right = mLoadBitmaps[i].getWidth();
	            		mSrcRect.bottom = mLoadBitmaps[i].getHeight();
	            		if (!mLoadBitmaps[i].isRecycled()) {
	            			canvas.drawBitmap(mLoadBitmaps[i], mSrcRect, mDstRect, mPaint);
	            		}
	            	} else {
	            		paintRect.setStyle(Paint.Style.FILL);
	            		paintRect.setColor(Color.GRAY);
	            		canvas.drawRect(mDstRect, paintRect);
	            		boolean found = false;
	            		for (Integer ind : mFailList) {
	            			if (ind == i) {
	            				found = true;
	            				break;
	            			}
	            		}
	            		if (found == false) {
	            			mLoadList.add(i);
	            		}
	            	}
	            } else {
	            	if (mLoadBitmaps[i] != null && !mLoadBitmaps[i].isRecycled()) {
	            		mLoadBitmaps[i].recycle();
	            		mLoadBitmaps[i] = null;
	            	}
	            }
	        }
            if (mLoadList.size() > 0) {
            	if (task == null) {
            		int index = mLoadList.get(0);
            		if (index >= 0 && index < mFileName.length) {
            			task = new LoadDataTask();
            			task.execute(mFileName[index], Integer.toString(index));
            		}
            	}
            	this.postInvalidateDelayed(INVALIDATE_DELAY);
            }
            if (isUseMask) {
            	canvas.drawRect(mViewRect, bgRectPaint);
            }
            if (isShowFileName && curFilename != null) {
            	canvas.drawText(curFilename, 0, -fileNamePaint.ascent() + fileNamePaint.descent(), fileNamePaint);
            }
        }
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mBitmapSizes != null) {
	        updateAspectQuotient(
	        		right - left, 
	        		bottom - top, 
	        		getBitmapTotalWidth(),
	        		getBitmapTotalHeight());
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
    
    public void zoomId(float f, float x, int fileid) {
    	setZoom(1.0f);
    	limitZoom();
		setPanX(0.5f);
		setPagePanY(fileid);
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
    	float rate = 1;
    	try {
	        if (mBitmapTotalWidth / mBitmapTotalHeight < (float)getWidth() / getHeight()) {
	        	rate = getWidth() / (mBitmapTotalWidth * getHeight() / mBitmapTotalHeight);
	        }
    	} catch (Throwable e) {
    		e.printStackTrace();
    	}
    	if (D) {
    		Log.d(TAG, "mBitmapTotalWidth == " + mBitmapTotalWidth + ", mBitmapTotalHeight == " + mBitmapTotalHeight + ", rate == " + rate);
    	}
        if (mZoom < MIN_ZOOM * rate) {
            setZoom(MIN_ZOOM * rate);
        } else if (mZoom > MAX_ZOOM * rate) {
            setZoom(MAX_ZOOM * rate);
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
    public void setImage(Point[] bitmapSizes, String[] filename, boolean useOptions, int pageMargin, float bitmapTotalWidth, float bitmapTotalHeight, PointF[] heightRates) {
        mBitmapSizes = bitmapSizes;
        mFileName = filename;
        mUseOptions = useOptions;
        mPageMargin = pageMargin;
        
        if (false) {
	        mBitmapTotalWidth = getBitmapTotalWidth();
	        mBitmapTotalHeight = getBitmapTotalHeight();
	        mHeightRates = getBitmapHeightRates();
	    } else {
	        mBitmapTotalWidth = bitmapTotalWidth;
	        mBitmapTotalHeight = bitmapTotalHeight;
	        mHeightRates = heightRates;
	    }
        mLoadBitmaps = new Bitmap[filename.length];
        
        if (mBitmapSizes != null) {
	        updateAspectQuotient(
	        		getWidth(), 
	        		getHeight(), 
	        		mBitmapTotalWidth, 
	        		mBitmapTotalHeight);
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
    
    
    private float getBitmapTotalWidth() {
    	float totalWidth = 1;
    	if (mBitmapSizes != null) {
	    	for (int i = 0; i < mBitmapSizes.length; i++) {
	    		if (mBitmapSizes[i] != null && mBitmapSizes[i].x > totalWidth) {
	    			totalWidth = mBitmapSizes[i].x;
	    		}
	    	}
	    }
    	return totalWidth;
    }
    
    private float getBitmapTotalHeight() {
    	float totalWidth = getBitmapTotalWidth();
    	float totalHeight = 1;
    	if (mBitmapSizes != null) {
    		totalHeight = 0;
    		for (int i = 0; i < mBitmapSizes.length; i++) {
    			if (mBitmapSizes[i] != null) {
    				totalHeight += totalWidth / mBitmapSizes[i].x * mBitmapSizes[i].y + mPageMargin;
    			}
	    	}
	    }
    	return totalHeight;
    }
    
    private PointF[] getBitmapHeightRates() {
    	float totalWidth = getBitmapTotalWidth();
    	float totalHeight = getBitmapTotalHeight();
    	PointF[] heightRates = null;
    	if (mBitmapSizes != null && totalHeight != 0) {
    		heightRates = new PointF[mBitmapSizes.length];
    		float height = 0;
    		float lastPos = 0;
    		for (int i = 0; i < mBitmapSizes.length; i++) {
    			if (mBitmapSizes[i] != null) {
    				height += totalWidth / mBitmapSizes[i].x * mBitmapSizes[i].y;
    				heightRates[i] = new PointF();
    				heightRates[i].x = lastPos;
    				heightRates[i].y = height / totalHeight;
    				height += mPageMargin;
    				lastPos = height / totalHeight;
    			}
	    	}
	    }
    	return heightRates;
    }
    
    public void setPagePanY(int page) {
    	if (page >= 0 && mHeightRates != null && page < mHeightRates.length && mHeightRates[page] != null) {
    		/**
    		 * pany - vh / 2 / (zoomy * totalH) = rate[i];
    		 */
    		setPanY(mHeightRates[page].x + 0.5f / getZoomY(mAspectQuotient));
        	limitPan();
        	invalidate();
    	}
    }
    
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mLoadBitmaps != null) {
			for (int i = 0; i < mLoadBitmaps.length; i++) {
				if (mLoadBitmaps[i] != null && !mLoadBitmaps[i].isRecycled()) {
					mLoadBitmaps[i].recycle();
					mLoadBitmaps[i] = null;
				}
			}
			mLoadBitmaps = null;
		}
	}



	private class LoadDataTask extends AsyncTask<String, Void, Boolean> {
		private boolean loadResult = false;
		private Bitmap bitmap;
		
		private volatile boolean isCancel = false;
		private String filename;
		private int index = 0;
		private int viewWidth, viewHeight;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			double sampleSizeValue = JkanjiGallerySettingActivity.getSampleSizeValue(getContext());
			if (sampleSizeValue < 0) {
				sampleSizeValue = 1;
			}
			if (getWidth() == 0 || 
				getHeight() == 0) {
				if (getResources() != null) {
					DisplayMetrics dm = getResources().getDisplayMetrics();
					if (dm != null) {
						viewWidth = (int)(dm.widthPixels * sampleSizeValue);
						viewHeight = (int)(dm.heightPixels * sampleSizeValue);
					}
				}
			} else {
				viewWidth = (int)(getWidth() * sampleSizeValue);
				viewHeight = (int)(getHeight() * sampleSizeValue);
			}
		}

		public void setCancel(boolean _isCancel) {
			this.isCancel = _isCancel;
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				filename = params[0];
				try {
					index = Integer.parseInt(params[1]);
				} catch (Throwable e) {
					e.printStackTrace();
					index = 0;
				}
				boolean isSuccess = true;
    			String fstr = new File(filename).getAbsolutePath();
    			if (!mUseOptions) {
    				if (use16Bits) {
    					if (useViewContentProvider) {
    						if (mOnPrevNextListener != null) {
    							bitmap = mOnPrevNextListener.actLoad(fstr, 1, 1, true, false);
    						}
    					} else {
    						bitmap = BitmapFactory.decodeFile(fstr, options16Bits);
    					}
    				} else {
    					if (useViewContentProvider) {
    						if (mOnPrevNextListener != null) {
    							bitmap = mOnPrevNextListener.actLoad(fstr, 1, 1, false, false);
    						}
    					} else {
    						bitmap = BitmapFactory.decodeFile(fstr);
    					}
    				}
    			} else {
					if (useViewContentProvider) {
						if (mOnPrevNextListener != null) {
							bitmap = mOnPrevNextListener.actLoad(fstr, viewWidth, viewHeight, false, true);
						}
					} else {
						bitmap = decodeSampledBitmapFromFile(fstr, viewWidth, viewHeight);
					}
    			}
    			if (bitmap == null) {
    				isSuccess = false;
    			}
				if (isSuccess) {
					loadResult = true;
				} else {
					loadResult = false;
				}
			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (this.isCancel == false) {
				if (result == true) {
					if (loadResult) {
		        		if (mLoadBitmaps != null && index >= 0 && index < mLoadBitmaps.length) {
		        			mLoadBitmaps[index] = bitmap;
		        		}
		        		if (D) {
		        			Log.d(TAG, "load success " + index  + ":" + filename);
		        		}
//						Toast.makeText(getContext(),
//								"加载成功  " + index  + ":" + filename, Toast.LENGTH_SHORT).show();
					} else {
						if (D) {
							Log.d(TAG, "load fail " + index  + ":" + filename);
						}
						Toast.makeText(getContext(),
							"加载失败:" + filename, Toast.LENGTH_SHORT).show();
						boolean found = false;
						for (Integer i : mFailList) {
							if (i == index) {
								found = true;
								break;
							}
						}
						if (found == false) {
							mFailList.add(index);
						}
					}
				} else if (result == false) {
					if (D) {
						Log.d(TAG, "load fail " + index  + ":" + filename);
					}
					Toast.makeText(getContext(),
							"加载失败:" + filename, Toast.LENGTH_SHORT).show();
					boolean found = false;
					for (Integer i : mFailList) {
						if (i == index) {
							found = true;
							break;
						}
					}
					if (found == false) {
						mFailList.add(index);
					}
				}
				invalidate();
			}
			task = null;
			bitmap = null;
		}
    }
	
	
    private static synchronized Bitmap decodeSampledBitmapFromFile(String filename, int reqWidth, int reqHeight) {
    	final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }
    
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
            final float totalPixels = width * height;
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;
            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }
}
