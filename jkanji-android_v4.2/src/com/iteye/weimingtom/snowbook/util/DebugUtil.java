package com.iteye.weimingtom.snowbook.util;

import java.util.Map;

import android.util.Log;

public class DebugUtil {
	private static final boolean D = false;
	private static final String TAG = "DebugUtil";
	
	public static void printStack() {
		if (D) {
			Map<Thread, StackTraceElement[]> ts = Thread.getAllStackTraces();
	        StackTraceElement[] ste = ts.get(Thread.currentThread());
	        for (StackTraceElement s : ste) {
	             Log.e(TAG, s.toString()); 
	        }
		}
	}
}
