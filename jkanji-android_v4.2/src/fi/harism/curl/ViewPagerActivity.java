package fi.harism.curl;

import java.util.ArrayList;

import com.elgubbo.sharetoclipboard.ShareToClipboardActivity;
import com.iteye.weimingtom.jkanji.BookDrawTextUtil;
import com.iteye.weimingtom.jkanji.JKanjiActivity;
import com.iteye.weimingtom.jkanji.JkanjiSenService;
import com.iteye.weimingtom.jkanji.JkanjiSettingActivity;
import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.jkanji.SQLiteReaderActivity;
import com.iteye.weimingtom.jkanji.BookDrawTextUtil.ExtraRubyInfo;
import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ViewPagerActivity 
	//see BitmapFragmentAdapter && FIXME in BitmapFragment -> onSizeChanged
	//extends FragmentActivity {
	extends Activity {
	
	private final static boolean D = false;
	private final static String TAG = "ViewPagerActivity";
	
	private volatile BookLoader bookLoader = new BookLoader();
	
    private ActionBar actionBar;
    
    //FIXME:
    //private BitmapFragmentAdapter mAdapter;
    private BitmapViewPagerAdapter mAdapter;
    
    public BitmapViewPager mPager;
	private LinearLayout progressLinearLayout;
	private TextView textViewLoading;
	private GestureDetector detector;
	
	private int aozoraRbSize, aozoraRtSize, aozoraSpaceSize;
	
	private UpdatePageTask updatePageTask = null;
	
	private OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
		@Override
		public void onPageScrollStateChanged(int state) {
			
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			
		}

		@Override
		public void onPageSelected(int position) {
			if (D) {
				Log.e(TAG, "onPageSelected");
			}
			
			//FIXME:
			//因为ViewPager预先创建当前页面附近的页面，所以需新重新调整为正确的页索引。
			bookLoader.setCurPage(mPager.getCurrentItem());
			
			
			//see BitmapViewPagerAdapter
			BitmapFragmentView image = (BitmapFragmentView)mPager.findViewWithTag(Integer.toString(position));
			if (image != null) {
				updatePagerAsync(image, mPager.getWidth(), mPager.getHeight(), position);
			}
		}
    };
	
	public BitmapViewPager.SizeChangedObserver fragmentSizeChangedObserver = new BitmapViewPager.SizeChangedObserver() {
		@Override
		public void onSizeChanged(int w, int h) {
			if (D) {
				Log.d(TAG, "ViewPagerTestActivity onSizeChanged " + w + "," + h);
			}
			mPager.setVisibility(BitmapViewPager.GONE);
			progressLinearLayout.setVisibility(LinearLayout.VISIBLE);
			bookLoader.isProgress = true;
			textViewLoading.setText("(2/2)计算文字排版位置...");
			if (bookLoader.reverseDirection) {
				new InitDataTask().execute(0, h, w);
			} else {
				new InitDataTask().execute(0, w, h);
			}
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bookLoader.onCreate(this);
        setContentView(R.layout.view_pager);
        
		progressLinearLayout = (LinearLayout) findViewById(R.id.progressLinearLayout);
		textViewLoading = (TextView) this.findViewById(R.id.textViewLoading);
		mPager = (BitmapViewPager)findViewById(R.id.pager);
		
		aozoraRbSize = JkanjiSettingActivity.getRbSize(this);
		aozoraRtSize = JkanjiSettingActivity.getRtSize(this);
		aozoraSpaceSize = JkanjiSettingActivity.getSpaceSize(this);
		
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("阅读器");
        actionBar.setHomeAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.book;
			}

			@Override
			public void performAction(View view) {
				//FIXME:
				//因为ViewPager预先创建当前页面附近的页面，所以需新重新调整为正确的页索引。
				bookLoader.setCurPage(mPager.getCurrentItem());
				BookInfoUtils.stopForgroundService(ViewPagerActivity.this);
				finish();
			}
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.del;
			}

			@Override
			public void performAction(View view) {
				if (D) {
					Log.d(TAG, "previous page");
				}
				//previous page
				if (bookLoader != null && mPager != null &&
					bookLoader.curPage > 0) {
					bookLoader.setCurPage(bookLoader.curPage - 1);
					mPager.setPage(bookLoader.curPage);
				}	
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				return R.drawable.forward;
			}

			@Override
			public void performAction(View view) {
				if (D) {
					Log.d(TAG, "next page");
				}
				//next page
				if (bookLoader != null && mPager != null &&
					bookLoader.curPage < bookLoader.totalPage - 1) {
					bookLoader.setCurPage(bookLoader.curPage + 1);
					mPager.setPage(bookLoader.curPage);
				}
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				return R.drawable.config;
			}

			@Override
			public void performAction(View view) {
				openOptionsMenu();
			}
        });
        actionBar.addAction(new ActionBar.Action() {
			@Override
			public int getDrawable() {
				//return R.drawable.icon_actionbar;
				//return R.drawable.memo;
				return R.drawable.search_sqlite;
			}

			@Override
			public void performAction(View view) {
//				Intent intent = new Intent(ViewPagerActivity.this, ShareToClipboardActivity.class);
//				startActivity(intent);
				Intent intent = new Intent(ViewPagerActivity.this, SQLiteReaderActivity.class);
				startActivity(intent);
			}
        });
        if (bookLoader.hasActionbar) {
        	actionBar.setVisibility(ActionBar.VISIBLE);
        } else {
        	actionBar.setVisibility(ActionBar.GONE);
        }
        
        //FIXME:
        //mAdapter = new BitmapFragmentAdapter(getSupportFragmentManager());
        mAdapter = new BitmapViewPagerAdapter(this);
        mPager.setAdapter(mAdapter);
        mPager.setSizeChangedObserver(fragmentSizeChangedObserver);
        
        mPager.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return detector.onTouchEvent(event);
			}
        });
