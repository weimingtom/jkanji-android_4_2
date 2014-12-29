package com.iteye.weimingtom.jkanji;

import fi.harism.curl.CurlActivity;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.iteye.weimingtom.jkanji.R;

public class JkanjiAozoraService extends Service {
    public static final String ACTION_FOREGROUND = "com.iteye.weimingtom.jkanji.JkanjiAozoraService.ACTION_FOREGROUND";
    public static final String ACTION_STOP = "com.iteye.weimingtom.jkanji.JkanjiAozoraService.ACTION_STOP";

    private Notification notification;
    private PendingIntent contentIntent;
    
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
    
    @Override
    public void onStart(Intent intent, int startId) {
    	super.onStart(intent, startId);
        handleCommand(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent, startId);
        return START_STICKY;
    }
	
	private void handleCommand(Intent intent, int startId) {
    	if (intent != null) {
	        if (ACTION_FOREGROUND.equals(intent.getAction())) {
	        	if (notification == null) {
		    		Intent newIntent = new Intent(this, JkanjiMainMenuActivity.class)
		    			.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					notification = new Notification(R.drawable.book_notification, 
			        		"日语简易词典（阅读器）",
			        		System.currentTimeMillis());
					contentIntent = PendingIntent.getActivity(this, 0, newIntent, 0);
			        notification.setLatestEventInfo(this, 
			        		getText(R.string.app_name), 
			        		"日语简易词典（阅读器）", 
			        		contentIntent);
			        startForeground(DataContext.AOZORA_SERVICE_ID, notification);
		        }
	        } else if (ACTION_STOP.equals(intent.getAction())) {
	        	notification = null;
	        	stopForeground(true);
	        	stopSelf(startId);
	        }
    	}
    }
	
    @Override
    public void onDestroy() {
    	super.onDestroy();
    }
}
