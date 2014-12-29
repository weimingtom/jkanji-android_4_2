package com.iteye.weimingtom.snowbook.util;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class RecyclingBitmapDrawableDebugHandler extends Handler {
	private static boolean D = false;
	private static String TAG = "RecyclingBitmapDrawableDebugHandler";
	
	private WeakReference<Context> mContext;
	
	public RecyclingBitmapDrawableDebugHandler(Context context) {
		mContext = new WeakReference<Context>(context);
	}
	
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		if (D) {
			Toast.makeText(mContext.get(), 
				"RecyclingBitmapDrawable.numInstance == " + RecyclingBitmapDrawable.numInstances, 
				Toast.LENGTH_SHORT).show();
			RecyclingBitmapDrawable.numInstances = 0;
		}
	}
}
