/*
   Copyright 2012 Harri Smatt

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package fi.harism.curl;

import java.util.ArrayList;

import com.elgubbo.sharetoclipboard.ShareToClipboardActivity;
import com.iteye.weimingtom.jkanji.BookDrawTextUtil;
import com.iteye.weimingtom.jkanji.JKanjiActivity;
import com.iteye.weimingtom.jkanji.JkanjiSenService;
import com.iteye.weimingtom.jkanji.JkanjiSettingActivity;
import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.jkanji.BookDrawTextUtil.ExtraRubyInfo;
import com.iteye.weimingtom.jkanji.SQLiteReaderActivity;
import com.iteye.weimingtom.jkanji.SimplePageView;
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

/**
 * Simple Activity for curl testing.
 * 
 * @author harism
 */
public class CurlActivity extends Activity {
	private final static boolean D = false;
	private final static String TAG = "CurlActivity";
	
	private volatile BookLoader bookLoader = new BookLoader();
	
    private ActionBar actionBar;
    private SimplePageView simplePage;
    private CurlView mCurlView;
	private LinearLayout progressLinearLayout;
	private TextView textViewLoading;
	private GestureDetector detector;
	
	private int aozoraRbSize, aozoraRtSize, aozoraSpaceSize;
	
	private final static class SaveContextData {
		int lastNonConfigurationPage;
		int lastNonConfigurationPosition;
		long lastNonConfigurationHistoryId;
		String lastNonConfigurationDesc;
		
		public SaveContextData() {
			lastNonConfigurationPage = -1;
			lastNonConfigurationPosition = -1;
			lastNonConfigurationHistoryId = -2L;
			lastNonConfigurationDesc = null;
		}
	}
	private SaveContextData saveContextData;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bookLoader.onCreate(this);
		setContentView(R.layout.page_curl);

		if (getLastNonConfigurationInstance() != null) {
			saveContextData = (SaveContextData) getLastNonConfigurationInstance();
		}
		if (saveContextData == null) {
			saveContextData = new SaveContextData();
		}
		