//        mPager.setOnPageChangeListener(onPageChangeListener);
        
		detector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
			@Override
			public boolean onDown(MotionEvent e) {
				return false;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				return false;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				
			}
			
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				return false;
			}

			@Override
			public void onShowPress(MotionEvent e) {
				
			}

			@Override
			public boolean onSingleTapUp(MotionEvent event) {
				//FIXME:
				//因为ViewPager预先创建当前页面附近的页面，所以需新重新调整为正确的页索引。
				bookLoader.setCurPage(mPager.getCurrentItem());
				float x;
				float y;
				int h;
				int w;
				if (bookLoader.reverseDirection) {
					h = mPager.getWidth();
					w = mPager.getHeight();	
					x = event.getY();
					y = h - event.getX();
				} else {
					h = mPager.getHeight();
					w = mPager.getWidth();
					x = event.getX();
					y = event.getY();
				}
				if (true) { //if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (mPager.getVisibility() == BitmapViewPager.VISIBLE) {
						float margin = BookInfoUtils.getMargin(ViewPagerActivity.this);
						if (D) {
							Log.d(TAG, "x == " + x + ", y == " + y + ", margin == " + margin + ", ty == " + (mPager.getTop() + margin));
						}
						if (bookLoader.isFastTap ||
							(y < margin) || 
							(y > h - margin)) {
							if (x < w * 0.5f) {
								if (!bookLoader.isVertical) {
									if (D) {
										Log.d(TAG, "previous page");
									}
									//previous page
									if (bookLoader.curPage > 0) {
										bookLoader.setCurPage(bookLoader.curPage - 1);
										mPager.setPage(bookLoader.curPage);
									}
								} else {
									if (D) {
										Log.d(TAG, "next page");
									}
									//next page
									if (bookLoader.curPage < bookLoader.totalPage - 1) {
										bookLoader.setCurPage(bookLoader.curPage + 1);
										mPager.setPage(bookLoader.curPage);
									}						
								}
							} else {
								if (!bookLoader.isVertical) {
									if (D) {
										Log.d(TAG, "next page");
									}
									//next page
									if (bookLoader.curPage < bookLoader.totalPage - 1) {
										bookLoader.setCurPage(bookLoader.curPage + 1);
										mPager.setPage(bookLoader.curPage);
									}
								} else {
									if (D) {
										Log.d(TAG, "previous page");
									}
									//previous page
									if (bookLoader.curPage > 0) {
										bookLoader.setCurPage(bookLoader.curPage - 1);
										mPager.setPage(bookLoader.curPage);
									}
								}
							}
						} else {
							if (x < margin) {
								if (!bookLoader.isVertical) {
									if (D) {
										Log.d(TAG, "previous page");
									}
									//previous page
									if (bookLoader.curPage > 0) {
										bookLoader.setCurPage(bookLoader.curPage - 1);
										mPager.setPage(bookLoader.curPage);
									}
								} else {
									if (D) {
										Log.d(TAG, "next page");
									}
									//next page
									if (bookLoader.curPage < bookLoader.totalPage - 1) {
										bookLoader.setCurPage(bookLoader.curPage + 1);
										mPager.setPage(bookLoader.curPage);
									}									
								}
							} else if (x > w - margin){
								if (!bookLoader.isVertical) {
									if (D) {
										Log.d(TAG, "next page");
									}
									//next page
									if (bookLoader.curPage < bookLoader.totalPage - 1) {
										bookLoader.setCurPage(bookLoader.curPage + 1);
										mPager.setPage(bookLoader.curPage);
									}
								} else {
									if (D) {
										Log.d(TAG, "previous page");
									}
									//previous page
									if (bookLoader.curPage > 0) {
										bookLoader.setCurPage(bookLoader.curPage - 1);
										mPager.setPage(bookLoader.curPage);
									}
								}
							} else {
								bookLoader.onPositionLine(x, y);
							}
						}
					}
				}
				return true;
			}
		});
		detector.setIsLongpressEnabled(false); 
        
		mPager.setVisibility(View.GONE);
		progressLinearLayout.setVisibility(LinearLayout.VISIBLE);
		bookLoader.isProgress = true;
		this.textViewLoading.setText("(1/2)加载文件与Sen数据...");
		if (bookLoader.textUtil == null) {
			new LoadDataTask().execute();
		}
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		bookLoader.onDestroy();
		if (updatePageTask != null) {
			updatePageTask.setCancel();
			updatePageTask = null;
		}
	}

	private class LoadDataTask extends AsyncTask<Void, Void, Boolean> {
		private String errorString = "";
		
		@Override
		protected Boolean doInBackground(Void... params) {
			if (bookLoader.isEnableSen) {
				try {
					BookInfoUtils.showResults(ViewPagerActivity.this, "Test", 0);
					startSenForgroundService();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			try {
				bookLoader.loadBG();
				bookLoader.loadData();
			} catch (Throwable e) {
				errorString = e.toString();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true && !isFinishing()) {
				//FIXME:
				progressLinearLayout.setVisibility(LinearLayout.GONE);
				bookLoader.isProgress = false;
				mPager.setVisibility(BitmapViewPager.VISIBLE);
			} else if (result == false) {
				Toast.makeText(ViewPagerActivity.this, 
					errorString, 
					Toast.LENGTH_SHORT).show();
				BookInfoUtils.stopForgroundService(ViewPagerActivity.this);
				finish();
			}
		}
    }
    
	private class InitDataTask extends AsyncTask<Integer, Void, Boolean> {
		private String errorString = "";
		private int type = 0;
		
		@Override
		protected Boolean doInBackground(Integer... params) {
			try {
				type = params[0];
				int w = params[1];
				int h = params[2];
				float scale = getResources().getDisplayMetrics().scaledDensity;
				int textColor = Color.BLACK;
				if (bookLoader.blackBack) {
					textColor = Color.WHITE;
				}
				bookLoader.textUtil.initText(bookLoader.textReader, 
						0 + BookInfoUtils.MARGIN * scale, 0 + BookInfoUtils.MARGIN * scale, w - BookInfoUtils.MARGIN * scale * 2, h - BookInfoUtils.MARGIN * scale * 2, 
						aozoraRbSize * scale, textColor, 
						false, Color.WHITE,
						bookLoader.typeface, bookLoader.rubyInfoList, 
						aozoraRtSize * scale, textColor,
						aozoraSpaceSize * scale, bookLoader.isVertical);
				bookLoader.totalPage = bookLoader.textUtil.getTotalPage();
			} catch (Throwable e) {
				e.printStackTrace();
				errorString = e.toString();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true && !isFinishing()) {
//				mPager.setAdapter(mAdapter);
				if (D) {
					Log.e(TAG, "onPostExecute");
				}
				if (bookLoader.basePage) {
					//FIXME:
					//if (saveContextData.lastNonConfigurationPage == 0) { //because of first open, so curPage == 0.
					if (true) {
						if (D) {
							Log.d(TAG, "setCurrentIndex page - 1:" + (bookLoader.page - 1));
						}
						//FIXME:
						if (bookLoader.page >= 1) {
							bookLoader.setCurPage(bookLoader.page - 1);
						} else {
							bookLoader.setCurPage(0);
						}
					} else {
						/*
						bookLoader.setCurPage(saveContextData.lastNonConfigurationPage);
						if (D) {
							Log.d(TAG, "setCurrentIndex curPage:" + bookLoader.curPage);
						}
						*/
					}
				} else {
//					if (D) {
//						Log.d(TAG, "data.lastNonConfigurationPosition = " + saveContextData.lastNonConfigurationPosition);
//					}
					//FIXME:
					//if (saveContextData.lastNonConfigurationPosition == -1) {
					if (true) {
						if (D) {
							Log.e(TAG, "InitDataTask bookLoader.setCurPage = " + bookLoader.position);
						}
						//FIXME:
						bookLoader.setCurPage(bookLoader.positionToPage(bookLoader.position));
					} else {
						//bookLoader.setCurPage(bookLoader.positionToPage(saveContextData.lastNonConfigurationPosition));
					}
				}

				int totalPage = bookLoader.textUtil.getTotalPage();
				if (totalPage > 0) {
					mAdapter.setPageCount(totalPage);
				} else {
					mAdapter.setPageCount(1);
				}
				mAdapter.notifyDataSetChanged();
				if (bookLoader.curPage < 0) {
					bookLoader.curPage = 0;
				} else if (bookLoader.curPage >= totalPage) {
					bookLoader.curPage = totalPage - 1;
				}
				if (D) {
					Log.d(TAG, "InitDataTask setCurPage : " + bookLoader.curPage);
				}
				BitmapFragmentView image = (BitmapFragmentView)mPager.findViewWithTag(Integer.toString(bookLoader.curPage));
				if (image == null) {
					mAdapter.tryLoad(bookLoader.curPage);
				} else {
					updatePagerAsync(image, mPager.getWidth(), mPager.getHeight(), bookLoader.curPage);
		        }
				mPager.setPage(bookLoader.curPage);
				//FIXME:
				mPager.setOnPageChangeListener(onPageChangeListener);
				
				progressLinearLayout.setVisibility(LinearLayout.GONE);
				bookLoader.isProgress = false;
				mPager.setVisibility(View.VISIBLE);
				//mPager.reque
				if (D) {
					Log.e("InitDataTask", "show mPager");
				}
				BookInfoUtils.startForgroundService(ViewPagerActivity.this);
			} else if (result == false) {
				Toast.makeText(ViewPagerActivity.this, 
						errorString, 
						Toast.LENGTH_SHORT).show();
				BookInfoUtils.stopForgroundService(ViewPagerActivity.this);
				finish();
			}
		}
    }
	
	@Override
	public void onResume() {
		super.onResume();
		bookLoader.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return bookLoader.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//FIXME:
		//因为ViewPager预先创建当前页面附近的页面，所以需新重新调整为正确的页索引。
		bookLoader.setCurPage(mPager.getCurrentItem());
		bookLoader.onOptionsItemSelected(item);
		return super.onOptionsItemSelected(item);
	}

	@Override
    protected Dialog onCreateDialog(int id) {
    	return bookLoader.onCreateDialog(id);
    }   
	
	@Override
	public void onBackPressed() {
		bookLoader.onBackPressed();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode) {
		case KeyEvent.KEYCODE_SEARCH:
			if (!bookLoader.isProgress) {
				this.startActivity(new Intent(this, JKanjiActivity.class));
			}
			return true;
			
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (bookLoader.isProgress) {
				return super.onKeyDown(keyCode, event);
			}
			if (bookLoader.useVolumeKey && bookLoader.curPage > 0) {
				bookLoader.setCurPage(bookLoader.curPage - 1);
				mPager.setPage(bookLoader.curPage);
			} else {
				return super.onKeyDown(keyCode, event);
			}
			return true;
			
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (bookLoader.isProgress) {
				return super.onKeyDown(keyCode, event);
			}
			if (bookLoader.useVolumeKey && bookLoader.curPage < bookLoader.totalPage - 1) {
				bookLoader.setCurPage(bookLoader.curPage + 1);
				mPager.setPage(bookLoader.curPage);
			} else {
				return super.onKeyDown(keyCode, event);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private Bitmap loadBitmap(int width, int height, int index) {
		if (width == 0) {
			width = 10;
		}
		if (height == 0) {
			height = 10;
		}
		Bitmap b = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		if (bookLoader.blackBack) {
			b.eraseColor(0xFF000000);
		} else {
			b.eraseColor(0xFFFFFFFF);
		}
		Canvas c = new Canvas(b);
		bookLoader.drawBG(c, width, height);
		bookLoader.textUtil.setPage(index);
		ArrayList<ExtraRubyInfo> extraRubyInfoList = null;
		if (bookLoader.isEnableSen) {
			BookDrawTextUtil.DrawTextInfo drawtextinfo = bookLoader.textUtil.getDrawText();
			if (drawtextinfo != null) {
				if (BookInfoUtils.USE_CONTENT_PROVIDER) {
					extraRubyInfoList = BookInfoUtils.showResults(this, 
							drawtextinfo.str, drawtextinfo.startPos);
				} else {
					extraRubyInfoList = BookInfoUtils.analyze(this, drawtextinfo);
				}
			}
		}
		bookLoader.textUtil.drawText(c, extraRubyInfoList);
		
		Paint p = new Paint();
		p.setAntiAlias(true);
		if (bookLoader.blackBack) {
			p.setColor(Color.WHITE);
		} else {
			p.setColor(Color.BLACK /*Color.BLUE*/);
		}
		float scale = getResources().getDisplayMetrics().scaledDensity;
		p.setTextSize(12 * scale);
		p.setTextAlign(Paint.Align.RIGHT);
		c.drawText("" + (index + 1) + "/" + bookLoader.textUtil.getTotalPage(), width - BookInfoUtils.MARGIN * scale / 2, height - BookInfoUtils.MARGIN * scale / 2 - p.descent(), p);
		
		return b;
	}
	
	public void updatePager(BitmapFragmentView view, int width, int height, int index) {
		if (width > 0 && height > 0) {
			if (D) {
				Log.e("updatePager", "updatePager " + width + "," + height);
			}
			bookLoader.setCurPage(index);
			Bitmap front;
			if (bookLoader.reverseDirection) {
				Bitmap temp = loadBitmap(height, width, index);
				Matrix matrix = new Matrix();
				matrix.reset();
				matrix.setRotate(90);
				front = Bitmap.createBitmap(temp, 
						0, 0, temp.getWidth(), temp.getHeight(), 
						matrix, true);
				temp.recycle();
			} else {
				front = loadBitmap(width, height, index);
			}
			if (D) {
				Log.d(TAG, "setPageBitmap");
			}
			if (D) {
				Log.e("updatePager", "updatePager " + index);
			}
			view.setPageBitmap(front);
		} else {
			view.setPageBitmap(null);
		}
		//FIXME:
		//因为ViewPager预先创建当前页面附近的页面，所以需新重新调整为正确的页索引。
		bookLoader.setCurPage(mPager.getCurrentItem());
	}
	
	public void updatePagerAsync(BitmapFragmentView view, int width, int height, int index) {
		if (updatePageTask != null) {
			updatePageTask.setCancel();
			updatePageTask = null;
		}
		updatePageTask = new UpdatePageTask(view, width, height, index);
		updatePageTask.execute();
	}
	
	public void startSenForgroundService() {
		if (JkanjiSettingActivity.getSenService(this)) {
			startService(
					new Intent(this, JkanjiSenService.class)
						.setAction(JkanjiSenService.ACTION_FOREGROUND));
		}
	}
	
	private class UpdatePageTask extends AsyncTask<Void, Void, Bitmap> {
		private BitmapFragmentView mView;
		private int mWidth, mHeight, mIndex;
		private boolean isCancel = false;
		
		public void setCancel() {
			isCancel = true;
		}
		
		public UpdatePageTask(BitmapFragmentView view, int width, int height, int index) {
			mView = view;
			mWidth = width;
			mHeight = height;
			mIndex = index;
		}
		
		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap retsult = null;
			if (mWidth > 0 && mHeight > 0) {
				if (D) {
					Log.e("updatePager", "updatePagerAsync " + mWidth + "," + mHeight);
				}
				bookLoader.setCurPage(mIndex);
				Bitmap front;
				if (bookLoader.reverseDirection) {
					Bitmap temp = loadBitmap(mHeight, mWidth, mIndex);
					Matrix matrix = new Matrix();
					matrix.reset();
					matrix.setRotate(90);
					front = Bitmap.createBitmap(temp, 
							0, 0, temp.getWidth(), temp.getHeight(), 
							matrix, true);
					temp.recycle();
				} else {
					front = loadBitmap(mWidth, mHeight, mIndex);
				}
				if (D) {
					Log.d(TAG, "setPageBitmap");
				}
				if (D) {
					Log.e("updatePager", "updatePager " + mIndex);
				}
				retsult = front;
			} else {
				retsult = null;
			}
			return retsult;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if (mView != null && !isCancel) {
				if (D) {
					Log.e(TAG, "UpdatePageTask success : w=" + mWidth + ",h=" + mHeight + ",i=" + mIndex + ",result=" + (result != null));
				}
				mView.setPageBitmap(result);
				//FIXME:
				//因为ViewPager预先创建当前页面附近的页面，所以需新重新调整为正确的页索引。
				bookLoader.setCurPage(mIndex);
			} else {
				if (D) {
					Log.e(TAG, "UpdatePageTask failed");
				}
				if (result != null) {
					result.recycle();
				}
			}
			updatePageTask = null;
		}
	}
}
