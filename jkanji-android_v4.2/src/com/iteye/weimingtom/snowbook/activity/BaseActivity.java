package com.iteye.weimingtom.snowbook.activity;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;

import com.iteye.weimingtom.jkanji.R;
import com.iteye.weimingtom.snowbook.util.BitmapDrawableLruCache;
import com.iteye.weimingtom.snowbook.util.RecyclingBitmapDrawableDebugHandler;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class BaseActivity extends SlidingFragmentActivity {
	private static final boolean D = false;
	private static final String TAG = "BaseActivity";
	
	protected ListFragment mFrag;

	private BitmapDrawableLruCache mMemoryCache;
	private static RecyclingBitmapDrawableDebugHandler recycleDebugHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		recycleDebugHandler = new RecyclingBitmapDrawableDebugHandler(this.getApplicationContext());
		mMemoryCache = new BitmapDrawableLruCache();
		
		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.snowbook_shadow_width);
		sm.setShadowDrawable(R.drawable.snowbook_shadow);
		sm.setBehindOffsetRes(R.dimen.snowbook_slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		
		//sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		
//		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		Drawable bg = getResources().getDrawable(R.drawable.actionbar_background);
//        getSupportActionBar().setBackgroundDrawable(bg);
    }
	
	public BitmapDrawableLruCache getBitmapDrawableLruCache() {
		return this.mMemoryCache;
	}
	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case ActionBarView.ID_HOME: //android.R.id.home:
//			toggle();
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (D) {
			Log.e(TAG, "onDestroy");
		}
		this.mMemoryCache.clearAll();
	}
	
	public void showRecycleDebug() {
		if (D) {
			recycleDebugHandler.sendEmptyMessageDelayed(0, 1000);
		}
	}
}
