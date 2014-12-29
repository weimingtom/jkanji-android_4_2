package com.iteye.weimingtom.littlenanami;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LittleNanamiAppWidgetProvider extends AppWidgetProvider {
	private final static boolean D = false;
	private final static String TAG = "LittleNanamiAppWidgetProvider";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		for (int id : appWidgetIds) {
			if (D) {
				Log.i(TAG, "onUpdate id : " + id);
			}
			//LittleNanamiService.updateScenario(context, id, 0);
			context.startService(new Intent(context, LittleNanamiService.class)
					.setAction(LittleNanamiService.ACTION_START)
					.putExtra(LittleNanamiService.EXTRA_WIDGET_ID, id));
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		for (int id : appWidgetIds) {
			if (D) {
				Log.i(TAG, "onDeleted id : " + id);
			}
			context.startService(new Intent(context, LittleNanamiService.class)
					.setAction(LittleNanamiService.ACTION_STOP)
					.putExtra(LittleNanamiService.EXTRA_WIDGET_ID, id));
		}
		super.onDeleted(context, appWidgetIds);
	}
	
	@Override
	public void onDisabled(Context context) {
		if (D) {
			Log.i(TAG, "onDisabled");
		}
		super.onDisabled(context);
		ScenarioUtil.clearAllScenario(context, CommonSettings.SCENARIO_FILE);
		context.startService(new Intent(context, LittleNanamiService.class)
			.setAction(LittleNanamiService.ACTION_KILL));
	}

	@Override
	public void onEnabled(Context context) {
		if (D) {
			Log.i(TAG, "onEnabled");
		}
		super.onEnabled(context);
	}
}
