package com.iteye.weimingtom.littlenanami;

import com.iteye.weimingtom.jkanji.R;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class LittleNanamiService extends Service {
	private final static boolean D = false;
	private final static String TAG = "LittleNanamiService";
	
	public static final String ACTION_START = "com.iteye.weimingtom.littlenanami.LittleNanamiService.ACTION_START";
	public static final String ACTION_STOP = "com.iteye.weimingtom.littlenanami.LittleNanamiService.ACTION_STOP";
	public static final String ACTION_TOUCH = "com.iteye.weimingtom.littlenanami.LittleNanamiService.ACTION_TOUCH";
	public static final String ACTION_CHANGE = "com.iteye.weimingtom.littlenanami.LittleNanamiService.ACTION_CHANGE";
	public static final String ACTION_KILL = "com.iteye.weimingtom.littlenanami.LittleNanamiService.ACTION_KILL";
	
	public static final String EXTRA_WIDGET_ID = "com.iteye.weimingtom.littlenanami.LittleNanamiService.EXTRA_WIDGET_ID";
	public static final String EXTRA_SCENARID = "com.iteye.weimingtom.littlenanami.LittleNanamiService.EXTRA_SCENARID";
	public static final String EXTRA_VISIBLE = "com.iteye.weimingtom.littlenanami.LittleNanamiService.EXTRA_VISIBLE";
	
	private int mRunning;
	
	private static Bitmap[] bitmaps;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		if (D) {
			Log.i(TAG, "onCreate");
		}
		super.onCreate();
		mRunning = 0;
		bitmaps = new Bitmap[CommonSettings.SCENARIO_TABLE.length];
		for (int i = 0; i < bitmaps.length; i++) {
			bitmaps[i] = BitmapFactory.decodeResource(this.getResources(), CommonSettings.SCENARIO_TABLE[i]);
		}
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (intent != null) {
			String action = intent.getAction();
			int widgetid = intent.getIntExtra(EXTRA_WIDGET_ID, -1);
			if (ACTION_START.equals(action)) {
				if (D) {
					Log.d(TAG, "onStart ACTION_START : " + widgetid);
				}
				mRunning++;
				putScenario(widgetid, 0);
				updateScenario(this, widgetid, 0, true);
			} else if (ACTION_STOP.equals(action)) {
				if (mRunning > 0) {
					if (D) {
						Log.d(TAG, "onStart ACTION_STOP : " + widgetid);
					}
					mRunning--;
					if (mRunning == 0) {
						stopSelf(startId);
					}
				}
			} else if (ACTION_TOUCH.equals(action)) {
				int scenario = getScenario(widgetid);
				Rect rect = intent.getSourceBounds();
				/**
				 * @see http://stackoverflow.com/questions/5429686/how-to-get-the-position-of-the-appwidget
				 * @see https://github.com/raumzeitlabor/raumzeitstatus/blob/master/android-widget/src/org/raumzeitlabor/status/StatusProvider.java
				 */
				int rectLeft, rectTop, rectRight, rectBottom;
				rectLeft = rectTop = rectRight = rectBottom = 0;
				if (rect != null) {
					if (D) {
						Log.d(TAG, "onStart ACTION_TOUCH : " + rect.toShortString());
					}
					rectLeft = rect.left;
					rectTop = rect.top;
					rectRight = rect.right;
					rectBottom = rect.bottom;
				}
				if (false) {
					scenario++;
					if (scenario >= CommonSettings.SCENARIO_TABLE.length) {
						scenario = 0;
					}
				}
				if (D) {
					Log.i(TAG, "onStart ACTION_TOUCH : " + widgetid + "(," + scenario + ")");
				}
				putScenario(widgetid, scenario);
				updateScenario(this, widgetid, scenario, true);
				if (true) {
					startActivity(new Intent(this, LittleNanamiBubbleActivity.class)
						.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
						.putExtra(LittleNanamiBubbleActivity.EXTRA_BUBBLE_WIDGETID, widgetid)
						.putExtra(LittleNanamiBubbleActivity.EXTRA_BUBBLE_SCENARIO, scenario)
						.putExtra(LittleNanamiBubbleActivity.EXTRA_BUBBLE_LEFT, rectLeft)
						.putExtra(LittleNanamiBubbleActivity.EXTRA_BUBBLE_TOP, rectTop)
						.putExtra(LittleNanamiBubbleActivity.EXTRA_BUBBLE_RIGHT, rectRight)
						.putExtra(LittleNanamiBubbleActivity.EXTRA_BUBBLE_BOTTOM, rectBottom)
					);
				}
			} else if (ACTION_CHANGE.equals(action)) {
				int scenario = intent.getIntExtra(EXTRA_SCENARID, 0);
				boolean visible = intent.getBooleanExtra(EXTRA_VISIBLE, true);
				if (scenario >= 0 && scenario < CommonSettings.SCENARIO_TABLE.length) {
					if (D) {
						Log.i(TAG, "onStart ACTION_CHANGE : " + widgetid + "," + scenario);
					}
					putScenario(widgetid, scenario);
					updateScenario(this, widgetid, scenario, visible);
				}
			} else if (ACTION_KILL.equals(action)) {
				this.stopSelf(startId);
			}
		}
	}
	
	@Override
	public void onDestroy() {
		if (D) {
			Log.i(TAG, "onDestroy");
		}
		super.onDestroy();
		for (int i = 0; i < bitmaps.length; i++) {
			if (bitmaps[i] != null) {
				bitmaps[i].recycle();
			}
		}
	}
	
	public static void updateScenario(Context context, int widgetId, int scenario, boolean visible) {
		if (widgetId >= 0) {
			if (D) {
				Log.e(TAG, "updateSingleWidget " + widgetId);
			}
			int resId = CommonSettings.SCENARIO_TABLE[scenario];
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_nanami_layout);
			Intent intent = new Intent(context, LittleNanamiService.class)
				.setAction(ACTION_TOUCH)
				.putExtra(EXTRA_WIDGET_ID, widgetId);
			PendingIntent pendingIntent = PendingIntent.getService(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			if (pendingIntent != null) {
				if (D) {
					Log.e(TAG, "updateSingleWidget pendingIntent");
				}
			}
			views.setOnClickPendingIntent(R.id.imageView1, pendingIntent);
			if (true) {
				if (visible) {
//					views.setViewVisibility(R.id.imageView1, View.VISIBLE);
					views.setImageViewResource(R.id.imageView1, resId);
				} else {
					views.setImageViewResource(R.id.imageView1, 0);
//					views.setViewVisibility(R.id.imageView1, View.GONE);
				}
			} else {
				views.setImageViewBitmap(R.id.imageView1, bitmaps[scenario]);
			}
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			if (true) {
				appWidgetManager.updateAppWidget(widgetId, views);
			} else {
				appWidgetManager.updateAppWidget(
						new ComponentName(context, LittleNanamiAppWidgetProvider.class), 
						views);
			}
			//Toast.makeText(context, "updateScenario", Toast.LENGTH_SHORT).show();
		}
	}
	
	private int getScenario(int widgetid) {
		return ScenarioUtil.getScenario(this, CommonSettings.SCENARIO_FILE, widgetid, 0);
	}
	
	private void putScenario(int widgetid, int scenario) {
		if (widgetid >= 0) {
			ScenarioUtil.putScenario(this, CommonSettings.SCENARIO_FILE, widgetid, scenario);
		}
	}
}
