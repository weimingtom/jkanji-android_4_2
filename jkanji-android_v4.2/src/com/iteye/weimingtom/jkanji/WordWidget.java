package com.iteye.weimingtom.jkanji;

import java.io.IOException;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import com.iteye.weimingtom.jkanji.R;

public class WordWidget extends AppWidgetProvider {
    private static final boolean D = false;
	private static final String TAG = "WordWidget";
    public static final String ACTION_UPDATE = "com.iteye.weimingtom.jkanji.UPDATE_MY_WIDGET";
    
	@Override
	public void onReceive(Context context, Intent intent) {
		if (D) {
        	Log.d(TAG, "onReceive");
        }
		super.onReceive(context, intent);
		String action = intent.getAction();
        if (action.equals(ACTION_UPDATE)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WordWidget.class));
            if (appWidgetIds.length > 0) {
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }
	}

	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		if (D) {
        	Log.d(TAG, "onUpdate");
        }
		super.onUpdate(context, appWidgetManager, appWidgetIds);
    	final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
	
    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        appWidgetManager.updateAppWidget(appWidgetId, buildUpdate(context));
    }
    
    private static RemoteViews buildUpdate(Context context) {
    	String word_title = null;
    	String word_reading = null;
    	String word_definition = null;
		MersenneTwisterRandom mt = new MersenneTwisterRandom();
		mt.init_genrand((int)System.currentTimeMillis());
		int total = getCount(context);
		if (total > 0) {
			int id = mt.nextInt(0, total - 1);
			Word word = getWord(context, id);
			word_title = word.kanji;
			if (D) {
				Log.e(TAG, "word : " + word);
			}
			word_reading = word.reading;
			word_definition = word.mean;
			if (word_title == null) {
				word_title = word_reading;
			}
		}
        RemoteViews updateViews = null;
        updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_word);
        updateViews.setTextViewText(R.id.word_title, word_title);
        updateViews.setTextViewText(R.id.word_reading, word_reading);
        updateViews.setTextViewText(R.id.word_definition, word_definition);
        Intent defineIntent = 
        		//new Intent(Intent.ACTION_RUN).setClass(context, JkanjiMainMenu.class);
        		new Intent(ACTION_UPDATE);
        //PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, defineIntent, 0);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, defineIntent, 0);
        updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
        return updateViews;
    }
    
    private static int getCount(Context context) {
		try {
			return RandomDictLoader.getTotal(context.getAssets(), RandomDictLoader.TYPE_JPWORDS);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
    }
	
    private static Word getWord(Context context, int index) {
		try {
			return RandomDictLoader.getWord(context.getAssets(), index, RandomDictLoader.TYPE_JPWORDS);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
    }
}