		if (D) {
			Log.d(TAG, "getLastNonConfigurationInstance == " + saveContextData.lastNonConfigurationPage);		
		}
		simplePage = (SimplePageView) findViewById(R.id.simplePage);
		mCurlView = (CurlView) findViewById(R.id.curl);
		progressLinearLayout = (LinearLayout) findViewById(R.id.progressLinearLayout);
		textViewLoading = (TextView) this.findViewById(R.id.textViewLoading);
		
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
				BookInfoUtils.stopForgroundService(CurlActivity.this);
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
				if (bookLoader != null && simplePage != null &&
					bookLoader.curPage > 0) {
					bookLoader.setCurPage(bookLoader.curPage - 1);
					simplePage.setPage(bookLoader.curPage);
					mCurlView.setCurrentIndex(bookLoader.curPage);
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
				if (bookLoader != null && simplePage != null &&
					bookLoader.curPage < bookLoader.totalPage - 1) {
					bookLoader.setCurPage(bookLoader.curPage + 1);
					simplePage.setPage(bookLoader.curPage);
					mCurlView.setCurrentIndex(bookLoader.curPage);
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
//				Intent intent = new Intent(CurlActivity.this, ShareToClipboardActivity.class);
//				startActivity(intent);
				Intent intent = new Intent(CurlActivity.this, SQLiteReaderActivity.class);
				startActivity(intent);
			}
        });
		
        if (bookLoader.hasActionbar) {
        	actionBar.setVisibility(ActionBar.VISIBLE);
        } else {
        	actionBar.setVisibility(ActionBar.GONE);
        }
		
		mCurlView.setPageProvider(new PageProvider());
		mCurlView.setSizeChangedObserver(new SizeChangedObserver());
		//mCurlView.setCurrentIndex(index);
		//this.curPage = index;
		
		//mCurlView.setBackgroundColor(0xFF202830);
		mCurlView.setBackgroundColor(Color.WHITE);
		mCurlView.setRenderLeftPage(false);
		mCurlView.setAllowLastPageCurl(false);
		// This is something somewhat experimental. Before uncommenting next
		// line, please see method comments in CurlView.
		//mCurlView.setEnableTouchPressure(true);

		detector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
			@Override
			public boolean onDown(MotionEvent e) {
				return false;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		        final int FLING_MIN_DISTANCE = 100, FLING_MIN_VELOCITY = 200;  
		        if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {  
		            // Fling left  
		        	if (!bookLoader.isVertical) {
			        	if (D) {
							Log.d(TAG, "next page");
						}
						//next page
						if (bookLoader.curPage < bookLoader.totalPage - 1) {
							bookLoader.setCurPage(bookLoader.curPage + 1);
							simplePage.setPage(bookLoader.curPage);
						}
		        	} else {
						if (D) {
							Log.d(TAG, "previous page");
						}
						//previous page
						if (bookLoader.curPage > 0) {
							bookLoader.setCurPage(bookLoader.curPage - 1);
							simplePage.setPage(bookLoader.curPage);
						}		        		
		        	}
		        } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {  
		            // Fling right
		        	if (!bookLoader.isVertical) {
			        	if (D) {
							Log.d(TAG, "previous page");
						}
						//previous page
						if (bookLoader.curPage > 0) {
							bookLoader.setCurPage(bookLoader.curPage - 1);
							simplePage.setPage(bookLoader.curPage);
						}
		        	} else {
			        	if (D) {
							Log.d(TAG, "next page");
						}
						//next page
						if (bookLoader.curPage < bookLoader.totalPage - 1) {
							bookLoader.setCurPage(bookLoader.curPage + 1);
							simplePage.setPage(bookLoader.curPage);
						}
		        	}
		        } else if(e2.getY()-e1.getY()>FLING_MIN_DISTANCE && Math.abs(velocityY)>FLING_MIN_VELOCITY) {
		            // Fling down 
		        	if (D) {
						Log.d(TAG, "previous page");
					}
					//previous page
					if (bookLoader.curPage > 0) {
						bookLoader.setCurPage(bookLoader.curPage - 1);
						simplePage.setPage(bookLoader.curPage);
					}
		        } else if(e1.getY()-e2.getY()>FLING_MIN_DISTANCE && Math.abs(velocityY)>FLING_MIN_VELOCITY) {
		            // Fling up
					if (D) {
						Log.d(TAG, "next page");
					}
					//next page
					if (bookLoader.curPage < bookLoader.totalPage - 1) {
						bookLoader.setCurPage(bookLoader.curPage + 1);
						simplePage.setPage(bookLoader.curPage);
					}
		        }
				return false;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				if (bookLoader.isFastTap) {
					bookLoader.isFastTap = false;
					actionBar.setTitle("阅读器");
					Toast.makeText(CurlActivity.this, "允许文字行触碰", Toast.LENGTH_SHORT).show();
				} else {
					bookLoader.isFastTap = true;
					actionBar.setTitle("阅读器(屏蔽文字行点击)");
					Toast.makeText(CurlActivity.this, "屏蔽文字行触碰", Toast.LENGTH_SHORT).show();
				}
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
				float x;
				float y;
				int h;
				int w;
				if (bookLoader.reverseDirection) {
					h = simplePage.getWidth();
					w = simplePage.getHeight();	
					x = event.getY();
					y = h - event.getX();
				} else {
					h = simplePage.getHeight();
					w = simplePage.getWidth();
					x = event.getX();
					y = event.getY();
				}
				if (true) { //if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (simplePage.getVisibility() == SimplePageView.VISIBLE) {
						float margin = BookInfoUtils.getMargin(CurlActivity.this);
						if (D) {
							Log.d(TAG, "x == " + x + ", y == " + y + ", margin == " + margin + ", ty == " + (simplePage.getTop() + margin));
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
										simplePage.setPage(bookLoader.curPage);
									}
								} else {
									if (D) {
										Log.d(TAG, "next page");
									}
									//next page
									if (bookLoader.curPage < bookLoader.totalPage - 1) {
										bookLoader.setCurPage(bookLoader.curPage + 1);
										simplePage.setPage(bookLoader.curPage);
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
										simplePage.setPage(bookLoader.curPage);
									}
								} else {
									if (D) {
										Log.d(TAG, "previous page");
									}
									//previous page
									if (bookLoader.curPage > 0) {
										bookLoader.setCurPage(bookLoader.curPage - 1);
										simplePage.setPage(bookLoader.curPage);
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
										simplePage.setPage(bookLoader.curPage);
									}
								} else {
									if (D) {
										Log.d(TAG, "next page");
									}
									//next page
									if (bookLoader.curPage < bookLoader.totalPage - 1) {
										bookLoader.setCurPage(bookLoader.curPage + 1);
										simplePage.setPage(bookLoader.curPage);
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
										simplePage.setPage(bookLoader.curPage);
									}
								} else {
									if (D) {
										Log.d(TAG, "previous page");
									}
									//previous page
									if (bookLoader.curPage > 0) {
										bookLoader.setCurPage(bookLoader.curPage - 1);
										simplePage.setPage(bookLoader.curPage);
									}
								}
							} else {
								bookLoader.onPositionLine(x, y);
							}
						}
					}
				}
				return false;
			}
		});
		detector.setIsLongpressEnabled(true); 

		simplePage.setClickable(true);
		simplePage.setFocusable(true);
		simplePage.setLongClickable(true);
		simplePage.setSizeChangedObserver(new SimpleSizeChangedObserver());
		simplePage.setPageProvider(new SimplePageProvider());
		simplePage.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return detector.onTouchEvent(event);
			}
		});
		
		simplePage.setVisibility(SimplePageView.GONE);
		mCurlView.setVisibility(CurlView.GONE);
		progressLinearLayout.setVisibility(LinearLayout.VISIBLE);
		bookLoader.isProgress = true;
		this.textViewLoading.setText("(1/2)加载文件与Sen数据...");
		if (bookLoader.textUtil == null) {
			new LoadDataTask().execute();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (bookLoader.curlType != BookInfoUtils.CURL_TYPE_SIMPLE) {
			mCurlView.onPause();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (bookLoader.curlType != BookInfoUtils.CURL_TYPE_SIMPLE) {
			mCurlView.onResume();
		}
		bookLoader.onResume();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		bookLoader.setCurPage(bookLoader.curPage);
		SaveContextData data = new SaveContextData();
		data.lastNonConfigurationPage = bookLoader.curPage;
		data.lastNonConfigurationPosition = bookLoader.curPosition;
		data.lastNonConfigurationHistoryId = bookLoader.curHistoryId;
		data.lastNonConfigurationDesc = bookLoader.curDesc;
		if (D) {
			Log.d(TAG, "data.lastNonConfigurationPosition = " + bookLoader.curPosition);
		}
		return data;
	}

	/**
	 * Bitmap provider.
	 */
	private class PageProvider implements CurlView.PageProvider {
		
		@Override
		public int getPageCount() {
			if (bookLoader.textUtil != null) {
				return bookLoader.textUtil.getTotalPage();
			} else {
				return 0;
			}
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
						extraRubyInfoList = BookInfoUtils.showResults(CurlActivity.this, 
								drawtextinfo.str, drawtextinfo.startPos);
					} else {
						extraRubyInfoList = BookInfoUtils.analyze(CurlActivity.this, drawtextinfo);
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
			float scale = CurlActivity.this.getResources().getDisplayMetrics().scaledDensity;
			p.setTextSize(12 * scale);
			p.setTextAlign(Paint.Align.RIGHT);
			//（触屏翻页模式）
			c.drawText("" + (index + 1) + "/" + bookLoader.textUtil.getTotalPage(), width - BookInfoUtils.MARGIN * scale / 2, height - BookInfoUtils.MARGIN * scale / 2 - p.descent(), p);

			return b;
		}

		@Override
		public void updatePage(CurlPage page, int width, int height, int index) {
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
			page.setTexture(front, CurlPage.SIDE_FRONT);
			page.setColor(Color.WHITE, CurlPage.SIDE_BACK);
		}

		@Override
		public void onCurentIndexChanged(int index) {
			bookLoader.setCurPage(index);			
		}
	}

	private class SimplePageProvider implements SimplePageView.PageProvider {
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
						extraRubyInfoList = BookInfoUtils.showResults(CurlActivity.this, 
								drawtextinfo.str, drawtextinfo.startPos);
					} else {
						extraRubyInfoList = BookInfoUtils.analyze(CurlActivity.this, drawtextinfo);
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
			//（按钮翻页模式）
			c.drawText("" + (index + 1) + "/" + bookLoader.textUtil.getTotalPage(), width - BookInfoUtils.MARGIN * scale / 2, height - BookInfoUtils.MARGIN * scale / 2 - p.descent(), p);
			
			return b;
		}

		@Override
		public void updatePage(SimplePageView view, int width, int height, int index) {
			//FIXME:
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
			view.setPageBitmap(front);
		}
	}
	
	/**
	 * CurlView size changed observer.
	 */
	private class SizeChangedObserver implements CurlView.SizeChangedObserver {
		@Override
		public void onSizeChanged(int w, int h) {
			if (w > h) {
				//mCurlView.setViewMode(CurlView.SHOW_TWO_PAGES);
				mCurlView.setViewMode(CurlView.SHOW_ONE_PAGE);
				//mCurlView.setMargins(.1f, .05f, .1f, .05f);
				mCurlView.setMargins(0, 0, 0, 0);
			} else {
				mCurlView.setViewMode(CurlView.SHOW_ONE_PAGE);
				//mCurlView.setMargins(.1f, .1f, .1f, .1f);
				mCurlView.setMargins(0, 0, 0, 0);
			}


			simplePage.setVisibility(SimplePageView.GONE);
			mCurlView.setVisibility(CurlView.GONE);
			progressLinearLayout.setVisibility(LinearLayout.VISIBLE);
			bookLoader.isProgress = true;
			textViewLoading.setText("(2/2)计算文字排版位置...");
			if (bookLoader.reverseDirection) {
				new InitDataTask().execute(0, h, w);
			} else {
				new InitDataTask().execute(0, w, h);
			}
		}
	}

	private class SimpleSizeChangedObserver implements SimplePageView.SizeChangedObserver {
		@Override
		public void onSizeChanged(int w, int h) {
			simplePage.setVisibility(SimplePageView.GONE);
			mCurlView.setVisibility(CurlView.GONE);
			progressLinearLayout.setVisibility(LinearLayout.VISIBLE);
			bookLoader.isProgress = true;
			textViewLoading.setText("(2/2)计算文字排版位置...");
			if (bookLoader.reverseDirection) {
				new InitDataTask().execute(1, h, w);
			} else {
				new InitDataTask().execute(1, w, h);
			}
		}
	}
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	bookLoader.onDestroy();
    }

	private class LoadDataTask extends AsyncTask<Void, Void, Boolean> {
		private String errorString = "";
		
		@Override
		protected Boolean doInBackground(Void... params) {
			if (bookLoader.isEnableSen) {
				try {
					BookInfoUtils.showResults(CurlActivity.this, "Test", 0);
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
				progressLinearLayout.setVisibility(LinearLayout.GONE);
				bookLoader.isProgress = false;
				if (bookLoader.curlType == BookInfoUtils.CURL_TYPE_CURL || 
					bookLoader.curlType == BookInfoUtils.CURL_TYPE_CURLSIMPLE) {
					mCurlView.setVisibility(CurlView.VISIBLE);
					simplePage.setVisibility(SimplePageView.GONE);
				} else {
					mCurlView.setVisibility(CurlView.GONE);
					simplePage.setVisibility(SimplePageView.VISIBLE);					
				}
			} else if (result == false) {
				Toast.makeText(CurlActivity.this, 
					errorString, 
					Toast.LENGTH_SHORT).show();
				BookInfoUtils.stopForgroundService(CurlActivity.this);
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
				errorString = e.toString();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true && !isFinishing()) {
				if (bookLoader.basePage) {
					if (saveContextData.lastNonConfigurationPage < 0) { //because of first open, so curPage == -1.
						bookLoader.setCurPage(bookLoader.page - 1);
						if (D) {
							Log.d(TAG, "setCurrentIndex page - 1:" + (bookLoader.page - 1));
						}
					} else {
						bookLoader.setCurPage(saveContextData.lastNonConfigurationPage);
						if (D) {
							Log.d(TAG, "setCurrentIndex curPage:" + bookLoader.curPage);
						}
					}
				} else {
					if (D) {
						Log.d(TAG, "data.lastNonConfigurationPosition = " + saveContextData.lastNonConfigurationPosition);
					}
					if (saveContextData.lastNonConfigurationPosition == -1) {
						bookLoader.setCurPage(bookLoader.positionToPage(bookLoader.position));
					} else {
						bookLoader.setCurPage(bookLoader.positionToPage(saveContextData.lastNonConfigurationPosition));
					}
				}
				
				//FIXME:
				if (saveContextData.lastNonConfigurationHistoryId != -2L) {
					bookLoader.curHistoryId = saveContextData.lastNonConfigurationHistoryId;
				}
				if (saveContextData.lastNonConfigurationDesc != null) {
					bookLoader.curDesc = saveContextData.lastNonConfigurationDesc;
				}
				if (type == 0) {
					if (bookLoader.curlType == BookInfoUtils.CURL_TYPE_CURL || 
						bookLoader.curlType == BookInfoUtils.CURL_TYPE_CURLSIMPLE) {
						if (D) {
							Log.d(TAG, "InitDataTask setCurPage : " + bookLoader.curPage);
						}
						//FIXME:
						//setCurPage(curPage);
						mCurlView.setCurrentIndex(bookLoader.curPage);
						progressLinearLayout.setVisibility(LinearLayout.GONE);
						bookLoader.isProgress = false;
						mCurlView.setVisibility(CurlView.VISIBLE);
						simplePage.setVisibility(SimplePageView.GONE);
					}
				} else {
					if (bookLoader.curlType == BookInfoUtils.CURL_TYPE_SIMPLE || 
						bookLoader.curlType == BookInfoUtils.CURL_TYPE_CURLSIMPLE) {
						if(D) {
							Log.d(TAG, "simplePage.setPage: " + bookLoader.curPage);
						}
						//FIXME:
						//setCurPage(curPage);
						simplePage.setPage(bookLoader.curPage);
						progressLinearLayout.setVisibility(LinearLayout.GONE);
						bookLoader.isProgress = false;
						mCurlView.setVisibility(CurlView.GONE);
						simplePage.setVisibility(SimplePageView.VISIBLE);
					}
				}
				BookInfoUtils.startForgroundService(CurlActivity.this);
			} else if (result == false) {
				Toast.makeText(CurlActivity.this, 
						errorString, 
						Toast.LENGTH_SHORT).show();
				BookInfoUtils.stopForgroundService(CurlActivity.this);
				finish();
			}
		}
    }
	
	@Override
	public void onBackPressed() {
		if (bookLoader.curlType == BookInfoUtils.CURL_TYPE_CURLSIMPLE && simplePage.getVisibility() == SimplePageView.VISIBLE) {
			Toast.makeText(this, 
					"切换到触屏翻页模式", 
					Toast.LENGTH_SHORT).show();
			progressLinearLayout.setVisibility(LinearLayout.GONE);
			bookLoader.isProgress = false;
			mCurlView.setVisibility(CurlView.VISIBLE);
			simplePage.setVisibility(SimplePageView.GONE);
			bookLoader.setCurPage(bookLoader.curPage);
			mCurlView.setCurrentIndex(bookLoader.curPage);
		} else {
			bookLoader.onBackPressed();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode) {
		case KeyEvent.KEYCODE_SEARCH:
			if (bookLoader.isProgress) {
				this.startActivity(new Intent(this, JKanjiActivity.class));
			}
			return true;
			
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (bookLoader.isProgress) {
				return super.onKeyDown(keyCode, event);
			}
			if (bookLoader.curlType == BookInfoUtils.CURL_TYPE_CURLSIMPLE && mCurlView.getVisibility() == CurlView.VISIBLE) {
				Toast.makeText(this,
						"切换到按钮翻页模式", 
						Toast.LENGTH_SHORT).show();
				progressLinearLayout.setVisibility(LinearLayout.GONE);
				bookLoader.isProgress = false;
				mCurlView.setVisibility(CurlView.GONE);
				simplePage.setVisibility(SimplePageView.VISIBLE);
				simplePage.setPage(bookLoader.curPage);
			} else if (bookLoader.useVolumeKey && simplePage.getVisibility() == SimplePageView.VISIBLE) {
				if (bookLoader.curPage > 0) {
					bookLoader.setCurPage(bookLoader.curPage - 1);
					simplePage.setPage(bookLoader.curPage);
				}
			} else {
				return super.onKeyDown(keyCode, event);
			}
			return true;
			
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (bookLoader.isProgress) {
				return super.onKeyDown(keyCode, event);
			}
			if (bookLoader.curlType == BookInfoUtils.CURL_TYPE_CURLSIMPLE && mCurlView.getVisibility() == CurlView.VISIBLE) {
				Toast.makeText(this, 
						"切换到按钮翻页模式", 
						Toast.LENGTH_SHORT).show();
				progressLinearLayout.setVisibility(LinearLayout.GONE);
				bookLoader.isProgress = false;
				mCurlView.setVisibility(CurlView.GONE);
				simplePage.setVisibility(SimplePageView.VISIBLE);
				simplePage.setPage(bookLoader.curPage);
			} else if (bookLoader.useVolumeKey && simplePage.getVisibility() == SimplePageView.VISIBLE) {
				if (bookLoader.curPage < bookLoader.totalPage - 1) {
					bookLoader.setCurPage(bookLoader.curPage + 1);
					simplePage.setPage(bookLoader.curPage);
				}
			} else {
				return super.onKeyDown(keyCode, event);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return bookLoader.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		bookLoader.onOptionsItemSelected(item);
		return super.onOptionsItemSelected(item);
	}

	@Override
    protected Dialog onCreateDialog(int id) {
    	return bookLoader.onCreateDialog(id);
    }
	
	public void startSenForgroundService() {
		if (JkanjiSettingActivity.getSenService(this)) {
			startService(
					new Intent(this, JkanjiSenService.class)
						.setAction(JkanjiSenService.ACTION_FOREGROUND));
		}
	}
}
