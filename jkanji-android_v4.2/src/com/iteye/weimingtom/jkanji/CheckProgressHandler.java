package com.iteye.weimingtom.jkanji;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CheckProgressHandler extends Handler {
	private final static boolean D = false;
	private final static String TAG = "CheckProgressHandler";
	
	public static final int STOP = 1;
	public static final int RUNNING = 2;
	
	private int state;
	
	public static interface ICheckProgress {
		public int getCurrentProgress();
		public int getMaxProgress();
	}
	
	private long delay;
	private ICheckProgress checker;
	
	public CheckProgressHandler(ICheckProgress checker, long delay) {
		this.checker = checker;
		this.delay = delay;
		this.state = STOP;
	}
	
	public void setState(int state) {
		this.state = state;
	}
	
	@Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.what == 0) {
        	if (this.state == RUNNING) {
				int cur = checker.getCurrentProgress();
				int max = checker.getMaxProgress();
				if (cur >= max) {
		        	if (D)
		        		Log.e(TAG, "CheckProgressHandler over");
		        	onTimer(cur, max);
		        } else {
					onTimer(cur, max);
		            this.sendEmptyMessageDelayed(0, delay);
		        }
	        }
        }
    }
	
	public boolean startTimer() {
		int cur = checker.getCurrentProgress();
		int max = checker.getMaxProgress();
		if (D)
			Log.e(TAG, "CheckProgressHandler:" + " now:" + cur + ", max:" + max);
		if (cur >= max) {
			onTimer(cur, max);
			if (D)
	    		Log.e(TAG, "CheckProgressHandler startTimer empty");
			return false;
		} else {
			if (D)
	    		Log.e(TAG, "CheckProgressHandler startTimer");
			this.sendEmptyMessage(0);
			this.setState(RUNNING);
			return true;
		}
	}
	
	protected void onTimer(int cur, int max) {
		if (D)
    		Log.e(TAG, "CheckProgressHandler onTimer:" + ((float)cur / max) * 100);
	}
}
